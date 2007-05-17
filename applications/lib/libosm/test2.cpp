#include "functions.h"
#include <iostream>

using std::cout;
using std::endl;

int main()
{
	OSM::LatLon a (51.05, -0.72);
	OSM::Mercator b= a.toMercator();
	cout << b.e << " " << b.n << endl;
	return 0;
}
