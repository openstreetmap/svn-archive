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

#define BUCKETS (1<<24) // (1<<24) /* Must be power of 2 */
#define TILEEXP (13)
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

int nodeCnt = 0;

struct halfSegType {
  int lon, lat, next, wayPtr;
};

/*
struct segType {
  int id, from, to;
} seg[30000000]; */

int NodeIdCmp (const void *a, const void *b)
{
  return ((nodeType*)b)->id - ((nodeType*)a)->id;
}

int *hashTable;
FILE *pak;

void WriteSeg (int wayPtr, int from, int to)
{
  nodeType key, *n;
  int bucket, i;
  halfSegType hs[2];
  for (i = 0; i < 2; i++) {
    key.id = i ? to : from;
    n = (nodeType *) bsearch (&key, node, nodeCnt, sizeof (key), NodeIdCmp);
    if (!n) return;
    hs[i].lon = n->lon;
    hs[i].lat = n->lat;
  }
  for (i = 0; i < 2; i++) {
    bucket = Hash (hs[i].lon, hs[i].lat);
    hs[i].next = hashTable[bucket];
    hs[i].wayPtr = i ? 0 : wayPtr;
    hashTable[bucket] = ftell (pak);
    fwrite (hs + i, sizeof (hs[i]), 1, pak);
  }
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
  GdkRectangle clip;
  clip.x = 0;
  clip.y = 0;
  clip.width = width;
  clip.height = draw->allocation.height;
  gdk_gc_set_clip_rectangle (draw->style->fg_gc[0], &clip);
  
  /* Here we wrap around from N85 to S85 ! */
  for (int slat = top; slat != bottom; slat += TILESIZE) {
    for (int slon = left; slon != right; slon += TILESIZE) {
      for (int hsOff = hashTable[Hash (slon, slat)]; hsOff != -1; ) {
        halfSegType *hs = (halfSegType *)(data + hsOff);
        hsOff = hs->next;
        /* If hs is not on the (slat, slon) tile, continue.
           If hs is the first half of a segment that is completely
           contained on the tiles that cover the window, continue because
           we will draw it when we see the other hs. Test doesn't work for
           wrapping... */
        if (((hs->lat ^ slat) >> TILEEXP) ||
            ((hs->lon ^ slon) >> TILEEXP) ||
            (hs->wayPtr && top <= hs[1].lat && hs[1].lat < bottom &&
            left <= hs[1].lon && hs[1].lon < right)) continue;
        if (!hs->wayPtr) hs--; /* Find first part of segment */
        gdk_draw_line (draw->window, draw->style->fg_gc[0],
          (hs->lon - clon) / perpixel + width / 2,
          draw->allocation.height / 2 - (hs->lat - clat) / perpixel,
          (hs[1].lon - clon) / perpixel + width / 2,
          draw->allocation.height / 2 - (hs[1].lat - clat) / perpixel);
      }
    }
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
  
    FILE *in = stdin; //fopen ("/dosc/OSM/northr4.osm", "r");
    char tag[81], key[81], value[81], quote;
    int segCnt = 0, from;

    printf ("Reading nodes...\n");
    node = (nodeType *) malloc (sizeof (*node) * 30000000);
    hashTable = (int *) malloc (sizeof (*hashTable) * BUCKETS);
    memset (hashTable, -1, sizeof (*hashTable) * BUCKETS);
    while (fscanf (in, " <%80[a-zA-Z0-9?/]", tag) == 1) {
      //printf ("%s", tag);
      do {
        while (fscanf (in, " %80[a-zA-Z0-9]", key)) {
          if (getc (in) == '=') {
            quote = getc (in);
            if (quote == '\'') fscanf (in, "%80[^']'", value); /* " */
            else if (quote == '"') fscanf (in, "%80[^\"]\"", value); /* " */
            else {
              ungetc (quote, in);
              fscanf (in, "%80[^ ]", value); /* " */
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
              }
            }
            if (!stricmp (tag, "segment")) {
              //if (!stricmp (key, "id")) seg[segCnt].id = atoi (value);
              if (!stricmp (key, "from")) {
                if (segCnt == 0) {
                  printf ("Sorting nodes...\n");
                  qsort (node, nodeCnt, sizeof (node[0]), NodeIdCmp);
                  printf ("%d %d\n", node[0].id, node[1].id);
                  printf ("Reading segments..\n");
                }
                from = atoi (value);
                if (++segCnt % 10000 == 0) printf ("%9d segments\n", segCnt);
              }
              if (!stricmp (key, "to")) WriteSeg (1, from, atoi (value));
            }
          } /* if key / value pair found */
        } /* while search for key / value pairs */
      } while (getc (in) != '>');
      //printf ("\n");
    } /* while we found another tag */
    fwrite (hashTable, sizeof (*hashTable), BUCKETS, pak);
    free (hashTable);
    free (node);
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
  hashTable = (int *) (data + ftell (pak)) - BUCKETS;
  
  clon = Longitude (28.20);
  clat = Latitude (-25.7);
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
