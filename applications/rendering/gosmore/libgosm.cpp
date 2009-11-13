#ifndef _WIN32
#include <sys/mman.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <sys/types.h>
#endif

#include <stdio.h>
#include <unistd.h>
#include <vector>
#include <deque>
#include <string>
#include <map>
#include <assert.h>
#include <float.h>
using namespace std;

#include "libgosm.h"

routeNodeType *route = NULL, *shortest = NULL;
routeHeapType *routeHeap;
long dhashSize, dLength;
int routeHeapSize, tlat, tlon, flat, flon, rlat, rlon, dlon16348, dlat16348;
int *hashTable, bucketsMin1, pakHead = 0xEB3A943, gosmStyleCnt;
char *gosmData, *gosmSstr[searchCnt];

ndType *ndBase;
styleStruct *style;
wayType *gosmSway[searchCnt];

// store the maximum speeds (over all waytypes) of each vehicle type
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
          gosmSstr[ipos] = gosmSstr[ipos - 1];
        }
        char *tag = gosmData + idx[m[dir]];
        gosmSstr[ipos] = tag;
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
              gosmSstr[ipos] = gosmSstr[ipos - 1];
            }
            gosmSway[ipos] = w;
            dista[ipos] = d;
            gosmSstr[ipos] = gosmData + idx[s - 1];
          }
        } // For each entry in the square
      } // For each of the 4 squares
      break; // count < searchCnt implies a bug. Don't loop infinitely.
    } // If the search list is filled by tags with this text
    count = c;
  } // For each
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

   We call nodes that recently had their "best" increased "active". The
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

void GosmFreeRoute (void)
{
  if (route) {
  #ifndef _WIN32
    munmap (route, (sizeof (*route)) * dhashSize);
  #else
    free (route);
  #endif
    free (routeHeap);
    route = NULL;
  }    
}

#ifndef _WIN32_WCE
#define RT_BLK_SIZE (unsigned long) 0x3ffff
#define RT_WIDTH 53400000
// Specifies maximum deviation from the direct path / as the crow flies
#define RT_LENGTH_SHR 2
/* Within a block we allocate roughly 1 routeNodeType for every 
     (RT_WIDTH / 100 / 100) << RT_LENGTH_SHR square meter of space.
   As the actual density approach this, some routes will not be considered.
*/
#else
#define RT_BLK_SIZE 0xffff
#endif

inline long HeapCmp (routeHeapType *a, routeHeapType *b)
{
  return ((a->r - route) ^ (b->r - route)) & ~RT_BLK_SIZE
    ? a->r - b->r : a->best - b->best;
}

routeNodeType *AddNd (ndType *nd, int dir, int cost, routeNodeType *newshort)
{ /* This function is called when we find a valid route that consists of the
     segments (hs, hs->other), (newshort->hs, newshort->hs->other),
     (newshort->shortest->hs, newshort->shortest->hs->other), .., 'to'
     with cost 'cost'.
     
     When cost is -1, this function just returns the entry for nd without
     modifying anything. */
  if (nd->lat == INT_MIN) return NULL; // Nodes missing from OSM-XML
  int offcenter = ((nd->lat - tlat) * (__int64) dlon16348 -
                   (nd->lon - tlon) * (__int64) dlat16348) >> 14;
  if (offcenter < -RT_WIDTH / 2 || offcenter > RT_WIDTH / 2) return NULL;
  unsigned i = 0, hash = (intptr_t) nd / 10 + dir;
  unsigned dst = (unsigned)(((nd->lat - tlat) * (__int64) dlat16348 +
                    (nd->lon - tlon) * (__int64) dlon16348) >> 14) + 6200000;
  if (dst > (unsigned) (6200000 * 2 + dLength)) return NULL;
  // Either too far beyond 'to' or too far beyond 'from'

  routeNodeType *n, *base = route +
    ((unsigned long)(dst >> RT_LENGTH_SHR) & ~RT_BLK_SIZE);
  // The value for route computed above works, but on short routes it
  // sometimes takes longer because it starts with the region beyond 'to'
  // and on longer routes it it is not realistic to expect the user to wait
  // a couple of minutes for a result. So we just use the first rt_block:
  base = route;

  do {
    if (i++ > 10) {
      //fprintf (stderr, "Double hash bailout : Table full. %9d %p\n",
      //  routeHeapSize, nd);
      // If you get the
      return NULL;
    }
    n = base + ((/*(hash >> 16) ^ */hash) & RT_BLK_SIZE);

    hash -= hash << 6;
    hash ^= hash >> 17;
    hash -= hash << 9;
    hash ^= hash << 4;
    hash -= hash << 3;
    hash ^= hash << 10;
    hash ^= hash >> 15;

//    hash = (unsigned) (hash * (__int64) 1664525 + 1013904223);
            
    if (n->nd == NULL) { /* First visit of this node */
      if (cost < 0) return NULL;
      if ((unsigned) routeHeapSize >= RT_BLK_SIZE) {
        printf ("Route Heap too big\n");
        return NULL;
      }
      n->nd = nd;
      routeHeap[routeHeapSize].best = 0x7fffffff;
      /* Will do later : routeHeap[routeHeapSize].r = n; */
      n->heapIdx = routeHeapSize++;
      n->dir = dir;
      n->remain = lrint (sqrt (Sqr ((__int64)(nd->lat - rlat)) +
                               Sqr ((__int64)(nd->lon - rlon))));
      if (!shortest || n->remain < shortest->remain) shortest = n;

      ROUTE_SET_ADDND_COUNT (routeAddCnt + 1);
    }
  } while (n->nd != nd || n->dir != dir);

  routeHeapType h;
  h.r = n;
  h.best = cost + n->remain - (!newshort ? 0 : newshort->heapIdx < 0
    ? newshort->remain + newshort->heapIdx
    : newshort->remain - routeHeap[newshort->heapIdx].best);
  // TODO make sure newshort is never in the heap and exploit it.
  if (cost >= 0 && (n->heapIdx < 0 ? -n->heapIdx : routeHeap[n->heapIdx].best)
                   > h.best) {
    n->shortest = newshort;
    if (n->heapIdx < 0) n->heapIdx = routeHeapSize++;
    for (; n->heapIdx > 1 && HeapCmp (&h, &routeHeap[n->heapIdx / 2]) < 0;
                    n->heapIdx /= 2) {
      memcpy (routeHeap + n->heapIdx, routeHeap + n->heapIdx / 2,
        sizeof (*routeHeap));
      routeHeap[n->heapIdx].r->heapIdx = n->heapIdx;
    }
    memcpy (&routeHeap[n->heapIdx], &h, sizeof (routeHeap[n->heapIdx]));
  }
  return n;
}

inline int IsOneway (wayType *w, int Vehicle)
{
  return Vehicle != footR &&
    (w->bits & (1 << (Vehicle == bicycleR ? bicycleOneway : onewayR)));
  //!((Vehicle == footR || Vehicle == bicycleR) &&
  //  (w->bits & (1 << motorcarR))) && (w->bits & (1<<onewayR));
}

static const int rhdBbox[][4] = {
  { Longitude (10.2), Latitude (-85.0), Longitude (42.1), Latitude (4.7) },
  // Africa. Not correct for Angola, DRC and a few other poor countries.
  { Longitude (-14.11), Latitude (49.83), Longitude (1.84), Latitude (60.03) },
  // UK & IE
  { Longitude (68.0), Latitude (0.0), Longitude (90.2), Latitude (31.4) },
  // India & Sri Lanka
  { Longitude (9.3), Latitude (-85.0), Longitude (179.0), Latitude (19.1) },
  // Aus, NZ, Indonesia, Malazia, Thailand.
  { Longitude (129.55), Latitude (18.0), Longitude (145.84), Latitude (45.55) }
  // Japan
};

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
      if (itr.nd[0]->other[0] == 0) continue;
      __int64 lon0 = lon - itr.nd[0]->lon, lat0 = lat - itr.nd[0]->lat,
              lon1 = lon - (itr.nd[0] + itr.nd[0]->other[0])->lon,
              lat1 = lat - (itr.nd[0] + itr.nd[0]->other[0])->lat,
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
    GosmFreeRoute ();
    dLength = lrint (sqrt (Sqr ((__int64)(flat - tlat)) +
                               Sqr ((__int64)(flon - tlon))));
    dlat16348 = ((__int64)(flat - tlat) << 14) / dLength;
    dlon16348 = ((__int64)(flon - tlon) << 14) / dLength;
    routeHeapSize = 1; /* Leave position 0 open to simplify the math */
    routeHeap = (routeHeapType*) malloc (RT_BLK_SIZE *
      sizeof (*routeHeap)); //(route + dhashSize) - 1;
    dLength += 6200000*2; // Padding near 'from' and 'to'
    #if 0 // See AddNd() why we don't do this currently
    dhashSize = ((dLength >> RT_LENGTH_SHR) + RT_BLK_SIZE) & ~RT_BLK_SIZE;
    // Round up
    #else
    dhashSize = RT_BLK_SIZE;
    #endif
    #ifndef _WIN32
    #if 1 // If you have lots of swap space
    int dzero = open ("/dev/zero", O_RDWR);
    if (!routeHeap || dzero == -1 ||
    #else // If you have lots of disk space
    int dzero = open ("route.tmp", O_CREAT | O_RDWR | O_TRUNC, 0600);
    if (!routeHeap || dzero == -1 || ftruncate (dzero,
                                 dhashSize * sizeof (*route)) != 0 ||
    #endif
        (route = (routeNodeType*) mmap (NULL, dhashSize * sizeof (*route),
        PROT_READ | PROT_WRITE, MAP_SHARED, dzero, 0)) == MAP_FAILED) {
      fprintf (stderr, "Error: Mmap of dnull for routing arrays\n");
      route = NULL;
      return;
    }
    if (dzero != -1) close (dzero);
    #else
/*    RT_BLK_SIZE should be variable for this case...
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
    #endif */

    while (dhashSize > 0 && !(route = (routeNodeType*)
        malloc ((sizeof (*route)) * dhashSize))) {
      if(1) { // Needs variable RT_BLK_SIZE
        free (routeHeap);
        return;
      }
      dhashSize = dhashSize / 4 * 3;
    }
    memset (route, 0, (sizeof (*route)) * dhashSize); 
    #endif

    rlat = flat;
    rlon = flon;
    AddNd (endNd[0], 0, toEndNd[0][0], NULL);
    AddNd (endNd[0] + endNd[0]->other[0], 1, toEndNd[0][1], NULL);
    AddNd (endNd[0], 1, toEndNd[0][0], NULL);
    AddNd (endNd[0] + endNd[0]->other[0], 0, toEndNd[0][1], NULL);

    #ifdef INSPECT
    printf ("\ncycleonewa: %s\n",
      Way (endNd[0])->bits & (1 << bicycleOneway) ? "yes" : "no");
    #define M(x) printf ("%10s: %s\n", #x, \
                   Way (endNd[0])->bits & (1 << x ## R) ? "yes" : "no");
    RESTRICTIONS
    #undef M
    // A bit confusing when the user clicks a way on which the selected
    // vehicle type is not allowed, because endNd will then be a different
    // way.
    #endif
  }
  else {
    routeNodeType *frn = AddNd (&from, 0, -1, NULL);
    if (frn) {
      if (frn->heapIdx < 0) frn->heapIdx = -0x7fffffff;
      else routeHeap[frn->heapIdx].best = 0x7fffffff;
    }

    routeNodeType *rn = AddNd (endNd[1], 0, -1, NULL);
    if (rn) AddNd (&from, 0, toEndNd[1][1], rn);
    routeNodeType *rno = AddNd (endNd[1] + endNd[1]->other[0], 1, -1, NULL);
    if (rno) AddNd (&from, 0, toEndNd[1][0], rno);
  }
  int rhd = FALSE;
  for (size_t i = 0; i < sizeof (rhdBbox) / sizeof (rhdBbox[0]); i++) {
    rhd = rhd || (rhdBbox[i][0] < tlon && rhdBbox[i][1] < tlat &&
                  tlon < rhdBbox[i][2] && tlat < rhdBbox[i][3]);
  }
  //printf (rhd ? "Right Hand Drive\n" : "Left Hand Drive\n");
  
  while (routeHeapSize > 1) {
    routeNodeType *root = routeHeap[1].r;
    root->heapIdx = -routeHeap[1].best; /* Root now removed from the heap */
    routeHeapSize--;
    for (int i = 2; i / 2 < routeHeapSize; ) {
      routeHeapType *end = &routeHeap[routeHeapSize];
      int sml = i >= routeHeapSize || HeapCmp (&routeHeap[i], end) > 0
        ? (i + 1 >= routeHeapSize || HeapCmp (&routeHeap[i], end) > 0
           ? routeHeapSize : i + 1)
        : HeapCmp (&routeHeap[i], &routeHeap[i + 1]) < 0 ? i : i + 1;
      memcpy (routeHeap + i / 2, routeHeap + sml, sizeof (*routeHeap));
      routeHeap[i / 2].r->heapIdx = i / 2;
      i = sml * 2;
    }
    if (root->nd == &from) { // Remove 'from' from the heap in case we
      shortest = root->shortest; // get called with recalculate=0
      break;
    }
    if (root->nd == (!root->dir ? endNd[1] : endNd[1] + endNd[1]->other[0])) {
      AddNd (&from, 0, toEndNd[1][1 - root->dir], root);
    }
    ndType *nd = root->nd, *other, *firstNd, *restrictItr;
    while (nd > ndBase && nd[-1].lon == nd->lon &&
      nd[-1].lat == nd->lat) nd--; /* Find first nd in node */
    firstNd = nd; // Save it for checking layout and restrictions
    int rootIsAdestination = Way (root->nd)->destination & (1 << Vehicle);
    /* Now work through the segments connected to root. */
    
    unsigned layout[4] = { 0, 0, 0, 0 }, lmask = 1;
    ndType *rtother = root->nd + root->nd->other[root->dir], *layoutNd[4];
    do {
      lmask <<= 2;
      for (int dir = 0; dir < 2; dir++) {
        if (nd->other[dir] != 0) {
          other = nd + nd->other[dir];
          int dot = (other->lat - nd->lat) * (nd->lat - rtother->lat) +
                    (other->lon - nd->lon) * (nd->lon - rtother->lon);
          int cross = (other->lat - nd->lat) * (nd->lon - rtother->lon) -
                      (other->lon - nd->lon) * (nd->lat - rtother->lat);
          int azimuth = (dot > cross ? 0 : 3) ^ (dot + cross > 0 ? 0 : 1);
          layout[azimuth] |= lmask << dir;
          layoutNd[azimuth] = nd;
        }
      }
    } while (++nd < ndBase + hashTable[bucketsMin1 + 1] &&
             nd->lon == nd[-1].lon && nd->lat == nd[-1].lat);
             
    //printf ("%d %d %d %d\n", layout[0], layout[1], layout[2], layout[3]);
    nd = firstNd;
    lmask = 1;
    do {
      if (StyleNr (Way (nd)) >= barrier_bollard &&
          StyleNr (Way (nd)) <= barrier_toll_booth &&
          !(Way (nd)->bits & (1 << Vehicle))) break;
      lmask <<= 2;
      if (root->remain > 500000 && -root->heapIdx - root->remain > 500000 &&
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
        
        if (nd->other[dir] == 0) continue;
        if (Vehicle != footR && Vehicle != bicycleR) {
          for (restrictItr = firstNd; restrictItr->other[0] == 0 &&
                          restrictItr->other[1] == 0; restrictItr++) {
            wayType *w  = Way (restrictItr);
            if (StyleNr (w) >= restriction_no_right_turn &&
                StyleNr (w) <= restriction_only_straight_on &&
                atoi ((char*)(w + 1) + 1) == nd->wayPtr &&
            (StyleNr (w) <= restriction_no_straight_on) ==
            (atoi (strchr ((char*)(w + 1) + 1, ' ')) == root->nd->wayPtr)) {
              break;
            }
          }
          if (restrictItr->other[0] == 0 &&
              restrictItr->other[1] == 0) continue;
        }
        // Tagged node, start or end of way or restriction.
        
        other = nd + nd->other[dir];
        wayType *w = Way (nd);
        int myV = Vehicle == bicycleR && (!(w->bits & (1 << bicycleR)) 
          || ((w->bits & (1 << bicycleOneway)) && !dir)) ? footR : Vehicle;
        // If pedestrians are allowed and cyclists not, we can dismount and
        // walk. The same applies when we can't cycle in the wrong direction.
        if ((w->bits & (1 << myV)) && (dir || !IsOneway (w, myV))) {
          int d = lrint (sqrt ((double)
            (Sqr ((__int64)(nd->lon - other->lon)) +
             Sqr ((__int64)(nd->lat - other->lat)))) *
            (!fast ? 1.0 : Style (w)->invSpeed[Vehicle]));
          if (Vehicle != myV) d *= 4; // Penalty for dismounting
          if (rootIsAdestination && !(w->destination & (1 << Vehicle))) {
            d += 5000000; // 500km penalty for entering v='destination' area.
          }
          
          // If (lmask<<dir)&layout[x] is set, we are going approximately
          // in direction x * 90 degrees, relative to the direction we
          // are going to (Remember that we are coming from 'root')
          // If layout[x] is not zero, there are segments going in that
          // direction
          if (layout[rhd ? 1 : 3] && ((lmask << dir) & layout[rhd ? 3 : 1])
              && fast && Style (w)->scaleMax > 100000) {
            d += 100000 * (fast ? Style (w)->invSpeed[Vehicle] : 1);
          // Turning right in the UK (or left in the rest of the world), when
          // we are on a major road (secondary+) that continues straight on,
          // you will probably have to wait for oncoming traffic.
          }
          
          if (layout[1] && layout[3] && ((lmask << dir) & layout[0])) {
            // Straight over a T-junction
            if ((Way (layoutNd[1])->bits & (1 << motorcarR)) &&
                (Way (layoutNd[3])->bits & (1 << motorcarR)) && fast) {
            // And motorcars are allowed on both sides
              d += (Style (Way (layoutNd[1]))->invSpeed[motorcarR] <
                    Style (w)->invSpeed[motorcarR] ? 50000 : 9000) *
                    (fast ? Style (w)->invSpeed[Vehicle] : 1);
            // Crossing a road that is faster that the road we are traveling
            // on incurs a 500m penalty. If they are equal, the penality is
            // 90m. TODO: residential crossing residential should be less,
            // perhaps 20m.
            }
          }
          AddNd (other, 1 - dir, d, root);
        } // If we found a segment we may follow
      } // for each direction
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
    if (nd->other[0] != 0) segCnt++;
    if (nd->other[1] != 0) segCnt++;
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
  ndBase = (ndType *)(gosmData + ((int*)(gosmData + size))[-1]);
  bucketsMin1 = ((int *) (gosmData + size))[-2];
  gosmStyleCnt = ((int *) (gosmData + size))[-3];
  style = (struct styleStruct *)
    (gosmData + size - sizeof (gosmStyleCnt) * 3) - gosmStyleCnt;
  hashTable = (int *) (style) - bucketsMin1 - (bucketsMin1 >> 7) - 3;
  
  memset (gosmSway, 0, sizeof (gosmSway));

  return ndBase && hashTable && *(int*) gosmData == pakHead;
}

// *** EVERYTHING AFTER THIS POINT IS NOT IN THE WINDOWS BUILDS ***

#ifndef _WIN32

void CalculateInvSpeeds (styleStruct *srec, int styleCnt)
{
  // for vehicle
  for (int i = 0; i < layerBit1; i++) {
    double maxspeed = 0; // More vehicle limit than (legal) maxspeed.
    for (int j = 0; j < styleCnt; j++) {
      maxspeed = max (srec[j].aveSpeed[i], maxspeed);
    }
    // for style
    for (int j = 0; j < styleCnt; j++) {
      // if no speed is defined for a vehicle on this style, then
      // set the aveSpeed to be the maximum of any other
      // vehicles. This speed will only be used if vehicle=yes is
      // defined on the way. (e.g. highway=foot motorcar=yes)
      if (srec[j].aveSpeed[i] == 0) { 
        for (int k = 0; k < layerBit1; k++) {
          if (srec[j].aveSpeed[i] < srec[j].aveSpeed[k]) {
            srec[j].aveSpeed[i] = srec[j].aveSpeed[k];
          } 
        } // without breaking the normal maximum speed for this vehicle
        if (srec[j].aveSpeed[i] > maxspeed) {
          srec[j].aveSpeed[i] = maxspeed;
        }
      }
      // store the proportion of maxspeed for routing
      srec[j].invSpeed[i] = maxspeed / srec[j].aveSpeed[i];
    }
  }
}

void GosmLoadAltStyle(const char* elemstylefile, const char* iconscsvfile) {
//  static styleStruct srec[2 << STYLE_BITS];
  elemstyleMapping map[2 << STYLE_BITS]; // this is needed for
					 // LoadElemstyles but ignored
  styleStruct *old = style;
  style = (styleStruct*) malloc (gosmStyleCnt * sizeof (*style)); // Mem leak
  memcpy (style, old, gosmStyleCnt * sizeof (*style));
  //memset (&srec, 0, sizeof (srec)); // defined globally
  memset (&map, 0, sizeof (map));
  LoadElemstyles(elemstylefile, iconscsvfile, style, map);
  CalculateInvSpeeds (style, gosmStyleCnt);
}

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

int LoadElemstyles(/* in */ const char *elemstylesfname, 
		   const char *iconsfname,
		   /* out */ styleStruct *srec, elemstyleMapping *map)
{
   //------------------------- elemstyle.xml : --------------------------
    int ruleCnt = 0, styleCnt = firstElemStyle;
    // zero-out elemstyle-to-stylestruct mappings
    FILE *icons_csv = fopen (iconsfname, "r");
    xmlTextReaderPtr sXml = xmlNewTextReaderFilename (elemstylesfname);
    if (!sXml || !icons_csv) {
      fprintf (stderr, "Either icons.csv or elemstyles.xml not found\n");
      return 3;
    }
    styleStruct s;
    memset (&s, 0, sizeof (s));
    s.lineColour = -1;
    s.areaColour = -1;

    for (int i = 0; i < styleCnt; i++) memcpy (&srec[i], &s, sizeof (s));

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
    /* These strcpys are necessary because elemstyles does not contain them */
    
    while (xmlTextReaderRead (sXml)) {
      char *name = (char*) xmlTextReaderName (sXml);
      //xmlChar *val = xmlTextReaderValue (sXml);
      if (xmlTextReaderNodeType (sXml) == XML_READER_TYPE_ELEMENT) {
        if (strcasecmp (name, "scale_max") == 0) {
          while (xmlTextReaderRead (sXml) && // memory leak :
            xmlStrcmp (xmlTextReaderName (sXml), BAD_CAST "#text") != 0) {}
          s.scaleMax = atoi ((char *) xmlTextReaderValue (sXml));
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
              s.lineWidth = atoi (v);
            }
            if (strcasecmp (n, "realwidth") == 0) {
              s.lineRWidth = atoi (v);
            }
            if (strcasecmp (n, "colour") == 0) {
              sscanf (v, "#%x", &s.lineColour);
            }
            if (strcasecmp (n, "colour_bg") == 0) {
              sscanf (v, "#%x", &s.lineColourBg);
            }
            s.dashed = s.dashed ||
              (strcasecmp (n, "dashed") == 0 && strcasecmp (v, "true") == 0);
          }
          if (strcasecmp (name, "area") == 0) {
            if (strcasecmp (n, "colour") == 0) {
              sscanf (v, "#%x", &s.areaColour);
            }
          }
          if (strcasecmp (name, "icon") == 0) {
            if (strcasecmp (n, "src") == 0) {
              /*while (v[strcspn ((char *) v, "/ ")]) {
                v[strcspn ((char *) v, "/ ")] = '_';
              }*/
              char line[400], fnd = FALSE;
              static const char *set[] = { "map-icons/classic.big/",
                "map-icons/classic.small/", "map-icons/square.big/",
                "map-icons/square.small/" };
              for (int i = 0; i < 4; i++) {
                s.x[i * 4 + 2] = s.x[i * 4 + 3] = 1;
              // Default to 1x1 dummys
                int slen = strlen (set[i]), vlen = strlen (v);
                rewind (icons_csv);
                while (fgets (line, sizeof (line) - 1, icons_csv)) {
                  if (strncmp (line, set[i], slen) == 0 &&
                      strncmp (line + slen, v, vlen - 1) == 0) {
                    sscanf (line + slen + vlen, ":%d:%d:%d:%d",
                      s.x + i * 4,     s.x + i * 4 + 1,
                      s.x + i * 4 + 2, s.x + i * 4 + 3);
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
              s.aveSpeed[field ## R] = atof (v); \
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
          memcpy (&srec[ipos], &s, sizeof (srec[ipos]));
          map[ipos].defaultRestrict = map[styleCnt].defaultRestrict;
          map[styleCnt].defaultRestrict = 0;
          strcpy(map[ipos].style_k,map[styleCnt].style_k);
          strcpy(map[ipos].style_v,map[styleCnt].style_v);
        }
        else if (styleCnt < (2 << STYLE_BITS) - 2) {
          memcpy (&srec[styleCnt++], &s, sizeof (srec[0]));
        }
        else fprintf (stderr, "Too many rules. Increase STYLE_BITS\n");

        memset (&s, 0, sizeof (s));
        s.lineColour = -1;
        s.areaColour = -1;
      }
      xmlFree (name);
      //xmlFree (val);      
    }

    xmlFreeTextReader (sXml);

    return styleCnt;
}

struct ltstr
{
  bool operator ()(const char *a, const char *b) const
  {
    return strcmp (a, b) < 0;
  }
};

struct k2vType {
  map<const char *, const char *, ltstr> m;
  const char *operator[](const char *k) const
  {
    return m.find (k) == m.end () ? NULL : m.find (k)->second;
  } // For std:map the operator[] is not const, so we wrap it in a new class
};

//----------------------------[ Osm2Gosmore ]-----------------------------
// This function translates the complicated and ever changing language of
// OSM into the superfast Gosmore language.
//
// Before Gosmore calls this function it will have matched the tags to
// an entry in elemstyles.xml and filled in 's'. If there are multiple
// matches, the first one is chosen. You can modify the values of 's',
// but be aware that the number of different 's' records are limited. So
// you should map (quantitize) rarely used values (like 38 km/h) to more
// frequently used values (like 40 km/h).
//
// It will also have filled in the access and destination fields of w.
// During a second pass, the center point of w will also be valid. So
// you can test if the way was inside any object for which you have data,
// like a city. Or you can adjust the average speed of unpaved ways based on
// an average annual rainfall map.
//
// You must return a list of strings which will be concatenated into the
// storage of the object and indexed for searching purposes. Normally each
// string will occupy one line (end in '\n'). For
// example { "Macdonalds\n", "restaurant\n" }. 
// Empty lines are removed and will not be indexed. And strings can span
// any number of lines. So
//   { "Jerusalem\nar:", "al-Quds\n" } is equivalent to
//   { "Jerusalem\n", "\nar:", "al-Quds\n" }
// and it means that the object can be searched for as Jerusalem and al-Quds.
// Furthermore, a clever renderer can then render the correct name.
// A single line can also be indexed multiple times. For example
// { "city:", "Paris\n" } will be indexed as "Paris" and "city:Paris".
//
// NOTE: Gosmore currently requires that you return the same strings during
// both passes of the rebuild, i.e. the strings cannot depend on w.c{lat,lon}

deque<string> Osm2Gosmore (k2vType &k2v, wayType &w, styleStruct &s,
  int isNode, int isRelation)
{
  deque<string> result;
  // First add name and 'ref' to the front so that they are displayed
  if (k2v["name"]) {
    result.push_front (string (k2v["name"]) + "\n");
    if (k2v["place"] && strcmp (k2v["place"], "city") == 0) {
      result.push_back ("city:" + string (k2v["name"]) + "\n");
    }
  }
  if (k2v["ref"]) result.push_back (string (k2v["ref"]) + "\n");
  map<const char *, const char *, ltstr>::iterator i = k2v.m.begin ();
  // Go through all the tags and add all the interesting things to 'result'
  // so that they will be indexed.
  for (; i != k2v.m.end (); i++) {
    if (strcmp (i->first, "name") != 0 &&
        strcmp (i->first, "ref") != 0 &&
        strncasecmp (i->first, "tiger:", 6) != 0 &&
        strcmp (i->first, "created_by") != 0 &&
        strcmp (i->first, "converted_by") != 0 &&
        strncasecmp (i->first, "source", 6) != 0 &&
        strncasecmp (i->first, "AND_", 4) != 0 &&
        strncasecmp (i->first, "AND:", 4) != 0 &&
        strncasecmp (i->first, "kms:", 4) != 0 &&
        strncasecmp (i->first, "LandPro08:", 10) != 0 &&
        strncasecmp (i->first, "NHD:", 4) != 0 &&
        strncasecmp (i->first, "massgis:", 8) != 0 &&
        strcmp (i->first, "addr:street") != 0 &&
        strcmp (i->first, "addr:postcode") != 0 &&
        strcmp (i->first, "addr:state") != 0 &&
        strcmp (i->first, "addr:city") != 0 &&
        strcmp (i->first, "addr:country") != 0 &&        
        strncasecmp (i->first, "KSJ2:", 5) != 0 && 
        strncasecmp (i->first, "geobase:", 8)  != 0 &&
        strncasecmp (i->first, "kms:", 4)  != 0 &&
        strncasecmp (i->first, "openGeoDB:", 10)  != 0 &&
        strncasecmp (i->first, "gnis:", 5)  != 0 &&
        strcmp (i->first, "note:ja") != 0 &&
        strcmp (i->first, "attribution") /* Mostly MassGIS */ != 0 &&
        strcmp (i->first, "layer") != 0 &&
        strcmp (i->first, "access") != 0 &&
        strcmp (i->first, "motorcar") != 0 &&
        strcmp (i->first, "bicycle") != 0 &&
        strcmp (i->first, "foot") != 0 &&
        strcmp (i->first, "goods") != 0 &&
        strcmp (i->first, "hgv") != 0 &&
        strcmp (i->first, "horse") != 0 &&
        strcmp (i->first, "motorcycle") != 0 &&
        strcmp (i->first, "psv") != 0 &&
        strcmp (i->first, "moped") != 0 &&
        strcmp (i->first, "mofa") != 0 &&
        strcmp (i->first, "motorboat") != 0 &&
        strcmp (i->first, "boat") != 0 &&
        strcmp (i->first, "oneway") != 0 &&
        strcmp (i->first, "roundabout") != 0 &&
        strcmp (i->first, "time") != 0 &&
        strcmp (i->first, "ele") != 0 &&
        strcmp (i->first, "hdop") != 0 &&
        strcmp (i->first, "sat") != 0 &&
        strcmp (i->first, "pdop") != 0 &&
        strcmp (i->first, "speed") != 0 &&
        strcmp (i->first, "course") != 0 &&
        strcmp (i->first, "fix") != 0 &&
        strcmp (i->first, "vdop") != 0 &&
        strcmp (i->second, "no") != 0      &&
        strcmp (i->second, "false") != 0 &&
        strcmp (i->first, "sagns_id") != 0 &&
        strcmp (i->first, "sangs_id") != 0 &&
        strcmp (i->first,"is_in") !=0 &&
        strcmp (i->second, "residential") != 0 &&
        strcmp (i->second, "unclassified") != 0 &&
        strcmp (i->second, "tertiary") != 0 &&
        strcmp (i->second, "secondary") != 0 && 
        strcmp (i->second, "primary") != 0 && // Esp. ValidateMode
        strcmp (i->second, "junction") != 0 && 
   /* Not approved and when it isn't obvious
      from the ways that it's a junction, the tag will 
      often be something ridiculous like 
      junction=junction ! */
        // blocked as highway:  strcmp (i->second, "mini_roundabout") != 0
        //                      && strcmp (i->second, "roundabout") != 0
        strcmp (i->second, "traffic_signals") != 0 &&
        strcmp (i->first, "editor") != 0 &&
        strcmp (i->first, "class") != 0 /* esp. class=node */ &&
        strcmp (i->first, "type") != 0 &&
        /* "type=..." is only allow for boules grounds. We block it because it
        is often misused. */
         0 != strcmp (i->second, 
  "National-Land Numerical Information (Railway) 2007, MLIT Japan") &&
         0 != strcmp (i->second, 
  "National-Land Numerical Information (Lake and Pond) 2005, MLIT Japan") &&
         0 != strcmp (i->second, 
  "National-Land Numerical Information (Administrative area) 2007, MLIT Japan") &&
         strcmp (i->second, "coastline_old") != 0 &&
         strcmp (i->first, "upload_tag") != 0 &&
         strcmp (i->first, "admin_level") != 0 &&
         (!isNode || (strcmp (i->first, "highway") != 0 &&
                      strcmp (i->second, "water") != 0 &&
                      strcmp (i->first, "abutters") != 0 &&
                      strcmp (i->second, "coastline") != 0))) {
      result.push_back (string (strcmp (i->second, "true") == 0 ||
        strcmp (i->second, "yes") == 0 || strcmp (i->second, "1") == 0
        ? i->first : i->second) + "\n");
    }
  }
  // Reduce the aveSpeeds when maxspeed mandates it
  if (k2v["maxspeed"] && isdigit (k2v["maxspeed"][0])) {
    const char *m = k2v["maxspeed"];
    double maxs = atof (m), best = 30, m2km = 1.609344;
    if (tolower (m[strcspn (m, "KMPSkmps")]) == 'm') maxs *= m2km;
    // Here we find the first alphabetic character and compare it to 'm'
    int v[] = { 5,7,10,15,20,30,32,40,50,60,70,80,90,100,110,120,130 };
    for (unsigned i = 0; i < sizeof (v) / sizeof (v); i++) {
      if (fabs (maxs - best) > fabs (maxs - v[i])) best = v[i];
      if (fabs (maxs - best) > fabs (maxs - v[i] * m2km)) best = v[i] * m2km;
    }
    s.aveSpeed[accessR] = best;
    for (int i = 0; i < layerBit1; i++) {
      if (s.aveSpeed[i] > best) s.aveSpeed[i] = best;
    }
  }
  // Now adjust for track type.
  if (k2v["tracktype"] && isdigit (k2v["tracktype"][5])) {
    s.aveSpeed[motorcarR] *= ('6' - k2v["tracktype"][5]) / 5.0;
    // Alternatively use ... (6 - atoi (k2v["tracktype"] + 5)) / 5.0;
    // TODO: Mooaar
  }
  if ((w.bits & (1 << onewayR)) && !(k2v["cycleway"] &&
    (strcmp (k2v["cycleway"], "opposite_lane") == 0 ||
     strcmp (k2v["cycleway"], "opposite_track") == 0 ||
     strcmp (k2v["cycleway"], "opposite") == 0))) {
    // On oneway roads, cyclists are only allowed to go in the opposite
    // direction, if the cycleway tag exist and starts with "opposite"
    w.bits |= 1 << bicycleOneway;
  }
//  printf ("%.5lf %.5lf\n", LonInverse (w.clon), LatInverse (w.clat));
  return result;
}

#define FWRITEY(y) { perror ("fwrite @ " #y); exit (1); }
#define FWRITEX(x) FWRITEY (x) // Use argument prescan feature of CPP
#define FWRITE(addr,size,count,f) { if (fwrite (addr, size, count, f) \
                                      != (size_t)(count)) FWRITEX (__LINE__) }

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
  FWRITE (&pakHead, sizeof (pakHead), 1, pak);

  //------------------------ elemstylesfile : -----------------------------
  styleStruct srec[2 << STYLE_BITS];
  elemstyleMapping eMap[2 << STYLE_BITS];
  memset (&srec, 0, sizeof (srec));
  memset (&eMap, 0, sizeof (eMap));
  
  int elemCnt = LoadElemstyles(elemstylefile, iconscsvfile,
				srec, eMap), styleCnt = elemCnt;
  
  
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
  int yesMask = 0, noMask = 0;
  struct {
    wayType *w; // Pointer to the first version in the master file.
    int off;
  } *wayFseek = NULL;
  int lowzList[1000], lowzListCnt = 0, wStyle = elemCnt, ref = 0, role = 0;
  int member[2], relationType = 0, onewayReverse = 0;
  vector<int> wayMember, cycleNet;
  map<int,int> wayId;
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
	  m->clat - m->dlat <= bbox[2] && m->clon - m->dlon <= bbox[3]) {
	masterWay[i].idx = wcnt;
	masterWay[i++].w = m;
      }
      m = (wayType*)((char*)m +
		     ((1 + strlen ((char*)(m + 1) + 1) + 1 + 3) & ~3)) + 1;
    }
    qsort (masterWay, i, sizeof (*masterWay), MasterWayCmp);
    assert (wayFseek = (typeof (wayFseek)) calloc (sizeof (*wayFseek),
				      ndStart / (sizeof (wayType) + 4)));
    for (unsigned j = 0; j < i; j++) {
      wayFseek[masterWay[j].idx].off = offset;
      wayFseek[masterWay[j].idx].w = masterWay[j].w;
      offset += sizeof (*masterWay[j].w) +
	((1 + strlen ((char*)(masterWay[j].w + 1) + 1) + 1 + 3) & ~3);
    }
    wayFseek[wcnt].off = offset;
    fflush (pak);
    if (ftruncate (fileno (pak), offset) != 0) perror ("ftruncate");
    free (masterWay);
    fseek (pak, wayFseek->off, SEEK_SET);
  }
  
  char *tag_k = NULL; //, *tags = (char *) BAD_CAST xmlStrdup (BAD_CAST "");
  //char *nameTag = NULL;
  k2vType k2v;
  deque<int> wayNd;
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
	  if ((!wayFseek || wayFseek->off) &&
	      (K_IS ("lcn_ref") || K_IS ("rcn_ref") || K_IS ("ncn_ref"))) {
	    cycleNet.push_back (ftell (pak));
	  }
          
	  int newStyle = 0;
	  // TODO: this for loop could be clearer as a while
	  for (; newStyle < elemCnt && !(K_IS (eMap[newStyle].style_k) &&
					  (eMap[newStyle].style_v[0] == '\0' || V_IS (eMap[newStyle].style_v)) &&
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
	  else if (newStyle < elemCnt && 
		   (wStyle == elemCnt || 
		    eMap[wStyle].ruleNr > eMap[newStyle].ruleNr)) {
	    wStyle = newStyle;
	  }
	  
	  if (K_IS ("layer")) w.bits |= atoi (avalue) << 29;
          
#define M(field) else if (K_IS (#field)) {				\
	    if (V_IS ("yes") || V_IS ("1") || V_IS ("permissive") ||	\
		V_IS ("true") || V_IS ("designated") ||			\
		V_IS ("official") || V_IS ("-1")) {			\
	      yesMask |= 1 << field ## R;				\
	      if (K_IS ("oneway") && V_IS ("-1")) onewayReverse = TRUE; \
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
	    
          k2v.m[tag_k] = avalue; // Will be freed after Osm2Gosmore()
	}
	else if (stricmp (aname, "k") == 0) tag_k = avalue;
	else xmlFree (avalue); // Not "k" or "v"
	
	xmlFree (aname);
      } /* While it's an attribute */
      if (relationType == 'w' && stricmp (name, "member") == 0) {
	map<int,int>::iterator refId = wayId.find (ref);
	if (refId != wayId.end ()) wayMember.push_back (refId->second);
      }
      if (!wayFseek || wayFseek->off) {
	if (stricmp (name, "member") == 0 && role != 'v') {
          map<int,int>::iterator refId = wayId.find (ref);
          if (refId != wayId.end ()) member[role == 'f' ? 0 : 1] = refId->second;
	}
	else if (stricmp (name, "nd") == 0 ||
		 stricmp (name, "member") == 0) wayNd.push_back (ref);
      }
      if (stricmp (name, "node") == 0 && bbox[0] <= nd.lat &&
	  bbox[1] <= nd.lon && nd.lat <= bbox[2] && nd.lon <= bbox[3]) {
	FWRITE (&nd, sizeof (nd), 1, groupf[NGROUP (nd.id)]);
      }
    }
    if (xmlTextReaderNodeType (xml) == XML_READER_TYPE_END_ELEMENT) {
      int nameIsNode = stricmp (name, "node") == 0;
      int nameIsRelation = stricmp (name, "relation") == 0;
      if (nameIsRelation) wayMember.clear ();
      if (stricmp (name, "way") == 0 || nameIsNode || nameIsRelation) {
	if (!nameIsRelation && !nameIsNode) {
	  wayId[nd.id] = ftell (pak);
	}
	if (nameIsRelation) {
	  //xmlFree (nameTag);
	  char str[22];
	  sprintf (str, "%d %d", member[0], member[1]);
	  k2v.m[(char *) xmlStrdup (BAD_CAST "name")] =
	    (char *) xmlStrdup (BAD_CAST str);
	  //nameTag = (char *) xmlStrdup (BAD_CAST str);
	}
	/*if (nameTag) {
	  char *oldTags = tags;
	  tags = (char *) xmlStrdup (BAD_CAST "\n");
	  tags = (char *) xmlStrcat (BAD_CAST tags, BAD_CAST nameTag);
	  tags = (char *) xmlStrcat (BAD_CAST tags, BAD_CAST oldTags);
	  xmlFree (oldTags);
	  xmlFree (nameTag);
	  nameTag = NULL;
	}*/
	if (wStyle == elemCnt) wayNd.clear ();
	else {
	  s[0].other = -2;
	  while (!wayNd.empty ()) {
            s[0].wayPtr = ftell (pak);
            s[1].wayPtr = TO_HALFSEG;
            s[1].other = s[0].other + 1;
            s[0].lat = onewayReverse ? wayNd.back () : wayNd.front ();
            if (lowzListCnt >=
                int (sizeof (lowzList) / sizeof (lowzList[0]))) lowzListCnt--;
            lowzList[lowzListCnt++] = s[0].lat;
            if (onewayReverse) wayNd.pop_back ();
            else wayNd.pop_front ();
            s[0].other = wayNd.empty () ? -2 : nOther++ * 2;
            FWRITE (s, sizeof (s), 1, groupf[S1GROUP (s[0].lat)]);
          }
	  if (nameIsNode && (!wayFseek || wayFseek->off)) {
	    s[0].lat = nd.id; // Create 2 fake halfSegs
	    s[0].wayPtr = ftell (pak);
	    s[1].wayPtr = TO_HALFSEG;
	    s[0].other = -2; // No next
	    s[1].other = -1; // No prev
	    lowzList[lowzListCnt++] = nd.id;
            FWRITE (s, sizeof (s), 1, groupf[S1GROUP (s[0].lat)]);
	  }
	  
	  w.bits |= ~noMask & (yesMask | (eMap[wStyle].defaultRestrict &
					  ((noMask & (1 << accessR)) ? (1 << onewayR) : ~0)));
	  if (w.destination & (1 << accessR)) w.destination = ~0;
	  memcpy (&srec[styleCnt], &srec[wStyle], sizeof (srec[0]));
	  if (wayFseek && wayFseek->off) {
	    w.clat = wayFseek->w->clat;
	    w.clon = wayFseek->w->clon;
	    w.dlat = wayFseek->w->dlat;
	    w.dlon = wayFseek->w->dlon;
          }
	  deque<string> tags = Osm2Gosmore (k2v, w, srec[styleCnt], isNode,
	    nameIsRelation);
	  while (memcmp (&srec[styleCnt], &srec[wStyle], sizeof (srec[0]))
	         != 0) wStyle++;
          w.bits += wStyle;
          if (wStyle == styleCnt) {
            if (styleCnt == (2 << STYLE_BITS) - 2) {
              fprintf (stderr, "*** Warning: Too many styles !!!\n");
            }
            if (styleCnt < (2 << STYLE_BITS) - 1) styleCnt++;
          }
          
	  if (srec[StyleNr (&w)].scaleMax > 10000000 &&
	      (!wayFseek || wayFseek->off)) { 
	    for (int i = 0; i < lowzListCnt; i++) {
	      if (i % 10 && i < lowzListCnt - 1) continue; // Skip some
	      s[0].lat = lowzList[i];
	      s[0].wayPtr = ftell (pak);
	      s[1].wayPtr = TO_HALFSEG;
	      s[1].other = i == 0 ? -4 : lowzOther++;
	      s[0].other = i == lowzListCnt -1 ? -4 : lowzOther++;
              FWRITE (s, sizeof (s), 1, groupf[S1GROUP (s[0].lat)]);
	    }
	  }
	  lowzListCnt = 0;
          
	  /*if (StyleNr (&w) < elemCnt && stricmp (eMap[StyleNr (&w)].style_v,
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
	  char *compact = tags[0] == '\n' ? tags + 1 : tags; */
	  if (!wayFseek || wayFseek->off) {
	    FWRITE (&w, sizeof (w), 1, pak);
	    FWRITE ("", 1, 1, pak); // '\0' at the front
	    unsigned newln = 0;
	    for (; !tags.empty (); tags.pop_front ()) {
	      if (newln) FWRITE ("\n", 1, 1, pak);
	      const char *compact = tags.front().c_str ();
	      if (tags.front ()[0] == '\n') compact++;
	      else {
	        // If all the idxes went into a single file, this can
	        // be simplified a lot. One day when RAM is cheap.
	        string line;
	        deque<string>::iterator tItr = tags.begin ();
	        do line += *tItr;
	        while (!strchr ((*tItr++).c_str (), '\n') &&
	               tItr != tags.end ());
	        
	        unsigned grp, idx = ftell (pak);
	        for (grp = 0; grp < IDXGROUPS - 1 &&
                 TagCmp (groupName[grp], (char*) line.c_str ()) < 0; grp++) {}
		FWRITE (&idx, sizeof (idx), 1, groupf[grp]);
	      }
	      unsigned l = strlen (compact);
	      newln = compact[l - 1] == '\n' ? 1 : 0;
	      if (l > newln) FWRITE (compact, l - newln, 1, pak);
	    }
	    FWRITE ("", 1, 1, pak); // '\0' at the back
/*	    for (char *ptr = tags; *ptr != '\0'; ) {
	      if (*ptr++ == '\n') {
		unsigned idx = ftell (pak) + ptr - 1 - tags, grp;
		for (grp = 0; grp < IDXGROUPS - 1 &&
		       TagCmp (groupName[grp], ptr) < 0; grp++) {}
		FWRITE (&idx, sizeof (idx), 1, groupf[grp]);
	      }
	    }
	    FWRITE (compact, strlen (compact) + 1, 1, pak); */
            
	    // Write variable length tags and align on 4 bytes
	    if (ftell (pak) & 3) {
	      FWRITE ("   ", 4 - (ftell (pak) & 3), 1, pak);
	    }
	  }
	  if (wayFseek) fseek (pak, (++wayFseek)->off, SEEK_SET);
	  //xmlFree (tags); // Just set tags[0] = '\0'
	  //tags = (char *) xmlStrdup (BAD_CAST "");
	}
	//tags[0] = '\0'; // Erase nodes with short names
	yesMask = noMask = 0;
	w.bits = 0;
	w.destination = 0;
	wStyle = elemCnt;
	onewayReverse = FALSE;
	while (!k2v.m.empty ()) {
	  xmlFree ((char*) k2v.m.begin()->second);
	  xmlFree ((char*) k2v.m.begin()->first);
	  k2v.m.erase (k2v.m.begin ());
	}
      }
    } // if it was </...>
    xmlFree (name);
  } // While reading xml
  wayId.clear ();
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
      FWRITE (s, sizeof (s), 1,
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
      if (seg[j].other >= 0) FWRITE (&offsetpair, sizeof (offsetpair), 1,
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
      FWRITE (&offsetpair, sizeof (offsetpair), 1,
	      groupf[PAIRGROUP2 (offsetpair.nOther1)]);
    }
    fclose (groupf[i]);
    unlink (groupName[i]);
  }
  free (pairing);
  
  int s2grp = S2GROUP (0), pairs, totalp = 0;
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
      ndWrite.other[0] = //seg[j].other >> 1; // Right shift handles -1 the
        seg[j].other < 0 ? 0 : (seg[j].other >> 1) - totalp;
      ndWrite.other[1] = //seg[j + 1].other >> 1; // way we want.
        seg[j + 1].other < 0 ? 0 : (seg[j + 1].other >> 1) - totalp;
      totalp++;
      FWRITE (&ndWrite, sizeof (ndWrite), 1, pak);
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
    for (int j = StyleNr (way) + 1; j < elemCnt; j++) {
      if (strncasecmp (eMap[j].style_k, "cyclenet", 8) == 0 &&
	  stricmp (eMap[j].style_k + 8, eMap[StyleNr (way)].style_k) == 0 &&
	  stricmp (eMap[j].style_v, eMap[StyleNr (way)].style_v) == 0) {
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
    if (fsize > 0) {
      fflush (groupf[i]);
      unsigned *idx = (unsigned *) mmap (NULL, fsize,
  				         PROT_READ | PROT_WRITE, MAP_SHARED, fileno (groupf[i]), 0);
      qsort (idx, fsize / sizeof (*idx), sizeof (*idx), IdxCmp);
      FWRITE (idx, fsize, 1, pak);
#if 0
      for (int j = 0; j < fsize / (int) sizeof (*idx); j++) {
        printf ("%.*s\n", strcspn (data + idx[j], "\n"), data + idx[j]);
      }
#endif
      munmap (idx, fsize);
    }
    fclose (groupf[i]);
    unlink (groupName[i]);
  }
#endif // LAMBERTUS
  //    printf ("ndCount=%d\n", ndCount);
  munmap (data, ndStart);
  FWRITE (hashTable, sizeof (*hashTable),
	  bucketsMin1 + (bucketsMin1 >> 7) + 3, pak);
	  
  CalculateInvSpeeds (srec, styleCnt);
  FWRITE (&srec, sizeof (srec[0]), styleCnt, pak);
  FWRITE (&styleCnt, sizeof(styleCnt), 1, pak); // File ends with these
  FWRITE (&bucketsMin1, sizeof (bucketsMin1), 1, pak); // 3 variables
  FWRITE (&ndStart, sizeof (ndStart), 1, pak); /* for ndBase */

  fclose (pak);
  free (hashTable);

  return 0;
}

#endif
