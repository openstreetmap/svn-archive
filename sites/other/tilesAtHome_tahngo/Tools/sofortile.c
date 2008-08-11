/*                                                                          */
/*  sofortile to clean tiles@home tilesetfiles for openstreetmap.org        */
/*                                                                          */
/* Compile: gcc -o sofortile -O2 `Magick-config --cflags --ldflags --libs`\ */
/*            sofortile.c                                                   */
/*                                                                          */
/* Copyright (C) 2008 Johan Thelmén                                         */
/* This program is free software: you can redistribute it and/or modify     */
/* it under the terms of the GNU General Public License as published by     */
/* the Free Software Foundation, either version 3 of the License, or        */
/* (at your option) any later version.                                      */
/*                                                                          */
/*  This program is distributed in the hope that it will be useful,         */
/*  but WITHOUT ANY WARRANTY; without even the implied warranty of          */
/*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           */
/*  GNU General Public License for more details.                            */
/*                                                                          */
/*  You should have received a copy of the GNU General Public License       */
/*  along with this program.  If not, see <http://www.gnu.org/licenses/>.   */
/*                                                                          */

#include <sys/types.h>
#include <sys/stat.h>
#include <time.h>
#include <stdio.h>
#include <stdlib.h>
#include <strings.h>
#include <wand/MagickWand.h>

#define FILEVERSION 1
#define MIN_VALID_OFFSET 4
#define HEADERSIZE 8+1366*4

#define ThrowWandException(wand) \
{ char *description; ExceptionType  severity; \
  description=MagickGetException(wand,&severity); \
  (void) fprintf(stderr,"%s %s %lu %s\n",GetMagickModule(),description); \
  description=(char *) MagickRelinquishMemory(description); \
  exit(-1); \
}

int main(int argc, char *argv[])
{ struct stat sb;
  FILE *fp;

  if (argc != 2) { fprintf(stderr, "Usage: %s tilefile\n", argv[0]); exit(EXIT_FAILURE); }
  if (stat(argv[1],&sb) == -1) { perror("stat"); exit(EXIT_FAILURE); }
  if (sb.st_size < HEADERSIZE) { printf("tileset %s too small, %i!", argv[1], sb.st_size ); exit(EXIT_FAILURE); }
  printf("File size: %lld bytes\n", (long long) sb.st_size);
  if ((fp = fopen(argv[1], "r+")) == NULL)  { perror("Could not open file"); exit(EXIT_FAILURE); }  /* + */

  char *tileset;
  if ((tileset = malloc(sb.st_size + 4)) == NULL) {perror("Could not allocate memory"); exit(EXIT_FAILURE); }
  if (fread(tileset, sb.st_size,1, fp) != 1) { perror("reading tileset"); exit(EXIT_FAILURE); }
  if (tileset[0] != FILEVERSION) { printf("tilesetfile %s is our VERSION", argv[1]); fclose(fp); exit(EXIT_FAILURE); }

  MagickBooleanType status;
  MagickWand  *mw;
  MagickWandGenesis();
  mw = NewMagickWand();
  int i,nexti;
  unsigned int *startpos,*endpos;
  unsigned int nextend, nextstart = 5472,size;
  for (i=1;i <= 1365; i++)
  { startpos = (unsigned int *)&tileset[4*i+4]; 
    if (*startpos < MIN_VALID_OFFSET ) continue;
//    { switch (*startpos)
//      { case 0: printf("#%d sea\n",i); continue;
//        case 1: printf("#%d land\n",i); continue;
//        case 2: printf("#%d transparent\n",i); continue;
//        default: printf("#%d Error!!\n",i); continue;
//    } }

    for (nexti = i; nexti <= 1366; ++nexti)
    { endpos = (unsigned int *)&tileset[4*nexti+8];
      if (*endpos > MIN_VALID_OFFSET) break;
    }
    size = *endpos - *startpos;
//    if (size == 0) continue; /* Last tile ? */
    printf("#%d Nextstart:%d Start:%d Stop:%d Size:%d ", i, nextstart, *startpos, *endpos , size );
    status = MagickReadImageBlob( mw, &tileset[*startpos], size);
    if (status == MagickFalse) ThrowWandException(mw);

    unsigned long colors;
    colors = MagickGetImageColors(mw);
    char *color;
    printf("Colors:%d      \r",colors);
    if (colors == 1) /* Blank tile */
    { PixelWand *p_wand = NewPixelWand();
      status = MagickGetImagePixelColor(mw, 1 , 1 , p_wand);
      if (status == MagickFalse) ThrowWandException(mw);
      color = PixelGetColorAsString(p_wand);
      if ( strcmp(color, "rgb(181,214,241)") == 0) { *startpos=1;      /*printf("Color is sea\n");*/ }
      else if ( strcmp(color, "rgb(248,248,248)") == 0) { *startpos=2; /*printf("Color is land\n");*/ }
        else printf("Other single color is %s\n", color);  // Print other blank color
      p_wand = DestroyPixelWand( p_wand );
    }
    status = MagickRemoveImage(mw); if (status == MagickFalse) ThrowWandException(mw);

// nextend = *endpos;  /* Where the next tile is starting */
//     else /* Non changed tile */
    if ( *startpos > MIN_VALID_OFFSET ) /* Only if we handle a real tile */
    { if ( *startpos != nextstart)     /* Move tile to beginning and update index */
      { //printf("#%d Update startpos:%d nextstart:%d size:%d\n", i , *startpos, nextstart, size);
        bcopy(&tileset[*startpos], &tileset[nextstart], size); /*src dst*/
        *startpos = nextstart;      /* Update index for new nextstart for image */
        nextstart = *startpos + size; /* Update where to expect next image to start */
      }else nextstart = *endpos;    /* Tile in correct place update lastpos */
    } 
  } /* Next tile */

  /* Write last index entry, please verify */
  startpos = (unsigned int *) &tileset[4*1366+4];
  *startpos = nextstart;

  /* Time to write back the tileset*/
  rewind(fp);
  if (fwrite(tileset, nextstart,1, fp) != 1) perror("writing tileset"); /* exit with error another time */
  ftruncate(fileno(fp), nextstart);
  
  DestroyMagickWand(mw);
  MagickWandTerminus();
  fclose(fp);
  exit(EXIT_SUCCESS);
}
