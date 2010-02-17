#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <stdio.h>
#include <unistd.h>
#include <sys/types.h>
#include <fcntl.h>
//#include <libxml/xmlreader.h>
//#include <libxml/xmlwriter.h>
#include <vector>
#include <map>
#include <assert.h>
using namespace std;

struct younion { // Union of 1 or more bboxes. Terminated with -1.
  int *i;
  younion (int *_i) : i (_i) {}
};

bool operator < (const younion &a, const younion &b)
{
  int *ap = a.i, *bp;
  for (bp = b.i; *bp == *ap && *bp != -1; bp++) ap++;
  return *ap < *bp;
}

char buf[409600]; // I assume the largest object will fit in 400KB

int main (int argc, char *argv[])
{
  int bcnt = (argc - 1) / 6;
  double b[bcnt][4], lat, lon;
  FILE *f[bcnt];
  if (argc <= 1 || argc % 6 != 1) {
    fprintf (stderr, "Usage: %s bottom left top right pname fname [...]\n"
      "Reads an OSM-XML file from standard in and cut it into the given rectangles.\n"
      "pname is exectuted for each rectangle and the XML is piped to it. It's output\n"
      "is redirected to 'fname'. %s does not properly implement job control, but\n"
      "gzip, bzip and cat are acceptable values for pname\n" , argv[0], argv[0]);
    return 1;
  }
  for (int i = 0; i < bcnt; i++) {
    #if 0
    int p[2]; //p[(argc - 1) / 6][2], i;
    pipe (p);
    if (fork () == 0) {
      close (p[STDOUT_FILENO]);
      //for (i--; i >= 0; i--) close (p[i][STDOUT_FILENO]);
      dup2 (p[STDIN_FILENO], STDIN_FILENO);
      FILE *out = fopen (argv[i * 6 + 6], "w");
      dup2 (fileno (out), STDOUT_FILENO);
      execlp (argv[i * 6 + 5], argv[i * 6 + 5], NULL);
    }
    f[i] = fdopen (p[STDOUT_FILENO], "w");
    #else
    FILE *out = fopen (argv[i*6+6], "w");
    dup2 (fileno (out), STDOUT_FILENO);
    f[i] = popen (argv[i*6+5], "w");
    assert (f[i]);
    fclose (out);
    #endif
    fprintf (f[i], "<?xml version='1.0' encoding='UTF-8'?>\n"
      "<osm version=\"0.6\" generator=\"bboxSplit %s\">\n"
      "<bound box=\"%s,%s,%s,%s\"" 
      /* origin=\"http://www.openstreetmap.org/api/0.6\" */ "/>\n" , __DATE__,
      argv[i * 6 + 1],  argv[i * 6 + 2], argv[i * 6 + 3], argv[i * 6 + 4]);
    for (int j = 0; j < 4; j++) b[i][j] = atof (argv[i * 6 + j + 1]);
  }
  vector<int*> areas;
  // This vector maps area ids to a list of bboxes and 'amap' maps a list
  // of bboxes back to the id.
  areas.push_back (new int[1]); // Tiny once off memory leak.
  areas.back ()[0] = -1; // Make 0 the empty area
  map<younion,int> amap;
  amap[younion (areas.back ())] = 0;
  
  areas.push_back (new int[bcnt + 1]); // Tiny once off memory leak.
  areas.back ()[0] = -1; // Always have an empty set ready.
  
  #define areasIndexType unsigned short
  vector<areasIndexType> nwr[3]; // Nodes, relations, ways
  char *start = buf;
  long tipe[10], id, olevel = 0, memberTipe = 0, ref, acnt = 0, level;
  for (int cnt = 0, i; (i = fread (buf + cnt, 1, sizeof (buf) - cnt, stdin)) > 0;) {
    cnt += i;
    char *ptr = start, *n;
    level = olevel;
    do {
      //printf ("-- %d %.20s\n", level, ptr);
      int isEnd = (ptr + 1 < buf + cnt) &&
        ((ptr[0] == '<' && ptr[1] == '/') || (ptr[0] == '/' && ptr[1] == '>'));
      for (n = ptr; n < buf + cnt &&
                    (isEnd ? *n != '>' : !isspace (*n) && *n != '/'); n++) {
        if (*n == '\"') {
          for (++n; n < buf + cnt && *n != '\"'; n++) {}
        }
        else if (*n == '\'') {
          for (++n; n < buf + cnt && *n != '\''; n++) {}
        }
      }
      if (isEnd && n < buf + cnt) n++; // Get rid of the '>'
      while (n < buf + cnt && isspace (*n)) n++;
      
      if (isEnd && level == 2 && tipe[level - 1] == 'o') { // Note: n may be at buf + cnt
        for (int j = 0; j < bcnt; j++) {
          fprintf (f[j], "</osm>\n");
          pclose (f[j]);
        }
        // Should we close the files and wait for the children to exit ?
        fprintf (stderr, "%s done using %d area combinations\n", argv[0], areas.size () - 1);
        return 0;
      }
      if (n >= buf + cnt) {}
      else if (isEnd) {
        //printf ("Ending %c at %d\n", tipe[level - 1], level);
        if (--level == 2 && tipe[level] == 'n') { // End of a node
          for (int j = 0; j < bcnt; j++) {
            if (b[j][0] < lat && b[j][1] < lon && lat < b[j][2] && lon < b[j][3]) {
              areas.back ()[acnt++] = j;
            }
          }
          areas.back ()[acnt] = -1;
        }
        else if ((tipe[level] == 'n' || tipe[level] == 'm')
                 && level == 3) { // End of an '<nd ..>' or a '<member ...>
          memberTipe = tipe[2] == 'w' || memberTipe == 'n' ? 0
                       : memberTipe == 'w' ? 1 : 2;
          if (ref < nwr[memberTipe].size ()) {
            for (int j = 0, k = 0; areas[nwr[memberTipe][ref]][j] != -1; j++) {
              while (k < acnt && areas.back()[k] < areas[nwr[memberTipe][ref]][j]) k++;
              if (k >= acnt || areas.back()[k] > areas[nwr[memberTipe][ref]][j]) {
                memmove (&areas.back()[k + 1], &areas.back()[k],
                  sizeof (areas[0][0]) * (acnt++ - k));
                areas.back()[k] = areas[nwr[memberTipe][ref]][j];
              }
            } // Merge the two lists
          }
        }
        if (level == 2 && acnt > 0) { // areas.back()[0] != -1) {
        //(tipe[2] == 'n' || tipe[2] == 'w' || tipe[2] == 'r')) { // not needed for valid OSM-XML
          for (int j = 0; j < acnt /* areas.back()[j] != -1*/; j++) {
            //assert (areas.back ()[j] < bcnt);
            fwrite (start, 1, n - start, f[areas.back()[j]]);
          }
          areas.back ()[acnt] = -1;
          map<younion,int>::iterator mf = amap.find (younion (areas.back()));
          if (mf == amap.end ()) {
            int pos = areas.size () - 1;
            if (pos >> (sizeof (areasIndexType) * 8)) {
              for (int j = 0; j < bcnt; j++) {
                fprintf (f[j], "</osm>\n");
                pclose (f[j]);
              }
              fprintf (stderr, "%s FATAL: Too many combinations of areas\n", argv[0]);
              return 2;
            }
            amap[younion (areas.back ())] = pos;
            mf = amap.find (younion (areas.back()));
            areas.push_back (new int[bcnt + 1]); // Tiny once off memory leak.
            //assert (f != amap.end());
          }
          int nwrIdx = tipe[2] == 'n' ? 0 : tipe[2] == 'w' ? 1 : 2;
          //printf (stderr, "Extending %c to %ld\n", tipe[2], id);
          while (nwr[nwrIdx].size () <= id) nwr[nwrIdx].push_back (0);
          // Initialize nwr with 0 which implies the empty union
          nwr[nwrIdx][id] = mf->second;
          areas.back ()[0] = -1;
          acnt = 0;
        } // if we found an entity that belongs to at least 1 bbox
        if (level == 2) {
          start = n;
          olevel = level;
        }
      } // If it's /> or </..>
      else if (*ptr == '<') tipe[level++] = ptr[1];
      // The tests for 'level' is not necessary for valid OSM-XML
      else if (level == 3 && strncasecmp (ptr, "id=", 3) == 0) {
        id = atoi (ptr[3] == '\'' || ptr[3] == '\"' ? ptr + 4 : ptr + 3);
      }
      else if (level == 3 && strncasecmp (ptr, "lat=", 4) == 0) {
        lat = atof (ptr[4] == '\'' || ptr[4] == '\"' ? ptr + 5 : ptr + 4);
      }
      else if (level == 3 && strncasecmp (ptr, "lon=", 4) == 0) {
        lon = atof (ptr[4] == '\'' || ptr[4] == '\"' ? ptr + 5 : ptr + 4);
      }
      else if (level == 4 && strncasecmp (ptr, "type=", 4) == 0) {
        memberTipe = ptr[5] == '\'' || ptr[5] == '\"' ? ptr[6] : ptr[5];
      }
      else if (level == 4 && strncasecmp (ptr, "ref=", 4) == 0) {
        ref = atoi (ptr[4] == '\'' || ptr[4] == '\"' ? ptr + 5 : ptr + 4);
      }
      ptr = n;
    } while (ptr + 1 < buf + cnt);
    memmove (buf, start, buf + cnt - start);
    cnt -= start - buf;
    start = buf;
  }
  for (int j = 0; j < bcnt; j++) {
    fprintf (f[j], "</osm>\n");
    pclose (f[j]);
  }
  fprintf (stderr, "Warning: Xml termination not found. Files should be OK.\n");
  fprintf (stderr, "%s done using %d area combinations\n", argv[0], areas.size () - 1);
  return 1;
}
