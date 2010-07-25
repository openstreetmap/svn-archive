#define __USE_LARGEFILE64
#include "bzhack.h"
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <stdio.h>
#include <string.h>

int main() {
   FILE *f, *fplanet;
   struct stat s;
   int id;
   unsigned long long filepos;
   char buf[7];
   f = fopen("/media/esata/2000/bz2nodeindex","r");
   fplanet = fopen("/media/esata/2000/planet-100717.osm.bz2","r");
   fstat(fileno(f), &s);
//   fseek(f, sizeof(struct bz2index), SEEK_SET);
   while (1) {
      int off = 0;
      fread(&id, sizeof(id), 1, f);
      fread(&filepos, sizeof(filepos), 1, f);
      printf("%i %llu\n", id, filepos);
      fseek(fplanet, filepos, SEEK_SET);
      fread(&buf, 7, 1, fplanet);

      if (strncmp("\x31\x41\x59\x26\x53", buf, 5) == 0) off = 1;
      else if (strncmp("\x62\x82\xB2\x4C\xA6", buf, 5) == 0) off = 2;
      else if (strncmp("\xC5\x05\x64\x99\x4D", buf, 5) == 0) off = 3;
      else if (strncmp("\x8A\x0A\xC9\x32\x9A", buf, 5) == 0) off = 4;
      else if (strncmp("\x14\x15\x92\x65\x35", buf, 5) == 0) off = 5;
      else if (strncmp("\x28\x2B\x24\xCA\x6B", buf, 5) == 0) off = 6;
      else if (strncmp("\x50\x56\x49\x94\xD6", buf, 5) == 0) off = 7;
      else if (strncmp("\xA0\xAC\x93\x29\xAC", buf, 5) == 0) off = 8;
      else if (strncmp("BZh4\x31", buf, 5) == 0) off = 9;
      printf("id: %i off:%i ftell: %llu value: %2hhx %2hhx %2hhx %2hhx %2hhx %2hhx %2hhx ftell: %li\n",
         id, off, filepos, buf[0], buf[1], buf[2], buf[3], buf[4], buf[5], buf[6], ftell(f));
      if (off == 0) {
         printf("%s\n", buf);
         break;
      }
      if (feof(f)) break;
   }
   return 0;
}

