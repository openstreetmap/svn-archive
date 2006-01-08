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

#include "Components2.h"
#include <iostream>
#include <iomanip>
#include <string>
#include <sstream>

#include "curlstuff.h"

using std::endl;
using std::setw;
using std::setfill;
using std::setprecision;
using std::cerr;

using std::cout;

namespace OpenStreetMap 
{


void Components2::destroy()
{
	for(vector<Node*>::iterator i=nodes.begin(); i!=nodes.end(); i++)
		delete *i;

	for(vector<Segment*>::iterator i=segments.begin(); i!=segments.end(); i++)
		delete *i;
}

Node *Components2::getNearestNode(double lat, double lon,double limit)
{
	double mindist=limit, dist;
	Node *nearest = NULL;
	for(int count=0; count<nodes.size(); count++)
	{
		if((dist=OpenStreetMap::dist(lat,lon,
					nodes[count]->getLat(),nodes[count]->getLon()))<limit)
		{
			if(dist<mindist)
			{
				mindist=dist;
				nearest = nodes[count];
			}
		}
	}
	return nearest;
}

vector<Node*> Components2::getNearestNodes(double lat, double lon,double limit)
{
	vector<Node*> nearestNodes;	
	double dist;
	for(int count=0; count<nodes.size(); count++)
	{
		if((dist=OpenStreetMap::dist(lat,lon,
					nodes[count]->getLat(),nodes[count]->getLon()))<limit)
		{
			nearestNodes.push_back(nodes[count]);
		}
	}
	return nearestNodes;
}

// find the segment which is common to two nodes
Segment* Components2::getSeg(vector<Node*> &n1, vector<Node*> &n2)
{
	bool found;
	for(int count=0; count<segments.size(); count++)
	{
		found = false;

		for(int count2=0; count2<n1.size(); count2++)
		{
			if (segments[count]->contains(n1[count2]))
			{
				found=true;
				break;
			}
		}

		if(found)
		{
			for(int count2=0; count2<n2.size(); count2++)
			{
				if (segments[count]->contains(n2[count2]))
					return segments[count];
			}
		}
	}
	return NULL;
}

// get a vector of segments containing a node
vector<Segment*> Components2::getSegs(Node * n)
{
	vector<Segment*> foundSegs;

	for(int count=0; count<segments.size(); count++)
	{
		if(segments[count]->contains(n))
			foundSegs.push_back(segments[count]);
	}
	return foundSegs;
}

// gets the new nodes
QByteArray Components2::getNewNodes()
{
	QByteArray xml;
	QTextStream stream (xml, IO_WriteOnly);
	stream<<"<osm version='0.2'>";
	for(int count=0; count<nodes.size(); count++)
	{
		if(nodes[count]->getOSMID()<0)
		{
			nodes[count]->toOSM(stream);
		}
	}
	stream<<"</osm>";
	return xml;
}

// gets the new segments 
QByteArray Components2::getNewSegments()
{
	QByteArray xml;
	QTextStream stream (xml, IO_WriteOnly);
	stream<<"<osm version='0.2'>";
	for(int count=0; count<segments.size(); count++)
	{
		if(segments[count]->getOSMID()<0)
		{
			segments[count]->segToOSM(stream);
		}
	}
	stream<<"</osm>";
	return xml;
}

// allocate new node IDs to new segments.
// This is called after the new nodes have been created in OSM when we do
// a batch upload of new nodes and segments.
// This is hacky in the extreme but is a stopgap measure as there will be
// a server routine to take a load of new nodes and segments and add them to
// the database.

void Components2::hackySetNodeIDs(QStringList& ids)
{
	// Allocate the returned IDs to the new nodes in the segment
	QStringList::Iterator i=ids.begin();
	for(int count=0; count<nodes.size(); count++)
	{
		if(nodes[count]->getOSMID()<0 && i!=ids.end() && 
				atoi((*i).ascii())) 
		{
			nodes[count]->setOSMID (atoi((*i).ascii()));
			i++;
		}
	}
}

// WARNING!! Very hacky!
// Assumes segment IDs will be 6-digit.
void Components2::hackySetSegIDs(const QString& segs)
{
	// Allocate the returned IDs to the new nodes in the segment
	int index = 0;
	for(int count=0; count<segments.size(); count++)
	{
		if(segments[count]->getOSMID()<0 && index<=segs.length()-6)
		{
			int id = atoi(segs.mid(index, 6).ascii());
			if(id>=100000&&id<=999999)
				segments[count]->setOSMID (id);
			index+=6;
		}
	}
}

// Merges these Components with another set
bool Components2::merge(Components2 *comp)
{
	int segIdx = minSegID(), nodeIdx = minNodeID();
	Node *curNode;
	Segment *curSeg;
	comp->rewindNodes();
	while(!comp->endNode())
	{
		curNode = comp->nextNode();
		if(curNode->getOSMID()<0)
			curNode->setOSMID(--nodeIdx);
		nodes.push_back(curNode);
	}
	comp->rewindSegments();
	while(!comp->endSegment())
	{
		curSeg = comp->nextSegment();
		if(curSeg->getOSMID()<0)
			curSeg->setOSMID(--segIdx);
		segments.push_back(curSeg);
	}

	nextNodeId = nodeIdx-1; 
	nextSegId = segIdx-1; 

	return true;
}

void Components2::toOSM(QTextStream &strm, bool allUid)
{
	strm << "<osm version='0.2'>";
	for(int count=0; count<nodes.size(); count++)
		nodes[count]->toOSM(strm,allUid);
	for(int count=0; count<segments.size(); count++)
		segments[count]->segToOSM(strm,allUid);
	strm << "</osm>";
}

void Components2::removeTrackPoints()
{
	for(vector<Node*>::iterator i=nodes.begin(); i!=nodes.end(); i++)
	{
		cerr<<(*i)->getType() << endl;
		if((*i)->getType()=="trackpoint")
		{
			delete *i;
			nodes.erase(i);
			i--;
		}
	}
}

// 'delete' a node
// note it doesn't actually erase it from memory - just removes it from
// the list
bool Components2::deleteNode(Node *n)
{
	for(vector<Node*>::iterator i=nodes.begin(); i!=nodes.end(); i++)
	{
		if((*i)==n)
		{
			nodes.erase(i);
			return true;
		}
	}
	return false;
}

// 'delete' a segment
// note it doesn't actually erase it from memory - just removes it from
// the list
bool Components2::deleteSegment(Segment *s)
{
	cerr<<"in deleteSegments()"<<endl;
	cerr<<"Segment: osmid=" << s->getOSMID() << endl;
	for(vector<Segment*>::iterator i=segments.begin(); i!=segments.end(); i++)
	{
		cerr<<"Trying segment: " << (*i)->getOSMID() << endl;
		if((*i)==s)
		{
			cerr<<"****FOUND THE SEGMENT****" << endl;
			segments.erase(i);
			return true;
		}
	}
	return false;
}

EarthPoint Components2::getAveragePoint() 
{
	EarthPoint avg;
	avg.x=avg.y=0.0;	

	for(int count=0; count<nodes.size(); count++)
	{
		avg.y += nodes[count]->getLat();
		avg.x += nodes[count]->getLon();
	}

	avg.y /= nodes.size();
	avg.x /= nodes.size();

	return avg;
}

Node *Components2::addOSMNode(int id,double lat, double lon,const QString& name,
			const QString& type)
{
	Node *newNode = new Node(id,lat,lon,name,type);
	nodes.push_back(newNode);
	if(id-1<nextNodeId)
		nextNodeId = id-1;
	return newNode;
}
Segment * Components2::addOSMSegment (int id,Node *n1, Node *n2, 
										const QString& name,const QString& type)
{
	n1->trackpointToNode();
	n2->trackpointToNode();
	Segment *seg = new Segment(id,n1,n2,name,type);
	segments.push_back(seg);
	if(id-1<nextSegId)
		nextSegId = id-1;
	return seg;
}

int Components2::minNodeID()
{
	int id=0;
	for(int count=0; count<nodes.size(); count++)
	{
		if(nodes[count]->getOSMID() < id)
			id = nodes[count]->getOSMID();
	}
	return id;
}

int Components2::minSegID()
{
	int id=0;
	for(int count=0; count<segments.size(); count++)
	{
		if(segments[count]->getOSMID() < id)
			id = segments[count]->getOSMID();
	}
	return id;
}

}
