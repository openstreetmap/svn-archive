#include "Components.h"

#include <vector>

namespace OSM
{

bool makeShp(OSM::Components *comp, const char* nodes, const char* ways);
bool makeNodeShp(OSM::Components *comp, const char* shpname);
bool makeWayShp(OSM::Components *comp, const char* shpname);
std::vector<double> getLongs(const std::vector<double>& wayCoords);
std::vector<double> getLats(const std::vector<double>& wayCoords);

}
