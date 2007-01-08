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
// SAME DIRECTION AND IN LOGICAL ORDER !!! - the way it should be :-) 

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
		cerr<<"w exists"<<endl;
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
	cerr << "getNodeSegments: nodeid=" << nodeid<<endl;
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
				cerr<<"    adding segmneet: "<< s->id << endl;
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
	std::vector<int> orderednodes;

	Way *w = getWay(wayid);
	std::map<int,std::vector<int> > segsRecordForEachNode;

	std::set<int> nodes = getWayNodes(wayid);
	for(std::set<int>::iterator i=nodes.begin(); i!=nodes.end(); i++)
	{
		cerr << "setting up segs record: node=" << *i << endl;
		segsRecordForEachNode[*i] = std::vector<int>(); 
		std::vector<int> v = getNodeSegments(*i);
		for(int count=0; count<v.size(); count++)
		{
			cerr << "getParentWayOfSegment("<<v[count]<<")=" << 
					getParentWayOfSegment(v[count]) << endl;
			if(getParentWayOfSegment(v[count])==wayid)
			{
				cerr << "    " << v[count] << "belongs to this way, adding"
					<<endl;
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

		cerr << "found a node with one segment=" << id << endl;
		int idx = 0, segid;

		bool found;

		// Loop until we reach another unique node
		do
		{
			// Get the segment we're interested in
			segid = segsRecordForEachNode[id][idx];
			cerr << "Parent segment: " << segid << endl;
			Segment *s = getSegment(segid);
			found=false;

			// If it's a segment in the data set, work with it. If it's
			// a null segment we'll just abort this attempt
			if(s && getNode(s->firstNode()) && getNode(s->secondNode()))
			{
				// Add the id of the node to the list of ordered nodes
				orderednodes.push_back(id);

				// Find the other node in the current segment
				if(s->firstNode()==id)
					id=getNode(s->secondNode())->id;
				else
					id=getNode(s->firstNode())->id;


				cerr << "Found next node: id=" << id << endl;
			
				// If we arrive back at the first node again, stop
				// (circular way)

				if(id!=firstID)
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
							cerr << "Found another segment: idx="<<idx << endl;
							found=true;
							break;
						}
					}
				}

				if(!found)
					orderednodes.push_back(id);
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

}
