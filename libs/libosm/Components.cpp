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
#include <iomanip>
#include <string>
#include <sstream>
#include <libshp/shapefil.h>
#include "Node.h"
#include "Parser.h"
#include "Way.h"
#include <vector>
#include <fstream>

#include "llgr.h"

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

std::set<int> Components::getWayNodes(int wayid)
{
	std::set<int> ids; 
	Node *n1, *n2;
	Segment *s;
	Way *w = getWay(wayid);
	if(w)
	{
		for(int count=0; count<w->nSegments(); count++)
		{
			s = getSegment(w->getSegment(count));
			if(s && getNode(s->firstNode()) && getNode(s->secondNode()))
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
std::set<std::string> Components::getWayTags()
{
	Way *w;
	std::set<std::string> tags;
	std::vector<std::string> curTags;

	rewindWays();
	while(hasMoreWays())
	{
		w = nextWay();
		curTags = w->getTags();
		for(int count=0; count<curTags.size(); count++)
			tags.insert(curTags[count]);
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
		for(int count=0; count<curTags.size(); count++)
			tags.insert(curTags[count]);
	}
	return tags;
}


std::vector<int> Components::getNodeSegments(int nodeid)
{
	Node *n = getNode(nodeid);
	std::vector<int> segments;

	if(n)
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

	Way *w = getWay(wayid);
	std::map<int,std::vector<int> > segsRecordForEachNode;

	std::set<int> nodes = getWayNodes(wayid);
	for(std::set<int>::iterator i=nodes.begin(); i!=nodes.end(); i++)
	{
		//cerr << "setting up segs record: node=" << *i << endl;
		segsRecordForEachNode[*i] = std::vector<int>(); 
		std::vector<int> v = getNodeSegments(*i);
		for(int count=0; count<v.size(); count++)
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


	// if there was a unique node (there should be!)
	if(id!=0)
	{
		int firstID = id;

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
			if(s && getNode(s->firstNode()) && getNode(s->secondNode()))
			{
				// Add the id of the node to the list of ordered nodes
				cerr << "Adding the ID: " << id << endl;
				orderednodes.push_back(id);

				// Find the other node in the current segment
				if(s->firstNode()==id)
					id=getNode(s->secondNode())->id;
				else
					id=getNode(s->firstNode())->id;


				//cerr << "Found next node: id=" << id << endl;
			
				// If we arrive back at any previous node again, stop
				// (way containing loop)
				bool loop=false;
				for(int z=0; z<orderednodes.size(); z++)
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

					for(int count=0; count<segsRecordForEachNode[id].size(); 
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
		EarthPoint ep (n->getLon(),n->getLat());
		EarthPoint ep2 = wgs84_ll_to_gr(ep);
		n->setCoords(ep2.y,ep2.x);
	}
}


bool Components::makeShp(const std::string& nodes, const std::string& ways)
{
		if (makeNodeShp(nodes))
		{
			if(makeWayShp(ways))
			{
				return true;
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

bool Components::makeWayShp(const std::string &shpname)
{
		// ARC means polyline!
		SHPHandle shp = SHPCreate(shpname.c_str(),SHPT_ARC); 
		if(shp)
		{
			DBFHandle dbf = DBFCreate(shpname.c_str());
			if(dbf)
			{
				std::map<int,std::string> fields;
				std::set<std::string> wayTags = getWayTags();
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
						if(wayCoords.size())
						{
							longs.clear();
							lats.clear();
							for(int count=0; count<wayCoords.size();count+=2)
								longs.push_back(wayCoords[count]);
							for(int count=1; count<wayCoords.size(); count+=2)
								lats.push_back(wayCoords[count]);

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
				for(int i=0; i<nodes.size()-1; i++)
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
