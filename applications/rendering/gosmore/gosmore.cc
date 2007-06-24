/* gosmore - European weed widely naturalized in North America having yellow
   flower heads and leaves resembling a cat's ears */
   
/* This software is placed by in the public domain by its author, Nic Roets */

/* real    8m12.369s
user    11m7.727s
sys     0m15.199s
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <sys/mman.h>
#include <ctype.h>
#include <gtk/gtk.h>
#include <obstack.h> /* For obstack_printf in GetDirections */

#define stricmp strcasecmp

#define BUCKETS (1<<22) /* Must be power of 2 */
#define TILEBITS (18)
#define TILESIZE (1<<TILEBITS)

inline int Hash (int lon, int lat)
{ /* This is a universal hashfuntion in GF(2^31-1). The hexadecimal numbers */
  /* are from random.org, but experimentation will surely yield better */
  /* numbers. The more we right shift lon and lat, the larger */
  /* each tile will be. We can add constants to the lat and lon variables */
  /* to make them positive, but the clashes will still occur between the */
  /* same tiles. */

  /* Mercator projection means tiles are not the same physical size. */
  /* Compensating for this very low on the agenda vs. e.g. compensating for */
  /* high node density in Western Europe. */
  long long v = ((lon >> TILEBITS) /* + (1<<19) */) * (long long) 0x00d20381 +
    ((lat >> TILEBITS) /*+ (1<<19)*/) * (long long) 0x75d087d9;
  while (v >> 31) v = (v & ((1<<31) - 1)) + (v >> 31);
  /* Replace loop with v = v % ((1<<31)-1) ? */
  return v & (BUCKETS - 1);
} /* This mask means the last bucket is very, very slightly under used. */

struct nodeType {
  int id, lon, lat;
};

#define TO_HALFSEG -1

struct halfSegType {
  int lon, lat, other, wayPtr;
};

struct wayType {
  int type : 6;
  int layer : 3;
  int oneway : 1;
  int zoom16384 : 17; /* To make the way just fill the display */
  int name; /* Offset into pak file */
  int clat, clon; /* Centre */
};

struct wayBuildType {
  wayType w;
  char *name;
  int idx;
};

enum { rail, residential, motorway, motorway_link, trunk, primary, secondary,
  tertiary, /* track, footway, */ unwayed /* = lastway */, station, suburb,
  junction, hamlet, village, town, city, place /* = lastnode */
};

struct highwayType {
  char *feature, *name, *colour;
  int width;
  double invSpeed; /* 1.0 is the fastest. Everything else must be bigger. */
  GdkLineStyle style;
} highway[] = {
  /* ways */
  { "railway", "rail"         , "black",  3, 99.0, GDK_LINE_ON_OFF_DASH },
  { "highway", "residential"  , "white",  1, 120.0 / 34.0, GDK_LINE_SOLID },
  { "highway", "motorway"     , "blue",   3, 1.0, GDK_LINE_SOLID },
  { "highway", "motorway_link", "blue",   3, 1.0, GDK_LINE_SOLID },
  { "highway", "trunk"        , "green",  3, 120.0 / 70.0, GDK_LINE_SOLID },
  { "highway", "primary"      , "red",    2, 120.0 / 60.0, GDK_LINE_SOLID },
  { "highway", "secondary"    , "orange", 2, 120.0 / 50.0, GDK_LINE_SOLID },
  { "highway", "tertiary"     , "yellow", 1, 120.0 / 40.0, GDK_LINE_SOLID },
  { "highway", "track"        , "brown",  1, 120.0 / 30.0, GDK_LINE_SOLID },
//  { "highway", "footway"        , "brown",  1, GDK_LINE_SOLID },
  /* nodes : */
  { "railway", "station"      , "red",    1, 1.0, GDK_LINE_SOLID },
  { "place",   "suburb"       , "black",  2, 1.0, GDK_LINE_SOLID },
  { "place",   "junction"     , "black",  1, 1.0, GDK_LINE_SOLID },
  { "place",   "halmet"       , "black",  1, 1.0, GDK_LINE_SOLID },
  { "place",   "village"      , "black",  1, 1.0, GDK_LINE_SOLID },
  { "place",   "town"         , "black",  2, 1.0, GDK_LINE_SOLID },
  { "place",   "city"         , "black",  3, 1.0, GDK_LINE_SOLID },
  { NULL, NULL /* named node of unidentified type */  , "gray",   1, 1.0,
    GDK_LINE_SOLID },
  { NULL, NULL /* unwayed */  , "gray",   1, 99.0, GDK_LINE_SOLID }
};

int NodeIdCmp (const void *a, const void *b)
{
  return ((nodeType*)a)->id - ((nodeType*)b)->id;
}

int HalfSegIdCmp (const void *a, const void *b)
{
  return ((halfSegType*)a)->other - ((halfSegType *)b)->other;
}

int HalfSegCmp (const halfSegType *a, const halfSegType *b)
{
  int hasha = Hash (a->lon, a->lat), hashb = Hash (b->lon, b->lat);
  return hasha != hashb ? hasha - hashb : a->lon != b->lon ? a->lon - b->lon :
    a->lat - b->lat;
}

int WayBuildCmp (const void *a, const void *b)
{
  return !((wayBuildType *)a)->name ? -1 : !((wayBuildType *)b)->name ? 1 :
    strcmp (((wayBuildType *)a)->name, ((wayBuildType *)b)->name);
}

void quicksort (void *base, int n, int size,
  int (*cmp)(const void *, const void*))
{ /* Builtin qsort performs badly when dataset does not fit into memory,
     probably because it uses mergesort. quicksort quickly divides the
     problem into sections that is small enough to fit into memory and
     finishes them before tackling the rest. */
  static halfSegType pivot[2]; /* 1 segment is largest object we're sorting */
  
  if (size * n > 50000000) printf ("%9d items of %d bytes\n", n, size);
  char *l = (char*) base, *h = (char*) base + (n - 1) * size;
  memcpy (pivot, l + n / 2 * size, size);
  memcpy (l + n / 2 * size, l, size);
  while (l < h) {
    while (l < h && (*cmp)(pivot, h) <= 0) h -= size;
    if (l < h) memcpy (l, h, size);
    while (l < h && (*cmp)(pivot, l) >= 0) l += size;
    if (l < h) memcpy (h, l, size);
  }
  if (l > h) fprintf (stderr, "sort warning !\n");
  memcpy (l, pivot, size);
  if (l - (char*) base > size)
    quicksort (base, (l - (char*) base) / size, size, cmp);
  if ((n - 2) * size > h - (char*) base)
    quicksort (h + size, n - (h - (char*) base) / size - 1, size, cmp);
}

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
int *hashTable;
char *data;

struct OsmItr { // Iterate over all the objects in a square
  halfSegType *hs[2]; /* Readonly. Either can be 'from' or 'to', but you */
  /* can be guaranteed that nodes will be in hs[0] */
  
  int slat, slon, left, right, top, bottom; /* Private */
  halfSegType *end;
  
  OsmItr (int l, int t, int r, int b)
  {
    left = l & (~(TILESIZE - 1));
    right = (r + TILESIZE - 1) & (~(TILESIZE-1));
    top = t & (~(TILESIZE - 1));
    bottom = (b + TILESIZE - 1) & (~(TILESIZE-1));
    
    slat = top;
    slon = left - TILESIZE;
    hs[0] = end = NULL;
  }
};

int Next (OsmItr &itr) /* Friend of osmItr */
{
  do {
    itr.hs[0]++;
    while (itr.hs[0] >= itr.end) {
      if ((itr.slon += TILESIZE) == itr.right) {
        itr.slon = itr.left;  /* Here we wrap around from N85 to S85 ! */
        if ((itr.slat += TILESIZE) == itr.bottom) return FALSE;
      }
      int bucket = Hash (itr.slon, itr.slat);
      itr.hs[0] = (halfSegType *) (data + hashTable[bucket]);
      itr.end = (halfSegType *) (data + hashTable[bucket + 1]);
    }
  } while (((itr.hs[0]->lon ^ itr.slon) >> TILEBITS) ||
           ((itr.hs[0]->lat ^ itr.slat) >> TILEBITS) ||
      ((itr.hs[1] = (halfSegType *) (data + itr.hs[0]->other)) > itr.hs[0] &&
       itr.left <= itr.hs[1]->lon && itr.hs[1]->lon < itr.right &&
       itr.top <= itr.hs[1]->lat && itr.hs[1]->lat < itr.bottom));
/* while hs[0] is a hash collision, or is the other half of something that
has already or will soon be iterated over. Test doesn't work for wrapping. */
  return TRUE;
}

halfSegType *FirstHalfSegAtNode (halfSegType *hs)
{
  while ((char *) hs > data + hashTable[0] && hs[-1].lon == hs->lon &&
    hs[-1].lat == hs->lat) hs--;
  return hs;
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
   promissing one. */
struct routeNodeType {
  halfSegType *hs;
  routeNodeType *shortest;
  int best, heapIdx;
} *route = NULL, *shortest = NULL, **routeHeap;
int dhashSize, routeHeapSize, limit, tlat, tlon, flat, flon;

#define Sqr(x) ((x)*(x))
int Best (routeNodeType *n)
{
  return limit < 2000000000 ? n->best : n->best +
    lrint (sqrt (Sqr ((long long)(n->hs->lon - flon)) +
                 Sqr ((long long)(n->hs->lat - flat))));
}

void AddRouteNode (halfSegType *hs, int best, routeNodeType *shortest)
{
  unsigned hash = (int) hs, i = 0;
  routeNodeType *n;
  do {
    if (i++ > 10) {
      fprintf (stderr, "Double hash bailout : Table full or hash function "
        "bad. Route will not be found or will be suboptimal\n");
      return;
    }
    hash = hash * (long long) 1664525 + 1013904223;
    /* Linear congruential generator from wikipedia */
    n = route + hash % dhashSize;
    if (n->hs == NULL) { /* First visit of this node */
      n->hs = hs;
      n->best = best + 1;
      /* Will do later : routeHeap[routeHeapSize] = n; */
      n->heapIdx = routeHeapSize++;
    }
  } while (n->hs != hs);
  if (n->best > best) {
    n->best = best;
    n->shortest = shortest;
    if (n->heapIdx < 0) n->heapIdx = routeHeapSize++;
    for (; n->heapIdx > 1 &&
         Best (n) < Best (routeHeap[n->heapIdx / 2]); n->heapIdx /= 2) {
      routeHeap[n->heapIdx] = routeHeap[n->heapIdx / 2];
      routeHeap[n->heapIdx]->heapIdx = n->heapIdx;
    }
    routeHeap[n->heapIdx] = n;
  }
}

void Route (int car, int fastest)
{ /* We start by finding the segment that is closest to 'from' and 'to' */
  halfSegType *endHs[2][2];
  int toEndHs[2][2];
  
  shortest = NULL;
  for (int i = 0; i < 2; i++) {
    int lon = i ? flon : tlon, lat = i ? flat : tlat;
    long long bestd = 4000000000000000000LL;
    /* find min (Sqr (distance)). Use long long so we don't loose accuracy */
    OsmItr itr (lon - 100000, lat - 100000, lon + 100000, lat + 100000);
    /* Search 1km x 1km around 'from' for the nearest segment to it */
    while (Next (itr)) {
      long long lon0 = lon - itr.hs[0]->lon, lat0 = lat - itr.hs[0]->lat,
                lon1 = lon - itr.hs[1]->lon, lat1 = lat - itr.hs[1]->lat,
                dlon = itr.hs[0]->lon - itr.hs[1]->lon,
                dlat = itr.hs[0]->lat - itr.hs[1]->lat;
      /* We use Pythagoras to test angles for being greater that 90 and
         consequently if the point is behind hs[0] or hs[1].
         If the point is "behind" hs[0], measure distance to hs[0] with
         Pythagoras. If it's "behind" hs[1], use Pythagoras to hs[1]. If
         neither, use perpendicular distance from a point to a line */
      long long d = dlon * lon0 >= - dlat * lat0 ? Sqr (lon0) + Sqr (lat0) :
        dlon * lon1 <= - dlat * lat1 ? Sqr (lon1) + Sqr (lat1) :
        Sqr ((dlon * lat1 - dlat * lon1) /
          lrint (sqrt (Sqr(dlon) + Sqr (dlat))));
      if (d < bestd) {
        int firstSeg = itr.hs[0]->wayPtr == TO_HALFSEG ? 1 : 0, oneway =
          ((wayType *)(data + itr.hs[firstSeg]->wayPtr))->oneway;
          
        bestd = d;
        double invSpeed = !fastest ? 1.0 : highway[
          ((wayType *)(data + itr.hs[firstSeg]->wayPtr))->type].invSpeed;
        toEndHs[i][0] = lrint (sqrt (Sqr (lon0) + Sqr (lat0)) * invSpeed);
        toEndHs[i][1] = lrint (sqrt (Sqr (lon1) + Sqr (lat1)) * invSpeed);
        if (dlon * lon1 <= -dlat * lat1) toEndHs[i][0] += toEndHs[i][1];
        else if (dlon * lon0 >= - dlat * lat0) toEndHs[i][1] += toEndHs[i][0];
        if (oneway) toEndHs[i][i ? firstSeg : 1 - firstSeg] = 200000000;
        endHs[i][0] = FirstHalfSegAtNode (itr.hs[0]);
        endHs[i][1] = FirstHalfSegAtNode (itr.hs[1]);
      }
    } /* For each candidate segment */
    if (bestd == 4000000000000000000LL) {
      fprintf (stderr, "No segment nearby\n");
      return;
    }
  } /* For 'from' and 'to', find the corresponding hs */
  free (route);
  dhashSize = (Sqr ((tlon - flon) >> 17) + Sqr ((tlat - flat) >> 17)) *
    1000 + 1000;
  if (dhashSize > 10000000) dhashSize = 10000000;
  route = (routeNodeType*) calloc (dhashSize, sizeof (*route));
  
  routeHeapSize = 1; /* Leave position 0 open to simplify the math */
  routeHeap = ((routeNodeType**) malloc (dhashSize*sizeof (*routeHeap))) - 1;
  
  for (int j = 0; j < 2; j++) AddRouteNode (endHs[0][j], toEndHs[0][j], NULL);
  for (limit = 2000000000; routeHeapSize > 1;) {
    routeNodeType *root = routeHeap[1];
    routeHeapSize--;
    int beste = Best (routeHeap[routeHeapSize]);
    for (int i = 2; ; ) {
      int besti = i < routeHeapSize ? Best (routeHeap[i]) : beste;
      int bestipp = i + 1 < routeHeapSize ? Best (routeHeap[i + 1]) : beste;
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
    if (root->best + lrint (sqrt (Sqr (root->hs->lon - tlon) +
                                  Sqr (root->hs->lat - tlat))) < limit) {
      for (int i = 0; i < 2; i++) {
        if (root->hs == endHs[1][i] && limit > root->best + toEndHs[1][i]) {
          shortest = root;
          /* if (limit == 2000000000) rebuild the heap for the new metric.
          Shouldn't be necessary */
          limit = root->best + toEndHs[1][i];
        }
      }
      halfSegType *hs = root->hs, *other;
      /* Now work through the segments connected to root. */
      do {
        other = (halfSegType *)(data + hs->other);
        int forward = hs->wayPtr != TO_HALFSEG;
        wayType *w = (wayType *)(data + (forward ? hs : other)->wayPtr);
        if (w->type < unwayed && (!forward || !w->oneway) &&
              (car ? 1 /* w->type != footway */ :
              w->type != motorway && w->type != motorway_link)) {
          int d = lrint (sqrt (Sqr ((long long)(hs->lon - other->lon)) +
                               Sqr ((long long)(hs->lat - other->lat))) *
                               (fastest ? highway[w->type].invSpeed : 1.0));
          other = FirstHalfSegAtNode (other);
          AddRouteNode (other, root->best + d, root);
        } // If we found a segment we may follow
      } while ((char*)++hs < data + hashTable[BUCKETS] &&
               hs->lon == hs[-1].lon && hs->lat == hs[-1].lat);
    } // if root->best is a candidate
  } // While there are active nodes left
  free (routeHeap + 1);
//  if (fastest) printf ("%lf
  printf ("%lf km\n", limit / 100000.0);
}

#define ZOOM_PAD_SIZE 20
#define STATUS_BAR    0

GtkWidget *draw, *car, *fastest;
int clon, clat, zoom;
/* zoom is the amount that fits into the window (regardless of window size) */

gint Click (GtkWidget *widget, GdkEventButton *event)
{
  int w = draw->allocation.width - ZOOM_PAD_SIZE;
  if (event->x > w) {
    zoom = lrint (exp (8 - 8*event->y / draw->allocation.width) * 10000);
  }
  else {
    int perpixel = zoom / w;
    if (event->button == 1) {
      clon += lrint ((event->x - w / 2) * perpixel);
      clat -= lrint ((event->y - draw->allocation.height / 2) * perpixel);
    }
    else if (event->button == 2) {
      flon = clon + lrint ((event->x - w / 2) * perpixel);
      flat = clat - lrint ((event->y - draw->allocation.height/2) * perpixel);
    }
    else {
      tlon = clon + lrint ((event->x - w / 2) * perpixel);
      tlat = clat -
        lrint ((event->y - draw->allocation.height / 2) * perpixel);
      Route (gtk_toggle_button_get_active (GTK_TOGGLE_BUTTON (car)),
             gtk_toggle_button_get_active (GTK_TOGGLE_BUTTON (fastest)));
      printf ("%d\n", gtk_toggle_button_get_active (GTK_TOGGLE_BUTTON (car)));
    }
  }
  gtk_widget_queue_clear (draw);
}

#define obstack_chunk_alloc malloc
#define obstack_chunk_free free

void GetDirections (GtkWidget *, gpointer)
{
  struct obstack o;
  obstack_init (&o);
  if (!shortest) obstack_printf (&o,
    "Mark the starting point with the middle button and the\n"
    "end point with the right button. Then click Get Directions again\n");
  else {
    for (routeNodeType *x = shortest; x; x = x->shortest) {
    }
  }
  obstack_1grow (&o, '\0');
  GtkWidget *window = gtk_window_new (GTK_WINDOW_TOPLEVEL);
  GtkWidget *view = gtk_text_view_new ();
  GtkTextBuffer *buffer = gtk_text_view_get_buffer (GTK_TEXT_VIEW (view));
  gtk_text_buffer_set_text (buffer, (char *) obstack_finish (&o), -1);
  obstack_free (&o, NULL);
  gtk_container_add (GTK_CONTAINER (window), view);
  gtk_widget_show (view);
  gtk_widget_show (window);
}

gint Scroll (GtkWidget *widget, GdkEventScroll *event, void *w_current)
{
   switch (event->direction) {
   case(GDK_SCROLL_UP):
           zoom = zoom*3/4;
           break;
   case(GDK_SCROLL_DOWN):
           zoom = zoom*4/3;
           break;
   }
   gtk_widget_queue_clear (draw);
}

struct name2renderType { // Build a list of names, sort by name,
  wayType *w;            // make unique by name, sort by y, then render
  int x, y, width;       // only if their y's does not overlap
};

int Name2RenderNameCmp (const void *a, const void *b)
{
  return strcmp (((name2renderType *)a)->w->name + data,
    ((name2renderType *)b)->w->name + data);
}

int Name2RenderYCmp (const void *a, const void *b)
{
  return ((name2renderType *)a)->y - ((name2renderType *)b)->y;
}

gint Expose (void)
{
  GdkColor highwayColour[sizeof (highway) / sizeof (highway[0])];
  for (int i = 0; i < sizeof (highway) / sizeof (highway[0]); i++) {
    gdk_color_parse (highway[i].colour, &highwayColour[i]);
    gdk_colormap_alloc_color (gdk_window_get_colormap (draw->window),
      &highwayColour[i], FALSE, TRUE); /* Possibly only at startup ? */
  }

  GdkRectangle clip;
  clip.x = 0;
  clip.y = 0;
  clip.height = draw->allocation.height - STATUS_BAR;
  clip.width = draw->allocation.width;
  gdk_gc_set_clip_rectangle (draw->style->fg_gc[0], &clip);
  gdk_gc_set_foreground (draw->style->fg_gc[0], &highwayColour[0]);
  gdk_draw_line (draw->window, draw->style->fg_gc[0],
    clip.width - ZOOM_PAD_SIZE / 2, 0,
    clip.width - ZOOM_PAD_SIZE / 2, clip.height); // Visual queue for zoom bar
  gdk_draw_line (draw->window, draw->style->fg_gc[0],
    clip.width - ZOOM_PAD_SIZE, clip.height - ZOOM_PAD_SIZE / 2,
    clip.width - ZOOM_PAD_SIZE / 2, clip.height); // Visual queue for zoom bar
  gdk_draw_line (draw->window, draw->style->fg_gc[0],
    clip.width, clip.height - ZOOM_PAD_SIZE / 2,
    clip.width - ZOOM_PAD_SIZE / 2, clip.height); // Visual queue for zoom bar
  gdk_draw_line (draw->window, draw->style->fg_gc[0],
    clip.width - ZOOM_PAD_SIZE, ZOOM_PAD_SIZE / 2,
    clip.width - ZOOM_PAD_SIZE / 2, 0); // Visual queue for zoom bar
  gdk_draw_line (draw->window, draw->style->fg_gc[0],
    clip.width, ZOOM_PAD_SIZE / 2,
    clip.width - ZOOM_PAD_SIZE / 2, 0); // Visual queue for zoom bar
    
  clip.width = draw->allocation.width - ZOOM_PAD_SIZE;
  gdk_gc_set_clip_rectangle (draw->style->fg_gc[0], &clip);
  
  GdkFont *f = gtk_style_get_font (draw->style);
  name2renderType name[3000];
  int perpixel = zoom / clip.width, nameCnt = 0;
//    zoom / sqrt (draw->allocation.width * draw->allocation.height);
  for (int thisLayer = -5, nextLayer; thisLayer < 6; thisLayer = nextLayer) {
    OsmItr itr (clon - perpixel * clip.width, clat - perpixel * clip.height,
      clon + perpixel * clip.width, clat + perpixel * clip.height);
    // Widen this a bit so that we render nodes that are just a bit offscreen ?
    nextLayer = 6;
    while (Next (itr)) {
      wayType *w = (wayType *)(data + (itr.hs[0]->wayPtr != TO_HALFSEG ?
        itr.hs[0]->wayPtr : itr.hs[1]->wayPtr));
      if (thisLayer < w->layer && w->layer < nextLayer) nextLayer = w->layer;
      if (w->layer != thisLayer) continue;
      if (nameCnt < sizeof (name) / sizeof (name[0]) &&
            data[w->name] != '\0') {
        gint lbearing, rbearing, width, ascent, descent;
        gdk_string_extents (f, w->name + data, &lbearing, &rbearing, &width,
          &ascent, &descent);
        
        name[nameCnt].width = itr.hs[1]->lon - itr.hs[0]->lon;
        if (name[nameCnt].width < 0) name[nameCnt].width *= -1; /* abs () */
        name[nameCnt].x = (itr.hs[0]->lon / 2 + itr.hs[1]->lon / 2 - clon) /
          perpixel + clip.width / 2 - width / 2;
        name[nameCnt].y = f->descent + clip.height / 2 -
          (itr.hs[0]->lat / 2 + itr.hs[1]->lat / 2 - clat) / perpixel;
        if (-f->ascent < name[nameCnt].y &&
            name[nameCnt].y < clip.height + f->descent &&
            -width / 2 < name[nameCnt].x &&
            name[nameCnt].x < clip.width + width / 2) name[nameCnt++].w = w;
      }
      
      if (w->type <= unwayed || w->type > place) {
        gdk_gc_set_foreground (draw->style->fg_gc[0],
          &highwayColour[w->type]);
        gdk_gc_set_line_attributes (draw->style->fg_gc[0],
          highway[w->type].width, highway[w->type].style, GDK_CAP_PROJECTING,
          GDK_JOIN_MITER);
        gdk_draw_line (draw->window, draw->style->fg_gc[0],
          (itr.hs[0]->lon - clon) / perpixel + clip.width / 2,
          clip.height / 2 - (itr.hs[0]->lat - clat) / perpixel,
          (itr.hs[1]->lon - clon) / perpixel + clip.width / 2,
          clip.height / 2 - (itr.hs[1]->lat - clat) / perpixel);
      }
    } /* for each visible tile */
  }
  qsort (name, nameCnt, sizeof (name[0]), Name2RenderNameCmp);
  for (int i = 1, deleted = 0; i < nameCnt; ) {
    memcpy (name + i, name + i + deleted, sizeof (name[0]));
    // I guess memcpy will always work (do nothing) if deleted == 0
    if (i && !strcmp (name[i - 1].w->name + data, name[i].w->name + data)) {
      if (name[i - 1].width < name[i].width) 
        memcpy (name + i - 1, name + i + deleted, sizeof (name[0]));
      deleted++; // Keep only coordinates with larger 'width'
      nameCnt--;
    }
    else i++;
  }
  qsort (name, nameCnt, sizeof (name[0]), Name2RenderYCmp);
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
  
  gdk_gc_set_foreground (draw->style->fg_gc[0], &highwayColour[0]);
  gdk_gc_set_line_attributes (draw->style->fg_gc[0],
    1, GDK_LINE_SOLID, GDK_CAP_PROJECTING, GDK_JOIN_MITER);
  if (shortest) {
    routeNodeType *x = shortest;
    gdk_draw_line (draw->window, draw->style->fg_gc[0],
      (flon - clon) / perpixel + clip.width / 2,
      clip.height / 2 - (flat - clat) / perpixel,
      (shortest->hs->lon - clon) / perpixel + clip.width / 2,
      clip.height / 2 - (shortest->hs->lat - clat) / perpixel);
    for (; x->shortest; x = x->shortest) {
      gdk_draw_line (draw->window, draw->style->fg_gc[0],
        (x->hs->lon - clon) / perpixel + clip.width / 2,
        clip.height / 2 - (x->hs->lat - clat) / perpixel,
        (x->shortest->hs->lon - clon) / perpixel + clip.width / 2,
        clip.height / 2 - (x->shortest->hs->lat - clat) / perpixel);
    }
    gdk_draw_line (draw->window, draw->style->fg_gc[0],
      (x->hs->lon - clon) / perpixel + clip.width / 2,
      clip.height / 2 - (x->hs->lat - clat) / perpixel,
      (tlon - clon) / perpixel + clip.width / 2,
      clip.height / 2 - (tlat - clat) / perpixel);
  }
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
wayType *incrementalWay;
#define wayArray ((wayType *)data)
#define wayCount (wayArray[0].name / sizeof (wayArray[0]))

gint IncrementalSearch (void)
{
  const gchar *key = gtk_entry_get_text (GTK_ENTRY (search));
  int i, l = 0, h = wayCount;
  while (l < h) {
    if (strcmp (data + wayArray[(h + l) / 2].name, key) >= 0) h = (h + l) / 2;
    else l = (h + l) / 2 + 1;
  }
  incrementalWay = wayArray + l;
  gtk_clist_freeze (GTK_CLIST (list));
  gtk_clist_clear (GTK_CLIST (list));
  for (i = 0; i < 40 && i + l < wayCount; i++) {
    char *name = data + incrementalWay[i].name;
    gtk_clist_append (GTK_CLIST (list), &name);
  }
  gtk_clist_thaw (GTK_CLIST (list));
}

void SelectName (GtkWidget *w, gint row, gint column, GdkEventButton *ev,
  gpointer data)
{
  clon = incrementalWay[row].clon;
  clat = incrementalWay[row].clat;
  zoom = incrementalWay[row].zoom16384 + 2 << 14;
  gtk_widget_queue_clear (draw);
}

int main (int argc, char *argv[])
{
  FILE *pak;

  if (argc > 1) {
    if (argc > 2 || stricmp (argv[1], "rebuild")) {
      fprintf (stderr, "Usage : %s [rebuild]\n", argv[0]);
      return 1;
    }
    if (!(pak = fopen ("gosmore.pak", "w+"))) {
      fprintf (stderr, "Cannot create gosmore.pak\n");
      return 2;
    }
    // Set up something for unwayed segments to point to. This will be
    // written to location 0 when we encounter the first <way> tag.
  
    char tag[301], key[301], value[301], quote, feature[301];
    int wayCnt = 0, from, nodeCnt = 0, halfSegCnt = 0;
    enum { doNodes, doSegments, doWays } mode = doNodes;
    int wleft, wright, wtop, wbottom;

    printf ("Reading nodes...\n");
    nodeType *node = (nodeType *) malloc (sizeof (*node) * 15000000);
    halfSegType *halfSeg = (halfSegType *) malloc (sizeof (*halfSeg) *
      30000000); /* (Number of segments + nodes with names) * 2 */
/* This array is sorted twice : First time 'other' is the segment id so
   we add the two halfs together and we sort by id.
   Second time 'other' is the index into the array before it is sorted. Then
   we sort by bucket number, lon and lat. Then we set 'other'
   to the offset of the other half in the sorted pak file. */
    wayBuildType *w = (wayBuildType *) calloc (sizeof (*w), 2000000);
    w[wayCnt].idx = wayCnt;
    w[wayCnt].w.type = sizeof (highway) / sizeof (highway[0]) - 1;
    w[wayCnt].w.layer = 5; // 5 means show duplicated segments clearly.
    w[wayCnt].name = strdup ("_unwayed");
   
    while (scanf (" <%300[a-zA-Z0-9?/]", tag) == 1) {
      //printf ("%s", tag);
      do {
        while (scanf (" %300[a-zA-Z0-9]", key)) {
          if (getchar () == '=') {
            quote = getchar ();
            if (quote == '\'') scanf ("%300[^']'", value); /* " */
            else if (quote == '"') scanf ("%300[^\"]\"", value); /* " */
            else {
              ungetc (quote, stdin);
              scanf ("%300[^ ]", value); /* " */
            }
            //printf (" %s='%s'", key, value);
            if (mode == doNodes && !stricmp (tag, "node")) {
              if (!stricmp (key, "id")) node[nodeCnt].id = atoi (value);
              if (!stricmp (key, "lat")) {
                node[nodeCnt].lat = Latitude (atof (value));
              }
              if (!stricmp (key, "lon")) {
                node[nodeCnt++].lon = Longitude (atof (value));
                
                if (w[wayCnt].name) halfSegCnt = wayCnt++ * 2;
                /* Now there is a way for the unwayed segment plus
                   wayCnt - 1 nodes with names, each with 2 half segments */
                w[wayCnt].w.type = sizeof (highway) / sizeof (highway[0]) - 2;
                w[wayCnt].idx = wayCnt;
                w[wayCnt].w.clat = node[nodeCnt - 1].lat;
                w[wayCnt].w.clon = node[nodeCnt - 1].lon;
                w[wayCnt].w.zoom16384 = 10;
                /* We prepare a way and two segments in case this node has
                   a name. */
                halfSeg[halfSegCnt].other = -1; /* Unused segment id */
                halfSeg[halfSegCnt].lat = w[wayCnt].w.clat;
                halfSeg[halfSegCnt].lon = w[wayCnt].w.clon;
                halfSeg[halfSegCnt].wayPtr = wayCnt;
                memcpy (halfSeg + halfSegCnt + 1, halfSeg + halfSegCnt,
                  sizeof (halfSeg[0]));
                
                if (nodeCnt % 100000 == 0) printf ("%9d nodes\n", nodeCnt);
              }
            }
            else if (mode <= doSegments && !stricmp (tag, "segment")) {
              if (mode < doSegments) {
                printf ("Sorting nodes...\n");
                quicksort (node, nodeCnt, sizeof (node[0]), NodeIdCmp);
                mode = doSegments;
              }
              if (!stricmp (key, "id")) {
                if (w[wayCnt].name) halfSegCnt = wayCnt++ * 2;
                halfSeg[halfSegCnt].other = atoi (value);
              }
              if (!stricmp (key, "from")) {
                from = atoi (value);
                if (halfSegCnt % 200000 == 0) {
                  printf ("%9d segments\n", halfSegCnt / 2);
                }
              }
              if (!stricmp (key, "to")) {
                nodeType key, *n;
                for (int i = 0; i < 2; i++) {
                  key.id = i ? atoi (value) : from;
                  n = (nodeType *) bsearch (&key, node, nodeCnt,
                    sizeof (key), NodeIdCmp);
                  if (!n) break;
                  halfSeg[i + halfSegCnt].lon = n->lon;
                  halfSeg[i + halfSegCnt].lat = n->lat;
                }
                if (n) {
                  halfSeg[halfSegCnt++].wayPtr = 0; /* unwayed */
                  halfSeg[halfSegCnt++].wayPtr = TO_HALFSEG;
                }
              }
            }
            else if (!stricmp (tag, "way")) {
              if (mode < doWays) {
                printf ("Sorting segments\n");
                quicksort (halfSeg, halfSegCnt / 2, sizeof (*halfSeg) * 2,
                  HalfSegIdCmp);
                mode = doWays;
                printf ("Creating ways\n");
              }
              else {
                if (!w[wayCnt].name) w[wayCnt].name = strdup ("");
                wayCnt++; // Flush way built in previous iteration
              }
              
              w[wayCnt].idx = wayCnt;
              w[wayCnt].w.type = sizeof (highway) / sizeof (highway[0]) - 1;
              wleft = INT_MAX;
              wright = -INT_MAX;
              wbottom = INT_MAX;
              wtop = -INT_MAX;
              if (wayCnt % 100000 == 0) printf ("%9d ways\n", wayCnt);
            }
            else if (mode == doWays && !stricmp (tag, "seg") &&
                !stricmp (key, "id")) {
              halfSegType key[2], *hs;
              key[0].other = atoi (value);
              if ((hs = (halfSegType *) bsearch (key, halfSeg, halfSegCnt / 2,
                      sizeof (*halfSeg) * 2, HalfSegIdCmp)) != NULL) {
                hs->wayPtr = wayCnt;
                for (int i = 0; i < 2; i++) {
                  if (wleft > hs[i].lon) wleft = hs[i].lon;
                  else if (wright < hs[i].lon) wright = hs[i].lon;
                  if (wbottom > hs[i].lat) wbottom = hs[i].lat;
                  else if (wtop < hs[i].lat) wtop = hs[i].lat;
                }
                w[wayCnt].w.clon = wleft / 2 + wright / 2; /* eager evaluat */
                w[wayCnt].w.clat = wtop / 2 + wbottom / 2;
                w[wayCnt].w.zoom16384 = (wright - wleft > wtop - wbottom ?
                  wright - wleft : wtop - wbottom) >> 14;
              }
            }
            else if (!stricmp (tag, "tag") /* && mode != doSegments but
            then we will have hundreds of complains of tagged segments */) {
              if (!stricmp (key, "k")) strcpy (feature, value);
              if (mode != doSegments && !stricmp (key, "v")) {
                if (!stricmp (feature, "oneway") &&
                  tolower (value[0]) == 'y') w[wayCnt].w.oneway = 1;
                else if (!strcmp (feature, "layer"))
                  w[wayCnt].w.layer = atoi (value);
                else if (!strcmp (feature, "name"))
                  w[wayCnt].name = strdup (value);
                //else if (!strcmp (feature, "ref")) strcpy (ref, value);
                else for (int i = 0; highway[i].name; i++) {
		  if (!stricmp (highway[i].feature, feature) &&
                      !stricmp (value, highway[i].name)) w[wayCnt].w.type = i;
                }
              }
            }
            else if (strcmp (tag, "?xml") && strcmp (tag, "osm"))
              fprintf (stderr, "Unexpected tag %s\n", tag);
          } /* if key / value pair found */
        } /* while search for key / value pairs */
      } while (getchar () != '>');
      //printf ("\n");
    } /* while we found another tag */
    if (mode == doWays) {
      if (!w[wayCnt].name) w[wayCnt].name = strdup ("");
      wayCnt++; // Flush way built above
    }
    free (node);
    printf ("Sorting ways by name\n");
    qsort (w, wayCnt, sizeof (*w), WayBuildCmp);
    int *wIdx = (int *) malloc (sizeof (*wIdx) * wayCnt);
    printf ("Writing ways\n");
    for (int i = 0, strPtr = wayCnt * sizeof (w[0].w); i < wayCnt; i++) {
      w[i].w.name = strPtr;
      fwrite (&w[i].w, sizeof (w[i].w), 1, pak);
      strPtr += strlen (w[i].name) + 1;
      wIdx[w[i].idx] = i * sizeof (w[0].w); // = ftell (pak)
    }
    for (int i = 0; i < wayCnt; i++) {
      fwrite (w[i].name, strlen (w[i].name) + 1, 1, pak);
      free (w[i].name);
    }
    free (w);
    printf ("Preparing for sorting half segments\n");
    for (int i = 0; i < halfSegCnt; i++) halfSeg[i].other = i;
    printf ("Sorting\n");
    quicksort (halfSeg, halfSegCnt, sizeof (*halfSeg),
	       (int (*)(const void*, const void*)) HalfSegCmp);
	       
    printf ("Calculating addresses\n");
    int *hsIdx = (int *) malloc (sizeof (*hsIdx) * halfSegCnt);
    int hsBase = ftell (pak);
    for (int i = halfSegCnt - 1; i >= 0; i--)
	       hsIdx[halfSeg[i].other] = hsBase + i * sizeof (*halfSeg);
    printf ("Writing Data\n");
    for (int i = 0; i < halfSegCnt; i++) {
      halfSeg[i].other = hsIdx[halfSeg[i].other ^ 1];
      halfSeg[i].wayPtr = halfSeg[i].wayPtr == TO_HALFSEG ? TO_HALFSEG :
        wIdx[halfSeg[i].wayPtr]; // Final pos of way.
      fwrite (&halfSeg[i], sizeof (*halfSeg), 1, pak);
    }
    printf ("Writing hash table\n");
    fwrite (&hsBase, sizeof (hsBase), 1, pak);
    for (int bucket = 0, i = 0; bucket < BUCKETS; bucket++) {
      while (i < halfSegCnt && Hash (halfSeg[i].lon,
				     halfSeg[i].lat) == bucket) {
        i++;
        hsBase += sizeof (*halfSeg);
      }
      fwrite (&hsBase, sizeof (hsBase), 1, pak);
    }
    fclose (pak); /* fflush instead ? */
    free (wIdx);
    free (halfSeg);
    free (hsIdx);
    /* It has BUCKETS + 1 entries so that we can easily look up where each
       bucket begins and ends */
  } /* if rebuilding */
  if (!(pak = fopen ("gosmore.pak", "r"))) {
    fprintf (stderr, "Cannot read gosmore.pak\nYou can (re)build it from\n"
      "the planet file e.g. bzip2 -d planet-...osm.bz2 | %s rebuild\n",
      argv[0]);
    return 3;
  }
  fseek (pak, 0, SEEK_END);
  data = (char*)
    mmap (0, ftell (pak), PROT_READ, MAP_SHARED, fileno (pak), 0);
  hashTable = (int *) (data + ftell (pak)) - BUCKETS - 1;

  wayType *w = 0 + (wayType *) data;
  //printf ("%d ways %d\n", w[0].name / sizeof (w[0]), w[w[0].name / sizeof (w[0]) - 1].name);
  clon = Longitude (28.30803);
  clat = Latitude (-25.78569);
  zoom = (w[0].zoom16384 + 3) << 14;
    //lrint (0.1 / 180 * 2147483648.0 * cos (26.1 / 180 * M_PI));

  gtk_init (&argc, &argv);
  draw = gtk_drawing_area_new ();
  gtk_signal_connect (GTK_OBJECT (draw), "expose_event",
    (GtkSignalFunc) Expose, NULL);
  gtk_signal_connect (GTK_OBJECT (draw), "button_press_event",
    (GtkSignalFunc) Click, NULL);
  gtk_widget_set_events (draw, GDK_EXPOSURE_MASK | GDK_BUTTON_PRESS_MASK);
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
    
  car = gtk_radio_button_new_with_label (NULL, "car");
  gtk_box_pack_start (GTK_BOX (vbox), car, FALSE, FALSE, 5);
  GtkWidget *bike = gtk_radio_button_new_with_label (
    gtk_radio_button_get_group (GTK_RADIO_BUTTON (car)), "bike");
  gtk_box_pack_start (GTK_BOX (vbox), bike, FALSE, FALSE, 5);

  fastest = gtk_radio_button_new_with_label (NULL, "fastest");
  gtk_box_pack_start (GTK_BOX (vbox), fastest, FALSE, FALSE, 5);
  GtkWidget *shortestRB = gtk_radio_button_new_with_label (
    gtk_radio_button_get_group (GTK_RADIO_BUTTON (fastest)), "shortest");
  gtk_box_pack_start (GTK_BOX (vbox), shortestRB, FALSE, FALSE, 5);
  
  GtkWidget *getDirs = gtk_button_new_with_label ("Get Directions");
  gtk_box_pack_start (GTK_BOX (vbox), getDirs, FALSE, FALSE, 5);
  gtk_signal_connect (GTK_OBJECT (getDirs), "clicked",
    GTK_SIGNAL_FUNC (GetDirections), NULL);

  gtk_signal_connect (GTK_OBJECT (window), "delete_event",
    GTK_SIGNAL_FUNC (gtk_main_quit), NULL);
  gtk_widget_set_usize (window, 400, 300);
  gtk_widget_show (search);
  gtk_widget_show (list);
  gtk_widget_show (draw);
  gtk_widget_show (car);
  gtk_widget_show (bike);
  gtk_widget_show (fastest);
  gtk_widget_show (shortestRB);
  gtk_widget_show (getDirs);
  gtk_widget_show (hbox);
  gtk_widget_show (vbox);
  gtk_widget_show (window);
  IncrementalSearch ();
  gtk_main ();
}
