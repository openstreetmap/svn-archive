// To run this program : 
// wget http://download.geonames.org/export/dump/cities1000.zip
// unzip cities1000.zip
// gcc geonames2osm.c && sort -nr -t$'\t' -k 15 cities1000.txt |
//   ./a.out >lowres.osm

#include <stdio.h>
#include <strings.h>

char list[180000][80], line[5000]; // Space for Jerusalem and its translations

int main (void)
{
  int cnt = 0, i, j, idx[30], ppl; // Do we need to set idx[0] = 0 ?
  while (fgets (line, sizeof (line), stdin)) {
    for (i = 0, j = 1; line[i] != '\0' && j < 30; i++) {
      if (line[i] == '\t') idx[j++] = i + 1;
    }
    if (j < 14) continue; // Bad line
    // For each geonames entry we generate 2 names and convert the ones that
    // are unique into OSM-XML.
    for (i = 1; i >= 0; i--) {
      sprintf (list[cnt], i ? "%.2s %.*s" : "%.0s%.*s", line +
        idx[line[idx[8]] == 'U' && line[idx[8]+1] == 'S' ? 10 : 8],
        idx[2] - idx[1] - 1, line + idx[1]);
      for (j = 0; j < cnt && strcasecmp (list[j], list[cnt]) != 0; j++) {}
      if (j < cnt) break; // If seen before
      ppl = atoi (line + idx[14]);
      if (i == 1) printf ("<node id='%d' lat='%.*s' lon='%.*s'>\n"
        "  <tag k='place' v='%s' />\n", 0x7fffffff - cnt,
        idx[5] - idx[4] - 1, line + idx[4],
        idx[6] - idx[5] - 1, line + idx[5], ppl > 456789 ? "city" :
        ppl > 23456 ? "town" : ppl > 789 ? "village" : "hamlet");
      printf ("  <tag k='%s' v=\"%s\" />\n", i ? "ref" : "name", list[cnt++]);
    }
    if (i < 1) printf ("</node>\n");
  }
}
