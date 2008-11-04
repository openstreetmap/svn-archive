#include "Way.h"
#include "Components.h"
#include <cfloat>
#include <iostream>

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

using std::endl;
using std::vector;

namespace OSM
{

// remove a node - returns its position
int Way::removeNode(int n)
{
	for(vector<int>::iterator i=nodes.begin(); i!=nodes.end(); i++)
	{
		if(*i==n)
		{
			int index = i-nodes.begin();
			nodes.erase(i);
			return index;
		}
	}
	return -1;
}

// Insert a node at position 'index'
bool Way::addNodeAt(int index, int n)
{
	vector<int>::iterator i = nodes.begin() + index;
	if(n>0)
	{
		nodes.insert(i,n);
		return true;
	}
	return false;
}

int Way::getNode(int i) const
{
	return (i>=0 && i<static_cast<int>(nodes.size())) ?  (nodes[i]) : -1;
}

void Way::toXML(std::ostream &strm)
{
	if (hasTags() || nodes.size()) {
		strm << "  <way id='" << id() << "'>" << endl;
		for(unsigned int count=0; count<nodes.size(); count++)
			strm  << "    <node id='" << nodes[count] << "'/>" << endl;
		tagsToXML(strm);
		strm << "  </way>" << endl;
	} else {
		strm << "  <way id='" << id() << "'/>" << endl;
	}

}

}
