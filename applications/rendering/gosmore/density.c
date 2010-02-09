// Run this program with :
// gcc -Wall -lm density.c && ./a.out <density.txt >density.sh
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
#define S 3
int split[S] = { 242, 418, 553 }, mid[S][4], bno, bcnt[S * 2 + 1];
char block[S * 2 + 1][98765], *bptr[S * 2 + 1];

#define N2(x) ((x) ? (x) + 'A' - 1 : '0')
int main (void)
{
  char fname[30];
  FILE *html, *gosm = fopen ("bboxes.c", "w"), *sh = fopen ("bboxSplit.sh", "w");
  memset (cov, 0, sizeof (cov));
  memset (mat, 0, sizeof (mat));
  html= fopen ("density.html", "w");
  fprintf (sh, "bzcat planet-latest.osm.bz2 | /home/nic/gosmore/bboxSplit \\\n");
  fprintf (html, "\
<html xmlns=\"http://www.w3.org/1999/xhtml\">\n\
    <head>\n\
        <title>Gosmore Map Selection</title>\n\
        <script src=\"http://www.openlayers.org/api/OpenLayers.js\"></script>\n\
        <script src=\"http://www.openstreetmap.org/openlayers/OpenStreetMap.js\"></script>\n\
        <script type=\"text/javascript\">\n\
            var box_extents = [\n");
  for (i = 0; i < 2*S + 1; i++) bptr[i] = block[i];
  memset (mid, -1, sizeof (mid));
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
      
      for (bno = S; bno > 0 && col[0] < split[bno - 1]; bno--);
      if (bno < S && col[1] > split[bno]) {
        mid[bno][0] = mid[bno][0] == -1 || col[0] < mid[bno][0] ? col[0] : mid[bno][0];
        mid[bno][1] = mid[bno][1] == -1 || row[0] < mid[bno][1] ? row[0] : mid[bno][1];
        mid[bno][2] = mid[bno][2] == -1 || col[1] > mid[bno][2] ? col[1] : mid[bno][2];
        mid[bno][3] = mid[bno][3] == -1 || row[1] > mid[bno][3] ? row[1] : mid[bno][3];
        bno += S + 1;
      }
      fprintf (sh, "  %10.5lf %10.5lf %10.5lf %10.5lf gzip %s.osm.gz \\\n",
        (atan (exp ((1 - row[1] / 512.0) * M_PI)) - M_PI / 4) / M_PI * 360,
        col[0] * 360.0 / DIM - 180,
        (atan (exp ((1 - row[0] / 512.0) * M_PI)) - M_PI / 4) / M_PI * 360,
        col[1] * 360.0 / DIM - 180, fname);
      bptr[bno] += sprintf (bptr[bno],
        " \\\n --bb  idTrackerType=\"BitSet\" left=%.5lf right=%.5lf top=%.5lf bottom=%.5lf --wx %.16s.osm.gz",
              col[0] * 360.0 / DIM - 180, col[1] * 360.0 / DIM - 180,
        (atan (exp ((1 - row[0] / 512.0) * M_PI)) - M_PI / 4) / M_PI * 360,
        (atan (exp ((1 - row[1] / 512.0) * M_PI)) - M_PI / 4) / M_PI * 360,
              fname);
      bcnt[bno]++;
      for (i = col[0]; i < col[1]; i++) {
        for (j = row[0]; j < row[1]; j++) cov[i][j] = 1;
      }
    }
  }
  fprintf (sh, "\n");
  printf ("bzcat planet-latest.osm.bz2 | ./gosmore sortRelations | \\\n\
 ionice -c 3 nice -n 19 osmosis --read-xml enableDateParsing=no file=/dev/stdin --tee %d",
    S * 2 + 1);
  for (i = 0; i <= S; i++) {
    printf (" \\\n  --bb idTrackerType=\"BitSet\" left=%.5lf right=%.5lf"
                                   " top=85.05113 bottom=-90 --wx %d.osm.gz",
      i == 0 ? -180.0 : split[i-1]*360/DIM-180, i < S ? split[i]*360/DIM-180 : 180.0, i);
    if (i < S) printf (" \\\n  --bb idTrackerType=\"BitSet\" left=%.5lf right=%.5lf"
           /*" top=%.5lf bottom=%.5lf*/ " top=85.05113 bottom=-90 --wx %d.osm.gz",
      mid[i][0]*360.0/DIM-180, mid[i][2]*360.0/DIM-180,
      /*mid[i][1]*360.0/DIM-180, mid[i][3]*360.0/DIM-180,*/ i + S + 1);
  }
  for (i = 0; i < 2*S + 1; i++) {
    printf ("\n\ngunzip <%d.osm.gz | ionice -c 3 nice -n 19 osmosis\
 --read-xml enableDateParsing=no file=/dev/stdin --tee %d%s\n",
      i, bcnt[i], block[i]);
  }
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
