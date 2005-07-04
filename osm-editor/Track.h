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
#ifndef TRACK_H
#define TRACK_H

#include <vector>
#include <qstring.h>
#include <fstream>
#include "TrackSeg.h"
using std::vector;

#include<iostream>
using namespace std;

namespace OpenStreetMap 
{


class Track 
{
private:
	QString id;	
	vector<TrackSeg*> segs;
	void writeTrkpt(std::ostream&, int);
	vector<SegPointInfo> findNearestSeg(const EarthPoint& p, double limit);

public:
	Track() { id="noname"; }
	~Track();
	void newSegment() {segs.push_back(new TrackSeg); }
	Track(Track* t);
	void setID(const QString& i)
		{ id=i; }
	QString getID() 
		{ return id; }
	bool addTrackpt(int seg,const QString& t, double lat, double lon);
	void toGPX(std::ostream&);
	bool deletePoints(const EarthPoint& p1, const EarthPoint& p2, double limit);
	bool segmentise(const QString& newType, const EarthPoint& p1,
						const EarthPoint& p2, double limit);
	int nSegs() { return segs.size(); }
	TrackSeg *getSeg(int i) { return (i>=0 && i<segs.size()) ? segs[i]: NULL;  }
	bool setSegType(int i,const QString& t); 
	bool hasPoints();
	bool nameTrackSeg(const EarthPoint& p1, const QString& name, double);
	void deleteExcessPoints (double angle,  double distance);
	void removeSegs();
	void copySegsFrom(Track *);
};


}

#endif /* not TRACK_H */
