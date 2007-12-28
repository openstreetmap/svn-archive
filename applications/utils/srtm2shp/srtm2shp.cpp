// This is a UK-centric hack which works off OSGB Grid References, using 
// ancient code from before the dawn of OSM (well, almost)
// Feel free to modify

#include "SRTMConGen.h"
#include <cstring>

void ReadCmdLineArgs(int argc, char *argv[],
						double bbox[4],
						int& interval, std::string& inCoord,
						std::string& outCoord, double& step,
						double& extend, bool& feet, 
						std::string& srtmlocation,std::string& shpfile);


////////////////////////////////////////////////////////////////////////////////

int main (int argc, char *argv[])
{

	if(argc<8)
	{
		cerr<<"Usage: srtm2shp -b comma_separated_bbox " 
			<<"[-I InCoordFormat] [-O OutCoordFormat]"<<endl
			<<"[-i height_interval]" <<" [-S step] [-f] [-l srtmlocation]" 
			<<" shpfile" <<endl;
		cerr<<endl<<"Allowed coord formats: latlon,Mercator" << endl
			<<"Mercator units are as in OpenStreetMap"<<endl;
		cerr<<endl<<"-f outputs heights in feet, otherwise metres"<<endl;
		cerr<<endl<<"Default SRTM location: ./data"<<endl;
		exit(1);
	}

	double bbox[4], step=-1, extend=0;
	int interval=50;
	std::string inCoord="latlon", outCoord="latlon",shpfile,srtmlocation="data";
	bool feet=false;

	ReadCmdLineArgs(argc,argv,bbox,interval,inCoord,outCoord,step,
					extend,feet,srtmlocation,shpfile);

	EarthPoint bottomleft,topright;
	SRTMConGen congen;

	if(inCoord=="Mercator")
	{
		step = (step<0 ? 10000: step);
		congen.setInCoord("Mercator");
	}
	else 
	{
		step = (step<0 ? 0.1: step);
	}

	if(outCoord=="Mercator")
		congen.setOutCoord("Mercator");

   	SHPHandle shp = SHPCreate(shpfile.c_str(),SHPT_ARC);
   	DBFHandle dbf = DBFCreate(shpfile.c_str());
   	int htidx = DBFAddField(dbf,"height",FTInteger,255,0);
    int mjridx = DBFAddField(dbf,"major",FTInteger,255,0);

	for(double ecount=bbox[0]; ecount<bbox[2]; ecount+=step)
	{
		for(double ncount=bbox[1]; ncount<bbox[3]; ncount+=step)
		{
			cerr<<"Trying: " << ecount<<","<<ncount<<endl;
			bottomleft.x=ecount-(step*extend);
			bottomleft.y=ncount-(step*extend);
			topright.x=(ecount+step)+(step*extend);
			topright.y=(ncount+step)+(step*extend);
			congen.makeGrid(srtmlocation,bottomleft,topright,feet);
   			congen.appendShp(shp,dbf,interval,htidx,mjridx);
			congen.deleteGrid();
		}
	}	
    DBFClose(dbf);
    SHPClose(shp);
	return 0;
}

void ReadCmdLineArgs(int argc, char *argv[],
						double bbox[4],
						int& interval, std::string& inCoord,
						std::string& outCoord, double& step,
						double& extend, bool& feet, 
						std::string& srtmlocation,std::string& shpfile)
{
	int required=0;
	while (argc>2 && strlen(argv[1])>1 && argv[1][0]=='-')
	{
		char *s;
		cerr<<argv[1]<<endl;

		switch(argv[1][1])
		{
			case 'b':
					s=strtok(argv[2],",");
					while (s!=NULL)
					{
						bbox[required++] = atof(s);
						s=strtok(NULL,",");
					}
					argc-=2;
					argv+=2;
					break;

			case 'i':
					interval=atoi(argv[2]);
					argc-=2;
					argv+=2;
					break;
			case 'I':
					inCoord=argv[2];
					argc-=2;
					argv+=2;
					break;
			case 'O':
					outCoord=argv[2];
					argc-=2;
					argv+=2;
					break;
			case 'S':
					step = atof(argv[2]);
					argc-=2;
					argv+=2;
					break;
			case 'x':
					extend = atof(argv[2]);
					argc-=2;
					argv+=2;
					break;
			case 'f':
					feet=true;
					argc-=2;
					argv+=2;
					break;
			case 'l':
					srtmlocation=argv[2];
					argc-=2;
					argv+=2;
					break;
			default:
					cerr<<"Unknown command line option " << argv[1][1]<<endl;
					exit(1);
		}
	}

	if(required<4)
	{
		cerr<<"Invalid bounding box!"<<endl;
		exit(1);
	}

	if(argc>1)
	{
		shpfile=argv[1];
	}
	else
	{
		cerr<<"No shapefile name specified"<<endl;
		exit(1);
	}
}



