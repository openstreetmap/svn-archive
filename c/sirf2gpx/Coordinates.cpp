#include "Coordinates.hpp"

#include <cmath>

// wgs84 parameters
const double a = 6378137.0;
const double b = 6356752.31424518;
const double f = .00335281066474740195; // 1.0 - (b / a)
const double e2 = .00669437999014115997; // (a*a - b*b) / (a*a)
const double edash2 = .00673949674227627580; // (a*a - b*b) / (b*b)

#define RAD_TO_DEG 57.29577951308232087721
#define SECONDS_PER_WEEK 604800L
#define GPS_TIME_ORIGIN 315964800L

using namespace std;

void Coordinates::convertToLLA(double x, double y, double z,
			       double &lat, double &lon, double &alt) {
  double p, theta;

  p = sqrt(x*x + y*y);
  theta = atan2(z*a, p*b);

  lon = atan2(y, x);
  lat = atan2(z + edash2 * b * pow(sin(theta), 3),
	      p -     e2 * a * pow(cos(theta), 3));
  alt = (p / cos(lat)) - a / sqrt(1.0 - e2 * pow(sin(lat), 2));

  lon *= RAD_TO_DEG;
  lat *= RAD_TO_DEG;
}

std::string Coordinates::convertToTimeString(unsigned int week,
					     double timeOfWeek) {
  char rv[30], t[10];
  time_t gps_time;
  size_t i;
  double dummy;

  gps_time = time_t(timeOfWeek) +
    time_t(week) * SECONDS_PER_WEEK +
    GPS_TIME_ORIGIN;

  i = strftime(rv, 30, "%Y-%m-%dT%H:%M:%S", gmtime(&gps_time));
  snprintf(t, 10, "%.3fZ", modf(timeOfWeek, &dummy));
  sprintf(&rv[i], "%s", &t[1]);

  return std::string(rv);
}

std::string Coordinates::convertToFix(unsigned int mode) {
  switch (mode) {
  case 3:
    return std::string("2d");
  case 4:
    return std::string("3d");
  }
  return std::string("none");
}

