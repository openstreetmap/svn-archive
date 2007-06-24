/* gosmore - European weed widely naturalized in North America having yellow
   flower heads and leaves resembling a cat's ears */
   
/* This software is placed by in the public domain by its author, Nic Roets */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <sys/mman.h>
#include <gtk/gtk.h>
//#include <gdk/gdkx.h>

#define stricmp strcasecmp

#define BUCKETS (1<<22) /* Must be power of 2 */
#define TILEEXP (18)
#define TILESIZE (1<<TILEEXP)

int Hash (int lon, int lat)
{ /* This is a universal hashfuntion in GF(2^31-1). The hexadecimal numbers */
  /* are from random.org, but experimentation will surely yield better */
  /* numbers. The more we right shift lon and lat, the larger */
  /* each tile will be. We can add constants to the lat and lon variables */
  /* to make them positive, but the clashes will still occur between the */
  /* same tiles. */

  /* Mercator projection means tiles are not the same physical size. */
  /* Compensating for this very low on the agenda vs. e.g. compensating for */
  /* high node density in Western Europe. */
  long long v = ((lon >> TILEEXP) /* + (1<<19) */) * (long long) 0x00d20381 +
    ((lat >> TILEEXP) /*+ (1<<19)*/) * (long long) 0x75d087d9;
  while (v >> 31) v = (v & ((1<<31) - 1)) + (v >> 31);
  /* Replace loop with v = v % ((1<<31)-1) ? */
  return v & (BUCKETS - 1);
} /* This mask means the last bucket is very, very slightly under used. */

struct nodeType {
  int id, lon, lat;
} *node;

#define TO_HALFSEG -1

struct halfSegType {
  int lon, lat, other, wayPtr;
} *halfSeg;
/* This array is sorted twice : First time 'other' is the segment id so
   we add the two halfs together and we sort by id.
   Second time we sort by bucket number, lon and lat. Then we set 'other'
   to the offset of the other half. */
   
struct wayType {
  int type;
} way;

struct highwayType {
  char *name, *colour;
} highway[] = {
  { "residential", "white" },
  { "motorway", "blue" },
  { "motorway_link", "blue" },
  { "trunk", "green" },
  { "primary", "red" },
  { "secondary", "orange" },
  { "tertiary", "yellow" },
  { "track", "brown" },
  { NULL /* everything else except*/, "black" },
  { NULL /* unwayed */, "gray" }
};
GdkColor highwayColour[sizeof (highway) / sizeof (highway[0])];

int nodeCnt = 0, halfSegCnt = 0;

int NodeIdCmp (const void *a, const void *b)
{
  return ((nodeType*)a)->id - ((nodeType*)b)->id;
}

int HalfSegIdCmp (const void *a, const void *b)
{
  return ((halfSegType*)a)->other - ((halfSegType *)b)->other;
}

int HalfSegCmp (const void *aidx, const void *bidx)
{
  halfSegType *a = halfSeg + *(int *) aidx, *b = halfSeg + *(int *) bidx;
  int hasha = Hash (a->lon, a->lat), hashb = Hash (b->lon, b->lat);
  return hasha != hashb ? hasha - hashb : a->lon != b->lon ? a->lon - b->lon :
    a->lat - b->lat;
}

int *hashTable;
FILE *pak;

void WriteSeg (int from, int to)
{
  nodeType key, *n;
  for (int i = 0; i < 2; i++) {
    key.id = i ? to : from;
    n = (nodeType *) bsearch (&key, node, nodeCnt, sizeof (key), NodeIdCmp);
    if (!n) return;
    halfSeg[i + halfSegCnt].lon = n->lon;
    halfSeg[i + halfSegCnt].lat = n->lat;
  }
  halfSeg[halfSegCnt++].wayPtr = 0;
  halfSeg[halfSegCnt++].wayPtr = TO_HALFSEG;
}

GtkWidget *draw;
char *data;
int clon, clat, zoom;
/* zoom is the amount that fits into the window (regardless of window size) */

/* Mercator projection onto a square means we have to clip
   everything beyond N85.05 and S85.05 */
int Latitude (double lat)
{
  return lat > 85.051128779 ? 2147483647 : lat < -85.051128779 ? -2147483647 :
    lrint (log (tan (M_PI / 4 + lat * M_PI / 360)) / M_PI * 2147483648.0);
}

int Longitude (double lon)
{
  return lrint (lon / 180 * 2147483648.0);
}

#define ZOOM_PAD_SIZE 20

gint Click (GtkWidget *widget, GdkEventButton *event)
{
  if (event->x > draw->allocation.width - ZOOM_PAD_SIZE) {
    zoom = lrint (exp (6 - 6*event->y / draw->allocation.width) * 30000);
  }
  else {
    int perpixel = zoom / draw->allocation.width;
    clon += lrint ((event->x - draw->allocation.width / 2) * perpixel);
    clat -= lrint ((event->y - draw->allocation.height / 2) * perpixel);
  }
  gtk_widget_queue_clear (draw);
}

gint Expose (void)
{
  int perpixel = zoom / draw->allocation.width;
//    zoom / sqrt (draw->allocation.width * draw->allocation.height);
  int width = draw->allocation.width - ZOOM_PAD_SIZE;
  int left = (clon - perpixel * width) & (~(TILESIZE-1));
  int right = (clon + perpixel * width + TILESIZE - 1) & (~(TILESIZE-1));
  int top = (clat - perpixel * draw->allocation.height) & (~(TILESIZE-1));
  int bottom = (clat + perpixel * draw->allocation.height + TILESIZE - 1) &
    (~(TILESIZE-1));
  // Widen this a bit so that we render nodes that are just a bit offscreen ?
    
  GdkRectangle clip;
  clip.x = 0;
  clip.y = 0;
  clip.width = width;
  clip.height = draw->allocation.height;
  gdk_gc_set_clip_rectangle (draw->style->fg_gc[0], &clip);
  
  for (int i = 0; i < sizeof (highway) / sizeof (highway[0]); i++) {
    gdk_color_parse (highway[i].colour, &highwayColour[i]);
    gdk_colormap_alloc_color (gdk_window_get_colormap (draw->window),
      &highwayColour[i], FALSE, TRUE); /* Possibly only at startup ? */
  }
  /* Here we wrap around from N85 to S85 ! */
  for (int slat = top; slat != bottom; slat += TILESIZE) {
    for (int slon = left; slon != right; slon += TILESIZE) {
      int bucket = Hash (slon, slat);
      for (halfSegType *hs = (halfSegType *)(data + hashTable[bucket]);
           hs < (halfSegType *)(data + hashTable[bucket + 1]); hs++) {
        /* If hs is not on the (slat, slon) tile, continue. */
        if (((hs->lat ^ slat) >> TILEEXP) ||
            ((hs->lon ^ slon) >> TILEEXP)) continue;
        halfSegType *other = (halfSegType *)(data + hs->other);
        /* If hs is the first half of a segment that is completely
           contained on the tiles that cover the window, continue because
           we will draw it when we see the other hs. Test doesn't work for
           wrapping... */
        if (hs->wayPtr != TO_HALFSEG && left <= other->lon && other->lon <
          right && top <= other->lat && other->lat < bottom) continue;
          
        wayType *w = (wayType *)(data +
          (hs->wayPtr != TO_HALFSEG ? hs->wayPtr : other->wayPtr));
        gdk_gc_set_foreground (draw->style->fg_gc[0],
          &highwayColour[w->type]);
        gdk_draw_line (draw->window, draw->style->fg_gc[0],
          (hs->lon - clon) / perpixel + width / 2,
          draw->allocation.height / 2 - (hs->lat - clat) / perpixel,
          (other->lon - clon) / perpixel + width / 2,
          draw->allocation.height / 2 - (other->lat - clat) / perpixel);
      }
    } /* for each visible tile */
  }
  
  return FALSE;
}

int main (int argc, char *argv[])
{

  if (argc > 1) {
    if (argc > 2 || stricmp (argv[1], "rebuild")) {
      fprintf (stderr, "Usage : %s [rebuild]\n", argv[0]);
      return 1;
    }
    if (!(pak = fopen ("gosmore.pak", "w+"))) {
      fprintf (stderr, "Cannot create gosmore.pak\n");
      return 2;
    }
    way.type = sizeof (highway) / sizeof (highway[0]) - 1;
    // Set up something for unwayed segments to point to. This will be
    // written to location 0 when we encounter the first <way> tag.
  
    char tag[301], key[301], value[301], quote, feature[301];
    int nodesSorted = 1, doWays = 0, from;

    printf ("Reading nodes...\n");
    node = (nodeType *) malloc (sizeof (*node) * 15000000);
    halfSeg = (halfSegType *) malloc (sizeof (*halfSeg) * 30000000);
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
            if (!stricmp (tag, "node")) {
              if (!stricmp (key, "id")) node[nodeCnt].id = atoi (value);
              if (!stricmp (key, "lat")) {
                node[nodeCnt].lat = Latitude (atof (value));
              }
              if (!stricmp (key, "lon")) {
                node[nodeCnt++].lon = Longitude (atof (value));
                if (nodeCnt % 100000 == 0) printf ("%9d nodes\n", nodeCnt);
                nodesSorted = 0;
              }
            }
            if (!stricmp (tag, "segment")) {
              if (!stricmp (key, "id")) {
                halfSeg[halfSegCnt].other = atoi (value);
                doWays = 0;
              }
              if (!stricmp (key, "from")) {
                if (!nodesSorted++) {
                  printf ("Sorting nodes...\n");
                  qsort (node, nodeCnt, sizeof (node[0]), NodeIdCmp);
                  printf ("%d %d\n", node[0].id, node[1].id);
                  printf ("Reading segments..\n");
                }
                from = atoi (value);
                if (halfSegCnt % 20000 == 0) {
                  printf ("%9d segments\n", halfSegCnt / 2);
                }
              }
              if (!stricmp (key, "to")) WriteSeg (from, atoi (value));
            }
            if (!stricmp (tag, "way")) {
              if (!doWays++) {
                printf ("Sorting segments\n");
                qsort (halfSeg, halfSegCnt / 2, sizeof (*halfSeg) * 2,
                  HalfSegIdCmp);
                printf ("Reading ways\n");
              }
              fwrite (&way, sizeof (way), 1, pak);
            }
            if (doWays && !stricmp (tag, "seg") && !stricmp (key, "id")) {
              halfSegType key[2], *hs;
              key[0].other = atoi (value);
              if ((hs = (halfSegType *) bsearch (key, halfSeg, halfSegCnt / 2,
                      sizeof (*halfSeg) * 2, HalfSegIdCmp)) != NULL) {
                hs->wayPtr = ftell (pak);
              }
            }
            if (doWays && !stricmp (tag, "tag")) {
              if (!stricmp (key, "k")) strcpy (feature, value);
              if (!stricmp (key, "v") && !stricmp (feature, "highway")) {
                for (way.type = 0; highway[way.type].name &&
                  stricmp (value, highway[way.type].name); way.type++) {}
              }
            }
          } /* if key / value pair found */
        } /* while search for key / value pairs */
      } while (getchar () != '>');
      //printf ("\n");
    } /* while we found another tag */
    free (node);
    fwrite (&way, sizeof (way), 1, pak); /* Flush out the last one */
    printf ("Sorting data\n");
    int *hsIdx = (int *) malloc (sizeof (*hsIdx) * halfSegCnt);
    for (int i = 0; i < halfSegCnt; i++) hsIdx[i] = i;
    qsort (hsIdx, halfSegCnt, sizeof (*hsIdx), HalfSegCmp);
    int hsBase = ftell (pak);
    printf ("Calculating addresses\n");
    for (int i = 0; i < halfSegCnt; i++) {
      halfSeg[hsIdx[i] ^ 1].other = i * sizeof (*halfSeg) + hsBase;
    }
    printf ("Writing Data\n");
    for (int i = 0; i < halfSegCnt; i++) {
      fwrite (halfSeg + hsIdx[i], sizeof (*halfSeg), 1, pak);
    }
    printf ("Writing hash table\n");
    fwrite (&hsBase, sizeof (hsBase), 1, pak);
    for (int bucket = 0, i = 0; bucket < BUCKETS; bucket++) {
      while (i < halfSegCnt &&
             Hash (halfSeg[hsIdx[i]].lon, halfSeg[hsIdx[i]].lat) == bucket) {
        i++;
        hsBase += sizeof (*halfSeg);
      }
      fwrite (&hsBase, sizeof (hsBase), 1, pak);
    }
    /* It has BUCKETS + 1 entries so that we can easily look up where each
       bucket begins and ends */
  } /* if rebuilding */
  else if (!(pak = fopen ("gosmore.pak", "r"))) {
    fprintf (stderr, "Cannot read gosmore.pak\nYou can (re)build it from\n"
      "the planet file e.g. bzip2 -d planet-...osm.bz2 | %s rebuild\n",
      argv[0]);
    return 3;
  }
  fseek (pak, 0, SEEK_END);
  data = (char*)
    mmap (0, ftell (pak), PROT_READ, MAP_SHARED, fileno (pak), 0);
  hashTable = (int *) (data + ftell (pak)) - BUCKETS - 1;
  
  clon = Longitude (27.9);
  clat = Latitude (-26.1);
  zoom = lrint (0.1 / 180 * 2147483648.0 * cos (26.1 / 180 * M_PI));

  gtk_init (&argc, &argv);
  draw = gtk_drawing_area_new ();
  gtk_signal_connect (GTK_OBJECT (draw), "expose_event",
    (GtkSignalFunc) Expose, NULL);
  gtk_signal_connect (GTK_OBJECT (draw), "button_press_event",
    (GtkSignalFunc) Click, NULL);
  gtk_widget_set_events (draw, GDK_EXPOSURE_MASK | GDK_BUTTON_PRESS_MASK);
  
  GtkWidget *window = gtk_window_new (GTK_WINDOW_TOPLEVEL);
  gtk_container_add (GTK_CONTAINER (window), draw);
  gtk_signal_connect (GTK_OBJECT (window), "delete_event",
    GTK_SIGNAL_FUNC (gtk_main_quit), NULL);
  gtk_widget_show (draw);
  gtk_widget_show (window);
  gtk_main ();
}
