/*
    Copyright (C) 2005 Nick Whitelegg, Hogweed Software, nick@hogweed.org 

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111 USA

 */
#ifndef FREEMAP_COMPONENT_H
#define FREEMAP_COMPONENT_H

#include "Node.h"
#include "Segment.h"
#include "Way.h"
#include <vector>
#include <utility>
#include <map>
#include <set>

using std::vector;

namespace OSM
{


class Components
{
private:
	std::map<int,Node*> nodes;
	std::map<int,Segment*> segments;
	std::map<int,Way*> ways;
	int nextNodeId, nextSegmentId, nextWayId;
	bool destroyComponents;

	std::map<int,Node*>::iterator nodeIterator;
	std::map<int,Segment*>::iterator segmentIterator;
	std::map<int,Way*>::iterator wayIterator;

public:
	Components() { nextNodeId = nextSegmentId = nextWayId = -1; 
						destroyComponents=true; nodeIterator=nodes.begin();
						segmentIterator=segments.begin();
						wayIterator=ways.begin(); }
	~Components() { if(destroyComponents) destroy(); }
	void setDestroyComponents(bool b) { destroyComponents = b; }
	void destroy();

	void addNode (Node *n)
	{
		int realID = (n->isFromOSM()) ? n->id : nextNodeId--;
		n->id=realID;
		nodes[realID] = n;
	}

	void addSegment (Segment *s)
	{
		int realID = (s->isFromOSM()) ? s->id : nextSegmentId--;
		s->id=realID;
		segments[realID] = s;
	}

	void addWay (Way *w)
	{
		int realID = (w->isFromOSM()) ? w->id : nextWayId--;
		w->id=realID;
		ways[realID] = w;
	}

	Node *getNode(int i) { return (nodes.find(i) != nodes.end())?nodes[i]:NULL;}
	Segment *getSegment(int i) { return (segments.find(i)!=segments.end())?
								segments[i] : NULL; }
	Way *getWay(int i) { return (ways.find(i) != ways.end())?ways[i]:NULL;}

	Node *nextNode()
	{
		Node *n =  (nodeIterator==nodes.end()) ? NULL: nodeIterator->second;
		nodeIterator++;
		return n;
	}

	Segment *nextSegment()
	{
		Segment *s =  (segmentIterator==segments.end()) ? NULL: 
			segmentIterator->second;
		segmentIterator++;
		return s;
	}

	Way *nextWay()
	{
		Way *w =  (wayIterator==ways.end()) ? NULL: 
			wayIterator->second;
		wayIterator++;
		return w;
	}

	void rewindNodes() { nodeIterator=nodes.begin(); }
	void rewindSegments() { segmentIterator=segments.begin(); }
	void rewindWays() { wayIterator=ways.begin(); }
	bool hasMoreNodes() { return nodeIterator!=nodes.end(); }
	bool hasMoreSegments() { return segmentIterator!=segments.end(); }
	bool hasMoreWays() { return wayIterator!=ways.end(); }

	std::vector<double> getWayCoords(int);

	std::set<std::string> getWayTags();
	std::set<std::string> getNodeTags();
};


} 

#endif // FREEMAP_COMPONENT_H
