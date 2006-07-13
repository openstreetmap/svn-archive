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

#ifndef POLYGON_H
#define POLYGON_H

#include <vector>
#include <fstream>
#include "functions.h"
#include "Map.h"
using std::vector;

#include <qstring.h>

namespace OpenStreetMap
{

class Polygon
{
private:
	QString type;
	vector<EarthPoint> points;

public:
	Polygon(){}
	Polygon(const QString& t) { type=t; }
	void setType(const QString &t){type=t;}
	QString getType(){return type; }
	void addPoint(double lat,double lon)
		{ points.push_back(EarthPoint(lon,lat)); }
	void addPoint(EarthPoint ll)
		{ points.push_back(ll); }
	int size(){ return points.size(); }
	void toGPX(std::ostream&);
	EarthPoint getPoint(int i){ return points[i]; }
};

}

#endif
