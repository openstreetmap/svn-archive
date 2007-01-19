/* fmapgen.cpp

   Freemap map generator

   Designed for generating Mapnik maps of the UK in OSGB projection.
   However it's easily adjusted, I suspect, to fit the national projection of
   any country.


   Parameters : 

   -u username
   -p password
   -b bbox
   -s scale (pixels/KM) 
   -w tile width 
   -h tile height
   -x XML file

*/

#include <mapnik/map.hpp>
#include <mapnik/layer.hpp>
#include <mapnik/envelope.hpp>
#include <mapnik/agg_renderer.hpp>
#include <mapnik/image_util.hpp>
#include <mapnik/load_map.hpp>
#include <mapnik/datasource_cache.hpp>
#include <mapnik/font_engine_freetype.hpp>

#include "Client.h"
#include "Parser.h"
#include "Components.h"
#include "llgr.h"

using namespace mapnik;
using std::cout;
using std::cerr;
using std::endl;
using OSM::EarthPoint;

struct Info 
{
	std::string username, password, xmlfile;
	double w, s, e, n; 
	double scale;
	int width, height;
};

bool genSHP(OSM::Client &client,EarthPoint& ep, EarthPoint& ep2,
				const std::string&, const std::string&);
void makeMapnikMap(EarthPoint& ep, EarthPoint& ep2, int w, int h,double);
std::string processCmdLineArgs(int argc, char* argv[],  Info& theInfo);



int main (int argc, char *argv[])
{
	Info  info;
	info.username = "";
	info.password = "";
	info.xmlfile = "freemap2.xml";
	info.w = 480000;
	info.s = 120000;
	info.e = 490000;
	info.n = 130000;
	info.scale = 100;
	info.width = info.height = 500;

	std::string error=processCmdLineArgs(argc,argv,info);
	if(error!="")
	{
		cerr << error << endl;
		exit(1);
	}

	datasource_cache::instance()->register_datasources
		("/usr/local/lib/mapnik/input");
	freetype_engine::instance()->register_font("/var/www/nick/data/Vera.ttf");
	OSM::Client client("http://www.openstreetmap.org/api/0.3");
	client.setLoginDetails(info.username, info.password);

	EarthPoint ep(info.w,info.s), ep2(0,0);
	
	while(ep.y < info.n)
	{
		ep.x = info.w;
		while (ep.x < info.e)
		{
			//EarthPoint gr = OSM::wgs84_ll_to_gr(ep);
			EarthPoint gr = ep;
			EarthPoint gr2 = EarthPoint(gr.x + (1000.0*info.width)/info.scale , 
				gr.y + (1000.0*info.height)/info.scale);
			ep2 = gr2;

			EarthPoint j=OSM::gr_to_wgs84_ll(ep), j2=OSM::gr_to_wgs84_ll(ep2);
			EarthPoint k= OSM::wgs84_ll_to_gr(j);

			if (genSHP(client,j,j2,"nodes","ways")) 
				makeMapnikMap(ep,ep2,info.width,info.height,info.scale);
			ep.x = ep2.x;
		}
		ep.y = ep2.y;
	}

	return 0;
}

std::string processCmdLineArgs(int argc, char* argv[], Info& theInfo)
{
	int i=1;
	while (i<argc)
	{
		if(!strcmp(argv[i],"-u"))
		{
			if(argc<=i+1)
				return("-u needs a username specified!");

			theInfo.username=argv[i+1];
			i++;
		}
		if(!strcmp(argv[i],"-p"))
		{
			if(argc<=i+1)
				return("-p needs a password specified!");

			theInfo.password=argv[i+1];
			i++;
		}
		else if (!strcmp(argv[i],"-b"))
		{
			if(argc<=i+4)
				return("-b needs a bounding box specified!");
			theInfo.w=atof(argv[i+1]);
			theInfo.s=atof(argv[i+2]);
			theInfo.e=atof(argv[i+3]);
			theInfo.n=atof(argv[i+4]);

			// For easy OSGB generation....
			if (  
				( ((int)theInfo.w)%1000) ||
				( ((int)theInfo.s)%1000) ||
				( ((int)theInfo.e)%1000) ||
				( ((int)theInfo.n)%1000)
				)
			{
				return "bounding box parameters not divisible by 1000!";
			}
			i+=4;
		}
		if(!strcmp(argv[i],"-s"))
		{
			if(argc<=i+1)
				return("-s needs a scale specified!");

			theInfo.scale=atof(argv[i+1]);
			i++;
		}
		if(!strcmp(argv[i],"-w"))
		{
			if(argc<=i+1)
				return("-w needs a width specified!");

			theInfo.width=atoi(argv[i+1]);
			i++;
		}
		if(!strcmp(argv[i],"-h"))
		{
			if(argc<=i+1)
				return("-h needs a height specified!");

			theInfo.height=atoi(argv[i+1]);
			i++;
		}
		if(!strcmp(argv[i],"-x"))
		{
			if(argc<=i+1)
				return("-x needs an XML file specified!");
			theInfo.xmlfile=argv[i+1];
			i++;
		}
		i++;
	}

	if(theInfo.username=="" || theInfo.password=="")
		return("Username and/or password unspecified");

	return "";
}

bool genSHP(OSM::Client &client,EarthPoint& ep, EarthPoint& ep2,
				const std::string& nodesSHP, const std::string& waysSHP)
{
	bool success = false;

	cerr << "Doing shape for bbox " << ep.x << " "<<ep.y << " " 
		<< ep2.x << " " << ep2.y << endl;

	std::string osmData = client.grabOSM("map",ep.x,ep.y,ep2.x,ep2.y);

	std::istringstream sstream;
	sstream.str(osmData);

	OSM::Components *comp = OSM::Parser::parse(sstream);
	if(comp)
	{
		cerr<<"doing cleanWays"<<endl;
		OSM::Components *comp2 = comp->cleanWays();
		cerr<<"done."<<endl;
		if(comp2)
		{	
			comp2->toOSGB();
			success = comp2->makeShp(nodesSHP,waysSHP);
			delete comp2;
		}
		delete comp;
	}
	return success;
}

void makeMapnikMap(EarthPoint& ep, EarthPoint& ep2, int w, int h,
						double scale)
{
	char pngfile[1024], shpfile[1024];
	sprintf(pngfile,"%03d%03d%03d.png",(int)scale,(int)(ep.x/1000),
						(int)(ep.y/1000));
	sprintf(shpfile,"%03d%03d%03d",(int)scale,(int)(ep.x/1000),
						(int)(ep.y/1000));
	static int i=1;
	Map m(w,h);
	load_map(m,"freemap2.xml");
	Layer osmlayer = m.getLayer(0); // srtm
	parameters p;
	p["type"] = "shape";
	p["file"] = shpfile; 
	m.getLayer(0).set_datasource(datasource_cache::instance()->create(p));
	Envelope<double> bbox (ep.x,ep.y,ep2.x,ep2.y); 
	m.zoomToBox(bbox);
	Image32 buf(m.getWidth(),m.getHeight());
	agg_renderer<Image32> r(m,buf);
	r.apply();
	ImageUtils::save_to_file(pngfile,"png",buf);
}
