/*
 * osmcut.c
 *
 * Cuts up osm data into a grid of individual files, each containing
 * an area of x by x degrees (with x variable).
 *
 * Usage:
 *   osmcut [--destination|-d outputdir] [--force|-f] 
 *          [--size|-s tilesize] input.osm
 *
 * Where tilesize is the length, in degrees, of a tile edge. This defaults
 * to 2.5 degrees.
 *
 * Each resulting file contains all nodes in the area, plus all ways
 * that reference any of the nodes.
 *
 * Relations are not yet supported (they are ignored on input).
 *
 * The following caveats apply:
 *
 * 1. A mapping table (node->tile) is stored in memory. You can compile
 *    this program with or without the USE_ARRAY flag to define the 
 *    type of storage:
 *    a) with USE_ARRAY - uses an array that consumes 2n bytes of memory
 *       where n is the highest node id present on input. At the time
 *       of writing, n is about 250 million which means the program needs
 *       500 MB of RAM. Memory usage does not depend on the actual number
 *       of nodes, just on the highest id.
 *    b) without USE_ARRAY - uses a hash table that consumes about 16n
 *       bytes of memory where n is the number of nodes present on input.
 *       This is slower than USE_ARRAY but means less memory usage for
 *       input files of (approx.) less than 30 million nodes.
 *
 * 2. The input file is read through mmap(), which depending on your OS
 *    might be unavailable or show strange memory usage numbers (on
 *    Linux, the program appears to gollbe up all available memory,
 *    presumable some mmap internal buffering). Which doesn't hurt
 *    since it works with less memory as well, it just uses what's there.
 *
 * 3. Output files are named by numbers, starting with 63240000. This
 *    was introduced by the original osmcut Java implementation. Files
 *    are stored in the path specified with the -d command line option.
 *
 * 4. Nodes are not copied to any output file if they have invalid 
 *    co-ordinates.
 *
 * 5. Ways are not copied to any output file if they contain one or more
 *    nodes that were not copied due to 4., or that were not present in 
 *    the input file.
 *
 * 6. If a way spans more than four tiles, it is only copied to the four
 *    tiles it touches first.
 *    
 * 7. The program needs to simultaneously open one file for each output
 *    tile that actually receives data. The program tries to increase the
 *    allowable number of open files to match the theoretical maximum 
 *    required for the given tile size (tile sizes less than 8 degrees
 *    mean a theoretical maximum of more than 1024 output tiles). You
 *    might have to run the program with root privileges (more specifically,
 *    the CAP_SYS_RESOURCE capability) for this to work. 
 *
 *    The program will abort if it cannot secure the required number of 
 *    file descriptors. However, depending on your input data most tiles
 *    will probably be empty, so it might be worth a try to run the program
 *    even if it cannot open the number of files that might theoretically be
 *    required. If you want to go ahead and run the program, use the --force
 *    option. It will abort later if it tries to open a file and hits the 
 *    limit.
 *
 * 8. The program does not use proper XML parsing. I have a version with 
 *    libxml2 support but that is painfully slow.
 *
 * 9. Nodes are first copied to the output tile they are on. In a second 
 *    pass, nodes will also be copied to up to three other tiles if they 
 *    happened to be on a way that touched these tiles. These "additional 
 *    nodes" will appear BEHIND the ways in the tile output files.
 *
 * Written by Frederik Ramm <frederik@remote.org>, public domain.
 */
#include <stdio.h>
#include <unistd.h>
#include <stdarg.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <getopt.h>
#include <errno.h>
#include <sys/time.h>
#include <sys/resource.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/mman.h>
#include <fcntl.h>
#include <glib.h>

#ifdef USE_ARRAY
#define NODE_STORAGE_INCREMENT (1<<20)
#define WAY_STORAGE_INCREMENT (1<<18)
unsigned short int *node_storage;
int node_storage_size;
unsigned short int *way_storage;
int way_storage_size;
#else
GHashTable *node_storage;
GHashTable *way_storage;
#endif

// node_additional_tiles holds information for nodes used by ways
// that cross a tile border. the key is the node id, and the value
// is 3 times sizeof(unsigned short int), containing up to three
// tile indexes to which the node still has to be written.
GHashTable *node_additional_tiles;

double tile_size=2.5;
int max_tile;
int *tiles;
char *outdir = ".";

void die(const char *fmt, ...)
{
    char *cpy;
    cpy = (char *) malloc(strlen(fmt) + 2);
    sprintf(cpy, "%s\n", fmt);
    va_list ap;
    va_start(ap, fmt);
    vfprintf(stderr, cpy, ap);
    va_end(ap);
    exit(1);
}
void warn(const char *fmt, ...)
{
    char *cpy;
    cpy = (char *) malloc(strlen(fmt) + 2);
    sprintf(cpy, "%s\n", fmt);
    va_list ap;
    va_start(ap, fmt);
    vfprintf(stderr, cpy, ap);
    va_end(ap);
    free(cpy);
}

void process_node(const char *start, const char **fileptr, gboolean pass2)
{
    const char *end = strchr(start, '>');
    double lat;
    double lon;
    int id;
    int properties = 0;
    int i;

    // step 1 - parse opening <node> tag

    *fileptr = strchr(start + 1, ' ');
    if (!*fileptr) die("XML parse error: node tag without attributes");
    while(*fileptr < end)
    {
        if (!strncmp((*fileptr) + 1, "lat=", 4))
        {
            *fileptr += 5;
            char delim = **fileptr;
            char *last = strchr(*fileptr + 1, delim);
            if (!last) die("XML parse error");
            lat = strtod(*fileptr + 1, NULL);
            *fileptr = last;
            if (++properties == 3) break;
        }
        else if (!strncmp(*fileptr + 1, "lon=", 4))
        {
            *fileptr += 5;
            char delim = **fileptr;
            char *last = strchr(*fileptr + 1, delim);
            if (!last) die("XML parse error");
            lon = strtod(*fileptr + 1, NULL);
            *fileptr = last;
            if (++properties == 3) break;
        }
        else if (!strncmp(*fileptr + 1, "id=", 3))
        {
            *fileptr += 4;
            char delim = **fileptr;
            char *last = strchr(*fileptr + 1, delim);
            if (!last) die("XML parse error");
            id = strtol(*fileptr + 1, NULL, 10);
            *fileptr = last;
            if (++properties == 3) break;
        }
        else
        {
            (*fileptr)++;
        }
        *fileptr = strchr(*fileptr + 1, ' ');
    }

    // step 2 - find matching end tag

    if (*(end-1) == '/')
    {
        // self-closing tag.
    }
    else
    {
        // search end tag.
        end = strstr(end, "</node>");
        if (!end) die("XML parse error, cannot find matching </node>");
        end += 6;
    }

    do { start--; } while(isspace(*start));

    // step 3 - find tile(s) for this node and store.
    
    // in pass 1, we only ever find one tile, computed from lat/lon.
    // in pass 2, we find up to 3 extra tiles.
    unsigned short int dummy[3] = { 0, 0, 0 }; // only first slot used in pass 1.
    unsigned short int *tile_ids = dummy;

    if (!pass2)
    {
        *tile_ids = (int) ((lat + 90) / tile_size) + 
            (int) ((lon + 180) / tile_size) * (int)(180 / tile_size) + 1;
        if (*tile_ids >= max_tile)
        {
            warn("node %d yields tile_id of %d when max expected was %d", 
                id, *tile_ids, max_tile-1);
#ifdef USE_ARRAY
            *(node_storage + id) = 0;
#endif
            return;
        }
        if (*tile_ids == 0)
        {
            warn("node %d yields tile_id 0 which cannot be processed by this implementation", 
                id);
#ifdef USE_ARRAY
            *(node_storage + id) = 0;
#endif
            return;
        }

#ifdef USE_ARRAY
        // grow array if required, store node in array.
        if (id >= node_storage_size)
        {
            int new_size = (id / NODE_STORAGE_INCREMENT + 1) * NODE_STORAGE_INCREMENT;
            unsigned short int *new_ptr = (unsigned short int *) realloc(node_storage, new_size * sizeof(unsigned short int));
            if (new_ptr == 0) die ("cannot allocate %d bytes of memory", new_size * sizeof(unsigned short int));
            node_storage = new_ptr;
            node_storage_size = new_size;
        }
        *(node_storage + id) = *tile_ids;
#else
        // stuff copies of key/value into hash table.
        int *idcopy = (int *) malloc(sizeof(int));
        *idcopy = id;
        unsigned short int *tid = (unsigned short int *) malloc(sizeof(unsigned short int));
        *tid = *tile_ids;
        g_hash_table_insert(node_storage, idcopy, tid);
#endif
    }
    else
    // step 3 (for pass 2) - find extra tiles for this node
    {
        tile_ids = g_hash_table_lookup(node_additional_tiles, &id);
    }

    // step 4 - copy node to proper out file(s) (create out file if required)
    
    if (tile_ids) 
    for (i=0; i<3; i++)
    {
        unsigned short int tile_id = *(tile_ids+i);
        if (tile_id == 0) break;
        if (tiles[tile_id] == 0)
        {
            char *opentag = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                "<osm generator=\"osmcut.c\" version=\"0.5\">\n";
            char outfile[255];
            int fd;
            snprintf(outfile, sizeof(outfile), "%s/%d", outdir, tile_id + 63240000);
            fd = open(outfile, O_CREAT|O_TRUNC|O_WRONLY, 0644);
            if (fd < 0)
            {
                die("cannot open %s: %s", outfile, strerror(errno));
            }
            tiles[tile_id] = fd;
            write(fd, opentag, strlen(opentag));
        }

        write(tiles[tile_id], start+1, end-start);
    }
    *fileptr = end+1;
}

// not normally called
int get_way_id(const char *start, const char *end)
{
    int id;
    int properties = 0;

    const char *x = strchr(start + 1, ' ');
    if (!x) return 0;
    while(x < end)
    {
        if (!strncmp(x+1, "id=", 3))
        {
            x += 4;
            char delim = *x;
            char *last = strchr(x + 1, delim);
            if (!last) return 0;
            return strtol(x + 1, NULL, 10);
            x=last;
        }
        else
        {
            x++;
        }
        x = strchr(x+1, ' ');
    }
    return 0;
}

void process_way(const char *start, const char **fileptr)
{
    const char *begin = *fileptr;
    const char *end = strchr(start, '>');
    int properties = 0;
    int tiles_used = 0;
    unsigned short int tile[4];
    int i;
    int j;

    memset(tile, 0, sizeof(tile));

    // step 1 - find matching end tag

    if (*(end-1) == '/')
    {
        // self-closing tag. way without content. strange! ignore.
        *fileptr = end+1;
        return;
    }
    
    // search end tag.
    *fileptr = end+1;
    end = strstr(end, "</way>");
    if (!end) die("XML parse error, cannot find matching </way>");

    // step 2 - process all <nd> elements, finding the relevant tiles
    
    while (1) 
    {
        char *nd = strchr(*fileptr, '<');
        if (!nd) die ("XML parse error, cannot find <nd> inside way #%d", get_way_id(start, end));
        if (nd == end) break;
        if (!strncmp(nd+1, "nd ref=", 7))
        {
            *fileptr = nd + 8;
            char delim = **fileptr;
            char *last = strchr(*fileptr + 1, delim);
            if (!last) die("XML parse error");
            int id = strtol(*fileptr + 1, NULL, 10);
            *fileptr = last;
#ifdef USE_ARRAY
            unsigned short int *tmp_tile = node_storage + id;
#else
            unsigned short int *tmp_tile = (unsigned short int *) g_hash_table_lookup(node_storage, &id);
#endif
            if (!tmp_tile || !*tmp_tile)
            {
                warn("not copying way %d as it contains un-mapped node %d", get_way_id(start, end), id);
                *fileptr = end+5; 
                return;
            }
            for (j=0; j<tiles_used; j++)
            {
                if (tile[j] == *tmp_tile)
                {
                    tmp_tile=0;
                    break;
                }
            }
            if (tmp_tile) 
            {
                if (tiles_used > 3) 
                {
                    warn("way %d uses more than 4 tiles, not fully processed", get_way_id(start, end));
                    continue;
                }
                tile[tiles_used++] = *tmp_tile;
            }
        }
        else
        {
            *fileptr = nd+1;
        }
    }

#if 0
    // step 3 - store tiles used for later relation processing

#ifdef USE_ARRAY
    // grow array if required, store node in array.
    if (id*4 >= way_storage_size)
    {
        int new_size = (id*4 / WAY_STORAGE_INCREMENT + 1) * WAY_STORAGE_INCREMENT;
        unsigned short int *new_ptr = (unsigned short int *) realloc(way_storage, new_size * sizeof(unsigned short int));
        if (new_ptr == 0) die ("cannot allocate %d bytes of memory", new_size * sizeof(unsigned short int));
        way_storage = new_ptr;
        way_storage_size = new_size;
    }
    memcpy(way_storage+id*4, tile, sizeof(tile));
#else
    // stuff copies of key/value into hash table.
    int *idcopy = (int *) malloc(sizeof(int));
    *idcopy = id;
    void tilecopy = malloc(sizeof(tile));
    memcpy(tilecopy, tile, sizeof(tile));
    g_hash_table_insert(way_storage, idcopy, tilecopy);
#endif
#endif

    // step 4 - write the way XML to each tile output file
    // (output files have already been opened when the nodes were written)
    
    do { start--; } while(isspace(*start));
    for (i=0; i<tiles_used; i++)
    {
        write(tiles[tile[i]], start+1, end-start+5);
    }

    // step 5 - if this way covered more than one tile, re-read the nodes
    // and remember their ID so we can later copy them 
    if (tiles_used > 1)
    {
        while (1) 
        {
            char *nd = strchr(begin, '<');
            if (!nd) die ("XML parse error, cannot find <nd> inside way #%d", get_way_id(start, end));
            if (nd == end) break;
            if (!strncmp(nd+1, "nd ref=", 7))
            {
                begin = nd + 8;
                char delim = *begin;
                char *last = strchr(begin + 1, delim);
                if (!last) die("XML parse error");
                int id = strtol(begin + 1, NULL, 10);
                begin = last;
#ifdef USE_ARRAY
                unsigned short int *tmp_tile = node_storage + id;
#else
                unsigned short int *tmp_tile = (unsigned short int *) g_hash_table_lookup(node_storage, &id);
#endif
                for (j=0; j<tiles_used; j++)
                {
                    if (tile[j] != *tmp_tile)
                    {
                        // might have to add this to the list 
                        unsigned short int *at = (unsigned short int *) g_hash_table_lookup(node_additional_tiles, &id);
                        if (!at)
                        {
                            // this node does not yet have extra tiles added.
                            unsigned short int *threevalues = (unsigned short int *) malloc(3 * sizeof (unsigned short int));
                            memset(threevalues, 0, 3 * sizeof (unsigned short int));
                            int *idcopy = (int *) malloc(sizeof(int));
                            *idcopy = id;
                            *threevalues = tile[j];
                            g_hash_table_insert(node_additional_tiles, idcopy, threevalues);
                        }
                        else
                        {
                            for (i = 0; i<3; i++)
                            {
                                if (*(at+i) == tile[j]) 
                                {
                                    // extra tile is already stored for this node.
                                    break;
                                }
                                else if (*(at+i) == 0)
                                {
                                    // store extra tile in first empty slot.
                                    *(at+i) = tile[j];
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            else
            {
                begin = nd+1;
            }
        }
    }
    end += 5;
    *fileptr = end+1;
}

int streamFile(char *filename) 
{
    struct stat buf;
    char *filedata;
    const char *current_file_ptr;
    int fd = open(filename, O_RDONLY); 
    if (fd < 0) die("cannot open %s: %s", filename, strerror(errno));
    if (fstat(fd, &buf) < 0) die("cannot fstat %s: %s", filename, strerror(errno));

    filedata = mmap(NULL, buf.st_size, PROT_READ, MAP_SHARED, fd, 0);
    if (filedata == MAP_FAILED) die("cannot mmap %s: %s", filename, strerror(errno));
    current_file_ptr = filedata;
    close(fd);

    while(1) 
    {
        const char *tag_open = strchr(current_file_ptr, '<');
        if (!tag_open) break;
        if (!strncmp(tag_open + 1, "node ", 5))
        {
            process_node(tag_open, &current_file_ptr, FALSE);
        }
        else if (!strncmp(tag_open + 1, "way ", 4))
        {
            process_way(tag_open, &current_file_ptr);
        }
        else if (!strncmp(tag_open + 1, "relation ", 9))
        {
            //process_relation(&tag_open, &current_file_ptr);
            current_file_ptr = tag_open + 1;
        }
        else
        {
            current_file_ptr = tag_open + 1;
        }
    }

    // second run, this time adding some nodes to extra tiles.
    current_file_ptr = filedata;

    while(1) 
    {
        const char *tag_open = strchr(current_file_ptr, '<');
        if (!tag_open) break;
        if (!strncmp(tag_open + 1, "node ", 5))
        {
            process_node(tag_open, &current_file_ptr, TRUE);
        }
        else if (!strncmp(tag_open + 1, "way ", 4))
        {
            break;
        }
        else
        {
            current_file_ptr = tag_open + 1;
        }
    }

    munmap(filedata, buf.st_size);
}

gboolean sint_equal(gconstpointer a, gconstpointer b)
{
    return !memcmp(a, b, sizeof(unsigned short int));
}

void usage()
{
    printf("osmcut.c - cut .osm files into tiles.\n");
    printf("Usage:\n");
    printf("osmcut [--destination|-d outputdir] [--size|-s tilesize] input.osm\n");
    printf("\n");
#ifdef USE_ARRAY
    printf("This version is compiled with array storage. Memory usage is\n");
    printf("proportional to the highest node ID in the input file. Use\n");
    printf("this if you want to process the whole planet.\n");
    printf("Otherwise, use the hash storage version for less memory consumption.\n");
#else
    printf("This version is compiled with hash storage. Memory usage is\n");
    printf("proportional to the number of nodes in the input file. Use\n");
    printf("this if you want to process files with less than 100 million\n");
    printf("nodes. Otherwise, use the array storage version.\n");
#endif
}

int main(int argc, char *argv[])
{
    int verbose=0;
    int force=0;
    int i;
    struct rlimit rlim;

    while (1) 
    {
        int c, option_index = 0;
        static struct option long_options[] = {
            {"verbose",  0, 0, 'v'},
            {"force",  0, 0, 'f'},
            {"destination",  1, 0, 'd'},
            {"size",  1, 0, 's'},
            {"help",     0, 0, 'h'},
            {0, 0, 0, 0}
        };

        c = getopt_long (argc, argv, "fhvd:s:", long_options, &option_index);
        if (c == -1)
            break;

        switch (c) {
            case 's': tile_size=strtod(optarg, NULL);  break;
            case 'v': verbose=1;  break;
            case 'f': force=1;  break;
            case 'd': outdir=optarg; break;
            case 'h':
            case '?':
            default:
                usage(argv[0]);
                exit(EXIT_FAILURE);
        }
    }

    max_tile = ((int) (360 / tile_size + 1) * ((int) (180 / tile_size)) + 1);
    if (max_tile > 1 << (8 * sizeof(unsigned short int)))
    {
        die("Specified tile size yields %d tiles but capacity is only %d on this architecture", 
            max_tile, 1 << (8 * sizeof(unsigned short int)));
    }

    if (getrlimit(RLIMIT_NOFILE, &rlim) == -1)
    {
        die("cannot read rlimit");
    }

    if (rlim.rlim_cur >= max_tile)
    {
        // ok, we have enough file handles
    }
    else
    {
        rlim.rlim_cur = max_tile + 20;
        if (rlim.rlim_cur > rlim.rlim_max)
        {
            rlim.rlim_max = rlim.rlim_cur;
        }
        if (setrlimit(RLIMIT_NOFILE, &rlim) == -1)
        {
            if (!force) die ("cannot increase file descriptor limit to %d; run with --force to ignore, or run as root", rlim.rlim_cur);
            warn ("cannot increase file descriptor limit to %d, continuing", rlim.rlim_cur);
        }
        else
        {
            printf("file descriptor limit increased to %d\n", rlim.rlim_cur);
        }
    }

#ifdef USE_ARRAY
    node_storage = (unsigned short int *) malloc(NODE_STORAGE_INCREMENT * sizeof(unsigned short int));
    node_storage_size = NODE_STORAGE_INCREMENT;
#if 0
    way_storage = (unsigned short int *) malloc(WAY_STORAGE_INCREMENT * sizeof(unsigned short int));
    way_storage_size = WAY_STORAGE_INCREMENT;
#endif
#else
    node_storage = g_hash_table_new(g_int_hash, sint_equal);
    way_storage = g_hash_table_new(g_int_hash, sint_equal);
#endif
    node_additional_tiles = g_hash_table_new(g_int_hash, sint_equal);
    tiles = (int *) malloc(max_tile * sizeof(int));
    for (i=0; i< max_tile; i++) tiles[i] = 0;

    while (optind < argc) 
    {
        streamFile(argv[optind++]);
    }

    for(i = 0; i < max_tile; i++)
    {
        if (tiles[i] > 0)
        {
            write(tiles[i], "\n</osm>\n", 8);
            close(tiles[i]);
        }
    }

    return 0;

}
