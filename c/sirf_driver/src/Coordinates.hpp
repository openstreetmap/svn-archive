#ifndef COORDINATES_H
#define COORDINATES_H

#include <string>

class Coordinates {
public:
  static void convertToLLA(double x, double y, double z,
			   double &lat, double &lon, double &alt);
  static std::string convertToTimeString(unsigned int week,
					 double timeOfWeek);
  static std::string convertToFix(unsigned int mode);
};

#endif /* COORDINATES_H */
