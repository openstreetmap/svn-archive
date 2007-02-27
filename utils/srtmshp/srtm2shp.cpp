// This is a UK-centric hack which works off OSGB Grid References, using 
// ancient code from before the dawn of OSM (well, almost)
// Feel free to modify

#include "SRTMConGen.h"

int main (int argc, char *argv[])
{
	double step = 100000.0;

	if(argc<8)
	{
		cerr<<"Usage: srtm2shp w s e n interval shpOut [extra]" << endl;
		exit(1);
	}

	double w = atof(argv[1]);
	double s = atof(argv[2]);
	double e = atof(argv[3]);
	double n = atof(argv[4]);
	int interval = atoi(argv[5]);

	//any old stuff for the scale - ignored by shapefiles, only for PNG
	//generation. Need to sort out the ConGen code to remove this anomaly :-)
	Map map (0.1);
	map.setGridRef(true);
	SRTMConGen congen;
   	SHPHandle shp = SHPCreate(argv[6],SHPT_ARC);
   	DBFHandle dbf = DBFCreate(argv[6]);
   	int htidx = DBFAddField(dbf,"height",FTInteger,255,0);
    int mjridx = DBFAddField(dbf,"major",FTInteger,255,0);

	for(int ecount=w; ecount<e; ecount+=step)
	{
		for(int ncount=s; ncount<n; ncount+=step)
		{
			map.setBBOX(ecount,ncount,ecount+step,ncount+step);
			if(argc>7)
				map.extend(atof(argv[7]));
			congen.makeGrid(map,1);
   			congen.appendShp(shp,dbf,interval,htidx,mjridx);
			congen.deleteGrid();
		}
	}	
    DBFClose(dbf);
    SHPClose(shp);
	return 0;
}
