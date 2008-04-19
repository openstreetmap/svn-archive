/* Implements the mid-layer processing for osm2pgsql
 * using several PostgreSQL tables
 * 
 * This layer stores data read in from the planet.osm file
 * and is then read by the backend processing code to
 * emit the final geometry-enabled output formats
*/
 
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>

#include <libpq-fe.h>

#include "osmtypes.h"
#include "middle.h"
#include "middle-pgsql.h"
#include "output-pgsql.h"
#include "pgsql.h"

enum table_id {
    t_node, t_way
} ;

struct table_desc {
    //enum table_id table;
    const char *name;
    const char *start;
    const char *create;
    const char *prepare;
    const char *copy;
    const char *analyze;
    const char *stop;

    int copyMode;    /* True if we are in copy mode */
};

static struct table_desc tables [] = {
    { 
        //table: t_node,
         name: "%s_nodes",
        start: "BEGIN;\n",
       create: "CREATE TABLE %s_nodes (id int4 PRIMARY KEY, lat double precision not null, lon double precision not null, tags text[]);\n",
      prepare: "PREPARE insert_node (int4, double precision, double precision, text[]) AS INSERT INTO %s_nodes VALUES ($1,$2,$3);\n"
               "PREPARE get_node (int4) AS SELECT lat,lon,tags FROM %s_nodes WHERE id = $1 LIMIT 1;\n",
         copy: "COPY %s_nodes FROM STDIN;\n",
      analyze: "ANALYZE %s_nodes;\n",
         stop: "COMMIT;\n"
    },
    { 
        //table: t_way,
         name: "%s_ways",
        start: "BEGIN;\n",
       create: "CREATE TABLE %s_ways (id int4 PRIMARY KEY, nodes int4[] not null, tags text[], pending boolean not null);\n"
               "CREATE INDEX %s_ways_idx ON %s_ways (id);\n",
      prepare: "PREPARE insert_way (int4, int4[], text[], boolean) AS INSERT INTO %s_ways VALUES ($1,$2,$3,$4);\n"
               "PREPARE get_way (int4) AS SELECT nodes, tags, array_upper(nodes,1) FROM %s_ways WHERE id = $1;\n"
               "PREPARE way_done(int4) AS UPDATE %s_ways SET pending = false WHERE id = $1;\n"
               "PREPARE pending_ways AS SELECT id FROM %s_ways WHERE pending;\n",
         copy: "COPY %s_ways FROM STDIN;\n",
      analyze: "ANALYZE %s_ways;\n",
         stop:  "COMMIT;\n"
    }
};

static int num_tables = sizeof(tables)/sizeof(tables[0]);
static PGconn **sql_conns;

/* Here we use a similar storage structure as middle-ram, except we allow
 * the array to be lossy so we can cap the total memory usage. Hence it is a
 * combination of a sparse array with a priority queue
 *
 * Like middle-ram we have a number of blocks all storing PER_BLOCK
 * ramNodes. However, here we also track the number of nodes in each block.
 * Seperately we have a priority queue like structure when maintains a list
 * of all the used block so we can easily find the block with the least
 * nodes. The cache has two phases:
 *
 * Phase 1: Loading initially, usedBlocks < maxBlocks. In this case when a
 * new block is needed we simply allocate it and put it in
 * queue[usedBlocks-1] which is the bottom of the tree. Every node added
 * increases it's usage. When we move onto the next block we percolate this
 * block up the queue until it reaches its correct position. The invariant
 * is that the priority tree is complete except for this last node. We do
 * not permit adding nodes to any other block to preserve this invariant.
 *
 * Phase 2: Once we've reached the maximum number of blocks permitted, we
 * change so that the block currently be inserted into is at the top of the
 * tree. When a new block is needed we take the one at the end of the queue,
 * as it is the one with the least number of nodes in it. When we move onto
 * the next block we first push the just completed block down to it's
 * correct position in the queue and then reuse the block that now at the
 * head.
 *
 * The result being that at any moment we have in memory the top maxBlock
 * blocks in terms of number of nodes in memory. This should maximize the
 * number of hits in lookups.
 *
 * Complexity:
 *  Insert node: O(1)
 *  Lookup node: O(1)
 *  Add new block: O(log usedBlocks)
 *  Reuse old block: O(log maxBlocks)
 */

/* Store +-20,000km Mercator co-ordinates as fixed point 32bit number with maximum precision */
/* Scale is chosen such that 40,000 * SCALE < 2^32          */
#define FIXED_POINT

static int scale = 100;
#define DOUBLE_TO_FIX(x) ((x) * scale)
#define FIX_TO_DOUBLE(x) (((double)x) / scale)

struct ramNode {
#ifdef FIXED_POINT
    int lon;
    int lat;
#else
    double lon;
    double lat;
#endif
};

struct ramNodeBlock {
  struct ramNode    *nodes;
  int used;
};

#define BLOCK_SHIFT 10
#define PER_BLOCK  (1 << BLOCK_SHIFT)
#define NUM_BLOCKS (1 << (32 - BLOCK_SHIFT))

static struct ramNodeBlock blocks[NUM_BLOCKS];
static int usedBlocks;
/* Note: maxBlocks *must* be odd, to make sure the priority queue has no nodes with only one child */
static int maxBlocks = 0;
static struct ramNodeBlock **queue;
static int storedNodes, totalNodes;
int nodesCacheHits, nodesCacheLookups;

static inline int id2block(int id)
{
    // + NUM_BLOCKS/2 allows for negative IDs
    return (id >> BLOCK_SHIFT) + NUM_BLOCKS/2;
}

static inline int id2offset(int id)
{
    return id & (PER_BLOCK-1);
}

static inline int block2id(int block, int offset)
{
    return ((block - NUM_BLOCKS/2) << BLOCK_SHIFT) + offset;
}

#define Swap(a,b) { typeof(a) __tmp = a; a = b; b = __tmp; }
static void percolate_up( int pos )
{
    int i = pos;
    while( i > 0 )
    {
      int parent = (i-1)>>1;
      if( queue[i]->used < queue[parent]->used )
      {
        Swap( queue[i], queue[parent] );
        i = parent;
      }
      else
        break;
    }
}

#define __unused  __attribute__ ((unused))
static int pgsql_ram_nodes_set(int id, double lat, double lon, struct keyval *tags __unused)
{
    int block  = id2block(id);
    int offset = id2offset(id);
    
    totalNodes++;

    if (!blocks[block].nodes) {
        if( usedBlocks < maxBlocks )
        {
          /* We've just finished with the previous block, so we need to percolate it up the queue to its correct position */
          if( usedBlocks > 0 )
            /* Upto log(usedBlocks) iterations */
            percolate_up( usedBlocks-1 );

          blocks[block].nodes = calloc(PER_BLOCK, sizeof(struct ramNode));
          blocks[block].used = 0;
          if (!blocks[block].nodes) {
              fprintf(stderr, "Error allocating nodes\n");
              exit_nicely();
          }
          queue[usedBlocks] = &blocks[block];
          usedBlocks++;

          /* If we've just used up the last possible block we enter the
           * transition and we change the invariant. To do this we percolate
           * the newly allocated block straight to the head */
          if( usedBlocks == maxBlocks )
            percolate_up( usedBlocks-1 );
        }
        else
        {
          /* We've reached the maximum number of blocks, so now we push the
           * current head of the tree down to the right level to restore the
           * priority queue invariant. Upto log(maxBlocks) iterations */
          
          int i=0;
          while( 2*i+1 < maxBlocks )
          {
            if( queue[2*i+1]->used <= queue[2*i+2]->used )
            {
              if( queue[i]->used > queue[2*i+1]->used )
              {
                Swap( queue[i], queue[2*i+1] );
                i = 2*i+1;
              }
              else
                break;
            }
            else
            {
              if( queue[i]->used > queue[2*i+2]->used )
              {
                Swap( queue[i], queue[2*i+2] );
                i = 2*i+2;
              }
              else
                break;
            }
          }
          /* Now the head of the queue is the smallest, so it becomes our replacement candidate */
          blocks[block].nodes = queue[0]->nodes;
          blocks[block].used = 0;
          memset( blocks[block].nodes, 0, PER_BLOCK * sizeof(struct ramNode) );
          
          /* Clear old head block and point to new block */
          storedNodes -= queue[0]->used;
          queue[0]->nodes = NULL;
          queue[0]->used = 0;
          queue[0] = &blocks[block];
        }
    }
    else
    {
      /* Insert into an existing block. We can't allow this in general or it
       * will break the invariant. However, it will work fine if all the
       * nodes come in numerical order, which is the common case */
      
      int expectedpos;
      if( usedBlocks < maxBlocks )
        expectedpos = usedBlocks-1;
      else
        expectedpos = 0;
        
      if( queue[expectedpos] != &blocks[block] )
      {
        fprintf( stderr, "Out of order node %d (%d,%d)\n", id, block, offset );
        return 1;
      }
    }
        
#ifdef FIXED_POINT
    blocks[block].nodes[offset].lat = DOUBLE_TO_FIX(lat);
    blocks[block].nodes[offset].lon = DOUBLE_TO_FIX(lon);
#else
    blocks[block].nodes[offset].lat = lat;
    blocks[block].nodes[offset].lon = lon;
#endif
    blocks[block].used++;
    storedNodes++;
    return 0;
}


int pgsql_ram_nodes_get(struct osmNode *out, int id)
{
    int block  = id2block(id);
    int offset = id2offset(id);
    nodesCacheLookups++;

    if (!blocks[block].nodes)
        return 1;

    if (!blocks[block].nodes[offset].lat && !blocks[block].nodes[offset].lon)
        return 1;

#ifdef FIXED_POINT
    out->lat = FIX_TO_DOUBLE(blocks[block].nodes[offset].lat);
    out->lon = FIX_TO_DOUBLE(blocks[block].nodes[offset].lon);
#else
    out->lat = blocks[block].nodes[offset].lat;
    out->lon = blocks[block].nodes[offset].lon;
#endif
    nodesCacheHits++;
    return 0;
}

static void pgsql_cleanup(void)
{
    int i;

    if (!sql_conns)
           return;

    for (i=0; i<num_tables; i++) {
        if (sql_conns[i]) {
            PQfinish(sql_conns[i]);
            sql_conns[i] = NULL;
        }
    }
}

char *pgsql_store_nodes(int *nds, int nd_count)
{
  static char *buffer;
  static int buflen;

  char *ptr;
  int i, first;
    
  if( buflen <= nd_count * 10 )
  {
    buflen = ((nd_count * 10) | 4095) + 1;  /* Round up to next page */
    buffer = realloc( buffer, buflen );
  }
_restart:

  ptr = buffer;
  first = 1;
  *ptr++ = '{';
  for( i=0; i<nd_count; i++ )
  {
    if( !first ) *ptr++ = ',';
    ptr += sprintf( ptr, "%d", nds[i] );
    
    if( (ptr-buffer) > (buflen-20) ) /* Almost overflowed? */
    {
      buflen <<= 1;
      buffer = realloc( buffer, buflen );
      
      goto _restart;
    }
    first = 0;
  }
  
  *ptr++ = '}';
  *ptr++ = 0;
  
  return buffer;
}

/* Special escape routine for escaping strings in array constants: double quote, backslash,newline, tab*/
static inline char *escape_tag( char *ptr, const char *in )
{
  while( *in )
  {
    switch(*in)
    {
      case '"':
        *ptr++ = '\\';
        *ptr++ = '\\';
        *ptr++ = '"';
        break;
      case '\\':
        *ptr++ = '\\';
        *ptr++ = '\\';
        *ptr++ = '\\';
        *ptr++ = '\\';
        break;
      case '\n':
        *ptr++ = '\\';
        *ptr++ = '\\';
        *ptr++ = 'n';
        break;
      case '\t':
        *ptr++ = '\\';
        *ptr++ = '\\';
        *ptr++ = 't';
        break;
      default:
        *ptr++ = *in;
        break;
    }
    in++;
  }
  return ptr;
}

char *pgsql_store_tags(struct keyval *tags)
{
  static char *buffer;
  static int buflen;

  char *ptr;
  struct keyval *i;
  int first;
    
  int countlist = countList(tags);
  if( countlist == 0 )
    return "\\N";
    
  if( buflen <= countlist * 24 ) /* LE so 0 always matches */
  {
    buflen = ((countlist * 24) | 4095) + 1;  /* Round up to next page */
    buffer = realloc( buffer, buflen );
  }
_restart:

  ptr = buffer;
  first = 1;
  *ptr++ = '{';
  /* The lists are circular, exit when we reach the head again */
  for( i=tags->next; i->key; i = i->next )
  {
    int maxlen = (strlen(i->key) + strlen(i->value)) * 4;
    if( (ptr+maxlen-buffer) > (buflen-20) ) /* Almost overflowed? */
    {
      buflen <<= 1;
      buffer = realloc( buffer, buflen );
      
      goto _restart;
    }
    if( !first ) *ptr++ = ',';
    *ptr++ = '"';
    ptr = escape_tag( ptr, i->key );
    *ptr++ = '"';
    *ptr++ = ',';
    *ptr++ = '"';
    ptr = escape_tag( ptr, i->value );
    *ptr++ = '"';
    
    first=0;
  }
  
  *ptr++ = '}';
  *ptr++ = 0;
  
  return buffer;
}

/* Decodes a portion of an array literal from postgres */
/* Argument should point to beginning of literal, on return points to delimiter */
static const char *decode_upto( const char *src, char *dst )
{
  int quoted = (*src == '"');
  if( quoted ) src++;
  
  while( quoted ? (*src != '"') : (*src != ',' && *src != '}') )
  {
    if( *src == '\\' )
    {
      switch( src[1] )
      {
        case 'n': *dst++ = '\n'; break;
        case 't': *dst++ = '\t'; break;
        default: *dst++ = src[1]; break;
      }
      src+=2;
    }
    else
      *dst++ = *src++;
  }
  if( quoted ) src++;
  *dst = 0;
  return src;
}

static void pgsql_parse_tags( const char *string, struct keyval *tags )
{
  char key[1024];
  char val[1024];
  
  if( *string == '\0' )
    return;
    
//  fprintf( stderr, "Parsing: %s\n", string );
  if( *string++ != '{' )
    return;
  while( *string != '}' )
  {
    string = decode_upto( string, key );
    /* String points to the comma */
    string++;
    string = decode_upto( string, val );
    /* String points to the comma or closing '}' */
    addItem( tags, key, val, 0 );
//    fprintf( stderr, "Extracted item: %s=%s\n", key, val );
    if( *string == ',' )
      string++;
  }
}

/* Parses an array of integers */
static void pgsql_parse_nodes( const char *src, int *nds, int nd_count )
{
  int count = 0;
  const char *string = src;
  
  if( *string++ != '{' )
    return;
  while( *string != '}' )
  {
    char *ptr;
    nds[count] = strtol( string, &ptr, 10 );
    string = ptr;
    if( *string == ',' )
      string++;
    count++;
  }
  if( count != nd_count )
  {
    fprintf( stderr, "parse_nodes problem: '%s' expected %d got %d\n", src, nd_count, count );
    exit_nicely();
  }
}

int pgsql_endCopy( enum table_id i )
{
    /* Terminate any pending COPY */
     if (tables[i].copyMode) {
        PGconn *sql_conn = sql_conns[i];
        int stop = PQputCopyEnd(sql_conn, NULL);
        if (stop != 1) {
            fprintf(stderr, "COPY_END for %s failed: %s\n", tables[i].copy, PQerrorMessage(sql_conn));
            exit_nicely();
        }

        PGresult *res = PQgetResult(sql_conn);
        if (PQresultStatus(res) != PGRES_COMMAND_OK) {
            fprintf(stderr, "COPY_END for %s failed: %s\n", tables[i].copy, PQerrorMessage(sql_conn));
            PQclear(res);
            exit_nicely();
        }
        PQclear(res);
        if (tables[i].analyze) {
            pgsql_exec(sql_conn, tables[i].analyze, PGRES_COMMAND_OK);
        }
        tables[i].copyMode = 0;
    }
    return 0;
}

static int pgsql_nodes_set(int id, double lat, double lon, struct keyval *tags)
{
    /* Four params: id, lat, lon, tags */
    char *paramValues[4];
    char *buffer;

    pgsql_ram_nodes_set( id, lat, lon, tags );
    if( tables[t_node].copyMode )
    {
      char *tag_buf = pgsql_store_tags(tags);
      int length = strlen(tag_buf) + 64;
      buffer = alloca( length );
      
      if( snprintf( buffer, length, "%d\t%.10f\t%.10f\t%s\n", id, lat, lon, pgsql_store_tags(tags) ) > (length-10) )
      { fprintf( stderr, "buffer overflow node id %d\n", id); return 1; }
      return pgsql_CopyData(__FUNCTION__, sql_conns[t_node], buffer);
    }
    buffer = alloca(64);
    paramValues[0] = buffer;
    paramValues[1] = paramValues[0] + sprintf( paramValues[0], "%d", id ) + 1;
    paramValues[2] = paramValues[1] + sprintf( paramValues[1], "%.10f", lat ) + 1;
    sprintf( paramValues[2], "%.10f", lon );

    paramValues[3] = pgsql_store_tags(tags);
    pgsql_execPrepared(sql_conns[t_node], "insert_node", 4, (const char * const *)paramValues, PGRES_COMMAND_OK);
    return 0;
}


static int pgsql_nodes_get(struct osmNode *out, int id)
{
    /* Check cache first */
    if( pgsql_ram_nodes_get( out, id ) == 0 )
      return 0;
      
    PGresult   *res;
    char tmp[16];
    char const *paramValues[1];
    PGconn *sql_conn = sql_conns[t_node];

    /* Make sure we're out of copy mode */
    pgsql_endCopy( t_node );

    snprintf(tmp, sizeof(tmp), "%d", id);
    paramValues[0] = tmp;
 
    res = pgsql_execPrepared(sql_conn, "get_node", 1, paramValues, PGRES_TUPLES_OK);

    if (PQntuples(res) != 1) {
        PQclear(res);
        return 1;
    } 

    out->lat = strtod(PQgetvalue(res, 0, 0), NULL);
    out->lon = strtod(PQgetvalue(res, 0, 1), NULL);
    PQclear(res);
    return 0;
}

/* This should be made more efficient by using an IN(ARRAY[]) construct */
static int pgsql_nodes_get_list(struct osmNode *nodes, int *ndids, int nd_count)
{
    int count = 0, i;
    for( i=0; i<nd_count; i++ )
    {
      if( pgsql_nodes_get( &nodes[count], ndids[i] ) == 0 )
        count++;
    }
    return count;
}

static int pgsql_ways_set(int way_id, int *nds, int nd_count, struct keyval *tags, int pending)
{
    /* Three params: id, nodes, tags, pending */
    char *paramValues[4];
    char *buffer;

    if( tables[t_way].copyMode )
    {
      char *tag_buf = pgsql_store_tags(tags);
      char *node_buf = pgsql_store_nodes(nds, nd_count);
      int length = strlen(tag_buf) + strlen(node_buf) + 64;
      buffer = alloca(length);
      if( snprintf( buffer, length, "%d\t%s\t%s\t%c\n", 
              way_id, pgsql_store_nodes(nds, nd_count), pgsql_store_tags(tags), pending?'t':'f' ) > (length-10) )
      { fprintf( stderr, "buffer overflow way id %d\n", way_id); return 1; }
      return pgsql_CopyData(__FUNCTION__, sql_conns[t_way], buffer);
    }
    buffer = alloca(64);
    paramValues[0] = buffer;
    paramValues[3] = paramValues[0] + sprintf( paramValues[0], "%d", way_id ) + 1;
    sprintf( paramValues[3], "%c", pending?'t':'f' );
    paramValues[1] = pgsql_store_nodes(nds, nd_count);
    paramValues[2] = pgsql_store_tags(tags);
    pgsql_execPrepared(sql_conns[t_way], "insert_way", 4, (const char * const *)paramValues, PGRES_COMMAND_OK);
    return 0;
}

/* Caller is responsible for freeing nodesptr & resetList(tags) */
static int pgsql_ways_get(int id, struct keyval *tags, struct osmNode **nodes_ptr, int *count_ptr)
{
    PGresult   *res;
    char tmp[16];
    char const *paramValues[1];
    PGconn *sql_conn = sql_conns[t_way];

    /* Make sure we're out of copy mode */
    pgsql_endCopy( t_way );

    snprintf(tmp, sizeof(tmp), "%d", id);
    paramValues[0] = tmp;
 
    res = pgsql_execPrepared(sql_conn, "get_way", 1, paramValues, PGRES_TUPLES_OK);

    if (PQntuples(res) != 1) {
        PQclear(res);
        return 1;
    } 

    pgsql_parse_tags( PQgetvalue(res, 0, 1), tags );

    int num_nodes = strtol(PQgetvalue(res, 0, 2), NULL, 10);
    int *list = alloca( sizeof(int)*num_nodes );
    *nodes_ptr = malloc( sizeof(struct osmNode) * num_nodes );
    pgsql_parse_nodes( PQgetvalue(res, 0, 0), list, num_nodes);
    
    *count_ptr = pgsql_nodes_get_list( *nodes_ptr, list, num_nodes);
    PQclear(res);
    return 0;
}

static int pgsql_ways_done(int id)
{
    char tmp[16];
    char const *paramValues[1];
    PGconn *sql_conn = sql_conns[t_way];

    /* Make sure we're out of copy mode */
    pgsql_endCopy( t_way );

    snprintf(tmp, sizeof(tmp), "%d", id);
    paramValues[0] = tmp;
 
    pgsql_execPrepared(sql_conn, "way_done", 1, paramValues, PGRES_COMMAND_OK);

    return 0;
}

static void pgsql_iterate_ways(int (*callback)(int id, struct keyval *tags, struct osmNode *nodes, int count))
{
    PGresult   *res_ways;
    int i, count = 0;

    fprintf(stderr, "\nGoing over pending ways\n");

    res_ways = pgsql_execPrepared(sql_conns[t_way], "pending_ways", 0, NULL, PGRES_TUPLES_OK);

    //fprintf(stderr, "\nIterating ways\n");
    for (i = 0; i < PQntuples(res_ways); i++) {
        int id = strtol(PQgetvalue(res_ways, i, 0), NULL, 10);
        struct keyval tags;
        struct osmNode *nodes;
        int nd_count;

        if (count++ %1000 == 0)
                fprintf(stderr, "\rprocessing way (%dk)", count/1000);

        initList(&tags);
        if( pgsql_ways_get(id, &tags, &nodes, &nd_count) )
          continue;
          
        callback(id, &tags, nodes, nd_count);
        pgsql_ways_done( id );

        free(nodes);
        resetList(&tags);
    }

    PQclear(res_ways);
    fprintf(stderr, "\n");
}

static void pgsql_analyze(void)
{
    int i;

    for (i=0; i<num_tables; i++) {
        PGconn *sql_conn = sql_conns[i];
 
        if (tables[i].analyze) {
            pgsql_exec(sql_conn, tables[i].analyze, PGRES_COMMAND_OK );
        }
    }
}

static void pgsql_end(void)
{
    int i;

    for (i=0; i<num_tables; i++) {
        PGconn *sql_conn = sql_conns[i];
 
        // Commit transaction
        if (tables[i].stop) {
            pgsql_exec(sql_conn, tables[i].stop, PGRES_COMMAND_OK);
        }

    }
}

/* Replace %s with prefix */
static inline void set_prefix( const char *prefix, const char **string )
{
  char buffer[1024];
  sprintf( buffer, *string, prefix, prefix, prefix, prefix );
  *string = strdup( buffer );
}

static int pgsql_start(const struct output_options *options)
{
    char sql[2048];
    PGresult   *res;
    int i;
    int dropcreate = 1;

    scale = options->scale;
    
    /* How much we can fit, and make sure it's odd */
    maxBlocks = (((options->cache*1024*1024)) / (PER_BLOCK*sizeof(struct ramNode))) | 1;
    queue = malloc( maxBlocks * sizeof(struct ramNodeBlock) );    
    
    fprintf( stderr, "Mid: pgsql, scale=%d, cache=%dMB, maxblocks=%d*%d\n", scale, options->cache, maxBlocks, PER_BLOCK*sizeof(struct ramNode) ); 
    
    /* We use a connection per table to enable the use of COPY */
    sql_conns = calloc(num_tables, sizeof(PGconn *));
    assert(sql_conns);

    for (i=0; i<num_tables; i++) {
        PGconn *sql_conn;
                        
        set_prefix( options->prefix, &(tables[i].name) );
        set_prefix( options->prefix, &(tables[i].start) );
        set_prefix( options->prefix, &(tables[i].create) );
        set_prefix( options->prefix, &(tables[i].prepare) );
        set_prefix( options->prefix, &(tables[i].copy) );
        set_prefix( options->prefix, &(tables[i].analyze) );
        set_prefix( options->prefix, &(tables[i].stop) );

        fprintf(stderr, "Setting up table: %s\n", tables[i].name);
        sql_conn = PQconnectdb(options->conninfo);

        /* Check to see that the backend connection was successfully made */
        if (PQstatus(sql_conn) != CONNECTION_OK) {
            fprintf(stderr, "Connection to database failed: %s\n", PQerrorMessage(sql_conn));
            exit_nicely();
        }
        sql_conns[i] = sql_conn;

        if (dropcreate) {
            sql[0] = '\0';
            strcat(sql, "DROP TABLE ");
            strcat(sql, tables[i].name);
            res = PQexec(sql_conn, sql);
            PQclear(res); /* Will be an error if table does not exist */
        }

        if (tables[i].start) {
            pgsql_exec(sql_conn, tables[i].start, PGRES_COMMAND_OK);
        }

        if (dropcreate && tables[i].create) {
            pgsql_exec(sql_conn, tables[i].create, PGRES_COMMAND_OK);
        }

        if (tables[i].prepare) {
            pgsql_exec(sql_conn, tables[i].prepare, PGRES_COMMAND_OK);
        }

        if (tables[i].copy) {
            pgsql_exec(sql_conn, tables[i].copy, PGRES_COPY_IN);
            tables[i].copyMode = 1;
        }
    }

    return 0;
}

static void pgsql_stop(void)
{
    PGconn *sql_conn;
    int i;

    fprintf( stderr, "node cache: stored: %d(%.2f%%), storage efficiency: %.2f%%, hit rate: %.2f%%\n", 
             storedNodes, 100.0f*storedNodes/totalNodes, 100.0f*storedNodes/(usedBlocks*PER_BLOCK),
             100.0f*nodesCacheHits/nodesCacheLookups );
          
    for( i=0; i<usedBlocks; i++ )
    {
      free(queue[i]->nodes);
      queue[i]->nodes = NULL;
    }
    free(queue);
   
   for (i=0; i<num_tables; i++) {
        //fprintf(stderr, "Stopping table: %s\n", tables[i].name);
        pgsql_endCopy(i);
        sql_conn = sql_conns[i];
        if (tables[i].stop) {
            pgsql_exec(sql_conn, tables[i].stop, PGRES_COMMAND_OK);
        }
        PQfinish(sql_conn);
        sql_conns[i] = NULL;
    }
    free(sql_conns);
    sql_conns = NULL;
}
 
struct middle_t mid_pgsql = {
        start:          pgsql_start,
        stop:           pgsql_stop,
        cleanup:        pgsql_cleanup,
        analyze:        pgsql_analyze,
        end:            pgsql_end,
        nodes_set:      pgsql_nodes_set,
//        nodes_get:      pgsql_nodes_get,
        nodes_get_list:      pgsql_nodes_get_list,
        ways_set:       pgsql_ways_set,
        ways_get:       pgsql_ways_get,
        ways_done:      pgsql_ways_done,
//        iterate_nodes:  pgsql_iterate_nodes,
        iterate_ways:   pgsql_iterate_ways
};
