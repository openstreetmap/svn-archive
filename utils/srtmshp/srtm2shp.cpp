// This is a UK-centric hack which works off OSGB Grid References, using 
// ancient code from before the dawn of OSM (well, almost)
// Feel free to modify

#include "SRTMConGen.h"

int main (int argc, char *argv[])
{

	if(argc<8)
	{
		cerr<<"Usage: srtm2shp e n scale w h interval shpOut [extra]" << endl;
		exit(1);
	}

	double e = atof(argv[1]);
	double n = atof(argv[2]);
	double scale = atof(argv[3]);
	int w = atoi(argv[4]);
	int h = atoi(argv[5]);
	int interval = atoi(argv[6]);
	Map map (e,n,scale/1000,w,h);
	if(argc>8)
		map.extend(atof(argv[8]));
	map.setGridRef(true);
	SRTMConGen congen(map,1);
	congen.generateShp(argv[7],interval);
	return 0;
}
