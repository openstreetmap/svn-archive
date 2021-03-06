/* Copyright 2010 Nic Roets as detailed in the README file. */
/* Written by Nic Roets with contribution(s) from Dave Hansen, Ted Mielczarek
   David Dean, Pablo D'Angelo, Dmitry and Adrian Batzill.
   Thanks to
   * Sven Geggus, Frederick Ramm, Johnny Rose Carlsen and Lambertus for hosting,
   * Stephan Rossig, Simon Wood, David Dean, Lambertus and many others for testing,
   * OSMF for partial funding. */
// rm -f obj/local/armeabi/objs/gosmore/gosmore.o.d; ../../../../android/android-ndk-r5/ndk-build
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <math.h>
#include <time.h>
#include <string>
#include <stack>
#include <vector>
#include <algorithm>
#include <queue>
#include <map>

#define LG  //__android_log_print (ANDROID_LOG_WARN, "Gosmore", "%d", __LINE__);
#ifdef ANDROID_NDK
#include <android/log.h>
#include <jni.h>
#include <GLES/gl.h>
#include <GLES/glext.h>
#define NOGTK
#define LOG //__android_log_print (ANDROID_LOG_WARN, "Gosmore", "%d", __LINE__);
#endif
using namespace std;
#ifndef _WIN32
#include <sys/mman.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/file.h>
#include <sys/time.h>
#include <errno.h>
#define TEXT(x) x
#define TCHAR char
#else
#include <windows.h>
#endif
#ifdef _WIN32_WCE
#define NOGTK
#endif
#if defined(NOGTK) && defined (_WIN32)
#include <io.h>
#include <sys/stat.h>
#include <windowsx.h>
#ifdef _WIN32_WCE
#include <sipapi.h>
#include <aygshell.h>
#include "ceglue.h"
#else
#include <mmsystem.h> // For playing a sound under W32
#define SipShowIM(x)
#define CeEnableBacklight(x) FALSE
#define CreateFileForMapping(a,b,c,d,e,f,g) CreateFile (a,b,c,d,e,f,g)
#endif
#include "libgosm.h"
#include "ConvertUTF.h"
#include "resource.h"

#define OLDOLDOPTIONS \
  o (ModelessDialog,  0, 2)

HINSTANCE hInst;
static HWND   mWnd, dlgWnd = NULL, hwndEdit, button3D, buttons[3];
#else
#include <unistd.h>
#include <sys/stat.h>
#include "libgosm.h"
#define wchar_t char
#define wsprintf sprintf
#endif
#if !defined (HEADLESS) && !defined (NOGTK)
#ifdef USE_GNOMESOUND
#include <libgnome/libgnome.h>
#endif
#include <gtk/gtk.h>
#include <gdk/gdk.h>
#include <gdk/gdkx.h>
#include <gdk-pixbuf/gdk-pixbuf.h>
#include <curl/curl.h>
#include <curl/easy.h>
#endif
#include "openglespolygon.h"
#ifdef USE_GEOCLUE // Not used and never worked
#include <geoclue/geoclue-position.h>
#endif
#ifdef USE_GPSD
#include <gps.h>
#endif

#define OPTIONS \
  o (FollowGPSr,      0, 2) \
  o (AddWayOrNode,    0, 2) \
  o (StartRoute,      0, 1) \
  o (EndRoute,        0, 1) \
  o (OrientNorthwards,0, 2) \
  o (LoadGPX,         0, 1) \
  o (FastestRoute,    0, 2) \
  o (Vehicle,         motorcarR, onewayR) \
  o (English,         0, \
                 sizeof (optionNameTable) / sizeof (optionNameTable[0])) \
  o (ButtonSize,      1, 5) /* Currently only used for AddWay scrollbar */ \
  o (IconSet,         0, 4) \
  o (DetailLevel,     0, 5) \
  o (ValidateMode,    0, 2) \
  o (Exit,            0, 2) \
  o (DisplayOff,      0, 1) \
  o (FullScreen,      0, 2) \
  o (ShowCompass,     0, 2) \
  o (Background,      0, 16) \
  o (ShowCoordinates, 0, 3) \
  o (ShowTrace,       0, 2) \
  o (ViewOSM,         0, 1) \
  o (EditInPotlatch,  0, 1) \
  o (ViewGMaps,       0, 1) \
  o (UpdateMap,       0, 1) \
  o (Layout,          0, 3) \
  o (CommPort,        0, 13) \
  o (BaudRate,        0, 6) \
  o (ZoomInKey,       0, 3) \
  o (ZoomOutKey,      0, 3) \
  o (MenuKey,         0, 3) \
  o (Keyboard,        0, 2) \
  o (DebounceDrag,    0, 2) \
  o (Future2,         0, 1) \
  o (Future3,         0, 1) \
  o (Future4,         0, 1) \
  o (ShowActiveRouteNodes, 0, 2) \
  o (SearchSpacing,   32, 1) \

int Display3D = 0; // Not an option but a button for now.

#define COMMANDS o (cmdturnleft /* Must be first */, 0, 0) \
  o (cmdkeepleft, 0, 0) \
  o (cmdturnright, 0, 0) o (cmdkeepright, 0, 0) o (cmdstop, 0, 0) \
  o (cmduturn, 0, 0) o (cmdround1, 0, 0) o (cmdround2, 0, 0) \
  o (cmdround3, 0, 0) o (cmdround4, 0, 0) o (cmdround5, 0, 0) \
  o (cmdround6, 0, 0) o (cmdround7, 0, 0) o (cmdround8, 0, 0)
char docPrefix[80] = "";
int GpsIdle=999;


// We emulate just enough of gtk to make it work
#if defined(NOGTK)
#define gtk_widget_queue_clear(x) // After Click() returns we Invalidate
#define gtk_toggle_button_set_active(x,y) // followGPRr
struct GtkWidget { 
  struct {
    int width, height;
  } allocation;
  int window;
};
typedef int GtkComboBox;
struct GdkEventButton {
  int x, y, button, time;
};

struct GdkEventScroll {
  int x, y, direction;
};

enum { GDK_SCROLL_UP, GDK_SCROLL_DOWN };

/*#define ROUTE_PEN 0
#define VALIDATE_PEN 1
#define RESERVED_PENS 2 */

#ifndef ANDROID_NDK
#define LOG logprintf ("%d\n", __LINE__);
#endif
#else
#define LOG // Less debug info needed because we have gdb
#ifndef RES_DIR
#define RES_DIR "/usr/share/gosmore/" /* Needed for "make CFLAGS=-g" */
#endif
const char *FindResource (const char *fname)
{ // Occasional minor memory leak : The caller never frees the memory.
  string s;
  struct stat dummy;
  // first check in current working directory
  if (stat (fname, &dummy) == 0) return strdup (fname);
  // then check in $HOME
  if (getenv ("HOME")) {
    s = (string) getenv ("HOME") + "/.gosmore/" + fname;
    if (stat (s.c_str (), &dummy) == 0) return strdup (s.c_str());
  }
  // then check in RES_DIR
  s = (string) RES_DIR + fname;
  if (stat (s.c_str (), &dummy) == 0) return strdup (s.c_str());
  // and then fall back on current working directory if it cannot be
  // found anywhere else (this is so new files are created in the
  // current working directory)
  return strdup (fname);
}
#endif
#ifndef HEADLESS
GtkWidget *draw, *location, *display3D, *followGPSr;
#endif

// used for showing logs to a file (with default)
// changed to more suitable value for WinCE in WinMain
char logFileName[80] = "gosmore.log.txt";

FILE * logFP(bool create = true) {
  static FILE * f = NULL;
  if (!f && create) {
    f = fopen(logFileName,"at");
    fprintf(f,"----- %s %s\n", __DATE__, __TIME__);
  }
  return f;
}

void logprintf(const char * format, ...)
{
// print [hh:mm:ss] timestamp to log first
#ifdef _WIN32_CE
  // wince doesn't implement the time.h functions
  SYSTEMTIME t;
  GetLocalTime(&t);
  fprintf(logFP(), "[%02d:%02d:%02d] ", t.wHour, t.wMinute, t.wSecond);
#else
  time_t seconds = time(NULL);
  struct tm * t = localtime(&seconds);
  fprintf(logFP(), "[%02d:%02d:%02d] ",t->tm_hour,t->tm_min,t->tm_sec);
#endif

  // then print original log string
  va_list args;
  va_start(args, format);
  vfprintf(logFP(), format, args);
  va_end (args);
}

struct klasTableStruct {
  const TCHAR *desc;
  const char *tags;
} klasTable[] = {
#define s(k,v,shortname,extraTags) \
  { TEXT (shortname), "  <tag k='" #k "' v='" #v "' />\n" extraTags },
STYLES
#undef s
};

#define notImplemented \
  o (ShowCompass,     ) \
  o (ShowPrecision,   ) \
  o (ShowSpeed,       ) \
  o (ShowHeading,     ) \
  o (ShowElevation,   ) \
  o (ShowDate,        ) \
  o (ShowTime,        ) \

#define o(en,min,max) en ## Num,
enum {
  OPTIONS wayPointIconNum, COMMANDS
  mapMode, optionMode, searchMode, chooseObjectToAdd
};
#undef o

int listYOffset; // Number of pixel. Changed by dragging.

//  TEXT (#en), TEXT (de), TEXT (es), TEXT (fr), TEXT (it), TEXT (nl) },
const char *optionNameTable[][mapMode] = {
#define o(en,min,max) #en,
  { OPTIONS }, // English is same as variable names
#undef o

#include "translations.c"
};

#define o(en,min,max) int en = min;
OPTIONS // Define a global variable for each option
#undef o

int clon, clat, zoom, option = EnglishNum, gpsSockTag, setLocBusy = FALSE, gDisplayOff;
/* zoom is the amount that fits into the window (regardless of window size) */

TCHAR currentBbox[80] = TEXT ("");

int ChangePak (const TCHAR *pakfile, int mlon, int mlat)
{ // Returns TRUE if pakfile or a bbox was loaded, false if default was loaded or failure
  static int bboxList[][4] = { 
#include "bboxes.c"
  }, world[] = { -512, -512, 512, 512 }, *bbox = NULL;

  if (bbox && bbox[0] <= (mlon >> 22) && bbox[1] <= ((-mlat) >> 22) &&
              bbox[2] >  (mlon >> 22) && bbox[3] >  ((-mlat) >> 22)) return 0;
  LG GosmFreeRoute ();
  memset (gosmSstr, 0, sizeof (gosmSstr));
  shortest = NULL;
        
  LG if (!pakfile) {
    int best = 0;
    for (size_t j = 0; j < sizeof (bboxList) / sizeof (bboxList[0]); j++) {
      int worst = min (mlon / 8 - (bboxList[j][0] << 19),
                  min (-mlat / 8 - (bboxList[j][1] << 19),
                  min ((bboxList[j][2] << 19) - mlon / 8,
                       (bboxList[j][3] << 19) + mlat / 8)));
      // Find the worst border of bbox j. worst < 0 implies we are
      // outside it.
      if (worst > best) { 
        best = worst;
        pakfile = currentBbox;
        bbox = bboxList[j];
        #ifdef _WIN32_WCE
        GetModuleFileName (NULL, currentBbox, sizeof (currentBbox) / sizeof (currentBbox[0]));
        wsprintf (wcsrchr (currentBbox, L'\\'), TEXT ("\\%04d%04d%04d%04d.pak"),
        #else
        sprintf (currentBbox, "%04d%04d%04d%04d.pak",
        #endif
          bboxList[j][0] + 512, bboxList[j][1] + 512,
          bboxList[j][2] + 512, bboxList[j][3] + 512);
      }
    }
  }
  else bbox = world;

  #ifdef WIN32
  static HANDLE gmap = INVALID_HANDLE_VALUE, fm = INVALID_HANDLE_VALUE;
  static void *map = NULL;
  LOG if (map) UnmapViewOfFile (map);
  LOG if (fm != INVALID_HANDLE_VALUE) CloseHandle (fm);
  LOG if (gmap != INVALID_HANDLE_VALUE) CloseHandle (gmap);
  
  LOG gmap = CreateFileForMapping (pakfile, GENERIC_READ, FILE_SHARE_READ,
    NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL /*FILE_FLAG_NO_BUFFERING*/,
    NULL);
  LOG if (gmap == INVALID_HANDLE_VALUE && currentBbox == pakfile) {
    #ifdef _WIN32_WCE
    wsprintf (wcsrchr (currentBbox, L'\\'), TEXT ("\\default.pak"));
    LOG gmap = CreateFileForMapping (pakfile, GENERIC_READ, FILE_SHARE_READ,
      NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL /*FILE_FLAG_NO_BUFFERING*/,
      NULL);    
    #else
    LOG gmap = CreateFileForMapping ("default.pak", GENERIC_READ,
      FILE_SHARE_READ,
      NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL /*FILE_FLAG_NO_BUFFERING*/,
      NULL);    
    #endif
  }
  LOG fm = gmap == INVALID_HANDLE_VALUE ? INVALID_HANDLE_VALUE :
    CreateFileMapping(gmap, NULL, PAGE_READONLY, 0, 0, 0);
  LOG map = fm == INVALID_HANDLE_VALUE ? NULL :
    MapViewOfFile (fm, FILE_MAP_READ, 0, 0, 0);
  int len = map ? GetFileSize (gmap, NULL) : 0;
  LOG Exit = !map || !GosmInit (map, GetFileSize(gmap, NULL));
  LOG if (Exit && gmap != INVALID_HANDLE_VALUE) {
    MessageBox (NULL, TEXT ("mmap problem. Pak file too big ?"),
      TEXT (""), MB_APPLMODAL|MB_OK);
  }

  #ifdef _WIN32
  FILE *gmap = _wfopen (/*"./gosmore.pak"*/ , TEXT ("rb"));

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
  #endif
  #else // defined (__linux__)
  static void *map = (void*) -1;
  static size_t len = 0 /* Shut up gcc */;
//  printf ("%s %d %d\n", pakfile, (mlon >> 22) + 512, 512 - (mlat >> 22));
  if (map != (void*) -1) munmap (map, len);
  
  FILE *pakmap = fopen64 (pakfile, "r");
  FILE *gmap = !pakmap && currentBbox == pakfile
                  ? fopen64 ("default.pak", "r") : pakmap;
  if (!gmap && currentBbox == pakfile) {
    gmap = fopen64 (RES_DIR "default.pak", "r");
  }
  LG len = gmap && fseek (gmap, 0, SEEK_END) == 0 ? ftell (gmap) : 0;
  map = !len ? (void*)-1
     : mmap (NULL, ftell (gmap), PROT_READ, MAP_SHARED, fileno (gmap), 0);
  LG Exit = map == (void *) -1 || !GosmInit (map, len);
  LG if (gmap) fclose (gmap);
  #endif
  /* // Slightly more portable:
  GMappedFile *gmap = g_mapped_file_new (pakfile, FALSE, NULL);
  Exit = !gmap || !GosmInit (g_mapped_file_get_contents (gmap),
      g_mapped_file_get_length (gmap));
  */
//  __android_log_print(ANDROID_LOG_WARN, "Gosmore", "re %p %d", bbox, Exit);
  LOG if (Exit) bbox = NULL;
  LG return pakmap != NULL;
}
#ifdef ANDROID_NDK
extern "C" jint Java_org_osmu_gosmore_MapActivity_changePak (JNIEnv*  env,
    jobject thiz, jstring js, jint mlon, jint mlat)
{
  LG IconSet = 1;
  DetailLevel = 3;
  ButtonSize = 4;
  Background = 1;
  option= mapMode;
  ShowCompass = 0;
  const char *sdcard = env->GetStringUTFChars(js, NULL);
  chdir (sdcard);
  LG env->ReleaseStringUTFChars(js, sdcard);
  if (ChangePak ("gosmore.pak", 0, 0)) return 2;
  if (Exit && ChangePak (NULL, mlon, mlat)) return 2;
  if (Exit) strcpy (currentBbox, "default");
//  __android_log_print(ANDROID_LOG_WARN, "Gosmore", "re %s", currentBbox);
  return Exit ? 0 : 1;
}

extern "C" jstring Java_org_osmu_gosmore_Update_currentBbox
                       (JNIEnv* env, jobject thiz)
{
//  __android_log_print(ANDROID_LOG_WARN, "Gosmore", "re %s", currentBbox);
  currentBbox[16] = '\0'; // Strip the .pak off for Java
  return env->NewStringUTF (currentBbox);
}

#endif

#ifndef HEADLESS

double cosAzimuth = 1.0, sinAzimuth = 0.0;
string highlight, searchStr ("Search");

inline void SetLocation (int nlon, int nlat)
{
  clon = nlon;
  clat = nlat;
  #ifndef NOGTK
  char lstr[50];
  int zl = 0;
  while (zl < 32 && (zoom >> zl)) zl++;
  setlocale (LC_NUMERIC, "C");
  sprintf (lstr, "?lat=%.5lf&lon=%.5lf&zoom=%d", LatInverse (nlat),
    LonInverse (nlon), 33 - zl);
  setLocBusy = TRUE;
  gtk_entry_set_text (GTK_ENTRY (location), lstr);
  setLocBusy = FALSE;
  #endif
}

#ifndef NOGTK
int ChangeLocation (void)
{
  if (setLocBusy) return FALSE;
  char *lstr = (char *) gtk_entry_get_text (GTK_ENTRY (location));
  double lat, lon;
  while (*lstr != '?' && *lstr != '\0') lstr++;
  setlocale (LC_NUMERIC, "C");
  if (sscanf (lstr, "?lat=%lf&lon=%lf&zoom=%d", &lat, &lon, &zoom) == 3) {
    clat = Latitude (lat);
    clon = Longitude (lon);
    zoom = 0xffffffff >> (zoom - 1);
    gtk_widget_queue_clear (draw);
  }
  return FALSE;
}

int ChangeOption (void)
{
  Display3D = gtk_toggle_button_get_active (GTK_TOGGLE_BUTTON (display3D));
  FollowGPSr = gtk_toggle_button_get_active (GTK_TOGGLE_BUTTON (followGPSr));
  gtk_widget_queue_clear (draw);
  return FALSE;
}
#endif
/*-------------------------------- NMEA processing ------------------------*/
/* My TGPS 374 frequently produces corrupt NMEA output (probably when the
   CPU goes into sleep mode) and this may also be true for GPS receivers on
   long serial lines. To overcome this, we ignore badly formed sentences and
   we interpolate the date where the jumps in time values seems plausible.
   
   For the GPX output there is an extra layer of filtering : The minimum
   number of observations are dropped so that the remaining observations does
   not imply any impossible manuavers like traveling back in time or
   reaching supersonic speed. This effeciently implemented transforming the
   problem into one of finding the shortest path through a graph :
   The nodes {a_i} are the list of n observations. Add the starting and
   ending nodes a_0 and a_n+1, which are both connected to all the other
   nodes. 2 observation nodes are connected if it's possible that the one
   will follow the other. If two nodes {a_i} and {a_j} are connected, the
   cost (weight) of the connection is j - 1 - i, i.e. the number of nodes
   that needs to be dropped. */
struct gpsNewStruct {
  struct {
    double latitude, longitude, track, speed, hdop, ele;
    char date[6], tm[6];
  } fix;
  int lat, lon; // Mercator
  unsigned dropped;
  struct gpsNewStruct *dptr;
} gpsTrack[18000], *gpsNew = gpsTrack;

// Return the basefilename for saving .osm and .gpx files
void getBaseFilename(char* basename, gpsNewStruct* first) {
  char VehicleName[80];
  #define M(v) Vehicle == v ## R ? #v :
  sprintf(VehicleName, "%s", RESTRICTIONS NULL);
  #undef M

  if (first) {
    sprintf (basename, "%s%.2s%.2s%.2s-%.6s-%s", docPrefix,
	     first->fix.date + 4, first->fix.date + 2, first->fix.date,
	     first->fix.tm, VehicleName);
  } else {
    // get time from computer if no gps traces
#ifdef _WIN32_WCE
    SYSTEMTIME t;
    GetSystemTime(&t);
    sprintf (basename, "%s%02d%02d%02d-%02d%02d%02d-%s", docPrefix,
	     t.wYear % 100, t.wMonth, t.wDay,
	     t.wHour, t.wMinute, t.wSecond, VehicleName);
#endif
  }
}

gpsNewStruct *FlushGpx (void)
{
  struct gpsNewStruct *a, *best, *first = NULL;
  for (best = gpsNew; gpsTrack <= best && best->dropped == 0; best--) {}
  gpsNew = gpsTrack;
  if (best <= gpsTrack) return NULL; // No observations
  for (a = best - 1; gpsTrack <= a && best < a + best->dropped; a--) {
    if (best->dropped > best - a + a->dropped) best = a;
  }
  // We want .., best->dptr->dptr->dptr, best->dptr->dptr, best->dptr, best
  // Now we reverse the linked list :
  while (best) {
    a = best->dptr;
    best->dptr = first;
    first = best;
    best = a;
  }
  
  char bname[80], fname[80];
  getBaseFilename(bname, first);
  sprintf (fname, "%s.gpx", bname);
  FILE *gpx = fopen (fname, "wb");
  if (!gpx) return first;
  fprintf (gpx, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\
<gpx\n\
 version=\"1.0\"\n\
creator=\"gosmore\"\n\
xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n\
xmlns=\"http://www.topografix.com/GPX/1/0\"\n\
xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\n\">\n\
<trk>\n\
<trkseg>\n");
  for (best = first; best; best = best->dptr) { // Iterate the linked list
    fprintf (gpx, "<trkpt lat=\"%.9lf\" lon=\"%.9lf\">\n",
      best->fix.latitude, best->fix.longitude);
    if (best->fix.ele < 1e+8) {
      fprintf (gpx, "<ele>%.3lf</ele>\n", best->fix.ele);
    }
    fprintf (gpx, "<time>20%.2s-%.2s-%.2sT%.2s:%.2s:%.2sZ</time>\n</trkpt>\n",
      best->fix.date + 4, best->fix.date + 2, best->fix.date,
      best->fix.tm, best->fix.tm + 2, best->fix.tm + 4);
    
//    if (best->next && ) fprintf (gpx, "</trkseg>\n</trk>\n<trk>\n<trkseg>\n");
  }
  fprintf (gpx, "</trkseg>\n\
</trk>\n\
</gpx>\n");
  return first;
}

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
    // latitude is at fStart[col], longitude is at fStart[col + 2].
    if (fNr >= (col == 0 ? 2 : col == 1 ? 6 : col == 2 ? 13 : 10)) {
      if (col > 0 && fLen[col] > 6 && memchr ("SsNn", rx[fStart[col + 1]], 4)
        && fLen[col + 2] > 7 && memchr ("EeWw", rx[fStart[col + 3]], 4) &&
        fLen[col == 1 ? 5 : 1] >= 6 &&
          memcmp (gpsNew->fix.tm, rx + fStart[col == 1 ? 5 : 1], 6) != 0) {
        double nLat = (rx[fStart[col]] - '0') * 10 + rx[fStart[col] + 1]
          - '0' + atof (rx + fStart[col] + 2) / 60;
        double nLon = ((rx[fStart[col + 2]] - '0') * 10 +
          rx[fStart[col + 2] + 1] - '0') * 10 + rx[fStart[col + 2] + 2]
          - '0' + atof (rx + fStart[col + 2] + 3) / 60;
        if (nLat > 90) nLat = nLat - 180;
        if (nLon > 180) nLon = nLon - 360; // Mungewell's Sirf Star 3
        if (tolower (rx[fStart[col + 1]]) == 's') nLat = -nLat;
        if (tolower (rx[fStart[col + 3]]) == 'w') nLon = -nLon;
        if (fabs (nLat) < 90 && fabs (nLon) < 180 &&
            (nLat != 0 || nLon != 0)) { // JNC when starting up
          if (gpsTrack + sizeof (gpsTrack) / sizeof (gpsTrack[0]) <=
            ++gpsNew) FlushGpx ();
          memcpy (gpsNew->fix.tm, rx + fStart[col == 1 ? 5 : 1], 6);
          gpsNew->dropped = 0;
          dataReady = TRUE; // Notify only when parsing is complete
          gpsNew->fix.latitude = nLat;
          gpsNew->fix.longitude = nLon;
          gpsNew->lat = Latitude (nLat);
          gpsNew->lon = Longitude (nLon);
          gpsNew->fix.ele = 1e+9;
        }
      } // If the timestamp wasn't seen before
      if (col == 2) {
        gpsNew->fix.hdop = atof (rx + fStart[8]);
        gpsNew->fix.ele = atof (rx + fStart[9]); // Check height of geoid ??
      }
      if (col == 3 && fLen[7] > 0 && fLen[8] > 0 && fLen[9] >= 6) {
        memcpy (gpsNew->fix.date, rx + fStart[9], 6);
        gpsNew->fix.speed = atof (rx + fStart[7]);
        gpsNew->fix.track = atof (rx + fStart[8]);
        
        //-------------------------------------------------------------
        // Now fix the dates and do a little bit of shortest path work
        int i, j, k, l; // TODO : Change indexes into pointers
        for (i = 0; gpsTrack < gpsNew + i && !gpsNew[i - 1].dropped &&
             (((((gpsNew[i].fix.tm[0] - gpsNew[i - 1].fix.tm[0]) * 10 +
                  gpsNew[i].fix.tm[1] - gpsNew[i - 1].fix.tm[1]) * 6 +
                  gpsNew[i].fix.tm[2] - gpsNew[i - 1].fix.tm[2]) * 10 +
                  gpsNew[i].fix.tm[3] - gpsNew[i - 1].fix.tm[3]) * 6 +
                  gpsNew[i].fix.tm[4] - gpsNew[i - 1].fix.tm[4]) * 10 +
                  gpsNew[i].fix.tm[5] - gpsNew[i - 1].fix.tm[5] < 30; i--) {}
        // Search backwards for a discontinuity in tm
        
        for (j = i; gpsTrack < gpsNew + j && !gpsNew[j - 1].dropped; j--) {}
        // Search backwards for the first observation missing its date
        
        for (k = j; k <= 0; k++) {
          memcpy (gpsNew[k].fix.date,
            gpsNew[gpsTrack < gpsNew + j && k < i ? j - 1 : 0].fix.date, 6);
          gpsNew[k].dptr = NULL; // Try gpsNew[k] as first observation
          gpsNew[k].dropped = gpsNew + k - gpsTrack + 1;
          for (l = k - 1; gpsTrack < gpsNew + l && k - l < 300 &&
               gpsNew[k].dropped > unsigned (k - l - 1); l--) {
            // At the point where we consider 300 bad observations, we are
            // more likely to be wasting CPU cycles.
            int tdiff =
              ((((((((gpsNew[k].fix.date[4] - gpsNew[l].fix.date[4]) * 10 +
              gpsNew[k].fix.date[5] - gpsNew[l].fix.date[5]) * 12 +
              (gpsNew[k].fix.date[2] - gpsNew[l].fix.date[2]) * 10 +
              gpsNew[k].fix.date[3] - gpsNew[l].fix.date[3]) * 31 +
              (gpsNew[k].fix.date[0] - gpsNew[l].fix.date[0]) * 10 +
              gpsNew[k].fix.date[1] - gpsNew[l].fix.date[1]) * 24 +
              (gpsNew[k].fix.tm[0] - gpsNew[l].fix.tm[0]) * 10 +
              gpsNew[k].fix.tm[1] - gpsNew[l].fix.tm[1]) * 6 +
              gpsNew[k].fix.tm[2] - gpsNew[l].fix.tm[2]) * 10 +
              gpsNew[k].fix.tm[3] - gpsNew[l].fix.tm[3]) * 6 +
              gpsNew[k].fix.tm[4] - gpsNew[l].fix.tm[4]) * 10 +
              gpsNew[k].fix.tm[5] - gpsNew[l].fix.tm[5];
              
            /* Calculate as if every month has 31 days. It causes us to
               underestimate the speed travelled in very rare circumstances,
               (e.g. midnight GMT on Feb 28) allowing a few bad observation
               to sneek in. */
            if (0 < tdiff && tdiff < 3600 * 24 * 62 /* Assume GPS used more */
                /* frequently than once every 2 months */ &&
                fabs (gpsNew[k].fix.latitude - gpsNew[l].fix.latitude) +
                fabs (gpsNew[k].fix.longitude - gpsNew[l].fix.longitude) *
                  cos (gpsNew[k].fix.latitude * (M_PI / 180.0)) <
                    tdiff * (1600 / 3600.0 * 360 / 40000) && // max 1600 km/h
                gpsNew[k].dropped > gpsNew[l].dropped + k - l - 1) {
              gpsNew[k].dropped = gpsNew[l].dropped + k - l - 1;
              gpsNew[k].dptr = gpsNew + l;
            }
          } // For each possible connection
        } // For each new observation
      } // If it's a properly formatted RMC
    } // If the sentence had enough columns for our purposes.
    else if (i == *got) break; // Retry when we receive more data
    *got -= i;
  } /* If we know the sentence type */
  return dataReady;
}

int command[3] = { 0, 0, 0 }, oldCommand = 0;

void CallRoute (int recalculate, int plon, int plat)
{
  Route (recalculate, plon, plat, Vehicle, FastestRoute);
  #ifdef _WIN32_WCE
  MSG msg;
  while (!PeekMessage (&msg, NULL, WM_KEYFIRST, WM_KEYLAST, PM_NOREMOVE) &&
         !PeekMessage (&msg, NULL, WM_MOUSEFIRST, WM_MOUSELAST, PM_NOREMOVE) &&
         RouteLoop ()) {}
  #else
  while (RouteLoop ()) {}
  #endif
}


#ifndef ANDROID_NDK
void DoFollowThing (gpsNewStruct *gps)
{
  static int lastTime = -1;
  char *d = gps->fix.date, *t = gps->fix.tm;
  int now = (((((t[0] - '0') * 10 + t[1] - '0') * 6 + t[2] - '0') * 10 +
                 t[3] - '0') * 6 + t[4] - '0') * 10 + t[5];
  if (lastTime - now > 60) {
    lastTime = now;
    
    int cdays[12] = { 0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334 };
    double M = (cdays[(d[2] - '0') * 10 + d[3] - '0' - 1] +
                   (d[4] - '0') * 10 + d[5] - '0' - 1) * M_PI * 2 / 365.242;
    int dayLen = lrint (acos (-
      tan (-cos (M + 10 * M_PI * 2 / 365) * 23.44 / 180 * M_PI) *
      tan (gps->fix.latitude / 180 * M_PI)) * 2 / M_PI * 12 * 60 * 60);
    /* See wikipedia/Declination for the "core" of the formula */
    // TODO: acos() may fail in arctic / antarctic circle.
    int noon = 12 * 60 * 60 - lrint (gps->fix.longitude / 360 * 24 * 60 * 60
                - (-7.655 * sin (M) + 9.873 * sin (2 * M + 3.588)) * 60);
    /* See wikipedia/Equation_of_time */
    int sSinceSunrise = (now - noon + dayLen / 2 + 24 * 3600) % (24 * 3600);
    Background = (Background & 7) + (sSinceSunrise < dayLen ? 8 : 0);
    #if 0
    noon += 7200; // Central African Time = UTC + 2 hours
    //printf ("%.5lf %.5lf %s %.3lf\n", lat, lon, date, M / M_PI / 2);
    printf ("Declination %.5lf\n", -cos (M + 10 * M_PI * 2 / 365) * 23.44);
    printf ("Noon %02d%02d%02d\n", noon / 60 / 60,
      noon / 60 % 60, noon % 60);
    printf ("Sunrise %02d%02d%02d\n", (noon - dayLen / 2) / 60 / 60,
      (noon - dayLen / 2) / 60 % 60, (noon - dayLen / 2) % 60);
    printf ("Sunset %02d%02d%02d\n", (noon + dayLen / 2) / 60 / 60,
      (noon + dayLen / 2) / 60 % 60, (noon + dayLen / 2) % 60);
    printf ("%6d / %6d at %.6s\n", sSinceSunrise, dayLen, gps->fix.tm);
    #endif
  }
  if (!/*gps->fix.mode >= MODE_2D &&*/ FollowGPSr) return;
  SetLocation (Longitude (gps->fix.longitude), Latitude (gps->fix.latitude));
  __int64 dlon = clon - flon, dlat = clat - flat;
  flon = clon;
  flat = clat;
#else

extern "C" void Java_org_osmu_gosmore_MapRenderer_startRoute (
  JNIEnv*  env, jobject thiz, jdouble lon, jdouble lat)
{
  flon = Longitude (lon);
  flat = Latitude (lat);
  GosmFreeRoute ();
  shortest = NULL;
}

extern "C" void Java_org_osmu_gosmore_MapRenderer_endRoute (
  JNIEnv*  env, jobject thiz, jdouble lon, jdouble lat, jboolean fastest,
  jint vehicle)
{
  tlon = Longitude (lon);
  tlat = Latitude (lat);
  Vehicle = vehicle;
  FastestRoute = fastest;
  Route (TRUE, 0, 0, Vehicle, FastestRoute);
}

extern "C" jint Java_org_osmu_gosmore_MapRenderer_doRoute (
  JNIEnv*  env, jobject thiz)
{
  if (!RouteLoop()) return routeSuccess ? 999 : -1;
  // I suspect shortest is never NULL here, but I still guard against it.
  return (jint)(!shortest ? 0 : ((Sqr (__int64 (shortest->nd->lon - tlon)) +
    Sqr (__int64 (shortest->nd->lat - tlat))) * 100) /
    (Sqr (__int64 (flon - tlon)) +
     Sqr (__int64 (flat - tlat)) + (1<<15)));
}

extern "C" jstring Java_org_osmu_gosmore_MapRenderer_navigate (
  JNIEnv*  env, jobject thiz, jdouble lon, jdouble lat, jfloat speed,
  jfloat bearing)
{
  const char *tts = "";
  flon = Longitude (lon);
  flat = Latitude (lat);
  __int64 dlon = Longitude (lon + speed * 3600.0 /
      40000000.0 / cos (lat * (M_PI / 180.0)) *
      sin (bearing * (M_PI / 180.0))) - flon;
  __int64 dlat = Latitude (lat + speed * 3600.0 /
      40000000.0 * cos (bearing * (M_PI / 180.0))) - flat;
  // Slightly faster would be dlon=speed*(...); dlat=speed*(...);
#endif
    // Predict the vector that will be traveled in the next 10seconds
//    printf ("%5.1lf m/s Heading %3.0lf\n", gps->fix.speed, gps->fix.track);
//    printf ("%lf %lf\n", gps->fix.latitude, gps->fix.longitude);
    
  if (routeSuccess) CallRoute (FALSE, dlon, dlat);

  static ndType *decide[3] = { NULL, NULL, NULL }, *oldDecide = NULL;
  decide[0] = NULL;
  command[0] = 0;
  if (shortest) {
    routeNodeType *x = shortest->shortest;
    if (!x) command[0] = cmdstopNum;
    if (x && Sqr (dlon) + Sqr (dlon) > 10000 /* faster than ~3 km/h */ &&
        dlon * (x->nd->lon - clon) + dlat * (x->nd->lat - clat) < 0) {
      command[0] = cmduturnNum;
      decide[0] = 0;
    }
    else if (x) {
      int icmd = -1, nextJunction = TRUE; // True means the user need to take action at
      // the first junction he comes across. Otherwise we're looking ahead.
      double dist = sqrt (Sqr ((double) (x->nd->lat - flat)) +
                          Sqr ((double) (x->nd->lon - flon)));
      for (x = shortest; icmd < 0 && x->shortest &&
           dist < 40000 /* roughly 300m */; x = x->shortest) {
        int roundExit = 0;
        while (icmd < 0 && x->shortest && ((1 << roundaboutR) &
                           (Way (x->shortest->nd))->bits)) {
          if (isupper (JunctionType (x->shortest->nd))) roundExit++;
          x = x->shortest;
        }
        if (!x->shortest || roundExit) {
          decide[0] = x->nd;
          icmd = cmdround1Num - 1 + roundExit;
          break;
        }
        
        ndType *n0 = x->nd, *n1 = x->shortest->nd, *nd = n1;
        //ndType *n2 = x->shortest->shortest ? x->shortest->shortest->nd : n1;
        int n2lat =
          x->shortest->shortest ? x->shortest->shortest->nd->lat : tlat;
        int n2lon =
          x->shortest->shortest ? x->shortest->shortest->nd->lon : tlon;
        while (nd[-1].lon == nd->lon && nd[-1].lat == nd->lat) nd--;
        int segCnt = 0; // Count number of segments at x->shortest
        int n2Left = /* Keep compiler quiet*/ 0, fLeft = INT_MIN;
        do {
          // TODO : Only count segment traversable by 'Vehicle'
          // Except for the case where a cyclist crosses a motorway_link.
          
          for (int o = 0; o <= 1; o++) {
            segCnt++;
            if (!nd->other[o]) continue;
            ndType *forkO = nd + nd->other[o];
            __int64 straight =
              (forkO->lat - n1->lat) * (__int64) (n1->lat - n0->lat) +
              (forkO->lon - n1->lon) * (__int64) (n1->lon - n0->lon), left =
              (forkO->lat - n1->lat) * (__int64) (n1->lon - n0->lon) -
              (forkO->lon - n1->lon) * (__int64) (n1->lat - n0->lat);
            int isNd2 = forkO->lat == n2lat && forkO->lon == n2lon;
            if (straight > left && straight > -left &&
                (!o || !Way (nd)->bits & onewayR)) {
              // If we are approaching a split, we can ignore oncoming
              // oneways (the user can avoid them on his own).
              //printf ("%d %d %d %lf\n", isNd2, o, Way (nd)->bits & onewayR, dist);
              (isNd2 ? n2Left : fLeft) = left * 16 / straight;
            }
            if (isNd2) icmd = straight < left
              ? nextJunction ? cmdturnleftNum : cmdkeepleftNum :
              straight > -left ? -1
              : nextJunction ? cmdturnrightNum : cmdkeeprightNum;
          }
        } while (++nd < ndBase + hashTable[bucketsMin1 + 1] &&
                 nd->lon == nd[-1].lon && nd->lat == nd[-1].lat);
        if (segCnt > 2) {
          decide[0] = n1;
          nextJunction = FALSE;
        }
        else icmd = -1;
        if (icmd < 0 && fLeft != INT_MIN) { // If it's a split
          icmd = fLeft < n2Left ? cmdkeepleftNum : cmdkeeprightNum;
          //printf ("%d\n", segCnt);
        }
        
        dist += sqrt (Sqr ((double) (n2lat - n1->lat)) +
                      Sqr ((double) (n2lon - n1->lon)));
      } // While looking ahead to the next turn.
      if (icmd >= 0) command[0] = icmd;
      if (!x->shortest && dist < 6000) {
        command[0] = cmdstopNum;
        decide[0] = NULL;
      }
    } // If not on final segment
  } // If the routing was successful

  if (command[0] && (oldCommand != command[0] || oldDecide != decide[0]) &&
      command[0] == command[1] && command[1] == command[2] &&
      decide[0] == decide[1] && decide[1] == decide[2]) {
    oldCommand = command[0];
    oldDecide = decide[0];
    #define o(cmd,dummy1,dummy2) TEXT (#cmd),
#ifdef ANDROID_NDK
    static const char *cmds[] = {
    "Turn left.", "Keep left.", "Turn right", "Keep right.",
    "You have reached your destination.", "Make a U turn if possible.",
    "At the roundabout, take the first exit.",
    "At the roundabout, take the second exit.",
    "At the roundabout, take the third exit.",
    "At the roundabout, take the fourth exit.",
    "At the roundabout, take the fifth exit.",
    "At the roundabout, take the sixth exit.",
    "At the roundabout, take the seventh exit.",
    "At the roundabout, take the eighth exit." };
    tts = cmds[command[0]-cmdturnleftNum];
#elif defined(_WIN32_WCE)
    static const wchar_t *cmdStr[] = { COMMANDS };
    wchar_t argv0[80];
    GetModuleFileName (NULL, argv0, sizeof (argv0) / sizeof (argv0[0]));
    wsprintf (wcsrchr (argv0, L'\\'), TEXT ("\\%s.wav"),
      cmdStr[command[0] - cmdturnleftNum] + 3);
    // waveOutSetVolume (/*pcm*/0, 0xFFFF); // Puts the sound at maximum volume
    PlaySound (argv0, NULL, SND_FILENAME | SND_NODEFAULT | SND_ASYNC );
#else
    static const char *cmdStr[] = { COMMANDS };
    string wav = string (RES_DIR) +  // +3 is to strip the leading "cmd"
      (cmdStr[command[0] - cmdturnleftNum] + 3) + ".wav";
#ifdef _WIN32
    string wwav = string (cmdStr[command[0] - cmdturnleftNum] + 3) + ".wav";
    PlaySound (wwav.c_str(), NULL, SND_FILENAME | SND_NODEFAULT | SND_ASYNC );
#elif  defined (USE_GNOMESOUND)
    gnome_sound_play (wav.c_str ());
#else
    if (fork () == 0) execlp ("aplay", "aplay", wav.c_str (), NULL);
#endif
//    printf ("%s\n", wav.c_str()); //cmdStr[command[0] - cmdturnleftNum]);
#endif
    #undef o
  }
  memmove (command + 1, command, sizeof (command) - sizeof (command[0]));
  memmove (decide + 1, decide, sizeof (decide) - sizeof (decide[0]));

  double dist = sqrt (Sqr ((double) dlon) + Sqr ((double) dlat));
  if (!OrientNorthwards && dist > 100.0) {
    cosAzimuth = dlat / dist;
    sinAzimuth = -dlon / dist;
  }                                            
  gtk_widget_queue_clear (draw);
  #ifdef ANDROID_NDK
  return env->NewStringUTF( tts);
  #endif
} // If following the GPSr and it has a fix.

// Regrettably here are a few static variables with very generic names. They
// are however unique in this file.
static int cosa, sina, xadj, width = 320, height = 240;
__int64 yadj;
#define FAR3D  100000 // 3D view has a limit of roughly 5 km forwards
#define WIDE3D 100000 // and roughly 5km between top left & top right corner
#define CAMERA2C 20000 // How far the camera is behind the user (clat/lon)
#define HEIGHT   12000 // Height of the camera
#define PIX45     256 // Y value corresponding to 45 degrees down
#define XFix PIX45

#define myint int
/* The 3D computations can all be done in signed 32 bits integers,
   provided overflow bits are simply discarded. The C specification says
   however that ints that overflow are undefined (as well as any
   expression that touches them). So if the 3D display looks garbled
   under a new compiler, try running with #define myint __int64
*/

#define MUL 64
#define Depth(lon,lat) \
           (int)(yadj + (lat) * (myint) cosa - (lon) * (myint) sina)
#define X1(lon,lat) \
           (int)(xadj + (lon) * (myint) cosa + (lat) * (myint) sina)
#define AdjDepth(lon,lat) (Depth (lon, lat) < PIX45 * HEIGHT * MUL / 5000 \
                      && Depth (lon, lat) > -PIX45 * HEIGHT * MUL / 5000 ? \
                         PIX45 * HEIGHT * MUL / 5000 : Depth (lon, lat))
#define Y(lon,lat) (Display3D ? PIX45 * HEIGHT * MUL / AdjDepth (lon, lat) \
     : yadj + (int)(((lon) * (__int64) sina - (lat) * (__int64) cosa) >> 32))
#define Y2(lon,lat) (Display3D ? PIX45 * HEIGHT * MUL * (__int64)65536 / AdjDepth (lon, lat) \
 : (yadj<<16) + (int)(((lon) * (__int64) sina - (lat) * (__int64) cosa) >> 16))
#define X(lon,lat) (Display3D ? width / 2 + \
((AdjDepth (lon, lat) > 0 ? 1 : -1) * \
  (X1 (lon, lat) / 32000 - AdjDepth (lon, lat) / XFix) > 0 ? 32000 : \
(AdjDepth (lon, lat) > 0 ? 1 : -1) * \
  (X1 (lon, lat) / 32000 + AdjDepth (lon, lat) / XFix) < 0 ? -32000 : \
X1(lon,lat) / (AdjDepth (lon, lat) / XFix)) \
: xadj + (int)(((lon) * (__int64) cosa + (lat) * (__int64) sina) >> 32))
#define X2(lon,lat) (Display3D ? X(lon,lat)*65536 : \
   (xadj<<16) + (int)(((lon) * (__int64) cosa + (lat) * (__int64) sina) >> 16))
//X2 and Y2 are X and Y but 65536 times larger

#ifndef NOGTK
#ifdef ROUTE_TEST
gint RouteTest (GtkWidget * /*widget*/, GdkEventButton *event, void *)
{
  static int ptime = 0;
  ptime = time (NULL);
  int w = width;
  int perpixel = zoom / w;
  clon += lrint ((event->x - w / 2) * perpixel);
  clat -= lrint ((event->y - height / 2) * perpixel);
/*    int plon = clon + lrint ((event->x - w / 2) * perpixel);
    int plat = clat -
      lrint ((event->y - height / 2) * perpixel); */
  FollowGPSr = TRUE;
  gpsNewStruct gNew;
  gNew.fix.latitude = LatInverse (clat);
  gNew.fix.longitude = LonInverse (clon);
  DoFollowThing (&gNew);
}
#else
#ifdef USE_GEOCLUE // Not used and never worked
static void GeoclueUpdate (GeocluePosition *position,
  GeocluePositionFields fields, int timestamp,
  double latitude, double longitude, double /*altitude*/,
  GeoclueAccuracy *, gpointer /*userdata*/)
{
  if (fields & GEO_CLUE_POSITION_FIELDS_LATITUDE &&
      fields & GEO_CLUE_POSITION_FIELDS_LONGITUDE) {
    gpsNewStruct gNew;
    gNew.fix.latitude = latitude;
    gNew.fix.longitude = longitude;
    DoFollowThing (&gNew);
  }
}
#endif

#ifdef USE_GPSD
void GpsMove (gps_data_t *gps, char */*buf*/, size_t /*len*/
#if GPSD_API_MAJOR_VERSION <= 3
  , int /*level*/
#endif
  )
{
  gpsNew->fix.latitude = gps->fix.latitude;
  gpsNew->fix.longitude = gps->fix.longitude;
  if (gps->fix.time > 1e+9) { // gpsfake produces some 0 values.
    gpsNew->fix.tm[0] = llrint (gps->fix.time) % (3600 * 24) / 36000 + '0';
    gpsNew->fix.tm[1] = llrint (gps->fix.time) / 3600 % 10 + '0';
    gpsNew->fix.tm[2] = llrint (gps->fix.time) % 3600 / 600 + '0';
    gpsNew->fix.tm[3] = llrint (gps->fix.time) / 60 % 10 + '0';
    gpsNew->fix.tm[4] = llrint (gps->fix.time) % 60 / 10 + '0';
    gpsNew->fix.tm[5] = llrint (gps->fix.time) % 10 + '0';
  }
  printf ("%.6s\n", gpsNew->fix.tm);
  DoFollowThing (gpsNew);
}
#endif

//void ReceiveNmea (gpointer /*data*/, gint source, GdkInputCondition /*c*/)
/*{
  static char rx[1200];
  static unsigned got = 0;
  int cnt = read (source, rx + got, sizeof (rx) - got);
  if (cnt == 0) {
    gdk_input_remove (gpsSockTag);
    return;
  }
  got += cnt;
  
  if (ProcessNmea (rx, &got)) DoFollowThing (gpsNew);
}*/
#endif // !ROUTE_TEST

#elif defined(_WIN32) // else NOGTK
#define NEWWAY_MAX_COORD 10
struct newWaysStruct {
  int coord[NEWWAY_MAX_COORD][2], klas, cnt, oneway, bridge;
  char name[40], note[40];
} newWays[500];


int newWayCnt = 0;

BOOL CALLBACK DlgSetTagsProc (HWND hwnd, UINT Msg, WPARAM wParam,
  LPARAM lParam)
{
  if (Msg != WM_COMMAND) return FALSE;
  HWND edit = GetDlgItem (hwnd, IDC_NAME);
  if (wParam == IDC_RD1 || wParam == IDC_RD2) {
    Edit_ReplaceSel (edit, TEXT (" Road"));
  }
  if (wParam == IDC_ST1 || wParam == IDC_ST2) {
    Edit_ReplaceSel (edit, TEXT (" Street"));
  }
  if (wParam == IDC_AVE1 || wParam == IDC_AVE2) {
    Edit_ReplaceSel (edit, TEXT (" Avenue"));
  }
  if (wParam == IDC_DR1 || wParam == IDC_DR2) {
    Edit_ReplaceSel (edit, TEXT (" Drive"));
  }
  if (wParam == IDOK) {
    #ifndef _WIN32_WCE
    Edit_GetLine (edit, 0, newWays[newWayCnt].name,
      sizeof (newWays[newWayCnt].name));
    Edit_GetLine (GetDlgItem (hwnd, IDC_NOTE), 0, newWays[newWayCnt].note,
      sizeof (newWays[newWayCnt].note));
    #else
    UTF16 name[40], *sStart = name;
    int wstrlen = Edit_GetLine (edit, 0, name, sizeof (name));
    unsigned char *tStart = (unsigned char*) newWays[newWayCnt].name;
    ConvertUTF16toUTF8 ((const UTF16 **)&sStart,  sStart + wstrlen,
        &tStart, tStart + sizeof (newWays[0].name), lenientConversion);

    wstrlen = Edit_GetLine (GetDlgItem (hwnd, IDC_NOTE), 0,
      name, sizeof (name));
    sStart = name;
    tStart = (unsigned char*) newWays[newWayCnt].note;
    ConvertUTF16toUTF8 ((const UTF16 **)&sStart,  sStart + wstrlen,
        &tStart, tStart + sizeof (newWays[0].note), lenientConversion);
    #endif
    newWays[newWayCnt].oneway = IsDlgButtonChecked (hwnd, IDC_ONEWAY2);
    newWays[newWayCnt++].bridge = IsDlgButtonChecked (hwnd, IDC_BRIDGE2);
  }
  if (wParam == IDCANCEL || wParam == IDOK) {
    SipShowIM (SIPF_OFF);
    EndDialog (hwnd, wParam == IDOK);
    return TRUE;
  }
  return FALSE;
}
/*
BOOL CALLBACK DlgSetTags2Proc (HWND hwnd, UINT Msg, WPARAM wParam,
  LPARAM lParam)
{
  if (Msg == WM_INITDIALOG) {
    HWND klist = GetDlgItem (hwnd, IDC_CLASS);
    for (int i = 0; i < sizeof (klasTable) / sizeof (klasTable[0]); i++) {
      SendMessage (klist, LB_ADDSTRING, 0, (LPARAM) klasTable[i].desc);
    }
  }
  if (Msg == WM_COMMAND && wParam == IDOK) {
    newWays[newWayCnt].cnt = newWayCoordCnt;
    newWays[newWayCnt].oneway = IsDlgButtonChecked (hwnd, IDC_ONEWAY);
    newWays[newWayCnt].bridge = IsDlgButtonChecked (hwnd, IDC_BRIDGE);
    newWays[newWayCnt++].klas = SendMessage (GetDlgItem (hwnd, IDC_CLASS),
                                  LB_GETCURSEL, 0, 0);
  }
  
  if (Msg == WM_COMMAND && (wParam == IDCANCEL || wParam == IDOK)) {
    EndDialog (hwnd, wParam == IDOK);
    return TRUE;
  }
  return FALSE;
}*/

#endif // NOGTK

#ifndef ANDROID_NDK
int Scroll (GtkWidget * /*widget*/, GdkEventScroll *event, void * /*w_cur*/)
{
  if (Display3D) {
    int k = event->direction == GDK_SCROLL_UP ? 2000 : -2000;
    SetLocation (clon - lrint (sinAzimuth * k),
                 clat + lrint (cosAzimuth * k));
  }
  else {
    int w = width, h = height;
    int perpixel = zoom / w;
    if (event->direction == GDK_SCROLL_UP) zoom = zoom / 4 * 3;
    if (event->direction == GDK_SCROLL_DOWN) zoom = zoom / 3 * 4;
    SetLocation (clon + lrint ((perpixel - zoom / w) *
      (cosAzimuth * (event->x - w / 2) - sinAzimuth * (h / 2 - event->y))),
      clat + lrint ((perpixel - zoom / w) *
      (cosAzimuth * (h / 2 - event->y) + sinAzimuth * (event->x - w / 2))));
  }
  gtk_widget_queue_clear (draw);
  return FALSE;
}
#endif

int objectAddRow = -1;
#define ADD_HEIGHT 32
#define ADD_WIDTH 64
void HitButton (int b)
{
  int returnToMap = b > 0 && option <= FastestRouteNum;
  
  #ifdef _WIN32
  if (AddWayOrNode && b == 0) {
    AddWayOrNode = 0;
    option = mapMode;
    if (newWays[newWayCnt].cnt) objectAddRow = 0;
    return;
  }
  #endif
  if (b == 0) {
    listYOffset = 0;
    option = option < mapMode ? mapMode
       : option == optionMode ? mapMode : optionMode;
  }
  else if (option == StartRouteNum) {
    flon = clon;
    flat = clat;
    GosmFreeRoute ();
    shortest = NULL;
  }
  else if (option == EndRouteNum) {
    tlon = clon;
    tlat = clat;
    CallRoute (TRUE, 0, 0);
  }
  #ifdef _WIN32
  else if (option == DisplayOffNum) {
    if (CeEnableBacklight(FALSE)) {
      gDisplayOff = TRUE;
    }
  }
  else if (option == BaudRateNum) BaudRate += b * 4800 - 7200;
  #endif
  #define o(en,min,max) else if (option == en ## Num) \
    en = (en - (min) + (b == 2 ? 1 : (max) - (min) - 1)) % \
      ((max) - (min)) + (min);
  OPTIONS
  #undef o
  else {
    if (b == 2) zoom = zoom / 4 * 3;
    if (b == 1) zoom = zoom / 3 * 4;
    if (b > 0) SetLocation (clon, clat);
  }
  if (option == OrientNorthwardsNum && OrientNorthwards) {
    cosAzimuth = 1.0;
    sinAzimuth = 0.0;
  }
  if (returnToMap) option = mapMode;
}

#ifndef NOGTK
struct wayPointStruct {
  int lat, lon;
};
deque<wayPointStruct> wayPoint;

void ExtractClipboard (GtkClipboard *, const gchar *t, void */*data*/)
{
  unsigned lonFirst = FALSE, hash = 0;
  int minLat = INT_MAX, minLon = INT_MAX, maxLat = INT_MIN, maxLon = INT_MIN;
  double deg[2];
  static unsigned oldh = 0; // Sometimes an extra queue_clear is needed
  wayPoint.clear ();
  if (!t) return;
  for (; *t != '\0'; t++) {
    if (strncasecmp (t, "lat", 3) == 0) lonFirst = FALSE;
    else if (strncasecmp (t, "lon", 3) == 0) lonFirst = TRUE;

    for (int i = 0; i < 2 && (isdigit (*t) || *t == '-'); i++) {
      deg[i] = atof (t);
      while (isdigit (*t) || *t == '-') t++;
      if (*t != '.') {
        // 25S 28E or 25??58???25???S 28??7???42???E or 25 58.5 S 28 6.3 E
        while (*t != '\0' && !isdigit (*t) && !isalpha (*t)) t++;
        deg[i] += atof (t) / (deg[i] < 0 ? -60 : 60);
        while (isdigit (*t) || *t == '.' || *t == ',') t++;
        while (*t != '\0' && !isalnum (*t)) t++;
        deg[i] += atof (t) / (deg[i] < 0 ? -3600 : 3600);
        while (*t != '\0' && !isalpha (*t)) t++;
      }
      else { // -25.12 28.1
        while (*t != '\0' && (isalnum (*t) || *t == '-' || *t == '.')) t++;
        while (*t != '\0' && !isalnum (*t)) t++;
      }
      
      if (*t != '\0' && strchr ("westWEST", *t) && !isalpha (t[1])) {
        // If t[1] is a letter, then it could be something like
        // "-25.1 28.2 school".
        if (strchr ("swSW", *t)) deg[i] = -deg[i];
        lonFirst = i == (strchr ("snSN", *t) ? 1 : 0);
        for (t++; isspace (*t); t++) {}
      }
      if (deg[i] < -180 || deg[i] > 180) break;
      if (i == 0 && (strncasecmp (t, "lat", 3) == 0 ||
                     strncasecmp (t, "lon", 3) == 0)) { // lat=-25.7 lon=28.2
        for (t += 3; t != '\0' && !isalnum (*t); t++) {}
      }
      if (i == 1) { // Success !
        //printf ("%lf %lf %u\n", deg[lonFirst ? 1 : 0], deg[lonFirst ? 0 : 1],
        //  lonFirst); // Debugging
        wayPoint.push_back (wayPointStruct ());
        wayPoint.back ().lon = Longitude (deg[lonFirst ? 0 : 1]);
        wayPoint.back ().lat = Latitude (deg[lonFirst ? 1 : 0]);
        lonFirst = FALSE; // Not too sure if we should reset lonFirst here.
        hash += wayPoint.back ().lon + wayPoint.back ().lat;
        // Bad but adequate hash function.
        if (minLon > wayPoint.back ().lon) minLon = wayPoint.back ().lon;
        if (maxLon < wayPoint.back ().lon) maxLon = wayPoint.back ().lon;
        if (minLat > wayPoint.back ().lat) minLat = wayPoint.back ().lat;
        if (maxLat < wayPoint.back ().lat) maxLat = wayPoint.back ().lat;
      }
    }
  }
  if (oldh != hash && !wayPoint.empty ()) {
    clat = minLat / 2 + maxLat / 2;
    clon = minLon / 2 + maxLon / 2;
    zoom = maxLat - minLat + maxLon - minLon + (1 << 15);
    gtk_widget_queue_clear (draw);
  }
  oldh = hash;
}

int UpdateWayPoints (GtkWidget *, GdkEvent *, gpointer *)
{
  GtkClipboard *c = gtk_clipboard_get (GDK_SELECTION_PRIMARY);
  gtk_clipboard_request_text (c, ExtractClipboard, &wayPoint);
  return FALSE;
}

//gint Drag (GtkWidget * /*widget*/, GdkEventMotion *event, void * /*w_cur*/)
/*
{
  if ((option == mapMode || option == optionMode) &&
          (event->state & GDK_BUTTON1_MASK)) {
    if (firstDrag[0] >= 0) gdk_draw_drawable (draw->window,
      draw->style[0].fg_gc[0], draw->window, 
      0, 0, lrint (event->x) - lastDrag[0], lrint (event->y) - lastDrag[1],
      width, height);
    lastDrag[0] = lrint (event->x);
    lastDrag[1] = lrint (event->y);
    if (firstDrag[0] < 0) {
      memcpy (firstDrag, lastDrag, sizeof (firstDrag));
      pressTime = event->time;
    }
  }
  return FALSE;
}*/

GtkWidget *bar;
int UpdateProcessFunction(void */*userData*/, double t, double d,
                                          double /*ultotal*/, double /*ulnow*/)
{
  gdk_threads_enter ();
  gtk_progress_set_value (GTK_PROGRESS (bar), d * 100.0 / t);
  gdk_threads_leave ();
  return 0;
}

void *UpdateMapThread (void *n)
{
  CURL *curl;
  FILE *outfile;
 
  curl = curl_easy_init();
  if(curl) {
    outfile = fopen("tmp.zip", "w");
 
    // string zip ((string)(char*)n + ".zip", cmd ("unzip " + zip);
    string url ("http://dev.openstreetmap.de/gosmore/" + (string)(char*)n + ".zip");
    curl_easy_setopt(curl, CURLOPT_URL, url.c_str ());
    curl_easy_setopt(curl, CURLOPT_WRITEDATA, outfile);
    curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, fwrite); //DefaultCurlWrite);
    curl_easy_setopt(curl, CURLOPT_READFUNCTION, fread); //my_read_func);
    curl_easy_setopt(curl, CURLOPT_NOPROGRESS, 0L);
    curl_easy_setopt(curl, CURLOPT_PROGRESSFUNCTION, UpdateProcessFunction);
    curl_easy_setopt(curl, CURLOPT_PROGRESSDATA, ""); // Bar);
 
    curl_easy_perform(curl);
 
    fclose(outfile);
    if (system ("unzip tmp.zip") == -1) fprintf (stderr, "Error:system()\n");
    string dst ((string)(char*)n + ".pak");
    rename ("gosmore.pak", dst.c_str ());
    unlink ("tmp.zip");
    gdk_threads_enter ();
    gtk_progress_bar_set_text (GTK_PROGRESS_BAR (bar), "Done");
/*  ChangePak (NULL, clon ^ 0x80000000, clat);
    Expose () I don't think it will work in this thread. SEGV. */
    
    gdk_threads_leave ();

    curl_easy_cleanup(curl);
  }
  free (n); // Malloced in one thread freed in another.
  return NULL; 
}

#endif
#if defined (_WIN32) && !defined (_WIN32_WCE)
DWORD WINAPI UpdateMapThread (LPVOID n)
{
  WSADATA d;
  WSAStartup (MAKEWORD (1, 1), &d);
  struct hostent *he = gethostbyname ("dev.openstreetmap.de");
  int s = socket (AF_INET, SOCK_STREAM, 0);
  struct sockaddr_in name;
  if (he && s != INVALID_SOCKET) {
    memset (&name, 0, sizeof (name));
    name.sin_family = AF_INET;
    name.sin_port = htons (80);
    memcpy (&name.sin_addr, he->h_addr_list[0], 4);
    string header = string ("GET /gosmore/") + string ((char*) n, 16) + 
                 ".zip HTTP/1.0\r\n"
                 "Host: dev.openstreetmap.de\r\n"
                 "\r\n";
    if (connect (s, (sockaddr *) &name, sizeof (name)) == 0 &&
        send (s, header.c_str (), strlen (header.c_str ()), 0) > 0) {
      char reply[4096], *ptr = reply, *lnl = NULL;
      int code, len, cnt = recv (s, reply, sizeof (reply), 0);
      sscanf (reply, "%*s %d", &code);
      while (cnt > 0 && ptr[0] != '\n' || !lnl) {
        if (cnt > 16 && (ptr[0] == '\n' || ptr[0] == '\r') &&
            strnicmp (ptr + 1, "Content-Length:", 15) == 0) {
          len = atoi (ptr + 16);
        }
        lnl = *ptr == '\n' ? ptr : *ptr == '\r' ? lnl : NULL;
        cnt--;
        ptr++;
        if (cnt < 1) {
          memmove (reply, ptr, cnt);
          ptr = reply;
          cnt += recv (s, ptr, sizeof (reply) - cnt, 0);
        }
      }
      if (cnt-- > 0) { // Get rid of the '\n'
        ptr++; // Get rid of the '\n'
        FILE *z = fopen ("tmp.zip", "wb");
        code = 0;
        do {
          fwrite (ptr, cnt, 1, z);
          if ((code + cnt) / (len / 1000 + 1) > code / (len / 1000 + 1)) {
            PostMessage (mWnd, WM_USER + 2, 0, (code + cnt) / (len / 1000 + 1));
          }
          code += cnt;
          ptr = reply;
        } while ((cnt = recv (s, reply, sizeof (reply), 0)) > 0);
        fclose (z);
        STARTUPINFO si;
        PROCESS_INFORMATION pi;
        ZeroMemory (&si, sizeof (si));
        ZeroMemory (&pi, sizeof (pi));
        si.cb = sizeof (si);
        CreateProcess ("7z.exe", "7z x -y tmp.zip", NULL, NULL,
          FALSE, 0, NULL, NULL, &si, &pi);
        WaitForSingleObject (pi.hProcess, INFINITE);
        CloseHandle (pi.hProcess);
        string dst (string ((char*) n, 16) + ".pak");
        rename ("gosmore.pak", dst.c_str ());
        _unlink ("tmp.zip");
      }
    }
    else closesocket (s);
  }
  free (n);
  PostMessage (mWnd, WM_USER + 2, 0, 0);
  return 0;
}
#endif

/* These macros calling macros may result in very long bloated code and
   inefficient machine code, depending on how well the compiler optimizes.
*/

#define CompactOptions ((width * height < 400 * 400))
int ListXY (int cnt, int isY)
{ // Returns either the x or the y for a certain list item
  int max = mapMode; //option == optionMode ? mapNode :
  int w = CompactOptions ? 70 : 105, h = CompactOptions ? 45 : 80;
  while ((width/w) * (height/h - 1) > max) {
    w++;
    h++;
  }
  return isY ? cnt / (width / w) * h + h / 2 - listYOffset :
    (cnt % (width / w)) * w + w / 2;
}

#ifdef ANDROID_NDK // OPENGL
typedef jint HDC;
#define gdk_draw_drawable(win,dgc,sdc,x,y,dx,dy,w,h) \
	do { int crop[] = { x, y + h, w, -h }; \
	glEnable(GL_BLEND); \
	glEnable(GL_TEXTURE_2D); \
	glTexParameteriv(GL_TEXTURE_2D, GL_TEXTURE_CROP_RECT_OES, crop); \
	glDrawTexiOES(dx, height - dy, 0, crop[2], h); \
	glDisable(GL_TEXTURE_2D); \
	glDisable(GL_BLEND);\
	} while (0) // TODO Try to get a glMatrix that match OES coordinates
#define gdk_draw_line(win,gc,sx,sy,dx,dy) \
  do { GdkPoint l[2] = { sx, sy, dx, dy }; \
    glVertexPointer (2, GL_SHORT, 0, l); \
    glDrawArrays (GL_LINES, 0, 2); } while (0)

static jmethodID drawTeks = 0;

#elif !defined(NOGTK)
typedef GdkGC *HDC;

static GdkGC *maskGC = NULL, *fg_gc;
static GdkBitmap *mask = NULL;
// create bitmap for generation the mask image for icons
// all icons must be smaller than these dimensions
static GdkBitmap *maskicon = NULL;
static GdkPixmap *icons = NULL;

#else
HDC icons, maskDc;
HFONT sysFont;
LOGFONT logFont;

#define gtk_combo_box_get_active(x) 1
#define gdk_draw_drawable(win,dgc,sdc,x,y,dx,dy,w,h) \
  BitBlt (dgc, dx, dy, w, h, maskDc, x, y, SRCAND); \
  BitBlt (dgc, dx, dy, w, h, sdc, x, y, SRCPAINT)
#define gdk_draw_line(win,gc,sx,sy,dx,dy) \
  do { MoveToEx (gc, sx, sy, NULL); LineTo (gc, dx, dy); } while (0)

#endif

static HDC mygc = NULL, iconsgc = NULL;

#ifdef PANGO_VERSION
PangoContext *pc;
PangoLayout  *pl;
#endif

void DrawString (int x, int y, const char *optStr)
{
  #ifdef PANGO_VERSION
  PangoMatrix mat = PANGO_MATRIX_INIT;
  pango_context_set_matrix (pc, &mat);
  pango_layout_set_text (pl, optStr, -1);
  gdk_draw_layout (GDK_DRAWABLE (draw->window),
                     fg_gc /*draw->style->fg_gc[0]*/, x, y, pl);
  #elif defined (_WIN32)
  SelectObject (mygc, sysFont);
  const unsigned char *sStart = (const unsigned char*) optStr;
  UTF16 wcTmp[70], *tStart = (UTF16 *) wcTmp;
  if (ConvertUTF8toUTF16 (&sStart,  sStart + strlen (optStr), &tStart,
           tStart + sizeof (wcTmp) / sizeof (wcTmp[0]), lenientConversion)
      == conversionOK) {
    ExtTextOutW (mygc, x, y, 0, NULL, (wchar_t*) wcTmp, tStart - wcTmp, NULL);
  }
  #endif
}

void DrawPoI (int dstx, int dsty, int *icon)
{
  if (icon[2] == 0 || dstx < -icon[2] || dsty < -icon[3] ||
    dstx > width + icon[2] ||
    // GDK need these tests for the Start&EndRoute markers
    dsty > height + icon[3]) return;
  #ifndef NOGTK
  // for gdk we first need to extract the portion of the mask
  if (!maskicon) maskicon = gdk_pixmap_new(NULL, 100, 100, 1);
  gdk_draw_drawable (maskicon, maskGC, mask,
                     icon[0], icon[1], 0, 0,
                     icon[2], icon[3]);
  // and set the clip region using that portion
  gdk_gc_set_clip_origin(iconsgc, dstx - icon[2] / 2, dsty - icon[3] / 2);
  gdk_gc_set_clip_mask(iconsgc, maskicon);
  #endif
  gdk_draw_drawable (draw->window, iconsgc, icons,
    icon[0], icon[1], dstx - icon[2] / 2, dsty - icon[3] / 2,
    icon[2], icon[3]);
}

void GeoSearch (const char *key)
{
  const char *comma = strchr (key, ',');
  if (!comma) comma = strstr (key, " near ");
  if (comma) {
    const char *cName = comma + (*comma == ',' ? 1 : 6);
    string citi = string ("city:") + (cName + strspn (cName, " "));
    const char *tag = gosmData + *GosmIdxSearch (citi.c_str (), 0);
    while (*--tag) {}
    ChangePak (NULL, ((wayType *)tag)[-1].clon, ((wayType *)tag)[-1].clat);
    string xkey = string (key, comma - key);
    //printf ("%s tag=%s\nxke %s\n", cName, tag + 1, xkey.c_str ());
    GosmSearch (((wayType *)tag)[-1].clon, ((wayType *)tag)[-1].clat, xkey.c_str ());
  }
  else GosmSearch (clon, clat, key);
}


#if defined (ANDROID_NDK)
/*extern "C" void Java_org_osmu_gosmore_MapView_setLocation (
		JNIEnv*  env, jobject thiz, jdouble lon, jdouble lat)
{
  LG clon = Longitude (lon);
  clat = Latitude (lat);
}*/

#define DisplaySearchResults(); // Never in searchMode, no-op
extern "C" void Java_org_osmu_gosmore_Search_search (
		JNIEnv*  env, jobject thiz, jstring jquery)
{
	static jmethodID meth = 0;
	static int started = 0;
	if (!started) {
	  started = 1;
	  jclass cls=env->GetObjectClass(thiz);
	  //cls =(jclass)( env->NewGlobalRef(jcls) );
      //cls = env->FindClass(
	//		  "org/osmu/gosmore/MyClb");

	  meth = env->GetMethodID(cls, "searchResult",
			  "(IIIIDIILjava/lang/String;DDI)V");
//	  env->ExceptionClear();
	  /*  if (method != 0)*/
//	    env->ExceptionClear();
	}
	const char *query = env->GetStringUTFChars(jquery, NULL);
	GeoSearch (query);
	LG env->ReleaseStringUTFChars(jquery, query);

#else
void DisplaySearchResults (void)
{
#endif
	    for (int i = 0, y = SearchSpacing / 2; i < searchCnt && gosmSstr[i];
	             i++, y += SearchSpacing) {
	      double dist = sqrt (double (Sqr ((__int64) clon - gosmSway[i]->clon) +
	          Sqr ((__int64) clat - gosmSway[i]->clat))) * (20000 / 2147483648.0) *
	        cos (LatInverse (clat) * (M_PI / 180));

	      int x = SearchSpacing + 70;
	      __int64 lx = X (gosmSway[i]->clon, gosmSway[i]->clat) - width / 2;
	      __int64 ly = Y (gosmSway[i]->clon, gosmSway[i]->clat) - height / 2;
	      double norm = lx || ly ? sqrt (double(lx * lx + ly * ly)) / 64 : 1;
	      int u = lrint (lx / norm), v = lrint (ly / norm);
	      string s (gosmSstr[i], strcspn (gosmSstr[i], "\n"));
	      char *name = (char *)(gosmSway[i] + 1) + 1;
	      if (name != gosmSstr[i]) s += " (" +
	                     string (name, strcspn (name, "\n")) + ")";
	      #ifdef ANDROID_NDK
	      jstring js = env->NewStringUTF (s.c_str ());
	      int *z = Style (gosmSway[i])->x + 4 * IconSet;
              env->CallVoidMethod (thiz, meth, z[0], z[1], z[2], z[3],
                          dist, u, v, js,
        		  LonInverse (gosmSway[i]->clon),
        		  LatInverse (gosmSway[i]->clat),
        		  gosmSway[i]->dlat + gosmSway[i]->dlon + (1 << 15));
	      #else
	      DrawPoI (SearchSpacing / 2, y, Style (gosmSway[i])->x + 4 * IconSet);

	      char distance[10]; // Formula inaccurate over long distances hence "Far"
	      sprintf (distance, dist > 998 ? "Far" : dist > 1 ? "%.0lf km" :
	        "%.0lf m", dist > 1 ? dist : dist * 1000);
	      DrawString (SearchSpacing + 33 - 11 * strcspn (distance, " "), y - 10,
	        distance); // Right adjustment is inaccurate
	      gdk_draw_line (draw->window, mygc, x + u / 8, y + v / 8,
	        x - u / 8, y - v / 8);
	      gdk_draw_line (draw->window, mygc, x + u / 8, y + v / 8,
	        x + u / 12 + v / 20, y - u / 20 + v / 12);
	      gdk_draw_line (draw->window, mygc, x + u / 8, y + v / 8,
	        x + u / 12 - v / 20, y + u / 20 + v / 12);

	      DrawString (SearchSpacing + x, y - 10, s.c_str ());

	      gdk_draw_line (draw->window, mygc, 0, y + SearchSpacing / 2,
	        width, y + SearchSpacing / 2);
	      #endif
	    }
}


#ifndef _WIN32_WCE
int HandleKeyboard (int, int, int)
{
#else
int HandleKeyboard (int event, int ex, int ey)
{ // Some WinCE devices, like the Mio Moov 200 does not have an input method
  // and any call to activate it or set the text on an EDIT or STATIC (label)
  // control will crash the application. So under WinCE we default to our
  // own keyboard.
  //
  // Draw our own keyboard (Expose Event) or handle the key (Click)
  if (Keyboard) return FALSE; // Using the Windows keyboard
  // DrawString (30, 5, searchStr.c_str ()); // For testing under GTK
  if (!event) {
    RECT r;
    r.left = 0;
    r.top = height - 32 * 3;
    r.right = width;
    r.bottom = height;
    FillRect (mygc, &r, (HBRUSH) GetStockObject (WHITE_BRUSH)); //brush[KeyboardNum]);
    SelectObject (mygc, GetStockObject (BLACK_PEN));
  }

  const char *kbLayout[] = { "qwertyuiop", "asdfghjkl", " zxcvbnm,$" };
  for (int i = 0; i < 3; i++) {
    for (int j = 0; kbLayout[i][j] != '\0'; j++) {
      int hb = width / strlen (kbLayout[0]) / 2, ys = 16;
      int x = (2 * j + (i & 1)) * hb, y = height - (3 - i) * ys * 2;
      if (event && ey >= y && ey < y + ys + ys && ex < x + hb + hb) {
        if (kbLayout[i][j] != '$') searchStr += kbLayout[i][j];
        else if (searchStr.length () > 0) searchStr.erase (searchStr.length () - 1, 1);
        logprintf ("'%s'\n", searchStr.c_str());
        GeoSearch (searchStr.c_str ());
        gtk_widget_queue_clear (draw);
        return TRUE;
      }
      if (!event) {
        if (j > 0) gdk_draw_line (draw->window, mygc, x, y, x, y + ys + ys);
        else gdk_draw_line (draw->window, mygc, 0, y, width, y);
        string chr = string ("") + kbLayout[i][j];
        if (kbLayout[i][j] == ' ') DrawString (x + hb - 5, y + ys / 2, "[ ]");
        else if (kbLayout[i][j] != '$') DrawString (x + hb, y + ys / 2, chr.c_str ());
        else { // Now draw the backspace symbol :
          gdk_draw_line (draw->window, mygc, x + 3, y + ys, x + hb + hb - 3, y + ys);
          gdk_draw_line (draw->window, mygc, x + 3, y + ys, x + hb, y + 3);
          gdk_draw_line (draw->window, mygc, x + 3, y + ys, x + hb, y + ys + ys - 3);
        }
      }
    }
  }
  #endif
  return FALSE;
}

static int lastRelease = 0, oldx = -1, oldy, pressTime = -1;

#if !defined (ANDROID_NDK)
/*extern "C" int Java_org_osmu_gosmore_MapRenderer_mouseEv (
		JNIEnv*  env, jobject thiz, jint x, jint y, jint evTime,
		jint button, jboolean click)*/
int MouseEv (int x, int y, int evTime, int button, int click)
{
  // Anything that covers more than 3 pixels in either direction is a drag.
  LG gtk_widget_queue_clear (draw); 
  int w = width, h = height;
  int isDrag = //DebounceDrag ?
        pressTime >= 0 && (lastRelease + 200 > evTime ||
                                pressTime + 200 < evTime);
//        : pressTime >= 0 && (abs((int)(firstDrag[0] - event->x)) > 3 ||
//                                abs((int)(firstDrag[1] - event->y)) > 3;
  // logprintf("Click (isDrag = %d): firstDrag = %d,%d; event = %d,%d\n",
  // 	    isDrag, firstDrag[0], firstDrag[1], event->x, event->y);
  if (pressTime == -1) pressTime = evTime;
  if (click) pressTime = -1;
  if (click) lastRelease = evTime;
  if (isDrag) {
    if (option == optionMode) {
      listYOffset = max (0, listYOffset + (int)lrint (oldy - y));
    }
    if (option == mapMode) {
      int lon = clon + lrint (zoom / w *
        (cosAzimuth * (Display3D ? 0 : oldx - x) - sinAzimuth * (y - oldy)));
      int lat = clat + lrint (zoom / w *
        (cosAzimuth * (y - oldy + sinAzimuth * (Display3D ? 0 : oldx - x))));
      if (Display3D) {
        double newa = atan2 (sinAzimuth, cosAzimuth) - (oldx-x) * M_PI / 580;
        cosAzimuth = cos (newa);
        sinAzimuth = sin (newa);
      }
      SetLocation (lon, lat);
    }
  }
  oldx = x;
  oldy = y;
  if (!click || isDrag) return 0;

  if (ButtonSize <= 0) ButtonSize = 4;
  int b = (height - lrint (y)) / (ButtonSize * 20);
  if (objectAddRow >= 0) {
    int perRow = (w - ButtonSize * 20) / ADD_WIDTH;
    if (x < w - ButtonSize * 20) {
      #ifdef _WIN32
      newWays[newWayCnt].klas = objectAddRow + x / ADD_WIDTH +
                                y / ADD_HEIGHT * perRow;
      SipShowIM (SIPF_ON);
      if (DialogBox (hInst, MAKEINTRESOURCE (IDD_SETTAGS), NULL,
          (DLGPROC) DlgSetTagsProc)) {} //DialogBox (hInst,
          //MAKEINTRESOURCE (IDD_SETTAGS2), NULL, (DLGPROC) DlgSetTags2Proc);
      newWays[newWayCnt].cnt = 0;
      #endif
      objectAddRow = -1;
    }
    else objectAddRow = y * (restriction_no_right_turn / perRow + 2) /
                              height * perRow;
  }
  else if (x > w - ButtonSize * 20 && b <
      (Layout >
       (MenuKey == 0 || option != mapMode ? 0 : 1) ? 3 : 0)) HitButton (b);
  else if (option == optionMode) {
    if (1) {
      for (int best = 9999, i = 0; i < mapMode; i++) {
        int d = lrint (fabs (double(ListXY (i, FALSE) - x)) +
                       fabs (double(ListXY (i, TRUE) - y)));
        if (d < best) {
          best = d;
          option = i;
        }
      }
      if (option <= OrientNorthwardsNum) HitButton (1);
      if (option >= ViewOSMNum && option <= ViewGMapsNum) {
        char lstr[200];
        int zl = 0;
        while (zl < 32 && (zoom >> zl)) zl++;
        setlocale (LC_NUMERIC, "C");
        sprintf (lstr,
         option == ViewOSMNum ? "%sopenstreetmap.org/?lat=%.5lf&lon=%.5lf&zoom=%d%s" :
         option == EditInPotlatchNum ? "%sopenstreetmap.org/edit?lat=%.5lf&lon=%.5lf&zoom=%d%s" :
         "%smaps.google.com/?ll=%.5lf,%.5lf&z=%d%s",
        #ifdef WIN32
          "http://", LatInverse (clat), LonInverse (clon), 33 - zl, "");
        #ifndef _WIN32_WCE
        ShellExecute (NULL, TEXT ("open"), lstr, NULL, NULL,
          SW_SHOWNORMAL);
        #else
        MessageBox (NULL, TEXT ("Not implemented"), TEXT ("Error"), MB_APPLMODAL|MB_OK);
        #endif
        #else
          "gnome-open 'http://", LatInverse (clat), LonInverse (clon), 33 - zl, "'");
        option = system (lstr); // Shut up GCC w.r.t. return value
        #endif
        option = mapMode;
      }
      #ifndef NOGTK
      else if (option == UpdateMapNum) {
        struct stat s;
        if (currentBbox[0] == '\0') {
          gtk_dialog_run (GTK_DIALOG (gtk_message_dialog_new (NULL,
            GTK_DIALOG_MODAL, GTK_MESSAGE_ERROR, GTK_BUTTONS_OK,
            "Error:\n"
            "Gosmore is running with a custom map\n"
            "Download aborted.")));
        }
        else if (stat (currentBbox, &s) == 0 &&
           (s.st_mtime > time (NULL) - 3600*24*7 ||
            s.st_ctime > time (NULL) - 3600 * 24 * 7)) {
          gtk_dialog_run (GTK_DIALOG (gtk_message_dialog_new (NULL,
            GTK_DIALOG_MODAL, GTK_MESSAGE_ERROR, GTK_BUTTONS_OK,
            "Error:\n"
            "%s has changed during the last 7 days,\n"
            "and is most likely up-to-date.\n"
            "Download aborted.", currentBbox)));
        }
        else {
          string msg (string ("Downloading ") + currentBbox);
          gtk_progress_bar_set_text (GTK_PROGRESS_BAR (bar), msg.c_str ());
          g_thread_create (&UpdateMapThread, strndup (currentBbox, 16), FALSE, NULL);
        }
        option = mapMode;
      }
      #else
      #if defined (_WIN32) && !defined (_WIN32_WCE)
      else if (option == UpdateMapNum) {
        struct stat s;
        if (currentBbox[0] == '\0') {
          MessageBox (NULL, "Error:\n"
            "Gosmore is running with a custom map\n"
            "Download aborted.", "Error", MB_APPLMODAL|MB_OK);
        }
        else if (stat (currentBbox, &s) == 0 &&
           (s.st_mtime > time (NULL) - 3600*24*7 ||
            s.st_ctime > time (NULL) - 3600 * 24 * 7)) {
          MessageBox (NULL, "Error:\n"
            "The .pak file has changed during the last 7 days,\n"
            "and is most likely up-to-date.\n"
            "Download aborted.", "Error", MB_APPLMODAL|MB_OK);
        }
        else {
          DWORD threadId;
          CreateThread (NULL, 0, UpdateMapThread, strdup (currentBbox), 0,
            &threadId);
        }
        option = mapMode;
      }
      #endif
      #endif
    }
  }
  else if (option == searchMode) {
    int row = y / SearchSpacing;
    if (!HandleKeyboard (TRUE, x, y) && row < searchCnt && gosmSstr[row]) {
      SetLocation (gosmSway[row]->clon, gosmSway[row]->clat);
      zoom = gosmSway[row]->dlat + gosmSway[row]->dlon + (1 << 15);
      if (zoom <= (1 << 15)) zoom = Style (gosmSway[row])->scaleMax;
      gtk_toggle_button_set_active (GTK_TOGGLE_BUTTON (followGPSr), FALSE);
      FollowGPSr = FALSE;
      option = mapMode;
      highlight = string (gosmSstr[row], strcspn (gosmSstr[row], "\n"));
      gtk_widget_queue_clear (draw);
    }
  }
  else {
    #ifdef ROUTE_TEST
    if (event->state & GDK_SHIFT_MASK) {
      return RouteTest (NULL /*widget*/, event, NULL /*para*/);
    }
    #endif
    int perpixel = zoom / w, dx = x - w / 2, dy = h / 2 - y;
    int lon = clon + lrint (perpixel *
      (cosAzimuth * (Display3D ? 0 : dx) - sinAzimuth * dy));
    int lat = clat + lrint (perpixel *
      (cosAzimuth * dy + sinAzimuth * (Display3D ? 0 : dx)));
    if (button == 1) {
      if (Display3D) {
        double newa = atan2 (sinAzimuth, cosAzimuth) - dx * M_PI / 580;
        cosAzimuth = cos (newa);
        sinAzimuth = sin (newa);
      }
      SetLocation (lon, lat);

      #ifdef _WIN32
      if (AddWayOrNode && newWays[newWayCnt].cnt < NEWWAY_MAX_COORD) {
        newWays[newWayCnt].coord[newWays[newWayCnt].cnt][0] = clon;
        newWays[newWayCnt].coord[newWays[newWayCnt].cnt++][1] = clat;
      }
      #endif
      gtk_toggle_button_set_active (GTK_TOGGLE_BUTTON (followGPSr), FALSE);
      FollowGPSr = 0;
    }
    else if (button == 2) {
      flon = lon;
      flat = lat;
      GosmFreeRoute ();
      shortest = NULL;
    }
    else {
      tlon = lon;
      tlat = lat;
      CallRoute (TRUE, 0, 0);
    }
  }
  return FALSE;
}
#endif

#define STATEINFO OPTIONS o (clat, 0, 0) o (clon, 0, 0) \
 o (sinAzimuth, 0, 0) o (cosAzimuth, 0, 0) o (zoom, 0, 0) o (option, 0, 0) \
 o (width, 0, 0) o (height, 0, 0)
#define o(x,min,max) sizeof (x) +
static const size_t stateSize = STATEINFO 0;
#undef o

#if 0
typedef struct {  /* For 3D, a list of segments is generated that is */
  ndType *nd;     /* z-sorted and rendered. */
  int f[2], t[2], tlen;
  char *text;
} renderNd;
#endif

/*inline double YDivisor (double x) { return x; }
inline double Clamp (double x) { return x; }*/
/*
inline int YDivisor (int y)
{
  return y > 5256 || y < -5256 ? y : y < 0 ? -5256 : 5256;
}
*/
/*
inline int Clamp2 (int x)
{
  return x < -32760 ? -32760 : x > 32760 ? 32760 : x;
}*/

void Draw3DLine (int sx, int sy, int dx, int dy)
{
  if (Display3D) {
    if (sy < 0) {
      if (dy < 0) return;
      sx = dx + (dx - sx) * (/*height*/ 1024 - dy) / (dy - sy);
      sy = /*height*/ 1024;
    }
    else if (dy < 0) {
      dx = sx + (sx - dx) * (/*height*/ 1024 - sy) / (sy - dy);
      dy = /*height*/ 1024;
    }
  }
  gdk_draw_line (draw->window, mygc, sx, sy, dx, dy);
}

int TestOrSet (int *bits, int set, int x0, int y0, int ax, int ay,
       int bx, int by)
/* This funtion manipulates bits in a rectangular area in a bitfield. (x0, y0)
   is one of the corners. (ax,ay) and (bx,by) are to vectors that define
   two of the sides. ay > 0
   The precise operation is determined by the 'set' boolean
*/
{
  if (by < 0) { // Top not given, so we find it first.
    x0 += bx;
    y0 += by;
    int nx = ax, ny = ay;
    ax = -bx;
    ay = -by;
    bx = nx;
    by = ny;
  }
  if (y0 < 0 || y0 + ay + by > height ||
      x0 + ax < 0 || x0 + bx > width) return TRUE;
  // Do not place anything offscreen.
  const int shf = 9;
  x0 <<= shf;
  int x1 = x0, d0 = (ax << shf) / (ay + 1), d1 = (bx << shf) / (by + 1);
  int bpr = (width + 31) / 32;
  bits += bpr * y0;
  for (int cnt = ay + by; cnt > 0; cnt--) {
    x0 += d0;
    x1 += d1;
    for (int i = x0 >> shf; i < (x1 >> shf); i++) {
      if (set) bits[i >> 5] |= 1 << (i & 31);
      else if (bits[i >> 5] & (1 << (i & 31))) return TRUE;
    } // This loop can be optimized
    //gdk_draw_line (draw->window, mygc, x0 >> shf, y0, x1 >> shf, y0);
    // Uncomment this line to see if we're testing the right spot
    // (and it looks kind of interesting )
    bits += bpr;
    if (cnt == by) d0 = (bx << shf) / by;
    if (cnt == ay) d1 = (ax << shf) / ay;
    y0++;
  }
  return FALSE;
}

/* Choose the part of the way that is best to render the text on. Currently
   the straightest part. We look at for the two points where the direct
   distance is long enough and it is also the closest to the distance
   between the two points along the curve. 
   TODO: Use the number of junctions between the two points (T / 4 way)
   TODO: Consider moments (standard deviation)
*/
struct linePtType {
  int x, y, cumulative;
  linePtType (int _x, int _y, int _c) : x (_x), y (_y), cumulative (_c) {}
};

struct text2Brendered {
  const char *s; // Either \n or \0 terminated
  int x, y, x2, y2, dst;
  text2Brendered (void) {}
};

void ConsiderText (queue<linePtType> *q, int finish, int len, int *best,
  text2Brendered *t)
{
  while (!q->empty ()) {
    int clip[2] = { 0, 0 }; // Used with q->front or q->back is off-screen
    int dx = q->back ().x - q->front ().x, dy = q->back ().y - q->front ().y;
    if (q->size () == 2) { // cumulative can't cope with clipping, so we
                           // only do it when we know detour will be 0
      LG for (int i = 0; i < 2; i++) {
        LG linePtType *f = !i ? &q->front () : &q->back ();
        LG if (f->x < 10 && dx != 0) clip[i] = max (clip[i], 256 * (10 - f->x) / (i ? -dx : dx));
        if (f->y < 10 && dy != 0) clip[i] = max (clip[i], 256 * (10 - f->y) / (i ? -dy : dy));
        int r2x = f->x - width + 10;
        LG if (r2x > 0 && dx != 0) clip[i] = max (clip[i], 256 * r2x / (i ? dx : -dx));
        int r2y = f->y - height + 10;
        if (r2y > 0 && dy != 0) clip[i] = max (clip[i], 256 * r2y / (i ? dy : -dy));
      }
    }
    int dst = isqrt (Sqr (dx) + Sqr (dy)) * (256 - clip[0] - clip[1]) / 256;
    int detour = q->size () == 2 ? 0 : q->back ().cumulative - q->front ().cumulative - dst;
    if (detour <= *best) {
      LG if (dst * DetailLevel > len * 14) {
        t->x = q->front ().x + dx * clip[0] / 256;
        t->y = q->front ().y + dy * clip[0] / 256;
        t->x2 = q->back ().x - dx * clip[1] / 256;
        t->y2 = q->back ().y - dy * clip[1] / 256;
        t->dst = dst;
        *best = detour;
      }
      if (!finish) break;
    }
    q->pop ();
  } // While shortening the queue
}

int WaySizeCmp (ndType **a, ndType **b)
{
  return Way (*a)->dlat * (__int64) Way (*a)->dlon >
         Way (*b)->dlat * (__int64) Way (*b)->dlon ? 1 : -1;
}

vector<short> casingCmds;
#define CASING_MAX 20
static short casing[10 + CASING_MAX * 2];
static int casingx, casingy, casingPts = 0;
void MoveTo (int x, int y)
{
#ifdef ANDROID_NDK // OPENGL
  if (casingPts) {
    casing[6] = casing[4];
    casing[7] = casing[5];
    int i, circ = min ((abs(casing[2] - casing[6]) + abs (casing[3] - casing[7])) / 3, CASING_MAX);
    for (i = 1; i < circ; i++) {
      casing[i + i + 6] = casing[6] +
        (casing[2] - casing[6]) * (1 - cos (i * M_PI / circ))/2 -
        (casing[3] - casing[7]) * sin (i * M_PI / circ)/2;
      casing[i + i + 7] = casing[7] +
        (casing[3] - casing[7]) * (1 - cos (i * M_PI / circ))/2 +
        (casing[2] - casing[6]) * sin (i * M_PI / circ)/2;
    }
    casing[i + i + 6] = casing[2];
    casing[i + i + 7] = casing[3];
    glVertexPointer (2, GL_SHORT, 0, casing + 6);
    casingCmds.push_back (i + 1);
    casingCmds.push_back (0); // pad
    casingCmds.insert (casingCmds.end(), casing + 6, casing + 8 + i + i);
    //if (vol) glDrawArrays (GL_TRIANGLE_FAN, 0, i + 1);
    glDrawArrays (GL_LINE_STRIP, 0, i + 1);
  }
#endif
  casingx = x;
  casingy = y;
  casingPts = 0;
}

void LineTo (int x, int y, int width)
{
#ifdef ANDROID_NDK // OPENGL
  int d = sqrt (double((x - casingx) * (x - casingx) +
                       (y - casingy) * (y - casingy))), i;
  casing[0] = casingx + (Display3D ? (y - casingy) * casingy * width / PIX45
    : (y - casingy) * width) / (d + d);
  casing[1] = casingy + (Display3D ? (casingx - x) * casingy * width / PIX45
    : (casingx - x) * width) / (d + d);
  casing[6] = casing[0] - (Display3D ? (y - casingy) * casingy * width / PIX45
    : (y - casingy) * width) / d;
  casing[7] = casing[1] - (Display3D ? (casingx - x) * casingy * width / PIX45
    : (casingx - x) * width) / d;
  if (!casingPts++) {
    int circ = min ((abs(casing[0] - casing[6]) + abs (casing[1] - casing[7])) / 3, CASING_MAX);
    for (i = 1; i < circ; i++) {
      casing[i + i + 6] = casing[6] + 
        (casing[0] - casing[6]) * (1 - cos (i * M_PI / circ))/2 +
        (casing[1] - casing[7]) * sin (i * M_PI / circ)/2;
      casing[i + i + 7] = casing[7] +
        (casing[1] - casing[7]) * (1 - cos (i * M_PI / circ))/2 -
        (casing[0] - casing[6]) * sin (i * M_PI / circ)/2;
    }
  }
  else {
    memcpy (casing + 8, (casing[2] - casingx) * (casingx - x) >
      (casing[3] - casingy) * (y - casingy) ? casing + 2 : casing + 4, sizeof (casing[2]) * 2);
    i = 2;
  }
  
  casing[2] = x + (Display3D ? (y - casingy) * y * width / PIX45
    : (y - casingy) * width) / (d + d);
  casing[3] = y + (Display3D ? (casingx - x) * y * width / PIX45
    : (casingx - x) * width) / (d + d);
  casing[4] = casing[2] - (Display3D ? (y - casingy) * y * width / PIX45
    : (y - casingy) * width) / d;
  casing[5] = casing[3] - (Display3D ? (casingx - x) * y * width / PIX45
    : (casingx - x) * width) / d;
  glVertexPointer (2, GL_SHORT, 0, casing);
  casingCmds.push_back (i + 3);
  casingCmds.push_back (0); // pad
  casingCmds.insert (casingCmds.end(), casing, casing + i + i + 6);
  //if (vol) glDrawArrays (GL_TRIANGLE_FAN, 0, i + 3);
  glDrawArrays (GL_LINE_LOOP, 0, i + 3);  
#else
  if (width) {} // Suppress warning from compiler
  gdk_draw_line (GDK_DRAWABLE (draw->window), mygc, casingx, casingy, x, y);
#endif
  casingx = x;
  casingy = y;
}

void FinishCasing ()
{
  #ifdef ANDROID_NDK // OPENGL
  MoveTo (0, 0); // Draw round ending for last line
  glVertexPointer (2, GL_SHORT, 0, &casingCmds[0] + 2);
  for (int i = 0; i < casingCmds.size ();
      i += casingCmds[i] < 0 ? 4 : casingCmds[i] + casingCmds[i] + 2) {
    #ifdef ANDROID_NDK // OPENGL ES
    if (casingCmds[i] < 0) glColor4x (casingCmds[i + 1] * 0x101,
           casingCmds[i + 2] * 0x101, casingCmds[i + 3] * 0x101, 0x10000);
    #else
    if (casingCmds[i] < 0) glColor3f (casingCmds[i + 1] / 255.0,
           casingCmds[i + 2] / 255.0, casingCmds[i + 3] / 255.0);
    #endif
    else {
      glVertexPointer (2, GL_SHORT, 0, &casingCmds[i] + 2);
      glDrawArrays (GL_TRIANGLE_FAN, 0, casingCmds[i]);
    }
  }
  casingCmds.clear();
  #endif
}

inline void SetCasingColor (short r, short g, short b)
{
  MoveTo (0, 0); // Complete previous line in previous colour
  casingCmds.push_back (-1);
  casingCmds.push_back (r); casingCmds.push_back (g); casingCmds.push_back (b);
}

#if defined (ANDROID_NDK)
extern "C" void Java_org_osmu_gosmore_MapRenderer_render (
		JNIEnv*  env, jobject thiz, jdouble lon, jdouble lat,
		jint dir, jint _zoom, jboolean threeD, jboolean follow,
		jint _width, jint _height)
{
  static int started = 0, icons /* Not used */;
  LG width = _width;
  height = _height;
  Display3D = threeD;
  clon = Longitude (lon);
  clat = Latitude (lat);
  FollowGPSr = follow;
  sinAzimuth = sin (dir * (M_PI / 180.0));
  cosAzimuth = cos (dir * (M_PI / 180.0));
  zoom = _zoom;
  if (!started) {
    started = 1;
    jclass cls=env->GetObjectClass(thiz);
    //cls =(jclass)( env->NewGlobalRef(jcls) );
    //cls = env->FindClass(
    //		  "org/osmu/gosmore/MyClb");

    LG drawTeks = env->GetMethodID(cls, "drawTeks", "(Ljava/lang/String;IIFF)V");
    // env->ExceptionClear();
    /* if (drawTeksod != 0)*/
    // env->ExceptionClear();
  }
  glDisable(GL_BLEND);
  if (currentBbox[0] != '\0' && hashTable[bucketsMin1] < 500000 &&
      zoom < 100000000) {
    int nn = clat > -(1<<24) && clat < (1<<24) &&
             clon > -(1<<24) && clon < (1<<24);
    jstring js = env->NewStringUTF (nn ? "No location." : "No map.");
    env->CallVoidMethod (thiz, drawTeks, js, width / 2, height/3*2,
      (float)0, (float)1);
    js = env->NewStringUTF (nn ? "Please search or enable the GPS" :
      "Please update the map.");
    env->CallVoidMethod (thiz, drawTeks, js, width / 2, height/3*2 + 30,
      (float)0, (float)1);
  }
#elif defined(NOGTK)
int DrawExpose (HPEN *pen, HBRUSH *brush)
{
  width = draw->allocation.width;
  height = draw->allocation.height;
  WCHAR wcTmp[70];

  iconsgc = mygc;


  SetTextColor (mygc, Background ? 0 : 0xffffff);
  if (objectAddRow >= 0) {
    SelectObject (mygc, sysFont);
    //SetBkMode (mygc, TRANSPARENT);
    SelectObject (mygc, GetStockObject (BLACK_PEN));
    for (int y = 0, i = objectAddRow; y < height;
              y += ADD_HEIGHT) {
      //gdk_draw_line (draw->window, mygc, 0, y, width, y);
      gdk_draw_line (draw->window, mygc,
        width - ButtonSize * 20,
        height * i / restriction_no_right_turn,
        width,
        height * i / restriction_no_right_turn);
      RECT klip;
      klip.bottom = y + ADD_HEIGHT;
      klip.top = y;
      for (int x = 0; x < width - ButtonSize * 20 -
          ADD_WIDTH && i < restriction_no_right_turn; x += ADD_WIDTH, i++) {
        int *icon = style[i].x + 4 * IconSet;
        gdk_draw_drawable (draw->window, mygc, icons, icon[0], icon[1],
          x - icon[2] / 2 + ADD_WIDTH / 2, y, icon[2], icon[3]);
        klip.left = x + 8;
        klip.right = x + ADD_WIDTH - 8;
        #ifndef _WIN32_WCE
        DrawString (x + 8, y + ADD_HEIGHT - 16, klasTable[i].desc);
        #else
        ExtTextOut (mygc, x + 8, y + ADD_HEIGHT - 16, ETO_CLIPPED,
          &klip, klasTable[i].desc, wcslen (klasTable[i].desc), NULL);
        #endif
      }
    }
    return FALSE;
  } // if displaying the klas / style / rule selection screen
#else

void SetColour (GdkColor *c, int hexTrip)
{
  c->red =    (hexTrip >> 16)        * 0x101;
  c->green = ((hexTrip >> 8) & 0xff) * 0x101;
  c->blue =   (hexTrip       & 0xff) * 0x101;
  gdk_colormap_alloc_color (gdk_window_get_colormap (draw->window),
      c, FALSE, TRUE);
}
          
gint DrawExpose (void)
{
  static GdkColor styleColour[2 << STYLE_BITS][2];
  static GdkColor /*routeColour, validateColour,*/ resultArrowColour;
  height = draw->allocation.height;
  width = draw->allocation.width;
  if (!mygc || !iconsgc) {
    mygc = gdk_gc_new (draw->window);
    fg_gc = gdk_gc_new (draw->window);
    iconsgc = gdk_gc_new (draw->window);
    for (int i = 0; i < stylecount; i++) {
      for (int j = 0; j < 2; j++) {
        SetColour (&styleColour[i][j],
         !j ? style[i].areaColour 
          : style[i].lineColour != -1 ? style[i].lineColour
          : (style[i].areaColour >> 1) & 0xefefef); // Dark border for polys
      }
    }
    /*SetColour (&routeColour, 0x00ff00);
    SetColour (&validateColour, 0xff9999);*/
    SetColour (&resultArrowColour, 0);
    gdk_gc_set_fill (mygc, GDK_SOLID);

    if (!icons) icons = gdk_pixmap_create_from_xpm (draw->window, &mask,
      NULL, FindResource ("icons.xpm"));
    maskGC = gdk_gc_new(mask);
  }
  static int oldBackground = -1;
  if (oldBackground != Background) {
    /*static const int bgVal[9] = { 0, 0xe0ffff, 0xd3d3d3, 0xe6e6fa,
      0xffffe0, 0xf5deb3, 0x7b68ee, 0x6b8e23, 0xffffff };
    GdkColor bg; */
    //SetColour (&bg, bgVal[
    gdk_window_set_background (draw->window, &styleColour[
              firstElemStyle + Background - (Background > 8 ? 8 : 0)][0]);
    oldBackground = Background;
  }
  GdkRectangle r =
    { 0, 0, width, height };
  gdk_window_begin_paint_rect (draw->window, &r);

//  gdk_gc_set_foreground (mygc, &styleColour[0][0]);
//  gdk_gc_set_line_attributes (mygc,
//    1, GDK_LINE_SOLID, GDK_CAP_PROJECTING, GDK_JOIN_MITER);
    
  PangoMatrix mat = PANGO_MATRIX_INIT;
  pc = gdk_pango_context_get_for_screen (gdk_screen_get_default ());
  pl = pango_layout_new (pc);
  pango_layout_set_width (pl, -1); // No wrapping 200 * PANGO_SCALE);
  if (Background == 0) {
    PangoAttribute *wit = pango_attr_foreground_new (0xffff, 0xffff, 0xffff);
    PangoAttrList *list = pango_attr_list_new ();//pango_layout_get_attributes (pl);
    pango_attr_list_insert (list, wit);
    pango_layout_set_attributes (pl, list);
    pango_attr_list_unref (list);
  }
/*    PangoAttribute *wit = pango_attr_background_new (0xffff, 0xffff, 0xffff);
    PangoAttrList *list = pango_attr_list_new ();//pango_layout_get_attributes (pl);
    pango_attr_list_insert (list, wit);
    pango_layout_set_attributes (pl, list);
    pango_attr_list_unref (list); */
#endif // GTK
  LG if (option == mapMode) ChangePak (NULL, clon, clat);
  // This call can be almost anywhere, e.g. SetLocation(). Calling it in
  // searchMode with GeoSearch may invalidate some of the results.

  if (ButtonSize <= 0) ButtonSize = 4;

  if (zoom < 0 || zoom > 1023456789) zoom = 1023456789;
  if (zoom / width <= 1) zoom += 4000;

  LG cosa = lrint (4294967296.0 * cosAzimuth * width / zoom);
  sina = lrint (4294967296.0 * sinAzimuth * width / zoom);
  xadj =
    width / 2 - ((clon * (__int64) cosa + clat * (__int64) sina) >> 32);
  yadj =
    height / 2 - ((clon * (__int64) sina - clat * (__int64) cosa) >> 32);

  LG if (Display3D) {
    cosa = lrint (cosAzimuth * MUL);
    sina = lrint (sinAzimuth * MUL);

    yadj = (clon + (int)(sinAzimuth * CAMERA2C)) * (myint) sina -
           (clat - (int)(cosAzimuth * CAMERA2C)) * (myint) cosa;
    xadj = -(clon + (int)(sinAzimuth * CAMERA2C)) * (myint) cosa -
            (clat - (int)(cosAzimuth * CAMERA2C)) * (myint) sina;
  }

  if (option == mapMode) {
//    int perpixel = zoom / width;
    int *block = (int*) calloc ((width + 31) / 32 * 4, height);

    stack<text2Brendered> text2B;
    text2B.push (text2Brendered ()); // Always have a spare one open
    vector<ndType*> area;
    stack<ndType*> dlist[12];
    // 5 under + 1 gound level + 5 above + icons
    
    if (ShowCompass) {
      for (int i = 0; i < 2; i++) {
        for (int m = -20; m <= 20; m += 40) {
          text2B.top ().s = m < 0 ? (i ? "N" : "W") : i ? "S" : "E";
          text2B.top ().x = width - 40 +
            lrint ((i ? -sinAzimuth : cosAzimuth) * m) - 50;
          text2B.top ().x2 = text2B.top ().x + 100;
          text2B.top ().dst = 100;
          text2B.top ().y2 = text2B.top ().y = height - 40 +
            lrint ((i ? cosAzimuth : sinAzimuth) * m);
          text2B.push (text2Brendered ());
        }
      }
    }
    
    // render map
    /* We need the smallest bbox that covers the test area. For 2D, the
       test area is a rectangle that is not aligned with the axis, so the
       bbox is the maxs and mins of the latitudes and longitudes of the 4
       corners. For 3D, the test area is a triangle, with the camera
       coordinate included twice, hence 4 tests
    */
    int latRadius[2] = { 0, 0 }, lonRadius[2] = { 0, 0 };
    for (int wc = -1; wc <= 1; wc += 2) { // width and
      for (int hc = -1; hc <= 1; hc += 2) { // height coefficients
        int w = !Display3D ? zoom : hc > 0 ? WIDE3D : 0, h = !Display3D
          ? zoom / width * height : hc > 0 ? FAR3D : CAMERA2C;
        int lon = lrint (w * cosAzimuth * wc - h * sinAzimuth * hc);
        int lat = lrint (h * cosAzimuth * hc + w * sinAzimuth * wc);
        lonRadius[0] = min (lonRadius[0], lon);
        lonRadius[1] = max (lonRadius[1], lon);
        latRadius[0] = min (latRadius[0], lat);
        latRadius[1] = max (latRadius[1], lat);
      }
    }
    LG OsmItr itr (clon + lonRadius[0] - 1000, clat + latRadius[0] - 1000,
                clon + lonRadius[1] + 1000, clat + latRadius[1] + 1000);
    // Widen this a bit so that we render nodes that are just a bit offscreen ?
    while (Next (itr)) {
      ndType *nd = itr.nd[0];
      wayType *w = Way (nd);

      if (Style (w)->scaleMax < zoom / width * 350 / (DetailLevel + 6)
          && !Display3D && w->dlat < zoom / width * 20 &&
                           w->dlon < zoom / width * 20) continue;
      // With 3D, the icons are filtered only much later when we know z.
      if (nd->other[0] != 0) {
        nd = itr.nd[0] + itr.nd[0]->other[0];
        if (nd->lat == INT_MIN) nd = itr.nd[0]; // Node excluded from build
        else if (itr.left <= nd->lon && nd->lon < itr.right &&
            itr.top  <= nd->lat && nd->lat < itr.bottom) continue;
      } // Only process this way when the Itr gives us the first node, or
      // the first node that's inside the viewing area
      if (nd->other[0] == 0 && nd->other[1] == 0) dlist[11].push (nd);
      else if (Style (w)->areaColour != -1 ||
        StyleNr(w) == natural_coastline) area.push_back (nd);
      // I guess polygons that enter the bbox multiple times are drawn
      // multiple times.
      else dlist[Layer (w) + 5].push (nd);
    }
    LG qsort (&area[0], area.size (), sizeof (area[0]),
      (int (*)(const void *a, const void *b))WaySizeCmp);
    //for (; !dlist[0].empty (); dlist[0].pop ()) {
    //  ndType *nd = dlist[0].top ();
    vector<PolygonEdge> coast;
    vector<FixedPoint*> cpiece;
    for (; !area.empty(); area.pop_back ()) {
      ndType *nd = area.back ();
      wayType *w = Way (nd);
      if (StyleNr(w) != natural_coastline) {
        while (nd->other[0] != 0) nd += nd->other[0];
      }
      #if defined (_WIN32_CE) || defined (_WIN32)
      #define GdkPoint POINT
      #endif
      vector<FixedPoint> pt;
      int oldx = 0, oldy = 0, x = 0 /* Shut up gcc*/, y = 0 /*Shut up gcc*/;
      int firstx = INT_MIN, firsty = INT_MIN /* Shut up gcc */;
      for (; ; nd += nd->other[1]) {
        if (nd->lat != INT_MIN) {
          pt.push_back (FixedPoint ());
          pt.back ().x = x = X2 (nd->lon, nd->lat);
          pt.back ().y = y = Y2 (nd->lon, nd->lat);
          if (Display3D) {
            if (firstx == INT_MIN) {
              firstx = x;
              firsty = y;
            }
            if (y > 0 && oldy < 0) {
              pt.back ().x = x + (x - oldx) * __int64((1<<26) - y) / (y - oldy);
              pt.back ().y = 1<<26; // Insert modified instance of old point
              pt.push_back (FixedPoint ());
              pt.back ().x = x; // before current point.
              pt.back ().y = y;
            }
            else if (y < 0) {
              if (oldy < 0) pt.pop_back ();
              else {
                pt.back ().x = oldx + (oldx - x) * __int64((1<<26) - oldy) / (oldy - y);
                pt.back ().y = 1<<26;
              }
            }
            oldx = x;
            oldy = y;
          }
          //pt[pts].x = X (nd->lon, nd->lat);
          //pt[pts++].y = Y (nd->lon, nd->lat);
        }
        if (!nd->other[1]) break;
      }
      
      if (Display3D && y < 0 && firsty > 0) {
        pt.push_back (FixedPoint ());
        pt.back ().x = firstx + (firstx - x) * __int64((1<<26) - firsty) / (firsty - y);
        pt.back ().y = 1<<26;
      }
      if (Display3D && firsty < 0 && y > 0) {
        pt.push_back (FixedPoint ());
        pt.back ().x = x + (x - firstx) * __int64((1<<26) - y) / (y - firsty);
        pt.back ().y = 1<<26;
      }
      if (!pt.empty ()) {
        #if defined(ANDROID_NDK) || !defined(NOGTK)
        if (StyleNr(w) == natural_coastline) {
          cpiece.push_back ((FixedPoint*) malloc (pt.size() * sizeof (FixedPoint)));
          memcpy (cpiece.back(), &pt[0], pt.size() * sizeof (FixedPoint));
          AddClockwise (coast, cpiece.back(), pt.size());
        }
        else {
          vector<PolygonEdge> pe;
          AddPolygon (pe, &pt[0], pt.size ());
          #ifdef ANDROID_NDK
          #define glSColor(x) glColor4x ((x) >> 8, (x) & 0xffff, \
                              ((x) << 8)&0xffff, 0x10000)
          glSColor (Style(w)->areaColour);
          Fill (pe,FALSE); 
          #else
          gdk_gc_set_foreground (mygc, &styleColour[Style (w) - style][0]);
          Fill (pe, FALSE, draw->window, mygc);
          #endif
          /*if (pt.size () < 10) {
            vector<PolygonEdge> pe2;
            AddPolygon (pe2, &pt[0], pt.size ());
            Fill (pe2,draw->window,mygc);
          }*/
             /*if (!Fill (pe)) {
            __android_log_print (ANDROID_LOG_WARN, "Gosmore", "s %d", pt.size());
            for (int m = 0; m < pt.size (); m++)
              __android_log_print (ANDROID_LOG_WARN, "Gosmore", "%d %d", pt[m].x, pt[m].y);
          }*/
          // TODO: border
          //glSColor ((Style(w)->areaColour >> 1) & 0xefefef);
          //glLineWidth(1);
        #elif defined(NOGTK)
        if (StyleNr(w) != natural_coastline) {
          SelectObject (mygc, brush[StyleNr (w)]);
          SelectObject (mygc, pen[StyleNr (w)]);
          Polygon (mygc, &pt[0], pt.size ());
        #else
        if (StyleNr(w) != natural_coastline) {
          gdk_gc_set_foreground (mygc, &styleColour[Style (w) - style][0]);
          gdk_draw_polygon (draw->window, mygc, TRUE, &pt[0], pt.size ());
          gdk_gc_set_foreground (mygc, &styleColour[Style (w) - style][1]);
          gdk_gc_set_line_attributes (mygc, Style (w)->lineWidth,
            Style (w)->dashed ? GDK_LINE_ON_OFF_DASH
            : GDK_LINE_SOLID, GDK_CAP_PROJECTING, GDK_JOIN_MITER);
          gdk_draw_polygon (draw->window, mygc, FALSE, &pt[0], pt.size ());
        #endif
          // Text placement: The basic idea is here : http://alienryderflex.com/polygon_fill/
          text2B.top ().dst = strcspn ((char*)(w + 1) + 1, "\n") * 9;
          text2B.top ().x = -1;
          for (unsigned i = 0; i < pt.size (); i++) {
            pt[i].x >>= 16;
            pt[i].y >>= 16;
          }
          for (unsigned i = 0; i < pt.size (); i++) {
            int iy = (pt[i].y + pt[i < pt.size () - 1 ? i + 1 : 0].y) / 2;
            // Look for a large horisontal space inside the poly at this y value
            vector<int> nx;
            for (unsigned j = 0, k = pt.size () - 1; j < pt.size (); j++) {
              if ((pt[j].y < iy && pt[k].y >= iy) || (pt[k].y < iy && pt[j].y >= iy)) {
                nx.push_back (pt[j].x + (pt[k].x - pt[j].x) * (iy - pt[j].y) /
                  (pt[k].y - pt[j].y));
              }
              k = j;
            }
            sort (nx.begin (), nx.end ());
            for (unsigned int j = 0; j < nx.size (); j += 2) {
              if (nx[j + 1] - nx[j] > text2B.top ().dst) {
                text2B.top ().x = nx[j];
                text2B.top ().x2 = nx[j + 1];
                text2B.top ().y = iy - 5;
                text2B.top ().dst = nx[j + 1] - nx[j];
              }
            }
          }
          if (text2B.top ().x >= 0) {
            text2B.top ().y2 = text2B.top ().y;
            text2B.top ().s = (char*)(w + 1) + 1;
            text2B.push (text2Brendered ());
          }
        } // If not a coastline
      } // Polygon not empty
    } // For each area
    #ifdef ANDROID_NDK
    glColor4x (0x8000, 0x8000, 0x10000, 0x10000);
    LG Fill (coast, TRUE);
    for (; cpiece.size () > 0; cpiece.pop_back()) free (cpiece.back());
    glLineWidth (2);
    #elif !defined (NOGTK)
    gdk_gc_set_foreground (mygc, &styleColour[natural_coastline][1]);
    Fill (coast, TRUE, draw->window,mygc);
    for (; cpiece.size () > 0; cpiece.pop_back()) free (cpiece.back());
    #endif

    LG queue<linePtType> q;
    LG for (int l = 0; l < 12; l++) {
      #ifdef ANDROID_NDK
      glColor4x (0, 0, 0, 0x10000);
      #endif
      LG for (; !dlist[l].empty (); dlist[l].pop ()) {
        ndType *nd = dlist[l].top ();
        wayType *w = Way (nd);

        int best = 30;
        int len = strcspn ((char *)(w + 1) + 1, "\n");
        
	// single-point node
        LG if (nd->other[0] == 0 && nd->other[1] == 0) {
          int x = X (nd->lon, nd->lat), y = Y (nd->lon, nd->lat);
          int *icon = Style (w)->x + 4 * IconSet, wd = icon[2], ht = icon[3];
          if ((!Display3D || y > Style (w)->scaleMax / 400) && !TestOrSet (
                      block, FALSE, x - wd / 2, y - ht / 2, 0, ht, wd, 0)) {
            TestOrSet (block, TRUE, x - wd / 2, y - ht / 2, 0, ht, wd, 0);
            LG DrawPoI (x, y, Style (w)->x + 4 * IconSet);
            
            #if 0 //def NOGTK
            SelectObject (mygc, sysFont);
            //SetBkMode (mygc, TRANSPARENT);
            const unsigned char *sStart = (const unsigned char *)(w + 1) + 1;
            UTF16 *tStart = (UTF16 *) wcTmp;
            if (ConvertUTF8toUTF16 (&sStart,  sStart + len, &tStart, tStart +
                  sizeof (wcTmp) / sizeof (wcTmp[0]), lenientConversion)
                == conversionOK) {
              ExtTextOutW (mygc, x - len * 3, y + icon[3] / 2, 0, NULL,
                  wcTmp, (wchar_t *) tStart - wcTmp, NULL);
            }
            #endif
            text2B.top ().x = x - 100;
            text2B.top ().x2 = x + 100;
            text2B.top ().dst = 200;
            text2B.top ().y2 = text2B.top ().y = y +
                               Style (w)->x[IconSet * 4 + 3] / 2;
            if (Sqr ((__int64) Style (w)->scaleMax / 2 /
                (zoom / width)) * DetailLevel > len * len * 100 &&
                len > 0) best = 0;
          }
        }
	// ways (including areas on WinMob : FIXME)
        else if (nd->other[1] != 0) {
	  // perform validation (on non-areas)
	  bool valid;
	  LG if (ValidateMode && Style(w)->areaColour == -1) {
	    valid = len > 0 && StyleNr (w) != highway_road;
	    // most ways should have labels and they should not be
	    // highway=road
	    
	    // valid = valid && ... (add more validation here)

	    // // LOG
	    // logprintf("valid = (len > 0) = %d > 0 = %d (%s)\n",
	    // 	    len,valid,(char *)(w + 1) + 1);

	  } else {
	    valid = true; 
	  }
	  if (highlight != "") {
            for (char *ptr = (char *)(w + 1) + 1; valid && *ptr != '\0'; ) {
              if (strncmp (highlight.c_str (), ptr, strcspn (ptr, "\n"))
                  == 0) valid = false;
              while (*ptr != '\0' && *ptr++ != '\n') {}
            } // Should highlighting get its own pen ?
          }
	  // two stages -> validate (if needed) then normal rendering
	  ndType *orig = nd;
	  LG for (int stage = ( valid ? 1 : 0);stage<2;stage++) {
	    nd = orig;
	    if (stage==0) {
            #ifdef ANDROID_NDK
	    	glColor4x (0x10000, 0x999A, 0x999A, 0x1000);
	    	glLineWidth(10);
            #elif !defined(NOGTK)
	      gdk_gc_set_foreground (mygc,
	        &styleColour[firstElemStyle + ValidateModeNum][1]); //&validateColour);
	      gdk_gc_set_line_attributes (mygc, 10,
		       GDK_LINE_SOLID, GDK_CAP_PROJECTING, GDK_JOIN_MITER);
            #else
	      SelectObject (mygc, pen[firstElemStyle + ValidateModeNum]);
	        //pen[VALIDATE_PEN]);
            #endif
	    }
	    else if (stage == 1) {
              #ifdef ANDROID_NDK
	      //glSColor (Style(w)->lineColour);
              //glLineWidth(Style(w)->lineWidth);
              SetCasingColor (Style(w)->lineColour >> 16,
                (Style(w)->lineColour>>8)&0xff, Style(w)->lineColour & 0xff);
              #elif !defined(NOGTK)
	      gdk_gc_set_foreground (mygc, &styleColour[Style (w) - style][1]);
	      gdk_gc_set_line_attributes (mygc, Style (w)->lineWidth,
		    Style (w)->dashed ? GDK_LINE_ON_OFF_DASH
		    : GDK_LINE_SOLID, GDK_CAP_PROJECTING, GDK_JOIN_MITER);
              #else
	      SelectObject (mygc, pen[StyleNr (w)]);
              #endif
	    }
	    int oldx = X (nd->lon, nd->lat), oldy = Y (nd->lon, nd->lat);
	    int cumulative = 0;
            LG q.push (linePtType (oldx, oldy, cumulative));
            MoveTo (oldx, oldy);
	    do {
	      ndType *next = nd + nd->other[1];
	      LG if (next->lat == INT_MIN) break; // Node excluded from build
	      LG int x = X (next->lon, next->lat), x2;
	      int y = Y (next->lon, next->lat), y2;
//	      printf ("%6.0lf %6.0lf - %6.0lf %6.0lf - %lf\n", x, y, oldx, oldy,
//	        AdjDepth (next->lon, next->lat));
	      if ((x <= width || oldx <= width) &&
		  (x >= 0 || oldx >= 0) && (y >= 0 || oldy >= 0) &&
		  (y <= height || oldy <= height)) {
//                printf ("%4d %4d - %4d %4d\n", x,y,oldx,oldy);
                /* If we're doing 3D and oldy is negative, it means the point
                   was behind the camera. Then we must draw an infinitely long
                   line from (x,y) with the same gradient as (x,y)-(oldx,oldy),
                   but away from (oldx,oldy). Or at least up to some y value
                   below the bottom of the screen. So we adjust oldx and oldy.
                   
                   When y is negative, we do something very similar. */
                if (!Display3D || y > 0) {
                  x2 = x;
                  y2 = y;
                  if (Display3D && oldy <= 0) {
                 /*   if (nx < 32760 && nx > -32760 &&
                      oldx < 32760 && oldx > -32760 &&
                      oldy < 32760 && oldy > -32760) */
                    oldx = x + (x - oldx) * (height + 10 - y) /
                      (y - oldy);
                    oldy = height + 10;
                    MoveTo (oldx, oldy);
                  }
                }
                else /*if (oldy > 0 which is true)*/ {
/*                  if (nx < 32760 && nx > -32760 &&
                    oldx < 32760 && oldx > -32760 &&
                    oldy < 32760 && oldy > -32760) */
                  x2 = oldx +
                    (oldx - x) * (height + 10 - oldy) / (oldy - y);
                  y2 = height + 10;
                }
                #ifdef ANDROID_NDK
                LineTo (x2, y2, Style(w)->lineWidth *
                  max (width * 1000 / zoom, 2));
                #else
                LG gdk_draw_line (draw->window, mygc, oldx, oldy, x2, y2);
                #endif
                // Draw3DLine
                LG if (oldx < 0 || oldx >= width ||
                    oldy < 0 || oldy >= height) {
                  cumulative += 9999; // Insert a break in the queue
                  LG q.push (linePtType (oldx, oldy, cumulative));
                  // TODO: Interpolate the segment to get a point that is
                  // closer to the screen. The same applies to the other push
                }
                LG cumulative += isqrt (Sqr (oldx - x2) + Sqr (oldy - y2));
                LG q.push (linePtType (x2, y2, cumulative));
                LG ConsiderText (&q, FALSE, len, &best, &text2B.top ());
	      }
	      else MoveTo (x, y);
	      LG nd = next;
	      oldx = x;
	      oldy = y;
	    } while (itr.left <= nd->lon && nd->lon < itr.right &&
		     itr.top  <= nd->lat && nd->lat < itr.bottom &&
		     nd->other[1] != 0);
            LG ConsiderText (&q, TRUE, len, &best, &text2B.top ());
	  }
	} /* If it has one or more segments */
	  
        LG if (best < 30) {
          text2B.top ().s = (char *)(w + 1) + 1;
          text2B.push (text2Brendered ());
        }
      } /* for each way / icon */
      FinishCasing();
    } // For each layer
  //  gdk_gc_set_foreground (draw->style->fg_gc[0], &highwayColour[rail]);
  //  gdk_gc_set_line_attributes (draw->style->fg_gc[0],
  //    1, GDK_LINE_SOLID, GDK_CAP_PROJECTING, GDK_JOIN_MITER);

    // render route
    LG routeNodeType *rt;
    if (shortest && (rt = shortest->shortest)) {
      double len;
      int nodeCnt = 1, x = X (rt->nd->lon, rt->nd->lat);
      int y = Y (rt->nd->lon, rt->nd->lat);
      __int64 sumLat = rt->nd->lat;
      #ifdef ANDROID_NDK
      glSColor (style[firstElemStyle + StartRouteNum].lineColour);
      glLineWidth (6);
      #define CHARWIDTH 12
      #elif !defined(NOGTK)
      gdk_gc_set_foreground (mygc,
        &styleColour[firstElemStyle + StartRouteNum][1]); //routeColour);
      gdk_gc_set_line_attributes (mygc, 6,
        GDK_LINE_SOLID, GDK_CAP_PROJECTING, GDK_JOIN_MITER);
      #define CHARWIDTH 12
      #else
      SelectObject (mygc, pen[firstElemStyle + StartRouteNum]);
      #define CHARWIDTH 6
      #endif
      if (routeSuccess) Draw3DLine (X (flon, flat), Y (flon, flat), x, y);
      
      len = sqrt (Sqr ((double) (rt->nd->lat - flat)) +
        Sqr ((double) (rt->nd->lon - flon)));
      for (; rt->shortest; rt = rt->shortest) {
        int nx = X (rt->shortest->nd->lon, rt->shortest->nd->lat);
        int ny = Y (rt->shortest->nd->lon, rt->shortest->nd->lat);
        if ((nx >= 0 || x >= 0) && (nx < width || x < width) &&
            (ny >= 0 || y >= 0) && (ny < height || y < height)) {
          // Gdk looks only at the lower 16 bits ?
          Draw3DLine (x, y, nx, ny);
        }
        len += sqrt (Sqr ((double) (rt->nd->lat - rt->shortest->nd->lat)) +
          Sqr ((double) (rt->nd->lon - rt->shortest->nd->lon)));
        sumLat += rt->nd->lat;
        nodeCnt++;
        x = nx;
        y = ny;
      }
      Draw3DLine (x, y, X (tlon, tlat), Y (tlon, tlat));
      len += sqrt (Sqr ((double) (rt->nd->lat - tlat)) +
        Sqr ((double) (rt->nd->lon - tlon)));
      char distStr[13];
      sprintf (distStr, "%.3lf km", len * (20000 / 2147483648.0) *
        cos (LatInverse (sumLat / nodeCnt) * (M_PI / 180)));
      #ifdef ANDROID_NDK
      jstring js = env->NewStringUTF (distStr);
      env->CallVoidMethod (thiz, drawTeks, js, width -
        CHARWIDTH/2 * strlen (distStr), 40, (float)0, (float)1);
      #else
      DrawString (width - CHARWIDTH * strlen (distStr), 10, distStr);
      #endif
      #if 0 //ndef NOGTK
      gdk_draw_string (draw->window, f, fg_gc, //draw->style->fg_gc[0],
        width - 7 * strlen (distStr), 10, distStr);
      #else
      #endif
    }
    LG DrawPoI (X (flon, flat), Y (flon, flat), IconSet * 4 +
      style[firstElemStyle + StartRouteNum].x);
    LG DrawPoI (X (tlon, tlat), Y (tlon, tlat), IconSet * 4 +
      style[firstElemStyle + EndRouteNum].x);
    #ifndef NOGTK
    LG for (deque<wayPointStruct>::iterator w = wayPoint.begin ();
         w != wayPoint.end (); w++) {
      DrawPoI (X (w->lon, w->lat), Y (w->lon, w->lat),
        style[firstElemStyle + wayPointIconNum].x);
    }
    
    LG for (int i = 1; shortest && ShowActiveRouteNodes && i < routeHeapSize; i++) {
      gdk_draw_line (draw->window, mygc,
        X (routeHeap[i].r->nd->lon, routeHeap[i].r->nd->lat) - 2,
        Y (routeHeap[i].r->nd->lon, routeHeap[i].r->nd->lat),
        X (routeHeap[i].r->nd->lon, routeHeap[i].r->nd->lat) + 2,
        Y (routeHeap[i].r->nd->lon, routeHeap[i].r->nd->lat));
    }
    #elif defined (_WIN32)
    for (int j = 0; j <= newWayCnt; j++) {
      int x = X (newWays[j].coord[0][0], newWays[j].coord[0][1]);
      int y = Y (newWays[j].coord[0][0], newWays[j].coord[0][1]);
      if (newWays[j].cnt == 1) {
        int *icon = style[j < newWayCnt ? newWays[j].klas : place_village].x
          + 4 * IconSet;
        gdk_draw_drawable (draw->window, mygc, icons, icon[0], icon[1],
          x - icon[2] / 2, y - icon[3] / 2, icon[2], icon[3]);
      }
      else {
        SelectObject (mygc, pen[j < newWayCnt ? newWays[j].klas: 0]);
        MoveToEx (mygc, x, y, NULL);
        for (int i = 1; i < newWays[j].cnt; i++) {
          LineTo (mygc, X (newWays[j].coord[i][0], newWays[j].coord[i][1]),
                        Y (newWays[j].coord[i][0], newWays[j].coord[i][1]));
        }
      }
    }
    if (ShowTrace) {
      for (gpsNewStruct *ptr = gpsTrack; ptr < gpsNew; ptr++) {
        SetPixel (mygc, X (ptr->lon, ptr->lat), Y (ptr->lon, ptr->lat), 0);
      }
    }
    #endif
    LG for (text2B.pop (); !text2B.empty ();  text2B.pop ()) {
      #ifndef ANDROID_NDK
      if (pressTime != -1) continue; // Don't render text while dragging
      #endif
      text2Brendered *t = &text2B.top();
      #ifdef PANGO_VERSION
      PangoRectangle rect;
      #else
      struct { int width, height; } rect;
      struct { double xx, xy, yy, yx; } mat;
      #endif
      int x0 = (t->x + t->x2) / 2, y0 = (t->y + t->y2) / 2;
      mat.yy = mat.xx = fabs (double (t->x - t->x2)) / (double) t->dst;
      mat.xy = (t->y - t->y2) / (double)(t->x > t->x2 ? -t->dst : t->dst);
      mat.yx = -mat.xy;

      double move = 0.6;
      for (const char *txt = t->s; *txt != '\0';) {
        #if PANGO_VERSION
        pango_context_set_matrix (pc, &mat);
        pango_layout_set_text (pl,
          string (txt, strcspn (txt, "\n")).c_str (), -1);
        pango_layout_get_pixel_extents (pl, &rect, NULL);
        #else
        rect.width = strcspn (txt, "\n") * 9;
        rect.height = 11;
        #endif
        y0 += lrint (mat.xx * (rect.height + 3) * move);
        x0 += lrint (mat.xy * (rect.height + 3) * move);
        move = 1.2;
        if (TestOrSet (block, FALSE, 
          lrint (x0 - rect.width * mat.xx / 2 - mat.xy * rect.height / 3),
          lrint (y0 - rect.width * mat.yx / 2 - mat.yy * rect.height / 3),
          lrint (mat.xy * (rect.height)), lrint (mat.xx * (rect.height)),
          lrint (mat.xx * (rect.width + 10)),
          lrint (mat.yx * (rect.width + 10)))) break;
        TestOrSet (block, TRUE, 
          lrint (x0 - rect.width * mat.xx / 2 - mat.xy * rect.height / 3),
          lrint (y0 - rect.width * mat.yx / 2 - mat.yy * rect.height / 3),
          lrint (mat.xy * (rect.height)), lrint (mat.xx * (rect.height)),
          lrint (mat.xx * (rect.width + 10)),
          lrint (mat.yx * (rect.width + 10)));
        #ifdef ANDROID_NDK
    	jstring js = env->NewStringUTF (
    			string (txt, strcspn (txt, "\n")).c_str ());
    	env->CallVoidMethod (thiz, drawTeks, js, x0, y0,
    			(float)mat.xy, (float)mat.xx);

        #elif !defined(NOGTK)
        gdk_draw_layout (GDK_DRAWABLE (draw->window),
          fg_gc /*draw->style->fg_gc[0]*/,
          x0 - (rect.width * mat.xx + rect.height * fabs (mat.xy)) / 2,
          y0 - (rect.height * mat.yy + rect.width * fabs (mat.xy)) / 2, pl);
        #else
        double hoek = atan2 (t->y2 - t->y, t->x - t->x2);
        if (t->x2 < t->x) hoek += M_PI;
        logFont.lfEscapement = logFont.lfOrientation = 1800 + int ((1800 / M_PI) * hoek);
        
        HFONT customFont = CreateFontIndirect (&logFont);
        HGDIOBJ oldf = SelectObject (mygc, customFont);
        //SetBkMode (mygc, TRANSPARENT);
        const unsigned char *sStart = (const unsigned char *) txt;
        UTF16 *tStart = (UTF16 *) wcTmp;
        int len = strcspn (txt, "\n");
        if (ConvertUTF8toUTF16 (&sStart,  sStart + len, &tStart,
              tStart + sizeof (wcTmp) / sizeof (wcTmp[0]),  lenientConversion)
            == conversionOK) {
          ExtTextOutW (mygc,
                x0 - lrint (len * 4 * mat.xx + rect.height / 2 * mat.xy),
                y0 + lrint (len * 4 * mat.xy - rect.height / 2 * mat.xx), 
                0, NULL, wcTmp, (wchar_t *) tStart - wcTmp, NULL);
        }
        SelectObject (mygc, oldf);
        DeleteObject (customFont);
        #endif
        if (zoom / width > 20) break;
        while (*txt != '\0' && *txt++ != '\n') {}
      }
    }
    free (block);
    if (FollowGPSr && command[0] && command[0] == command[1] && command[0] == command[2]) {
      DrawPoI (width / 2, height / 6,
        style[firstElemStyle + command[0]].x + 8); // Always square.big
    }
  } // Not in the menu
  else if (option == searchMode) {
    #ifndef NOGTK
    gdk_gc_set_foreground (mygc, &resultArrowColour);
    gdk_gc_set_line_attributes (mygc, 1,
       GDK_LINE_SOLID, GDK_CAP_PROJECTING, GDK_JOIN_MITER);
    #elif defined (_WIN32)
    SelectObject (mygc, GetStockObject (BLACK_PEN));
    #endif
	DisplaySearchResults ();
    HandleKeyboard (FALSE, 0, 0);
  }
  else if (option == optionMode) {
    for (int i = 0; i < wayPointIconNum; i++) {
      if (CompactOptions) {
        char l1[20], *s = (char*) optionNameTable[English][i], *ptr = s + 1;
        while (!isspace (*ptr) && !isupper (*ptr) && *ptr) ptr++;
        sprintf (l1, "%.*s", ptr - s, s);
        DrawString (ListXY (i, FALSE) - strlen (l1) * 5, ListXY (i, TRUE) - 8, l1);
        DrawString (ListXY (i, FALSE) - strlen (ptr) * 5, ListXY (i, TRUE) + 8, ptr);
      }
      else {
        DrawPoI (ListXY (i, FALSE), ListXY (i, TRUE) - 5,
          style[firstElemStyle + i].x); // Always classic.big
        DrawString (ListXY (i, FALSE) - strlen (optionNameTable[English][i]) *
          5, ListXY (i, TRUE) + style[firstElemStyle + i].x[3] / 2 - 5,
          optionNameTable[English][i]);
      }
    }
  }
  else {
    char optStr[30];
    if (option == VehicleNum) {
      #define M(v) Vehicle == v ## R ? #v :
      sprintf (optStr, "%s : %s", optionNameTable[English][option],
        RESTRICTIONS NULL);
      #undef M
    }
    else sprintf (optStr, "%s : %d", optionNameTable[English][option],
    #define o(en,min,max) option == en ## Num ? en :
    OPTIONS
    #undef o
      0);
    DrawString (50, height / 2, optStr);
  }
  #ifndef NOGTK
  /* Buttons now on the top row 
  gdk_draw_rectangle (draw->window, draw->style->bg_gc[0], TRUE,
    width - ButtonSize * 20, height - ButtonSize * 60,
    width, height);
  for (int i = 0; i < 3; i++) {
    gdk_draw_string (draw->window, f, draw->style->fg_gc[0],
      width - ButtonSize * 10 - 5, height + (f->ascent - f->descent)
      / 2 - ButtonSize * (20 * i + 10), i == 0 ? "O" : i == 1 ? "-" : "+");
  }
  */
  gdk_window_end_paint (draw->window);
  gdk_flush ();
  #else
  #ifdef WIN32
  int i = (Layout > (MenuKey == 0 || option != mapMode ? 0 : 1) ? 3 : 0);
  RECT r;
  r.left = width - ButtonSize * 20;
  r.top = height - ButtonSize * 20 * i;
  r.right = width;
  r.bottom = height;
  FillRect (mygc, &r, (HBRUSH) GetStockObject(LTGRAY_BRUSH));
  SelectObject (mygc, sysFont);
  //SetBkMode (mygc, TRANSPARENT);
  while (--i >= 0) {
    ExtTextOut (mygc, width - ButtonSize * 10 - 5, height - 5 -
        ButtonSize * (20 * i + 10), 0, NULL, i == 0 ? TEXT ("O") :
        i == 1 ? TEXT ("-") : TEXT ("+"), 1, NULL);
  }
  #endif

  char coord[64];
  if (ShowCoordinates == 1) {
    if(GpsIdle==999)
      snprintf (coord, 63, "%9.5lf %10.5lf zoom=%d GPS OFF %s %sfollowing", LatInverse (clat), LonInverse (clon),zoom,routeSuccess?"Route":"No Route",FollowGPSr?"":"not ");
    else
      snprintf (coord, 63, "%9.5lf %10.5lf zoom=%d GPS idle %ds %s %sfollowing", LatInverse (clat), LonInverse (clon),zoom,GpsIdle,routeSuccess?"Route":"No Route",FollowGPSr?"":"not ");
    DrawString (0, 5, coord);
  }
  #ifdef _WIN32
  else if (ShowCoordinates == 2) {
    MEMORYSTATUS memStat;
    GlobalMemoryStatus (&memStat);
    sprintf (coord, "%9d", memStat.dwAvailPhys );
    DrawString (0, 5, coord);
    //ExtTextOut (mygc, 0, 10, 0, NULL, coord, 9, NULL);
  }
  #endif
  #endif
  #ifdef CAIRO_VERSION
//  cairo_destroy (cai);
  #endif
  #ifdef ANDROID_NDK
  return;
  #else
  return FALSE;
  #endif
}

#ifndef NOGTK
gint Drag (GtkWidget * /*widget*/, GdkEventMotion *event, void * /*w_cur*/)
{
  if (event->state & GDK_BUTTON1_MASK) MouseEv
          (event->x, event->y, event->time, 1, FALSE);
  return TRUE;
}

int Click (GtkWidget * /*widget*/, GdkEventButton *event, void * /*para*/)
{
  MouseEv (event->x, event->y, event->time, event->button, TRUE);
  return TRUE;
}

GtkWidget *searchW;

int ToggleSearchResults (void)
{
  option = option == searchMode ? mapMode : searchMode;
  highlight = string ();
  gtk_widget_queue_clear (draw);
  return FALSE;
}

int IncrementalSearch (void)
{
  option = searchMode;
  GeoSearch ((char*) gtk_entry_get_text (GTK_ENTRY (searchW)));
  gtk_widget_queue_clear (draw);
  return FALSE;
}

void HitGtkButton (GtkWidget * /*w*/, void *data)
{
  HitButton ((intptr_t)data);
  gtk_widget_queue_clear (draw);
}

//------------------------------------------------------------------------
// Callbacks that are called with the user drops an icon and then binds it
#if 0
static gboolean DropOnDraw (GtkWidget *w, GdkDragContext *c, gint /*x*/,
  gint /* y */, guint time, gpointer)
{
  if (c->targets) {
    gtk_drag_get_data (w, c,
      GDK_POINTER_TO_ATOM (g_list_nth_data (c->targets, 0)), time);
  }
  return c->targets ? TRUE : FALSE;
}
#endif

#endif // NOGTK

#endif // HEADLESS

inline void SerializeOptions (FILE *optFile, int r, const TCHAR *pakfile)
{
  LOG IconSet = 1;
  DetailLevel = 3;
  ButtonSize = 4;
  Background = 1;
  if (optFile) {
    #define o(en,min,max) Exit = r ? !fread (&en, sizeof (en), 1, optFile) \
                                   : !fwrite (&en, sizeof (en), 1, optFile);
    OPTIONS
    o (clat, 0, 0)
    o (clon, 0, 0)
    o (zoom, 0, 0)
    o (tlat, 0, 0)
    o (tlon, 0, 0)
    o (flat, 0, 0)
    o (flon, 0, 0)
    #undef o
    option = mapMode;
  }
  LOG if (r) ChangePak (pakfile, clon, clat); // This will set up Exit
  LOG if (Exit && r) ChangePak (NULL, clon, clat);
/*  char *tag = gosmData +
    *(int *)(ndBase + hashTable[bucketsMin1 + (bucketsMin1 >> 7) + 2]);
  while (*--tag) {}
  SetLocation (((wayType*)tag)[-1].clon, ((wayType*)tag)[-1].clat);
  zoom = ((wayType*)tag)[-1].dlat + ((wayType*)tag)[-1].dlon + (1 << 15); */
}

#ifndef NOGTK

//----------------------------- Load Control ------------------------------
// The idea is keep everything (pak file + temp data) in RAM and never
// refuse a request. Only when calculations indicate that resources are
// low will the oldest process return early with an incomplete (jump)
// route. A number of constants are hard coded.
#define MAX_INST 170
struct ldCtrlType {
  int free;
  struct {
    struct timeval start;
    volatile int maks; // Reads from this value should be atomic
  } inst[MAX_INST]; // A circular buffer
} *ld;

void UpdateLdCtrl (int i, int calls, int li, int aliveCnt, int cpus)
{
  do {
  // From the newest process to the oldest
    i -= i == 0 ? 1 - MAX_INST : 1;
    if (ld->inst[i].start.tv_sec) {
      // Now we wind the clock back to the point where instance i started.
      // We make the calculation as if aliveCnt+1 equal instances were eating
      // memory from that point on. So we adjust calls for the amount of
      // memory the that wasn't consumed due to the later starting times of
      // the newer processes. A 2.2 Ghz Xeon takes roughly 12ms to complete
      // a call, but we make it 19ms to be safe.
      calls += ((ld->inst[li].start.tv_sec - ld->inst[i].start.tv_sec) *
        1000000 + ld->inst[li].start.tv_usec - ld->inst[i].start.tv_usec)
        / 19000 * aliveCnt;
      li = i; // li is oldest process still alive that was started after i.
      ld->inst[i].maks = aliveCnt > cpus / 2 ? 0 : calls / ++aliveCnt;
      //fprintf (stderr, "Setting maks for %d to %d\n", i, calls / aliveCnt);
    }
  } while (i != ld->free);
}

int UserInterface (int argc, char *argv[], 
		   const char* pakfile, const char* stylefile) {

  option = mapMode;
  IconSet = 1;
  DetailLevel = 2;
  FastestRoute = 1;
  Background = 1;
  Vehicle = motorcarR;
  char *h = getenv ("HOME");
  string optFname = string (h ? h : ".") + "/.gosmore.opt";
  FILE *optFile = fopen (optFname.c_str(), "r+");
  if (strcmp (pakfile, "nopak") == 0 && h) {
    string ddir (string (h) + "/.gosmore");
    mkdir (ddir.c_str (), 0755); // Usually already exists
    if (chdir (ddir.c_str ())) perror ("chdir(\".gosmore\")");
    SerializeOptions (optFile, TRUE, NULL);
  }
  else SerializeOptions (optFile, TRUE, pakfile);
  Keyboard = 1;
  if (Exit) {
    fprintf (stderr, "Cannot read %s\n"
	     "You can (re)build it from\n"
	     "the planet file e.g. " 
	     "bzip2 -d planet-...osm.bz2 | %s rebuild\n",
	     pakfile, argv[0]);
    #ifndef HEADLESS
    gtk_init (&argc, &argv);
    gtk_dialog_run (GTK_DIALOG (gtk_message_dialog_new (NULL,
      GTK_DIALOG_MODAL, GTK_MESSAGE_ERROR, GTK_BUTTONS_OK,
      "Cannot read %s\nYou can (re)build it from\n"
      "the planet file e.g. bzip2 -d planet-...osm.bz2 | %s rebuild\n",
      pakfile, argv[0])));
    #endif
    return 8;
  }
  #if 0
  /* This code give an idea for the order of magnitude of the theorical
     limit of exhaustive search routing */
  __int64 total = 0;
  printf ("%d\n", hashTable[bucketsMin1 + 1]);
  for (ndType *nd = ndBase; nd < ndBase + hashTable[bucketsMin1 + 1]; nd++) {    
    if (nd->other[1]) total += lrint (sqrt (    
      Sqr ((__int64)(nd->lat - (nd + nd->other[1])->lat)) +    
      Sqr ((__int64)(nd->lon - (nd + nd->other[1])->lon))) *
      Style (Way (nd))->invSpeed[motorcarR]);
  }
  printf ("%lf\n", (double) total);
  exit (0);
  #endif

  if (stylefile) {
#ifndef _WIN32
    GosmLoadAltStyle(stylefile,FindResource("icons.csv"));
#else
    fprintf(stderr, "Overiding style information is not currently supported"
	    " in Windows.");
#endif
  }


  if (getenv ("QUERY_STRING")) {
    double x0, y0, x1, y1;
    char vehicle[20];
    sscanf (getenv ("QUERY_STRING"),
      "flat=%lf&flon=%lf&tlat=%lf&tlon=%lf&fast=%d&v=%19[a-z]",
      &y0, &x0, &y1, &x1, &FastestRoute, vehicle);
    flat = Latitude (y0);
    flon = Longitude (x0);
    tlat = Latitude (y1);
    tlon = Longitude (x1);
    #define M(v) if (strcmp (vehicle, #v) == 0) Vehicle = v ## R;
    RESTRICTIONS
    #undef M
    Route (TRUE, 0, 0, Vehicle, FastestRoute);
    #ifdef LD_CTRL
    struct timezone tz;
    FILE *f = fopen ("ld_ctrl", "r+");
    if (!f) {
      if ((f = fopen ("ld_ctrl", "w+")) == NULL) return 2;
      for (size_t i = 0; i < sizeof (*ld); i++) fputc (0, f);
      fflush (f);
    }
    if (!f || (char*)-1L == (char*) (ld = 
             (ldCtrlType *) mmap (NULL, sizeof (*ld), PROT_READ | PROT_WRITE,
                                  MAP_SHARED, fileno (f), 0))
        //|| lockf (F_LOCK, fileno (f), sizeof (*ld)) != 0) {
		|| lockf (fileno (f), F_LOCK, sizeof (*ld)) != 0) {
      printf ("Content-Type: text/plain\n\r\n\rLd ctrl error%p %p %s\n\r",
        f, ld, strerror (errno));
      return 0;
    }
    int calls = (sysconf (_SC_PAGESIZE) / 4096 * sysconf (_SC_PHYS_PAGES)
      - 2200000) / 1500, myInst = ld->free;
    // Calculate how much memory the machine has left after assuming the pak
    // file(s) are in RAM. Then calculate how many times RouteLoop() can be
    // called across all instances under the assumption that each call will
    // consume roughly 6MB of RAM.
    //fprintf (stderr, "Starting instance %d\n", ld->free);
    gettimeofday (&ld->inst[myInst].start, &tz);
    if (++ld->free >= MAX_INST) ld->free = 0;
    UpdateLdCtrl (ld->free, calls, 0, 0, sysconf (_SC_NPROCESSORS_ONLN));
    //lockf (F_ULOCK, fileno (f), sizeof (*ld));
	lockf (fileno (f), F_ULOCK, sizeof (*ld));
    for (int i = 0; i < ld->inst[myInst].maks && RouteLoop (); i++) {}
    //lockf (F_LOCK, fileno (f), sizeof (*ld));
	lockf (fileno (f), F_LOCK, sizeof (*ld));
    ld->inst[myInst].start.tv_sec = 0; // Mark that we're done.
    UpdateLdCtrl (ld->free, calls, 0, 0, sysconf (_SC_NPROCESSORS_ONLN));
    //lockf (F_ULOCK, fileno (f), sizeof (*ld));
	lockf (fileno (f), F_ULOCK, sizeof (*ld));
    #else
    while (RouteLoop ()) {}
/*  It is reasonable to assume that there exists a short route in the actual
    road network between any two points on the that are closer than 1km to
    each. Therefore, once we come close to 'to', we should find a route
    quickly thereafter. If we don't it means 'to' is in a small, unlinked part
    of the network and trying to complete the route is wasting time.
    
    Well, I guess that in developing countries there are many sets of roads
    that run on opposite sides of rivers with no bridge or tunnel to connect
    them. So I'm commenting it out, because it's things like these that
    have bit me in the past.
    for (int i = 4000; i > 0 && RouteLoop (); i--) {
      if (flat - 100000 < shortest->nd->lat && shortest->nd->lat < flat + 100000 &&
          flon - 100000 < shortest->nd->lon && shortest->nd->lon < flon + 100000 &&
          i > 50) i = 50;
    } */
    #endif
    printf ("Content-Type: text/plain\n\r\n\r");
    if (!shortest) printf ("No route found\n\r");
    else {
      if (!routeSuccess) printf ("Jump\n\r");
      styleStruct *firstS = Style (Way (shortest->nd));
      double ups = firstS->invSpeed[Vehicle] / 3.6 
          * firstS->aveSpeed[Vehicle] / 20000000 * 2147483648.0 / 
          cos (LatInverse (flat / 2 + tlat / 2) * (M_PI / 180));
      // ups (Units per second) also works as an unsigned int.
      double fSegLat = shortest->shortest ?
        shortest->shortest->nd->lat - shortest->nd->lat : 1;
      double fSegLon = shortest->shortest ?
        shortest->shortest->nd->lon - shortest->nd->lon : 1;
      double fpr = (fSegLat * (flat - shortest->nd->lat) +
                    fSegLon * (flon - shortest->nd->lon)) /
                   (Sqr (fSegLat) + Sqr (fSegLon));
      fpr = fpr > 1 ? 1 : fpr < 0 ? 0 : fpr; // Clamp to [0,1]
      for (; shortest; shortest = shortest->shortest) {
        wayType *w = Way (shortest->nd);
        char *name = (char*)(w + 1) + 1;
        unsigned style= StyleNr(w);
        printf ("%lf,%lf,%c%s,%s,%.0lf,%.*s\n\r",
          LatInverse (shortest->nd->lat + fSegLat * fpr),
          LonInverse (shortest->nd->lon + fSegLon * fpr), JunctionType (shortest->nd),
          ((1 << roundaboutR) & (Way (shortest->nd))->bits) ? "r" : "",
          style < sizeof(klasTable)/sizeof(klasTable[0]) ? klasTable[style].desc :
          "(unknown-style)", ((shortest->heapIdx < 0
          ? -shortest->heapIdx : routeHeap[shortest->heapIdx].best) -
          shortest->remain) / ups,
          (int) strcspn (name, "\n"), name);
        fpr = 0;
        if (!shortest->shortest) {
          // I don't know why, but sometimes shortest->dir is wrong. But
          // AFAIK it only happens at the first or last segment of the way,
          // so inverting 'dir' solves the problem...
          ndType *final = shortest->nd + shortest->nd->other[
            (shortest->nd->other[shortest->dir] ? 0 : 1) ^ shortest->dir];
          double pr = ((final->lat - shortest->nd->lat) * (double)
            (tlat - shortest->nd->lat) + (final->lon - shortest->nd->lon) *
            (double)(tlon - shortest->nd->lon)) /
            (Sqr ((double)(final->lat - shortest->nd->lat)) +
             Sqr ((double)(final->lon - shortest->nd->lon)) + 1);
          pr = pr > 1 ? 1 : pr < 0 ? 0 : pr; // Clamp to [0,1]
          printf("%lf,%lf,j,(unknown-style),0,fini\n\r",
      LatInverse (shortest->nd->lat + pr * (final->lat - shortest->nd->lat)),
      LonInverse (shortest->nd->lon + pr * (final->lon - shortest->nd->lon)));
//      shortest->dir, shortest->nd->other[shortest->dir]);
//      shortest->nd->other[1-shortest->dir]);
      //final->lat - shortest->nd->lat, final->lon - shortest->nd->lon);
        }
      }
    }
    return 0;
  }

  printf ("%s is provided as is and any warranties are disclaimed. \n",argv[0]);
  #ifndef HEADLESS

  curl_global_init (CURL_GLOBAL_ALL);
  g_thread_init (NULL);  // Something to do with curl progress bar
  gtk_init (&argc, &argv);
#ifdef USE_GNOMESOUND
  gnome_sound_init ("localhost");
#endif
  draw = gtk_drawing_area_new ();
  gtk_widget_set_double_buffered (draw, FALSE);
  gtk_signal_connect (GTK_OBJECT (draw), "expose_event",
    (GtkSignalFunc) DrawExpose, NULL);
  gtk_signal_connect (GTK_OBJECT (draw), "button-release-event",
    (GtkSignalFunc) Click, NULL);
  gtk_signal_connect (GTK_OBJECT (draw), "motion_notify_event",
    (GtkSignalFunc) Drag, NULL);
  gtk_widget_set_events (draw, GDK_EXPOSURE_MASK | GDK_BUTTON_RELEASE_MASK |
    GDK_BUTTON_PRESS_MASK |  GDK_POINTER_MOTION_MASK);
  gtk_signal_connect (GTK_OBJECT (draw), "scroll_event",
                       (GtkSignalFunc) Scroll, NULL);

  static GtkTargetEntry drawDndTargets[] = {
    { (gchar*) "text/uri-list", 0, 0 },
    { (gchar*) "image/png", 0, 1 /* Will autodetect type on drop */ },
    { (gchar*) "image/ico", 0, 1 },
  };
  gtk_drag_dest_set (draw, GTK_DEST_DEFAULT_ALL /* GTK_DEST_DEFAULT_MOTION | GTK_DEST_DEFAULT_HIGHLIGHT*/, drawDndTargets,
    G_N_ELEMENTS (drawDndTargets), GDK_ACTION_COPY);
                       
//  g_signal_connect (draw, "drag-data-received", G_CALLBACK (DropReceived), NULL);
//  g_signal_connect (draw, "drag-drop", G_CALLBACK (DropOnDraw), NULL);
//  ReadTsList ();
  
  GtkWidget *window = gtk_window_new (GTK_WINDOW_TOPLEVEL);
  gtk_signal_connect (GTK_OBJECT (window), "focus-in-event",
                       (GtkSignalFunc) UpdateWayPoints, NULL);
  /* The new layout will work better on smaller screens esp. touch screens by
  moving less used options to the menu and only displaying search results
  when they are required. It will also be more familiar to casual users
  because it will resemble a webbrowser */
  GtkWidget *hbox = gtk_hbox_new (FALSE, 3), *vbox = gtk_vbox_new (FALSE, 0);
  gtk_container_add (GTK_CONTAINER (window), vbox);
  gtk_box_pack_start (GTK_BOX (vbox), hbox, FALSE, FALSE, 0);

  GtkWidget *btn[3];
  for (int i = 0; i < 3; i++) {
    btn[i] = gtk_button_new_with_label (i == 0 ? "O" : i == 1 ? "-" : "+");
    gtk_widget_set_size_request (btn[i], 27, 20);
    gtk_box_pack_start (GTK_BOX (hbox), btn[i], FALSE, FALSE, 5);
    //gtk_widget_show (btn[i]);
    gtk_signal_connect (GTK_OBJECT (btn[i]), "clicked",
      GTK_SIGNAL_FUNC (HitGtkButton), (char*)i);
  }  

  searchW = gtk_entry_new ();
  gtk_box_pack_start (GTK_BOX (hbox), searchW, FALSE, FALSE, 5);
  gtk_entry_set_text (GTK_ENTRY (searchW), "Search");
  gtk_signal_connect (GTK_OBJECT (searchW), "changed",
    GTK_SIGNAL_FUNC (IncrementalSearch), NULL);
  gtk_signal_connect (GTK_OBJECT (searchW), "button-press-event",
    GTK_SIGNAL_FUNC (ToggleSearchResults), NULL);

  gtk_box_pack_start (GTK_BOX (vbox), draw, TRUE, TRUE, 0);
  
  location = gtk_entry_new ();
  gtk_box_pack_start (GTK_BOX (vbox), location, FALSE, FALSE, 5);
  gtk_signal_connect (GTK_OBJECT (location), "changed",
    GTK_SIGNAL_FUNC (ChangeLocation), NULL);
  
  display3D = gtk_toggle_button_new_with_label ("3D");
  gtk_box_pack_start (GTK_BOX (hbox), display3D, FALSE, FALSE, 5);
  gtk_signal_connect (GTK_OBJECT (display3D), "clicked",
    GTK_SIGNAL_FUNC (ChangeOption), NULL);
  //gtk_widget_show (display3D);

  followGPSr = gtk_toggle_button_new_with_label ("Lock");
  
  #ifdef USE_GEOCLUE // Not used and never worked
  g_type_init ();
  GeoclueMaster *master = geoclue_master_get_default ();
  Gerror *error = NULL;
  GeoclueMasterClient *client =
    geoclue_master_create_client (master, NULL, &error);
  g_object_unref (master);
  if (client) {
//    if (!geoclue_master_client_set_requirements (client,
//      GEOCLUE_ACCURACY_LEVEL_LOCALITY, 0, TRUE, GEOCLUE_RESOURSE
    GeocluePosition *pos = geoclue_master_client_create_position (client, NULL);
    g_signal_connect (G_OBJECT (pos), "position-changed",
      G_CALLBACK (GeoclueUpdate), NULL);
    gtk_box_pack_start (GTK_BOX (hbox), followGPSr, FALSE, FALSE, 5);
    gtk_signal_connect (GTK_OBJECT (followGPSr), "clicked",
      GTK_SIGNAL_FUNC (ChangeOption), NULL);
  }
  #endif
  
  //#if !defined (_WIN32) && !defined (ROUTE_TEST)
  #if 0
  struct sockaddr_in sa;
  int gpsSock = socket (PF_INET, SOCK_STREAM, 0);
  sa.sin_family = AF_INET;
  sa.sin_port = htons (2947);
  sa.sin_addr.s_addr = htonl (0x7f000001); // (204<<24)|(17<<16)|(205<<8)|18;
  if (gpsSock != -1 &&
      connect (gpsSock, (struct sockaddr *)&sa, sizeof (sa)) == 0) {
    send (gpsSock, "R\n", 2, 0);
    gpsSockTag = gdk_input_add (/*gpsData->gps_fd*/ gpsSock, GDK_INPUT_READ,
      (GdkInputFunction) ReceiveNmea /*gps_poll*/, NULL);
  #endif
  #ifdef USE_GPSD
  gps_data_t *gpsData = gps_open ("127.0.0.1", "2947");
  if (gpsData) {
    gps_set_raw_hook (gpsData, GpsMove);
    #if GPSD_API_MAJOR_VERSION <= 3
    gps_query (gpsData, "w+x\n");
    #else
    gps_stream (gpsData, WATCH_ENABLE, NULL);
    #endif
    gpsSockTag = gdk_input_add (gpsData->gps_fd, GDK_INPUT_READ,
      (GdkInputFunction) gps_poll, gpsData);
    
    gtk_box_pack_start (GTK_BOX (hbox), followGPSr, FALSE, FALSE, 5);
    gtk_signal_connect (GTK_OBJECT (followGPSr), "clicked",
      GTK_SIGNAL_FUNC (ChangeOption), NULL);
    //gtk_widget_show (followGPSr);
  }
  #endif

  GtkAdjustment *adj = (GtkAdjustment*) gtk_adjustment_new (0, 0, 100, 0, 0, 0);
  bar = gtk_progress_bar_new_with_adjustment (adj);
  gtk_container_add (GTK_CONTAINER (hbox), bar);

  gtk_signal_connect (GTK_OBJECT (window), "delete_event",
    GTK_SIGNAL_FUNC (gtk_main_quit), NULL);
  
  gtk_window_set_default_size (GTK_WINDOW (window), 550, 550);
//  gtk_widget_show (searchW);
//  gtk_widget_show (location);
//  gtk_widget_show (draw);
/*  gtk_widget_show (getDirs); */
//  gtk_widget_show (hbox);
//  gtk_widget_show (vbox);
  gtk_widget_show_all (window);

/*  GtkWidget *styleEditor = gtk_window_new (GTK_WINDOW_TOPLEVEL);
  GtkWidget *seScroll = gtk_scrolled_window_new (NULL, NULL);
  //gtk_scrolled_window_set_policy (GTK_SCROLLED_WINDOW (seScroll), );
  gtk_container_add (GTK_CONTAINER (styleEditor), seScroll);
  #define TCOLS 6
  GtkWidget *seTable = gtk_table_new (100, TCOLS, FALSE);
  gtk_scrolled_window_add_with_viewport (GTK_SCROLLED_WINDOW (seScroll), seTable);
  for (int i = 0; i < 100; i++) {
    GtkWidget *v[TCOLS];
    v[0] = gtk_label_new ("abcdefghijl" + i % 5);
    v[1] = gtk_label_new ("01234567" + i % 5);
//    GdkColor c = { 9999, 8888, 7777, 6666 };
    v[2] = gtk_color_button_new ();
    v[3] = gtk_font_button_new ();
    v[4] = gtk_spin_button_new_with_range (0, 50, 1);
    v[5] = gtk_toggle_button_new_with_label ("- -");
    const char *sName[] = { NULL, NULL, "color-set", "font-set", "value-changed", "clicked" };
    for (int j = 0; j < TCOLS; j++) {
    //gtk_clist_append (GTK_CLIST (seList), (gchar**) v);
      gtk_table_attach_defaults (GTK_TABLE (seTable), v[j], j, j + 1, i, i + 1);
      if (sName[j]) gtk_signal_connect (GTK_OBJECT (v[j]), sName[j],
        GTK_SIGNAL_FUNC (SeUpdate), (gpointer) (intptr_t) (i * 0x100 + j));
    }
  }
  gtk_widget_show_all (styleEditor); */

  ChangeOption ();
  IncrementalSearch ();
  gtk_widget_grab_focus (searchW);
  gdk_threads_enter (); // Something to do with curl progress bar
  gtk_main ();
  gdk_threads_leave (); // Something to do with curl progress bar
  FlushGpx ();
  if (optFile) rewind (optFile);
  else optFile = fopen (optFname.c_str (), "w");
  SerializeOptions (optFile, FALSE, NULL);
  
  #endif // HEADLESS
  return 0;
}

int main (int argc, char *argv[])
{  
  int nextarg = 1;
  bool rebuild = false;
  const char* master = "";
  int bbox[4] = { INT_MIN, INT_MIN, 0x7fffffff, 0x7fffffff };
  
  setlocale (LC_NUMERIC, "C"); /* Ensure decimal sign is "." for NMEA parsing. */
  
  if (argc > 1 && stricmp (argv[1], "sortRelations") == 0) {
    return SortRelations ();
  }
  if (argc > 1 && stricmp(argv[1], "rebuild") == 0) {
    if (argc < 6 && argc > 4) {
      fprintf (stderr, 
	       "Usage : %s [rebuild [bbox for 2 pass]] [pakfile [stylefile]]\n"
	       "See http://wiki.openstreetmap.org/index.php/gosmore\n", 
	       argv[0]);
      return 1;
    }
    rebuild=true;
    nextarg++;
    if (argc >= 6) {
      master = FindResource("master.pak");
      bbox[0] = Latitude (atof (argv[2]));
      bbox[1] = Longitude (atof (argv[3]));
      bbox[2] = Latitude (atof (argv[4]));
      bbox[3] = Longitude (atof (argv[5]));
      nextarg += 4;
    }
  }
  
  // check if a pakfile was specified on the command line
  const char* pakfile;
  const char* stylefile = NULL;
  if (argc > nextarg) {
    pakfile=argv[nextarg];
    nextarg++;
    if (argc > nextarg)  {
      stylefile=argv[nextarg];
    } else if (rebuild) { 
      stylefile=FindResource("elemstyles.xml");
    }
  } else {
    pakfile=FindResource("gosmore.pak");
    if (rebuild) {
      stylefile=FindResource("elemstyles.xml");
    }
  }
  
  if (rebuild) {
#ifndef _WIN32
    printf("Building %s using style %s...\n",pakfile,stylefile);

    RebuildPak(pakfile, stylefile, FindResource("icons.csv"), master, bbox);
#else
    fprintf(stderr,"Pakfile rebuild is not currently supported in Windows.\n");
#endif
  }

  return UserInterface (argc, argv, pakfile, stylefile);

  // close the logfile if it has been opened. No. Rather let libc to it.
  //if (logFP(false)) fclose(logFP(false));
}
#elif _WIN32 // NOGTK / WIN32 and WINCE Native;
//-------------------------- WIN32 and WINCE Native ------------------
HANDLE port = INVALID_HANDLE_VALUE;

HBITMAP bmp = NULL, bufBmp = NULL;
HDC iconsDc, bufDc;
HPEN pen[2 << STYLE_BITS];
HBRUSH brush[2 << STYLE_BITS];
UTF16 appendTmp[50];

LRESULT CALLBACK MainWndProc(HWND hWnd,UINT message,
                                  WPARAM wParam,LPARAM lParam)
{
  PAINTSTRUCT ps;
  RECT rect;
  //static wchar_t msg[200] = TEXT("No coms");
  int topBar = Layout != 1 ? 30 : 0;
  static int updatePercent = 0;

  switch(message) {
    #if 0
    case WM_HOTKEY:
      if (VK_TBACK == HIWORD(lParam) && (0 != (MOD_KEYUP & LOWORD(lParam)))) {
        PostQuitMessage (0);
      }
      break;

    case WM_ACTIVATE:
      // Ensure that unwanted wince elements are hidden
      if (SHFullScreenPtr) {
	if (FullScreen) {
	  (*SHFullScreenPtr)(mWnd, SHFS_HIDETASKBAR |
			     SHFS_HIDESTARTICON | SHFS_HIDESIPBUTTON);
	} else {
	  (*SHFullScreenPtr)(mWnd, SHFS_HIDESIPBUTTON);
	}
      }
      break;
    #endif
 
    case WM_CREATE:
      LOG for (int i = 0; i < 3; i++) {
        buttons[i] = CreateWindow(TEXT ("BUTTON"), i == 0 ? TEXT ("O") :
                      i == 1 ? TEXT ("-") : TEXT ("+"), BS_PUSHBUTTON |
                      WS_CHILD | WS_VISIBLE | WS_TABSTOP,
                      0, 0, 0, 0, hWnd, (HMENU) (IDC_EDIT1 + 1 + i),
                      (HINSTANCE) GetWindowLong(hWnd, GWL_HINSTANCE), 
                      NULL);       // pointer not needed 
      }
      LOG button3D = CreateWindow(TEXT ("BUTTON"), TEXT ("3D"), BS_CHECKBOX |
                    WS_CHILD | WS_VISIBLE | WS_TABSTOP,
                    0, 0, 0, 0, hWnd, (HMENU) (IDC_EDIT1 + 1 + 3),
                    (HINSTANCE) GetWindowLong(hWnd, GWL_HINSTANCE), 
                    NULL);       // pointer not needed 
      LOG hwndEdit = CreateWindow(TEXT ("EDIT"),
                    NULL, WS_CHILD | WS_VISIBLE | WS_BORDER | ES_LEFT,
                    0, 0, 0, 0,  // set size in WM_SIZE message
                    hWnd, (HMENU) IDC_EDIT1/*ID_EDITCHILD*/,
                    (HINSTANCE) GetWindowLong(hWnd, GWL_HINSTANCE), 
                    NULL);       // pointer not needed 
      if (Keyboard) SendMessage (hwndEdit, WM_SETTEXT, 0, (LPARAM) TEXT ("Search")); 
      //else SetClassLongPtr (hwndEdit, GCLP_HBRBACKGROUND, (LONG) GetStockObject (WHITE_BRUSH));
//      SendMessage (hwndEdit, EM_SETEVENTMASK, 0, ENM_UPDATE | ENM_SETFOCUS);
      break;
    case WM_SETFOCUS: 
      if (Keyboard) SetFocus(hwndEdit); 
      break;
    case WM_SIZE: 
      LOG draw->allocation.width = LOWORD (lParam);
      LOG draw->allocation.height = HIWORD (lParam) - topBar;
      if (Keyboard) MoveWindow (hwndEdit, Layout > 1 ? 8 : 140, topBar - 25,
        draw->allocation.width - (Layout > 1 ? 66 : 200), 20, TRUE);
      LOG MoveWindow(button3D, draw->allocation.width - 55,
        Layout != 1 ? 5 : -25, 50, 20, TRUE);
      for (int i = 0; i < 3; i++) { // Same as LBUTTON_UP. Put in function !!
        LOG MoveWindow (buttons[i], (2 * i + 1) * 70 / 3 - 15,
          Layout ? -25 : 5, 30, 20, TRUE);
      }
      LOG if (bufBmp) {
        DeleteObject (bufBmp);
        bufBmp = NULL;
      }
      LOG InvalidateRect (hWnd, NULL, FALSE);
      break;
    case WM_DESTROY:
      LOG PostQuitMessage(0);
      break;
    /*case WM_CTLCOLORSTATIC: // Tried to make hwndEdit a STATIC when !Keyboard
      SetBkMode ((HDC)wParam, TRANSPARENT);
      return (LONG) GetStockObject (WHITE_BRUSH); */
    case WM_PAINT:
      do { // Keep compiler happy.
        BeginPaint (hWnd, &ps);
      //GetClientRect (hWnd, &r);
      //SetTextColor(ps.hdc,(i==state)?RGB(0,128,0):RGB(0,0,0));
      //r.left = 50;
      // r.top = 50;
	if (bmp == NULL) {
          LOG bmp = LoadBitmap (hInst, MAKEINTRESOURCE (IDB_BITMAP1));
          LOG iconsDc = CreateCompatibleDC (ps.hdc);
          LOG SelectObject(iconsDc, bmp);

	  // get mask for iconsDc
	  LOG bmp = LoadBitmap (hInst, MAKEINTRESOURCE (IDB_BITMAP2));
	  LOG maskDc = CreateCompatibleDC (ps.hdc);
	  LOG SelectObject(maskDc, bmp);

          LOG bufDc = CreateCompatibleDC (ps.hdc); //bufDc //GetDC (hWnd));
          /*pen[ROUTE_PEN] = CreatePen (PS_SOLID, 6, 0x00ff00);
	  pen[VALIDATE_PEN] = CreatePen (PS_SOLID, 10, 0x9999ff); */
	  map<int,HPEN> pcache;
	  map<int,HBRUSH> bcache;
          LOG for (int i = 0; i < stylecount; i++) {
	    // replace line colour with area colour 
	    // if no line colour specified
	    int c = style[i].lineColour != -1 ? style[i].lineColour
	      : (style[i].areaColour & 0xfefefe) >> 1; 
            if (c != -1) {
	      // logprintf ("PEN[%d] %d %x %d\n",i,style[i].dashed, c, style[i].lineWidth);
              int idx = (style[i].dashed ? 1 : 0) +
                (style[i].lineWidth & 0x3f) * 2 + ((c & 0xffffff) << 7);
              map<int,HPEN>::iterator f = pcache.find (idx);
              pen[i] = f != pcache.end() ? f->second :
                CreatePen (style[i].dashed ? PS_DASH : PS_SOLID,
			 max (1, style[i].lineWidth), (c >> 16) |
			 (c & 0xff00) |
			 ((c & 0xff) << 16));
              pcache[idx] = pen[i];
            }
            if ((c = style[i].areaColour) != -1) {
	      // logprintf ("BR[%d] %x\n", i, c);
              map<int,HBRUSH>::iterator f = bcache.find (c);
              brush[i] = f != bcache.end () ? f->second :
                CreateSolidBrush ((c>>16) | (c&0xff00) | ((c&0xff) << 16));
              bcache[c] = brush[i];
            }
          }
          LOG sysFont = (HFONT) GetStockObject (SYSTEM_FONT);
          LOG GetObject (sysFont, sizeof (logFont), &logFont);
          #ifndef _WIN32_WCE
          logFont.lfWeight = 400;
          strcpy (logFont.lfFaceName, TEXT ("Arial"));
          /*#else
          logFont.lfWeight = 400; // TODO ******** Testing WM6
          wcscpy (logFont.lfFaceName, TEXT ("Arial")); */
          #endif
          LOG SetBkMode (bufDc, TRANSPARENT); // Is this really necessary ?
        }
	rect.top = rect.left = 0;
	rect.right = draw->allocation.width;
	rect.bottom = draw->allocation.height;
        if (bufBmp == NULL) { // i.e. after WM_SIZE
          LOG bufBmp = CreateCompatibleBitmap (ps.hdc, draw->allocation.width,
            draw->allocation.height);
          LOG SelectObject (bufDc, bufBmp);
          LOG FillRect (bufDc, &rect, (HBRUSH) GetStockObject(WHITE_BRUSH));
        }
	mygc = bufDc;
	icons = iconsDc;
	if (option == BackgroundNum) {
	 FillRect (bufDc, &rect,
	   brush[firstElemStyle + Background - (Background > 8 ? 8 : 0)]);
        }
        DrawExpose (pen, brush);
        
	BitBlt (ps.hdc, 0, topBar, rect.right,  rect.bottom, bufDc, 0, 0, SRCCOPY);
	if (updatePercent) {
	  MoveToEx (ps.hdc, 0, topBar, NULL);
	  LineTo (ps.hdc, updatePercent * draw->allocation.width / 1000, topBar);
	}
      //SetBkColor(ps.hdc,RGB(63,63,63));
	FillRect (bufDc, &rect, brush[firstElemStyle + Background - (Background > 8 ? 8 : 0)]);
	 //(HBRUSH) GetStockObject(WHITE_BRUSH));
	rect.bottom = topBar;
	FillRect (ps.hdc, &rect, (HBRUSH) GetStockObject(WHITE_BRUSH));
	if (!Keyboard) {
          UTF16 wcTmp[70], *tStart = (UTF16 *) wcTmp;
          const unsigned char *sStart = (const unsigned char*) searchStr.c_str ();
          if (ConvertUTF8toUTF16 (&sStart, sStart + searchStr.length (),
                &tStart, tStart + sizeof (wcTmp) / sizeof (wcTmp[0]), lenientConversion)
              == conversionOK) {
            //SendMessage (hwndEdit, WM_SETTEXT, 0, (LPARAM) (wchar_t*) wcTmp);
            ExtTextOutW (ps.hdc, Layout > 1 ? 8 : 140, topBar - 25, 0, NULL,
              (wchar_t*) wcTmp, tStart - wcTmp, NULL);
          }
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
      break;
    case WM_USER + 1:
      /*
      wsprintf (msg, TEXT ("%c%c %c%c %9.5lf %10.5lf %lf %lf"),
        gpsNew.fix.date[0], gpsNew.fix.date[1],
        gpsNew.fix.tm[4], gpsNew.fix.tm[5],
        gpsNew.fix.latitude, gpsNew.fix.longitude, gpsNew.fix.ele,
	gpsNew.fix.hdop); */
      DoFollowThing ((gpsNewStruct*)lParam);
      if (FollowGPSr) InvalidateRect (hWnd, NULL, FALSE);
      break;
    case WM_USER + 2:
       do {
         HDC wdc = GetDC (hWnd);
         updatePercent = lParam;
         MoveToEx (wdc, 0, topBar, NULL);
         LineTo (wdc, updatePercent * draw->allocation.width / 1000, topBar);
         ReleaseDC (hWnd, wdc);
       } while (0);
       break;
    case WM_LBUTTONDOWN:
      //pressTime = GetTickCount ();
      SetCapture (hWnd);
      break;
    case WM_LBUTTONUP:
      ReleaseCapture ();
      if (gDisplayOff) {
        CeEnableBacklight(TRUE);
        gDisplayOff = FALSE;
        break;
      }
      if (1) {
        MouseEv ((short) LOWORD (lParam), (short) HIWORD (lParam) - topBar,
          GetTickCount (), 1, TRUE);
        if (option == LayoutNum) {
          if (Keyboard) MoveWindow(hwndEdit, Layout > 1 ? 8 : 140,
            Layout != 1 ? 5 : -25,
            draw->allocation.width - (Layout > 1 ? 66 : 200), 20, TRUE);
          MoveWindow(button3D, draw->allocation.width - 55,
            Layout != 1 ? 5 : -25, 50, 20, TRUE);
          for (int i = 0; i < 3; i++) { // Same as WM_SIZE. Put in function !!
            MoveWindow (buttons[i], (2 * i + 1) * 70 / 3 - 15,
              Layout ? -25 : 5, 30, 20, TRUE);
          }
        }
        if (Keyboard && option != searchMode) SipShowIM (SIPF_OFF);
      }
      else if (!Keyboard && LOWORD (lParam) > (Layout > 1 ? 8 : 140)) {
        option = option == searchMode ? mapMode : searchMode;
      }
      InvalidateRect (hWnd, NULL, FALSE);
      break;
    case WM_MOUSEMOVE:
      if (wParam & MK_LBUTTON) {
        MouseEv ((short) LOWORD (lParam), (short) HIWORD (lParam) - topBar,
                 GetTickCount (), 1, FALSE);
        InvalidateRect (hWnd, NULL, FALSE);
      }
/*      if (wParam & MK_LBUTTON) {
        if (firstDrag[0] >= 0) {
          HDC wdc = GetDC (hWnd);
          int wadj = lastDrag[0] - LOWORD (lParam);
          int hadj = lastDrag[1] - HIWORD (lParam) + topBar;
          BitBlt (wdc, wadj < 0 ? -wadj : 0, (hadj < 0 ? -hadj : 0) + topBar, 
            draw->allocation.width - (wadj < 0 ? -wadj : wadj),
            draw->allocation.height + topBar - (hadj < 0 ? -hadj : hadj),
            wdc, wadj > 0 ? wadj : 0, (hadj > 0 ? hadj : 0) + topBar, SRCCOPY);
          ReleaseDC (hWnd, wdc);
        }
        lastDrag[0] = LOWORD (lParam);
        lastDrag[1] = HIWORD (lParam) - topBar;
        if (firstDrag[0] < 0) memcpy (firstDrag, lastDrag, sizeof (firstDrag));
      }*/
      break;
    case WM_MOUSEWHEEL:
      do {
        GdkEventScroll ev;
        POINT p;
        p.x = GET_X_LPARAM (lParam);
        p.y = GET_Y_LPARAM (lParam);
        ScreenToClient (hWnd, &p);
        ev.x = p.x;
        ev.y = p.y - topBar;
        
        ev.direction = GET_WHEEL_DELTA_WPARAM (wParam) > 0
          ? GDK_SCROLL_UP : GDK_SCROLL_DOWN;
        Scroll (NULL, &ev, NULL);
        InvalidateRect (hWnd, NULL, FALSE);
      } while (0);
      break;
    case WM_COMMAND:
      if (HIWORD (wParam) == BN_CLICKED &&
          LOWORD (wParam) > IDC_EDIT1 && LOWORD (wParam) <= IDC_EDIT1 + 3) {
        HitButton (LOWORD (wParam) - IDC_EDIT1 - 1);
        if (Keyboard && optionMode != searchMode) SipShowIM (SIPF_OFF);
        InvalidateRect (hWnd, NULL, FALSE);
      }
      if (HIWORD (wParam) == BN_CLICKED && LOWORD (wParam) == IDC_EDIT1 + 4) {
        Display3D ^= 1;
        Button_SetCheck (button3D, Display3D ? BST_CHECKED : BST_UNCHECKED);
        InvalidateRect (hWnd, NULL, FALSE);
      }
      if (HIWORD (wParam) == EN_UPDATE && LOWORD (wParam) == IDC_EDIT1) {
        char editStr[50];

        memset (appendTmp, 0, sizeof (appendTmp));
        #ifndef _WIN32_WCE
        Edit_GetLine (hwndEdit, 0, editStr, sizeof (editStr));
        if (1) {
        #else
        int wstrlen = Edit_GetLine (hwndEdit, 0, appendTmp, sizeof (appendTmp));
        unsigned char *tStart = (unsigned char*) editStr;
        const UTF16 *sStart = (const UTF16 *) appendTmp;
        if (ConvertUTF16toUTF8 (&sStart,  sStart + wstrlen,
              &tStart, tStart + sizeof (gosmSstr), lenientConversion)
            == conversionOK) {
          *tStart = '\0';
          /* SipShowIM (SIPF_ON); The only way we can get here without the
          IM showing is if the device has a hardware keyboard/keypak */
        #endif
          option = searchMode;
          GeoSearch (editStr);
          InvalidateRect (hWnd, NULL, FALSE);
        }
     }
	 break;
    default:
      return DefWindowProc (hWnd, message, wParam, lParam);
  }
  if (Exit) PostMessage (hWnd, WM_CLOSE, 0, 0);
  return FALSE;
}

BOOL InitApplication (void)
{
  WNDCLASS wc;

  wc.style=0;
  wc.lpfnWndProc=(WNDPROC)MainWndProc;
  wc.cbClsExtra=0;
  wc.cbWndExtra=0;
  wc.hInstance= hInst;
  wc.hIcon=LoadIcon(hInst, MAKEINTRESOURCE(ID_MAINICON)); 
  wc.hCursor=LoadCursor(NULL,IDC_ARROW);
  wc.hbrBackground=(HBRUSH) GetStockObject(WHITE_BRUSH);
  wc.lpszMenuName = NULL;
  wc.lpszClassName = TEXT ("GosmoreWClass");

  return(RegisterClass(&wc));
}

HWND InitInstance(int nCmdShow)
{
  HWND prev;
  // check if gosmore is already running
  prev = FindWindow(TEXT ("GosmoreWClass"), NULL);
  if (prev != NULL) {
    ShowWindow(prev, SW_RESTORE);
    SetForegroundWindow(prev);
    return FALSE;
  } else {
    
    mWnd = CreateWindow (TEXT ("GosmoreWClass"), TEXT ("gosmore"), 
    #ifdef _WIN32_WCE
    WS_DLGFRAME,
    #else
    WS_OVERLAPPEDWINDOW | WS_CLIPCHILDREN,
    #endif
			 CW_USEDEFAULT, CW_USEDEFAULT, CW_USEDEFAULT, 
			 CW_USEDEFAULT,NULL,NULL, hInst,NULL);
    
    if(!mWnd) return(FALSE);
    
    ShowWindow (mWnd,nCmdShow);
    //UpdateWindow (mWnd);
    
    
    return mWnd;
  }
}

volatile int guiDone = FALSE;

DWORD WINAPI NmeaReader (LPVOID lParam)
{
  static int lastgps=0;
  // loop back here if existing connection fails
  while (!guiDone) {
  #ifndef _WIN32_WCE
    Sleep (1000);
  #else
    // $GPGLL,2546.6752,S,02817.5780,E,210130.812,V,S*5B
    DWORD nBytes, got = 0;
    COMMTIMEOUTS commTiming;
    char rx[300];
    
    wchar_t portname[6];
    wsprintf (portname, TEXT ("COM%d:"), CommPort);

    logprintf ("Attempting first connect to CommPort.\n");

    // Attempt to reconnect to NMEA device every 1 second until connected
    while (!guiDone &&
	   (port=CreateFile (portname, GENERIC_READ | GENERIC_WRITE, 0,
		 NULL, OPEN_EXISTING, 0, 0)) == INVALID_HANDLE_VALUE) {
      Sleep(1000);
      //logprintf("Retrying connect to CommPort\n");
    }

    if (port != INVALID_HANDLE_VALUE) {

      logprintf("Connected to CommPort\n");
	  
#if 1
      GetCommTimeouts (port, &commTiming);
      commTiming.ReadIntervalTimeout = 20;
      commTiming.ReadTotalTimeoutMultiplier = 0;
      commTiming.ReadTotalTimeoutConstant = 200; /* Bailout when nothing on the port */
      
      commTiming.WriteTotalTimeoutMultiplier=5; /* No writing */
      commTiming.WriteTotalTimeoutConstant=5;
      SetCommTimeouts (port, &commTiming);
#endif
      if (BaudRate) {
	DCB portState;
	if(!GetCommState(port, &portState)) {
	  MessageBox (NULL, TEXT ("GetCommState Error"), TEXT (""),
		      MB_APPLMODAL|MB_OK);
	  return(1);
	}
	portState.BaudRate = BaudRate;
	//portState.Parity=0;
	//portState.StopBits=ONESTOPBIT;
	//portState.ByteSize=8;
	//portState.fBinary=1;
	//portState.fParity=0;
	//portState.fOutxCtsFlow=0;
	//portState.fOutxDsrFlow=0;
	//portState.fDtrControl=DTR_CONTROL_ENABLE;
	//portState.fDsrSensitivity=0;
	//portState.fTXContinueOnXoff=1;
	//portState.fOutX=0;
	//portState.fInX=0;
	//portState.fErrorChar=0;
	//portState.fNull=0;
	//portState.fRtsControl=RTS_CONTROL_ENABLE;
	//portState.fAbortOnError=1;
	
	if(!SetCommState(port, &portState)) {
	  MessageBox (NULL, TEXT ("SetCommState Error"), TEXT (""),
		      MB_APPLMODAL|MB_OK);
	  return(1);
	}
      }
      
      /* Idea for Windows Mobile 5
	 #include <gpsapi.h>
	 if (WM5) {
	 GPS_POSITION pos;
	 HANDLE hand = GPSOpenDevice (NULL, NULL, NULL, 0);
	 while (!guiDone && hand != NULL) {
	 if (GPSGetPosition (hand, &pos, 500, 0) == ERROR_SUCCESS &&
	 (pos.dwValidFields & GPS_VALID_LATITUDE)) {
	 Sleep (800);
	 pos.dblLatitude, pos.dblLongitude;
	 }
	 else Sleep (100);
	 }
	 if (hand) GPSCloseDevice (hand);
	 } */
      
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
      
      //char logName[80];
      //sprintf (logName, "%slog.nmea", docPrefix);
      //FILE *log = fopen (logName, "wb");

      // keep reading nmea until guiDone or serial port fails
      bool status;
      while (!guiDone &&
	     (status = ReadFile(port, rx + got, 
				     sizeof(rx) - got, &nBytes, NULL))) {
	//	logprintf ("status = %d, nBytes = %d\n", status, nBytes);
	if (nBytes > 0) {
	  got += nBytes;
	  //if (log) fwrite (rx, nBytes, 1, log);
	  
	  //wndStr[0]='\0';
	  //FormatMessage (FORMAT_MESSAGE_FROM_SYSTEM, NULL, GetLastError(),
	  //MAKELANGID(LANG_ENGLISH,SUBLANG_ENGLISH_US),wndStr,STRLEN,NULL);
	  
	  if (ProcessNmea (rx, (unsigned*)&got)) {
            int now;
            now=GetTickCount();
            if(!lastgps) lastgps=now;
            GpsIdle=(now-lastgps)/1000;
            lastgps=now;
	    PostMessage (mWnd, WM_USER + 1, 0, (int) /* intptr_t */ gpsNew);
	  }
	} // if nBytes > 0
      } // while ReadFile(...)
      if (!guiDone) {
	logprintf("Connection to CommPort failed.\n");
      }
    } // if port != INVALID_FILE_HANDLE
  #endif
  } // while !guiDone
  guiDone = FALSE;
  GpsIdle=999; /* Let user know GPS is off */
  //if (log) fclose (log);
  CloseHandle (port);
  return 0;
}


void XmlOut (FILE *newWayFile, const char *k, const char *v)
{
  if (*v != '\0') {
    fprintf (newWayFile, "  <tag k='%s' v='", k);
    for (; *v != '\0'; v++) {
      if (*v == '\'') fprintf (newWayFile, "&apos;");
      else if (*v == '&') fprintf (newWayFile, "&amp;");
      else fputc (*v, newWayFile);
    }
    fprintf (newWayFile, "' />\n");
  }
}


extern "C" {
int WINAPI WinMain(
    HINSTANCE  hInstance,	  // handle of current instance
    HINSTANCE  hPrevInstance,	  // handle of previous instance
    #ifdef _WIN32_WCE
    LPWSTR  lpszCmdLine,	          // pointer to command line
    #else
    LPSTR lpszCmdLine,
    #endif
    int  nCmdShow)	          // show state of window
{
  if(hPrevInstance) return(FALSE);
  hInst = hInstance;
  gDisplayOff = FALSE;
  wchar_t argv0[80];
  GetModuleFileNameW (NULL, argv0, sizeof (argv0) / sizeof (argv0[0]));
  UTF16 *sStart = (UTF16*) argv0, *rchr = (UTF16*) wcsrchr (argv0, L'\\');
  wcscpy (rchr ? (wchar_t *) rchr + 1 : argv0, L"");
  unsigned char *tStart = (unsigned char *) docPrefix;
  ConvertUTF16toUTF8 ((const UTF16 **) &sStart, sStart + wcslen (argv0),
    &tStart, tStart + sizeof (docPrefix), lenientConversion);
  *tStart = '\0';
  #ifndef _WIN32_WCE
  if (strncmp (lpszCmdLine, "rebuild", 7) == 0) {
    int bbox[4] = { INT_MIN, INT_MIN, 0x7fffffff, 0x7fffffff };
    _setmaxstdio (1024); // Try to prevent 'Too many open files'
    RebuildPak("gosmore.pak", "elemstyles.xml", "icons.csv", "", bbox);
  }
  else if (lpszCmdLine[0] != '\0') {
    chdir (docPrefix);
    char cmdl[200];
    sprintf (cmdl, "start cmd /C \"7z -so e %s | gosmore rebuild\"", lpszCmdLine);
    system (cmdl);
    return 0;
  }
  #endif
  #if 0
  GetModuleFileName (NULL, docPrefix, sizeof (docPrefix));
  if (strrchr (docPrefix, '\\')) *strrchr (docPrefix, '\\') = '\0';
  #endif

  char optFileName[sizeof(docPrefix) + 13];
  sprintf (optFileName, "%s\\gosmore.opt", docPrefix);
  FILE *optFile = fopen (optFileName, "r");  
  if (!optFile) {
    strcpy (docPrefix, "\\My Documents\\");
    optFile = fopen ("\\My Documents\\gosmore.opt", "rb");
  }

  //store log file name
  sprintf (logFileName, "%s\\gosmore.log.txt", docPrefix);

  #ifdef _WIN32_WCE
  wcscat (argv0, L"gosmore.pak");
  SerializeOptions (optFile, TRUE, argv0);
  #else
  SerializeOptions (optFile, TRUE, "gosmore.pak");
  Keyboard = 1;
  #endif
  int newWayFileNr = 0;
  LOG if (optFile) fread (&newWayFileNr, sizeof (newWayFileNr), 1, optFile);
  if (Exit) {
    MessageBox (NULL, TEXT ("Pak file not found"), TEXT (""),
      MB_APPLMODAL|MB_OK);
    return 1;
  }
  GtkWidget dumdraw;
  draw = &dumdraw;

  LOG if(!InitApplication ()) return(FALSE);
  LOG if (!InitInstance (nCmdShow)) return(FALSE);

  newWays[0].cnt = 0;

  #ifdef _WIN32_WCE
  LOG InitCeGlue();
  if (SHFullScreenPtr) {
    if (FullScreen) {
      (*SHFullScreenPtr)(mWnd, SHFS_HIDETASKBAR |
			 SHFS_HIDESTARTICON | SHFS_HIDESIPBUTTON);
      MoveWindow (mWnd, 0, 0, GetSystemMetrics(SM_CXSCREEN),
		  GetSystemMetrics(SM_CYSCREEN), FALSE);
    } else {
      (*SHFullScreenPtr)(mWnd, SHFS_HIDESIPBUTTON);
    }  
  }
  #endif

  DWORD threadId;
  if (CommPort == 0) {}
  else /* if((port=CreateFile (portname, GENERIC_READ | GENERIC_WRITE, 0,
          NULL, OPEN_EXISTING, 0, 0)) != INVALID_HANDLE_VALUE) */ {
    LOG CreateThread (NULL, 0, NmeaReader, NULL, 0, &threadId);
    }
  /*   else MessageBox (NULL, TEXT ("No Port"), TEXT (""), MB_APPLMODAL|MB_OK); */

  MSG    msg;
  LOG while (GetMessage (&msg, NULL, 0, 0)) {
    //logprintf ("%d %d %d %d\n", msg.hwnd == mWnd, msg.message, msg.lParam, msg.wParam);
    int oldCsum = clat + clon, found = msg.message == WM_KEYDOWN;
    if (Keyboard && msg.hwnd == hwndEdit && msg.message == WM_LBUTTONDOWN) {
      option = option == searchMode ? mapMode : searchMode;
      SipShowIM (option == searchMode ? SIPF_ON : SIPF_OFF);
      InvalidateRect (mWnd, NULL, FALSE);
    } // I couldn't find an EN_ event that traps a click on the searchbar.
    if (msg.message == WM_KEYDOWN) {
      if ((msg.wParam == '0' && option != searchMode) || msg.wParam == MenuKey) {
        HitButton (0);
        if (Keyboard && optionMode != searchMode) SipShowIM (SIPF_OFF);
      }
      else if (msg.wParam == '8' && option != searchMode) HitButton (1);
      else if (msg.wParam == '9' && option != searchMode) HitButton (2);

      else if (option == ZoomInKeyNum) ZoomInKey = msg.wParam;
      else if (option == ZoomOutKeyNum) ZoomOutKey = msg.wParam;
      else if (option == MenuKeyNum) MenuKey = msg.wParam;
      
      else if (msg.wParam == (DWORD) ZoomInKey) zoom = zoom * 3 / 4;
      else if (msg.wParam == (DWORD) ZoomOutKey) zoom = zoom * 4 / 3;

      else if (VK_DOWN == msg.wParam) clat -= zoom / 2;
      else if (VK_UP == msg.wParam) clat += zoom / 2;
      else if (VK_LEFT == msg.wParam) clon -= zoom / 2;
      else if (VK_RIGHT == msg.wParam) clon += zoom / 2;
      else found = FALSE;
      
      if (found) InvalidateRect (mWnd, NULL, FALSE);
      if (oldCsum != clat + clon) FollowGPSr = FALSE;
    }
    if (!found) {
      TranslateMessage (&msg);
      DispatchMessage (&msg);
    }
  }
  guiDone = TRUE;

  LOG while (port != INVALID_HANDLE_VALUE && guiDone) Sleep (1000);

  optFile = fopen (optFileName, "r+b");
  if (!optFile) optFile = fopen ("\\My Documents\\gosmore.opt", "wb");
  LOG SerializeOptions (optFile, FALSE, NULL);
  if (optFile) {
    fwrite (&newWayFileNr, sizeof (newWayFileNr), 1, optFile);
    fclose (optFile);
  }
  LOG gpsNewStruct *first = FlushGpx ();
  if (newWayCnt > 0) {
    char VehicleName[80];
    #define M(v) Vehicle == v ## R ? #v :
    sprintf(VehicleName, "%s", RESTRICTIONS NULL);
    #undef M

    char bname[80], fname[80];
    LOG getBaseFilename(bname, first);
    sprintf (fname, "%s.osm", bname);

    FILE *newWayFile = fopen (fname, "w");
    if (newWayFile) {
      LOG fprintf (newWayFile, "<?xml version='1.0' encoding='UTF-8'?>\n"
                           "<osm version='0.6' generator='gosmore'>\n");
      for (int j, id = -1, i = 0; i < newWayCnt; i++) {
        for (j = 0; j < newWays[i].cnt; j++) {
          fprintf (newWayFile, "<node id='%d' visible='true' lat='%.5lf' "
            "lon='%.5lf' %s>\n", id - j, LatInverse (newWays[i].coord[j][1]),
            LonInverse (newWays[i].coord[j][0]),
            newWays[i].cnt <= 1 ? "" : "/");
        }
        if (newWays[i].cnt > 1) {
          fprintf (newWayFile, "<way id='%d' action='modify' "
            "visible='true'>\n", id - newWays[i].cnt);
          for (j = 0; j < newWays[i].cnt; j++) {
            fprintf (newWayFile, "  <nd ref='%d'/>\n", id--);
          }
        }
        id--;
	XmlOut (newWayFile, "todo", "FIXME - Added by gosmore");
        if (newWays[i].oneway) XmlOut (newWayFile, "oneway", "yes");
        if (newWays[i].bridge) XmlOut (newWayFile, "bridge", "yes");
        if (newWays[i].klas >= 0) fprintf (newWayFile, "%s",
          klasTable[newWays[i].klas].tags);
        XmlOut (newWayFile, "name", newWays[i].name);
        XmlOut (newWayFile, "note", newWays[i].note);
        fprintf (newWayFile, "</%s>\n", newWays[i].cnt <= 1 ? "node" : "way");
      }
      fprintf (newWayFile, "</osm>\n");
      fclose (newWayFile);
    }
  }

  LOG if (logFP(false)) fclose(logFP(false));

  return 0;
}
} // extern "C"
#endif
