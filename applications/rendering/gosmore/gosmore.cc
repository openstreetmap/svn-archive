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
//#include <gtk/gtklist.h>

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
  GdkLineStyle style;
} highway[] = {
  /* ways */
  { "railway", "rail"         , "black",  3, GDK_LINE_ON_OFF_DASH },
  { "highway", "residential"  , "white",  1, GDK_LINE_SOLID },
  { "highway", "motorway"     , "blue",   3, GDK_LINE_SOLID },
  { "highway", "motorway_link", "blue",   3, GDK_LINE_SOLID },
  { "highway", "trunk"        , "green",  3, GDK_LINE_SOLID },
  { "highway", "primary"      , "red",    2, GDK_LINE_SOLID },
  { "highway", "secondary"    , "orange", 2, GDK_LINE_SOLID },
  { "highway", "tertiary"     , "yellow", 1, GDK_LINE_SOLID },
  { "highway", "track"        , "brown",  1, GDK_LINE_SOLID },
//  { "highway", "footway"        , "brown",  1, GDK_LINE_SOLID },
  /* nodes : */
  { "railway", "station"      , "red",    1, GDK_LINE_SOLID },
  { "place",   "suburb"       , "black",  2, GDK_LINE_SOLID },
  { "place",   "junction"     , "black",  1, GDK_LINE_SOLID },
  { "place",   "halmet"       , "black",  1, GDK_LINE_SOLID },
  { "place",   "village"      , "black",  1, GDK_LINE_SOLID },
  { "place",   "town"         , "black",  2, GDK_LINE_SOLID },
  { "place",   "city"         , "black",  3, GDK_LINE_SOLID },
  { NULL, NULL /* named node of unidentified type */  , "gray",   1,
    GDK_LINE_SOLID },
  { NULL, NULL /* unwayed */  , "gray",   1, GDK_LINE_SOLID }
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
{ /* Buildin qsort performs badly when dataset does not fit into memory,
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

struct routeNodeType {
  halfSegType *hs;
  routeNodeType *shortest;
  int active, best;
} route[50000], *shortest = NULL;

#define Sqr(x) ((x)*(x))
void Route (int flon, int flat, int tlon, int tlat)
{ /* We start by finding the segment that is closest to 'from' and 'to' */
  int toToNode[2];
  int routeCnt = 0, car = 1;
  route[2].shortest = NULL;
  route[3].shortest = NULL;
  
  for (int i = 0; i < 2; i++) {
    int lon = i ? flon : tlon, lat = i ? flat : tlat;
    long long bestd = 4000000000000000000LL;
    /* find min (Sqr (distance)). Use long long so we don't loose accuracy */
    OsmItr itr (lon - 300000, lat - 300000, lon + 300000, lat + 300000);
    /* Search 1km x 1km around 'from' for the nearest segment to it */
    while (Next (itr)) {
      int firstSeg = itr.hs[0]->wayPtr == TO_HALFSEG, oneway =
        ((wayType *)(data + itr.hs[firstSeg]->wayPtr))->oneway;
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
        bestd = d;
        for (int j = 0; j < 2; j++) {
          route[i * 2 + j].best = 2000000000;
          route[i * 2 + j].hs = FirstHalfSegAtNode (itr.hs[j]);
          route[i * 2 + j].active = i;
        }
        if (!i) {
          toToNode[0] = lrint (sqrt (Sqr (lon0) + Sqr (lat0)));
          toToNode[1] = lrint (sqrt (Sqr (lon1) + Sqr (lat1)));
          if (dlon * lon1 <= -dlat * lat1) toToNode[0] = 100000000;
          else if (dlon * lon0 >= - dlat * lat0) toToNode[1] = 100000000;
          if (oneway) toToNode[firstSeg] = 200000000;
        }
        else {
          route[2].best = lrint (sqrt (Sqr (lon0) + Sqr (lat0)));
          route[3].best = lrint (sqrt (Sqr (lon1) + Sqr (lat1)));
          if (dlon * lon1 <= -dlat * lat1) route[2].best += route[3].best;
          else if (dlon * lon0 >= - dlat*lat0) route[3].best += route[2].best;
          if (oneway) route[2 + firstSeg].best = 200000000;
        }
      }
    } /* For each candidate segment */
    if (bestd == 4000000000000000000LL) {
      fprintf (stderr, "No segment nearby\n");
      return;
    }
  } /* For 'from' and 'to' */
  routeCnt = 4; // 2 'from' nodes and 2 'to' nodes
  for (int limit = 2000000000;;) {
    int n;
    if (limit == 2000000000) {
      /* First step is find a route with our heurisitc : Find the node n such
         that d(n,from) + n->best is smallest. */
      int bestd = 2000000000, d;
      for (int i = 0; i < routeCnt; i++) {
        if (route[i].active && bestd > (d = route[i].best + lrint (sqrt (
        Sqr (route[i].hs->lon - tlon) + Sqr (route[i].hs->lat - tlat))))) {
          bestd = d;
          n = i;
        }
      }
      if (bestd == 2000000000) {
        fprintf (stderr, "Route does not exist\n");
        return;
      }
    }
    else {
      for (n = 0; n < routeCnt && !route[n].active; n++) {}
      if (n >= routeCnt) break; // No active nodes left, so we're done
        // Figure out which nodes belong to the shortest route
      if (route[n].best + Sqr (route[n].hs->lon - tlon) +
                          Sqr (route[n].hs->lat - tlat) > limit) {
        route[n].active = 0;
        continue; // Over the limit. No evaluation necessary 
      }
    }
    /* Then work through the segments connected to n. Repeat. */
    halfSegType *hs = route[n].hs, *other;
    do {
      other = (halfSegType *)(data + hs->other);
      int forward = hs->wayPtr != TO_HALFSEG;
      wayType *w = (wayType *)(data + (forward ? hs : other)->wayPtr);
      if (w->type < unwayed && (forward || !w->oneway) &&
            (car ? 1 /* w->type != footway */ :
            w->type != motorway && w->type != motorway_link)) {
        int d = route[n].best + lrint (sqrt (
          Sqr ((long long)(hs->lon - other->lon)) +
          Sqr ((long long)(hs->lat - other->lat))));
        other = FirstHalfSegAtNode (other);
        int i;
        for (i = 0; i < routeCnt && route[i].hs != other; i++) {}
        if (i >= sizeof (route) / sizeof (route[0])) {
          fprintf (stderr, "To many nodes\n");
          /* route[0].shortest = NULL; Perhaps we found a road... */
          return;
        }
        if (i >= routeCnt || route[i].best > d) {
          if (i >= routeCnt) {
            routeCnt++;
            route[i].hs = other;
          }
          route[i].best = d;
          route[i].shortest = route + n;
          route[i].active = 1;
          if (i < 2 && limit > d + toToNode[i]) {
            limit = d + toToNode[i];
            shortest = route + i;
          }
        }
      } // If we found a segment we may follow
    } while ((char*)++hs < data + hashTable[BUCKETS] &&
             hs->lon == hs[-1].lon && hs->lat == hs[-1].lat);
    route[n].active = 0;
  } // forever : while we're searching for the shortest route
}

#define ZOOM_PAD_SIZE 20
#define STATUS_BAR    0

GtkWidget *draw;
int clon, clat, zoom;
/* zoom is the amount that fits into the window (regardless of window size) */

gint Click (GtkWidget *widget, GdkEventButton *event)
{
  static int flat = 0, flon = 0;
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
      Route (flon, flat,
        clon + lrint ((event->x - w / 2) * perpixel),
        clat - lrint ((event->y - draw->allocation.height / 2) * perpixel));
    }
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
  for (routeNodeType *x = shortest; x && x->shortest; ) {
    gdk_draw_line (draw->window, draw->style->fg_gc[0],
      (x->hs->lon - clon) / perpixel + clip.width / 2,
      clip.height / 2 - (x->hs->lat - clat) / perpixel,
      (x->shortest->hs->lon - clon) / perpixel + clip.width / 2,
      clip.height / 2 - (x->shortest->hs->lat - clat) / perpixel);
    x = x->shortest;
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
  for (i = 0; i < 60 && i + l < wayCount; i++) {
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

/*  Route (Longitude (28.29302), Latitude (-25.77761),
         Longitude (27.9519540115356), Latitude (-26.0750263440503));
*/
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

  gtk_signal_connect (GTK_OBJECT (window), "delete_event",
    GTK_SIGNAL_FUNC (gtk_main_quit), NULL);
  gtk_widget_set_usize (window, 400, 300);
  gtk_widget_show (hbox);
  gtk_widget_show (vbox);
  gtk_widget_show (search);
  gtk_widget_show (list);
  gtk_widget_show (draw);
  gtk_widget_show (window);
  IncrementalSearch ();
  gtk_main ();
}
