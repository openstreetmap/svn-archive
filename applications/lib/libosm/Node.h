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

/**
 * A node represents a point with a unique ID and a known (latitude/longitude)
 */
class Node: public Object
{
public:
	/**
	 * Constructor. Creates a node at the given position with index 0.
	 * @param lat Latitude of the point (node)
	 * @param lon Longitude of the point (node)
	 */
	Node(double lat = 0, double lon = 0);

	/**
	 * Constructor. Creates a node at the given position with the given index.
	 * @param index Unique node index
	 * @param lat Latitude of the point (node)
	 * @param lon Longitude of the point (node)
	 */
	Node(int index, double lat, double lon);

	/**
	 * Nodes are considered equal if both their
	 * longitude and latitude position differs less than
	 * 0.000001 each.
	 * @param other Node to compare to
	 * @return True if the nodes are equal (lon/lat difference is smaller than 0.000001 for each)
	 */
	bool operator==(const Node& other);

	/**
	 * Accessor for the node latitude
	 * @return Latitude of this node
	 */
	double getLat();

	/**
	 * Accessor for the node longitude
	 * @return Longitude of this node
	 */
	double getLon();

	/**
	 * Set longitude and latitude
	 * @param lat New latitude of this node
	 * @param lon New longitude of this node
	 * @see #getLat, #getLon
	 */
	void setCoords(double lat, double lon);

	/**
	 * Write an xml representation of this node to the given stream
	 * @param strm Stream to write to
	 */
	void toXML(std::ostream &strm);

private:
	double lat, lon;
};

}
#endif
