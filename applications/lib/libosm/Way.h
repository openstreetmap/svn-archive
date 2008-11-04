#ifndef WAY_H
#define WAY_H

/*
 Copyright (C) 2006 Nick Whitelegg, Hogweed Software, nick@hogweed.org

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

#include "Node.h"
#include "Object.h"

#include <vector>

namespace OSM
{

/**
 * A way is an ordered list of nodes
 */
class Way: public Object
{
public:
	/**
	 * Constructor Creates a new Way (ordered list of nodes)
	 * @param id The unique id to assign. Default is 0
	 */
	Way(int id = 0);

	/**
	 * Add another node to this way
	 * @param id The ID of the node to add
	 */
	void addNode(int id);

	/**
	 * Add a node to the way at the specified position if possible
	 * @param index Place to insert the node at
	 * @param n
	 * @return
	 */
	bool addNodeAt(unsigned int index, int n);

	/**
	 * Remove a node from this way
	 * @param id The ID of the node to remove
	 * @return The position of the node in the way
	 */
	int removeNode(int id);

	/**
	 * Accessor for the node at the given  index
	 * @param index
	 * @return
	 */
	int getNode(unsigned int index) const;

	/**
	 * Returns the number of nodes
	 * @return The number of nodes this way consists of
	 */
	int nNodes() const;

	/**
	 * Write an xml representation of this way and its elements
	 * to the given stream
	 * @param strm
	 */
	void toXML(std::ostream &strm);

private:
	/** Keeps node ids this way contains */
	std::vector<int> nodes;
};

}

#endif
