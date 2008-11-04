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


void Components::destroy()
{
    for(std::map<int,Node*>::iterator i=nodes.begin(); i!=nodes.end(); i++)
        delete i->second;

    for(std::map<int,Segment*>::iterator i=segments.begin();
			i!=segments.end(); i++)
        delete i->second;

    for(std::map<int,Way*>::iterator i=ways.begin(); i!=ways.end(); i++)
        delete i->second;
}

// Return a vector of the coordinates of all the points making up a way,
// in lon-lat order.
// WILL ONLY WORK IF THE WAY IS STORED SENSIBLY, i.e. ALL SEGMENTS ALIGNED IN
// SAME DIRECTION AND IN LOGICAL ORDER !!!

std::vector<double> Components::getWayCoords(int id)
{
	std::vector<double> coords;
	Node *n1, *n2;
	Segment *s;
	Way *w = getWay(id);
	if(w)
	{
		for(int count=0; count<w->nSegments(); count++)
		{
			s = getSegment(w->getSegment(count));
			if(s)
			{

				// Add both nodes of first segment
				if(coords.empty())
				{
					n1=getNode(s->firstNode());
					if(n1)
					{
						coords.push_back(n1->getLon());
						coords.push_back(n1->getLat());
					}
				}

				// For all other segments, only add the second node
				n2 = getNode(s->secondNode());
				if(n2)
				{
					coords.push_back(n2->getLon());
					coords.push_back(n2->getLat());
				}
			}
		}
	}
	return coords;
}

// 310107 adds null node IDs too
std::set<int> Components::getWayNodes(int wayid)
{
	std::set<int> ids;
	Segment *s;
	Way *w = getWay(wayid);
	if(w)
	{
		for(int count=0; count<w->nSegments(); count++)
		{
			s = getSegment(w->getSegment(count));
			if(s)// && getNode(s->firstNode()) && getNode(s->secondNode()))
			{
				ids.insert(s->firstNode());
				ids.insert(s->secondNode());
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


std::vector<int> Components::getNodeSegments(int nodeid)
{
	//Node *n = getNode(nodeid);
	std::vector<int> segments;

	//if(n)
	if(1)
	{
		rewindSegments();
		while(hasMoreSegments())
		{
			Segment *s = nextSegment();
			if(s->firstNode()==nodeid || s->secondNode()==nodeid)
			{
				segments.push_back(s->id);
			}
		}
	}
	return segments;
}

int Components::getParentWayOfSegment(int segid)
{
	Segment *s=getSegment(segid);
	if(s)
	{
		rewindWays();
		while(hasMoreWays())
		{
			Way *w=nextWay();
			for(int count=0; count<w->nSegments(); count++)
			{
				if(w->getSegment(count)==segid)
					return w->id;
			}
		}
	}
	return 0;
}

// orders a way.  returns the nodes in order.

std::vector<int> Components::orderWay(int wayid)
{
	cerr << "orderWay()"<<endl;
	std::vector<int> orderednodes;

	getWay(wayid);
	std::map<int,std::vector<int> > segsRecordForEachNode;

	std::set<int> nodes = getWayNodes(wayid);
	for(std::set<int>::iterator i=nodes.begin(); i!=nodes.end(); i++)
	{
		//cerr << "setting up segs record: node=" << *i << endl;
		segsRecordForEachNode[*i] = std::vector<int>();
		std::vector<int> v = getNodeSegments(*i);
		for(unsigned int count=0; count<v.size(); count++)
		{
			if(getParentWayOfSegment(v[count])==wayid)
			{
				segsRecordForEachNode[*i].push_back(v[count]);
			}
		}
	}

	// find the unique node id in the segsRecordForEachNode
	int id=0;
	for(std::map<int,vector<int> >::iterator i=segsRecordForEachNode.begin();
			i!=segsRecordForEachNode.end(); i++)
	{
		if (i->second.size()==1)
		{
			id=i->first;
			break;
		}
	}


	// if no unique node could be found but there are nodes in this way, it's
	// a circular way so just take the first node that's not NULL
	if(id==0)
	{
		int count=0;
		std::set<int>::iterator i = nodes.begin();
		while(id==0 && i!=nodes.end())
		{
			if(getNode(*i))
				id=*i;
			count++;
		}
	}

	// if there is now a valid node (there should be!)
	if(id!=0)
	{
		//cerr << "found a node with one segment=" << id << endl;
		int idx = 0, segid;

		bool found;

		// Loop until we reach another unique node
		do
		{
			// Get the segment we're interested in
			segid = segsRecordForEachNode[id][idx];
			//cerr << "Parent segment: " << segid << endl;
			Segment *s = getSegment(segid);
			found=false;

			// If it's a segment in the data set, work with it. If it's
			// a null segment we'll just abort this attempt
			// 310107 do not quit on null nodes.
			if(s) // 310107&&getNode(s->firstNode())&&getNode(s->secondNode()))
			{
				// Add the id of the node to the list of ordered nodes
				cerr << "Adding the ID: " << id << endl;

				// 310107 only add a non null node
				if(getNode(id))
					orderednodes.push_back(id);

				// Find the other node in the current segment
				if(s->firstNode()==id)
					//id=getNode(s->secondNode())->id;
					id=s->secondNode();
				else
					//id=getNode(s->firstNode())->id;
					id = s->firstNode();


				//cerr << "Found next node: id=" << id << endl;

				// If we arrive back at any previous node again, stop
				// (way containing loop)
				bool loop=false;
				for(unsigned int z=0; z<orderednodes.size(); z++)
				{
					if(orderednodes[z]==id)
					{
						loop=true;
						break;
					}
				}

				if(loop)
					cerr<<"   Stopping way as reached a previous id"<<endl;

				if(!loop)
				{
					// Find another segment of the new node
					// If there isn't one, found will be false, so we quit

					for(unsigned int count=0; count<segsRecordForEachNode[id].size();
						count++)
					{
						if(segsRecordForEachNode[id][count] != segid)
						{
							// Save the index so we can identify the segment
							// next go
							idx=count;
							found=true;
							break;
						}
					}
				}

				if(!found)
				{
					cerr << "Adding the ID: " << id << endl;
					if(getNode(id))
						orderednodes.push_back(id);
				}
			}
		}while(found);



	}
	return orderednodes;
}

void Components::toXML(std::ostream &strm)
{
	strm << "<?xml version='1.0'?>"<<endl<<"<osm version='0.3'>" << endl;
	rewindNodes();
	while(hasMoreNodes())
	{
		Node *n=nextNode();
		n->toXML(strm);
	}
	rewindSegments();
	while(hasMoreSegments())
	{
		Segment *s=nextSegment();
		s->toXML(strm);
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
						wayCoords = getWayCoords(way->id);
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

Components * Components::cleanWays()
{
	Components *compOut = new Components;

	std::map<int,Way*>::iterator i = ways.begin();
	//rewindWays();
	while(i!=ways.end())
	//while(hasMoreWays())
	{
		//OSM::Way *w = nextWay();
		OSM::Way *w = i->second;
		if(w)
		{
			cerr<<"Calling orderWay on way ID " << i->first << " or "  <<
					w->id << endl;
			std::vector<int> nodes = orderWay(w->id);

			if(nodes.size())
			{
				OSM::Way *way = new OSM::Way;
				way->tags = w->tags;
				compOut->addWay(way);
				for(unsigned int i=0; i<nodes.size()-1; i++)
				{
					int segid=compOut->addSegment
						(new OSM::Segment(nodes[i],nodes[i+1]));
					way->addSegment(segid);
				}
			}
		}
		i++;
	}

	rewindNodes();
	while(hasMoreNodes())
	{
		OSM::Node *n = new OSM::Node(*(nextNode()));
		compOut->addNode(n);
	}
	return compOut;
}

}
