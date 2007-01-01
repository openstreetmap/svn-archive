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

}
