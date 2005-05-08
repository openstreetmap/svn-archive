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

#include <vector>
#include <utility>
#include <qstring.h>
#include <fstream>
#include <algorithm>
#include <map>

#include "functions.h"
#include "Track.h"
#include "Waypoint.h"
#include "Segment.h"
#include "Polygon.h"

#include <iostream>
using std::cout;
using std::endl;

using std::vector;
using std::istream;
using std::ostream;

namespace OpenStreetMap 
{


class Components 
{
private:
	Track * track;
	Waypoints * waypoints;
	vector<SegDef> segdefs;
	vector<Polygon*> polygons;

	vector<SegDef>::iterator findPlace(int);

public:
	Components() { waypoints=new Waypoints; track=new Track;}
	void clearAll();

	void toGPX(const char*);

	~Components() { clearAll(); }
	bool addWaypoint (const Waypoint&) ;

	bool addTrackpoint (const QString& timestamp, double lat, double lon);
	void addTrack(Track * t) { track=t; }

	void setWaypoints(Waypoints * w) { waypoints=w; }
	bool hasTrack() { return track && (track->size()>0); }
	bool hasWaypoints() { return waypoints && (waypoints->size()>0); }
 	Waypoint getWaypoint (int i) throw(QString);
	TrackPoint getTrackpoint(int i) throw(QString);
	int nWaypoints() { return waypoints ? waypoints->size(): 0; }
	int nTrackpoints() { return track ? track->size():0 ; }
	int nSegdefs() { return segdefs.size(); }
	bool setTrackID(const char*); 
	SegDef getSegdef(int i) { return segdefs[i]; }
	void addSegdef(int ,int , const QString& );
	void printSegDefs();

	bool deleteTrackpoints(int,int);

	bool alterWaypoint(int i,const QString& name,const QString& type)
		{ return (waypoints) ? waypoints->alterWaypoint(i,name,type): false; }

	void addPolygon(Polygon* p) { polygons.push_back(p); }
	int nPolygons() { return polygons.size(); }
	Polygon *getPolygon(int);
};


} 

#endif // FREEMAP_COMPONENT_H
