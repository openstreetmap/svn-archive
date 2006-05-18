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
#include <qtextstream.h>
#include <vector>
#include <utility>
using std::vector;

namespace OpenStreetMap 
{


class Components2 
{
private:
	vector<Node*> nodes;
	vector<Segment*> segments;
	vector<Way*> ways;
	vector<TrackPoint*> trackpoints;
	vector<Node*>::iterator ni; 
	vector<Segment*>::iterator si;
	vector<Way*>::iterator wi;
	vector<TrackPoint*>::iterator ti;
	int nextNodeId, nextSegId;

	int minNodeID();
	int minSegID();

public:
	Components2() { nextNodeId = nextSegId = -1; }
	void destroy();

	Node *addNewNode(double lat, double lon, const QString& name, 
					const QString& type, const QString& timestamp="")
	{
		return addOSMNode(nextNodeId--,lat,lon,name,type,timestamp);
	}

	Node *addOSMNode(int id,double lat, double lon, const QString& name, 
					const QString& type,const QString& timestamp="");

	void addNode (Node *n)
	{
		nodes.push_back(n);
	}

	Node *getNearestNode (double lat, double lon,double);
	Segment *getNearestSegment (double lat, double lon,double);
	vector<Node*> getNearestNodes (double lat, double lon, double limit);

	Segment * addNewSegment (Node *n1, Node *n2, const QString& name,
								const QString& type)
	{
		return addOSMSegment(nextSegId--,n1,n2,name,type);
	}

	Segment * addOSMSegment (int id,Node *n1, Node *n2, const QString& name,
							const QString& type);


	Way *getWay(int id);
	Segment *getSeg(vector<Node*>& n1, vector<Node*>& n2);
	vector<Segment*> getSegs(Node*);

	void rewindNodes()
	{
		ni=nodes.begin();
	}

	void rewindSegments()
	{
		si=segments.begin();
	}

	void rewindWays()
	{
		wi=ways.begin();
	}

	void rewindTrackPoints()
	{
		ti=trackpoints.begin();
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

	Way *nextWay()
	{
		Way *w = *wi;
		wi++;
		return w;
	}

	TrackPoint *nextTrackPoint()
	{
		TrackPoint *t = *ti;
		ti++;
		return t;
	}

	bool endNode()
	{
		return ni==nodes.end();
	}

	bool endSegment()
	{
		return si==segments.end();
	}

	bool endWay()
	{
		return wi==ways.end();
	}

	bool endTrackPoint()
	{
		return ti==trackpoints.end();
	}

	bool merge(Components2 *comp);

	void toOSM(QTextStream &strm,bool);
	void removeTrackPoints();

	bool deleteNode(Node*);
	bool deleteSegment(Segment*);
	bool deleteWay(Way*);
	EarthPoint getAveragePoint(); 

	QByteArray getNewNodesXML();
	QByteArray getNewSegmentsXML();
	vector<Node*> getNewNodes();
	vector<Segment*> getNewSegments();

	// 130506 removed hacky crap for doing multiple nodes/segs at once - the
	// scheduler should now handle this (at least it has been so far....)
	
	void addWay (Way *w)
	{
		cerr<<"*****ADDING WAY TO COMPONENTS*****"<<endl;
		ways.push_back(w);
		cerr<<ways.size()<<endl;
	}

	std::pair<Segment*,Segment*>* breakSegment(Segment *s, Node *newNode);
	void toGPX(QTextStream& stream);
	vector<Node*> getWaypoints();

	TrackPoint *addTrackPoint(double lat, double lon,
				const QString& timestamp);
};


} 

#endif // FREEMAP_COMPONENT_H
