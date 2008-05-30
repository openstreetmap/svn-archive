/* This software is placed by in the public domain by its authors. */
/* Written by Nic Roets with contribution(s) from Dave Hansen. */

#define WIN32_LEAN_AND_MEAN
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <ctype.h>
#include <assert.h>
#ifndef _WIN32
#include <sys/mman.h>
#include <libxml/xmlreader.h>
#include <arpa/inet.h>
#include <sys/types.h>
#include <sys/socket.h>
#define TEXT(x) x
#else
#include <windows.h>
#define M_PI 3.14159265358979323846 // Not in math ??
#endif
#ifdef _WIN32_WCE
//#include <aygshell.h>
#include <tpcshell.h>
#include <winuserm.h>
#include "resource.h"
typedef int intptr_t;
#else
#include <unistd.h>
#define wchar_t char
#endif
#ifndef TRUE
#define TRUE 1
#define FALSE 0
#endif

#if !defined (HEADLESS) && !defined (_WIN32_WCE)
#include <gtk/gtk.h>
#endif

#ifdef USE_FLITE
#include <flite/flite.h>
extern "C" {
  cst_voice *register_cmu_us_kal (void);
}
#endif
FILE *flitePipe = stdout;

#ifndef _WIN32
#define stricmp strcasecmp
typedef long long __int64;
#else
#define strncasecmp _strnicmp
#define stricmp _stricmp
#define lrint(x) int ((x) < 0 ? (x) - 0.5 : (x) + 0.5) 
// We emulate just enough of gtk to make it work
#endif
#ifdef _WIN32_WCE
#define gtk_widget_queue_clear(x) // After Click() returns we Invalidate
struct GtkWidget { 
  struct {
    int width, height;
  } allocation;
  int window;
};
typedef int GtkComboBox;
struct GdkEventButton {
  int x, y, button;
};
#endif

#define TILEBITS (18)
#define TILESIZE (1<<TILEBITS)
#ifndef INT_MIN
#define INT_MIN -2147483648
#endif

#define RESTRICTIONS M (access) M (bicycle) M (foot) M (goods) M (hgv) \
  M (horse) M (motorcycle) M (motorcar) M (psv) M (motorboat) M (boat) \
  M (oneway)

#define M(field) field ## R,
enum { STYLE_BITS = 8, RESTRICTIONS l1,l2,l3 };
#undef M

struct styleStruct {
  int  x[16], lineWidth, lineRWidth, lineColour, lineColourBg, dashed;
  int  scaleMax, areaColour, dummy /* pad to 8 for 64 bit compatibility */;
  double aveSpeed[l1], invSpeed[l1];
};

struct ndType {
  int wayPtr, lat, lon, other[2];
};

struct wayType {
  int bits;
  int clat, clon, dlat, dlon; /* Centre coordinates and (half)diameter */
};

inline int Layer (wayType *w) { return w->bits >> 29; }

int Latitude (double lat)
{ /* Mercator projection onto a square means we have to clip
     everything beyond N85.05 and S85.05 */
  return lat > 85.051128779 ? 2147483647 : lat < -85.051128779 ? -2147483647 :
    lrint (log (tan (M_PI / 4 + lat * M_PI / 360)) / M_PI * 2147483648.0);
}

int Longitude (double lon)
{
  return lrint (lon / 180 * 2147483648.0);
}

/*---------- Global variables -----------*/
int *hashTable, bucketsMin1;
char *data;
ndType *ndBase;
styleStruct *style;

inline int StyleNr (wayType *w) { return w->bits & ((2 << STYLE_BITS) - 1); }

inline styleStruct *Style (wayType *w) { return &style[StyleNr (w)]; }

int Holes (int x)
{
  x = ((x & 0xff00) << 8) | (x & 0xff);
  x = ((x & 0xf000f0) << 4) | (x & 0xf000f);
  x = ((x & 0xc0c0c0c) << 2) | (x & 0x3030303);
  return ((x & 0xaaaaaaaa) << 1) | (x & 0x55555555);
}

int inline ZEnc (int lon, int lat)
{ // Input as bits : lon15,lon14,...lon0 and lat15,lat14,...,lat0
  int t = (lon << 16) | lat;
  t = (t & 0xff0000ff) | ((t & 0x00ff0000) >> 8) | ((t & 0x0000ff00) << 8);
  t = (t & 0xf00ff00f) | ((t & 0x0f000f00) >> 4) | ((t & 0x00f000f0) << 4);
  t = (t & 0xc3c3c3c3) | ((t & 0x30303030) >> 2) | ((t & 0x0c0c0c0c) << 2);
  return (t & 0x99999999) | ((t & 0x44444444) >> 1) | ((t & 0x22222222) << 1);
} // Output as bits : lon15,lat15,lon14,lat14,...,lon0,lat0

inline int Hash (int lon, int lat, int lowz = 0)
{ /* All the normal tiles (that make up a super tile) are mapped to sequential
     buckets thereby improving caching and reducing the number of disk tracks
     required to render / route through a super tile sized area. 
     The map to sequential buckets is a 2-D Hilbert curve. */
  if (lowz) {
    lon >>= 7;
    lat >>= 7;
  }
  
  int t = ZEnc (lon >> TILEBITS, ((unsigned) lat) >> TILEBITS);
  int s = ((((unsigned)t & 0xaaaaaaaa) >> 1) | ((t & 0x55555555) << 1)) ^ ~t;
  // s=ZEnc(lon,lat)^ZEnc(lat,lon), so it can be used to swap lat and lon.
  #define SUPERTILEBITS (TILEBITS + 8)
  for (int lead = 1 << (SUPERTILEBITS * 2 - TILEBITS * 2); lead; lead >>= 2) {
    if (!(t & lead)) t ^= ((t & (lead << 1)) ? s : ~s) & (lead - 1);
  }

  return (((((t & 0xaaaaaaaa) >> 1) ^ t) + (lon >> SUPERTILEBITS) * 0x00d20381
    + (lat >> SUPERTILEBITS) * 0x75d087d9) &
    (lowz ? bucketsMin1 >> 7 : bucketsMin1)) + (lowz ? bucketsMin1 + 1 : 0);
}

int TagCmp (char *a, char *b)
{ // This works like the ordering of books in a library : We ignore
  // meaningless words like "the", "street" and "north". We (should) also map
  // deprecated words to their new words, like petrol to fuel
  // TODO : We should consider an algorithm like double metasound.
  static char *omit[] = { /* "the", in the middle ?? */ "ave", "avenue", 
    "blvd", "boulevard", "byp", "bypass",
    "cir", "circle", "cres", "crescent", "ct", "court", "ctr", "center",
    "dr", "drive", "hwy", "highway", "ln", "lane", "loop",
    "pass", "pky", "parkway", "pl", "place", "plz", "plaza",
    /* "run" */ "rd", "road", "sq", "square", "st", "street",
    "ter", "terrace", "tpke", "turnpike", /*trce, trace, trl, trail */
    "walk",  "way"
  };
  static char *words[] = { "", "first", "second", "third", "fourth", "fifth",
    "sixth", "seventh", "eighth", "nineth", "tenth", "eleventh", "twelth",
    "thirteenth", "fourteenth", "fifthteenth", "sixteenth", "seventeenth",
    "eighteenth", "nineteenth", "twentieth" };
  static char *teens[] = { "", "", "twenty ", "thirty ", "fourty ",
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

struct OsmItr { // Iterate over all the objects in a square
  ndType *nd[1]; /* Readonly. Either can be 'from' or 'to', but you */
  /* can be guaranteed that nodes will be in hs[0] */
  
  int slat, slon, left, right, top, bottom, tsize; /* Private */
  ndType *end;
  
  OsmItr (int l, int t, int r, int b)
  {
    tsize = r - l > 10000000 ? TILESIZE << 7 : TILESIZE;
    left = l & (~(tsize - 1));
    right = (r + tsize - 1) & (~(tsize-1));
    top = t & (~(tsize - 1));
    bottom = (b + tsize - 1) & (~(tsize-1));
    
    slat = top;
    slon = left - tsize;
    nd[0] = end = NULL;
  }
};

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

enum { langEn, langDe, langEs, langFr, langIt, langNl, numberOfLang };

#define OPTIONS \
  o (FollowGPSr, "?", "?", "?", "?", "?")

#define notImplemented \
  o (English,         "Deutsch", "Español", "Français", "Italiano", \
                      "Nederlands"),\
  o (CommPort,        "?", "?", "?", "?", "?"), \
  o (BaudRate,        "?", "?", "?", "?", "?"), \
  o (OrientNorthward, "?", "?", "?", "?", "?"), \
  o (ZoomInKey,       "?", "?", "?", "?", "?"), \
  o (HideZInButton,   "?", "?", "?", "?", "?"), \
  o (ZoomOutKey,      "?", "?", "?", "?", "?"), \
  o (HideZOutButton,  "?", "?", "?", "?", "?"), \
  o (ShowCompass,     "?", "?", "?", "?", "?"), \
  o (ShowCoordinates, "?", "?", "?", "?", "?"), \
  o (ShowPrecision,   "?", "?", "?", "?", "?"), \
  o (ShowSpeed,       "?", "?", "?", "?", "?"), \
  o (ShowHeading,     "?", "?", "?", "?", "?"), \
  o (ShowElevation,   "?", "?", "?", "?", "?"), \
  o (ShowDate,        "?", "?", "?", "?", "?"), \
  o (ShowTime,        "?", "?", "?", "?", "?"), \
  o (ShowSearchButton,"?", "?", "?", "?", "?"), \
  o (ShowCreatePoint, "?", "?", "?", "?", "?"), \
  o (ConfigureKey,    "?", "?", "?", "?", "?"), \
  o (HideConfButton,  "?", "?", "?", "?", "?"), \
  o (SmallIcons,      "?", "?", "?", "?", "?"), \
  o (SquareIcons,     "?", "?", "?", "?", "?"), \
  o (FastestRoute,    "?", "?", "?", "?", "?"), \
  o (PedestrianRoute, "?", "?", "?", "?", "?")

#define o(en,de,es,fr,it,nl) en
enum { OPTIONS, numberOfOptions };
#undef o

#define o(en,de,es,fr,it,nl) { \
  TEXT (#en), TEXT (de), TEXT (es), TEXT (fr), TEXT (it), TEXT (nl) }
wchar_t *optionNameTable[][numberOfLang] = { OPTIONS };
#undef o

int option[numberOfOptions];

#define optionName(x) optionNameTable[x][option[English]]

#define Sqr(x) ((x)*(x))
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
   are "states", namely the ability to reach nd direkly from nd->other[dir]
*/
struct routeNodeType {
  ndType *nd;
  routeNodeType *shortest;
  int best, heapIdx, dir, remain; // Dir is 0 or 1
} *route = NULL, *shortest = NULL, **routeHeap;
int dhashSize, routeHeapSize, tlat, tlon, flat, flon, car, fastest;

void AddNd (ndType *nd, int dir, int cost, routeNodeType *newshort)
{ /* This function is called when we find a valid route that consists of the
     segments (hs, hs->other), (newshort->hs, newshort->hs->other),
     (newshort->shortest->hs, newshort->shortest->hs->other), .., 'to'
     with cost 'cost'. */
  unsigned hash = (intptr_t) nd / 10 + dir, i = 0;
  routeNodeType *n;
  do {
    if (i++ > 10) {
      //fprintf (stderr, "Double hash bailout : Table full, hash function "
      //  "bad or no route exists\n");
      return;
    }
    n = route + hash % dhashSize;
    /* Linear congruential generator from wikipedia */
    hash = (unsigned) (hash * (__int64) 1664525 + 1013904223);
    if (n->nd == NULL) { /* First visit of this node */
      n->nd = nd;
      n->best = 0x7fffffff;
      /* Will do later : routeHeap[routeHeapSize] = n; */
      n->heapIdx = routeHeapSize++;
      n->dir = dir;
      n->remain = nd == ndBase - 1 ? 0
        : sqrt (Sqr ((__int64)(nd->lat - flat)) +
                Sqr ((__int64)(nd->lon - flon)));
      if (!shortest || n->remain < shortest->remain) shortest = n;
    }
  } while (n->nd != nd || n->dir != dir);

  int diff = n->remain + (newshort ? newshort->best - newshort->remain : 0);
  if (n->best > cost + diff) {
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
}

void Route (int recalculate)
{ /* Recalculate is faster but only valid if 'to', 'car' and 'fastest' did not
     change */
/* We start by finding the segment that is closest to 'from' and 'to' */
  ndType *endNd[2];
  int toEndNd[2][2];
  
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
      if (!(((wayType*)(data + itr.nd[0]->wayPtr))->bits & (1<<car))) continue;
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
      if (d < bestd) {
        wayType *w = (wayType *)(data + itr.nd[0]->wayPtr);
        bestd = d;
        double invSpeed = 1;//!fastest ? 1.0 : Style (w)->invSpeed[car];
        //printf ("%d %lf\n", i, invSpeed);
        toEndNd[i][0] =
          lrint (sqrt ((double)(Sqr (lon0) + Sqr (lat0))) * invSpeed);
        toEndNd[i][1] =
          lrint (sqrt ((double)(Sqr (lon1) + Sqr (lat1))) * invSpeed);
        if (dlon * lon1 <= -dlat * lat1) toEndNd[i][1] += toEndNd[i][0] * 9;
        if (dlon * lon0 >= -dlat * lat0) toEndNd[i][0] += toEndNd[i][1] * 9;

        if (w->bits & (1 << onewayR)) toEndNd[i][1] = 200000000;
        /* It's possible to go up a oneway at the end, but at a huge penalty*/
        /* It's also possible to go up a 1 segment of a footway with a car
           without penalty. */
        endNd[i] = itr.nd[0];
        /* The router only stops after it has traversed endHs[1], so if we
           want 'limit' to be accurate, we must subtract it's length
        if (i) {
          toEndHs[1][0] -= segLen; 
          toEndHs[1][1] -= segLen;
        } */
      }
    } /* For each candidate segment */
    if (bestd == (__int64) 1 << 62) {
      fprintf (stderr, "No segment nearby\n");
      return;
    }
  } /* For 'from' and 'to', find segment that passes nearby */
  if (recalculate) {
    free (route);
    dhashSize = Sqr ((tlon - flon) >> 17) + Sqr ((tlat - flat) >> 17) + 20;
    dhashSize = dhashSize < 10000 ? dhashSize * 1000 : 10000000;
    // This memory management may not match computer capabilities
    route = (routeNodeType*) calloc (dhashSize, sizeof (*route));
  }

  routeHeapSize = 1; /* Leave position 0 open to simplify the math */
  routeHeap = ((routeNodeType**) malloc (dhashSize*sizeof (*routeHeap))) - 1;
  
  if (recalculate) {
    AddNd (endNd[0], 0, toEndNd[0][0], NULL);
    AddNd (ndBase + endNd[0]->other[0], 1, toEndNd[0][1], NULL);
    AddNd (endNd[0], 1, toEndNd[0][0], NULL);
    AddNd (ndBase + endNd[0]->other[0], 0, toEndNd[0][1], NULL);
  }
  else {
    for (int i = 0; i < dhashSize; i++) {
      if (route[i].nd) {
        route[i].best++; // Force re-add to the heap
        AddNd (route[i].nd, route[i].dir, route[i].best - 1,
          route[i].shortest);
      }
    }
  }
  
  while (routeHeapSize > 1) {
    routeNodeType *root = routeHeap[1];
    if (root->nd == ndBase - 1) { // ndBase - 1 is a marker for 'from'
      shortest = root->shortest;
      break;
    }
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
    if (root->nd == (!root->dir ? endNd[1] : ndBase + endNd[1]->other[0])) {
      AddNd (ndBase - 1, 0, toEndNd[1][1 - root->dir], root);
    }
    ndType *nd = root->nd, *other;
    while (nd > ndBase && nd[-1].lon == nd->lon &&
      nd[-1].lat == nd->lat) nd--; /* Find first nd in node */

    /* Now work through the segments connected to root. */
    do {
      for (int dir = 0; dir < 2; dir++) {
        if (nd == root->nd && dir == root->dir) continue;
        /* Don't consider an immediate U-turn to reach root->hs->other.
           This doesn't exclude 179.99 degree turns though. */
        
        if (nd->other[dir] < 0) continue; // Named node
        
        other = ndBase + nd->other[dir];
        wayType *w = (wayType *)(data + nd->wayPtr);
        if ((w->bits & (1<<car)) && (dir || !(w->bits & (1 << onewayR)))) {
          int d = lrint (sqrt ((double)
            (Sqr ((__int64)(nd->lon - other->lon)) +
             Sqr ((__int64)(nd->lat - other->lat)))) *
                               (fastest ? Style (w)->invSpeed[car] : 1.0));
          AddNd (other, 1 - dir, d, root);
        } // If we found a segment we may follow
      }
    } while (++nd < ndBase + hashTable[bucketsMin1 + 1] &&
             nd->lon == nd[-1].lon && nd->lat == nd[-1].lat);
  } // While there are active nodes left
  free (routeHeap + 1);
//  if (fastest) printf ("%lf
//  printf ("%lf km\n", limit / 100000.0);
}

#ifndef HEADLESS
#define ZOOM_PAD_SIZE 20
#define STATUS_BAR    0

GtkWidget *draw, *followGPSr;
GtkComboBox *iconSet, *carBtn, *fastestBtn, *detailBtn;
int clon, clat, zoom, gpsSockTag;
/* zoom is the amount that fits into the window (regardless of window size) */

struct gpsNewStruct {
  struct {
    double latitude, longitude, track, speed, hdop, ele;
    int mode;
    char date[6], tm[6];
  } fix;
} gpsNew;

int ProcessNmea (char *rx, unsigned *got)
{
  unsigned dataReady = FALSE, i;
  for (i = 0; i < *got && rx[i] != '$'; i++, (*got)--) {}
  while (rx[i] == '$') {
    //for (j = 0; j < got; i++, j++) rx[j] = rx[i];
    int fLen[19], fStart[19], fNr;
    memmove (rx, rx + i, *got);
    for (i = 1, fNr = 0; i < *got && rx[i] != '$' && fNr < 19; fNr++) {
      fStart[fNr] = i;
      while (i < *got && (rx[i] == '.' || isdigit (rx[i]))) i++;
      fLen[fNr] = i - fStart[fNr];
      while (i < *got && rx[i] != '$' && rx[i++] != ',') {}
    }
    while (i < *got && rx[i] != '$') i++;
    int col = memcmp (rx, "$GPGLL", 6) == 0 ? 1 :
      memcmp (rx, "$GPGGA", 6) == 0 ? 2 :
      memcmp (rx, "$GPRMC", 6) == 0 ? 3 : 0;
    if (fNr >= (col == 0 ? 2 : col == 1 ? 6 : col == 2 ? 13 : 10)) {
      if (col > 0 && fLen[col == 1 ? 5 : 1] >= 6 &&
          memcmp (gpsNew.fix.tm, rx + fStart[col == 1 ? 5 : 1], 6) != 0) {
        memcpy (gpsNew.fix.tm, rx + fStart[col == 1 ? 5 : 1], 6);
        dataReady = TRUE; // Notify only when parsing is complete
      }
      if (col == 2) gpsNew.fix.hdop = atof (rx + fStart[8]);
      if (col == 3 && fLen[7] > 0 && fLen[8] > 0 && fLen[9] >= 6) {
        memcpy (gpsNew.fix.date, rx + fStart[9], 6);
        gpsNew.fix.speed = atof (rx + fStart[7]);
        gpsNew.fix.track = atof (rx + fStart[8]);
      }
      if (col == 2 && fLen[9] > 0) gpsNew.fix.ele = atoi (rx + fStart[9]);
      if (col > 0 && fLen[col] > 6 && memchr ("SsNn", rx[fStart[col + 1]], 4)
        && fLen[col + 2] > 7 && memchr ("EeWw", rx[fStart[col + 3]], 4)) {
        double nLat = (rx[fStart[col]] - '0') * 10 + rx[fStart[col] + 1]
          - '0' + atof (rx + fStart[col] + 2) / 60;
        double nLon = ((rx[fStart[col + 2]] - '0') * 10 +
          rx[fStart[col + 2] + 1] - '0') * 10 + rx[fStart[col + 2] + 2]
          - '0' + atof (rx + fStart[col + 2] + 3) / 60;
        if (tolower (rx[fStart[col + 1]]) == 's') nLat = -nLat;
        if (tolower (rx[fStart[col + 3]]) == 'w') nLon = -nLon;
        if (fabs (nLat) < 90 && fabs (nLon) < 180) {
          gpsNew.fix.latitude = nLat;
          gpsNew.fix.longitude = nLon;
        }
      }
    }
    else if (i == *got) break; // Retry when we receive more data
    *got -= i;
  } /* If we know the sentence type */
  return dataReady;
}

#ifndef _WIN32_WCE
#ifdef ROUTE_TEST
gint RouteTest (GtkWidget *widget, GdkEventButton *event, void *)
{
  static int ptime = 0;
  if (TRUE) {
    ptime = time (NULL);
    int w = draw->allocation.width - ZOOM_PAD_SIZE;
    int perpixel = zoom / w;
    clon += lrint ((event->x - w / 2) * perpixel);
    clat -= lrint ((event->y - draw->allocation.height / 2) * perpixel);
    int plon = clon + lrint ((event->x - w / 2) * perpixel);
    int plat = clat -
      lrint ((event->y - draw->allocation.height / 2) * perpixel);
#else
// void GpsMove (gps_data_t *gps, char */*buf*/, size_t /*len*/, int /*level*/)
void ReceiveNmea (gpointer /*data*/, gint source, GdkInputCondition /*c*/)
{
  static char rx[1200];
  static unsigned got = 0;
  int cnt = read (source, rx + got, sizeof (rx) - got);
  if (cnt == 0) {
    gdk_input_remove (gpsSockTag);
    return;
  }
  got += cnt;
  gpsNewStruct *gps = &gpsNew;
  
  if (ProcessNmea (rx, &got) && //gps->fix.mode >= MODE_2D &&
      gtk_toggle_button_get_active (GTK_TOGGLE_BUTTON (followGPSr))) {
    clon = Longitude (gps->fix.longitude);
    clat = Latitude (gps->fix.latitude);
    int plon = Longitude (gps->fix.longitude + gps->fix.speed * 3600.0 /
      40000000.0 / cos (gps->fix.latitude * (M_PI / 180.0)) *
      sin (gps->fix.track * (M_PI / 180.0)));
    int plat = Latitude (gps->fix.latitude + gps->fix.speed * 3600.0 /
      40000000.0 * cos (gps->fix.track * (M_PI / 180.0)));
    // Predict the vector that will be traveled in the next 10seconds
//    printf ("%5.1lf m/s Heading %3.0lf\n", gps->fix.speed, gps->fix.track);
//    printf ("%lf %lf\n", gps->fix.latitude, gps->fix.longitude);
#endif
    
    flon = clon;
    flat = clat;
    #if 0
    Route (FALSE);
    if (shortest) {
      __int64 dlon = plon - clon, dlat = plat - clat;
      if (!shortest->shortest && dlon * (tlon - clon) > dlat * (clat - tlat)
                             && dlon * (tlon - plon) < dlat * (plat - tlat)) {
        // Only stop once both C and P are acute angles in CPT, according to
        // Pythagoras.
        fprintf (flitePipe, "%ld Stop\n", (long)time (NULL));
      }
      char *oldName = NULL;
      for (routeNodeType *ahead = shortest; ahead;
           ahead = ahead->shortest) {
        __int64 alon = ((halfSegType *)(ahead->hs->other + data))->lon -
          ahead->hs->lon;
        __int64 alat = ((halfSegType *)(ahead->hs->other + data))->lat -
          ahead->hs->lat;
        __int64 divisor = dlon * alat - dlat * alon;
        __int64 dividend = dlon * alon + dlat * alat;
        __int64 slon = ahead->hs->lon - clon;
        __int64 slat = ahead->hs->lat - clat;
        if (ahead == shortest && ahead->shortest && dividend < 0 &&
            dividend < divisor && divisor < -dividend &&
            Sqr (slon + alon) + Sqr (slat + alat) > 64000000) {
          fprintf (flitePipe, "%ld U turn\n", (long)time (NULL));
          break; // Only when first node is far behind us.
        }
        __int64 dintercept = divisor == 0 ? 9223372036854775807LL :
            dividend * (dlon * slat - dlat * slon) /
            divisor + dlon * slon + dlat * slat;
        char *name = data + ((wayType *)(data +
          (ahead->hs->wayPtr == TO_HALFSEG ? (halfSegType*)
                  (ahead->hs->other + data) : ahead->hs)->wayPtr))->name;
        if (dividend < 0 || divisor > dividend || divisor < -dividend) {
          // If segment goes "back" or makes a 45 degree angle with the
          // motion vector.
          //flite_text_to_speech ("U turn", fliteV, "play");
          if (dintercept < dlon * dlon + dlat * dlat) {
            // Found a turn that should be made in the next 10 seconds.
            fprintf (flitePipe, "%ld %s in %s\n", (long)time (NULL),
              divisor > 0 ? "Left" : "Right", name);
          }
          break;
        }
        if (name[0] != '\0') {
          if (oldName && stricmp (oldName, name)) {
            if (dintercept < dlon * dlon + dlat * dlat) {
              fprintf (flitePipe, "%ld %s\n", (long)time (NULL), name);
            }
            break;
          }
          oldName = name;
        }
      } // While looking for a turn ahead.
    } // If the routing was successful
    #endif
    gtk_widget_queue_clear (draw);
  } // If following the GPSr and it has a fix.
}

int Click (GtkWidget * /*widget*/, GdkEventButton *event, void * /*para*/)
{
  int w = draw->allocation.width - ZOOM_PAD_SIZE;
  #ifdef ROUTE_TEST
  if (event->state) {
    return RouteTest (widget, event, para);
  }
  #endif
  if (event->x > w) {
    zoom = lrint (exp (12 - 12*event->y / draw->allocation.height) * 5000);
  }
  else {
    int perpixel = zoom / w;
    if (event->button == 1) {
      clon += lrint ((event->x - w / 2) * perpixel);
      clat -= lrint ((event->y - draw->allocation.height / 2) * perpixel);
      gtk_toggle_button_set_active (GTK_TOGGLE_BUTTON (followGPSr), FALSE);
    }
    else if (event->button == 2) {
      flon = clon + lrint ((event->x - w / 2) * perpixel);
      flat = clat - lrint ((event->y - draw->allocation.height/2) * perpixel);
    }
    else {
      tlon = clon + lrint ((event->x - w / 2) * perpixel);
      tlat = clat -
        lrint ((event->y - draw->allocation.height / 2) * perpixel);
      car = !gtk_combo_box_get_active (carBtn) ? motorcarR : bicycleR;
      fastest = !gtk_combo_box_get_active (fastestBtn);
      Route (TRUE);
    }
  }
  gtk_widget_queue_clear (draw);
  return FALSE;
}
#endif // _WIN32_WCE

#if 0
void GetDirections (GtkWidget *, gpointer)
{
  char *msg;
  if (!shortest) msg = strdup (
    "Mark the starting point with the middle button and the\n"
    "end point with the right button. Then click Get Directions again\n");
  else {
    for (int i = 0; i < 2; i++) {
      int len = 0;
      char *last = "";
      __int64 dlon = 0, dlat = 1, bSqr = 1; /* Point North */
      for (routeNodeType *x = shortest; x; x = x->shortest) {
        halfSegType *other = (halfSegType *)(data + x->hs->other);
        int forward = x->hs->wayPtr != TO_HALFSEG;
        wayType *w = (wayType *)(data + (forward ? x->hs : other)->wayPtr);
        
        // I think the formula below can be substantially simplified using
        // the method used in GpsMove
        __int64 nlon = other->lon - x->hs->lon, nlat = other->lat-x->hs->lat;
        __int64 cSqr = Sqr (nlon) + Sqr (nlat);
        __int64 lhs = bSqr + cSqr - Sqr (nlon - dlon) - Sqr (nlat - dlat);
        /* Use cosine rule to determine if the angle is obtuse or greater than
           45 degrees */
        if (lhs < 0 || Sqr (lhs) < 2 * bSqr * cSqr) {
          /* (-nlat,nlon) is perpendicular to (nlon,nlat). Then we use
             Pythagoras test for obtuse angle for left and right */
          if (!i) len += 11;
          else len += sprintf (msg + len, "%s turn\n",
            nlon * dlat < nlat * dlon ? "Left" : "Right");
        }
        dlon = nlon;
        dlat = nlat;
        bSqr = cSqr;
        
        if (strcmp (w->name + data, last)) {
          last = w->name + data;
          if (!i) len += strlen (last) + 1;
          else len += sprintf (msg + len, "%s\n", last);
        }
      }
      if (!i) msg = (char*) malloc (len + 1);
    } // First calculate len, then create message.
  }
  GtkWidget *window = gtk_window_new (GTK_WINDOW_TOPLEVEL);
  GtkWidget *view = gtk_text_view_new ();
  GtkWidget *scrol = gtk_scrolled_window_new (NULL, NULL);
//  gtk_scrolled_winGTK_POLICY_AUTOMATIC,
//    GTK_POLICY_ALWAYS);
  GtkTextBuffer *buffer = gtk_text_view_get_buffer (GTK_TEXT_VIEW (view));
  gtk_text_view_set_editable (GTK_TEXT_VIEW (view), FALSE);
  gtk_text_buffer_set_text (buffer, msg, -1);
  free (msg);
  gtk_scrolled_window_add_with_viewport (GTK_SCROLLED_WINDOW (scrol), view);
  gtk_container_add (GTK_CONTAINER (window), scrol);
  gtk_widget_set_size_request (window, 300, 300);
  gtk_widget_show (view);
  gtk_widget_show (scrol);
  gtk_widget_show (window);
}

#endif

struct name2renderType { // Build a list of names, sort by name,
  wayType *w;            // make unique by name, sort by y, then render
  int x, y, width;       // only if their y's does not overlap
};

#ifdef _WIN32_WCE
int Expose (HDC mygc, HDC icons, HPEN *pen)
{
  struct {
    int width, height;
  } clip;
  clip.width = GetSystemMetrics(SM_CXSCREEN);
  clip.height = GetSystemMetrics(SM_CYSCREEN);
  HFONT sysFont = (HFONT) GetStockObject (SYSTEM_FONT);
  LOGFONT logFont;
  GetObject (sysFont, sizeof (logFont), &logFont);
  WCHAR wcTmp[70];
  int detail = 2;

#define gtk_combo_box_get_active(x) 1
#define gdk_draw_drawable(win,dgc,sdc,x,y,dx,dy,w,h) \
  BitBlt (dgc, dx, dy, w, h, sdc, x, y, SRCCOPY)
#define gdk_draw_line(win,gc,sx,sy,dx,dy) \
  MoveToEx (gc, sx, sy, NULL); LineTo (gc, dx, dy)
#else
gint Scroll (GtkWidget * /*widget*/, GdkEventScroll *event, void * /*w_cur*/)
{
  if (event->direction == GDK_SCROLL_UP) zoom = zoom / 4 * 3;
  if (event->direction == GDK_SCROLL_DOWN) zoom = zoom / 3 * 4;
  gtk_widget_queue_clear (draw);
  return FALSE;
}

gint Expose (void)
{
  static GdkColor styleColour[2 << STYLE_BITS][2], routeColour;
  static GdkPixmap *icons = NULL;
  static GdkGC *mygc = NULL;
  if (!mygc) {
    mygc = gdk_gc_new (draw->window);
    for (int i = 0; i < 1 || style[i - 1].scaleMax; i++) {
      for (int j = 0; j < 2; j++) {
        int c = !j ? style[i].areaColour 
          : style[i].lineColour ? style[i].lineColour
          : (style[i].areaColour >> 1) & 0xefefef; // Dark border
        styleColour[i][j].red =    (c >> 16)        * 0x101;
        styleColour[i][j].green = ((c >> 8) & 0xff) * 0x101;
        styleColour[i][j].blue =   (c       & 0xff) * 0x101;
        gdk_colormap_alloc_color (gdk_window_get_colormap (draw->window),
          &styleColour[i][j], FALSE, TRUE);
      }
    }
    routeColour.red = 0xffff;
    routeColour.green = routeColour.blue = 0;
    gdk_colormap_alloc_color (gdk_window_get_colormap (draw->window),
      &routeColour, FALSE, TRUE);
    gdk_gc_set_fill (mygc, GDK_SOLID);
    icons = gdk_pixmap_create_from_xpm (draw->window, NULL, NULL,
      "icons.xpm");
  }  

  GdkRectangle clip;
  clip.x = 0;
  clip.y = 0;
  clip.height = draw->allocation.height - STATUS_BAR;
  clip.width = draw->allocation.width;
  gdk_gc_set_clip_rectangle (mygc, &clip);
  gdk_gc_set_foreground (mygc, &styleColour[0][0]);
  gdk_gc_set_line_attributes (mygc,
    1, GDK_LINE_SOLID, GDK_CAP_PROJECTING, GDK_JOIN_MITER);
  gdk_draw_line (draw->window, mygc,
    clip.width - ZOOM_PAD_SIZE / 2, 0,
    clip.width - ZOOM_PAD_SIZE / 2, clip.height); // Visual queue for zoom bar
  gdk_draw_line (draw->window, mygc,
    clip.width - ZOOM_PAD_SIZE, clip.height - ZOOM_PAD_SIZE / 2,
    clip.width - ZOOM_PAD_SIZE / 2, clip.height); // Visual queue for zoom bar
  gdk_draw_line (draw->window, mygc,
    clip.width, clip.height - ZOOM_PAD_SIZE / 2,
    clip.width - ZOOM_PAD_SIZE / 2, clip.height); // Visual queue for zoom bar
  gdk_draw_line (draw->window, mygc,
    clip.width - ZOOM_PAD_SIZE, ZOOM_PAD_SIZE / 2,
    clip.width - ZOOM_PAD_SIZE / 2, 0); // Visual queue for zoom bar
  gdk_draw_line (draw->window, mygc,
    clip.width, ZOOM_PAD_SIZE / 2,
    clip.width - ZOOM_PAD_SIZE / 2, 0); // Visual queue for zoom bar
    
  clip.width = draw->allocation.width - ZOOM_PAD_SIZE;
  gdk_gc_set_clip_rectangle (mygc, &clip);
  
  GdkFont *f = gtk_style_get_font (draw->style);
  int detail = 4 - gtk_combo_box_get_active (detailBtn);
#endif // !_WIN32_WCE
  #ifdef CAIRO_VERSION
  cairo_t *cai = gdk_cairo_create (draw->window);
  if (detail < 4) {
    cairo_font_options_t *caiFontOptions = cairo_font_options_create ();
    cairo_get_font_options (cai, caiFontOptions);
    cairo_font_options_set_antialias (caiFontOptions, CAIRO_ANTIALIAS_NONE);
    cairo_set_font_options (cai, caiFontOptions);
  }
  cairo_matrix_t mat;
  cairo_matrix_init_identity (&mat);
  #endif
  if (zoom < 0) zoom = 2012345678;
  if (zoom / clip.width == 0) zoom += 4000;
  int perpixel = zoom / clip.width, iset = gtk_combo_box_get_active (iconSet);
  int doAreas = TRUE;
//    zoom / sqrt (draw->allocation.width * draw->allocation.height);
  for (int thisLayer = -5, nextLayer; thisLayer < 6;
       thisLayer = nextLayer, doAreas = !doAreas) {
    OsmItr itr (clon - perpixel * clip.width / 2,
      clat - perpixel * clip.height / 2,
      clon + perpixel * clip.width / 2, clat + perpixel * clip.height / 2);
    // Widen this a bit so that we render nodes that are just a bit offscreen ?
    nextLayer = 6;
    
    while (Next (itr)) {
      wayType *w = (wayType *)(data + itr.nd[0]->wayPtr);
      if (Style (w)->scaleMax < perpixel * 175 / (detail + 4)) continue;
      
      if (detail < 4 && Style (w)->areaColour) {
        if (thisLayer > -5) continue;  // Draw all areas with layer -5
      }
      else if (zoom < 100000*100) {
      // Under low-zoom we draw everything on layer -5 (faster)
        if (thisLayer < Layer (w) && Layer (w) < nextLayer) {
          nextLayer = Layer (w);
        }
        if (detail == 4) {
          if (doAreas) nextLayer = thisLayer;
          if (Style (w)->areaColour ? !doAreas : doAreas) continue;
        }
        if (Layer (w) != thisLayer) continue;
      }
      ndType *nd = itr.nd[0];
      if (itr.nd[0]->other[0] >= 0) {
        nd = ndBase + itr.nd[0]->other[0];
        if (nd->lat == INT_MIN) nd = itr.nd[0]; // Node excluded from build
        else if (itr.left <= nd->lon && nd->lon < itr.right &&
            itr.top  <= nd->lat && nd->lat < itr.bottom) continue;
      } // Only process this way when the Itr gives us the first node, or
      // the first node that's inside the viewing area

      #ifndef _WIN32_WCE
      __int64 maxLenSqr = 0;
      double x0, y0;
      #else
      int best = 0, bestW, bestH, x0, y0;
      #endif
      int len = strcspn ((char *)(w + 1) + 1, "\n");
      
      if (nd->other[0] < 0 && nd->other[1] < 0) {
        int x = clip.width / 2 + (nd->lon - clon) / perpixel;
        int y = clip.height / 2 - (nd->lat - clat) / perpixel;
        int *icon = Style (w)->x + 4 * iset;
        if (icons && icon[2] != 0) {
          gdk_draw_drawable (draw->window, mygc, icons,
            icon[0], icon[1], x - icon[2] / 2, y - icon[3] / 2,
            icon[2], icon[3]);
        }
        
	#ifdef _WIN32_WCE
        SelectObject (mygc, sysFont);
        MultiByteToWideChar (CP_UTF8, 0, (char *)(w + 1) + 1,
          len, wcTmp, sizeof (wcTmp));
        ExtTextOut (mygc, x - len * 3, y + icon[3] / 2, 0, NULL,
  	      wcTmp, len, NULL);	
	#endif
        #ifdef CAIRO_VERSION
        //if (Style (w)->scaleMax > zoom / 2 || zoom < 2000) {
          mat.xx = mat.yy = 12.0;
          mat.xy = mat.yx = 0;
          x0 = x - mat.xx / 12.0 * 3 * len; /* Render the name of the node */
          y0 = y + mat.xx * f->ascent / 12.0 + icon[3] / 2;
          maxLenSqr = 4000000000000LL; // Without scaleMax, use 400000000
        //}
        #endif
      }
      else if (Style (w)->areaColour) {
        #ifndef _WIN32_WCE
        while (nd->other[0] >= 0) nd = ndBase + nd->other[0];
        static GdkPoint pt[1000];
        unsigned pts;
        for (pts = 0; pts < sizeof (pt) / sizeof (pt[0]) && nd->other[1] >= 0;
             nd = ndBase + nd->other[1]) {
          if (nd->lat != INT_MIN) {
            pt[pts].x = (nd->lon - clon) / perpixel + clip.width / 2;
            pt[pts++].y = clip.height / 2 - (nd->lat - clat) / perpixel;
          }
        }
        gdk_gc_set_foreground (mygc, &styleColour[Style (w) - style][0]);
        gdk_draw_polygon (draw->window, mygc, TRUE, pt, pts);
        gdk_gc_set_foreground (mygc, &styleColour[Style (w) - style][1]);
        gdk_gc_set_line_attributes (mygc, Style (w)->lineWidth,
          Style (w)->dashed ? GDK_LINE_ON_OFF_DASH
          : GDK_LINE_SOLID, GDK_CAP_PROJECTING, GDK_JOIN_MITER);
        gdk_draw_polygon (draw->window, mygc, FALSE, pt, pts);
	#endif
      }
      else if (nd->other[1] >= 0) {
        #ifndef _WIN32_WCE
        gdk_gc_set_foreground (mygc, &styleColour[Style (w) - style][1]);
        gdk_gc_set_line_attributes (mygc, Style (w)->lineWidth,
          Style (w)->dashed ? GDK_LINE_ON_OFF_DASH
          : GDK_LINE_SOLID, GDK_CAP_PROJECTING, GDK_JOIN_MITER);
        #else
	SelectObject (mygc, pen[StyleNr (w)]);
        #endif
        do {
          ndType *next = ndBase + nd->other[1];
          if (next->lat == INT_MIN) break; // Node excluded from build
          gdk_draw_line (draw->window, mygc,
            (nd->lon - clon) / perpixel + clip.width / 2,
            clip.height / 2 - (nd->lat - clat) / perpixel,
            (next->lon - clon) / perpixel + clip.width / 2,
            clip.height / 2 - (next->lat - clat) / perpixel);
	  #ifdef _WIN32_WCE
	  int newb = nd->lon > next->lon
	    ? nd->lon - next->lon : next->lon - nd->lon;
	  if (newb < nd->lat - next->lat) newb = nd->lat - next->lat;
	  if (newb < next->lat - nd->lat) newb = next->lat - nd->lat;
	  if (best < newb) {
	    best = newb;
	    bestW = (next->lon > nd->lon ? -1 : 1) * (next->lon - nd->lon);
	    bestH = (next->lon > nd->lon ? -1 : 1) * (next->lat - nd->lat);
            x0 = next->lon / 2 + nd->lon / 2;
	    y0 = next->lat / 2 + nd->lat / 2;
	  }
	  #endif
          #ifdef CAIRO_VERSION
          __int64 lenSqr = (nd->lon - next->lon) * (__int64)(nd->lon - next->lon) +
                             (nd->lat - next->lat) * (__int64)(nd->lat - next->lat);
          if (lenSqr > maxLenSqr) {
            maxLenSqr = lenSqr;
            mat.yy = mat.xx = 12 * fabs (nd->lon - next->lon) / sqrt (lenSqr);
            mat.xy = (nd->lon > next->lon ? 12.0 : -12.0) *
                                        (nd->lat - next->lat) / sqrt (lenSqr);
            mat.yx = -mat.xy;
            x0 = clip.width / 2 + (nd->lon / 2 + next->lon / 2 - clon) /
              perpixel + mat.yx * f->descent / 12.0 - mat.xx / 12.0 * 3 * len;
            y0 = clip.height / 2 - (nd->lat / 2 + next->lat / 2 - clat) /
              perpixel - mat.xx * f->descent / 12.0 - mat.yx / 12.0 * 3 * len;
          }
	  #endif
          nd = next;
        } while (itr.left <= nd->lon && nd->lon < itr.right &&
                 itr.top  <= nd->lat && nd->lat < itr.bottom &&
                 nd->other[1] >= 0);
      } /* If it has one or more segments */

      #ifdef _WIN32_WCE
      if (best > perpixel * len * 4) {
        double hoek = atan2 (bestH, bestW);
        logFont.lfEscapement = logFont.lfOrientation =
          1800 + int ((1800 / M_PI) * hoek);
        
        HFONT customFont = CreateFontIndirect (&logFont);
        HGDIOBJ oldf = SelectObject (mygc, customFont);
        MultiByteToWideChar (CP_UTF8, 0, (char *)(w + 1) + 1,
          len, wcTmp, sizeof (wcTmp));
        ExtTextOut (mygc, (x0 - clon) / perpixel + clip.width / 2 +
	      int (len * 3 * cos (hoek)),
              clip.height / 2 - (y0 - clat) / perpixel -
	      int (len * 3 * sin (hoek)), 0, NULL,
  	      wcTmp, len, NULL);
        SelectObject (mygc, customFont);
        DeleteObject (customFont);
      }
      #endif
      #ifdef CAIRO_VERSION
      if (maxLenSqr * detail > perpixel * (__int64) perpixel *
          len * len * 100 && len > 0) {
        for (char *txt = (char *)(w + 1) + 1; *txt != '\0';) {
          cairo_set_font_matrix (cai, &mat);
          char *line = (char *) malloc (strcspn (txt, "\n") + 1);
          memcpy (line, txt, strcspn (txt, "\n"));
          line[strcspn (txt, "\n")] = '\0';
          cairo_move_to (cai, x0, y0);
          cairo_show_text (cai, line);
          free (line);
          if (perpixel > 10) break;
          y0 += mat.xx * (f->ascent + f->descent) / 12;
          x0 += mat.xy * (f->ascent + f->descent) / 12;
          while (*txt != '\0' && *txt++ != '\n') {}
        }
      }
      #endif
    } /* for each visible tile */
  }
  #ifdef CAIRO_VERSION
  cairo_destroy (cai);
  #endif
#if 0
//  printf ("%d %d %s\n", name[0].x, name[0].y, name[0].w->name + data);
  for (int i = 0, y = -1000; i < nameCnt; i++) {
    if (y + f->ascent + f->descent < name[i].y) {
      y = name[i].y;
      gdk_gc_set_foreground (draw->style->fg_gc[0],
        &highwayColour[name[i].w->type]);
      gdk_draw_string (draw->window, f, draw->style->fg_gc[0],
        name[i].x, name[i].y, name[i].w->name + data);
    }
  }
#endif
//  gdk_gc_set_foreground (draw->style->fg_gc[0], &highwayColour[rail]);
//  gdk_gc_set_line_attributes (draw->style->fg_gc[0],
//    1, GDK_LINE_SOLID, GDK_CAP_PROJECTING, GDK_JOIN_MITER);
#ifndef _WIN32_WCE
  routeNodeType *x;
  if (shortest && (x = shortest->shortest)) {
    gdk_gc_set_foreground (mygc, &routeColour);
    gdk_gc_set_line_attributes (mygc, 5,
      GDK_LINE_SOLID, GDK_CAP_PROJECTING, GDK_JOIN_MITER);
    if (routeHeapSize > 1) {
      gdk_draw_line (draw->window, mygc,
        (flon - clon) / perpixel + clip.width / 2,
        clip.height / 2 - (flat - clat) / perpixel,
        (x->nd->lon - clon) / perpixel + clip.width / 2,
        clip.height / 2 - (x->nd->lat - clat) / perpixel);
    }
    for (; x->shortest; x = x->shortest) {
      gdk_draw_line (draw->window, mygc,
        (x->nd->lon - clon) / perpixel + clip.width / 2,
        clip.height / 2 - (x->nd->lat - clat) / perpixel,
        (x->shortest->nd->lon - clon) / perpixel + clip.width / 2,
        clip.height / 2 - (x->shortest->nd->lat - clat) / perpixel);
    }
    gdk_draw_line (draw->window, mygc,
      (x->nd->lon - clon) / perpixel + clip.width / 2,
      clip.height / 2 - (x->nd->lat - clat) / perpixel,
      (tlon - clon) / perpixel + clip.width / 2,
      clip.height / 2 - (tlat - clat) / perpixel);
  }
#endif
/*
  clip.height = draw->allocation.height;
  gdk_gc_set_clip_rectangle (draw->style->fg_gc[0], &clip);
  gdk_draw_string (draw->window, f, draw->style->fg_gc[0],
    clip.width/2, clip.height - f->descent, "gosmore");
  */
  return FALSE;
}

GtkWidget *search;
GtkWidget *list;
wayType *incrementalWay[40];

#ifndef _WIN32_WCE
/* Current algorithm performs badly in many cases.
   Solution :
   1. Bsearch idx such that
      ZEnc (way[idx]) < ZEnc (clon/lat) < ZEnc (way[idx+1])
   2. Fill the list with ways around idx.
   3. Now there's a circle with clon/clat as its centre and that runs through
      the worst way just found. Let's say it's diameter is d. There exist
      4 Z squares smaller that 2d by 2d that cover this circle. Find them
      with binary search and search through them for the nearest ways.
*/
void PopulateList (int *base, int num, int *count, int firstInR)
{ // May not change incrementalWay[0..firstInR-1]
  static __int64 dista[sizeof (incrementalWay)/sizeof (incrementalWay[0])];
  if (num <= 0) return;
  while (TagCmp (data + base[0], data + base[num / 2]) != 0) {
    num = num / 2; // Range contains more than one name. Do only 1st half.
  }
  char *name = data + base[num / 2], *n = name;
  while (*--n) {}
  wayType *w = (wayType *)n - 1;
  int lt = ZEnc ((unsigned) clon >> 16, (unsigned) clat >> 16) <
    ZEnc ((unsigned) w->clon >> 16, (unsigned) w->clat >> 16);
  if (lt) PopulateList (base, num / 2, count, firstInR);
  else PopulateList (base + num / 2 + 1, num - 1 - num / 2, count, firstInR);
  
  int i;
  __int64 dist = Sqr ((__int64)(clon - w->clon)) +
    Sqr ((__int64)(clat - w->clat));
  for (i = *count - 1; firstInR <= i && dist < dista[i]; i--) {
    if (i + 1 < int (sizeof (incrementalWay) / sizeof (incrementalWay[0]))) {
      incrementalWay[i + 1] = incrementalWay[i];
      dista[i + 1] = dista[i];
    }
  }
  if (i + 1 < int (sizeof (incrementalWay) / sizeof (incrementalWay[0]))) {
    incrementalWay[i + 1] = w;
    dista[i + 1] = dist;
  }
  if (*count < int (sizeof (incrementalWay) / sizeof (incrementalWay[0]))) {
    char *m = (char *) malloc (strcspn (name, "\n") + 1);
    sprintf (m, "%.*s", strcspn (name, "\n"), name); // asprintf ?
    gtk_clist_append (GTK_CLIST (list), &m);
    free (m);
    (*count)++;
  }
  int mask = !lt ? w->clon | w->clat : (~w->clon) | (~w->clat);
  mask |= mask >> 1;
  mask |= mask >> 2;
  mask |= mask >> 4;
  mask |= mask >> 8;
  mask |= mask >> 16;
  if (*count < int (sizeof (incrementalWay) / sizeof (incrementalWay[0])) ||
      (!lt ? mask < clon && mask < clat && Sqr ((__int64)(clon - mask)) + 
             Sqr ((__int64)(clat - mask)) < dista[*count - 1]
           : clon < ~mask && clat < ~mask && Sqr ((__int64)(~mask - clon)) +
             Sqr ((__int64)(~mask - clat)) < dista[*count - 1])) {
    if (!lt) PopulateList (base, num / 2, count, firstInR);
    else PopulateList (base + num/2 + 1, num - 1 - num/2, count, firstInR);
  }
}

gint IncrementalSearch (void)
{
  char *key = (char *) gtk_entry_get_text (GTK_ENTRY (search));
  int *idx =
    (int *)(ndBase + hashTable[bucketsMin1 + (bucketsMin1 >> 7) + 2]);
  int l = 0, h = hashTable - idx, count;
  while (l < h) {
    if (TagCmp (data + idx[(h + l) / 2], key) >= 0) h = (h + l) / 2;
    else l = (h + l) / 2 + 1;
  }
  gtk_clist_freeze (GTK_CLIST (list));
  gtk_clist_clear (GTK_CLIST (list));
//  char *lastName = data + idx[min (hashTable - idx), 
//    int (sizeof (incrementalWay) / sizeof (incrementalWay[0]))) + l - 1];
  for (count = 0; count + l < hashTable - idx && count <
      int (sizeof (incrementalWay) / sizeof (incrementalWay[0]));) {
    PopulateList (idx + l + count, hashTable - idx - l >=
      int (sizeof (incrementalWay) / sizeof (incrementalWay[0])) &&
      TagCmp (data + idx[l + count], data +
    idx[l + sizeof (incrementalWay) / sizeof (incrementalWay[0]) - 1]) != 0
      ? int (sizeof (incrementalWay) / sizeof (incrementalWay[0])) - count
      : hashTable - idx - l - count, &count, count);
  }
  gtk_clist_thaw (GTK_CLIST (list));
  return FALSE;
}

void SelectName (GtkWidget * /*w*/, gint row, gint /*column*/,
  GdkEventButton * /*ev*/, gpointer /*data*/)
{
  clon = incrementalWay[row]->clon;
  clat = incrementalWay[row]->clat;
  zoom = incrementalWay[row]->dlat + incrementalWay[row]->dlon + (2 << 14);
  gtk_toggle_button_set_active (GTK_TOGGLE_BUTTON (followGPSr), FALSE);
  gtk_widget_queue_clear (draw);
}
#endif // !_WIN32_WCE
#endif // HEADLESS

#ifndef _WIN32_WCE
int UserInterface (int argc, char *argv[])
{
  #ifdef USE_FLITE
    int pyp[2];
    pipe (pyp);
    if (fork () == 0) { // A simple child to play all the voices it has
      FILE *rpipe = fdopen (pyp[0], "r");
      flite_init ();    // time for without blocking the main process
      cst_voice *fliteV = register_cmu_us_kal ();
      for (;;) {
        char msg[301];
        time_t preread = time (NULL), other;
        fscanf (rpipe, "%ld %300[^\n]", &other, msg);
        if (preread <= other) flite_text_to_speech (msg, fliteV, "play");
      }
    }
    flitePipe = fdopen (pyp[1], "w");
    setlinebuf (flitePipe);
  #endif
  #if defined (__linux__)
  FILE *gmap = fopen64 ("gosmore.pak", "r");
  #else
  GMappedFile *gmap = g_mapped_file_new ("gosmore.pak", FALSE, NULL);
  #endif
  if (!gmap) {
    fprintf (stderr, "Cannot read gosmore.pak\nYou can (re)build it from\n"
      "the planet file e.g. bzip2 -d planet-...osm.bz2 | %s rebuild\n",
      argv[0]);
    return 4;
  }
  #ifdef __linux__
  int ndCount[3];
  fseek (gmap, -sizeof (ndCount), SEEK_END);
  fread (ndCount, sizeof (ndCount), 1, gmap);
  long pakSize = ftello64 (gmap);
  data = (char *) mmap (NULL, ndCount[2],
                 PROT_READ, MAP_SHARED, fileno (gmap), 0);

  ndBase = (ndType *) ((char *)mmap (NULL, pakSize - (ndCount[2] & ~0xfff), //ndCount[0] * sizeof (*ndBase),
       PROT_READ, MAP_SHARED, fileno (gmap), ndCount[2] & ~0xfff) +
     (ndCount[2] & 0xfff));
  bucketsMin1 = ndCount[1];
  hashTable = (int *)((char *)ndBase + pakSize - ndCount[2]) - bucketsMin1
    - (bucketsMin1 >> 7) - 5;
  #else
  data = (char*) g_mapped_file_get_contents (gmap);
  bucketsMin1 = ((int *) (data + g_mapped_file_get_length (gmap)))[-2];
  hashTable = (int *) (data + g_mapped_file_get_length (gmap)) -
    bucketsMin1 - (bucketsMin1 >> 7) - 5;
  ndBase = (ndType *)(data + hashTable[bucketsMin1 + (bucketsMin1 >> 7) + 4]);
  #endif
  if (!data || !ndBase || !hashTable) {
    fprintf (stderr, "mmap failed\n");
    return 8;
  }
  style = (struct styleStruct *)(data + 4);

  if (getenv ("QUERY_STRING")) {
    double x0, y0, x1, y1;
    char vehicle[20];
    sscanf (getenv ("QUERY_STRING"),
      "flat=%lf&flon=%lf&tlat=%lf&tlon=%lf&fast=%d&v=%19[a-z]",
      &y0, &x0, &y1, &x1, &fastest, vehicle);
    flat = Latitude (y0);
    flon = Longitude (x0);
    tlat = Latitude (y1);
    tlon = Longitude (x1);
    #define M(v) if (strcmp (vehicle, #v) == 0) car = v ## R;
    RESTRICTIONS
    #undef M
    Route (TRUE);
    printf ("Content-Type: text/plain\n\r\n\r");
    if (!shortest) printf ("No route found\n\r");
    else if (routeHeapSize <= 1) printf ("Jump\n\r");
    for (; shortest; shortest = shortest->shortest) {
      printf ("%lf,%lf\n\r", (atan (exp (shortest->nd->lat / 2147483648.0 *
        M_PI)) - M_PI / 4) / M_PI * 360,
        shortest->nd->lon / 2147483648.0 * 180);
    }
    return 0;
  }

  printf ("%s is in the public domain and comes without warrantee\n",argv[0]);
  #ifndef HEADLESS
  clon = ndBase[0].lon; // Longitude (-0.272228);
  clat = ndBase[0].lat; // Latitude (51.927977);
  zoom = lrint (0.1 / 5 / 180 * 2147483648.0 * cos (26.1 / 180 * M_PI));
  
  gtk_init (&argc, &argv);
  draw = gtk_drawing_area_new ();
  gtk_signal_connect (GTK_OBJECT (draw), "expose_event",
    (GtkSignalFunc) Expose, NULL);
  gtk_signal_connect (GTK_OBJECT (draw), "button_press_event",
    (GtkSignalFunc) Click, NULL);
  gtk_widget_set_events (draw, GDK_EXPOSURE_MASK | GDK_BUTTON_PRESS_MASK |
    GDK_POINTER_MOTION_MASK);
  gtk_signal_connect (GTK_OBJECT (draw), "scroll_event",
                       (GtkSignalFunc) Scroll, NULL);
  
  GtkWidget *window = gtk_window_new (GTK_WINDOW_TOPLEVEL);
  GtkWidget *hbox = gtk_hbox_new (FALSE, 5), *vbox = gtk_vbox_new (FALSE, 0);
  gtk_container_add (GTK_CONTAINER (window), hbox);
  gtk_box_pack_start (GTK_BOX (hbox), draw, TRUE, TRUE, 0);
  gtk_box_pack_end (GTK_BOX (hbox), vbox, FALSE, FALSE, 0);

  search = gtk_entry_new ();
  gtk_box_pack_start (GTK_BOX (vbox), search, FALSE, FALSE, 5);
  gtk_entry_set_text (GTK_ENTRY (search), "Search");
  gtk_signal_connect (GTK_OBJECT (search), "changed",
    GTK_SIGNAL_FUNC (IncrementalSearch), NULL);
  
  list = gtk_clist_new (1);
  gtk_clist_set_selection_mode (GTK_CLIST (list), GTK_SELECTION_SINGLE);
  gtk_box_pack_start (GTK_BOX (vbox), list, TRUE, TRUE, 5);
  gtk_signal_connect (GTK_OBJECT (list), "select_row",
    GTK_SIGNAL_FUNC (SelectName), NULL);
    
  carBtn = GTK_COMBO_BOX (gtk_combo_box_new_text ());
  gtk_combo_box_append_text (carBtn, "car");
  gtk_combo_box_append_text (carBtn, "bicycle");
  gtk_combo_box_set_active (carBtn, 0);
  gtk_box_pack_start (GTK_BOX (vbox), GTK_WIDGET (carBtn), FALSE, FALSE, 5);

  fastestBtn = GTK_COMBO_BOX (gtk_combo_box_new_text ());
  gtk_combo_box_append_text (fastestBtn, "fastest");
  gtk_combo_box_append_text (fastestBtn, "shortest");
  gtk_combo_box_set_active (fastestBtn, 0);
  gtk_box_pack_start (GTK_BOX (vbox),
    GTK_WIDGET (fastestBtn), FALSE, FALSE, 5);

  detailBtn = GTK_COMBO_BOX (gtk_combo_box_new_text ());
  gtk_combo_box_append_text (detailBtn, "Highest");
  gtk_combo_box_append_text (detailBtn, "High");
  gtk_combo_box_append_text (detailBtn, "Normal");
  gtk_combo_box_append_text (detailBtn, "Low");
  gtk_combo_box_append_text (detailBtn, "Lowest");
  gtk_combo_box_set_active (detailBtn, 2);
  gtk_box_pack_start (GTK_BOX (vbox), GTK_WIDGET (detailBtn), FALSE, FALSE,5);

  iconSet = GTK_COMBO_BOX (gtk_combo_box_new_text ());
  gtk_combo_box_append_text (iconSet, "CLASSIC");
  gtk_combo_box_append_text (iconSet, "classic");
  gtk_combo_box_append_text (iconSet, "SQUARE");
  gtk_combo_box_append_text (iconSet, "square");
  gtk_combo_box_set_active (iconSet, 1);
  gtk_box_pack_start (GTK_BOX (vbox), GTK_WIDGET (iconSet), FALSE, FALSE, 5);
  
  GtkWidget *getDirs = gtk_button_new_with_label ("Get Directions");
/*  gtk_box_pack_start (GTK_BOX (vbox), getDirs, FALSE, FALSE, 5);
  gtk_signal_connect (GTK_OBJECT (getDirs), "clicked",
    GTK_SIGNAL_FUNC (GetDirections), NULL);
*/
  followGPSr = gtk_check_button_new_with_label ("Follow GPSr");
  
  #ifndef WIN32  
  struct sockaddr_in sa;
  int gpsSock = socket (PF_INET, SOCK_STREAM, 0);
  sa.sin_family = AF_INET;
  sa.sin_port = htons (2947);
  sa.sin_addr.s_addr = htonl (0x7f000001); //(204<<24)|(17<<16)|(205<<8)|18); 
  if (gpsSock != -1 &&
      connect (gpsSock, (struct sockaddr *)&sa, sizeof (sa)) == 0) {
    send (gpsSock, "R\n", 2, 0);
    gpsSockTag = gdk_input_add (/*gpsData->gps_fd*/ gpsSock, GDK_INPUT_READ,
      (GdkInputFunction) ReceiveNmea /*gps_poll*/, NULL);

    gtk_box_pack_start (GTK_BOX (vbox), followGPSr, FALSE, FALSE, 5);
    gtk_widget_show (followGPSr);
    // gtk_signal_connect (GTK_OBJECT (followGPSr), "clicked",
  }
  #endif

  gtk_signal_connect (GTK_OBJECT (window), "delete_event",
    GTK_SIGNAL_FUNC (gtk_main_quit), NULL);
  
  gtk_widget_set_usize (window, 400, 300);
  gtk_widget_show (search);
  gtk_widget_show (list);
  gtk_widget_show (draw);
  gtk_widget_show (GTK_WIDGET (carBtn));
  gtk_widget_show (GTK_WIDGET (fastestBtn));
  gtk_widget_show (GTK_WIDGET (detailBtn));
  gtk_widget_show (GTK_WIDGET (iconSet));
  gtk_widget_show (getDirs);
  gtk_widget_show (hbox);
  gtk_widget_show (vbox);
  gtk_widget_show (window);
  IncrementalSearch ();
  gtk_main ();
  #endif // HEADLESS
  return 0;
}
#endif // !_WIN32_WCE

/*--------------------------------- Rebuid code ---------------------------*/
#ifndef _WIN32_WCE
// These defines are only used during rebuild
#define MAX_BUCKETS (1<<26)
#define IDXGROUPS 676
#define NGROUPS 90
#define NGROUP(x)  ((x) / 3000000 % NGROUPS + IDXGROUPS)
#define S1GROUPS NGROUPS
#define S1GROUP(x) ((x) / 3000000 % NGROUPS + IDXGROUPS + NGROUPS)
#define S2GROUPS 33 // Last group is reserved for lowzoom halfSegs
#define S2GROUP(x) ((x) / (MAX_BUCKETS / (S2GROUPS - 1)) + IDXGROUPS + NGROUPS * 2)
#define PAIRS (32 * 1024 * 1024)
#define PAIRGROUPS 50
#define PAIRGROUP(x) ((x) / PAIRS + S2GROUP (0) + S2GROUPS)
#define PAIRGROUPS2 50
#define PAIRGROUP2(x) ((x) / PAIRS + PAIRGROUP (0) + PAIRGROUPS)
#define MAX_NODES 3000000 /* Max in a group */
#define FIRST_LOWZ_OTHER (PAIRS * (PAIRGROUPS - 1))

#define REBUILDWATCH(x) fprintf (stderr, "%3d %s\n", ++rebuildCnt, #x); x

#define TO_HALFSEG -1 // Rebuild only

struct halfSegType { // Rebuild only
  int lon, lat, other, wayPtr;
};

struct nodeType {
  int id, lon, lat;
};

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
    a->lat - b->lat;
}

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

int main (int argc, char *argv[])
{
  #ifndef WIN32
  int rebuildCnt = 0;
  if (argc > 1) {
    if ((argc != 6 && argc > 2) || stricmp (argv[1], "rebuild")) {
      fprintf (stderr, "Usage : %s [rebuild [bbox for 2 pass]]\n"
      "See http://wiki.openstreetmap.org/index.php/gosmore\n", argv[0]);
      return 1;
    }
    FILE *pak, *masterf;
    int head = 0xEB3A941, styleCnt = 0, ndStart;
    int bbox[4] = { INT_MIN, INT_MIN, 0x7fffffff, 0x7fffffff };
    wayType *master = /* shutup gcc */ NULL;
    if (argc == 6) {
      if (!(masterf = fopen64 ("master.pak", "r")) ||
          fseek (masterf, -sizeof (ndStart), SEEK_END) != 0 ||
          fread (&ndStart, sizeof (ndStart), 1, masterf) != 1 ||
          (long)(master = (wayType *)mmap (NULL, ndStart, PROT_READ,
                                MAP_SHARED, fileno (masterf), 0)) == -1) {
        fprintf (stderr, "Unable to open master.pak for bbox rebuild\n");
        return 4;
      }
      bbox[0] = Latitude (atof (argv[2]));
      bbox[1] = Longitude (atof (argv[3]));
      bbox[2] = Latitude (atof (argv[4]));
      bbox[3] = Longitude (atof (argv[5]));
    }
    if (!(pak = fopen64 ("gosmore.pak", "w+"))) {
      fprintf (stderr, "Cannot create gosmore.pak\n");
      return 2;
    }
    fwrite (&head, sizeof (head), 1, pak);
    
    //------------------------- elemstyle.xml : --------------------------
    char *style_k[2 << STYLE_BITS], *style_v[2 << STYLE_BITS];
    int defaultRestrict[2 << STYLE_BITS];
    memset (defaultRestrict, 0, sizeof (defaultRestrict));
    FILE *icons_csv = fopen ("icons.csv", "r");
    xmlTextReaderPtr sXml = xmlNewTextReaderFilename ("elemstyles.xml");
    if (!sXml || !icons_csv) {
      fprintf (stderr, "Either icons.csv or elemstyles.xml not found\n");
      return 3;
    }
    styleStruct srec[2 << STYLE_BITS];
    memset (&srec, 0, sizeof (srec));
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
            if (strcasecmp (n, "k") == 0) style_k[styleCnt] = strdup (v);
            if (strcasecmp (n, "v") == 0) style_v[styleCnt] = strdup (v);
          }                                     // memory leak -^
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
              char line[80];
              static char *set[] = { "classic.big_", "classic.small_",
                "square.big_", "square.small_" };
              for (int i = 0; i < 4; i++) {
                int slen = strlen (set[i]), vlen = strlen (v);
                rewind (icons_csv);
                while (fgets (line, sizeof (line) - 1, icons_csv)) {
                  if (strncmp (line, set[i], slen) == 0 &&
                      strncmp (line + slen, v, vlen - 1) == 0) {
                    sscanf (line + slen + vlen, ":%d:%d:%d:%d",
                      srec[styleCnt].x + i * 4, srec[styleCnt].x + i * 4 + 1,
                      srec[styleCnt].x + i * 4 + 2,
                      srec[styleCnt].x + i * 4 + 3);
                  }
                }
              }
            }
          }
          if (strcasecmp (name, "routing") == 0 && atoi (v) > 0) {
            #define M(field) if (strcasecmp (n, #field) == 0) {\
              defaultRestrict[styleCnt] |= 1 << field ## R; \
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
        if (styleCnt < (2 << STYLE_BITS) - 1) styleCnt++;
        else fprintf (stderr, "Too many rules. Increase STYLE_BITS\n");
      }
      xmlFree (name);
      //xmlFree (val);      
    }
    for (int i = 0; i < l1; i++) {
      double max = 0;
      for (int j = 0; j < styleCnt; j++) {
        if (srec[j].aveSpeed[i] > max) max = srec[j].aveSpeed[i];
      }
      for (int j = 0; j < styleCnt; j++) {
        srec[j].invSpeed[i] = max / srec[j].aveSpeed[i];
      }
    }
    fwrite (&srec, sizeof (srec[0]), styleCnt + 1, pak);    
    xmlFreeTextReader (sXml);

    //-------------------------- OSM Data File : ---------------------------
    xmlTextReaderPtr xml = xmlReaderForFd (STDIN_FILENO, "", NULL, 0);
//    xmlTextReaderPtr xml = xmlReaderForFile ("/dosc/osm/r28_2.osm", "", 0);
    FILE *groupf[PAIRGROUP2 (0) + PAIRGROUPS2];
    char groupName[PAIRGROUP2 (0) + PAIRGROUPS2][9];
    for (int i = 0; i < PAIRGROUP2 (0) + PAIRGROUPS2; i++) {
      sprintf (groupName[i], "%c%c%d.tmp", i / 26 % 26 + 'a', i % 26 + 'a',
        i / 26 / 26);
      if (!(groupf[i] = fopen64 (groupName[i], "w+"))) {
        fprintf (stderr, "Cannot create temporary file.\nPossibly too many"
          " open files, in which case you must run ulimit -n or recompile\n");
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
    int yesMask = 0, noMask = 0, wayInBbox = 1;
    int lowzList[1000], lowzListCnt = 0;
    // wayInBbox : 1 way slips through...
    s[0].lat = 0; // Should be -1 ?
    s[0].other = -2;
    s[1].other = -1;
    wayType w;
    w.clat = 0;
    w.clon = 0;
    w.dlat = INT_MIN;
    w.dlon = INT_MIN;
    w.bits = styleCnt;
    
    master = (wayType *)(((char *)master) + ftell (pak));
    
    char *tag_k = NULL, *tags = (char *) BAD_CAST xmlStrdup (BAD_CAST "");
    REBUILDWATCH (while (xmlTextReaderRead (xml))) {
      char *name = (char *) BAD_CAST xmlTextReaderName (xml);
      //xmlChar *value = xmlTextReaderValue (xml); // always empty
      if (xmlTextReaderNodeType (xml) == XML_READER_TYPE_ELEMENT) {
        isNode = stricmp (name, "way") != 0 && 
                 (stricmp (name, "node") == 0 || isNode);
        while (xmlTextReaderMoveToNextAttribute (xml)) {
          char *aname = (char *) BAD_CAST xmlTextReaderName (xml);
          char *avalue = (char *) BAD_CAST xmlTextReaderValue (xml);
  //        if (xmlStrcasecmp (name, "node") == 0) 
          if (stricmp (aname, "id") == 0) nd.id = atoi (avalue);
          if (stricmp (aname, "lat") == 0) nd.lat = Latitude (atof (avalue));
          if (stricmp (aname, "lon") == 0) nd.lon = Longitude (atof (avalue));
          if (stricmp (name, "nd") == 0 && stricmp (aname, "ref") == 0
              && wayInBbox) {
            if (s[0].lat) {
              fwrite (s, sizeof (s), 1, groupf[S1GROUP (s[0].lat)]);
            }
            s[0].wayPtr = ftello64 (pak);
            s[1].wayPtr = TO_HALFSEG;
            s[1].other = s[0].other + 1;
            s[0].other = nOther++ * 2;
            s[0].lat = atoi (avalue);
            if (lowzListCnt >=
                int (sizeof (lowzList) / sizeof (lowzList[0]))) lowzListCnt--;
            lowzList[lowzListCnt++] = atoi (avalue);
          }
          if (stricmp (aname, "v") == 0) {
            #define K_IS(x) (stricmp (tag_k, x) == 0)
            #define V_IS(x) (stricmp (avalue, x) == 0)
            if (StyleNr (&w) == styleCnt) {
              for (w.bits &= ~((2<<STYLE_BITS) - 1); StyleNr (&w) < styleCnt
                        && !(K_IS (style_k[StyleNr (&w)])
                             && V_IS (style_v[StyleNr (&w)])); w.bits++) {}
            }
            if (K_IS ("name")) {
              xmlChar *tmp = xmlStrdup (BAD_CAST "\n");
              tmp = xmlStrcat (BAD_CAST tmp, BAD_CAST avalue);
              avalue = tags; // Old 'tags' will be freed
              tags = (char*) xmlStrcat (tmp, BAD_CAST tags);
              // name always first tag.
            }
            else if (K_IS ("layer")) w.bits |= atoi (avalue) << 29;
            
            #define M(field) else if (K_IS (#field)) { \
                if (V_IS ("yes") || V_IS ("1") || V_IS ("permissive")) { \
                  yesMask |= 1 << field ## R; \
                } else if (V_IS ("no") || V_IS ("0") || V_IS ("private")) { \
                  noMask |= 1 << field ## R; \
                } \
              }
            RESTRICTIONS
            #undef M
            
            else if (!K_IS ("created_by") && !K_IS ("converted_by") &&
              strncasecmp (tag_k, "source", 6) != 0 &&
              !V_IS ("no") && !V_IS ("false") && 
              strncasecmp (tag_k, "tiger:", 6) != 0 &&
              !K_IS ("attribution") /* Mostly MassGIS */ &&
              !K_IS ("sagns_id") && !K_IS ("sangs_id") && 
              !K_IS ("is_in") && !V_IS ("residential") &&
              !V_IS ("junction") && /* Not approved and when it isn't obvious
                from the ways that it's a junction, the tag will often be
                something ridiculous like junction=junction ! */
// blocked as highway:  !V_IS ("mini_roundabout") && !V_IS ("roundabout") &&
              !V_IS ("traffic_signals") && !K_IS ("editor") &&
              !K_IS ("time") && !K_IS ("ele") && !K_IS ("hdop") &&
                !K_IS ("sat") && !K_IS ("pdop") && !K_IS ("speed") &&
                !K_IS ("course") && !K_IS ("fix") && !K_IS ("vdop") &&
              !K_IS ("class") /* esp. class=node */ &&
              !K_IS ("type") /* This is only for boules, but we drop it
                because it's often misused */ &&
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
          }
          else xmlFree (avalue);
          xmlFree (aname);
        } /* While it's an attribute */
        if (stricmp (name, "node") == 0 && bbox[0] <= nd.lat &&
            bbox[1] <= nd.lon && nd.lat <= bbox[2] && nd.lon <= bbox[3]) {
          fwrite (&nd, sizeof (nd), 1, groupf[NGROUP (nd.id)]);
        }
      }
      if (xmlTextReaderNodeType (xml) == XML_READER_TYPE_END_ELEMENT) {
        int nameIsNode = stricmp (name, "node") == 0;
        if (stricmp (name, "way") == 0 || nameIsNode) {
          if (!nameIsNode || (strlen (tags) > 8 || StyleNr (&w)!=styleCnt)) {
            if (nameIsNode && wayInBbox) {
              if (s[0].lat) { // Flush s
                fwrite (s, sizeof (s), 1, groupf[S1GROUP (s[0].lat)]);
              }
              s[0].lat = nd.id; // Create 2 fake halfSegs
              s[0].wayPtr = ftello64 (pak);
              s[1].wayPtr = TO_HALFSEG;
              s[0].other = -2; // No next
              s[1].other = -1; // No prev
              lowzList[lowzListCnt++] = nd.id;
            }
            if (s[0].other > -2) { // Not lowz
              if (s[0].other >= 0) nOther--; // Reclaim unused 'other' number
              s[0].other = -2;
            }

            if (srec[StyleNr (&w)].scaleMax > 10000000 && wayInBbox) {
              for (int i = 0; i < lowzListCnt; i++) {
                if (i % 4 && i < lowzListCnt - 1) continue; // Skip some
                if (s[0].lat) { // Flush s
                  fwrite (s, sizeof (s), 1, groupf[S1GROUP (s[0].lat)]);
                }
                s[0].lat = lowzList[i];
                s[0].wayPtr = ftello64 (pak);
                s[1].wayPtr = TO_HALFSEG;
                s[1].other = i == 0 ? -4 : lowzOther++;
                s[0].other = i == lowzListCnt -1 ? -4 : lowzOther++;
              }
            }
            lowzListCnt = 0;
          
            if (StyleNr (&w) < styleCnt && stricmp (style_v[StyleNr (&w)],
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
            w.bits |= (defaultRestrict[StyleNr (&w)] | yesMask) &
              ((noMask & accessR) ? 0 : ~noMask);
            char *compact = tags[0] == '\n' ? tags + 1 : tags;
            if (wayInBbox) {
              fwrite (&w, sizeof (w), 1, pak);
              fwrite (tags + strlen (tags), 1, 1, pak); // '\0' at the front
              for (char *ptr = tags; *ptr != '\0'; ) {
                if (*ptr++ == '\n') {
                  unsigned idx = ftello64 (pak) + ptr - 1 - tags, grp;
                  for (grp = 0; grp < IDXGROUPS - 1 &&
                     TagCmp (groupName[grp], ptr) < 0; grp++) {}
                  fwrite (&idx, sizeof (idx), 1, groupf[grp]);
                }
              }
              fwrite (compact, strlen (compact) + 1, 1, pak);
            
              // Write variable length tags and align on 4 bytes
              if (ftello64 (pak) & 3) {
                fwrite (tags, 4 - (ftello64 (pak) & 3), 1, pak);
              }
            }
            master = (wayType*)((char*)master +
              ((1 + strlen (compact) + 1 + 3) & ~3)) + 1;
            wayInBbox = argc < 6 || (bbox[0] <= master->clat + master->dlat
                                  && bbox[1] <= master->clon + master->dlon
                                  && master->clat - master->dlat <= bbox[2]
                                  && master->clon - master->dlat <= bbox[3]);
            //xmlFree (tags); // Just set tags[0] = '\0'
            //tags = (char *) xmlStrdup (BAD_CAST "");
          }
          tags[0] = '\0'; // Erase nodes with short names
          yesMask = noMask = 0;
          w.bits = styleCnt;
        }
      } // if it was </...>
      xmlFree (name);
    } // While reading xml
    if (s[0].lat && wayInBbox) {
      fwrite (s, sizeof (s), 1, groupf[S1GROUP (s[0].lat)]);
    }
    assert (nOther * 2 < FIRST_LOWZ_OTHER);
    bucketsMin1 = (nOther >> 5) | (nOther >> 6);
    bucketsMin1 |= bucketsMin1 >> 2;
    bucketsMin1 |= bucketsMin1 >> 4;
    bucketsMin1 |= bucketsMin1 >> 8;
    bucketsMin1 |= bucketsMin1 >> 16;
    assert (bucketsMin1 < MAX_BUCKETS);
    
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
      int size = ftell (groupf[i]);
      rewind (groupf[i]);
      REBUILDWATCH (halfSegType *seg = (halfSegType *) mmap (NULL, size,
        PROT_READ | PROT_WRITE, MAP_SHARED, fileno (groupf[i]), 0));
      qsort (seg, size / sizeof (s), sizeof (s),
        (int (*)(const void *, const void *))HalfSegCmp);
      for (int j = 0; j < size / (int) sizeof (seg[0]); j++) {
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
    
    ndStart = ftello64 (pak);
    
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
    REBUILDWATCH (for (int i = 0; i < IDXGROUPS; i++)) {
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
//    printf ("ndCount=%d\n", ndCount);
    munmap (pak, ndStart);
    fwrite (hashTable, sizeof (*hashTable),
      bucketsMin1 + (bucketsMin1 >> 7) + 3, pak);
    fwrite (&bucketsMin1, sizeof (bucketsMin1), 1, pak);
    fwrite (&ndStart, sizeof (ndStart), 1, pak); /* for ndBase */
    fclose (pak);
    free (hashTable);
  } /* if rebuilding */
  #endif // WIN32
  return UserInterface (argc, argv);
}
#else // _WIN32_WCE
//----------------------------- _WIN32_WCE ------------------
HANDLE port;
volatile int gpsNewDataReady = FALSE; // Serves as lock on gpsNew

HINSTANCE hInst;
HWND   hwnd;
HBITMAP bmp;
HDC memDc, bufDc;
HPEN pen[2 << STYLE_BITS];

BOOL CALLBACK DlgSearchProc (
	HWND hwnd, 
	UINT Msg, 
	WPARAM wParam, 
	LPARAM lParam)
{
    switch (Msg) {
    case WM_COMMAND:
      if (LOWORD (wParam) == IDC_EDIT1) {
        HWND hwndList = GetDlgItem (hwnd, IDC_LIST1);
        //SendMessage (hwndList, LB_SETITEMDATA, 
        SendMessage (hwndList, LB_ADDSTRING, 0, (LPARAM) TEXT ("45")); //, 123);
	return TRUE;
      }
      else if (wParam == IDC_SEARCHGO
        /* || LOWORD (wParam) == IDC_LIST1 && HIWORD (wParam) == LBN_DBLCLK */) {
        HWND hwndList = GetDlgItem (hwnd, IDC_LIST1);
        int idx = SendMessage (hwndList, LB_GETCURSEL, 0, 0);
        EndDialog (hwnd, 0);
        return TRUE;
      }
    }
    return FALSE;
}

int pakSize;
LRESULT CALLBACK MainWndProc(HWND hWnd,UINT message,
                                  WPARAM wParam,LPARAM lParam)
{
  PAINTSTRUCT ps;
  RECT rect;
  static wchar_t msg[200] = TEXT("No coms");
  static int done = FALSE;

  switch(message) {
    #if 0
    case WM_HOTKEY:
      if (VK_TBACK == HIWORD(lParam) && (0 != (MOD_KEYUP & LOWORD(lParam)))) {
        PostQuitMessage (0);
      }
      break;
    #endif

    case WM_DESTROY:
      PostQuitMessage(0);
      break;
    case WM_PAINT:
      do { // Keep compiler happy.
      BeginPaint (hWnd, &ps);
      //GetClientRect (hWnd, &r);
      //SetBkColor(ps.hdc,RGB(63,63,63));
      //SetTextColor(ps.hdc,(i==state)?RGB(0,128,0):RGB(0,0,0));
      //r.left = 50;
      // r.top = 50;
      if (data) {
	if (!done) {
          bmp = LoadBitmap (hInst, MAKEINTRESOURCE (IDB_BITMAP1));
          memDc = CreateCompatibleDC (ps.hdc);
          SelectObject(memDc, bmp);

          bufDc = CreateCompatibleDC (ps.hdc); //bufDc //GetDC (hWnd));
          bmp = CreateCompatibleBitmap (ps.hdc, GetSystemMetrics(SM_CXSCREEN),
            GetSystemMetrics(SM_CYSCREEN));
          SelectObject (bufDc, bmp);
          for (int i = 0; i < 1 || style[i - 1].scaleMax; i++) {
            pen[i] = CreatePen (style[i].dashed ? PS_DASH : PS_SOLID,
              style[i].lineWidth, (style[i].lineColour >> 16) |
                (style[i].lineColour & 0xff00) |
                ((style[i].lineColour & 0xff) << 16));
          }
          done = TRUE;
        }
	rect.top = rect.left = 0;
	rect.right = GetSystemMetrics(SM_CXSCREEN);
	rect.bottom = GetSystemMetrics(SM_CYSCREEN);
        Expose (bufDc, memDc, pen);
	BitBlt (ps.hdc, 0, 0, rect.right,  rect.bottom, bufDc, 0, 0, SRCCOPY);
	FillRect (bufDc, &rect, (HBRUSH) GetStockObject(WHITE_BRUSH));
      }
      else {
        wsprintf (msg, TEXT ("Can't allocate %d bytes"), pakSize);
//        wsprintf (msg, TEXT ("%x bytes"), *(int*)&w);
        ExtTextOut (ps.hdc, 50, 50, 0, NULL, msg, wcslen (msg), NULL);
      }
//      HPEN pen = CreatePen (a[c2].lineDashed ? PS_DASH : PS_SOLID,
      EndPaint (hWnd, &ps);
      } while (0);
      break;
    case WM_CHAR:

      break;
    case WM_KEYDOWN:
      // The TGPS 375 can generate 12 keys :
      // VK_RETURN, VK_UP, VK_DOWN, VK_LEFT, VK_RIGHT,
      // 193=0xC1=Zoom in, 194=0xC2=Zoom out, 198=0xC6=menu 197=0xC5=settings
      // 195=0xC3=V+, 196=0xC4=V- which is VK_APP1 to VK_APP6
      // and WM_CHAR:VK_BACK
//        nResult = DialogBox(hInst, MAKEINTRESOURCE(IDD_DLGSEARCH),
//	  NULL, (DLGPROC)DlgSearchProc);
      if (wParam == 193) zoom = zoom * 3 / 4;
      if (wParam == 194) zoom = zoom * 4 / 3;
      if (wParam == 197) option[FollowGPSr] = !option[FollowGPSr];

      if (VK_DOWN == wParam) clat -= zoom / 2;
      else if (VK_UP == wParam) clat += zoom / 2;
      else if (VK_LEFT == wParam) clon -= zoom / 2;
      else if (VK_RIGHT == wParam) clon += zoom / 2;
      else goto noChangeFollow;
        option[FollowGPSr] = FALSE;
      noChangeFollow:

      if (VK_RETURN == wParam) {
        PostMessage (hwnd, WM_CLOSE, 0, 0);
      }
      else InvalidateRect (hWnd, NULL, FALSE);
      break;
    case WM_USER + 1:
      /*
      wsprintf (msg, TEXT ("%c%c %c%c %9.5lf %10.5lf %lf %lf"),
        gpsNew.fix.date[0], gpsNew.fix.date[1],
        gpsNew.fix.tm[4], gpsNew.fix.tm[5],
        gpsNew.fix.latitude, gpsNew.fix.longitude, gpsNew.fix.ele,
	gpsNew.fix.hdop); */
      if (option[FollowGPSr]) {
        clat = Latitude (gpsNew.fix.latitude);
        clon = Longitude (gpsNew.fix.longitude);
        InvalidateRect (hWnd, NULL, FALSE);
      }
      gpsNewDataReady = FALSE;
      break;
/*    case WM_LBUTTONDOWN:
      //MoveTo (LOWORD(lParam), HIWORD(lParam));
      //PostQuitMessage (0);
      //if (HIWORD(lParam) < 30) {
        // state=LOWORD(lParam)/STATEWID;
      //}
      break;
    case WM_LBUTTONUP:
      break;
    case WM_MOUSEMOVE:
      //LineTo (LOWORD(lParam), HIWORD(lParam));
      break;
    case WM_COMMAND:
     //switch(wParam) {
     //}
     break; */
    default:
      return(DefWindowProc(hWnd,message,wParam,lParam));
  }
  return(NULL);
}

BOOL InitApplication (void)
{
  WNDCLASS wc;

  wc.style=0;
  wc.lpfnWndProc=(WNDPROC)MainWndProc;
  wc.cbClsExtra=0;
  wc.cbWndExtra=0;
  wc.hInstance= hInst;
  wc.hIcon=NULL; 
  wc.hCursor=LoadCursor(NULL,IDC_ARROW);
  wc.hbrBackground=(HBRUSH) GetStockObject(WHITE_BRUSH);
  wc.lpszMenuName = NULL;
  wc.lpszClassName = TEXT ("GosmoreWClass");

  return(RegisterClass(&wc));
}

HWND InitInstance(int nCmdShow)
{
  hwnd= CreateWindow (TEXT ("GosmoreWClass"), TEXT ("gosmore"), WS_DLGFRAME,
    0, 0, CW_USEDEFAULT/* 20 */,/* 240*/CW_USEDEFAULT,NULL,NULL, hInst,NULL);

  if(!hwnd) return(FALSE);

  ShowWindow(hwnd,nCmdShow);
  UpdateWindow(hwnd);


  return hwnd;
}

volatile int guiDone = FALSE;

DWORD WINAPI NmeaReader (LPVOID lParam)
{
 // $GPGLL,2546.6752,S,02817.5780,E,210130.812,V,S*5B
  DWORD nBytes;
//  DCB    portState;
//  COMMTIMEOUTS commTiming;
  char rx[1200];

  //ReadFile(port, rx, sizeof(rx), &nBytes, NULL);
//  Sleep (1000);
  /* It seems as the CreateFile before returns the action has been completed, causing
  the subsequent change of baudrate to fail. This read / sleep ensures that the port is open
  before continuing. */
    #if 0
    GetCommTimeouts (port, &commTiming);
    commTiming.ReadIntervalTimeout=20; /* Blocking reads */
    commTiming.ReadTotalTimeoutMultiplier=0;
    commTiming.ReadTotalTimeoutConstant=0;

    commTiming.WriteTotalTimeoutMultiplier=5; /* No writing */
    commTiming.WriteTotalTimeoutConstant=5;
    SetCommTimeouts (port, &commTiming);
    #endif
#if 0
    if(!GetCommState(port, &portState)) {
      MessageBox (NULL, TEXT ("GetCommState Error"), TEXT (""),
        MB_APPLMODAL|MB_OK);
      return(1);
    }
    portState.BaudRate=CBR_38400;
    portState.Parity=0;
    portState.StopBits=ONESTOPBIT;
    portState.ByteSize=8;
    portState.fBinary=1;
    portState.fParity=0;
    portState.fOutxCtsFlow=0;
    portState.fOutxDsrFlow=0;
    portState.fDtrControl=DTR_CONTROL_ENABLE;
    portState.fDsrSensitivity=0;
    portState.fTXContinueOnXoff=1;
    portState.fOutX=0;
    portState.fInX=0;
    portState.fErrorChar=0;
    portState.fNull=0;
    portState.fRtsControl=RTS_CONTROL_ENABLE;
    portState.fAbortOnError=1;

    if(!SetCommState(port, &portState)) {
      MessageBox (NULL, TEXT ("SetCommState Error"), TEXT (""),
        MB_APPLMODAL|MB_OK);
      return(1);
    }
#endif

  #if 0
  PurgeComm (port, PURGE_RXCLEAR); /* Baud rate wouldn't change without this ! */
  DWORD nBytes2 = 0;
  COMSTAT cStat;
  ClearCommError (port, &nBytes, &cStat);
  rx2 = (char*) malloc (600);
  ReadFile(port, rx, sizeof(rx), &nBytes, NULL);
    if(!GetCommState(port, &portState)) {
      MessageBox (NULL, TEXT ("GetCommState Error"), TEXT (""),
        MB_APPLMODAL|MB_OK);
      return(1);
    }
  ReadFile(port, rx2, 600, &nBytes2, NULL);
  #endif
  FILE *log = fopen ("\\My Documents\\log.nmea", "a");
  while (!guiDone) {
    //nBytes = sizeof (rx) - got;
    //got = 0;
    if (!ReadFile(port, rx, sizeof(rx), &nBytes, NULL) || nBytes <= 0) {
      continue;
    }
    if (log) fwrite (rx, nBytes, 1, log);

    //wndStr[0]='\0';
    //FormatMessage (FORMAT_MESSAGE_FROM_SYSTEM, NULL, GetLastError(),
    //MAKELANGID(LANG_ENGLISH,SUBLANG_ENGLISH_US),wndStr,STRLEN,NULL);
    
    if (!gpsNewDataReady &&
        (gpsNewDataReady = ProcessNmea (rx, (unsigned*)&nBytes))) {
      PostMessage (hwnd, WM_USER + 1, 0, 0);
    }
  }
  guiDone = FALSE;
  if (log) fclose (log);
  CloseHandle (port);
  return 0;
}


int WINAPI WinMain(
    HINSTANCE  hInstance,	  // handle of current instance
    HINSTANCE  hPrevInstance,	  // handle of previous instance
    LPWSTR  lpszCmdLine,	          // pointer to command line
    int  nCmdShow)	          // show state of window
{
  if(hPrevInstance) return(FALSE);
  hInst = hInstance;
  FILE *gmap = fopen ("gosmore.pak", "rb");

  if (!gmap) {
    MessageBox (NULL, TEXT ("No pak file"), TEXT (""), MB_APPLMODAL|MB_OK);
    return 1;
  }
  fseek (gmap, 0, SEEK_END);
  pakSize = ftell (gmap);
  fseek (gmap, 0, SEEK_SET);
  data = (char *) malloc (pakSize);
  if (!data) {
    MessageBox (NULL, TEXT ("Out of memory"), TEXT (""), MB_APPLMODAL|MB_OK);
    return 1; // This may mean memory is available, but fragmented.
  } // Splitting the 5 parts may help.
  fread (data, pakSize, 1, gmap);
  style = (struct styleStruct *)(data + 4);
  hashTable = (int *) (data + pakSize);
  ndBase = (ndType *)(data + hashTable[-1]);
  bucketsMin1 = hashTable[-2];
  hashTable -= bucketsMin1 + (bucketsMin1 >> 7) + 5;

  clon = ndBase[1].lon; //Longitude (-0.272228);
  clat = ndBase[1].lat; //Latitude (51.927977);
  zoom = lrint (0.1 / 5 / 180 * 2147483648.0 * cos (26.1 / 180 * M_PI));

  if(!InitApplication ()) return(FALSE);
  if (!InitInstance (nCmdShow)) return(FALSE);

  DWORD threadId;
  if((port=CreateFile (TEXT ("COM1:"), GENERIC_READ | GENERIC_WRITE, 0,
          NULL, OPEN_EXISTING, 0, 0)) != INVALID_HANDLE_VALUE) {
    CreateThread (NULL, 0, NmeaReader, NULL, 0, &threadId);
  }
  else MessageBox (NULL, TEXT ("No Port"), TEXT (""), MB_APPLMODAL|MB_OK);

  MSG    msg;
  while (GetMessage (&msg, NULL, 0, 0)) {
    TranslateMessage (&msg);
    DispatchMessage (&msg);
  }
  guiDone = TRUE;
  while (guiDone) Sleep (1000);
  return 0;
}
#endif
