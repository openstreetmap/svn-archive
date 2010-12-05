#ifndef REFELL_H
#define REFELL_H

// Based on Jonathan Stott's JCoord.
// Licenced under the GNU GPL.

/**
 * Class to represent a reference ellipsoid. Also provides a number of
 * pre-determined reference ellipsoids as constants.
 * 
 * (c) 2006 Jonathan Stott
 * 
 * Created on 11-Feb-2006
 * 
 * @author Jonathan Stott
 * @version 1.0
 * @since 1.0
 */

// Converted from JCoord (Java version)

 class RefEll {

  /**
   * Airy 1830 Reference Ellipsoid
   */
		
   //static final RefEll AIRY_1830 = new RefEll(6377563.396, 6356256.909);

  /**
   * WGS84 Reference Ellipsoid
   */
  //static final RefEll WGS84     = new RefEll(6378137.000, 6356752.3141);

  /**
   * Semi-major axis
   */
private:
  double             maj;

  /**
   * Semi-minor axis
   */
  double             min;

  /**
   * Eccentricity
   */
  double             ecc;

public:

  /**
   * Create a new reference ellipsoid
   * 
   * @param maj
   *          semi-major axis
   * @param min
   *          semi-minor axis
   * @since 1.0
   */
   RefEll(double maj, double min) {
    this->maj = maj;
    this->min = min;
    this->ecc = ((maj * maj) - (min * min)) / (maj * maj);
  }


  /**
   * Return the semi-major axis.
   * 
   * @return the semi-major axis
   * @since 1.0
   */
   double getMaj() {
    return maj;
  }


  /**
   * Return the semi-minor axis
   * 
   * @return the semi-minor axis
   * @since 1.0
   */
   double getMin() {
    return min;
  }


  /**
   * Return the eccentricity.
   * 
   * @return the eccentricity
   * @since 1.0
   */
   double getEcc() {
    return ecc;
  }
};

#endif
