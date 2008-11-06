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

class Components
{
public:
	Components();
	~Components();

	void setDestroyComponents(bool enabled);

	void destroy();

	int addNode(Node *n);

	int addWay(Way *w);

	Node *getNode(int i);

	Way *getWay(int i);

	Node *nextNode();

	Way *nextWay();

	void rewindNodes();

	void rewindWays();

	bool hasMoreNodes() const;

	bool hasMoreWays() const;

	std::vector<double> getWayCoords(int);

	std::vector<int> getWayNodes(int wayid);

	int getParentWayOfNode(int nodeid);

	std::set<std::string> getWayTags(FeatureClassification* classification =
			NULL, bool doArea = false);
	std::set<std::string> getNodeTags();

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
