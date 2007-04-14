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
	vector<Way*> areas;
	vector<TrackPoint*> trackpoints;
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
		if(!nodeExists(n->getOSMID()))
			nodes.push_back(n);
	}

	void addSegment (Segment *s)
	{
		if(!segmentExists(s->getOSMID()))
			segments.push_back(s);
	}

	Node *getNearestNode (double lat, double lon,double);
	int getNearestTrackPoint (double lat, double lon,double);
	Segment *getNearestSegment (double lat, double lon,double);
	vector<Node*> getNearestNodes (double lat, double lon, double limit);

	Segment * addNewSegment (Node *n1, Node *n2) 
	{
		return addOSMSegment(nextSegId--,n1,n2);
	}

	Segment * addOSMSegment (int id,Node *n1, Node *n2);


	Way *getWayByID(int id);
	Segment *getSegmentByID(int id);
	Segment *getSeg(vector<Node*>& n1, vector<Node*>& n2);
	vector<Segment*> getSegs(Node*);

	bool merge(Components2 *comp);

	void toOSM(QTextStream &strm,bool);
	void removeTrackPoints();

	bool deleteNode(Node*);
	bool deleteSegment(Segment*);
	bool deleteWay(Way*);
	void deleteTrackPoints(int,int);
	EarthPoint getAveragePoint(); 
	EarthPoint getAverageTrackPoint(); 

	QByteArray getNewNodesXML();
	QByteArray getNewSegmentsXML();
	vector<Node*> getNewNodes();
	vector<Segment*> getNewSegments();

	// 130506 removed hacky crap for doing multiple nodes/segs at once - the
	// scheduler should now handle this (at least it has been so far....)
	
	void addWay (Way *w)
	{
		cerr<<"*****ADDING WAY TO COMPONENTS*****"<<endl;
		if(!wayExists(w->getOSMID()))
			ways.push_back(w);
		cerr<<ways.size()<<endl;
	}

	void addArea (Way *a)
	{
		if(!areaExists(a->getOSMID()))
			areas.push_back(a);
	}

	std::pair<Segment*,Segment*>* breakSegment(Segment *s, Node *newNode);
	void toGPX(QTextStream& stream);
	vector<Node*> getWaypoints();

	TrackPoint *addTrackPoint(double lat, double lon,
				const QString& timestamp);

	bool nodeExists(int);
	bool segmentExists(int);
	bool wayExists(int);
	bool areaExists(int);

	TrackPoint *getTrackPoint(int i) { return trackpoints[i]; }
	Node *getNode(int i) { return nodes[i]; }
	Segment *getSegment(int i) { return segments[i]; }
	Way *getWay(int i) { return ways[i]; }
	Way *getArea(int i) { return areas[i]; }

	int nNodes() { return nodes.size(); }
	int nSegments() { return segments.size(); }
	int nWays() { return ways.size(); }
	int nAreas() { return areas.size(); }
	int nTrackPoints() { return trackpoints.size(); }
};


} 

#endif // FREEMAP_COMPONENT_H
