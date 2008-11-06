/*

Copyright (C) 2005 Nick Whitelegg, Hogweed Software, nick@hogweed.org

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111 USA

 */
// 180306 updated for 0.3
#include "Components.h"
#include <iostream>
#include <fstream>
#include <iomanip>
#include <string>
#include <sstream>
#include <shapefil.h>
#include "Node.h"
#include "Parser.h"
#include "Way.h"
#include <vector>
#include <fstream>
#include "llgr.h"
#include "FeatureClassification.h"
#include "FeaturesParser.h"

#include "ccoord/LatLng.h"
#include "ccoord/OSRef.h"

using std::endl;
using std::setw;
using std::setfill;
using std::setprecision;
using std::cerr;

using std::cout;

namespace OSM
{


Components::Components() :
	nodes(), ways(), nextNodeId(-1), nextSegmentId(-1), nextWayId(-1),
			destroyComponents(true)
{
	nodeIterator = nodes.begin();
	wayIterator = ways.begin();
}

Components::~Components()
{
	if (destroyComponents)
		destroy();
}

void Components::setDestroyComponents(bool b)
{
	destroyComponents = b;
}

void Components::destroy()
{
    for(std::map<int,Node*>::iterator i=nodes.begin(); i!=nodes.end(); i++)
        delete i->second;

    for(std::map<int,Way*>::iterator i=ways.begin(); i!=ways.end(); i++)
        delete i->second;
}

int Components::addNode(Node *n)
{
	int realID = n->id() ? n->id() : nextNodeId--;
	n->setId(realID);
	nodes[realID] = n;
	return realID;
}

int Components::addWay(Way *w)
{
	int realID = w->id() ? w->id() : nextWayId--;
	w->setId(realID);
	ways[realID] = w;
	return realID;
}

Node *Components::getNode(int i)
{
	return (nodes.find(i) != nodes.end()) ? nodes[i] : NULL;
}

Way *Components::getWay(int i)
{
	return (ways.find(i) != ways.end()) ? ways[i] : NULL;
}

Node *Components::nextNode()
{
	Node *n = (nodeIterator == nodes.end()) ? NULL : nodeIterator->second;
	nodeIterator++;
	return n;
}

Way *Components::nextWay()
{
	Way *w = (wayIterator == ways.end()) ? NULL : wayIterator->second;
	wayIterator++;
	return w;
}

void Components::rewindNodes()
{
	nodeIterator = nodes.begin();
}
void Components::rewindWays()
{
	wayIterator = ways.begin();
}
bool Components::hasMoreNodes() const
{
	return nodeIterator != nodes.end();
}
bool Components::hasMoreWays() const
{
	return wayIterator != ways.end();
}

// Return a vector of the coordinates of all the points making up a way,
// in lon-lat order.
// WILL ONLY WORK IF THE WAY IS STORED SENSIBLY, i.e. ALL SEGMENTS ALIGNED IN
// SAME DIRECTION AND IN LOGICAL ORDER !!!

std::vector<double> Components::getWayCoords(int id)
{
	std::vector<double> coords;
	Node *n1;
	Way *w = getWay(id);
	if(w)
	{
		for(int count=0; count<w->nNodes(); count++)
		{
			n1 = getNode(w->getNode(count));
			if(n1)
			{
					coords.push_back(n1->getLon());
					coords.push_back(n1->getLat());
			}
		}
	}
	return coords;
}

// 310107 adds null node IDs too
std::vector<int> Components::getWayNodes(int wayid)
{
	std::vector<int> ids;
	Way *w = getWay(wayid);
	Node *n = 0;
	if(w)
	{
		for(int count=0; count<w->nNodes(); count++)
		{
			n = getNode(w->getNode(count));
			if(n)// && getNode(s->firstNode()) && getNode(s->secondNode()))
			{
				ids.push_back(n->id());
			}
		}
	}
	return ids;
}

// get all way tags
// this could be used eg. to work out how many columns are needed in a shapefile
std::set<std::string> Components::getWayTags
	(FeatureClassification *classification, bool doArea)
{
	Way *w;
	std::set<std::string> tags;
	std::vector<std::string> curTags;

	rewindWays();
	while(hasMoreWays())
	{
		w = nextWay();
		if ( (classification==NULL) ||
			(doArea==true && classification->getFeatureClass(w)=="area") ||
			(doArea==false && classification->getFeatureClass(w)!="area") )
		{
			curTags = w->getTags();
			for(unsigned int count=0; count<curTags.size(); count++)
				tags.insert(curTags[count]);
		}
	}
	return tags;
}

std::set<std::string> Components::getNodeTags()
{
	Node *n;
	std::set<std::string> tags;
	std::vector<std::string> curTags;

	rewindNodes();
	while(hasMoreNodes())
	{
		n = nextNode();
		curTags = n->getTags();
		for(unsigned int count=0; count<curTags.size(); count++)
			tags.insert(curTags[count]);
	}
	return tags;
}


int Components::getParentWayOfNode(int nodeid)
{
	Node *n=getNode(nodeid);
	if(n)
	{
		rewindWays();
		while(hasMoreWays())
		{
			Way *w=nextWay();
			for(int count=0; count<w->nNodes(); count++)
			{
				if(w->getNode(count)==nodeid)
					return w->id();
			}
		}
	}
	return 0;
}

void Components::toXML(std::ostream &strm)
{
	strm << "<?xml version='1.0'?>"<<endl<<"<osm version='0.5'>" << endl;
	rewindNodes();
	while(hasMoreNodes())
	{
		Node *n=nextNode();
		n->toXML(strm);
	}
	rewindWays();
	while(hasMoreWays())
	{
		Way *w=nextWay();
		w->toXML(strm);
	}
	strm << "</osm>"<<endl;
}

void Components::toOSGB()
{
	rewindNodes();
	while(hasMoreNodes())
	{
		Node *n=nextNode();
		LatLng latLng (n->getLat(),n->getLon());
		latLng.toOSGB36();
		OSRef ref = latLng.toOSRef();
		n->setCoords(ref.getNorthing(),ref.getEasting());
	}
}


bool Components::makeShp(const std::string& nodes, const std::string& ways,
						 const std::string& areas,
						 const std::string &featuresFile)
{
	std::ifstream in(featuresFile.c_str());
	if(in.good())
	{
		FeatureClassification *featureClassification=FeaturesParser::parse(in);
		in.close();
		if (featureClassification && makeNodeShp(nodes))
		{
			if(makeWayShp(ways,featureClassification))
			{
				if(makeWayShp(areas,featureClassification,true))
				{
					return true;
				}
			}
		}
	}
	return false;
}

bool Components::makeNodeShp(const std::string& shpname)
{
		SHPHandle shp = SHPCreate(shpname.c_str(),SHPT_POINT);
		if(shp)
		{
			DBFHandle dbf = DBFCreate(shpname.c_str());
			if(dbf)
			{
				std::map<int,std::string> fields;
				std::set<std::string> nodeTags = getNodeTags();
				for(std::set<std::string>::iterator i=nodeTags.begin();
					i!=nodeTags.end(); i++)
				{
					fields[DBFAddField(dbf,i->c_str(),FTString,255,0)] = *i;
				}

				double lon, lat;

				rewindNodes();
				while(hasMoreNodes())
				{
					Node *node = nextNode();

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

bool Components::makeWayShp(const std::string &shpname,
				FeatureClassification *classification, bool doArea)
{
		int shpclass = (doArea==true) ? SHPT_POLYGON: SHPT_ARC;
		// ARC means polyline!
		SHPHandle shp = SHPCreate(shpname.c_str(),shpclass);
		if(shp)
		{
			DBFHandle dbf = DBFCreate(shpname.c_str());
			if(dbf)
			{
				std::map<int,std::string> fields;
				std::set<std::string> wayTags =
						getWayTags(classification,doArea);
				for(std::set<std::string>::iterator i=wayTags.begin();
					i!=wayTags.end(); i++)
				{
					fields[DBFAddField(dbf,i->c_str(),FTString,255,0)] = *i;
				}

				std::map<int,Way*>::iterator i=ways.begin();
				//rewindWays();
				std::vector<double> wayCoords, longs, lats;

				while(i!=ways.end())
				//while(hasMoreWays())
				{
					//Way *way = nextWay();
					Way *way= i->second;
					if(way)
					{
						wayCoords = getWayCoords(way->id());
						if(wayCoords.size()
	&& ((doArea==true && classification->getFeatureClass(way)=="area") ||
		(doArea==false && classification->getFeatureClass(way)!="area"))
						  )
						{
							longs.clear();
							lats.clear();
							for(unsigned int count=0; count<wayCoords.size();count+=2)
								longs.push_back(wayCoords[count]);
							for(unsigned int count=1; count<wayCoords.size(); count+=2)
								lats.push_back(wayCoords[count]);

							SHPObject *object = SHPCreateSimpleObject
								(shpclass,wayCoords.size()/2,
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
					i++;
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

}
