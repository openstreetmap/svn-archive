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
#include "Way.h"
#include "FeatureClassification.h"

#include <map>
#include <set>
#include <utility>
#include <vector>

namespace OSM
{

/**
 * Core components of OSM -- nodes, ways and relations
 */
class Components
{
public:
	/** Constructor */
	Components();

	/** Destructor */
	~Components();

	/**
	 * Toggle destructor behaviour regarding freeing memory
	 * @param enabled If true, nodes and ways will be deleted in the destructor.
	 * Otherwise the destructor frees nothing.
	 */
	void setDestroyComponents(bool enabled);

	/**
	 * Free memory consumed by nodes and ways
	 */
	void destroy();

	/**
	 * Add a new node to this Components instance
	 * @param node The node to add
	 * @return Node id after adding it. If the node passed in has an id of 0,
	 * a negative ID will be assigned and returned. Otherwise the original
	 * ID of the given node will be returned.
	 */
	int addNode(Node *node);

	/**
	 * Add a new way to this Components instance
	 * @param way The way to add
	 * @return Way id after adding it. If the way passed in has an id of 0,
	 * a negative ID will be assigned and returned. Otherwise the original
	 * ID of the given way will be returned.
	 */
	int addWay(Way *w);

	/**
	 * Accessor for the node with the given ID
	 * @param id Node id to search for
	 * @return The node with the given ID if existing in this Components instance, NULL otherwise
	 */
	Node *getNode(int id) const;

	/**
	 * Accessor for the way with the given ID
	 * @param id Way id to search for
	 * @return The way with the given ID if existing in this Components instance, NULL otherwise
	 */
	Way *getWay(int id) const;

	/**
	 * Accessor for the next node
	 * @return The next (as per ID) node, or NULL if at the end
	 */
	Node *nextNode();

	/**
	 * Accessor for the next way
	 * @return The next (as per ID) way, or NULL if at the end
	 */
	Way *nextWay();

	/**
	 * Set the node iterator to the beginning such that #nextNode will
	 * return the first node, if any.
	 */
	void rewindNodes();

	/**
	 * Set the way iterator to the beginning such that #nextWay will
	 * return the first way, if any.
	 */
	void rewindWays();

	/**
	 * @return True if the node iterator is not at the end
	 * @see rewindNodes
	 * @see nextNode
	 */
	bool hasMoreNodes() const;

	/**
	 * @return True if the way iterator is not at the end
	 * @see rewindWays
	 * @see nextWay
	 */
	bool hasMoreWays() const;

	/**
	 * Return a vector of the coordinates of all the points making up a way,
	 * in lon-lat order.
	 * @param id Way id to search for
	 * @return An empty vector if no way with the given ID is part of this
	 * Components instance, or a vector of size 2*n for a way of n nodes
	 * containing the lon/lat coordinates of all way nodes
	 */
	std::vector<double> getWayCoords(int id) const;

	/**
	 * Return a vector of all node IDs for the given way
	 * @param id Way ID
	 * @return A vector of all node IDs for the given way. The vector will be empty
	 * if the given way does not exist in this Components instance or has no
	 * nodes assigned to it
	 */
	std::vector<int> getWayNodes(int id) const;

	/**
	 * Returns the ID of the first way found containing the given node key
	 * @param id ID of the node to search for
	 * @return The ID of the first way found containing a node with the given key,
	 * or 0 if no such way can be found
	 */
	int getParentWayOfNode(int id) const;

	std::set<std::string> getWayTags(FeatureClassification* classification =
			NULL, bool doArea = false) const;

	std::set<std::string> getNodeTags() const;

	void toXML(std::ostream &strm);

	void toOSGB();

	bool makeShp(const std::string& nodes, const std::string& ways,
			const std::string&, const std::string&);

	bool makeNodeShp(const std::string& shpname);

	bool makeWayShp(const std::string &shpname, FeatureClassification*, bool =
			false);

private:
	std::map<int, Node*> nodes;
	std::map<int, Way*> ways;
	int nextNodeId, nextSegmentId, nextWayId;
	bool destroyComponents;

	std::map<int, Node*>::iterator nodeIterator;
	std::map<int, Way*>::iterator wayIterator;
};

}

#endif // FREEMAP_COMPONENT_H
