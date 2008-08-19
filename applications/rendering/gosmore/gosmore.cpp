/* This software is placed by in the public domain by its authors. */
/* Written by Nic Roets with contribution(s) from Dave Hansen and
   Ted Mielczarek. */

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
#include <windowsx.h>
//#include <winuserm.h> // For playing a sound ??
#include <sipapi.h>
#include <aygshell.h>
#include "ceglue.h"
#include "ConvertUTF.h"
#include "resource.h"
typedef int intptr_t;

// Unfortunately eMbedded Visual C++ TEXT() function does not use UTF8
// So we have to repeat the OPTIONS table
#define OPTIONS \
  o (FollowGPSr,      0, 2) \
  o (AddWayOrNode,    0, 2) \
  o (Search,          0, 1) \
  o (StartRoute,      0, 1) \
  o (EndRoute,        0, 1) \
  o (OrientNorthwards,0, 2) \
  o (FastestRoute,    0, 2) \
  o (Vehicle,         motorcarR, onewayR) \
  o (English,         0, \
            sizeof (optionNameTable) / sizeof (optionNameTable[0])) \
  o (ButtonSize,      1, 5) \
  o (IconSet,         0, 4) \
  o (DetailLevel,     0, 5) \
  o (CommPort,        0, 13) \
  o (BaudRate,        0, 6) \
  o (QuickOptions,    0, 2) \
  o (Exit,            0, 2) \
  o (ZoomInKey,       0, 3) \
  o (ZoomOutKey,      0, 3) \
  o (MenuKey,         0, 3) \
  o (HideZoomButtons, 0, 2) \
  o (ShowCoordinates, 0, 3) \
  o (ShowTrace,       0, 2) \
  o (ModelessDialog,  0, 2)
#else
#include <unistd.h>
#include <sys/stat.h>
#include <string>
using namespace std;
#define wchar_t char
#define wsprintf sprintf
#define OPTIONS \
  o (FollowGPSr,      0, 2) \
  o (Search,          0, 1) \
  o (StartRoute,      0, 1) \
  o (EndRoute,        0, 1) \
  o (OrientNorthwards,0, 2) \
  o (FastestRoute,    0, 2) \
  o (Vehicle,         motorcarR, onewayR) \
  o (English,         0, \
                 sizeof (optionNameTable) / sizeof (optionNameTable[0])) \
  o (ButtonSize,      1, 5) \
  o (IconSet,         0, 4) \
  o (DetailLevel,     0, 5) \
  o (ShowActiveRouteNodes, 0, 2)

#define HideZoomButtons 0
#define MenuKey 0
#endif
char docPrefix[80] = "";
#ifndef TRUE
#define TRUE 1
#define FALSE 0
#endif

#if !defined (HEADLESS) && !defined (_WIN32_WCE)
#include <gtk/gtk.h>
#include "icons.xpm"
#endif

#if __APPLE__ // Thanks to Ted Mielczarek
#define fopen64(x,y) fopen(x,y)
#endif

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
HWND hwndList;
UTF16 appendTmp[50];
char searchStr[50];

#define gtk_clist_append(x,str) { \
    const unsigned char *sStart = (const unsigned char*) *str; \
    UTF16 *tStart = appendTmp; \
    if (ConvertUTF8toUTF16 (&sStart,  sStart + strlen (*str) + 1, \
        &tStart, appendTmp + sizeof (appendTmp) / sizeof (appendTmp[0]), \
        lenientConversion) == conversionOK) { \
      SendMessage (hwndList, LB_ADDSTRING, 0, (LPARAM) appendTmp); \
    } \
  }
#define gtk_entry_get_text(x) searchStr
#define gtk_clist_freeze(x)
#define gtk_clist_clear(x)
#define gtk_clist_thaw(x)
#define gtk_toggle_button_set_active(x,y) // followGPRr
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

HINSTANCE hInst;
HWND   mWnd, dlgWnd = NULL;

BOOL CALLBACK DlgSearchProc (
	HWND hwnd, 
	UINT Msg, 
	WPARAM wParam, 
	LPARAM lParam);
#else
const char *FindResource (const char *fname)
{
  static string s;
  struct stat dummy;
  if (stat (fname, &dummy) == 0) return fname;
  s = (string) getenv ("HOME") + "/.gosmore/" + fname;
  if (stat (s.c_str (), &dummy) != 0) s = (string) RES_DIR + fname;
  return s.c_str ();
}
#endif

#define TILEBITS (18)
#define TILESIZE (1<<TILEBITS)
#ifndef INT_MIN
#define INT_MIN 0x80000000 // -2147483648
#endif

#define RESTRICTIONS M (access) M (motorcar) M (bicycle) M (foot) M (goods) \
  M (hgv) M (horse) M (motorcycle) M (psv) M (motorboat) M (boat) \
  M (oneway) M (roundabout)

#define M(field) field ## R,
enum { STYLE_BITS = 8, RESTRICTIONS l1,l2,l3 };
#undef M

// Below is a list of the tags that the user may add. It should also include
// any tags that gosmore.cpp may test for directly, e.g.
// StyleNr(w) == highway_traffic_signals .
// See http://etricceline.de/osm/Europe/En/tags.htm for the most popular tags
// The fields are k, v, short name and the additional tags.
#define STYLES \
 s (highway, residential,     "residential"     , "") \
 s (highway, unclassified,    "unclassified"    , "") \
 s (highway, tertiary,        "tertiary"        , "") \
 s (highway, secondary,       "secondary"       , "") \
 s (highway, primary,         "primary"         , "") \
 s (highway, trunk,           "trunk"           , "") \
 s (highway, footway,         "footway"         , "") \
 s (highway, service,         "service"         , "") \
 s (highway, track,           "track"           , "") \
 s (highway, cycleway,        "cycleway"        , "") \
 s (highway, pedestrian,      "pedestrian"      , "") \
 s (highway, steps,           "steps"           , "") \
 s (highway, bridleway,       "bridleway"       , "") \
 s (railway, rail,            "railway"         , "") \
 s (railway, station,         "railway station" , "") \
 s (highway, mini_roundabout, "mini roundabout" , "") \
 s (highway, traffic_signals, "traffic signals" , "") \
 s (highway, bus_stop,        "bus stop"        , "") \
 s (amenity, parking,         "parking"         , "") \
 s (amenity, fuel,            "fuel"            , "") \
 s (amenity, school,          "school"          , "") \
 s (place,   village,         "village"         , "") \
 s (shop,    supermarket,     "supermarket"     , "") \
 s (religion, christian,      "church"          , \
               "  <tag k='amenity' v='place_of_worship' />\n") \
 s (religion, jewish,         "synagogue"       , \
               "  <tag k='amenity' v='place_of_worship' />\n") \
 s (religion, muslim,         "mosque"          , \
               "  <tag k='amenity' v='place_of_worship' />\n") \
 s (amenity, pub,             "pub"             , "") \
 s (amenity, restaurant,      "restaurant"      , "") \
 s (power,   tower,           "power tower"     , "") \
 s (waterway, stream,         "stream"          , "") \
 s (amenity, grave_yard,      "grave yard"      , "") \
 s (amenity, crematorium,     "crematorium"     , "") \
 s (amenity, shelter,         "shelter"         , "") \
 s (tourism, picnic_site,     "picnic site"     , "") \
 s (leisure, common,          "common area"     , "") \
 s (amenity, park_bench,      "park bench"      , "") \
 s (tourism, viewpoint,       "viewpoint"       , "") \
 s (tourism, artwork,         "artwork"         , "") \
 s (tourism, museum,          "museum"          , "") \
 s (tourism, theme_park,      "theme park"      , "") \
 s (tourism, zoo,             "zoo"             , "") \
 s (leisure, playground,      "playground"      , "") \
 s (leisure, park,            "park"            , "") \
 s (leisure, nature_reserve,  "nature reserve"  , "") \
 s (leisure, miniature_golf,  "miniature golf"  , "") \
 s (leisure, golf_course,     "golf course"     , "") \
 s (leisure, sports_centre,   "sports centre"   , "") \
 s (leisure, stadium,         "stadium"         , "") \
 s (leisure, pitch,           "pitch"           , "") \
 s (leisure, track,           "track"           , "") \
 s (sport,   athletics,       "athletics"       , "") \
 s (sport,   10pin,           "10 pin"          , "") \
 s (sport,   boules,          "boules"          , "") \
 s (sport,   bowls,           "bowls"           , "") \
 s (sport,   baseball,        "baseball"        , "") \
 s (sport,   basketball,      "basketball"      , "") \
 s (sport,   cricket,         "cricket"         , "") \
 s (sport,   cricket_nets,    "cricket_nets"    , "") \
 s (sport,   croquet,         "croquet"         , "") \
 s (sport,   dog_racing,      "dog racing"      , "") \
 s (sport,   equestrian,      "equestrian"      , "") \
 s (sport,   football,        "football"        , "") \
 s (sport,   soccer,          "soccer"          , "") \
 s (sport,   climbing,        "climbing"        , "") \
 s (sport,   gymnastics,      "gymnastics"      , "") \
 s (sport,   hockey,          "hockey"          , "") \
 s (sport,   horse_racing,    "horse racing"    , "") \
 s (sport,   motor,           "motor sport"     , "") \
 s (sport,   pelota,          "pelota"          , "") \
 s (sport,   rugby,           "rugby"           , "") \
 s (sport,   australian_football, "australian football" , "") \
 s (sport,   skating,         "skating"         , "") \
 s (sport,   skateboard,      "skateboard"      , "") \
 s (sport,   handball,        "handball"        , "") \
 s (sport,   table_tennis,    "table tennis"    , "") \
 s (sport,   tennis,          "tennis"          , "") \
 s (sport,   racquet,         "racquet"         , "") \
 s (sport,   badminton,       "badminton"       , "") \
 s (sport,   paintball,       "paintball"       , "") \
 s (sport,   shooting,        "shooting"        , "") \
 s (sport,   volleyball,      "volleyball"      , "") \
 s (sport,   beachvolleyball, "beach volleyball" , "") \
 s (sport,   archery,         "archery"         , "") \
 s (sport,   skiing,          "skiing"          , "") \
 s (sport,   rowing,          "rowing"          , "") \
 s (sport,   sailing,         "sailing"         , "") \
 s (sport,   diving,          "diving"          , "") \
 s (sport,   swimming,        "swimming"        , "") \
 s (leisure, swimming_pool,   "swimming pool"   , "") \
 s (leisure, water_park,      "water park"      , "") \
 s (leisure, marina,          "marina"          , "") \
 s (leisure, slipway,         "slipway"         , "") \
 s (leisure, fishing,         "fishing"         , "") \
 s (shop,    bakery,          "bakery"          , "") \
 s (shop,    butcher,         "butcher"         , "") \
 s (shop,    florist,         "florist"         , "") \
 s (shop,    groceries,       "groceries"       , "") \
 s (shop,    clothes,         "clothing shop"   , "") \
 s (shop,    shoes,           "shoe shop"       , "") \
 s (shop,    jewelry,         "jewelry store"   , "") \
 s (shop,    books,           "bookshop"        , "") \
 s (shop,    newsagent,       "newsagent"       , "") \
 s (shop,    furniture,       "furniture store" , "") \
 s (shop,    hifi,            "Hi-Fi store"     , "") \
 s (shop,    electronics,     "electronics store" , "") \
 s (shop,    computer,        "computer shop"   , "") \
 s (shop,    video,           "video rental"    , "") \
 s (shop,    toys,            "toy shop"        , "") \
 s (shop,    motorcycle,      "motorcycle"      , "") \
 s (shop,    car_repair,      "car repair"      , "") \
 s (shop,    doityourself,    "doityourself"    , "") \
 s (shop,    garden_centre,   "garden centre"   , "") \
 s (shop,    outdoor,         "outdoor"         , "") \
 s (shop,    bicycle,         "bicycle shop"    , "") \
 s (shop,    dry_cleaning,    "dry cleaning"    , "") \
 s (shop,    laundry,         "laundry"         , "") \
 s (shop,    hairdresser,     "hairdresser"     , "") \
 s (shop,    travel_agency,   "travel_agency"   , "") \
 s (shop,    convenience,     "convenience"     , "") \
 s (shop,    mall,            "mall"            , "") \
 s (shop,    department_store, "department store" , "") \
 s (amenity, biergarten,      "biergarten"      , "") \
 s (amenity, nightclub,       "nightclub"       , "") \
 s (amenity, bar,             "bar"             , "") \
 s (amenity, cafe,            "cafe"            , "") \
 s (amenity, fast_food,       "fast food"       , "") \
 s (amenity, ice_cream,       "icecream"        , "") \
 s (amenity, bicycle_rental,  "bicycle rental"  , "") \
 s (amenity, car_rental,      "car rental"      , "") \
 s (amenity, car_sharing,     "car sharing"     , "") \
 s (amenity, car_wash,        "car wash"        , "") \
 s (amenity, taxi,            "taxi"            , "") \
 s (amenity, telephone,       "telephone"       , "") \
 s (amenity, post_office,     "post office"     , "") \
 s (amenity, post_box,        "post box"        , "") \
 s (tourism, information,     "tourist info"    , "") \
 s (amenity, toilets,         "toilets"         , "") \
 s (amenity, recycling,       "recycling"       , "") \
 s (amenity, fire_station,    "fire station"    , "") \
 s (amenity, police,          "police"          , "") \
 s (amenity, courthouse,      "courthouse"      , "") \
 s (amenity, prison,          "prison"          , "") \
 s (amenity, public_building, "public building" , "") \
 s (amenity, townhall,        "townhall"        , "") \
 s (amenity, cinema,          "cinema"          , "") \
 s (amenity, arts_centre,     "arts centre"     , "") \
 s (amenity, theatre,         "theatre"         , "") \
 s (tourism, hotel,           "hotel"           , "") \
 s (tourism, motel,           "motel"           , "") \
 s (tourism, guest_house,     "guest house"     , "") \
 s (tourism, hostel,          "hostel"          , "") \
 s (tourism, chalet,          "chalet"          , "") \
 s (tourism, camp_site,       "camp site"       , "") \
 s (tourism, caravan_site,    "caravan site"    , "") \
 s (amenity, pharmacy,        "pharmacy"        , "") \
 s (amenity, dentist,         "dentist"         , "") \
 s (amenity, hospital,        "hospital"        , "") \
 s (amenity, bank,            "bank"            , "") \
 s (amenity, bureau_de_change, "bureau de change" , "") \
 s (amenity, atm,             "atm"            , "") \
 s (amenity, drinking_water,  "drinking water"  , "") \
 s (amenity, fountain,        "fountain"        , "") \
 s (natural, spring,          "spring"          , "") \
 s (amenity, university,      "university"      , "") \
 s (amenity, college,         "college"         , "") \
 s (amenity, kindergarten,    "kindergarten"    , "") \
 s (highway, living_street,   "living street"   , "") \
 s (highway, motorway,        "motorway"        , "") \
 s (highway, motorway_link,   "mway link"       , "") \
 s (highway, trunk_link,      "trunk link"      , "") \
 s (highway, primary_link,    "primary_link"    , "") \
 s (building, yes,            "building"        , "") \
 s (landuse, forest,          "forest"          , "") \
 s (landuse, residential,     "residential area", "") \
 s (landuse, industrial,      "industrial area" , "") \
 s (landuse, retail,          "retail area"     , "") \
 s (landuse, commercial,      "commercial area" , "") \
 s (landuse, construction,    "construction area" , "") \
 s (landuse, reservoir,       "reservoir"       , "") \
 s (natural, water,           "lake / dam"      , "") \
 s (landuse, basin,           "basin"           , "") \
 s (landuse, landfill,        "landfill"        , "") \
 s (landuse, quarry,          "quarry"          , "") \
 s (landuse, cemetery,        "cemetery"        , "") \
 s (landuse, allotments,      "allotments"      , "") \
 s (landuse, farm,            "farmland"        , "") \
 s (landuse, farmyard,        "farmyard"        , "") \
 s (landuse, military,        "military area"   , "") \
 s (religion, bahai,          "bahai"           , \
               "  <tag k='amenity' v='place_of_worship' />\n") \
 s (religion, buddhist,       "buddhist"        , \
               "  <tag k='amenity' v='place_of_worship' />\n") \
 s (religion, hindu,          "hindu"           , \
               "  <tag k='amenity' v='place_of_worship' />\n") \
 s (religion, jain,           "jainism"         , \
               "  <tag k='amenity' v='place_of_worship' />\n") \
 s (religion, sikh,           "sikhism"         , \
               "  <tag k='amenity' v='place_of_worship' />\n") \
 s (religion, shinto,         "shinto"          , \
               "  <tag k='amenity' v='place_of_worship' />\n") \
 s (religion, taoist,         "taoism"          , \
               "  <tag k='amenity' v='place_of_worship' />\n") \
 /* relations must be last and restriction_no_right_turn must be first */ \
 s (restriction, no_right_turn, ""              , "") \
 s (restriction, no_left_turn, ""               , "") \
 s (restriction, no_u_turn, ""                  , "") \
 s (restriction, no_straight_on, ""             , "") \
 s (restriction, only_right_turn, ""            , "") \
 s (restriction, only_left_turn, ""             , "") \
 s (restriction, only_straight_on, ""           , "") \
 /* restriction_only_straight_on must be the last restriction */

#define XXXFSDFSF \
 s (sport,   orienteering,    "orienteering"    , "") \
 s (sport,   gym,             "gym"             , "") \
 /* sport=golf isn't a golf course, so what is it ? */ \

#define s(k,v,shortname,extraTags) k ## _ ## v,
enum { STYLES firstElemStyle }; // highway_residential, ...
#undef s

struct klasTableStruct {
  const wchar_t *desc;
  const char *tags;
} klasTable[] = {
#define s(k,v,shortname,extraTags) \
  { TEXT (shortname), "  <tag k='" #k "' v='" #v "' />\n" extraTags },
STYLES
#undef s
};

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

inline int Latitude (double lat)
{ /* Mercator projection onto a square means we have to clip
     everything beyond N85.05 and S85.05 */
  return lat > 85.051128779 ? 2147483647 : lat < -85.051128779 ? -2147483647 :
    lrint (log (tan (M_PI / 4 + lat * M_PI / 360)) / M_PI * 2147483648.0);
}

inline int Longitude (double lon)
{
  return lrint (lon / 180 * 2147483648.0);
}

inline double LatInverse (int lat)
{
  return (atan (exp (lat / 2147483648.0 * M_PI)) - M_PI / 4) / M_PI * 360;
}

inline double LonInverse (int lon)
{
  return lon / 2147483648.0 * 180;
}

/*---------- Global variables -----------*/
int *hashTable, bucketsMin1, pakHead = 0xEB3A942;
char *data;
ndType *ndBase;
styleStruct *style;

inline int StyleNr (wayType *w) { return w->bits & ((2 << STYLE_BITS) - 1); }

inline styleStruct *Style (wayType *w) { return &style[StyleNr (w)]; }

unsigned inline ZEnc (int lon, int lat)
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

#define notImplemented \
  o (ShowCompass,     ) \
  o (ShowPrecision,   ) \
  o (ShowSpeed,       ) \
  o (ShowHeading,     ) \
  o (ShowElevation,   ) \
  o (ShowDate,        ) \
  o (ShowTime,        ) \

#define o(en,min,max) en ## Num,
enum { OPTIONS numberOfOptions, chooseObjectToAdd };
#undef o

//  TEXT (#en), TEXT (de), TEXT (es), TEXT (fr), TEXT (it), TEXT (nl) },
const char *optionNameTable[][numberOfOptions] = {
#define o(en,min,max) #en,
  { OPTIONS }, // English is same as variable names
#undef o

#ifdef _WIN32_WCE
#include "translations.c"
#endif
};

#define o(en,min,max) int en = min;
OPTIONS
#undef o

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
   are "states", namely the ability to reach nd directly from nd->other[dir]
*/
struct routeNodeType {
  ndType *nd;
  routeNodeType *shortest;
  int best, heapIdx, dir, remain; // Dir is 0 or 1
} *route = NULL, *shortest = NULL, **routeHeap;
int dhashSize, routeHeapSize, tlat, tlon, flat, flon, rlat, rlon;

#ifdef ROUTE_CALIBRATE
int routeAddCnt;
#define ROUTE_SET_ADDND_COUNT(x) routeAddCnt = (x)
#define ROUTE_SHOW_STATS printf ("%d / %d\n", routeAddCnt, dhashSize); \
  fprintf (stderr, "flat=%lf&flon=%lf&tlat=%lf&tlon=%lf&fast=%d&v=motorcar\n", \
    LatInverse (flat), LonInverse (flon), LatInverse (tlat), \
    LonInverse (tlon), FastestRoute)
// This ratio must be around 0.5. Close to 0 or 1 is bad
#else
#define ROUTE_SET_ADDND_COUNT(x)
#define ROUTE_SHOW_STATS
#endif

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
    if (i++ > 10) {
      //fprintf (stderr, "Double hash bailout : Table full, hash function "
      //  "bad or no route exists\n");
      return NULL;
    }
    n = route + hash % dhashSize;
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

inline int IsOneway (wayType *w)
{
  return Vehicle != footR && Vehicle != bicycleR && (w->bits & (1<<onewayR));
}

void Route (int recalculate, int plon, int plat)
{ /* Recalculate is faster but only valid if 'to', 'Vehicle' and
     'FastestRoute' did not change */
/* We start by finding the segment that is closest to 'from' and 'to' */
  static ndType *endNd[2] = { NULL, NULL}, from;
  static int toEndNd[2][2];
  
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
      if (!(((wayType*)(data + itr.nd[0]->wayPtr))->bits & (1<<Vehicle))) {
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
      
      wayType *w = (wayType *)(data + itr.nd[0]->wayPtr);
      if (i) { // For 'from' we take motion into account
        __int64 motion = segLen ? 3 * (dlon * plon + dlat * plat) / segLen
          : 0;
        // What is the most appropriate multiplier for motion ?
        if (motion > 0 && IsOneway (w)) d += Sqr (motion);
        else d -= Sqr (motion);
        // Is it better to say :
        // d = lrint (sqrt ((double) d));
        // if (motion < 0 || IsOneway (w)) d += motion;
        // else d -= motion; 
      }
      
      if (d < bestd) {
        bestd = d;
        double invSpeed = !FastestRoute ? 1.0 : Style (w)->invSpeed[Vehicle];
        //printf ("%d %lf\n", i, invSpeed);
        toEndNd[i][0] =
          lrint (sqrt ((double)(Sqr (lon0) + Sqr (lat0))) * invSpeed);
        toEndNd[i][1] =
          lrint (sqrt ((double)(Sqr (lon1) + Sqr (lat1))) * invSpeed);
//        if (dlon * lon1 <= -dlat * lat1) toEndNd[i][1] += toEndNd[i][0] * 9;
//        if (dlon * lon0 >= -dlat * lat0) toEndNd[i][0] += toEndNd[i][1] * 9;

        if (IsOneway (w)) toEndNd[i][1 - i] = 200000000;
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
    /* Now work through the segments connected to root. */
    do {
//          if (StyleNr ((wayType *)(data + nd->wayPtr)) >= restriction_no_right_turn) printf ("%d\n", nd - firstNd);
      for (int dir = 0; dir < 2; dir++) {
        if (nd == root->nd && dir == root->dir) continue;
        /* Don't consider an immediate U-turn to reach root->hs->other.
           This doesn't exclude 179.99 degree turns though. */
        
        if (nd->other[dir] < 0) continue;
        if (Vehicle != footR && Vehicle != bicycleR) {
          for (restrictItr = firstNd; restrictItr->other[0] < 0 &&
                          restrictItr->other[1] < 0; restrictItr++) {
            wayType *w  = (wayType *)(data + restrictItr->wayPtr);
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
        wayType *w = (wayType *)(data + nd->wayPtr);
        if ((w->bits & (1 << Vehicle)) && (dir || !IsOneway (w))) {
          int d = lrint (sqrt ((double)
            (Sqr ((__int64)(nd->lon - other->lon)) +
             Sqr ((__int64)(nd->lat - other->lat)))) *
                        (FastestRoute ? Style (w)->invSpeed[Vehicle] : 1.0));                  
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
    if (StyleNr ((wayType*)(nd->wayPtr + data)) == highway_traffic_signals) {
      ret = 't';
    }
    if (StyleNr ((wayType*)(nd->wayPtr + data)) == highway_mini_roundabout) {
      ret = 'm';
    }   
  } while (++nd < ndBase + hashTable[bucketsMin1 + 1] &&
           nd->lon == nd[-1].lon && nd->lat == nd[-1].lat);
  return segCnt > 2 ? toupper (ret) : ret;
}

#ifndef HEADLESS
#define STATUS_BAR    0

GtkWidget *draw, *location, *followGPSr, *orientNorthwards;
GtkComboBox *iconSet, *carBtn, *fastestBtn, *detailBtn;
int clon, clat, zoom, option = EnglishNum, gpsSockTag, setLocBusy = FALSE;
/* zoom is the amount that fits into the window (regardless of window size) */
double cosAzimuth = 1.0, sinAzimuth = 0.0;

inline void SetLocation (int nlon, int nlat)
{
  clon = nlon;
  clat = nlat;
  #ifndef _WIN32_WCE
  char lstr[50];
  int zl = 0;
  while (zl < 32 && (zoom >> zl)) zl++;
  sprintf (lstr, "?lat=%.5lf&lon=%.5lf&zoom=%d", LatInverse (nlat),
    LonInverse (nlon), 33 - zl);
  setLocBusy = TRUE;
  gtk_entry_set_text (GTK_ENTRY (location), lstr);
  setLocBusy = FALSE;
  #endif
}

#ifndef _WIN32_WCE
int ChangeLocation (void)
{
  if (setLocBusy) return FALSE;
  char *lstr = (char *) gtk_entry_get_text (GTK_ENTRY (location));
  double lat, lon;
  while (*lstr != '?' && *lstr != '\0') lstr++;
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
  IconSet = gtk_combo_box_get_active (iconSet);
  Vehicle = gtk_combo_box_get_active (carBtn) + motorcarR;
  DetailLevel = 4 - gtk_combo_box_get_active (detailBtn);
  FollowGPSr = gtk_toggle_button_get_active (GTK_TOGGLE_BUTTON (followGPSr));
  OrientNorthwards =
    gtk_toggle_button_get_active (GTK_TOGGLE_BUTTON (orientNorthwards));
  if (OrientNorthwards) {
    cosAzimuth = 1.0;
    sinAzimuth = 0.0;
  }
  FastestRoute = !gtk_combo_box_get_active (fastestBtn);
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
  char fname[80];
  sprintf (fname, "%s%.2s%.2s%.2s-%.6s.gpx", docPrefix, first->fix.date + 4,
    first->fix.date + 2, first->fix.date, first->fix.tm);
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
    fprintf (gpx, "<trkpt lat=\"%12.9lf\" lon=\"%12.9lf\">\n",
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
        if (fabs (nLat) < 90 && fabs (nLon) < 180) {
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

void DoFollowThing (gpsNewStruct *gps)
{
  if (!/*gps->fix.mode >= MODE_2D &&*/ FollowGPSr) return;
  SetLocation (Longitude (gps->fix.longitude), Latitude (gps->fix.latitude));
/*    int plon = Longitude (gps->fix.longitude + gps->fix.speed * 3600.0 /
      40000000.0 / cos (gps->fix.latitude * (M_PI / 180.0)) *
      sin (gps->fix.track * (M_PI / 180.0)));
    int plat = Latitude (gps->fix.latitude + gps->fix.speed * 3600.0 /
      40000000.0 * cos (gps->fix.track * (M_PI / 180.0))); */
    // Predict the vector that will be traveled in the next 10seconds
//    printf ("%5.1lf m/s Heading %3.0lf\n", gps->fix.speed, gps->fix.track);
//    printf ("%lf %lf\n", gps->fix.latitude, gps->fix.longitude);
    
  __int64 dlon = clon - flon, dlat = clat - flat;
  flon = clon;
  flat = clat;
  if (route) Route (FALSE, dlon, dlat);

  static ndType *decide[3] = { NULL, NULL, NULL }, *oldDecide = NULL;
  static const wchar_t *command[3] = { NULL, NULL, NULL }, *oldCommand = NULL;
  decide[0] = NULL;
  command[0] = NULL;
  if (shortest) {
    routeNodeType *x = shortest->shortest;
    if (!x) command[0] = TEXT ("stop");
    if (x && Sqr (dlon) + Sqr (dlon) > 10000 /* faster than ~3 km/h */ &&
        dlon * (x->nd->lon - clon) + dlat * (x->nd->lat - clat) < 0) {
      command[0] = TEXT ("uturn");
      decide[0] = NULL;
    }
    else if (x) {
      int nextJunction = TRUE;
      double dist = sqrt (Sqr ((double) (x->nd->lat - flat)) +
                          Sqr ((double) (x->nd->lon - flon)));
      for (x = shortest; x->shortest &&
           dist < 40000 /* roughly 300m */; x = x->shortest) {
        int roundExit = 0;
        while (x->shortest && ((1 << roundaboutR) &
                       ((wayType*)(x->shortest->nd->wayPtr + data))->bits)) {
          if (isupper (JunctionType (x->shortest->nd))) roundExit++;
          x = x->shortest;
        }
        if (!x->shortest || roundExit) {
          decide[0] = x->nd;
          static const wchar_t *rtxt[] = { NULL, TEXT ("round1"),
            TEXT ("round2"),
            TEXT ("round3"), TEXT ("round4"), TEXT ("round5"),
            TEXT ("round6"), TEXT ("round7"), TEXT ("round8") };
          command[0] = rtxt[roundExit];
          break;
        }
        
        ndType *n0 = x->nd, *n1 = x->shortest->nd, *nd = n1;
        int n2lat =
          x->shortest->shortest ? x->shortest->shortest->nd->lat : tlat;
        int n2lon =
          x->shortest->shortest ? x->shortest->shortest->nd->lon : tlon;
        while (nd > ndBase && nd[-1].lon == nd->lon &&
          nd[-1].lat == nd->lat) nd--;
        int segCnt = 0; // Count number of segments at x->shortest
        do {
          // TODO : Only count segment traversable by 'Vehicle'
          // Except for the case where a cyclist crosses a motorway.
          if (nd->other[0] >= 0) segCnt++;
          if (nd->other[1] >= 0) segCnt++;
        } while (++nd < ndBase + hashTable[bucketsMin1 + 1] &&
                 nd->lon == nd[-1].lon && nd->lat == nd[-1].lat);
        if (segCnt > 2) {
          __int64 straight =
            (n2lat - n1->lat) * (__int64) (n1->lat - n0->lat) +
            (n2lon - n1->lon) * (__int64) (n1->lon - n0->lon), left =
            (n2lat - n1->lat) * (__int64) (n1->lon - n0->lon) -
            (n2lon - n1->lon) * (__int64) (n1->lat - n0->lat);
          decide[0] = n1;
          if (straight < left) {
            command[0] = nextJunction ? TEXT ("turnleft") : TEXT ("keepleft");
            break;
          }
          if (straight < -left) {
            command[0] = nextJunction
                             ? TEXT ("turnright"): TEXT ("keepright");
            break;
          }
          nextJunction = FALSE;
        }
        dist += sqrt (Sqr ((double) (n2lat - n1->lat)) +
                      Sqr ((double) (n2lon - n1->lon)));
      } // While looking ahead to the next turn.
      if (!x->shortest && dist < 6000) {
        command[0] = TEXT ("stop");
        decide[0] = NULL;
      }
    } // If not on final segment
  } // If the routing was successful

  if (command[0] && (oldCommand != command[0] || oldDecide != decide[0]) &&
      command[0] == command[1] && command[1] == command[2] &&
      decide[0] == decide[1] && decide[1] == decide[2]) {
    oldCommand = command[0];
    oldDecide = decide[0];
#ifdef _WIN32_WCE
    wchar_t argv0[80];
    GetModuleFileName (NULL, argv0, sizeof (argv0) / sizeof (argv0[0]));
    wcscpy (argv0 + wcslen (argv0) - 12, command[0]); // gosm_arm.exe to a.wav
    wcscpy (argv0 + wcslen (argv0), TEXT (".wav"));
    PlaySound (argv0, NULL, SND_FILENAME | SND_NODEFAULT);
#else
    printf ("%s\n", command[0]);
#endif
  }
  memmove (command + 1, command, sizeof (command) - sizeof (command[0]));
  memmove (decide + 1, decide, sizeof (decide) - sizeof (decide[0]));

  double dist = sqrt (Sqr ((double) dlon) + Sqr ((double) dlat));
  if (!OrientNorthwards && dist > 100.0) {
    cosAzimuth = dlat / dist;
    sinAzimuth = -dlon / dist;
  }                                            
  gtk_widget_queue_clear (draw);
} // If following the GPSr and it has a fix.

#ifndef _WIN32_WCE
#ifdef ROUTE_TEST
gint RouteTest (GtkWidget * /*widget*/, GdkEventButton *event, void *)
{
  static int ptime = 0;
  ptime = time (NULL);
  int w = draw->allocation.width;
  int perpixel = zoom / w;
  clon += lrint ((event->x - w / 2) * perpixel);
  clat -= lrint ((event->y - draw->allocation.height / 2) * perpixel);
/*    int plon = clon + lrint ((event->x - w / 2) * perpixel);
    int plat = clat -
      lrint ((event->y - draw->allocation.height / 2) * perpixel); */
  FollowGPSr = TRUE;
  gpsNewStruct gNew;
  gNew.fix.latitude = LatInverse (clat);
  gNew.fix.longitude = LonInverse (clon);
  DoFollowThing (&gNew);
}
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
  
  if (ProcessNmea (rx, &got)) DoFollowThing (gpsNew);
}
#endif // !ROUTE_TEST

gint Scroll (GtkWidget * /*widget*/, GdkEventScroll *event, void * /*w_cur*/)
{
  if (event->direction == GDK_SCROLL_UP) zoom = zoom / 4 * 3;
  if (event->direction == GDK_SCROLL_DOWN) zoom = zoom / 3 * 4;
  SetLocation (clon, clat);
  gtk_widget_queue_clear (draw);
  return FALSE;
}

#else // _WIN32_WCE
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

BOOL CALLBACK DlgChooseOProc (HWND hwnd, UINT Msg, WPARAM wParam,
  LPARAM lParam)
{
  if (Msg == WM_INITDIALOG) {
    HWND klist = GetDlgItem (hwnd, IDC_LISTO);
    for (int i = 0; i < numberOfOptions; i++) {
      const unsigned char *sStart = (const unsigned char*)
        optionNameTable[English][i];
      UTF16 wcTmp[30], *tStart = wcTmp;
      if (ConvertUTF8toUTF16 (&sStart,  sStart + strlen ((char*) sStart) + 1,
            &tStart, wcTmp + sizeof (wcTmp) / sizeof (wcTmp[0]),
            lenientConversion) == conversionOK) {
        SendMessage (klist, LB_ADDSTRING, 0, (LPARAM) wcTmp);
      }
    }
  }
  
  if (Msg == WM_COMMAND && (wParam == IDCANCEL || wParam == IDOK)) {
    EndDialog (hwnd, wParam == IDOK ? SendMessage (
      GetDlgItem (hwnd, IDC_LISTO), LB_GETCURSEL, 0, 0) : -1);
    return TRUE;
  }
  return FALSE;
}
#endif // _WIN32_WCE

int objectAddRow = -1;
#define ADD_HEIGHT 32
#define ADD_WIDTH 64
void HitButton (int b)
{
  int returnToMap = b > 0 && option <= FastestRouteNum;
  #ifdef _WIN32_WCE
  if (AddWayOrNode && b == 0) {
    AddWayOrNode = 0;
    option = numberOfOptions;
    if (newWays[newWayCnt].cnt) objectAddRow = 0;
    return;
  }
  if (QuickOptions && b == 0) {
    option = DialogBox (hInst, MAKEINTRESOURCE (IDD_CHOOSEO), NULL,
      (DLGPROC) DlgChooseOProc);
    if (option == -1) option = numberOfOptions;
    
    #define o(en,min,max) \
      if (option == en ## Num && min == 0 && max <= 2) b = 1;
    OPTIONS
    #undef o
    if (b == 0) return;
    returnToMap = TRUE;
    // If it's a binary option, fall through to toggle it
  }
  #endif
    if (b == 0) option = (option + 1) % (numberOfOptions + 1);
    else if (option == StartRouteNum) {
      flon = clon;
      flat = clat;
      free (route);
      route = NULL;
      shortest = NULL;
    }
    else if (option == EndRouteNum) {
      tlon = clon;
      tlat = clat;
      Route (TRUE, 0, 0);
    }
    #ifdef _WIN32_WCE
    else if (option == SearchNum) {
      SipShowIM (SIPF_ON);
      if (ModelessDialog) ShowWindow (dlgWnd, SW_SHOW);
      else DialogBox (hInst, MAKEINTRESOURCE(IDD_DLGSEARCH),
               NULL, (DLGPROC)DlgSearchProc);
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
    if (returnToMap) option = numberOfOptions;
}

int Click (GtkWidget * /*widget*/, GdkEventButton *event, void * /*para*/)
{
  int w = draw->allocation.width, h = draw->allocation.height;
  #ifdef ROUTE_TEST
  if (event->state) {
    return RouteTest (NULL /*widget*/, event, NULL /*para*/);
  }
  #endif
  if (ButtonSize <= 0) ButtonSize = 4;
  int b = (draw->allocation.height - lrint (event->y)) / (ButtonSize * 20);
  if (objectAddRow >= 0) {
    int perRow = (w - ButtonSize * 20) / ADD_WIDTH;
    if (event->x < w - ButtonSize * 20) {
      #ifdef _WIN32_WCE
      newWays[newWayCnt].klas = objectAddRow + event->x / ADD_WIDTH +
                                event->y / ADD_HEIGHT * perRow;
      SipShowIM (SIPF_ON);
      if (DialogBox (hInst, MAKEINTRESOURCE (IDD_SETTAGS), NULL,
          (DLGPROC) DlgSetTagsProc)) {} //DialogBox (hInst,
          //MAKEINTRESOURCE (IDD_SETTAGS2), NULL, (DLGPROC) DlgSetTags2Proc);
      newWays[newWayCnt].cnt = 0;
      #endif
      objectAddRow = -1;
    }
    else objectAddRow = event->y * (restriction_no_right_turn / perRow + 2) /
                            draw->allocation.height * perRow;
  }
  else if (event->x > w - ButtonSize * 20 && b <
      (!HideZoomButtons || option != numberOfOptions ? 3 : 
      MenuKey != 0 ? 0 : 1)) HitButton (b);
  else {
    int perpixel = zoom / w;
    int lon = clon + lrint (cosAzimuth * perpixel * (event->x - w / 2) -
                            sinAzimuth * perpixel * (h / 2 - event->y));
    int lat = clat + lrint (cosAzimuth * perpixel * (h / 2 - event->y) +
                            sinAzimuth * perpixel * (event->x - w / 2));
    if (event->button == 1) {
      SetLocation (lon, lat);

      #ifdef _WIN32_WCE
      if (AddWayOrNode && newWays[newWayCnt].cnt < NEWWAY_MAX_COORD) {
        newWays[newWayCnt].coord[newWays[newWayCnt].cnt][0] = clon;
        newWays[newWayCnt].coord[newWays[newWayCnt].cnt++][1] = clat;
      }
      #endif
      gtk_toggle_button_set_active (GTK_TOGGLE_BUTTON (followGPSr), FALSE);
      FollowGPSr = 0;
    }
    else if (event->button == 2) {
      flon = lon;
      flat = lat;
      free (route);
      route = NULL;
      shortest = NULL;
    }
    else {
      tlon = lon;
      tlat = lat;
      Route (TRUE, 0, 0);
    }
  }
  gtk_widget_queue_clear (draw);
  return FALSE;
}

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
/*  clip.width = GetSystemMetrics(SM_CXSCREEN);
  clip.height = GetSystemMetrics(SM_CYSCREEN); */
  HFONT sysFont = (HFONT) GetStockObject (SYSTEM_FONT);
  LOGFONT logFont;
  GetObject (sysFont, sizeof (logFont), &logFont);
  WCHAR wcTmp[70];

#define gtk_combo_box_get_active(x) 1
#define gdk_draw_drawable(win,dgc,sdc,x,y,dx,dy,w,h) \
  BitBlt (dgc, dx, dy, w, h, sdc, x, y, SRCCOPY)
#define gdk_draw_line(win,gc,sx,sy,dx,dy) \
  MoveToEx (gc, sx, sy, NULL); LineTo (gc, dx, dy)

  if (objectAddRow >= 0) {
    SelectObject (mygc, sysFont);
    SelectObject (mygc, GetStockObject (BLACK_PEN));
    for (int y = 0, i = objectAddRow; y < draw->allocation.height;
              y += ADD_HEIGHT) {
      //gdk_draw_line (draw->window, mygc, 0, y, draw->allocation.width, y);
      gdk_draw_line (draw->window, mygc,
        draw->allocation.width - ButtonSize * 20,
        draw->allocation.height * i / restriction_no_right_turn,
        draw->allocation.width,
        draw->allocation.height * i / restriction_no_right_turn);
      RECT klip;
      klip.bottom = y + ADD_HEIGHT;
      klip.top = y;
      for (int x = 0; x < draw->allocation.width - ButtonSize * 20 -
          ADD_WIDTH && i < restriction_no_right_turn; x += ADD_WIDTH, i++) {
        int *icon = style[i].x + 4 * IconSet;
        gdk_draw_drawable (draw->window, mygc, icons, icon[0], icon[1],
          x - icon[2] / 2 + ADD_WIDTH / 2, y, icon[2], icon[3]);
        klip.left = x + 8;
        klip.right = x + ADD_WIDTH - 8;
        ExtTextOut (mygc, x + 8, y + ADD_HEIGHT - 16, ETO_CLIPPED,
          &klip, klasTable[i].desc, wcslen (klasTable[i].desc), NULL);
      }
    }
    return FALSE;
  } // if displaying the klas / style / rule selection screen
#else
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
          : style[i].lineColour != -1 ? style[i].lineColour
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
    #ifndef WIN32
    icons = gdk_pixmap_create_from_xpm (draw->window, NULL, NULL,
      FindResource ("icons.xpm"));
    #else
    icons = gdk_pixmap_create_from_xpm_d (draw->window, NULL, NULL,
      (gchar**) icons_xpm);
    #endif
  }  

//  gdk_gc_set_clip_rectangle (mygc, &clip);
//  gdk_gc_set_foreground (mygc, &styleColour[0][0]);
//  gdk_gc_set_line_attributes (mygc,
//    1, GDK_LINE_SOLID, GDK_CAP_PROJECTING, GDK_JOIN_MITER);
    
//  clip.width = draw->allocation.width - ZOOM_PAD_SIZE;
//  gdk_gc_set_clip_rectangle (mygc, &clip);
  
  GdkFont *f = gtk_style_get_font (draw->style);
  GdkRectangle clip;
  clip.x = 0;
  clip.y = 0;
#endif // !_WIN32_WCE
  clip.height = draw->allocation.height - STATUS_BAR;
  clip.width = draw->allocation.width;
  if (ButtonSize <= 0) ButtonSize = 4;
  #ifdef CAIRO_VERSION
  cairo_t *cai = gdk_cairo_create (draw->window);
  if (DetailLevel < 2) {
    cairo_font_options_t *caiFontOptions = cairo_font_options_create ();
    cairo_get_font_options (cai, caiFontOptions);
    cairo_font_options_set_antialias (caiFontOptions, CAIRO_ANTIALIAS_NONE);
    cairo_set_font_options (cai, caiFontOptions);
  }
  cairo_matrix_t mat;
  cairo_matrix_init_identity (&mat);
  #endif
  if (option == numberOfOptions) {
    if (zoom < 0) zoom = 2012345678;
    if (zoom / clip.width <= 1) zoom += 4000;
    int cosa = lrint (4294967296.0 * cosAzimuth * clip.width / zoom);
    int sina = lrint (4294967296.0 * sinAzimuth * clip.width / zoom);
    int xadj = clip.width / 2 -
                 ((clon * (__int64) cosa + clat * (__int64) sina) >> 32);
    int yadj = clip.height / 2 -
                 ((clon * (__int64) sina - clat * (__int64) cosa) >> 32);
    #define X(lon,lat) (xadj + \
                 (((lon) * (__int64) cosa + (lat) * (__int64) sina) >> 32))
    #define Y(lon,lat) (yadj + \
                 (((lon) * (__int64) sina - (lat) * (__int64) cosa) >> 32))

    int lonRadius = lrint (fabs (cosAzimuth) * zoom +
          fabs (sinAzimuth) * zoom / clip.width * clip.height) / 2 + 1000;
    int latRadius = lrint (fabs (cosAzimuth) * zoom / clip.width *
          clip.height + fabs (sinAzimuth) * zoom) / 2 + 10000;
//    int perpixel = zoom / clip.width;
    int doAreas = TRUE, blockIcon[2 * 128];
    memset (blockIcon, 0, sizeof (blockIcon)); // One bit per 16 x 16 area
  //    zoom / sqrt (draw->allocation.width * draw->allocation.height);
    for (int thisLayer = -5, nextLayer; thisLayer < 6;
         thisLayer = nextLayer, doAreas = !doAreas) {
      OsmItr itr (clon - lonRadius, clat - latRadius,
                  clon + lonRadius, clat + latRadius);
      // Widen this a bit so that we render nodes that are just a bit offscreen ?
      nextLayer = 6;
      
      while (Next (itr)) {
        ndType *nd = itr.nd[0];
        wayType *w = (wayType *)(data + nd->wayPtr);
        if (Style (w)->scaleMax <
                  zoom / clip.width * 175 / (DetailLevel + 6)) continue;
        
        int wLayer = nd->other[0] < 0 && nd->other[1] < 0 ? 5 : Layer (w);
        if (DetailLevel < 2 && Style (w)->areaColour != -1) {
          if (thisLayer > -5) continue;  // Draw all areas with layer -5
        }
        else if (zoom < 100000*100) {
        // Under low-zoom we draw everything on layer -5 (faster)
          if (thisLayer < wLayer && wLayer < nextLayer) nextLayer = wLayer;
          if (DetailLevel > 1) {
            if (doAreas) nextLayer = thisLayer;
            if (Style (w)->areaColour != -1 ? !doAreas : doAreas) continue;
          }
          if (wLayer != thisLayer) continue;
        }
        if (nd->other[0] >= 0) {
          nd = ndBase + itr.nd[0]->other[0];
          if (nd->lat == INT_MIN) nd = itr.nd[0]; // Node excluded from build
          else if (itr.left <= nd->lon && nd->lon < itr.right &&
              itr.top  <= nd->lat && nd->lat < itr.bottom) continue;
        } // Only process this way when the Itr gives us the first node, or
        // the first node that's inside the viewing area

        #ifndef _WIN32_WCE
        __int64 maxLenSqr = 0;
        double x0 = 0.0, y0 = 0.0; /* shut up gcc */
        #else
        int best = 0, bestW, bestH, x0, y0;
        #endif
        int len = strcspn ((char *)(w + 1) + 1, "\n");
        
        if (nd->other[0] < 0 && nd->other[1] < 0) {
          int x = X (nd->lon, nd->lat), y = Y (nd->lon, nd->lat);
          int *b = blockIcon + (x / (48 * 32) + y / 22 * 1) %
                      (sizeof (blockIcon) / sizeof (blockIcon[0]));
          if (!(*b & (1 << (x / 48 % 32)))) {
            *b |= 1 << (x / 48 % 32);
            int *icon = Style (w)->x + 4 * IconSet;
            if (icons && icon[2] != 0) {
              gdk_draw_drawable (draw->window, mygc, icons,
                icon[0], icon[1], x - icon[2] / 2, y - icon[3] / 2,
                icon[2], icon[3]);
            }
            
            #ifdef _WIN32_WCE
            SelectObject (mygc, sysFont);
            const unsigned char *sStart = (const unsigned char *)(w + 1) + 1;
            UTF16 *tStart = (UTF16 *) wcTmp;
            if (ConvertUTF8toUTF16 (&sStart,  sStart + len, &tStart, tStart +
                  sizeof (wcTmp) / sizeof (wcTmp[0]), lenientConversion)
                == conversionOK) {
              ExtTextOut (mygc, x - len * 3, y + icon[3] / 2, 0, NULL,
                  wcTmp, (wchar_t *) tStart - wcTmp, NULL);
            }
            #endif
            #ifdef CAIRO_VERSION
            //if (Style (w)->scaleMax > zoom / 2 || zoom < 2000) {
              mat.xx = mat.yy = 12.0;
              mat.xy = mat.yx = 0;
              x0 = x - mat.xx / 12.0 * 3 * len; /* Render the name of the node */
              y0 = y + mat.xx * f->ascent / 12.0 + icon[3] / 2;
              maxLenSqr = Sqr ((__int64) Style (w)->scaleMax);
              //4000000000000LL; // Without scaleMax, use 400000000
            //}
            #endif
          }
        }
        else if (Style (w)->areaColour != -1) {
          #ifndef _WIN32_WCE
          while (nd->other[0] >= 0) nd = ndBase + nd->other[0];
          static GdkPoint pt[1000];
          unsigned pts;
          for (pts = 0; pts < sizeof (pt) / sizeof (pt[0]) && nd->other[1] >= 0;
               nd = ndBase + nd->other[1]) {
            if (nd->lat != INT_MIN) {
              pt[pts].x = X (nd->lon, nd->lat);
              pt[pts++].y = Y (nd->lon, nd->lat);
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
          SelectObject (mygc, pen[StyleNr (w) + 1]);
          #endif
          int oldx = X (nd->lon, nd->lat);
          int oldy = Y (nd->lon, nd->lat);
          do {
            ndType *next = ndBase + nd->other[1];
            if (next->lat == INT_MIN) break; // Node excluded from build
            int x = X (next->lon, next->lat);
            int y = Y (next->lon, next->lat);
            if ((x <= clip.width || oldx <= clip.width) &&
                (x >= 0 || oldx >= 0) && (y >= 0 || oldy >= 0) &&
                (y <= clip.height || oldy <= clip.height)) {
              gdk_draw_line (draw->window, mygc, oldx, oldy, x, y);
              #ifdef _WIN32_WCE
              int newb = oldx > x ? oldx - x : x - oldx;
              if (newb < oldy - y) newb = oldy - y;
              if (newb < y - oldy) newb = y - oldy;
              if (best < newb) {
                best = newb;
                bestW = (x > oldx ? -1 : 1) * (x - oldx);
                bestH = (x > oldx ? -1 : 1) * (oldy - y);
                x0 = next->lon / 2 + nd->lon / 2;
                y0 = next->lat / 2 + nd->lat / 2;
              }
              #endif
              #ifdef CAIRO_VERSION
              __int64 lenSqr = (nd->lon - next->lon) * (__int64)(nd->lon - next->lon) +
                                 (nd->lat - next->lat) * (__int64)(nd->lat - next->lat);
              if (lenSqr > maxLenSqr) {
                maxLenSqr = lenSqr;
                double lonDiff = (nd->lon - next->lon) * cosAzimuth +
                                 (nd->lat - next->lat) * sinAzimuth;
                mat.yy = mat.xx = 12 * fabs (lonDiff) / sqrt (lenSqr);
                mat.xy = (lonDiff > 0 ? 12.0 : -12.0) *
                         ((nd->lat - next->lat) * cosAzimuth -
                          (nd->lon - next->lon) * sinAzimuth) / sqrt (lenSqr);
                mat.yx = -mat.xy;
                x0 = X (nd->lon / 2 + next->lon / 2,
                        nd->lat / 2 + next->lat / 2) +
                  mat.yx * f->descent / 12.0 - mat.xx / 12.0 * 3 * len;
                y0 = Y (nd->lon / 2 + next->lon / 2,
                        nd->lat / 2 + next->lat / 2) +
                  mat.xx * f->descent / 12.0 - mat.yx / 12.0 * 3 * len;
              }
              #endif
            }
            nd = next;
            oldx = x;
            oldy = y;
          } while (itr.left <= nd->lon && nd->lon < itr.right &&
                   itr.top  <= nd->lat && nd->lat < itr.bottom &&
                   nd->other[1] >= 0);
        } /* If it has one or more segments */

        #ifdef _WIN32_WCE
        if (best > len * 4) {
          double hoek = atan2 (bestH, bestW);
          logFont.lfEscapement = logFont.lfOrientation =
            1800 + int ((1800 / M_PI) * hoek);
          
          HFONT customFont = CreateFontIndirect (&logFont);
          HGDIOBJ oldf = SelectObject (mygc, customFont);
          const unsigned char *sStart = (const unsigned char *)(w + 1) + 1;
          UTF16 *tStart = (UTF16 *) wcTmp;
          if (ConvertUTF8toUTF16 (&sStart,  sStart + len, &tStart, tStart +
                sizeof (wcTmp) / sizeof (wcTmp[0]), lenientConversion)
              == conversionOK) {
            ExtTextOut (mygc, X (x0, y0) + int (len * 3 * cos (hoek)),
                  Y (x0, y0) - int (len * 3 * sin (hoek)), 0, NULL,
                  wcTmp, (wchar_t *) tStart - wcTmp, NULL);
          }
          SelectObject (mygc, oldf);
          DeleteObject (customFont);
        }
        #endif
        #ifdef CAIRO_VERSION
        if (maxLenSqr * DetailLevel > (zoom / clip.width) *
              (__int64) (zoom / clip.width) * len * len * 100 && len > 0) {
          for (char *txt = (char *)(w + 1) + 1; *txt != '\0';) {
            cairo_set_font_matrix (cai, &mat);
            char *line = (char *) malloc (strcspn (txt, "\n") + 1);
            memcpy (line, txt, strcspn (txt, "\n"));
            line[strcspn (txt, "\n")] = '\0';
            cairo_move_to (cai, x0, y0);
            cairo_show_text (cai, line);
            free (line);
            if (zoom / clip.width > 30) break;
            y0 += mat.xx * (f->ascent + f->descent) / 12;
            x0 += mat.xy * (f->ascent + f->descent) / 12;
            while (*txt != '\0' && *txt++ != '\n') {}
          }
        }
        #endif
      } /* for each visible tile */
    } // For each layer
  //  gdk_gc_set_foreground (draw->style->fg_gc[0], &highwayColour[rail]);
  //  gdk_gc_set_line_attributes (draw->style->fg_gc[0],
  //    1, GDK_LINE_SOLID, GDK_CAP_PROJECTING, GDK_JOIN_MITER);
    routeNodeType *x;
    if (shortest && (x = shortest->shortest)) {
      double len;
      int nodeCnt = 1;
      __int64 sumLat = x->nd->lat;
      #ifndef _WIN32_WCE
      gdk_gc_set_foreground (mygc, &routeColour);
      gdk_gc_set_line_attributes (mygc, 5,
        GDK_LINE_SOLID, GDK_CAP_PROJECTING, GDK_JOIN_MITER);
      #else
      SelectObject (mygc, pen[0]);
      #endif
      if (routeHeapSize > 1) {
        gdk_draw_line (draw->window, mygc, X (flon, flat), Y (flon, flat),
          X (x->nd->lon, x->nd->lat), Y (x->nd->lon, x->nd->lat));
      }
      len = sqrt (Sqr ((double) (x->nd->lat - flat)) +
        Sqr ((double) (x->nd->lon - flon)));
      for (; x->shortest; x = x->shortest) {
        gdk_draw_line (draw->window, mygc, X (x->nd->lon, x->nd->lat),
          Y (x->nd->lon, x->nd->lat),
          X (x->shortest->nd->lon, x->shortest->nd->lat),
          Y (x->shortest->nd->lon, x->shortest->nd->lat));
        len += sqrt (Sqr ((double) (x->nd->lat - x->shortest->nd->lat)) +
          Sqr ((double) (x->nd->lon - x->shortest->nd->lon)));
        sumLat += x->nd->lat;
        nodeCnt++;
      }
      gdk_draw_line (draw->window, mygc, X (x->nd->lon, x->nd->lat),
        Y (x->nd->lon, x->nd->lat), X (tlon, tlat), Y (tlon, tlat));
      len += sqrt (Sqr ((double) (x->nd->lat - tlat)) +
        Sqr ((double) (x->nd->lon - tlon)));
      wchar_t distStr[13];
      wsprintf (distStr, TEXT ("%.3lf km"), len * (20000 / 2147483648.0) *
        cos (LatInverse (sumLat / nodeCnt) * (M_PI / 180)));
      #ifndef _WIN32_WCE
      gdk_draw_string (draw->window, f, draw->style->fg_gc[0],
        clip.width - 7 * strlen (distStr), 10, distStr);
      #else
      SelectObject (mygc, sysFont);
      ExtTextOut (mygc, clip.width - 7 * wcslen (distStr), 0, 0, NULL,
        distStr, wcslen (distStr), NULL);
      #endif
    }
    #ifndef _WIN32_WCE
    for (int i = 1; ShowActiveRouteNodes && i < routeHeapSize; i++) {
      gdk_draw_line (draw->window, mygc,
        X (routeHeap[i]->nd->lon, routeHeap[i]->nd->lat) - 2,
        Y (routeHeap[i]->nd->lon, routeHeap[i]->nd->lat),
        X (routeHeap[i]->nd->lon, routeHeap[i]->nd->lat) + 2,
        Y (routeHeap[i]->nd->lon, routeHeap[i]->nd->lat));
    }
    #else
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
        SelectObject (mygc, pen[j < newWayCnt ? newWays[j].klas + 1 : 0]);
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
  } // Not in the menu
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
    #ifndef _WIN32_WCE
    cairo_move_to (cai, 50, draw->allocation.height / 2);
    cairo_show_text (cai, optStr);
    #else
    SelectObject (mygc, sysFont);
    const unsigned char *sStart = (const unsigned char*) optStr;
    UTF16 *tStart = (UTF16 *) wcTmp;
    if (ConvertUTF8toUTF16 (&sStart,  sStart + strlen (optStr), &tStart,
             tStart + sizeof (wcTmp) / sizeof (wcTmp[0]), lenientConversion)
        == conversionOK) {
      ExtTextOut (mygc, 50, draw->allocation.height / 2, 0, NULL,
         wcTmp, (wchar_t*) tStart - wcTmp, NULL);
    }
    #endif
  }
  #ifndef _WIN32_WCE
  gdk_draw_rectangle (draw->window, draw->style->bg_gc[0], TRUE,
    clip.width - ButtonSize * 20, clip.height - ButtonSize * 60,
    clip.width, clip.height);
  for (int i = 0; i < 3; i++) {
    gdk_draw_string (draw->window, f, draw->style->fg_gc[0],
      clip.width - ButtonSize * 10 - 5, clip.height + (f->ascent - f->descent)
      / 2 - ButtonSize * (20 * i + 10), i == 0 ? "O" : i == 1 ? "-" : "+");
  }
  #else
  int i = !HideZoomButtons || option != numberOfOptions ? 3 :
                                                MenuKey != 0 ? 0 : 1;
  RECT r;
  r.left = clip.width - ButtonSize * 20;
  r.top = clip.height - ButtonSize * 20 * i;
  r.right = clip.width;
  r.bottom = clip.height;
  FillRect (mygc, &r, (HBRUSH) GetStockObject(LTGRAY_BRUSH));
  SelectObject (mygc, sysFont);
  while (--i >= 0) {
    ExtTextOut (mygc, clip.width - ButtonSize * 10 - 5, clip.height - 5 -
        ButtonSize * (20 * i + 10), 0, NULL, i == 0 ? TEXT ("O") :
        i == 1 ? TEXT ("-") : TEXT ("+"), 1, NULL);
  }

  wchar_t coord[21];
  if (ShowCoordinates == 1) {
    wsprintf (coord, TEXT ("%9.5lf %10.5lf"), LatInverse (clat),
      LonInverse (clon));
    ExtTextOut (mygc, 0, 0, 0, NULL, coord, 20, NULL);
  }
  else if (ShowCoordinates == 2) {
    MEMORYSTATUS memStat;
    GlobalMemoryStatus (&memStat);
    wsprintf (coord, TEXT ("%9d"), memStat.dwAvailPhys );
    ExtTextOut (mygc, 0, 0, 0, NULL, coord, 9, NULL);
  }
  #endif
  #ifdef CAIRO_VERSION
  cairo_destroy (cai);
  #endif
/*
  clip.height = draw->allocation.height;
  gdk_gc_set_clip_rectangle (draw->style->fg_gc[0], &clip);
  gdk_draw_string (draw->window, f, draw->style->fg_gc[0],
    clip.width/2, clip.height - f->descent, "gosmore");
  */
  return FALSE;
}

GtkWidget *searchW;
GtkWidget *list;
#define numIncWays 40
wayType *incrementalWay[numIncWays];

int IdxSearch (int *idx, int h, char *key, unsigned z)
{
  for (int l = 0; l < h;) {
    char *tag = data + idx[(h + l) / 2];
    int diff = TagCmp (tag, key);
    while (*--tag) {}
    if (diff > 0 || (diff == 0 &&
      ZEnc ((unsigned)((wayType *)tag)[-1].clat >> 16, 
            (unsigned)((wayType *)tag)[-1].clon >> 16) >= z)) h = (h + l) / 2;
    else l = (h + l) / 2 + 1;
  }
  return h;
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
int IncrementalSearch (void)
{
  __int64 dista[numIncWays];
  char *taga[numIncWays];
  char *key = (char *) gtk_entry_get_text (GTK_ENTRY (searchW));
  int *idx =
    (int *)(ndBase + hashTable[bucketsMin1 + (bucketsMin1 >> 7) + 2]);
  int l = IdxSearch (idx, hashTable - idx, key, 0), count;
//  char *lastName = data + idx[min (hashTable - idx), 
//    int (sizeof (incrementalWay) / sizeof (incrementalWay[0]))) + l - 1];
  int cz = ZEnc ((unsigned) clat >> 16, (unsigned) clon >> 16);
  for (count = 0; count + l < hashTable - idx && count < numIncWays;) {
    int m[2], c = count, ipos, dir, bits;
    m[0] = IdxSearch (idx, hashTable - idx, data + idx[count + l], cz);
    m[1] = m[0] - 1;
    __int64 distm[2] = { -1, -1 }, big = ((unsigned __int64) 1 << 63) - 1;
    while (c < numIncWays && (distm[0] < big || distm[1] < big)) {
      dir = distm[0] < distm[1] ? 0 : 1;
      if (distm[dir] != -1) {
        for (ipos = c; count < ipos && distm[dir] < dista[ipos - 1]; ipos--) {
          dista[ipos] = dista[ipos - 1];
          incrementalWay[ipos] = incrementalWay[ipos - 1];
          taga[ipos] = taga[ipos - 1];
        }
        char *tag = data + idx[m[dir]];
        taga[ipos] = tag;
        while (*--tag) {}
        incrementalWay[ipos] = (wayType*)tag - 1;
        dista[ipos] = distm[dir];
        c++;
      }
      m[dir] += dir ? 1 : -1;

      if (0 <= m[dir] && m[dir] < hashTable - idx &&
        TagCmp (data + idx[m[dir]], data + idx[count + l]) == 0) {
        char *tag = data + idx[m[dir]];
        while (*--tag) {}
        distm[dir] = Sqr ((__int64)(clon - ((wayType*)tag)[-1].clon)) +
          Sqr ((__int64)(clat - ((wayType*)tag)[-1].clat));
      }
      else distm[dir] = big;
    }
    if (count == c) break; // Something's wrong. idx[count + l] not found !
    if (c >= numIncWays) {
      c = count; // Redo the adding
      for (bits = 0; bits < 16 && dista[numIncWays - 1] >> (bits * 2 + 32);
        bits++) {}
/* Print Z's for first solution 
      for (int j = c; j < numIncWays; j++) {
        for (int i = 0; i < 32; i++) printf ("%d%s",
          (ZEnc ((unsigned) incrementalWay[j]->clat >> 16,
                 (unsigned) incrementalWay[j]->clon >> 16) >> (31 - i)) & 1,
          i == 31 ? " y\n" : i % 2 ? " " : "");
      } */
/* Print centre, up, down, right and left to see if they're in the square
      for (int i = 0; i < 32; i++) printf ("%d%s", (cz >> (31 - i)) & 1,
        i == 31 ? " x\n" : i % 2 ? " " : "");
      for (int i = 0; i < 32; i++) printf ("%d%s", (
        ZEnc ((unsigned)(clat + (int) sqrt (dista[numIncWays - 1])) >> 16,
              (unsigned)clon >> 16) >> (31 - i)) & 1,
        i == 31 ? " x\n" : i % 2 ? " " : "");
      for (int i = 0; i < 32; i++) printf ("%d%s", (
        ZEnc ((unsigned)(clat - (int) sqrt (dista[numIncWays - 1])) >> 16,
              (unsigned)clon >> 16) >> (31 - i)) & 1,
        i == 31 ? " x\n" : i % 2 ? " " : "");
      for (int i = 0; i < 32; i++) printf ("%d%s", (
        ZEnc ((unsigned)clat >> 16,
              (unsigned)(clon + (int) sqrt (dista[numIncWays - 1])) >> 16) >> (31 - i)) & 1,
        i == 31 ? " x\n" : i % 2 ? " " : "");
      for (int i = 0; i < 32; i++) printf ("%d%s", (
        ZEnc ((unsigned)clat >> 16,
              (unsigned)(clon - (int) sqrt (dista[numIncWays - 1])) >> 16) >> (31 - i)) & 1,
        i == 31 ? " x\n" : i % 2 ? " " : "");
*/      
      int swap = cz ^ ZEnc (
        (unsigned) (clat + (clat & (1 << (bits + 16))) * 4 -
                                              (2 << (bits + 16))) >> 16,
        (unsigned) (clon + (clon & (1 << (bits + 16))) * 4 -
                                              (2 << (bits + 16))) >> 16);
      // Now we search through the 4 squares around (clat, clon)
      for (int mask = 0, maskI = 0; maskI < 4; mask += 0x55555555, maskI++) {
        int s = IdxSearch (idx, hashTable - idx, data + idx[count + l],
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
          char *tag = data + idx[s++];
          if (TagCmp (data + idx[count + l], tag) != 0) break;
          while (*--tag) {}
          wayType *w = (wayType*)tag - 1;
          if ((ZEnc ((unsigned)w->clat >> 16, (unsigned) w->clon >> 16) ^
               cz ^ (mask & swap)) >> (2 + (bits << 1))) break;
          __int64 d = Sqr ((__int64)(w->clat - clat)) +
                      Sqr ((__int64)(w->clon - clon));
          if (count < numIncWays || d < dista[count - 1]) {
            if (count < numIncWays) count++;
            for (ipos = count - 1; ipos > c && d < dista[ipos - 1]; ipos--) {
              dista[ipos] = dista[ipos - 1];
              incrementalWay[ipos] = incrementalWay[ipos - 1];
              taga[ipos] = taga[ipos - 1];
            }
            incrementalWay[ipos] = w;
            dista[ipos] = d;
            taga[ipos] = data + idx[s - 1];
          }
        } // For each entry in the square
      } // For each of the 4 squares
      break; // count < numIncWays implies a bug. Don't loop infinitely.
    } // If the search list is filled by tags with this text
    count = c;
  } // For each
  gtk_clist_freeze (GTK_CLIST (list));
  gtk_clist_clear (GTK_CLIST (list));
  for (int i = 0; i < count; i++) {
    char *m = (char *) malloc (strcspn (taga[i], "\n") + 1);
    sprintf (m, "%.*s", strcspn (taga[i], "\n"), taga[i]);
    gtk_clist_append (GTK_CLIST (list), &m);
    free (m);
  }
  gtk_clist_thaw (GTK_CLIST (list));
  return FALSE;
}

void SelectName (GtkWidget * /*w*/, int row, int /*column*/,
  GdkEventButton * /*ev*/, void * /*data*/)
{
  SetLocation (incrementalWay[row]->clon, incrementalWay[row]->clat);
  zoom = incrementalWay[row]->dlat + incrementalWay[row]->dlon + (1 << 15);
  gtk_toggle_button_set_active (GTK_TOGGLE_BUTTON (followGPSr), FALSE);
  FollowGPSr = FALSE;
  gtk_widget_queue_clear (draw);
}

void InitializeOptions (void)
{
  char *tag = data +
    *(int *)(ndBase + hashTable[bucketsMin1 + (bucketsMin1 >> 7) + 2]);
  while (*--tag) {}
  SetLocation (((wayType*)tag)[-1].clon, ((wayType*)tag)[-1].clat);
  zoom = ((wayType*)tag)[-1].dlat + ((wayType*)tag)[-1].dlon + (1 << 15);
}

#endif // HEADLESS

#ifndef _WIN32_WCE
int UserInterface (int argc, char *argv[])
{
  #if defined (__linux__)
  FILE *gmap = fopen64 ("gosmore.pak", "r");
  #else
  GMappedFile *gmap = g_mapped_file_new ("gosmore.pak", FALSE, NULL);
  #endif
  if (gmap) {
    #ifdef __linux__
    int ndCount[3];
    fseek (gmap, -sizeof (ndCount), SEEK_END);
    fread (ndCount, sizeof (ndCount), 1, gmap);
    long pakSize = ftello64 (gmap);
    data = (char *) mmap (NULL, ndCount[2],
                   PROT_READ, MAP_SHARED, fileno (gmap), 0);

    ndBase = (ndType *) ((char *)mmap (NULL, pakSize - (ndCount[2] & ~0xfff),
    //ndCount[0] * sizeof (*ndBase),
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
  }
  if (!gmap || !data || !ndBase || !hashTable || *(int*) data != pakHead) {
    fprintf (stderr, "Cannot read gosmore.pak\nYou can (re)build it from\n"
      "the planet file e.g. bzip2 -d planet-...osm.bz2 | %s rebuild\n",
      argv[0]);
    #ifndef HEADLESS
    gtk_init (&argc, &argv);
    gtk_dialog_run (GTK_DIALOG (gtk_message_dialog_new (NULL,
      GTK_DIALOG_MODAL, GTK_MESSAGE_ERROR, GTK_BUTTONS_OK,
      "Cannot read gosmore.pak\nYou can (re)build it from\n"
      "the planet file e.g. bzip2 -d planet-...osm.bz2 | %s rebuild\n",
      argv[0])));
    #endif
    return 8;
  }
  style = (struct styleStruct *)(data + 4);

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
    Route (TRUE, 0, 0);
    printf ("Content-Type: text/plain\n\r\n\r");
    if (!shortest) printf ("No route found\n\r");
    else if (routeHeapSize <= 1) printf ("Jump\n\r");
    for (; shortest; shortest = shortest->shortest) {
      wayType *w = (wayType*)(shortest->nd->wayPtr + data);
      char *name = (char*)(w + 1) + 1;
      printf ("%lf,%lf,%c,%s,%.*s\n\r", LatInverse (shortest->nd->lat),
        LonInverse (shortest->nd->lon), JunctionType (shortest->nd),
        klasTable[StyleNr (w)].desc, strcspn (name, "\n"), name);
    }
    return 0;
  }

  printf ("%s is in the public domain and comes without warrantee\n",argv[0]);
  #ifndef HEADLESS
  
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

  searchW = gtk_entry_new ();
  gtk_box_pack_start (GTK_BOX (vbox), searchW, FALSE, FALSE, 5);
  gtk_entry_set_text (GTK_ENTRY (searchW), "Search");
  gtk_signal_connect (GTK_OBJECT (searchW), "changed",
    GTK_SIGNAL_FUNC (IncrementalSearch), NULL);
  
  list = gtk_clist_new (1);
  gtk_clist_set_selection_mode (GTK_CLIST (list), GTK_SELECTION_SINGLE);
  gtk_box_pack_start (GTK_BOX (vbox), list, TRUE, TRUE, 5);
  gtk_signal_connect (GTK_OBJECT (list), "select_row",
    GTK_SIGNAL_FUNC (SelectName), NULL);
    
  carBtn = GTK_COMBO_BOX (gtk_combo_box_new_text ());
  #define M(x) if (motorcarR <= x ## R && x ## R < onewayR) \
                             gtk_combo_box_append_text (carBtn, #x);
  RESTRICTIONS
  #undef M
  gtk_combo_box_set_active (carBtn, 0);
  gtk_box_pack_start (GTK_BOX (vbox), GTK_WIDGET (carBtn), FALSE, FALSE, 5);
  gtk_signal_connect (GTK_OBJECT (carBtn), "changed",
    GTK_SIGNAL_FUNC (ChangeOption), NULL);

  fastestBtn = GTK_COMBO_BOX (gtk_combo_box_new_text ());
  gtk_combo_box_append_text (fastestBtn, "fastest");
  gtk_combo_box_append_text (fastestBtn, "shortest");
  gtk_combo_box_set_active (fastestBtn, 0);
  gtk_box_pack_start (GTK_BOX (vbox),
    GTK_WIDGET (fastestBtn), FALSE, FALSE, 5);
  gtk_signal_connect (GTK_OBJECT (fastestBtn), "changed",
    GTK_SIGNAL_FUNC (ChangeOption), NULL);

  detailBtn = GTK_COMBO_BOX (gtk_combo_box_new_text ());
  gtk_combo_box_append_text (detailBtn, "Highest");
  gtk_combo_box_append_text (detailBtn, "High");
  gtk_combo_box_append_text (detailBtn, "Normal");
  gtk_combo_box_append_text (detailBtn, "Low");
  gtk_combo_box_append_text (detailBtn, "Lowest");
  gtk_combo_box_set_active (detailBtn, 2);
  gtk_box_pack_start (GTK_BOX (vbox), GTK_WIDGET (detailBtn), FALSE, FALSE,5);
  gtk_signal_connect (GTK_OBJECT (detailBtn), "changed",
    GTK_SIGNAL_FUNC (ChangeOption), NULL);

  iconSet = GTK_COMBO_BOX (gtk_combo_box_new_text ());
  gtk_combo_box_append_text (iconSet, "Classic.Big");
  gtk_combo_box_append_text (iconSet, "Classic.Small                       ");
  gtk_combo_box_append_text (iconSet, "Square.Big");
  gtk_combo_box_append_text (iconSet, "Square.Small");
  gtk_combo_box_set_active (iconSet, 1);
  gtk_box_pack_start (GTK_BOX (vbox), GTK_WIDGET (iconSet), FALSE, FALSE, 5);
  gtk_signal_connect (GTK_OBJECT (iconSet), "changed",
    GTK_SIGNAL_FUNC (ChangeOption), NULL);

//  GtkWidget *getDirs = gtk_button_new_with_label ("Get Directions");
/*  gtk_box_pack_start (GTK_BOX (vbox), getDirs, FALSE, FALSE, 5);
  gtk_signal_connect (GTK_OBJECT (getDirs), "clicked",
    GTK_SIGNAL_FUNC (GetDirections), NULL);
*/
  location = gtk_entry_new ();
  gtk_box_pack_start (GTK_BOX (vbox), location, FALSE, FALSE, 5);
  gtk_signal_connect (GTK_OBJECT (location), "changed",
    GTK_SIGNAL_FUNC (ChangeLocation), NULL);
  
  orientNorthwards = gtk_check_button_new_with_label ("OrientNorthwards");
  gtk_box_pack_start (GTK_BOX (vbox), orientNorthwards, FALSE, FALSE, 5);
  gtk_signal_connect (GTK_OBJECT (orientNorthwards), "clicked",
    GTK_SIGNAL_FUNC (ChangeOption), NULL);
  gtk_widget_show (orientNorthwards);

  followGPSr = gtk_check_button_new_with_label ("Follow GPSr");
  
  #if !defined (WIN32) && !defined (ROUTE_TEST)
  struct sockaddr_in sa;
  int gpsSock = socket (PF_INET, SOCK_STREAM, 0);
  sa.sin_family = AF_INET;
  sa.sin_port = htons (2947);
  sa.sin_addr.s_addr = htonl (0x7f000001); // (204<<24)|(17<<16)|(205<<8)|18);
  if (gpsSock != -1 &&
      connect (gpsSock, (struct sockaddr *)&sa, sizeof (sa)) == 0) {
    send (gpsSock, "R\n", 2, 0);
    gpsSockTag = gdk_input_add (/*gpsData->gps_fd*/ gpsSock, GDK_INPUT_READ,
      (GdkInputFunction) ReceiveNmea /*gps_poll*/, NULL);

    gtk_box_pack_start (GTK_BOX (vbox), followGPSr, FALSE, FALSE, 5);
    gtk_signal_connect (GTK_OBJECT (followGPSr), "clicked",
      GTK_SIGNAL_FUNC (ChangeOption), NULL);
    gtk_widget_show (followGPSr);
  }
  #endif

  gtk_signal_connect (GTK_OBJECT (window), "delete_event",
    GTK_SIGNAL_FUNC (gtk_main_quit), NULL);
  
  gtk_widget_set_usize (window, 750, 550);
  gtk_widget_show (searchW);
  gtk_widget_show (list);
  gtk_widget_show (location);
  gtk_widget_show (draw);
  gtk_widget_show (GTK_WIDGET (carBtn));
  gtk_widget_show (GTK_WIDGET (fastestBtn));
  gtk_widget_show (GTK_WIDGET (detailBtn));
  gtk_widget_show (GTK_WIDGET (iconSet));
/*  gtk_widget_show (getDirs); */
  gtk_widget_show (hbox);
  gtk_widget_show (vbox);
  gtk_widget_show (window);
  option = numberOfOptions;
  ChangeOption ();
  IncrementalSearch ();
  InitializeOptions ();
  gtk_main ();
  FlushGpx ();
  
  #endif // HEADLESS
  return 0;
}
#endif // !_WIN32_WCE

/*--------------------------------- Rebuid code ---------------------------*/
#ifndef _WIN32_WCE
// These defines are only used during rebuild
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

int main (int argc, char *argv[])
{
  assert (l3 < 32);
  #ifndef WIN32
  int rebuildCnt = 0;
  if (argc > 1) {
    if ((argc != 6 && argc > 2) || stricmp (argv[1], "rebuild")) {
      fprintf (stderr, "Usage : %s [rebuild [bbox for 2 pass]]\n"
      "See http://wiki.openstreetmap.org/index.php/gosmore\n", argv[0]);
      return 1;
    }
    FILE *pak, *masterf;
    int styleCnt = firstElemStyle, ndStart;
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
    fwrite (&pakHead, sizeof (pakHead), 1, pak);
    
    //------------------------- elemstyle.xml : --------------------------
    const char *style_k[2 << STYLE_BITS], *style_v[2 << STYLE_BITS];
    int ruleCnt = 0, ruleNr[2 << STYLE_BITS];
    int defaultRestrict[2 << STYLE_BITS];
    memset (defaultRestrict, 0, sizeof (defaultRestrict));
    FILE *icons_csv = fopen (FindResource ("icons.csv"), "r");
    xmlTextReaderPtr sXml = xmlNewTextReaderFilename (
      FindResource ("elemstyles.xml"));
    if (!sXml || !icons_csv) {
      fprintf (stderr, "Either icons.csv or elemstyles.xml not found\n");
      return 3;
    }
    styleStruct srec[2 << STYLE_BITS];
    memset (&srec, 0, sizeof (srec));
    for (int i = 0; i < int (sizeof (srec) / sizeof (srec[0])); i++) {
      srec[i].lineColour = -1;
      srec[i].areaColour = -1;
      style_k[i] = style_v[i] = "";
    }
    /* If elemstyles contain these, we can delete these assignments : */
    for (int i = restriction_no_right_turn;
            i <= restriction_only_straight_on; i++) {
      style_k[i] = "restriction";
      srec[i].scaleMax = 1;
      srec[i].lineColour = 0; // Make it match.
    }
    style_v[restriction_no_right_turn] = "no_right_turn";
    style_v[restriction_no_left_turn] = "no_left_turn";
    style_v[restriction_no_u_turn] = "no_u_turn";
    style_v[restriction_no_straight_on] = "no_straight_on";
    style_v[restriction_only_right_turn] = "only_right_turn";
    style_v[restriction_only_left_turn] = "only_left_turn";
    style_v[restriction_only_straight_on] = "only_straight_on";
    
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
        int ipos = styleCnt;
        #define s(k,v,shortname,extraTags) \
          if (strcmp (#k, style_k[styleCnt]) == 0 && \
              strcmp (#v, style_v[styleCnt]) == 0) ipos = k ## _ ## v;
        STYLES
        #undef s
        ruleNr[ipos] = ruleCnt++;
        if (ipos != styleCnt) {
          memcpy (&srec[ipos], &srec[styleCnt], sizeof (srec[ipos]));
          memcpy (&srec[styleCnt], &srec[styleCnt + 1], sizeof (srec[0]));
          defaultRestrict[ipos] = defaultRestrict[styleCnt];
          defaultRestrict[styleCnt] = 0;
          style_k[ipos] = style_k[styleCnt];
          style_v[ipos] = style_v[styleCnt];
        }
        else if (styleCnt < (2 << STYLE_BITS) - 2) styleCnt++;
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
        if (srec[j].aveSpeed[i] == 0) { // e.g. highway=foot motorcar=yes
          for (int k = 0; k < l1; k++) {
            if (srec[j].aveSpeed[i] < srec[j].aveSpeed[k]) {
              srec[j].aveSpeed[i] = srec[j].aveSpeed[k];
            } // As fast as any other vehicle,
          } // without breaking our own speed limit :
          if (srec[j].aveSpeed[i] > max) srec[j].aveSpeed[i] = max;
        }
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
      if (i < S2GROUP (0) && !(groupf[i] = fopen64 (groupName[i], "w+"))) {
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
    int yesMask = 0, noMask = 0, *wayFseek = NULL;
    int lowzList[1000], lowzListCnt = 0, wStyle = styleCnt, ref = 0, role = 0;
    int member[2], *wayId = (int*) malloc (40000000 * 2 * 4), wayIdCnt = 0;
    s[0].lat = 0; // Should be -1 ?
    s[0].other = -2;
    s[1].other = -1;
    wayType w;
    w.clat = 0;
    w.clon = 0;
    w.dlat = INT_MIN;
    w.dlon = INT_MIN;
    w.bits = 0;
    
    if (argc >= 6) {
      masterWayType *masterWay = (masterWayType *) malloc (
        sizeof (*masterWay) * (ndStart / (sizeof (wayType) + 4)));

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
          if (stricmp (aname, "role") == 0) role = avalue[0];
          #define K_IS(x) (stricmp (tag_k, x) == 0)
          #define V_IS(x) (stricmp (avalue, x) == 0)
          if (stricmp (aname, "v") == 0) {
            int newStyle = 0;
            for (; newStyle < styleCnt && !(K_IS (style_k[newStyle]) &&
              V_IS (style_v[newStyle]) && (isNode ? srec[newStyle].x[2] :
                srec[newStyle].lineColour != -1 ||
                srec[newStyle].areaColour != -1)); newStyle++) {}
            // elemstyles rules are from most important to least important
            // Ulf has placed rules at the beginning that will highlight
            // errors, like oneway=true -> icon=deprecated. So they must only
            // match nodes when no line or area colour was given and only
            // match ways when no icon was given.
            if (defaultRestrict[newStyle] & (1 << roundaboutR)) {
              yesMask |= defaultRestrict[newStyle];
            }
            else if (newStyle < styleCnt && (wStyle == styleCnt ||
                     ruleNr[wStyle] > ruleNr[newStyle])) wStyle = newStyle;

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
            
            #define M(field) else if (K_IS (#field)) { \
                if (V_IS ("yes") || V_IS ("1") || V_IS ("permissive") || \
                    V_IS ("true")) { \
                  yesMask |= 1 << field ## R; \
                } else if (V_IS ("no") || V_IS ("0") || V_IS ("private")) { \
                  noMask |= 1 << field ## R; \
                } \
              }
            RESTRICTIONS
            #undef M
            
            else if (!V_IS ("no") && !V_IS ("false") && 
              !K_IS ("sagns_id") && !K_IS ("sangs_id") && 
              !K_IS ("is_in") && !V_IS ("residential") &&
              !V_IS ("junction") && /* Not approved and when it isn't obvious
                from the ways that it's a junction, the tag will often be
                something ridiculous like junction=junction ! */
// blocked as highway:  !V_IS ("mini_roundabout") && !V_IS ("roundabout") &&
              !V_IS ("traffic_signals") && !K_IS ("editor") &&
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
            if (strncasecmp (tag_k, "tiger:", 6) == 0 ||
                K_IS ("created_by") || K_IS ("converted_by") ||
                strncasecmp (tag_k, "source", 6) == 0 ||
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
        if (!wayFseek || *wayFseek) {
          if (stricmp (name, "member") == 0 && role != 'v') {
            for (int i = 0; i < wayIdCnt; i += 2) {
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
        if (stricmp (name, "way") == 0 || nameIsNode || nameIsRelation) {
          w.bits += wStyle;
          if (!nameIsRelation && !nameIsNode) {
            wayId[wayIdCnt++] = nd.id;
            wayId[wayIdCnt++] = ftell (pak);
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
          wStyle = styleCnt;
        }
      } // if it was </...>
      xmlFree (name);
    } // While reading xml
    free (wayId);
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
HANDLE port = INVALID_HANDLE_VALUE;

HBITMAP bmp;
HDC memDc, bufDc;
HPEN pen[2 << STYLE_BITS];
int pakSize;

BOOL CALLBACK DlgSearchProc (
	HWND hwnd, 
	UINT Msg, 
	WPARAM wParam, 
	LPARAM lParam)
{
    switch (Msg) {
    case WM_COMMAND:
      if (LOWORD (wParam) == IDC_EDIT1) {
        HWND edit = GetDlgItem (hwnd, IDC_EDIT1);
        memset (appendTmp, 0, sizeof (appendTmp));
        int wstrlen = Edit_GetLine (edit, 0, appendTmp, sizeof (appendTmp));
        unsigned char *tStart = (unsigned char*) searchStr;
        const UTF16 *sStart = (const UTF16 *) appendTmp;
        if (ConvertUTF16toUTF8 (&sStart,  sStart + wstrlen,
              &tStart, tStart + sizeof (searchStr), lenientConversion)
            == conversionOK) {
          hwndList = GetDlgItem (hwnd, IDC_LIST1);
          SendMessage (hwndList, LB_RESETCONTENT, 0, 0);
          IncrementalSearch ();
        }
	return TRUE;
      }
      else if (wParam == IDC_SEARCHGO
         || LOWORD (wParam) == IDC_LIST1 && HIWORD (wParam) == LBN_DBLCLK) {
        HWND hwndList = GetDlgItem (hwnd, IDC_LIST1);
        int idx = SendMessage (hwndList, LB_GETCURSEL, 0, 0);
        SipShowIM (SIPF_OFF);
        if (ModelessDialog) ShowWindow (hwnd, SW_HIDE);
        else EndDialog (hwnd, 0);
        if (idx != LB_ERR) SelectName (NULL, idx, 0, NULL, NULL);
        InvalidateRect (mWnd, NULL, FALSE);
        return TRUE;
      }
      else if (wParam == IDC_BUTTON1) {
        SipShowIM (SIPF_OFF);
        if (ModelessDialog) ShowWindow (hwnd, SW_HIDE);
        else EndDialog (hwnd, 0);
      }
    }
    return FALSE;
}

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
          pen[0] = CreatePen (PS_SOLID, 4, 0xff);
          for (int i = 0; i < 1 || style[i - 1].scaleMax; i++) {
            pen[i + 1] = CreatePen (style[i].dashed ? PS_DASH : PS_SOLID,
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
      InvalidateRect (hWnd, NULL, FALSE);
      if (wParam == '0' || wParam == MenuKey) HitButton (0);
      if (wParam == '8') HitButton (1);
      if (wParam == '9') HitButton (2);
      if (Exit) PostMessage (hWnd, WM_CLOSE, 0, 0);
      if (ZoomInKeyNum <= option && option < HideZoomButtonsNum) {
        #define o(en,min,max) if (option == en ## Num) en = wParam;
        OPTIONS
        #undef o
        break;
      }
      if (wParam == (DWORD) ZoomInKey) zoom = zoom * 3 / 4;
      if (wParam == (DWORD) ZoomOutKey) zoom = zoom * 4 / 3;

      do { // Keep compiler happy
        int oldCsum = clat + clon;
        if (VK_DOWN == wParam) clat -= zoom / 2;
        else if (VK_UP == wParam) clat += zoom / 2;
        else if (VK_LEFT == wParam) clon -= zoom / 2;
        else if (VK_RIGHT == wParam) clon += zoom / 2;
        if (oldCsum != clat + clon) FollowGPSr = FALSE;
      } while (0);
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
    case WM_LBUTTONDOWN:
      //MoveTo (LOWORD(lParam), HIWORD(lParam));
      //PostQuitMessage (0);
      //if (HIWORD(lParam) < 30) {
        // state=LOWORD(lParam)/STATEWID;
      //}
      GdkEventButton ev;
      ev.x = LOWORD (lParam);
      ev.y = HIWORD (lParam);
      ev.button = 1;
      Click (NULL, &ev, NULL);
      if (Exit) PostMessage (hWnd, WM_CLOSE, 0, 0);
      InvalidateRect (hWnd, NULL, FALSE);
      break;
    case WM_LBUTTONUP:
      break;
    case WM_MOUSEMOVE:
      //LineTo (LOWORD(lParam), HIWORD(lParam));
      break;
    /*case WM_COMMAND:
     //switch(wParam) {
     //}
     break; */
    default:
      return(DefWindowProc(hWnd,message,wParam,lParam));
  }
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
  wc.hIcon=NULL; 
  wc.hCursor=LoadCursor(NULL,IDC_ARROW);
  wc.hbrBackground=(HBRUSH) GetStockObject(WHITE_BRUSH);
  wc.lpszMenuName = NULL;
  wc.lpszClassName = TEXT ("GosmoreWClass");

  return(RegisterClass(&wc));
}

HWND InitInstance(int nCmdShow)
{
  mWnd = CreateWindow (TEXT ("GosmoreWClass"), TEXT ("gosmore"), WS_DLGFRAME,
    0, 0, CW_USEDEFAULT/* 20 */,/* 240*/CW_USEDEFAULT,NULL,NULL, hInst,NULL);

  if(!mWnd) return(FALSE);

  ShowWindow (mWnd,nCmdShow);
  //UpdateWindow (mWnd);


  return mWnd;
}

volatile int guiDone = FALSE;

DWORD WINAPI NmeaReader (LPVOID lParam)
{
 // $GPGLL,2546.6752,S,02817.5780,E,210130.812,V,S*5B
  DWORD nBytes;
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
    if (BaudRate) {
      DCB portState;
      if(!GetCommState(port, &portState)) {
        MessageBox (NULL, TEXT ("GetCommState Error"), TEXT (""),
          MB_APPLMODAL|MB_OK);
        return(1);
      }
      portState.BaudRate = BaudRate;
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
    }

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
  //FILE *log = fopen ("\\My Documents\\log.nmea", "a");
  while (!guiDone) {
    //nBytes = sizeof (rx) - got;
    //got = 0;
    if (!ReadFile(port, rx, sizeof(rx), &nBytes, NULL) || nBytes <= 0) {
      continue;
    }
    //if (log) fwrite (rx, nBytes, 1, log);

    //wndStr[0]='\0';
    //FormatMessage (FORMAT_MESSAGE_FROM_SYSTEM, NULL, GetLastError(),
    //MAKELANGID(LANG_ENGLISH,SUBLANG_ENGLISH_US),wndStr,STRLEN,NULL);
    
    if (ProcessNmea (rx, (unsigned*)&nBytes)) {
      PostMessage (mWnd, WM_USER + 1, 0, (intptr_t) gpsNew);
    }
  }
  guiDone = FALSE;
  //if (log) fclose (log);
  CloseHandle (port);
  return 0;
}

void XmlOut (FILE *newWayFile, char *k, char *v)
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

int WINAPI WinMain(
    HINSTANCE  hInstance,	  // handle of current instance
    HINSTANCE  hPrevInstance,	  // handle of previous instance
    LPWSTR  lpszCmdLine,	          // pointer to command line
    int  nCmdShow)	          // show state of window
{
  if(hPrevInstance) return(FALSE);
  hInst = hInstance;
  wchar_t argv0[80];
  GetModuleFileName (NULL, argv0, sizeof (argv0) / sizeof (argv0[0]));

  wcscpy (argv0 + wcslen (argv0) - 8, TEXT ("ore.opt")); // _arm.exe to ore.opt
  FILE *optFile = _wfopen (argv0, TEXT ("r"));
  
  if (!optFile) {
    strcpy (docPrefix, "\\My Documents\\");
    optFile = fopen ("\\My Documents\\gosmore.opt", "rb");
  }
  else {
    UTF16 *sStart = (UTF16*) argv0;
    unsigned char *tStart = (unsigned char *) docPrefix;
    ConvertUTF16toUTF8 ((const UTF16 **) &sStart, sStart + wcslen (argv0) - 11,
      &tStart, tStart + sizeof (docPrefix), lenientConversion);
    *tStart = '\0';
  }

  wcscpy (argv0 + wcslen (argv0) - 7, TEXT ("ore.pak")); // _arm.exe to ore.pak
  HANDLE gmap = CreateFileForMapping (argv0, GENERIC_READ, FILE_SHARE_READ,
    NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
  if (gmap == INVALID_HANDLE_VALUE) {
    MessageBox (NULL, TEXT ("No pak file"), TEXT (""), MB_APPLMODAL|MB_OK);
    return 1;
  }
  pakSize = GetFileSize(gmap, NULL);
  gmap = CreateFileMapping(gmap, NULL, PAGE_READONLY, 0, 0, 0);
  data = (char*) MapViewOfFile (gmap, FILE_MAP_READ, 0, 0, 0);
  if (!data) {
    MessageBox (NULL, TEXT ("mmap problem. Pak file too big ?"),
      TEXT (""), MB_APPLMODAL|MB_OK);
    return 1;
  }

  #if 0
  FILE *gmap = _wfopen (/*"./gosmore.pak"*/ argv0, TEXT ("rb"));

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
  style = (struct styleStruct *)(data + 4);
  hashTable = (int *) (data + pakSize);
  ndBase = (ndType *)(data + hashTable[-1]);
  bucketsMin1 = hashTable[-2];
  hashTable -= bucketsMin1 + (bucketsMin1 >> 7) + 5;

  if(!InitApplication ()) return(FALSE);
  if (!InitInstance (nCmdShow)) return(FALSE);

  GtkWidget dumdraw;
  dumdraw.allocation.width = GetSystemMetrics(SM_CXSCREEN);
  dumdraw.allocation.height = GetSystemMetrics(SM_CYSCREEN);
  draw = &dumdraw;

  InitCeGlue ();
  if (SHFullScreenPtr) {
    (*SHFullScreenPtr)(mWnd, SHFS_HIDETASKBAR |
      SHFS_HIDESTARTICON | SHFS_HIDESIPBUTTON);
    MoveWindow (mWnd, 0, 0, dumdraw.allocation.width,
      dumdraw.allocation.height, FALSE);
  }

  newWays[0].cnt = 0;
  IconSet = 1;
  DetailLevel = 3;
  ButtonSize = 4;
  int newWayFileNr = 0;
  if (optFile) {
    #define o(en,min,max) fread (&en, sizeof (en), 1, optFile);
    OPTIONS
    #undef o
    fread (&newWayFileNr, sizeof (newWayFileNr), 1, optFile);
    fclose (optFile);
    option = numberOfOptions;
  }
  Exit = 0;
  IncrementalSearch ();
  InitializeOptions ();

  dlgWnd = CreateDialog (hInst, MAKEINTRESOURCE(IDD_DLGSEARCH),
    NULL, (DLGPROC)DlgSearchProc); // Just in case user goes modeless

  DWORD threadId;
  wchar_t portname[6];
  wsprintf (portname, TEXT ("COM%d:"), CommPort);
  if (CommPort == 0) {}
  else if((port=CreateFile (portname, GENERIC_READ | GENERIC_WRITE, 0,
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

  while (port != INVALID_HANDLE_VALUE && guiDone) Sleep (1000);

  optFile = _wfopen (argv0, TEXT ("r+"));
  if (!optFile) optFile = fopen ("\\My Documents\\gosmore.opt", "wb");
  if (optFile) {
    #define o(en,min,max) fwrite (&en, sizeof (en),1, optFile);
    OPTIONS
    #undef o
    fread (&newWayFileNr, sizeof (newWayFileNr), 1, optFile);
    fclose (optFile);
  }
  gpsNewStruct *first = FlushGpx ();
  if (newWayCnt > 0) {
    char newWayFileName[40];
    if (first) sprintf (newWayFileName, "%s%.2s%.2s%.2s-%.6s.osm", docPrefix,
      first->fix.date + 4, first->fix.date + 2, first->fix.date,
      first->fix.tm);
    else sprintf (newWayFileName, "%sgosmore%d.osm", newWayFileNr);
    FILE *newWayFile = fopen (newWayFileName, "w");
    if (newWayFile) {
      fprintf (newWayFile, "<?xml version='1.0' encoding='UTF-8'?>\n"
                           "<osm version='0.5' generator='gosmore'>\n");
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

  return 0;
}
#endif
