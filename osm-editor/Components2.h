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
#include <qtextstream.h>
#include <vector>
using std::vector;

namespace OpenStreetMap 
{


class Components2 
{
private:
	vector<Node*> nodes;
	vector<Segment*> segments;
	vector<Node*>::iterator ni; 
	vector<Segment*>::iterator si;
	int nextNodeId, nextSegId;

public:
	Components2() { nextNodeId = nextSegId = -1; }
	void destroy();

	Node *addNewNode(double lat, double lon, const QString& name, 
					const QString& type)
	{
		return addOSMNode(nextNodeId--,lat,lon,name,type);
	}

	Node *addOSMNode(int id,double lat, double lon, const QString& name, 
					const QString& type)
	{
		Node *newNode = new Node(id,lat,lon,name,type);
		nodes.push_back(newNode);
		return newNode;
	}

	Node *getNearestNode (double lat, double lon,double);
	vector<Node*> getNearestNodes (double lat, double lon, double limit);

	Segment * addNewSegment (Node *n1, Node *n2, const QString& name,
								const QString& type)
	{
		return addOSMSegment(nextSegId--,n1,n2,name,type);
	}

	Segment * addOSMSegment (int id,Node *n1, Node *n2, const QString& name,
							const QString& type)
	{
		n1->trackpointToNode();
		n2->trackpointToNode();
		Segment *seg = new Segment(id,n1,n2,name,type);
		segments.push_back(seg);
		return seg;
	}

	Segment *getSeg(vector<Node*>& n1, vector<Node*>& n2);

	void rewindNodes()
	{
		ni=nodes.begin();
	}

	void rewindSegments()
	{
		si=segments.begin();
	}

	Node *nextNode()
	{
		Node *n  = *ni;
		ni++;
		return n;
	}

	Segment *nextSegment()
	{
		Segment *s = *si;
		si++;
		return s;
	}

	bool endNode()
	{
		return ni==nodes.end();
	}

	bool endSegment()
	{
		return si==segments.end();
	}

	bool merge(Components2 *comp);
	void newUploadToOSM(const char* username,const char* password);

	void toOSM(QTextStream &strm,bool);
	void removeTrackPoints();
};


} 

#endif // FREEMAP_COMPONENT_H
