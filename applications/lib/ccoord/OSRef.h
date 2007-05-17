#ifndef OSREF_H
#define OSREF_H

#include "RefEll.h"
#include "LatLng.h"
#include "util.h"
#include <cmath>
#include <string>

// Based on Jonathan Stott's JCoord.
// Licenced under the GNU GPL.

/**
 * Class to represent an Ordnance Survey grid reference
 * 
 * (c) 2006 Jonathan Stott
 * 
 * Created on 11-02-2006
 * 
 * @author Jonathan Stott
 * @version 1.0
 * @since 1.0
 */
class OSRef {

private:
  /**
   * Easting
   */
  double easting;

  /**
   * Northing
   */
  double northing;


  /**
   * Create a new Ordnance Survey grid reference.
   * 
   * @param easting
   *          the easting in metres
   * @param northing
   *          the northing in metres
   * @since 1.0
   */

public:
  OSRef(double easting, double northing) {
    this->easting = easting;
    this->northing = northing;
  }


  /**
   * Convert this OSGB grid reference to a latitude/longitude pair using the
   * OSGB36 datum. Note that, the LatLng object may need to be converted to the
   * WGS84 datum depending on the application.
   * 
   * @return a LatLng object representing this OSGB grid reference using the
   *         OSGB36 datum
   * @since 1.0
   */
  LatLng toLatLng(); 


  /**
   * Get the easting.
   * 
   * @return the easting in metres
   * @since 1.0
   */
   double getEasting() {
    return easting;
  }


  /**
   * Get the northing.
   * 
   * @return the northing in metres
   * @since 1.0
   */
  double getNorthing() {
    return northing;
  }

  std::string toSixFigureString();

  std::string getGridSquare()
  {
	return toSixFigureString().substr(0,2);
  }
};

#endif
