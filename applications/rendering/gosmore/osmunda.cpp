#include <stdio.h>
#include <unistd.h>
#include <libxml/xmlreader.h>
#include <sys/mman.h>
#include "libgosm.h"

int main (void)
{
  xmlTextReaderPtr xml = xmlReaderForFd (STDIN_FILENO, "", NULL, 0);
  FILE *file = fopen ("gosmore.pak", "r");
  if (!xml || !file || fseek (file, 0, SEEK_END) != 0 ||
        !GosmInit (mmap (NULL, ftell (file), PROT_READ, MAP_SHARED,
                              fileno (file), 0), ftell (file))) {
    fprintf (stderr, "Unable to open gosmore.pak\n");
    return 1;
  }
                              
  
  int ptCnt = 0;
  printf ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
  "<gpx\n"
  " version=\"1.0\"\n"
  " creator=\"osmunda\"\n"
  " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
  " xmlns=\"http://www.topografix.com/GPX/1/0\"\n"
  " xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/\">\n");
  while (xmlTextReaderRead (xml)) {
    char *name = (char *) BAD_CAST xmlTextReaderName (xml);
    if (xmlTextReaderNodeType (xml) == XML_READER_TYPE_ELEMENT) {
      while (stricmp (name, "trkpt") == 0 &&
             xmlTextReaderMoveToNextAttribute (xml)) {
        char *aname = (char *) BAD_CAST xmlTextReaderName (xml);
        char *avalue = (char *) BAD_CAST xmlTextReaderValue (xml);
        if (stricmp (aname, "lat") == 0) tlat = Latitude (atof (avalue));
        if (stricmp (aname, "lon") == 0) tlon = Longitude (atof (avalue));
        xmlFree (aname);
        xmlFree (avalue);
      }
    }
    if (xmlTextReaderNodeType (xml) == XML_READER_TYPE_END_ELEMENT) {
      if (stricmp (name, "trkpt") == 0) {
        if (ptCnt++ > 0) {
          int vehicle[] = { bicycleR, motorcarR, footR }, i;
          for (i = 0; i < sizeof (vehicle) / sizeof (vehicle[0]); i++) {
            Route (TRUE, 0, 0, /*tlon - flon, tlat - flat,*/ bicycleR, 0);
            //routeNode *itr;
            //for (itr = shortest; itr->shortest; itr = itr->shortest) {}
            if (routeHeapSize > 0 && (!shortest || !shortest->shortest ||
                !shortest->shortest->shortest)) break;
          }
          if (i == sizeof (vehicle) / sizeof (vehicle[0])) {
//                fprintf (stderr, "%d\n", shortest->best);
            printf ("<trk>\n<trkseg>\n<trkpt lat=\"%.5lf\" "
              "lon=\"%.5lf\"/>\n<trkpt lat=\"%.5lf\" lon=\"%.5lf\"/>\n"
              "</trkseg>\n</trk>\n", LatInverse (flat), LonInverse (flon),
              LatInverse (tlat), LonInverse (tlon));
          }
        }
        flat = tlat;
        flon = tlon;
      }
      if (stricmp (name, "trk") == 0) ptCnt = 0; // Gap in track
    }
    xmlFree (name);
  }
  printf ("</gpx>\n");
  return 0;
}
