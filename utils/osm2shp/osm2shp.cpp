#include <libshp/shapefil.h>
#include "Node.h"
#include "Components.h"
#include "Parser.h"
#include "Way.h"
#include <vector>
#include <fstream>

using std::cerr;
using std::endl;

bool makeNodeShp(OSM::Components *comp, const char* shpname);
bool makeWayShp(OSM::Components *comp, const char* shpname);
std::vector<double> getLongs(const std::vector<double>& wayCoords);
std::vector<double> getLats(const std::vector<double>& wayCoords);

int main (int argc, char* argv[])
{
	if(argc!=4)
	{
		cerr << "Usage: osm2shp OSMfile nodeSHPfile waySHPfile" << endl;
		exit(1);
	}

	std::ifstream in(argv[1]);
	if(in.good())
	{
		OSM::Components *comp = OSM::Parser::parse(in);
		in.close();
		if(comp)
		{
			makeNodeShp(comp,argv[2]);
			makeWayShp(comp,argv[3]);
			delete comp;
		}
		else
		{
			cerr << OSM::Parser::getError() << endl;
			exit(1);
		}
	}

	return 0;
}

bool makeNodeShp(OSM::Components *comp, const char* shpname)
{
		SHPHandle shp = SHPCreate(shpname,SHPT_POINT);
		if(shp)
		{
			DBFHandle dbf = DBFCreate(shpname);
			if(dbf)
			{
				int amenity = DBFAddField(dbf,"amenity",FTString,255,0);
				int natural = DBFAddField(dbf,"natural",FTString,255,0);
				int place = DBFAddField(dbf,"place",FTString,255,0);
				int name = DBFAddField(dbf,"name",FTString,255,0);

				double lon, lat;

				comp->rewindNodes();
				while(comp->hasMoreNodes())
				{
					OSM::Node *node = comp->nextNode();

					// We're only interested in nodes with tags
					if(node && node->hasTags())
					{
						lon = node->getLon(); 
						lat=node->getLat();
						SHPObject *object = SHPCreateSimpleObject
							(SHPT_POINT,1,&lon,&lat,NULL);

						int i = SHPWriteObject(shp, -1, object);

						SHPDestroyObject(object);

						DBFWriteStringAttribute
							(dbf,i,amenity,node->getTag("amenity").c_str());
						DBFWriteStringAttribute
							(dbf,i,natural,node->getTag("natural").c_str());
						DBFWriteStringAttribute
							(dbf,i,place,node->getTag("place").c_str());
						DBFWriteStringAttribute
							(dbf,i,name,node->getTag("name").c_str());
					}
				}

				DBFClose(dbf);
			}
			else
			{
				cerr << "could not open node dbf" << endl;
				return false;
			}
			SHPClose(shp);
		}
		else
		{
			cerr << "could not open node shp" << endl;
			return false;
		}
	
	return true;
}

bool makeWayShp(OSM::Components *comp, const char* shpname)
{
		SHPHandle shp = SHPCreate(shpname,SHPT_ARC); // ARC means polyline!
		if(shp)
		{
			DBFHandle dbf = DBFCreate(shpname);
			if(dbf)
			{
				int highway = DBFAddField(dbf,"highway",FTString,255,0);
				int name = DBFAddField(dbf,"name",FTString,255,0);
				int ref = DBFAddField(dbf,"ref",FTString,255,0);

				comp->rewindWays();
				std::vector<double> wayCoords, longs, lats;

				while(comp->hasMoreWays())
				{
					OSM::Way *way = comp->nextWay();
					if(way)
					{
						wayCoords = comp->getWayCoords(way->id);
						if(wayCoords.size())
						{
							longs = getLongs(wayCoords);
							lats = getLats(wayCoords);

							SHPObject *object = SHPCreateSimpleObject
								(SHPT_ARC,wayCoords.size()/2,
									&(longs[0]),&(lats[0]),NULL);

							int i = SHPWriteObject(shp, -1, object);

							SHPDestroyObject(object);

							DBFWriteStringAttribute
								(dbf,i,highway,way->getTag("highway").c_str());
							DBFWriteStringAttribute
								(dbf,i,name,way->getTag("name").c_str());
							DBFWriteStringAttribute
								(dbf,i,ref,way->getTag("ref").c_str());
						}
					}
				}

				DBFClose(dbf);
			}
			else
			{
				cerr << "could not open way dbf" << endl;
				return false;
			}
			SHPClose(shp);
		}
		else
		{
			cerr << "could not open way shp" << endl;
			return false;
		}
	
	return true;
}

std::vector<double> getLongs(const std::vector<double>& wayCoords)
{
	std::vector<double> longs;
	int size=wayCoords.size();
	for(int count=0; count<wayCoords.size(); count+=2)
	{
		longs.push_back(wayCoords[count]);
	}
	return longs;
}

std::vector<double> getLats(const std::vector<double>& wayCoords)
{
	std::vector<double> lats;
	for(int count=1; count<wayCoords.size(); count+=2)
	{
		lats.push_back(wayCoords[count]);
	}
	return lats;
}

