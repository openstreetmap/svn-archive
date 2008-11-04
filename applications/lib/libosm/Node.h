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

#include "Object.h"

#include <cmath>
#include <fstream>
#include <iostream>
#include <map>
#include <string>
#include <vector>

namespace OSM
{

class Node: public Object
{
private:
	double lat, lon;

public:
	Node(double lt = 0, double ln = 0) :
		Object(0), lat(lt), lon(ln)
	{
	}

	Node(int i, double lt, double ln) :
		Object(i), lat(lt), lon(ln)
	{
	}

	bool operator==(const Node& tp)
	{
		return (fabs(lat - tp.lat) < 0.000001) && (fabs(lon - tp.lon)
				< 0.000001);
	}

	double getLat()
	{
		return lat;
	}
	double getLon()
	{
		return lon;
	}

	void setCoords(double lat, double lon)
	{
		this->lat = lat;
		this->lon = lon;
	}

	void toXML(std::ostream &strm)
	{
		std::streamsize old = strm.precision(15);
		if (hasTags())
		{
			strm << "  <node id='" << id() << "' lat='" << lat << "' lon='"
					<< lon << "'>" << std::endl;
			tagsToXML(strm);
			strm << "  </node>" << std::endl;
		}
		else
		{
			strm << "  <node id='" << id() << "' lat='" << lat << "' lon='"
					<< lon << "'/>" << std::endl;
		}
		strm.precision(old);
	}
};

}
#endif
