#ifndef NODE_H
#define NODE_H

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


#include <vector>
#include <map>
#include <fstream>
#include <string>

#include <iostream>

#include <cmath>
#include "Object.h"

using std::ostream;
using std::endl;

namespace OSM
{

class Node : public Object
{
private:
	double lat, lon;

public:
	Node()
	{
		lat=lon=0; 
		id = 0; 
	}

	Node(double lt, double ln)
	{
		lat=lt; 
		lon=ln; 
		id=0; 
	}
	Node(int i,double lt, double ln)
	{
		lat=lt; 
		lon=ln; 
		id=i; 
	}

	bool operator==(const Node& tp)
	{ 
		return (fabs(lat-tp.lat)<0.000001) && (fabs(lon-tp.lon)<0.000001); 
	}

	double getLat() { return lat; }
	double getLon() { return lon; }

	void setCoords(double lat,double lon)
		{ this->lat=lat; this->lon=lon; }

	void toXML(std::ostream &strm)
	{
		strm << "<node id='" << id << "' lat='" << lat << "' lon='" << lon
			<<"'>" << endl;
		tagsToXML(strm);
		strm << "</node>" << endl;
	}
};

}
#endif
