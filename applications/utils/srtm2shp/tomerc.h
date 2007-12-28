#include "Map.h"

EarthPoint lltomerc(const EarthPoint& ep);
double lonToMerc(double lon);
double latToMerc(double lat);
double mercToLat(double merc_y);
double phi2(double ts, double e);
double mercToLon(double merc_x);
EarthPoint merctoll(const EarthPoint& ep);
