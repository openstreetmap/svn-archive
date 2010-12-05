#include "LatLng.h"
#include "OSRef.h"
#include "util.h"
#include "RefEll.h"
#include <cmath>

// Based on Jonathan Stott's JCoord.
// Licenced under the GNU GPL.

LatLng OSRef::toLatLng()

  {
    double OSGB_F0 = 0.9996012717;
    double N0 = -100000.0;
    double E0 = 400000.0;
    double phi0 = toRadians(49.0);
    double lambda0 = toRadians(-2.0);
   	RefEll AIRY_1830 (6377563.396, 6356256.909);
    double a = AIRY_1830.getMaj();
    double b = AIRY_1830.getMin();
    double eSquared = AIRY_1830.getEcc();
    double phi = 0.0;
    double lambda = 0.0;
    double E = this->easting;
    double N = this->northing;
    double n = (a - b) / (a + b);
    double M = 0.0;
    double phiPrime = ((N - N0) / (a * OSGB_F0)) + phi0;
    do {
      M =
          (b * OSGB_F0)
              * (((1 + n + ((5.0 / 4.0) * n * n) + ((5.0 / 4.0) * n * n * n)) * (phiPrime - phi0))
                  - (((3 * n) + (3 * n * n) + ((21.0 / 8.0) * n * n * n))
                      * sin(phiPrime - phi0) * cos(phiPrime + phi0))
                  + ((((15.0 / 8.0) * n * n) + ((15.0 / 8.0) * n * n * n))
                      * sin(2.0 * (phiPrime - phi0)) * 
                      cos(2.0 * (phiPrime + phi0))) - (((35.0 / 24.0) * n * n 
							  	* n)
                  * sin(3.0 * (phiPrime - phi0)) * 
                  cos(3.0 * (phiPrime + phi0))));
      phiPrime += (N - N0 - M) / (a * OSGB_F0);
    } while ((N - N0 - M) >= 0.001);
    double v =
        a * OSGB_F0
            * pow(1.0 - eSquared * sinSquared(phiPrime), -0.5);
    double rho =
        a * OSGB_F0 * (1.0 - eSquared)
            * pow(1.0 - eSquared * sinSquared(phiPrime), -1.5);
    double etaSquared = (v / rho) - 1.0;
    double VII = tan(phiPrime) / (2 * rho * v);
    double VIII =
        (tan(phiPrime) / (24.0 * rho * pow(v, 3.0)))
            * (5.0 + (3.0 * tanSquared(phiPrime)) + etaSquared - (9.0 * 
                tanSquared(phiPrime) * etaSquared));
    double IX =
        (tan(phiPrime) / (720.0 * rho * pow(v, 5.0)))
            * (61.0 + (90.0 * tanSquared(phiPrime)) + (45.0 * 
                tanSquared(phiPrime) * tanSquared(phiPrime)));
    double X = sec(phiPrime) / v;
    double XI =
        (sec(phiPrime) / (6.0 * v * v * v))
            * ((v / rho) + (2 * tanSquared(phiPrime)));
    double XII =
        (sec(phiPrime) / (120.0 * pow(v, 5.0)))
            * (5.0 + (28.0 * tanSquared(phiPrime)) + (24.0 * 
                tanSquared(phiPrime) * tanSquared(phiPrime)));
    double XIIA =
        (sec(phiPrime) / (5040.0 * pow(v, 7.0)))
            * (61.0
                + (662.0 * tanSquared(phiPrime))
                + (1320.0 * tanSquared(phiPrime) * 
                    tanSquared(phiPrime)) + (720.0 * tanSquared(phiPrime)
                * tanSquared(phiPrime) * tanSquared(phiPrime)));
    phi =
        phiPrime - (VII * pow(E - E0, 2.0))
            + (VIII * pow(E - E0, 4.0)) - (IX * pow(E - E0, 6.0));
    lambda =
        lambda0 + (X * (E - E0)) - (XI * pow(E - E0, 3.0))
            + (XII * pow(E - E0, 5.0)) - (XIIA * pow(E - E0, 7.0));

    return LatLng(toDegrees(phi), toDegrees(lambda));
  }
