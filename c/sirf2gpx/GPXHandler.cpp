#include "GPXHandler.hpp"
#include "Coordinates.hpp"

extern "C" {
#include <time.h>
#include <sys/time.h>
}

#include <iostream>

GPXHandler::GPXHandler(const char *filename, const AuthorInfo &_info) : 
  info(_info) {
  if ((fh = fopen(filename, "w")) == NULL) {
    // throw an error or something
  } else {
    writeHeader();
  }
  trackNumber = 0;
  inTrack = false;
  minLat = minLon = 180.0;
  maxLat = maxLon = -180.0;
}

GPXHandler::~GPXHandler() {
  writeFooter();
  fclose(fh);
}

void GPXHandler::handle(MeasuredNavigationDataOut p) {
  double lat, lon, alt;
  std::string datestring, fixstring;
  int numSats;

  if ((p.getMode1() & 7) > 2) { // need at least 2d solution

    if (!inTrack) {
      // start a new track
      fprintf(fh, "<trkseg>\n<number>%d</number>\n", trackNumber++);
      inTrack = true;
    }
    
    Coordinates::convertToLLA(p.getX(), p.getY(), p.getZ(),
			      lat, lon, alt);

    // update bounds
    if (lat > maxLat) maxLat = lat;
    if (lat < minLat) minLat = lat;
    if (lon > maxLon) maxLon = lon;
    if (lon < minLon) minLon = lon;

    datestring = 
      Coordinates::convertToTimeString(p.getWeek() + 1024, p.getTimeOfWeek());
    fixstring = Coordinates::convertToFix(p.getMode1() & 7);
    numSats = p.getSatellites();
    
    /*
      std::cout << "GPX: fix = " << fixstring << " (" 
      << numSats << ")" << std::endl;
    */
    fprintf(fh, "<trkpt lat=\"%.6f\" lon=\"%.6f\">"
	    "<ele>%.6f</ele>"
	    "<time>%s</time>"
	    "<sat>%d</sat>"
	    "<fix>%s</fix>"
	    "<pdop>%.3f</pdop>"
	    "</trkpt>\n",
	    lat, lon, alt, datestring.c_str(), numSats,
	    fixstring.c_str(), p.getDOP());
    fflush(fh);

  } else {

    if (inTrack) {
      // we just went out of track!
      inTrack = false;
      fprintf(fh, "</trkseg>\n");
      fflush(fh);
    }
  }

}

void GPXHandler::writeHeader() {
  time_t utc = ::time(NULL);
  struct tm *the_time = ::gmtime(&utc);

  fprintf(fh, "<?xml version=\"1.0\"?>\n"
	  "<gpx xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
	  " xmlns=\"http://www.topografix.com/GPX/1/1\"\n" 
	  " version=\"1.1\"\n creator=\"Test Program\"\n"
	  " xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1"
	  " http://www.topografix.com/GPX/1/1/gpx.xsd" 
	  " http://www.topografix.com/GPX/gpx_style/0/2"
	  " http://www.topografix.com/GPX/gpx_style/0/2/gpx_style.xsd\">\n"
	  "<metadata>\n <name>Test Document</name>\n"
	  " <desc>A document to test the gpx writing possibilites.</desc>\n"
	  "<author>\n<name>%s</name>\n<email id=\"%s\" domain=\"%s\"/>\n"
	  "</author>\n<copyright author=\"%s\">\n<year>%d</year>\n"
	  "<license>%s</license>\n</copyright>\n</metadata>\n<trk>\n",
	  info.getFullName().c_str(), info.getEmailID().c_str(), 
	  info.getEmailDomain().c_str(), info.getFullName().c_str(),
	  the_time->tm_year + 1900, info.getCopyrightURL().c_str());
  fflush(fh);
}

void GPXHandler::writeFooter() {
  if (inTrack) {
    fprintf(fh, "</trkseg>\n");
  }
  fprintf(fh, "</trk>\n<bounds minlat=\"%.6f\" minlon=\"%.6f\" "
	  "maxlat=\"%.6f\" maxlon=\"%.6f\"/>\n</gpx>\n",
	  minLat, minLon, maxLat, maxLon);
  fflush(fh);
}
