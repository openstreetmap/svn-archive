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
#include <string>

#include "functions.h"
#include "Track.h"
#include "Waypoint.h"
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
	Track * track, *clonedTrack, *activeTrack;
	Waypoints * waypoints;
	vector<Polygon*> polygons;


	void doToGPX(std::ostream &outfile);

public:
	Components() { waypoints=new Waypoints; track=new Track; clonedTrack=NULL;
					activeTrack = track;}
	void clearAll();

	void toGPX(const char*);
	std::string toGPX();

	~Components() { clearAll(); }
	bool addWaypoint (const Waypoint&) ;

	bool addTrackpoint (int seg,const QString& timestamp, double lat, 
					double lon);
	bool addTrackpoint (int,const TrackPoint&);
	void addTrack(Track * t) { if(track) delete track; 
								track=t; activeTrack = track; }

	void setWaypoints(Waypoints * w) { if(waypoints)delete waypoints;
			waypoints=w; }
	bool hasTrack() { return track && track->hasPoints() ; }
	bool hasWaypoints() { return waypoints && (waypoints->size()>0); }
 	Waypoint getWaypoint (int i) throw(QString);
	int nWaypoints() { return waypoints ? waypoints->size(): 0; }
	bool setTrackID(const char*); 
	void addSegdef(int ,int , const QString& );

	bool alterWaypoint(int i,const QString& name,const QString& type)
		{ return (waypoints) ? waypoints->alterWaypoint(i,name,type): false; }

	void addPolygon(Polygon* p) { polygons.push_back(p); }
	int nPolygons() { return polygons.size(); }
	Polygon *getPolygon(int);

	int nSegs() { return track->nSegs(); }
	TrackSeg *getSeg(int i) { return activeTrack->getSeg(i); }
	bool deleteTrackpoints(const RetrievedTrackPoint& p1, 
					const RetrievedTrackPoint& p2, 
					double limit)
		{ return track->deletePoints(p1,p2,limit);}
	bool segmentiseTrack(const QString& newType, const RetrievedTrackPoint& p1,
						const RetrievedTrackPoint& p2, double limit)
		{ return track->segmentise(newType,p1,p2,limit); }
	bool formNewSeg(const QString& newType, const RetrievedTrackPoint& p1,
						const RetrievedTrackPoint& p2, double limit)
		{ return track->formNewSeg(newType,p1,p2,limit); }
	bool linkNewPoint(const RetrievedTrackPoint& p1,
						const RetrievedTrackPoint& p2, 
						const RetrievedTrackPoint & p3,
						double limit)
		{ return track->linkNewPoint(p1,p2,p3,limit); }
	bool linkNewPoint(const RetrievedTrackPoint& p1,
						const RetrievedTrackPoint& p2, 
						const EarthPoint & ep,
						double limit)
		{ return track->linkNewPoint(p1,p2,ep,limit); }
	void newSegment() { track->newSegment(); }
	bool setSegType(int i,const QString& t) { return track->setSegType(i,t); }
	bool setSegId(int i,int id) { return track->setSegId(i,id); }
	bool setSegName(int i,const QString& t) { return track->setSegName(i,t); }
	bool deleteWaypoint(int index);
	void cloneTrack() { if(clonedTrack==NULL) clonedTrack=new Track(track); 
			else restoreClonedTrack(); }
	void deleteExcessTrackPoints (double angle, double distance)
		{ clonedTrack->deleteExcessPoints(angle,distance); }
	void updateTrack() { track->removeSegs(); track->copySegsFrom(clonedTrack);
							delete clonedTrack; clonedTrack=NULL;  }
	void restoreClonedTrack() { clonedTrack->removeSegs();
								clonedTrack->copySegsFrom(track); } 
	void setActiveNormal() { activeTrack=track; }
	void setActiveCloned() { activeTrack=clonedTrack; }
	bool isCloned() { return clonedTrack!=NULL; }
	TrackSeg* getNearestSeg(const EarthPoint& p, double limit)
		{ return track->findNearestSeg(p,limit); }
	RetrievedTrackPoint findNearestTrackpoint(const EarthPoint &p, 
					double limit)
		{ return track->findNearestTrackpoint(p,limit); }
	bool merge(Components *);
	void removeSegs(const QString& type)
		{ track->removeSegs(type); }
	EarthPoint getAveragePoint() throw (QString)
		{ return track->getAveragePoint(); }
	void uploadToOSM(char* username,char* password)
		{ track->uploadToOSM(username,password);
		  waypoints->uploadToOSM(username,password); }

};


} 

#endif // FREEMAP_COMPONENT_H
