#ifdef ROUTE_SRV
#include <sys/mman.h>
#endif

#include <stdio.h>
#include <unistd.h>
#include <vector>
#include <assert.h>
using namespace std;

#include "libgosm.h"

routeNodeType *route = NULL, *shortest = NULL, **routeHeap;
long dhashSize;
int routeHeapSize, tlat, tlon, flat, flon, rlat, rlon;
int *hashTable, bucketsMin1, pakHead = 0xEB3A942;
char *gosmData, *gosmSstr[searchCnt];

// this is used if the stylerec in the pakfile are overwritten with
// one loaded from an alternative xml stylefile
styleStruct srec[2 << STYLE_BITS];

ndType *ndBase;
styleStruct *style;
wayType *gosmSway[searchCnt];

int TagCmp (char *a, char *b)
{ // This works like the ordering of books in a library : We ignore
  // meaningless words like "the", "street" and "north". We (should) also map
  // deprecated words to their new words, like petrol to fuel
  // TODO : We should consider an algorithm like double metasound.
  static const char *omit[] = { /* "the", in the middle of a name ?? */
    "ave", "avenue", "blvd", "boulevard", "byp", "bypass",
    "cir", "circle", "close", "cres", "crescent", "ct", "court", "ctr",
      "center",
    "dr", "drive", "hwy", "highway", "ln", "lane", "loop",
    "pass", "pky", "parkway", "pl", "place", "plz", "plaza",
    /* "run" */ "rd", "road", "sq", "square", "st", "street",
    "ter", "terrace", "tpke", "turnpike", /*trce, trace, trl, trail */
    "walk",  "way"
  };
  static const char *words[] = { "", "first", "second", "third", "fourth",
    "fifth", "sixth", "seventh", "eighth", "nineth", "tenth", "eleventh",
    "twelth", "thirteenth", "fourteenth", "fifthteenth", "sixteenth",
    "seventeenth", "eighteenth", "nineteenth", "twentieth" };
  static const char *teens[] = { "", "", "twenty ", "thirty ", "fourty ",
    "fifty ", "sixty ", "seventy ", "eighty ", "ninety " };
  
  if (stricmp (a, "the ") == 0) a += 4;
  if (stricmp (b, "the ") == 0) b += 4;
  if (strchr ("WEST", a[0]) && a[1] == ' ') a += 2; // e.g. N 21st St
  if (strchr ("WEST", b[0]) && b[1] == ' ') b += 2;

  for (;;) {
    char n[2][30] = { "", "" }, *ptr[2];
    int wl[2];
    for (int i = 0; i < 2; i++) {
      char **p = i ? &b : &a;
      if ((*p)[0] == ' ') {
        for (int i = 0; i < int (sizeof (omit) / sizeof (omit[0])); i++) {
          if (strncasecmp (*p + 1, omit[i], strlen (omit[i])) == 0 &&
              !isalpha ((*p)[1 + strlen (omit[i])])) {
            (*p) += 1 + strlen (omit[i]);
            break;
          }
        }
      }
      if (isdigit (**p) && (!isdigit((*p)[1]) || !isdigit ((*p)[2]))
              /* && isalpha (*p + strcspn (*p, "0123456789"))*/) {
        // while (atoi (*p) > 99) (*p)++; // Buggy
        if (atoi (*p) > 20) strcpy (n[i], teens[atoi ((*p)++) / 10]);
        strcat (n[i], words[atoi (*p)]);
        while (isdigit (**p) /*|| isalpha (**p)*/) (*p)++;
        ptr[i] = n[i];
        wl[i] = strlen (n[i]);
      }
      else {
        ptr[i] = *p;
        wl[i] = **p == ' ' ? 1 : strcspn (*p , " \n");
      }
    }
    int result = strncasecmp (ptr[0], ptr[1], wl[0] < wl[1] ? wl[1] : wl[0]);
    if (result || *ptr[0] == '\0' || *ptr[0] == '\n') return result;
    if (n[0][0] == '\0') a += wl[1]; // In case b was 21st
    if (n[1][0] == '\0') b += wl[0]; // In case a was 32nd
  }
}

/* 1. Bsearch idx such that
      ZEnc (way[idx]) < ZEnc (clon/lat) < ZEnc (way[idx+1])
   2. Fill the list with ways around idx.
   3. Now there's a circle with clon/clat as its centre and that runs through
      the worst way just found. Let's say it's diameter is d. There exist
      4 Z squares smaller that 2d by 2d that cover this circle. Find them
      with binary search and search through them for the nearest ways.
   The worst case is when the nearest nodes are far along a relatively
   straight line.
*/
static int IdxSearch (int *idx, int h, char *key, unsigned z)
{
  for (int l = 0; l < h;) {
    char *tag = gosmData + idx[(h + l) / 2];
    int diff = TagCmp (tag, key);
    while (*--tag) {}
    if (diff > 0 || (diff == 0 &&
      ZEnc ((unsigned)((wayType *)tag)[-1].clat >> 16, 
            (unsigned)((wayType *)tag)[-1].clon >> 16) >= z)) h = (h + l) / 2;
    else l = (h + l) / 2 + 1;
  }
  return h;
}

void GosmSearch (int clon, int clat, char *key)
{
  __int64 dista[searchCnt];
  char *taga[searchCnt];
  int *idx =
    (int *)(ndBase + hashTable[bucketsMin1 + (bucketsMin1 >> 7) + 2]);
  int l = IdxSearch (idx, hashTable - idx, key, 0), count;
//  char *lastName = data + idx[min (hashTable - idx), 
//    int (sizeof (gosmSway) / sizeof (gosmSway[0]))) + l - 1];
  int cz = ZEnc ((unsigned) clat >> 16, (unsigned) clon >> 16);
  for (count = 0; count + l < hashTable - idx && count < searchCnt;) {
    int m[2], c = count, ipos, dir, bits;
    m[0] = IdxSearch (idx, hashTable - idx, gosmData + idx[count + l], cz);
    m[1] = m[0] - 1;
    __int64 distm[2] = { -1, -1 }, big = ((unsigned __int64) 1 << 63) - 1;
    while (c < searchCnt && (distm[0] < big || distm[1] < big)) {
      dir = distm[0] < distm[1] ? 0 : 1;
      if (distm[dir] != -1) {
        for (ipos = c; count < ipos && distm[dir] < dista[ipos - 1]; ipos--) {
          dista[ipos] = dista[ipos - 1];
          gosmSway[ipos] = gosmSway[ipos - 1];
          taga[ipos] = taga[ipos - 1];
        }
        char *tag = gosmData + idx[m[dir]];
        taga[ipos] = tag;
        while (*--tag) {}
        gosmSway[ipos] = (wayType*)tag - 1;
        dista[ipos] = distm[dir];
        c++;
      }
      m[dir] += dir ? 1 : -1;

      if (0 <= m[dir] && m[dir] < hashTable - idx &&
        TagCmp (gosmData + idx[m[dir]], gosmData + idx[count + l]) == 0) {
        char *tag = gosmData + idx[m[dir]];
        while (*--tag) {}
        distm[dir] = Sqr ((__int64)(clon - ((wayType*)tag)[-1].clon)) +
          Sqr ((__int64)(clat - ((wayType*)tag)[-1].clat));
      }
      else distm[dir] = big;
    }
    if (count == c) break; // Something's wrong. idx[count + l] not found !
    if (c >= searchCnt) {
      c = count; // Redo the adding
      for (bits = 0; bits < 16 && dista[searchCnt - 1] >> (bits * 2 + 32);
        bits++) {}
/* Print Z's for first solution 
      for (int j = c; j < searchCnt; j++) {
        for (int i = 0; i < 32; i++) printf ("%d%s",
          (ZEnc ((unsigned) gosmSway[j]->clat >> 16,
                 (unsigned) gosmSway[j]->clon >> 16) >> (31 - i)) & 1,
          i == 31 ? " y\n" : i % 2 ? " " : "");
      } */
/* Print centre, up, down, right and left to see if they're in the square
      for (int i = 0; i < 32; i++) printf ("%d%s", (cz >> (31 - i)) & 1,
        i == 31 ? " x\n" : i % 2 ? " " : "");
      for (int i = 0; i < 32; i++) printf ("%d%s", (
        ZEnc ((unsigned)(clat + (int) sqrt (dista[searchCnt - 1])) >> 16,
              (unsigned)clon >> 16) >> (31 - i)) & 1,
        i == 31 ? " x\n" : i % 2 ? " " : "");
      for (int i = 0; i < 32; i++) printf ("%d%s", (
        ZEnc ((unsigned)(clat - (int) sqrt (dista[searchCnt - 1])) >> 16,
              (unsigned)clon >> 16) >> (31 - i)) & 1,
        i == 31 ? " x\n" : i % 2 ? " " : "");
      for (int i = 0; i < 32; i++) printf ("%d%s", (
        ZEnc ((unsigned)clat >> 16,
              (unsigned)(clon + (int) sqrt (dista[searchCnt - 1])) >> 16) >> (31 - i)) & 1,
        i == 31 ? " x\n" : i % 2 ? " " : "");
      for (int i = 0; i < 32; i++) printf ("%d%s", (
        ZEnc ((unsigned)clat >> 16,
              (unsigned)(clon - (int) sqrt (dista[searchCnt - 1])) >> 16) >> (31 - i)) & 1,
        i == 31 ? " x\n" : i % 2 ? " " : "");
*/      
      int swap = cz ^ ZEnc (
        (unsigned) (clat + (clat & (1 << (bits + 16))) * 4 -
                                              (2 << (bits + 16))) >> 16,
        (unsigned) (clon + (clon & (1 << (bits + 16))) * 4 -
                                              (2 << (bits + 16))) >> 16);
      // Now we search through the 4 squares around (clat, clon)
      for (int mask = 0, maskI = 0; maskI < 4; mask += 0x55555555, maskI++) {
        int s = IdxSearch (idx, hashTable - idx, gosmData + idx[count + l],
          (cz ^ (mask & swap)) & ~((4 << (bits << 1)) - 1));
/* Print the square
        for (int i = 0; i < 32; i++) printf ("%d%s", 
          (((cz ^ (mask & swap)) & ~((4 << (bits << 1)) - 1)) >> (31 - i)) & 1,
          i == 31 ? "\n" : i % 2 ? " " : "");
        for (int i = 0; i < 32; i++) printf ("%d%s", 
          (((cz ^ (mask & swap)) | ((4 << (bits << 1)) - 1)) >> (31 - i)) & 1,
          i == 31 ? "\n" : i % 2 ? " " : "");
*/
        for (;;) {
          char *tag = gosmData + idx[s++];
          if (TagCmp (gosmData + idx[count + l], tag) != 0) break;
          while (*--tag) {}
          wayType *w = (wayType*)tag - 1;
          if ((ZEnc ((unsigned)w->clat >> 16, (unsigned) w->clon >> 16) ^
               cz ^ (mask & swap)) >> (2 + (bits << 1))) break;
          __int64 d = Sqr ((__int64)(w->clat - clat)) +
                      Sqr ((__int64)(w->clon - clon));
          if (count < searchCnt || d < dista[count - 1]) {
            if (count < searchCnt) count++;
            for (ipos = count - 1; ipos > c && d < dista[ipos - 1]; ipos--) {
              dista[ipos] = dista[ipos - 1];
              gosmSway[ipos] = gosmSway[ipos - 1];
              taga[ipos] = taga[ipos - 1];
            }
            gosmSway[ipos] = w;
            dista[ipos] = d;
            taga[ipos] = gosmData + idx[s - 1];
          }
        } // For each entry in the square
      } // For each of the 4 squares
      break; // count < searchCnt implies a bug. Don't loop infinitely.
    } // If the search list is filled by tags with this text
    count = c;
  } // For each
  for (int i = 0; i < searchCnt; i++) free (gosmSstr[i]);
  for (int j = 0; j < count; j++) {
    gosmSstr[j] = (char *) malloc (strcspn (taga[j], "\n") + 1);
    sprintf (gosmSstr[j], "%.*s", (int) strcspn (taga[j], "\n"), taga[j]);
  }
  for (int k = count; k < searchCnt; k++) gosmSstr[k] = NULL;
}

/*------------------------------- OsmItr --------------------------------*/
int Next (OsmItr &itr) /* Friend of osmItr */
{
  do {
    itr.nd[0]++;
    while (itr.nd[0] >= itr.end) {
      if ((itr.slon += itr.tsize) == itr.right) {
        itr.slon = itr.left;  /* Here we wrap around from N85 to S85 ! */
        if ((itr.slat += itr.tsize) == itr.bottom) return FALSE;
      }
      int bucket = Hash (itr.slon, itr.slat, itr.tsize != TILESIZE);
      itr.nd[0] = ndBase + hashTable[bucket];
      itr.end = ndBase + hashTable[bucket + 1];
    }
  } while (((itr.nd[0]->lon ^ itr.slon) & (~(itr.tsize - 1))) ||
           ((itr.nd[0]->lat ^ itr.slat) & (~(itr.tsize - 1))));
/*      ((itr.hs[1] = (halfSegType *) (data + itr.hs[0]->other)) > itr.hs[0] &&
       itr.left <= itr.hs[1]->lon && itr.hs[1]->lon < itr.right &&
       itr.top <= itr.hs[1]->lat && itr.hs[1]->lat < itr.bottom)); */
/* while nd[0] is a hash collision, */ 
  return TRUE;
}

/* Routing starts at the 'to' point and moves to the 'from' point. This will
   help when we do in car navigation because the 'from' point will change
   often while the 'to' point stays fixed, so we can keep the array of nodes.
   It also makes the generation of the directions easier.

   We use "double hashing" to keep track of the shortest distance to each
   node. So we guess an upper limit for the number of nodes that will be
   considered and then multiply by a few so that there won't be too many
   clashes. For short distances we allow for dense urban road networks,
   but beyond a certain point there is bound to be farmland or seas.

   We call nodes that rescently had their "best" increased "active". The
   active nodes are stored in a heap so that we can quickly find the most
   promissing one.
   
   OSM nodes are not our "graph-theor"etic nodes. Our "graph-theor"etic nodes
   are "states", namely the ability to reach nd directly from nd->other[dir]
*/
#ifdef ROUTE_CALIBRATE
int routeAddCnt;
#define ROUTE_SET_ADDND_COUNT(x) routeAddCnt = (x)
#define ROUTE_SHOW_STATS printf ("%d / %d\n", routeAddCnt, dhashSize); \
  fprintf (stderr, "flat=%lf&flon=%lf&tlat=%lf&tlon=%lf&fast=%d&v=motorcar\n", \
    LatInverse (flat), LonInverse (flon), LatInverse (tlat), \
    LonInverse (tlon), fast)
// This ratio must be around 0.5. Close to 0 or 1 is bad
#else
#define ROUTE_SET_ADDND_COUNT(x)
#define ROUTE_SHOW_STATS
#endif

static ndType *endNd[2] = { NULL, NULL}, from;
static int toEndNd[2][2];  

routeNodeType *AddNd (ndType *nd, int dir, int cost, routeNodeType *newshort)
{ /* This function is called when we find a valid route that consists of the
     segments (hs, hs->other), (newshort->hs, newshort->hs->other),
     (newshort->shortest->hs, newshort->shortest->hs->other), .., 'to'
     with cost 'cost'.
     
     When cost is -1 this function just returns the entry for nd without
     modifying anything. */
  unsigned hash = (intptr_t) nd / 10 + dir, i = 0;
  routeNodeType *n;
  do {
    #ifdef ROUTE_SRV
    n = route + (nd == &from ? dhashSize - 2 : (nd - ndBase) * 2) + dir;
    #else
    if (i++ > 10) {
      //fprintf (stderr, "Double hash bailout : Table full, hash function "
      //  "bad or no route exists\n");
      return NULL;
    }
    n = route + hash % dhashSize;
    #endif
    /* Linear congruential generator from wikipedia */
    hash = (unsigned) (hash * (__int64) 1664525 + 1013904223);
    if (n->nd == NULL) { /* First visit of this node */
      if (cost < 0) return NULL;
      n->nd = nd;
      n->best = 0x7fffffff;
      /* Will do later : routeHeap[routeHeapSize] = n; */
      n->heapIdx = routeHeapSize++;
      n->dir = dir;
      n->remain = lrint (sqrt (Sqr ((__int64)(nd->lat - rlat)) +
                               Sqr ((__int64)(nd->lon - rlon))));
      if (!shortest || n->remain < shortest->remain) shortest = n;
      ROUTE_SET_ADDND_COUNT (routeAddCnt + 1);
    }
  } while (n->nd != nd || n->dir != dir);

  int diff = n->remain + (newshort ? newshort->best - newshort->remain : 0);
  if (cost >= 0 && n->best > cost + diff) {
    n->best = cost + diff;
    n->shortest = newshort;
    if (n->heapIdx < 0) n->heapIdx = routeHeapSize++;
    for (; n->heapIdx > 1 &&
         n->best < routeHeap[n->heapIdx / 2]->best; n->heapIdx /= 2) {
      routeHeap[n->heapIdx] = routeHeap[n->heapIdx / 2];
      routeHeap[n->heapIdx]->heapIdx = n->heapIdx;
    }
    routeHeap[n->heapIdx] = n;
  }
  return n;
}

inline int IsOneway (wayType *w, int Vehicle)
{
  return !((Vehicle == footR || Vehicle == bicycleR) &&
    (w->bits & (1 << motorcarR))) && (w->bits & (1<<onewayR));
}

void Route (int recalculate, int plon, int plat, int Vehicle, int fast)
{ /* Recalculate is faster but only valid if 'to', 'Vehicle' and
     'fast' did not change */
/* We start by finding the segment that is closest to 'from' and 'to' */
  ROUTE_SET_ADDND_COUNT (0);
  shortest = NULL;
  for (int i = recalculate ? 0 : 1; i < 2; i++) {
    int lon = i ? flon : tlon, lat = i ? flat : tlat;
    __int64 bestd = (__int64) 1 << 62;
    /* find min (Sqr (distance)). Use long long so we don't loose accuracy */
    OsmItr itr (lon - 100000, lat - 100000, lon + 100000, lat + 100000);
    /* Search 1km x 1km around 'from' for the nearest segment to it */
    while (Next (itr)) {
      // We don't do for (int dir = 0; dir < 1; dir++) {
      // because if our search box is large enough, it will also give us
      // the other node.
      if (!(Way (itr.nd[0])->bits & (1 << Vehicle))) {
        continue;
      }
      if (itr.nd[0]->other[0] < 0) continue;
      __int64 lon0 = lon - itr.nd[0]->lon, lat0 = lat - itr.nd[0]->lat,
              lon1 = lon - (ndBase + itr.nd[0]->other[0])->lon,
              lat1 = lat - (ndBase + itr.nd[0]->other[0])->lat,
              dlon = lon1 - lon0, dlat = lat1 - lat0;
      /* We use Pythagoras to test angles for being greater that 90 and
         consequently if the point is behind hs[0] or hs[1].
         If the point is "behind" hs[0], measure distance to hs[0] with
         Pythagoras. If it's "behind" hs[1], use Pythagoras to hs[1]. If
         neither, use perpendicular distance from a point to a line */
      int segLen = lrint (sqrt ((double)(Sqr(dlon) + Sqr (dlat))));
      __int64 d = dlon * lon0 >= - dlat * lat0 ? Sqr (lon0) + Sqr (lat0) :
        dlon * lon1 <= - dlat * lat1 ? Sqr (lon1) + Sqr (lat1) :
        Sqr ((dlon * lat1 - dlat * lon1) / segLen);
      
      wayType *w = Way (itr.nd[0]);
      if (i) { // For 'from' we take motion into account
        __int64 motion = segLen ? 3 * (dlon * plon + dlat * plat) / segLen
          : 0;
        // What is the most appropriate multiplier for motion ?
        if (motion > 0 && IsOneway (w, Vehicle)) d += Sqr (motion);
        else d -= Sqr (motion);
        // Is it better to say :
        // d = lrint (sqrt ((double) d));
        // if (motion < 0 || IsOneway (w)) d += motion;
        // else d -= motion; 
      }
      
      if (d < bestd) {
        bestd = d;
        double invSpeed = !fast ? 1.0 : Style (w)->invSpeed[Vehicle];
        //printf ("%d %lf\n", i, invSpeed);
        toEndNd[i][0] =
          lrint (sqrt ((double)(Sqr (lon0) + Sqr (lat0))) * invSpeed);
        toEndNd[i][1] =
          lrint (sqrt ((double)(Sqr (lon1) + Sqr (lat1))) * invSpeed);
//        if (dlon * lon1 <= -dlat * lat1) toEndNd[i][1] += toEndNd[i][0] * 9;
//        if (dlon * lon0 >= -dlat * lat0) toEndNd[i][0] += toEndNd[i][1] * 9;

        if (IsOneway (w, Vehicle)) toEndNd[i][1 - i] = 200000000;
        /*  It's possible to go up a oneway at the end, but at a huge penalty*/
        endNd[i] = itr.nd[0];
        /* The router only stops after it has traversed endHs[1], so if we
           want 'limit' to be accurate, we must subtract it's length
        if (i) {
          toEndHs[1][0] -= segLen; 
          toEndHs[1][1] -= segLen;
        } */
      }
    } /* For each candidate segment */
    if (bestd == ((__int64) 1 << 62) || !endNd[0]) {
      endNd[i] = NULL;
      //fprintf (stderr, "No segment nearby\n");
      return;
    }
  } /* For 'from' and 'to', find segment that passes nearby */
  from.lat = flat;
  from.lon = flon;
  if (recalculate || !route) {
    #ifdef ROUTE_SRV
    static FILE *tfile = NULL;
    if (tfile) {
      fclose (tfile);
      munmap (route, (sizeof (*route) + sizeof (*routeHeap)) * dhashSize);
    }
    dhashSize = hashTable[bucketsMin1 + (bucketsMin1 >> 7) + 2] * 2;
    if ((tfile = tmpfile ()) == NULL || ftruncate (fileno (tfile), dhashSize *
               (sizeof (*route) + sizeof (*routeHeap))) != 0 ||
        (route = (routeNodeType*) mmap (NULL, dhashSize *
        (sizeof (*route) + sizeof (*routeHeap)),
        PROT_READ | PROT_WRITE, MAP_SHARED, fileno (tfile), 0)) == MAP_FAILED) {
      fprintf (stderr, "Ftruncate and Mmap of routing arrays\n");
      route = NULL;
      return;
    }
    #else
    free (route);
    dhashSize = Sqr ((tlon - flon) >> 16) + Sqr ((tlat - flat) >> 16) + 20;
    dhashSize = dhashSize < 10000 ? dhashSize * 1000 : 10000000;
    // Allocate one piece of memory for both route and routeHeap, so that
    // we can easily retry if it fails on a small device
    #ifdef _WIN32_WCE
    MEMORYSTATUS memStat;
    GlobalMemoryStatus (&memStat);
    int lim = (memStat.dwAvailPhys - 1400000) / // Leave 1.4 MB free
                 (sizeof (*route) + sizeof (*routeHeap));
    if (dhashSize > lim && lim > 0) dhashSize = lim;
    #endif

    while (dhashSize > 0 && !(route = (routeNodeType*)
        malloc ((sizeof (*route) + sizeof (*routeHeap)) * dhashSize))) {
      dhashSize = dhashSize / 4 * 3;
    }
    memset (route, 0, sizeof (dhashSize) * dhashSize);
    #endif
    routeHeapSize = 1; /* Leave position 0 open to simplify the math */
    routeHeap = (routeNodeType**) (route + dhashSize) - 1;

    rlat = flat;
    rlon = flon;
    AddNd (endNd[0], 0, toEndNd[0][0], NULL);
    AddNd (ndBase + endNd[0]->other[0], 1, toEndNd[0][1], NULL);
    AddNd (endNd[0], 1, toEndNd[0][0], NULL);
    AddNd (ndBase + endNd[0]->other[0], 0, toEndNd[0][1], NULL);
  }
  else {
    routeNodeType *frn = AddNd (&from, 0, -1, NULL);
    if (frn) frn->best = 0x7fffffff;

    routeNodeType *rn = AddNd (endNd[1], 0, -1, NULL);
    if (rn) AddNd (&from, 0, toEndNd[1][1], rn);
    routeNodeType *rno = AddNd (ndBase + endNd[1]->other[0], 1, -1, NULL);
    if (rno) AddNd (&from, 0, toEndNd[1][0], rno);
  }
  
  while (routeHeapSize > 1) {
    routeNodeType *root = routeHeap[1];
    routeHeapSize--;
    int beste = routeHeap[routeHeapSize]->best;
    for (int i = 2; ; ) {
      int besti = i < routeHeapSize ? routeHeap[i]->best : beste;
      int bestipp = i + 1 < routeHeapSize ? routeHeap[i + 1]->best : beste;
      if (besti > bestipp) i++;
      else bestipp = besti;
      if (beste <= bestipp) {
        routeHeap[i / 2] = routeHeap[routeHeapSize];
        routeHeap[i / 2]->heapIdx = i / 2;
        break;
      }
      routeHeap[i / 2] = routeHeap[i];
      routeHeap[i / 2]->heapIdx = i / 2;
      i = i * 2;
    }
    root->heapIdx = -1; /* Root now removed from the heap */
    if (root->nd == &from) { // Remove 'from' from the heap in case we
      shortest = root->shortest; // get called with recalculate=0
      break;
    }
    if (root->nd == (!root->dir ? endNd[1] : ndBase + endNd[1]->other[0])) {
      AddNd (&from, 0, toEndNd[1][1 - root->dir], root);
    }
    ndType *nd = root->nd, *other, *firstNd, *restrictItr;
    while (nd > ndBase && nd[-1].lon == nd->lon &&
      nd[-1].lat == nd->lat) nd--; /* Find first nd in node */
    firstNd = nd; // Save it for checking restrictions
    int rootIsAdestination = Way (root->nd)->destination & (1 << Vehicle);
    /* Now work through the segments connected to root. */
    do {
      if (StyleNr (Way (nd)) >= barrier_bollard &&
          StyleNr (Way (nd)) <= barrier_toll_booth &&
          !(Way (nd)->bits & (1 << Vehicle))) break;
      if (root->remain > 500000 && root->best - root->remain > 500000 &&
          (StyleNr (Way (nd)) == highway_residential ||
           StyleNr (Way (nd)) == highway_service ||
           StyleNr (Way (nd)) == highway_living_street ||
           StyleNr (Way (nd)) == highway_unclassified)) continue;
      /* When more than 50km from the start and the finish, ignore minor
         roads. This reduces the number of calculations. */
      for (int dir = 0; dir < 2; dir++) {
        if (nd == root->nd && dir == root->dir) continue;
        /* Don't consider an immediate U-turn to reach root->hs->other.
           This doesn't exclude 179.99 degree turns though. */
        
        if (nd->other[dir] < 0) continue;
        if (Vehicle != footR && Vehicle != bicycleR) {
          for (restrictItr = firstNd; restrictItr->other[0] < 0 &&
                          restrictItr->other[1] < 0; restrictItr++) {
            wayType *w  = Way (restrictItr);
            if (StyleNr (w) < restriction_no_right_turn ||
                StyleNr (w) > restriction_only_straight_on) continue;
  //          printf ("aa\n");
            if (atoi ((char*)(w + 1) + 1) == nd->wayPtr &&
                atoi (strchr ((char*)(w + 1) + 1, ' ')) == root->nd->wayPtr) {
               ndType *n2 = ndBase + root->nd->other[root->dir];
               ndType *n0 = ndBase + nd->other[dir];
               __int64 straight =
                 (n2->lat - nd->lat) * (__int64)(nd->lat - n0->lat) +
                 (n2->lon - nd->lon) * (__int64)(nd->lon - n0->lon), left =
                 (n2->lat - nd->lat) * (__int64)(nd->lon - n0->lon) -
                 (n2->lon - nd->lon) * (__int64)(nd->lat - n0->lat);
               int azi = straight < left ? (straight < -left ? 3 : 0) :
                 straight < -left ? 2 : 1;
//               printf ("%d %9d %9d %d %d\n", azi, n2->lon - nd->lon, n0->lon - nd->lon, straight < left, straight < -left);
//               printf ("%d %9d %9d\n", azi, n2->lat - nd->lat, n0->lat - nd->lat);
               static const int no[] = { restriction_no_left_turn,
                 restriction_no_straight_on, restriction_no_right_turn,
                 restriction_no_u_turn },
                 only[] = { restriction_only_left_turn,
                 restriction_only_straight_on, restriction_only_right_turn,
                 -1 /*  restriction_only_u_turn */ };
               if (StyleNr (w) == only[azi ^ 1] ||
                   StyleNr (w) == only[azi ^ 2] || StyleNr (w) == only[azi ^ 3]
                   || StyleNr (w) == no[azi]) break;
//               printf ("%d %d %d\n", azi, n2->lon, n0->lon);
            }
          }
          if (restrictItr->other[0] < 0 &&
              restrictItr->other[1] < 0) continue;
        }
        // Tagged node, start or end of way or restriction.
        
        other = ndBase + nd->other[dir];
        wayType *w = Way (nd);
        if ((w->bits & (1 << Vehicle)) && (dir || !IsOneway (w, Vehicle))) {
          int d = lrint (sqrt ((double)
            (Sqr ((__int64)(nd->lon - other->lon)) +
             Sqr ((__int64)(nd->lat - other->lat)))) *
                        (fast ? Style (w)->invSpeed[Vehicle] : 1.0));     
          if (rootIsAdestination && !(w->destination & (1 << Vehicle))) {
            d += 5000000; // 500km penalty for entering v='destination' area.
          }
          AddNd (other, 1 - dir, d, root);
        } // If we found a segment we may follow
      }
    } while (++nd < ndBase + hashTable[bucketsMin1 + 1] &&
             nd->lon == nd[-1].lon && nd->lat == nd[-1].lat);
  } // While there are active nodes left
  ROUTE_SHOW_STATS;
//  if (fastest) printf ("%lf
//  printf ("%lf km\n", limit / 100000.0);
}

int JunctionType (ndType *nd)
{
  int ret = 'j';
  while (nd > ndBase && nd[-1].lon == nd->lon &&
    nd[-1].lat == nd->lat) nd--;
  int segCnt = 0; // Count number of segments at x->shortest
  do {
    // TODO : Only count segment traversable by 'Vehicle'
    // Except for the case where a cyclist passes a motorway_link.
    // TODO : Don't count oneways entering the roundabout
    if (nd->other[0] >= 0) segCnt++;
    if (nd->other[1] >= 0) segCnt++;
    if (StyleNr (Way (nd)) == highway_traffic_signals) {
      ret = 't';
    }
    if (StyleNr (Way (nd)) == highway_mini_roundabout) {
      ret = 'm';
    }   
  } while (++nd < ndBase + hashTable[bucketsMin1 + 1] &&
           nd->lon == nd[-1].lon && nd->lat == nd[-1].lat);
  return segCnt > 2 ? toupper (ret) : ret;
}

int GosmInit (void *d, long size)
{
  if (!d) return FALSE;
  gosmData = (char*) d;
  bucketsMin1 = ((int *) (gosmData + size))[-2];
  hashTable = (int *) (gosmData + size) - bucketsMin1 - (bucketsMin1 >> 7)
                      - 5;
  ndBase = (ndType *)(gosmData + hashTable[bucketsMin1 + (bucketsMin1 >> 7)
     + 4]);
  style = (struct styleStruct *)(gosmData + 4);
  memset (gosmSway, 0, sizeof (gosmSway));
  return ndBase && hashTable && *(int*) gosmData == pakHead;
}

void GosmLoadAltStyle(const char* elemstylefile, const char* iconscsvfile) {
  elemstyleMapping map[2 << STYLE_BITS]; // this is needed for
					 // LoadElemstyles but ignored
  memset (&srec, 0, sizeof (srec)); // defined globally
  memset (&map, 0, sizeof (map));
  LoadElemstyles(elemstylefile, iconscsvfile, srec, map, firstElemStyle);
  // over-ride style record loaded from pakfile with alternative
  style = &(srec[0]);
}

// *** EVERYTHING AFTER THIS POINT IS NOT IN THE WINDOWS BUILDS ***

#ifndef _WIN32

/*--------------------------------- Rebuild code ---------------------------*/
// These defines are only used during rebuild

#include <sys/mman.h>
#include <libxml/xmlreader.h>

#define MAX_BUCKETS (1<<26)
#define IDXGROUPS 676
#define NGROUPS 60
#define MAX_NODES 9000000 /* Max in a group */
#define S2GROUPS 129 // Last group is reserved for lowzoom halfSegs
#define NGROUP(x)  ((x) / MAX_NODES % NGROUPS + IDXGROUPS)
#define S1GROUPS NGROUPS
#define S1GROUP(x) ((x) / MAX_NODES % NGROUPS + IDXGROUPS + NGROUPS)
#define S2GROUP(x) ((x) / (MAX_BUCKETS / (S2GROUPS - 1)) + IDXGROUPS + NGROUPS * 2)
#define PAIRS (16 * 1024 * 1024)
#define PAIRGROUPS 120
#define PAIRGROUP(x) ((x) / PAIRS + S2GROUP (0) + S2GROUPS)
#define PAIRGROUPS2 120
#define PAIRGROUP2(x) ((x) / PAIRS + PAIRGROUP (0) + PAIRGROUPS)
#define FIRST_LOWZ_OTHER (PAIRS * (PAIRGROUPS - 1))

#define REBUILDWATCH(x) fprintf (stderr, "%3d %s\n", ++rebuildCnt, #x); x

#define TO_HALFSEG -1 // Rebuild only

struct halfSegType { // Rebuild only
  int lon, lat, other, wayPtr;
};

struct nodeType {
  int id, lon, lat;
};

char *data;

inline nodeType *FindNode (nodeType *table, int id)
{
  unsigned hash = id;
  for (;;) {
    nodeType *n = &table[hash % MAX_NODES];
    if (n->id < 0 || n->id == id) return n;
    hash = hash * (__int64) 1664525 + 1013904223;
  }
}

int HalfSegCmp (const halfSegType *a, const halfSegType *b)
{
  int lowz = a->other < -2 || FIRST_LOWZ_OTHER <= a->other;
  int hasha = Hash (a->lon, a->lat, lowz), hashb = Hash (b->lon, b->lat, lowz);
  return hasha != hashb ? hasha - hashb : a->lon != b->lon ? a->lon - b->lon :
    a->lat != b->lat ? a->lat - b->lat :
    (b->other < 0 && b[1].other < 0 ? 1 : 0) -
    (a->other < 0 && a[1].other < 0 ? 1 : 0);
} // First sort by hash bucket, then by lon, then by lat.
// If they are all the same, the nodes goes in the front where so that it's
// easy to iterate through the turn restrictions.

int IdxCmp (const void *aptr, const void *bptr)
{
  char *ta = data + *(unsigned *)aptr, *tb = data + *(unsigned *)bptr;
  int tag = TagCmp (ta, tb);
  while (*--ta) {}
  while (*--tb) {}
  unsigned a = ZEnc ((unsigned)((wayType *)ta)[-1].clat >> 16, 
                     (unsigned)((wayType *)ta)[-1].clon >> 16);
  unsigned b = ZEnc ((unsigned)((wayType *)tb)[-1].clat >> 16, 
                     (unsigned)((wayType *)tb)[-1].clon >> 16);
  return tag ? tag : a < b ? -1 : 1;
}

/* To reduce the number of cache misses and disk seeks we need to construct
 the pack file so that waysTypes that are physically close to each other, are
 also close to each other in the file. We only know where ways are physically
 after the first pass, so the reordering is one done during a bbox rebuild.
 
 Finding an optimal solution is quite similar to find a soluting to the
 traveling salesman problem. Instead we just place them in 2-D Hilbert curve
 order using a qsort. */
typedef struct {
  wayType *w;
  int idx;
} masterWayType;

int MasterWayCmp (const void *a, const void *b)
{
  int r[2], t, s, i, lead;
  for (i = 0; i < 2; i++) {
    t = ZEnc (((masterWayType *)(i ? b : a))->w->clon >> 16,
      ((unsigned)((masterWayType *)(i ? b : a))->w->clat) >> 16);
    s = ((((unsigned)t & 0xaaaaaaaa) >> 1) | ((t & 0x55555555) << 1)) ^ ~t;
    for (lead = 1 << 30; lead; lead >>= 2) {
      if (!(t & lead)) t ^= ((t & (lead << 1)) ? s : ~s) & (lead - 1);
    }
    r[i] = ((t & 0xaaaaaaaa) >> 1) ^ t;
  }
  return r[0] < r[1] ? 1 : r[0] > r[1] ? -1 : 0;
}

int LoadElemstyles(const char *elemstylesfname, const char *iconsfname, 
		   styleStruct *srec, elemstyleMapping *map, 
		   int styleCnt)
{
   //------------------------- elemstyle.xml : --------------------------
    int ruleCnt = 0;
    // zero-out elemstyle-to-stylestruct mappings
    FILE *icons_csv = fopen (iconsfname, "r");
    xmlTextReaderPtr sXml = xmlNewTextReaderFilename (elemstylesfname);
    if (!sXml || !icons_csv) {
      fprintf (stderr, "Either icons.csv or elemstyles.xml not found\n");
      return 3;
    }
    for (int i = 0; i < (2 << STYLE_BITS); i++) {
      srec[i].lineColour = -1;
      srec[i].areaColour = -1;
    }
    /* If elemstyles contain these, we can delete these assignments : */
    for (int i = restriction_no_right_turn;
            i <= restriction_only_straight_on; i++) {
      strcpy(map[i].style_k,"restriction");
      srec[i].scaleMax = 1;
      srec[i].lineColour = 0; // Make it match.
    }
    strcpy(map[restriction_no_right_turn].style_v,"no_right_turn");
    strcpy(map[restriction_no_left_turn].style_v,"no_left_turn");
    strcpy(map[restriction_no_u_turn].style_v,"no_u_turn");
    strcpy(map[restriction_no_straight_on].style_v,"no_straight_on");
    strcpy(map[restriction_only_right_turn].style_v,"only_right_turn");
    strcpy(map[restriction_only_left_turn].style_v,"only_left_turn");
    strcpy(map[restriction_only_straight_on].style_v,"only_straight_on");

    while (xmlTextReaderRead (sXml)) {
      char *name = (char*) xmlTextReaderName (sXml);
      //xmlChar *val = xmlTextReaderValue (sXml);
      if (xmlTextReaderNodeType (sXml) == XML_READER_TYPE_ELEMENT) {
        if (strcasecmp (name, "scale_max") == 0) {
          while (xmlTextReaderRead (sXml) && // memory leak :
            xmlStrcmp (xmlTextReaderName (sXml), BAD_CAST "#text") != 0) {}
          srec[styleCnt].scaleMax = atoi ((char *) xmlTextReaderValue (sXml));
        }
        while (xmlTextReaderMoveToNextAttribute (sXml)) {
          char *n = (char *) xmlTextReaderName (sXml);
          char *v = (char *) xmlTextReaderValue (sXml);
          if (strcasecmp (name, "condition") == 0) {
	    if (strcasecmp (n, "k") == 0) strcpy(map[styleCnt].style_k, v);
	    if (strcasecmp (n, "v") == 0) strcpy(map[styleCnt].style_v, v);
          }
          if (strcasecmp (name, "line") == 0) {
            if (strcasecmp (n, "width") == 0) {
              srec[styleCnt].lineWidth = atoi (v);
            }
            if (strcasecmp (n, "realwidth") == 0) {
              srec[styleCnt].lineRWidth = atoi (v);
            }
            if (strcasecmp (n, "colour") == 0) {
              sscanf (v, "#%x", &srec[styleCnt].lineColour);
            }
            if (strcasecmp (n, "colour_bg") == 0) {
              sscanf (v, "#%x", &srec[styleCnt].lineColourBg);
            }
            srec[styleCnt].dashed = srec[styleCnt].dashed ||
              (strcasecmp (n, "dashed") == 0 && strcasecmp (v, "true") == 0);
          }
          if (strcasecmp (name, "area") == 0) {
            if (strcasecmp (n, "colour") == 0) {
              sscanf (v, "#%x", &srec[styleCnt].areaColour);
            }
          }
          if (strcasecmp (name, "icon") == 0) {
            if (strcasecmp (n, "src") == 0) {
              while (v[strcspn ((char *) v, "/ ")]) {
                v[strcspn ((char *) v, "/ ")] = '_';
              }
              char line[80], fnd = FALSE;
              static const char *set[] = { "classic.big_", "classic.small_",
                "square.big_", "square.small_" };
              for (int i = 0; i < 4; i++) {
                srec[styleCnt].x[i * 4 + 2] = srec[styleCnt].x[i * 4 + 3] = 1;
              // Default to 1x1 dummys
                int slen = strlen (set[i]), vlen = strlen (v);
                rewind (icons_csv);
                while (fgets (line, sizeof (line) - 1, icons_csv)) {
                  if (strncmp (line, set[i], slen) == 0 &&
                      strncmp (line + slen, v, vlen - 1) == 0) {
                    sscanf (line + slen + vlen, ":%d:%d:%d:%d",
                      srec[styleCnt].x + i * 4, srec[styleCnt].x + i * 4 + 1,
                      srec[styleCnt].x + i * 4 + 2,
                      srec[styleCnt].x + i * 4 + 3);
                    fnd = TRUE;
                  }
                }
              }
              if (!fnd) fprintf (stderr, "Icon %s not found\n", v);
            }
          }
          if (strcasecmp (name, "routing") == 0 && atoi (v) > 0) {
            #define M(field) if (strcasecmp (n, #field) == 0) {\
              map[styleCnt].defaultRestrict |= 1 << field ## R; \
              srec[styleCnt].aveSpeed[field ## R] = atof (v); \
            }
            RESTRICTIONS
            #undef M
          }
          
          xmlFree (v);
          xmlFree (n);
        }
      }
      else if (xmlTextReaderNodeType (sXml) == XML_READER_TYPE_END_ELEMENT
                  && strcasecmp ((char *) name, "rule") == 0) {
        int ipos;
        #define s(k,v,shortname,extraTags) { #k, #v },
        static const char *stylet[][2] = { STYLES };
        #undef s
        for (ipos = 0; ipos < firstElemStyle; ipos++) {
          if (strcmp (stylet[ipos][0], map[styleCnt].style_k) == 0 && 
              strcmp (stylet[ipos][1], map[styleCnt].style_v) == 0) break;
        }
        map[ipos < firstElemStyle ? ipos : styleCnt].ruleNr = ruleCnt++;
        if (ipos < firstElemStyle) {
          memcpy (&srec[ipos], &srec[styleCnt], sizeof (srec[ipos]));
          memcpy (&srec[styleCnt], &srec[styleCnt + 1], sizeof (srec[0]));
          map[ipos].defaultRestrict = map[styleCnt].defaultRestrict;
          map[styleCnt].defaultRestrict = 0;
          strcpy(map[ipos].style_k,map[styleCnt].style_k);
          strcpy(map[ipos].style_v,map[styleCnt].style_v);
        }
        else if (styleCnt < (2 << STYLE_BITS) - 2) styleCnt++;
        else fprintf (stderr, "Too many rules. Increase STYLE_BITS\n");
      }
      xmlFree (name);
      //xmlFree (val);      
    }
    for (int i = 0; i < layerBit1; i++) {
      double max = 0;
      for (int j = 0; j < styleCnt; j++) {
        if (srec[j].aveSpeed[i] > max) max = srec[j].aveSpeed[i];
      }
      for (int j = 0; j < styleCnt; j++) {
        if (srec[j].aveSpeed[i] == 0) { // e.g. highway=foot motorcar=yes
          for (int k = 0; k < layerBit1; k++) {
            if (srec[j].aveSpeed[i] < srec[j].aveSpeed[k]) {
              srec[j].aveSpeed[i] = srec[j].aveSpeed[k];
            } // As fast as any other vehicle,
          } // without breaking our own speed limit :
          if (srec[j].aveSpeed[i] > max) srec[j].aveSpeed[i] = max;
        }
        srec[j].invSpeed[i] = max / srec[j].aveSpeed[i];
      }
    }
    xmlFreeTextReader (sXml);

    return styleCnt;
}

int RebuildPak(const char* pakfile, const char* elemstylefile, 
	       const char* iconscsvfile, const char* masterpakfile, 
	       const int bbox[4]) {
  assert (layerBit3 < 32);

  int rebuildCnt = 0;
  FILE *pak, *masterf;
  int ndStart;
  wayType *master = NULL;
  if (strcmp(masterpakfile,"")) {
    if (!(masterf = fopen64 (masterpakfile, "r")) ||
	fseek (masterf, -sizeof (ndStart), SEEK_END) != 0 ||
	fread (&ndStart, sizeof (ndStart), 1, masterf) != 1 ||
	(long)(master = (wayType *)mmap (NULL, ndStart, PROT_READ,
					 MAP_SHARED, fileno (masterf), 
					 0)) == -1) {
      fprintf (stderr, "Unable to open %s for bbox rebuild\n",masterpakfile);
      return 4;
    }
  }
  
  if (!(pak = fopen64 (pakfile, "w+"))) {
    fprintf (stderr, "Cannot create %s\n",pakfile);
    return 2;
  }
  fwrite (&pakHead, sizeof (pakHead), 1, pak);

  //------------------------ elemstylesfile : -----------------------------
  styleStruct srec[2 << STYLE_BITS];
  elemstyleMapping map[2 << STYLE_BITS];
  memset (&srec, 0, sizeof (srec));
  memset (&map, 0, sizeof (map));
  
  int styleCnt = LoadElemstyles(elemstylefile, iconscsvfile, srec, map,
				firstElemStyle);
  fwrite (&srec, sizeof (srec[0]), styleCnt + 1, pak);    

  //------------------ OSM Data File (/dev/stdin) : ------------------------
  xmlTextReaderPtr xml = xmlReaderForFd (STDIN_FILENO, "", NULL, 0);
  FILE *groupf[PAIRGROUP2 (0) + PAIRGROUPS2];
  char groupName[PAIRGROUP2 (0) + PAIRGROUPS2][9];
  for (int i = 0; i < PAIRGROUP2 (0) + PAIRGROUPS2; i++) {
    sprintf (groupName[i], "%c%c%d.tmp", i / 26 % 26 + 'a', i % 26 + 'a',
	     i / 26 / 26);
    if (i < S2GROUP (0) && !(groupf[i] = fopen64 (groupName[i], "w+"))) {
      fprintf (stderr, "Cannot create temporary file.\n"
	       "Possibly too many open files, in which case you must run "
	       "ulimit -n or recompile\n");
      return 9;
      
    }
  }
  
#if 0 // For making sure we have a Hilbert curve
  bucketsMin1 = MAX_BUCKETS - 1;
  for (int x = 0; x < 16; x++) {
    for (int y = 0; y < 16; y++) {
      printf ("%7d ", Hash (x << TILEBITS, y << TILEBITS));
    }
    printf ("\n");
  }
#endif
    
  nodeType nd;
  halfSegType s[2];
  int nOther = 0, lowzOther = FIRST_LOWZ_OTHER, isNode = 0;
  int yesMask = 0, noMask = 0, *wayFseek = NULL;
  int lowzList[1000], lowzListCnt = 0, wStyle = styleCnt, ref = 0, role = 0;
  int member[2], relationType = 0;
  vector<int> wayId, wayMember, cycleNet;
  s[0].lat = 0; // Should be -1 ?
  s[0].other = -2;
  s[1].other = -1;
  wayType w;
  w.clat = 0;
  w.clon = 0;
  w.dlat = INT_MIN;
  w.dlon = INT_MIN;
  w.bits = 0;
  w.destination = 0;
  
  // if we are doing a second pass bbox rebuild
  if (master) {
    masterWayType *masterWay = (masterWayType *) 
      malloc (sizeof (*masterWay) * (ndStart / (sizeof (wayType) + 4)));
    
    unsigned i = 0, offset = ftell (pak), wcnt;
    wayType *m = (wayType *)(((char *)master) + offset);
    for (wcnt = 0; (char*) m < (char*) master + ndStart; wcnt++) {
      if (bbox[0] <= m->clat + m->dlat && bbox[1] <= m->clon + m->dlon &&
	  m->clat - m->dlat <= bbox[2] && m->clon - m->dlon <= bbox[3] &&
	  StyleNr (m) < styleCnt) {
	masterWay[i].idx = wcnt;
	masterWay[i++].w = m;
      }
      m = (wayType*)((char*)m +
		     ((1 + strlen ((char*)(m + 1) + 1) + 1 + 3) & ~3)) + 1;
    }
    qsort (masterWay, i, sizeof (*masterWay), MasterWayCmp);
    assert (wayFseek = (int*) calloc (sizeof (*wayFseek),
				      ndStart / (sizeof (wayType) + 4)));
    for (unsigned j = 0; j < i; j++) {
      wayFseek[masterWay[j].idx] = offset;
      offset += sizeof (*masterWay[j].w) +
	((1 + strlen ((char*)(masterWay[j].w + 1) + 1) + 1 + 3) & ~3);
    }
    wayFseek[wcnt] = offset;
    fflush (pak);
    ftruncate (fileno (pak), offset); // fflush first ?
    free (masterWay);
    fseek (pak, *wayFseek, SEEK_SET);
  }
  
  char *tag_k = NULL, *tags = (char *) BAD_CAST xmlStrdup (BAD_CAST "");
  char *nameTag = NULL;
  REBUILDWATCH (while (xmlTextReaderRead (xml))) {
    char *name = (char *) BAD_CAST xmlTextReaderName (xml);
    //xmlChar *value = xmlTextReaderValue (xml); // always empty
    if (xmlTextReaderNodeType (xml) == XML_READER_TYPE_ELEMENT) {
      isNode = stricmp (name, "way") != 0 && stricmp (name, "relation") != 0
	&& (stricmp (name, "node") == 0 || isNode);
      while (xmlTextReaderMoveToNextAttribute (xml)) {
	char *aname = (char *) BAD_CAST xmlTextReaderName (xml);
	char *avalue = (char *) BAD_CAST xmlTextReaderValue (xml);
	//        if (xmlStrcasecmp (name, "node") == 0) 
	if (stricmp (aname, "id") == 0) nd.id = atoi (avalue);
	if (stricmp (aname, "lat") == 0) nd.lat = Latitude (atof (avalue));
	if (stricmp (aname, "lon") == 0) nd.lon = Longitude (atof (avalue));
	if (stricmp (aname, "ref") == 0) ref = atoi (avalue);
	if (stricmp (aname, "type") == 0) relationType = avalue[0];
	if (stricmp (aname, "role") == 0) role = avalue[0];

#define K_IS(x) (stricmp (tag_k, x) == 0)
#define V_IS(x) (stricmp (avalue, x) == 0)

	if (stricmp (aname, "v") == 0) {
	  if (K_IS ("route") && V_IS ("bicycle")) {
	    cycleNet.insert (cycleNet.end (), wayMember.begin (), 
			     wayMember.end ());
	  }
	  if ((!wayFseek || *wayFseek) &&
	      (K_IS ("lcn_ref") || K_IS ("rcn_ref") || K_IS ("ncn_ref"))) {
	    cycleNet.push_back (ftell (pak));
	  }
          
	  int newStyle = 0;
	  // TODO: this for loop could be clearer as a while
	  for (; newStyle < styleCnt && !(K_IS (map[newStyle].style_k) &&
					  (map[newStyle].style_v[0] == '\0' || V_IS (map[newStyle].style_v)) &&
					  (isNode ? srec[newStyle].x[2] :
					   srec[newStyle].lineColour != -1 ||
					   srec[newStyle].areaColour != -1)); newStyle++) {}
	  // elemstyles rules are from most important to least important
	  // Ulf has placed rules at the beginning that will highlight
	  // errors, like oneway=true -> icon=deprecated. So they must only
	  // match nodes when no line or area colour was given and only
	  // match ways when no icon was given.
	  if (K_IS ("junction") && V_IS ("roundabout")) {
	    yesMask |= (1 << onewayR) | (1 << roundaboutR);
	  }
	  else if (newStyle < styleCnt && 
		   (wStyle == styleCnt || 
		    map[wStyle].ruleNr > map[newStyle].ruleNr)) {
	    wStyle = newStyle;
	  }
	  
	  if (K_IS ("name")) {
	    nameTag = avalue;
	    avalue = (char*) xmlStrdup (BAD_CAST "");
	  }
	  else if (K_IS ("ref")) {
	    xmlChar *tmp = xmlStrdup (BAD_CAST "\n");
	    tmp = xmlStrcat (BAD_CAST tmp, BAD_CAST avalue);
	    avalue = tags; // Old 'tags' will be freed
	    tags = (char*) xmlStrcat (tmp, BAD_CAST tags);
	    // name always first tag.
	  }
	  else if (K_IS ("layer")) w.bits |= atoi (avalue) << 29;
          
#define M(field) else if (K_IS (#field)) {				\
	    if (V_IS ("yes") || V_IS ("1") || V_IS ("permissive") ||	\
		V_IS ("true")) {					\
	      yesMask |= 1 << field ## R;				\
	    } else if (V_IS ("no") || V_IS ("0") || V_IS ("private")) { \
	      noMask |= 1 << field ## R;				\
	    }								\
	    else if (V_IS ("destination")) {				\
	      yesMask |= 1 << field ## R;				\
	      w.destination |= 1 << field ## R;				\
	    }								\
	  }
	  RESTRICTIONS
#undef M
	    
	  else if (!V_IS ("no") && !V_IS ("false") && 
		   !K_IS ("sagns_id") && !K_IS ("sangs_id") && 
		   !K_IS ("is_in") && !V_IS ("residential") &&
		   !V_IS ("unclassified") && !V_IS ("tertiary") &&
		   !V_IS ("secondary") && !V_IS ("primary") && // Esp. ValidateMode
		   !V_IS ("junction") && /* Not approved and when it isn't obvious
					    from the ways that it's a junction, the tag will 
					    often be something ridiculous like 
					    junction=junction ! */
		   // blocked as highway:  !V_IS ("mini_roundabout") && !V_IS ("roundabout") &&
		   !V_IS ("traffic_signals") && !K_IS ("editor") &&
		   !K_IS ("class") /* esp. class=node */ &&
		   !K_IS ("type") /* This is only for boules, but we drop it
				     because it's often misused */ &&
		   !V_IS ("National-Land Numerical Information (Railway) 2007, MLIT Japan") &&
		   !V_IS ("National-Land Numerical Information (Lake and Pond) 2005, MLIT Japan") &&
		   !V_IS ("National-Land Numerical Information (Administrative area) 2007, MLIT Japan") &&
		   !V_IS ("coastline_old") &&
		   !K_IS ("upload_tag") && !K_IS ("admin_level") &&
		   (!isNode || (!K_IS ("highway") && !V_IS ("water") &&
				!K_IS ("abutters") && !V_IS ("coastline")))) {
	    // First block out tags that will bloat the index, will not make
	    // sense or are implied.
	    
	    // tags = xmlStrcat (tags, tag_k); // with this it's
	    // tags = xmlStrcat (tags, "="); // it's amenity=fuel
	    tags = (char *) xmlStrcat (BAD_CAST tags, BAD_CAST "\n");
	    tags = (char *) BAD_CAST xmlStrcat (BAD_CAST tags,  
						V_IS ("yes") || V_IS ("1") || V_IS ("true")
						? BAD_CAST tag_k : BAD_CAST avalue);
	  }
	}
	if (stricmp (aname, "k") == 0) {
	  xmlFree (tag_k);
	  tag_k = avalue;
	  if (strncasecmp (tag_k, "tiger:", 6) == 0 ||
	      K_IS ("created_by") || K_IS ("converted_by") ||
	      strncasecmp (tag_k, "source", 6) == 0 ||
	      strncasecmp (tag_k, "AND_", 4) == 0 ||
	      strncasecmp (tag_k, "AND:", 4) == 0 ||
	      strncasecmp (tag_k, "KSJ2:", 4) == 0 || K_IS ("note:ja") ||
	      K_IS ("attribution") /* Mostly MassGIS */ ||
	      K_IS ("time") || K_IS ("ele") || K_IS ("hdop") ||
	      K_IS ("sat") || K_IS ("pdop") || K_IS ("speed") ||
	      K_IS ("course") || K_IS ("fix") || K_IS ("vdop")) {
	    xmlFree (aname);
	    break;
	  }
	}
	else xmlFree (avalue);
	xmlFree (aname);
      } /* While it's an attribute */
      if (relationType == 'w' && stricmp (name, "member") == 0) {
	for (unsigned i = 0; i < wayId.size (); i += 2) {
	  if (ref == wayId[i]) wayMember.push_back (wayId[i + 1]);
	}
      }
      if (!wayFseek || *wayFseek) {
	if (stricmp (name, "member") == 0 && role != 'v') {
	  for (unsigned i = 0; i < wayId.size (); i += 2) {
	    if (ref == wayId[i]) member[role == 'f' ? 0 : 1] = wayId[i + 1];
	  }
	}
	else if (stricmp (name, "nd") == 0 ||
		 stricmp (name, "member") == 0) {
	  if (s[0].lat) {
	    fwrite (s, sizeof (s), 1, groupf[S1GROUP (s[0].lat)]);
	  }
	  s[0].wayPtr = ftell (pak);
	  s[1].wayPtr = TO_HALFSEG;
	  s[1].other = s[0].other + 1;
	  s[0].other = nOther++ * 2;
	  s[0].lat = ref;
	  if (lowzListCnt >=
	      int (sizeof (lowzList) / sizeof (lowzList[0]))) lowzListCnt--;
	  lowzList[lowzListCnt++] = ref;
	}
      }
      if (stricmp (name, "node") == 0 && bbox[0] <= nd.lat &&
	  bbox[1] <= nd.lon && nd.lat <= bbox[2] && nd.lon <= bbox[3]) {
	fwrite (&nd, sizeof (nd), 1, groupf[NGROUP (nd.id)]);
      }
    }
    if (xmlTextReaderNodeType (xml) == XML_READER_TYPE_END_ELEMENT) {
      int nameIsNode = stricmp (name, "node") == 0;
      int nameIsRelation = stricmp (name, "relation") == 0;
      if (nameIsRelation) wayMember.clear ();
      if (stricmp (name, "way") == 0 || nameIsNode || nameIsRelation) {
	w.bits += wStyle;
	if (!nameIsRelation && !nameIsNode) {
	  wayId.push_back (nd.id);
	  wayId.push_back (ftell (pak));
	}
	if (nameIsRelation) {
	  xmlFree (nameTag);
	  char str[21];
	  sprintf (str, "%d %d", member[0], member[1]);
	  nameTag = (char *) xmlStrdup (BAD_CAST str);
	}
	if (nameTag) {
	  char *oldTags = tags;
	  tags = (char *) xmlStrdup (BAD_CAST "\n");
	  tags = (char *) xmlStrcat (BAD_CAST tags, BAD_CAST nameTag);
	  tags = (char *) xmlStrcat (BAD_CAST tags, BAD_CAST oldTags);
	  xmlFree (oldTags);
	  xmlFree (nameTag);
	  nameTag = NULL;
	}
	if (!nameIsNode || strlen (tags) > 8 || wStyle != styleCnt) {
	  if (nameIsNode && (!wayFseek || *wayFseek)) {
	    if (s[0].lat) { // Flush s
	      fwrite (s, sizeof (s), 1, groupf[S1GROUP (s[0].lat)]);
	    }
	    s[0].lat = nd.id; // Create 2 fake halfSegs
	    s[0].wayPtr = ftell (pak);
	    s[1].wayPtr = TO_HALFSEG;
	    s[0].other = -2; // No next
	    s[1].other = -1; // No prev
	    lowzList[lowzListCnt++] = nd.id;
	  }
	  if (s[0].other > -2) { // Not lowz
	    if (s[0].other >= 0) nOther--; // Reclaim unused 'other' number
	    s[0].other = -2;
	  }
	  
	  if (srec[StyleNr (&w)].scaleMax > 10000000 &&
	      (!wayFseek || *wayFseek)) {
	    for (int i = 0; i < lowzListCnt; i++) {
	      if (i % 10 && i < lowzListCnt - 1) continue; // Skip some
	      if (s[0].lat) { // Flush s
		fwrite (s, sizeof (s), 1, groupf[S1GROUP (s[0].lat)]);
	      }
	      s[0].lat = lowzList[i];
	      s[0].wayPtr = ftell (pak);
	      s[1].wayPtr = TO_HALFSEG;
	      s[1].other = i == 0 ? -4 : lowzOther++;
	      s[0].other = i == lowzListCnt -1 ? -4 : lowzOther++;
	    }
	  }
	  lowzListCnt = 0;
          
	  if (StyleNr (&w) < styleCnt && stricmp (map[StyleNr (&w)].style_v,
						  "city") == 0 && tags[0] == '\n') {
	    int nlen = strcspn (tags + 1, "\n");
	    char *n = (char *) xmlMalloc (strlen (tags) + 1 + nlen + 5 + 1);
	    strcpy (n, tags);
	    memcpy (n + strlen (tags), tags, 1 + nlen);
	    strcpy (n + strlen (tags) + 1 + nlen, " City");
	    //fprintf (stderr, "Mark : %s\n", n + strlen (tags) + 1);
	    xmlFree (tags);
	    tags = n; 
	  }
	  w.bits |= ~noMask & (yesMask | (map[StyleNr (&w)].defaultRestrict &
					  ((noMask & (1 << accessR)) ? (1 << onewayR) : ~0)));
	  if (w.destination & (1 << accessR)) w.destination = ~0;
	  char *compact = tags[0] == '\n' ? tags + 1 : tags;
	  if (!wayFseek || *wayFseek) {
	    fwrite (&w, sizeof (w), 1, pak);
	    fwrite (tags + strlen (tags), 1, 1, pak); // '\0' at the front
	    for (char *ptr = tags; *ptr != '\0'; ) {
	      if (*ptr++ == '\n') {
		unsigned idx = ftell (pak) + ptr - 1 - tags, grp;
		for (grp = 0; grp < IDXGROUPS - 1 &&
		       TagCmp (groupName[grp], ptr) < 0; grp++) {}
		fwrite (&idx, sizeof (idx), 1, groupf[grp]);
	      }
	    }
	    fwrite (compact, strlen (compact) + 1, 1, pak);
            
	    // Write variable length tags and align on 4 bytes
	    if (ftell (pak) & 3) {
	      fwrite (tags, 4 - (ftell (pak) & 3), 1, pak);
	    }
	  }
	  if (wayFseek) fseek (pak, *++wayFseek, SEEK_SET);
	  //xmlFree (tags); // Just set tags[0] = '\0'
	  //tags = (char *) xmlStrdup (BAD_CAST "");
	}
	tags[0] = '\0'; // Erase nodes with short names
	yesMask = noMask = 0;
	w.bits = 0;
	w.destination = 0;
	wStyle = styleCnt;
      }
    } // if it was </...>
    xmlFree (name);
  } // While reading xml
  wayId.clear ();
  if (s[0].lat && (!wayFseek || *wayFseek)) {
    fwrite (s, sizeof (s), 1, groupf[S1GROUP (s[0].lat)]);
  }
  assert (nOther * 2 < FIRST_LOWZ_OTHER);
  bucketsMin1 = (nOther >> 5) | (nOther >> 4);
  bucketsMin1 |= bucketsMin1 >> 2;
  bucketsMin1 |= bucketsMin1 >> 4;
  bucketsMin1 |= bucketsMin1 >> 8;
  bucketsMin1 |= bucketsMin1 >> 16;
  assert (bucketsMin1 < MAX_BUCKETS);
  
  for (int i = 0; i < IDXGROUPS; i++) fclose (groupf[i]);
  for (int i = S2GROUP (0); i < PAIRGROUP2 (0) + PAIRGROUPS2; i++) {
    assert (groupf[i] = fopen64 (groupName[i], "w+"));
  } // Avoid exceeding ulimit
  
  nodeType *nodes = (nodeType *) malloc (sizeof (*nodes) * MAX_NODES);
  if (!nodes) {
    fprintf (stderr, "Out of memory. Reduce MAX_NODES and increase GRPs\n");
    return 3;
  }
  for (int i = NGROUP (0); i < NGROUP (0) + NGROUPS; i++) {
    rewind (groupf[i]);
    memset (nodes, -1, sizeof (*nodes) * MAX_NODES);
    REBUILDWATCH (while (fread (&nd, sizeof (nd), 1, groupf[i]) == 1)) {
      memcpy (FindNode (nodes, nd.id), &nd, sizeof (nd));
    }
    fclose (groupf[i]);
    unlink (groupName[i]);
    rewind (groupf[i + NGROUPS]);
    REBUILDWATCH (while (fread (s, sizeof (s), 1, groupf[i + NGROUPS])
			 == 1)) {
      nodeType *n = FindNode (nodes, s[0].lat);
      //if (n->id == -1) printf ("** Undefined node %d\n", s[0].lat);
      s[0].lat = s[1].lat = n->id != -1 ? n->lat : INT_MIN;
      s[0].lon = s[1].lon = n->id != -1 ? n->lon : INT_MIN;
      fwrite (s, sizeof (s), 1,
	      groupf[-2 <= s[0].other && s[0].other < FIRST_LOWZ_OTHER
		     ? S2GROUP (Hash (s[0].lon, s[0].lat)) : PAIRGROUP (0) - 1]);
    }
    fclose (groupf[i + NGROUPS]);
    unlink (groupName[i + NGROUPS]);
  }
  free (nodes);
  
  struct {
    int nOther1, final;
  } offsetpair;
  offsetpair.final = 0;
  
  hashTable = (int *) malloc (sizeof (*hashTable) *
			      (bucketsMin1 + (bucketsMin1 >> 7) + 3));
  int bucket = -1;
  for (int i = S2GROUP (0); i < S2GROUP (0) + S2GROUPS; i++) {
    fflush (groupf[i]);
    size_t size = ftell (groupf[i]);
    rewind (groupf[i]);
    REBUILDWATCH (halfSegType *seg = (halfSegType *) mmap (NULL, size,
							   PROT_READ | PROT_WRITE, MAP_SHARED, fileno (groupf[i]), 0));
    qsort (seg, size / sizeof (s), sizeof (s),
	   (int (*)(const void *, const void *))HalfSegCmp);
    for (int j = 0; j < int (size / sizeof (seg[0])); j++) {
      if (!(j & 1)) {
	while (bucket < Hash (seg[j].lon, seg[j].lat,
			      i >= S2GROUP (0) + S2GROUPS - 1)) {
	  hashTable[++bucket] = offsetpair.final / 2;
	}
      }
      offsetpair.nOther1 = seg[j].other;
      if (seg[j].other >= 0) fwrite (&offsetpair, sizeof (offsetpair), 1,
				     groupf[PAIRGROUP (offsetpair.nOther1)]);
      offsetpair.final++;
    }
    munmap (seg, size);
  }
  while (bucket < bucketsMin1 + (bucketsMin1 >> 7) + 2) {
    hashTable[++bucket] = offsetpair.final / 2;
  }
  
  ndStart = ftell (pak);
  
  int *pairing = (int *) malloc (sizeof (*pairing) * PAIRS);
  for (int i = PAIRGROUP (0); i < PAIRGROUP (0) + PAIRGROUPS; i++) {
    REBUILDWATCH (rewind (groupf[i]));
    while (fread (&offsetpair, sizeof (offsetpair), 1, groupf[i]) == 1) {
      pairing[offsetpair.nOther1 % PAIRS] = offsetpair.final;
    }
    int pairs = ftell (groupf[i]) / sizeof (offsetpair);
    for (int j = 0; j < pairs; j++) {
      offsetpair.final = pairing[j ^ 1];
      offsetpair.nOther1 = pairing[j];
      fwrite (&offsetpair, sizeof (offsetpair), 1,
	      groupf[PAIRGROUP2 (offsetpair.nOther1)]);
    }
    fclose (groupf[i]);
    unlink (groupName[i]);
  }
  free (pairing);
  
  int s2grp = S2GROUP (0), pairs;
  halfSegType *seg = (halfSegType *) malloc (PAIRS * sizeof (*seg));
  assert (seg /* Out of memory. Reduce PAIRS for small scale rebuilds. */);
  ndType ndWrite;
  for (int i = PAIRGROUP2 (0); i < PAIRGROUP2 (0) + PAIRGROUPS2; i++) {
    REBUILDWATCH (for (pairs = 0; pairs < PAIRS &&
			 s2grp < S2GROUP (0) + S2GROUPS; )) {
      if (fread (&seg[pairs], sizeof (seg[0]), 2, groupf [s2grp]) == 2) {
	pairs += 2;
      }
      else {
	fclose (groupf[s2grp]);
	unlink (groupName[s2grp]);
	s2grp++;
      }
    }
    rewind (groupf[i]);
    while (fread (&offsetpair, sizeof (offsetpair), 1, groupf[i]) == 1) {
      seg[offsetpair.nOther1 % PAIRS].other = offsetpair.final;
    }
    for (int j = 0; j < pairs; j += 2) {
      ndWrite.wayPtr = seg[j].wayPtr;
      ndWrite.lat = seg[j].lat;
      ndWrite.lon = seg[j].lon;
      ndWrite.other[0] = seg[j].other >> 1; // Right shift handles -1 the
      ndWrite.other[1] = seg[j + 1].other >> 1; // way we want.
      fwrite (&ndWrite, sizeof (ndWrite), 1, pak);
    }
    fclose (groupf[i]);
    unlink (groupName[i]);
  }
  free (seg);
  
  fflush (pak);
  data = (char *) mmap (NULL, ndStart,
			PROT_READ | PROT_WRITE, MAP_SHARED, fileno (pak), 0);
  fseek (pak, ndStart, SEEK_SET);
  REBUILDWATCH (for (unsigned i = 0; i < cycleNet.size (); i++)) {
    wayType *way = (wayType*) (data + cycleNet[i]);
    for (int j = StyleNr (way) + 1; j < styleCnt; j++) {
      if (strncasecmp (map[j].style_k, "cyclenet", 8) == 0 &&
	  stricmp (map[j].style_k + 8, map[StyleNr (way)].style_k) == 0 &&
	  stricmp (map[j].style_v, map[StyleNr (way)].style_v) == 0) {
	way->bits = (way->bits & ~((2 << STYLE_BITS) - 1)) | j;
      }
    }
  }
  REBUILDWATCH (while (fread (&ndWrite, sizeof (ndWrite), 1, pak) == 1)) {
    //if (bucket > Hash (ndWrite.lon, ndWrite.lat)) printf ("unsorted !\n");
    wayType *way = (wayType*) (data + ndWrite.wayPtr);
    
    /* The difficult way of calculating bounding boxes,
       namely to adjust the centerpoint (it does save us a pass) : */
    // Block lost nodes with if (ndWrite.lat == INT_MIN) continue;
    if (way->clat + way->dlat < ndWrite.lat) {
      way->dlat = way->dlat < 0 ? 0 : // Bootstrap
	(way->dlat - way->clat + ndWrite.lat) / 2;
      way->clat = ndWrite.lat - way->dlat;
    }
    if (way->clat - way->dlat > ndWrite.lat) {
      way->dlat = (way->dlat + way->clat - ndWrite.lat) / 2;
      way->clat = ndWrite.lat + way->dlat;
    }
    if (way->clon + way->dlon < ndWrite.lon) {
      way->dlon = way->dlon < 0 ? 0 : // Bootstrap
	(way->dlon - way->clon + ndWrite.lon) / 2;
      way->clon = ndWrite.lon - way->dlon;
    }
    if (way->clon - way->dlon > ndWrite.lon) {
      way->dlon = (way->dlon + way->clon - ndWrite.lon) / 2;
      way->clon = ndWrite.lon + way->dlon;
    }
  }
#ifndef LAMBERTUS
  REBUILDWATCH (for (int i = 0; i < IDXGROUPS; i++)) {
    assert (groupf[i] = fopen64 (groupName[i], "r+"));
    fseek (groupf[i], 0, SEEK_END);
    int fsize = ftell (groupf[i]);
    fflush (groupf[i]);
    unsigned *idx = (unsigned *) mmap (NULL, fsize,
				       PROT_READ | PROT_WRITE, MAP_SHARED, fileno (groupf[i]), 0);
    qsort (idx, fsize / sizeof (*idx), sizeof (*idx), IdxCmp);
    fwrite (idx, fsize, 1, pak);
#if 0
    for (int j = 0; j < fsize / (int) sizeof (*idx); j++) {
      printf ("%.*s\n", strcspn (data + idx[j], "\n"), data + idx[j]);
    }
#endif
    munmap (idx, fsize);
    fclose (groupf[i]);
    unlink (groupName[i]);
  }
#endif // LAMBERTUS
  //    printf ("ndCount=%d\n", ndCount);
  munmap (data, ndStart);
  fwrite (hashTable, sizeof (*hashTable),
	  bucketsMin1 + (bucketsMin1 >> 7) + 3, pak);
  fwrite (&bucketsMin1, sizeof (bucketsMin1), 1, pak);
  fwrite (&ndStart, sizeof (ndStart), 1, pak); /* for ndBase */

  fclose (pak);
  free (hashTable);

  return 0;
}

#endif
