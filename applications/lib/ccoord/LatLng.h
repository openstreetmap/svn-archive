#ifndef LATLNG_H
#define LATLNG_H

#include "RefEll.h"
#include "util.h"
#include <cmath>

// Based on Jonathan Stott's JCoord.
// Licenced under the GNU GPL.

class OSRef;

/**
 * Class to represent a latitude/longitude pair.
 * 
 * (c) 2006 Jonathan Stott
 * 
 * Created on 11-02-2006
 * 
 * @author Jonathan Stott
 * @version 1.0
 * @since 1.0
 */
class LatLng {

private:
  /**
   * Latitude in degrees.
   */
  double lat;

  /**
   * Longitude in degrees.
   */
  double lng;

public:
  /**
   * Create a new LatLng object to represent a latitude/longitude pair.
   * 
   * @param lat
   *          the latitude in degrees
   * @param lng
   *          the longitude in degrees
   * @since 1.0
   */
 
   LatLng(double lat, double lng) {
    this->lat = lat;
    this->lng = lng;
  }


  /**
   * Convert this->latitude and longitude into an OSGB (Ordnance Survey of Great
   * Britain) grid reference.
   * 
   * @return the converted OSGB grid reference
   * @since 1.0
   */
  OSRef toOSRef(); 


  /**
   * Convert this->LatLng from the OSGB36 datum to the WGS84 datum using an
   * approximate Helmert transformation.
   * 
   * @since 1.0
   */
  void toWGS84() {
   	 RefEll AIRY_1830 (6377563.396, 6356256.909);
    double a = AIRY_1830.getMaj();
    double eSquared = AIRY_1830.getEcc();
    double phi = toRadians(lat);
    double lambda = toRadians(lng);
    double v = a / (sqrt(1 - eSquared * sinSquared(phi)));
    double H = 0; // height
    double x = (v + H) * cos(phi) * cos(lambda);
    double y = (v + H) * cos(phi) * sin(lambda);
    double z = ((1 - eSquared) * v + H) * sin(phi);

    double tx = 446.448;
    double ty = -124.157;
    double tz = 542.060;
    double s = -0.0000204894;
    double rx = toRadians(0.00004172222);
    double ry = toRadians(0.00006861111);
    double rz = toRadians(0.00023391666);

    double xB = tx + (x * (1 + s)) + (-rx * y) + (ry * z);
    double yB = ty + (rz * x) + (y * (1 + s)) + (-rx * z);
    double zB = tz + (-ry * x) + (rx * y) + (z * (1 + s));

  	 RefEll WGS84     (6378137.000, 6356752.3141);
    a = WGS84.getMaj();
    eSquared = WGS84.getEcc();

    double lambdaB = toDegrees(atan(yB / xB));
    double p = sqrt((xB * xB) + (yB * yB));
    double phiN = atan(zB / (p * (1 - eSquared)));
    for (int i = 1; i < 10; i++) {
      v = a / (sqrt(1 - eSquared * sinSquared(phiN)));
      double phiN1 = atan((zB + (eSquared * v * sin(phiN))) / p);
      phiN = phiN1;
    }

    double phiB = toDegrees(phiN);

    lat = phiB;
    lng = lambdaB;
  }


  /**
   * Convert this->LatLng from the WGS84 datum to the OSGB36 datum using an
   * approximate Helmert transformation.
   * 
   * @since 1.0
   */
  void toOSGB36() {
    RefEll wgs84 (6378137.000, 6356752.3141);
    double a = wgs84.getMaj();
    double eSquared = wgs84.getEcc();
    double phi = toRadians(lat);
    double lambda = toRadians(lng);
    double v = a / (sqrt(1 - eSquared * sinSquared(phi)));
    double H = 0; // height
    double x = (v + H) * cos(phi) * cos(lambda);
    double y = (v + H) * cos(phi) * sin(lambda);
    double z = ((1 - eSquared) * v + H) * sin(phi);

    double tx = -446.448;
    double ty = 124.157;
    double tz = -542.060;
    double s = 0.0000204894;
    double rx = toRadians(-0.00004172222);
    double ry = toRadians(-0.00006861111);
    double rz = toRadians(-0.00023391666);

    double xB = tx + (x * (1 + s)) + (-rx * y) + (ry * z);
    double yB = ty + (rz * x) + (y * (1 + s)) + (-rx * z);
    double zB = tz + (-ry * x) + (rx * y) + (z * (1 + s));

    RefEll airy1830 (6377563.396, 6356256.909);
    a = airy1830.getMaj();
    eSquared = airy1830.getEcc();

    double lambdaB = toDegrees(atan(yB / xB));
    double p = sqrt((xB * xB) + (yB * yB));
    double phiN = atan(zB / (p * (1 - eSquared)));
    for (int i = 1; i < 10; i++) {
      v = a / (sqrt(1 - eSquared * sinSquared(phiN)));
      double phiN1 = atan((zB + (eSquared * v * sin(phiN))) / p);
      phiN = phiN1;
    }

    double phiB = toDegrees(phiN);

    lat = phiB;
    lng = lambdaB;
  }


  /**
   * Calculate the surface distance in kilometres from the this->LatLng to the
   * given LatLng.
   * 
   * @param ll
   * @return the surface distance in km
   * @since 1.0
   */
  double distance(LatLng ll) {
    double er = 6366.707;

    double latFrom = toRadians(getLat());
    double latTo = toRadians(ll.getLat());
    double lngFrom = toRadians(getLng());
    double lngTo = toRadians(ll.getLng());

    double d =
        acos(sin(latFrom) * sin(latTo) + cos(latFrom)
            * cos(latTo) * cos(lngTo - lngFrom))
            * er;

    return d;
  }


  /**
   * Return the latitude in degrees.
   * 
   * @return the latitude in degrees
   * @since 1.0
   */
  double getLat() {
    return lat;
  }


  /**
   * Return the longitude in degrees.
   * 
   * @return the longitude in degrees
   * @since 1.0
   */
  double getLng() {
    return lng;
  }
};

#endif
