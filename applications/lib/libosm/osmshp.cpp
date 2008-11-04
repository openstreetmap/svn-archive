#include "osmshp.h"
#include <shapefil.h>
#include "Node.h"
#include "Components.h"
#include "Parser.h"
#include "Way.h"
#include "osmshp.h"
#include <vector>
#include <fstream>

using std::cerr;
using std::endl;

namespace OSM
{

bool makeShp(OSM::Components *comp, const char* nodes, const char* ways)
{
	if (makeNodeShp(comp,nodes))
	{
		if(makeWayShp(comp,ways))
		{
			return true;
		}
	}
	return false;
}

bool makeNodeShp(OSM::Components *comp, const char* shpname)
{
		SHPHandle shp = SHPCreate(shpname,SHPT_POINT);
		if(shp)
		{
			DBFHandle dbf = DBFCreate(shpname);
			if(dbf)
			{
				std::map<int,std::string> fields;
				std::set<std::string> nodeTags = comp->getNodeTags();
				for(std::set<std::string>::iterator i=nodeTags.begin();
					i!=nodeTags.end(); i++)
				{
					fields[DBFAddField(dbf,i->c_str(),FTString,255,0)] = *i;
				}

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

						int objid = SHPWriteObject(shp, -1, object);

						SHPDestroyObject(object);

						for(std::map<int,std::string>::iterator j=
								fields.begin(); j!=fields.end(); j++)
						{
							DBFWriteStringAttribute
								(dbf,objid,j->first,
									node->getTag(j->second).c_str());
						}
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
				std::map<int,std::string> fields;
				std::set<std::string> wayTags = comp->getWayTags();
				for(std::set<std::string>::iterator i=wayTags.begin();
					i!=wayTags.end(); i++)
				{
					fields[DBFAddField(dbf,i->c_str(),FTString,255,0)] = *i;
				}

				comp->rewindWays();
				std::vector<double> wayCoords, longs, lats;

				while(comp->hasMoreWays())
				{
					OSM::Way *way = comp->nextWay();
					if(way)
					{
						wayCoords = comp->getWayCoords(way->id());
						if(wayCoords.size())
						{
							longs = getLongs(wayCoords);
							lats = getLats(wayCoords);

							SHPObject *object = SHPCreateSimpleObject
								(SHPT_ARC,wayCoords.size()/2,
									&(longs[0]),&(lats[0]),NULL);

							int objid = SHPWriteObject(shp, -1, object);

							SHPDestroyObject(object);

							for(std::map<int,std::string>::iterator j=
								fields.begin(); j!=fields.end(); j++)
							{
								DBFWriteStringAttribute
								(dbf,objid,j->first,
									way->getTag(j->second).c_str());
							}
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
	for(unsigned int count=0; count<wayCoords.size(); count+=2)
	{
		longs.push_back(wayCoords[count]);
	}
	return longs;
}

std::vector<double> getLats(const std::vector<double>& wayCoords)
{
	std::vector<double> lats;
	for(unsigned int count=1; count<wayCoords.size(); count+=2)
	{
		lats.push_back(wayCoords[count]);
	}
	return lats;
}

}
