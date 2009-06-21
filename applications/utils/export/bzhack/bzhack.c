#define __USE_LARGEFILE64
#include <bzlib.h>
#include "bzlib_private.h"
#include <errno.h>
#include <xmlparse.h>
#include <math.h>
#include "bzhack.h"

FILE *fnodetile, *fbz2nodeindex, *fbz2wayindex, *fbz2changesetindex;

static int filepos = 0;

struct bz2index sbz2nodeindex, sbz2changesetindex;

void start_hndl(void *data, const char *el, const char **attr) {
   int i;
   if (strcmp(el, "node") == 0) {
      struct nodetile s;
      for (i = 0 ; ; i+=2 ) {
         if (attr[i] == 0) break;
         if (strcmp(attr[i], "id") == 0) {
            s.id = atoi(attr[i+1]);
         }
         if (strcmp(attr[i], "lon") == 0) {
            s.lon = (int)(floor((strtod(attr[i+1], NULL) + 180.0) / 360.0 * pow(2.0, 15))); // z = 15
         }
         if (strcmp(attr[i], "lat") == 0) {
            double latd = strtod(attr[i+1], NULL);
            s.lat = (int)(floor((1.0 - log( tan(latd * M_PI/180.0) + 1.0 / cos(latd * M_PI/180.0)) / M_PI) / 2.0 * pow(2.0, 15)));
         }
      }
      if (filepos) {
         sbz2nodeindex.id = s.id;
         sbz2nodeindex.filepos = filepos;
         fwrite(&sbz2nodeindex, sizeof(struct bz2index), 1, fbz2nodeindex);
         filepos = 0;
      }
      fwrite(&s, sizeof(struct nodetile), 1, fnodetile);
   } else if (strcmp(el, "way") == 0) {
      printf("whoei!\n");
   } else if (strcmp(el, "changeset") == 0) {
      if (filepos) {
         int id = 0;
         for (i = 0 ; ; i+=2 ) {
            if (attr[i] == 0) break;
            if (strcmp(attr[i], "id") == 0) {
               id = atoi(attr[i+1]);
            }
         }
         sbz2changesetindex.id = id;
         sbz2changesetindex.filepos = filepos;
         fwrite(&sbz2changesetindex, sizeof(struct bz2index), 1, fbz2changesetindex);
         filepos = 0;
      }
   }
}

void end_hndl(void *data, const char *el) {
   return;
}

int main() {
    FILE *f;
    char *buf;
    char bufo[500000];
    int ret;
    int i;
    int buf_size = 2000000;
    int avail_in = buf_size;
    int avail_out = sizeof(bufo);

    buf = malloc(avail_in);

    fnodetile = fopen("/media/esata/2000/nodetile", "w");
    fbz2nodeindex = fopen("/media/esata/2000/bz2nodeindex", "w");
    fbz2wayindex = fopen("/media/esata/2000/bz2wayindex", "w");
    fbz2changesetindex = fopen("/media/esata/2000/bz2changeset", "w");

    XML_Parser parser;
    bz_stream strm;

    parser = XML_ParserCreate("UTF-8");
    XML_SetElementHandler(parser, start_hndl, end_hndl);

    // initialize strm
    memset(&strm, 0, sizeof(strm));
    strm.next_in = buf;

    BZ2_bzDecompressInit(&strm, 0, 0);
    f = fopen("/media/esata/2000/planet-090608.osm.bz2", "r");
    if (!f) {
        printf("Kan planet niet openen");
        return 1;
    }

    // --- seek
    #define OFFSET(N) N ## 5
    //fseek(f, 16615273, SEEK_SET); // offset - 1
    fseek(f, 1191443766, SEEK_SET); // offset - 1
    ret = fread(buf+sizeof(OFFSET(sync_block_))-2, 1, 1, f);
    strm.avail_in = sizeof(OFFSET(sync_block_))-1;
    strm.avail_out = avail_out;
    strm.next_out = bufo;
    buf[sizeof(OFFSET(sync_block_))-2] &= ((1 << OFFSET()) - 1);
    buf[sizeof(OFFSET(sync_block_))-2] |= OFFSET(sync_block_)[sizeof(OFFSET(sync_block_))-2];

    memcpy(buf, OFFSET(sync_block_), sizeof(OFFSET(sync_block_))-2);
    ret = BZ2_bzDecompress(&strm);
    printf("ret: %i, avail_out: %i\n", ret, strm.avail_out);
    if (ret == BZ_STREAM_END) {
       ret = BZ2_bzDecompressEnd(&strm);
       ret = BZ2_bzDecompressInit(&strm, 0, 0);
       strm.next_in = "B";
       strm.avail_in = 1;
       BZ2_bzDecompress(&strm);
    } else {
       while (strm.avail_out == avail_out) {
          strm.avail_out = avail_out;
          strm.next_out = bufo;
          ret = BZ2_bzDecompress(&strm);
          if (ret != BZ_OK) {
             printf("failed\n");
             return 1;
          }
       }
    }
    
    //

    // fill next_in
    strm.avail_in = avail_in;
    strm.next_in = buf;
    ret = fread(strm.next_in, 1, avail_in, f);
    printf("read ret: %i\n", ret);

    for (i = 0; ; i++) {
       // refill buf when avail_in is getting below watermark
       if (avail_in < 1700000) {
          memmove(buf, strm.next_in, avail_in);
          ret = fread(buf + avail_in, 1, buf_size - avail_in, f);
          strm.next_in = buf;
          avail_in = buf_size;
          strm.avail_in = avail_in;
       }
       // set bz2 filepos
       filepos = ftello(f)-avail_in;

       // output debug data
       printf("pos: %li, block: %i, i: %i, off: %i\n", ftell(f)-avail_in, ((DState*)strm.state)->currBlockNo, i, ((DState*)strm.state)->bsLive);

       // Decompress first block
       strm.avail_out = 0;
       strm.avail_in = avail_in;
       ret = BZ2_bzDecompress(&strm);
       if (ret != BZ_OK) {
          ret = BZ2_bzDecompressEnd(&strm);
          ret = BZ2_bzDecompressInit(&strm, 0, 0);
          ret = BZ2_bzDecompress(&strm);
       }
       avail_in = strm.avail_in;
       if (ret == BZ_STREAM_END) {
          ret = BZ2_bzDecompressEnd(&strm);
          printf("DecompressEnd ret: %i\n", ret);
          ret = BZ2_bzDecompressInit(&strm, 0, 0);
          printf("DecompressInit ret: %i\n", ret);
       } else {
          // Output first block
          strm.avail_in = 0;
          strm.avail_out = avail_out;
          strm.next_out = bufo;
          ret = BZ2_bzDecompress(&strm);
          if (ret != BZ_OK) {
             printf("Output first block ret: %i\n", ret);
             return 1;
          }
          XML_Parse(parser, bufo, sizeof(bufo) - strm.avail_out, 0);

          // Output until end of block
          while (strm.avail_out == avail_out) {
             strm.avail_out = avail_out;
             strm.next_out = bufo;
             ret = BZ2_bzDecompress(&strm);
             if (ret != BZ_OK) {
                printf("Oops! ret: %i\n", ret);
                return 1;
             }
             XML_Parse(parser, bufo, sizeof(bufo) - strm.avail_out, 0);
          }
       }
       if (feof(f)) break;
    }
    return 0;
}

