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
using std::vector;


namespace OpenStreetMap 
{
	
struct SegDef;

struct TrackPoint
{
	time_t timestamp;
	double lat, lon;

	TrackPoint(){timestamp=0; lat=lon=0; }
	TrackPoint(time_t t, double lt, double ln)
		{ timestamp=t; lat=lt; lon=ln; }
};

class Track 
{
private:
	vector<TrackPoint> points;
	QString id;	
	
public:
	void setID(const QString& i)
		{ id=i; }
	QString getID() 
		{ return id; }
	void addTrackpt(time_t t, double lat, double lon)
		{ points.push_back(TrackPoint(t,lat,lon)); }
	TrackPoint getPoint(int i) { return points[i]; }
	void toGPX(std::ostream&,const vector<SegDef>&);
	int size(){ return points.size(); }
	bool deletePoints(int,int);
};


}

#endif
