// Run this program with :
// gcc -Wall -lm density.c && ./a.exe <density4.txt >density.sh
// sudo apt-get install netpbm
// for n in *.pnm; do pnmtopng <$n >${n%.pnm}.png; done
// 
// The input to it is a list of OO Calc expressions (sums)
// Only the ranges are extracted. The actual calculations are ignored.
// First it will output the osmosis command for generating the extracts
// Then it will output a list of cells that are covered by none of the bboxes
// (if any). It is recommended to add ranges until all cells are covered.
// At the same time it will create and image map (density.html) and the
// associated images.
#include <stdio.h>
#include <string.h>
#include <math.h>

//#include "density.xbm"

#define DIM 1024
int col[2], row[2], c, se = 0, cov[DIM][DIM], i, j, m, mat[DIM][DIM];
int hshrink, vshrink, bcnt[2] = { 0, 0 };
char block[2][98765], *bptr[2] = { block[0], block[1] };

#define N2(x) ((x) ? (x) + 'A' - 1 : '0')
int main (void)
{
  char fname[30];
  FILE *pnm, *html, *html2, *gosm = fopen ("bboxes.c", "w");
  memset (cov, 0, sizeof (cov));
  memset (mat, 0, sizeof (mat));
  html= fopen ("density.html", "w");
  fprintf (html, "\
<html xmlns=\"http://www.w3.org/1999/xhtml\">\n\
    <head>\n\
        <title>Gosmore Map Selection</title>\n\
        <script src=\"http://www.openlayers.org/api/OpenLayers.js\"></script>\n\
        <script src=\"http://www.openstreetmap.org/openlayers/OpenStreetMap.js\"></script>\n\
        <script type=\"text/javascript\">\n\
            var box_extents = [\n");
  while (scanf ("%d %d %d %d\n", &col[0], &row[0], &col[1], &row[1]) > 0) {
    if (1) {
      fprintf (gosm, "{ %d, %d, %d, %d },\n", col[0] - 512, row[0] - 512,
        col[1] - 512, row[1] - 512);
      sprintf (fname, "%04d%04d%04d%04d", col[0], row[0],
               col[1], row[1]);
      fprintf (html, "[ %10.5lf, %10.5lf, %10.5lf, %10.5lf, \"%s\" ],\n",
              col[0] * 360.0 / DIM - 180,
        (atan (exp ((1 - row[1] / 512.0) * M_PI)) - M_PI / 4) / M_PI * 360,
                col[1] * 360.0 / DIM - 180,
        (atan (exp ((1 - row[0] / 512.0) * M_PI)) - M_PI / 4) / M_PI * 360,
              fname);
      
/*      hshrink = col[1] - col[0] > 60 ? 15 : (col[1] - col[0]) / 4;
      vshrink = row[1] - row[0] > 60 ? 15 : (row[1] - row[0]) / 4;
      for (i = row[0] + vshrink; i <= row[1] - vshrink; i++) {
        mat[i][col[0] + hshrink] = mat[i][col[1] - hshrink] = 192;
      }
      for (j = col[0] + hshrink; j <= col[1] - hshrink; j++) {
        mat[row[0] + vshrink][j] = mat[row[1] - vshrink][j] = 192;
      }
      fprintf (html, "<area shape=\"rect\" coords=\"%d,%d,%d,%d\" href=\"%.16s.html\" \n\
  OnMouseOver=\"mouseover('%.16s')\" OnMouseOut=\"mouseover('map')\" />\n",
		 col[0] + hshrink, row[0] + vshrink,
		 col[1] - hshrink, row[1] - vshrink, fname, fname);*/
//	printf (" --bp file=%s --wx %.16s.osm.bz2 \\\n",fname, fname);
      bptr[col[1] > 418 ? 0 : 1] += sprintf (bptr[col[1] > 418 ? 0 : 1],
        " \\\n --bb  idTrackerType=\"BitSet\" left=%.5lf right=%.5lf top=%.5lf bottom=%.5lf --wx %.16s.osm.gz",
              col[0] * 360.0 / DIM - 180, col[1] * 360.0 / DIM - 180,
        (atan (exp ((1 - row[0] / 512.0) * M_PI)) - M_PI / 4) / M_PI * 360,
        (atan (exp ((1 - row[1] / 512.0) * M_PI)) - M_PI / 4) / M_PI * 360,
              fname);
      bcnt[col[1] > 418 ? 0 : 1]++;
/*    poly = fopen (fname, "w");
      fprintf (poly, "%.16s\n1\n", fname);
      for (i = 0; i < 4; i++) {
        fprintf (poly, "  %10.5lf  %10.5lf\n", col[i * (3 - i) / 2] * 360.0
                 / DIM - 180.0,  (atan (exp ((1 - row[i / 2] / 512.0)
                                       * M_PI)) - M_PI / 4) / M_PI * 360);
      }
      fprintf (poly, "END\nEND\n");
      fclose (poly); */
      for (i = col[0]; i < col[1]; i++) {
        for (j = row[0]; j < row[1]; j++) cov[i][j] = 1;
      }
    }
  }
  printf ("bzcat planet-latest.osm.bz2 | ./gosmore sort Relations | \n\
 osmosis --read-xml enableDateParsing=no file=/dev/stdin --tee %d \\\n\
  --bb idTrackerType=\"BitSet\" left=-180 right=-33.04688\
 top=85.05113 bottom=-90 --wx left.osm.gz%s\n\n\
gunzip <left.osm.gz | osmosis\
 --read-xml enableDateParsing=no file=/dev/stdin --tee %d%s\n\n",
      bcnt[0] + 1, block[0], bcnt[1], block[1]);
  c = 0; // The error count
  for (i = 0; i < DIM; i++) {
    for (j = 0; j < DIM; j++) {
      if (!cov[i][j]) fprintf (stderr, "ERROR %d ! %c%c%c%d not coverd !\n",
        c++, (i)/26/26+'A'-1, (i)/26%26+'A'-1, (i)%26+'A', j+1);
    }
  }
  fclose (html);
  return c;
}
