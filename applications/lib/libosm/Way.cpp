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

namespace OSM
{

// remove a segment - returns its position
int Way::removeSegment(int s)
{
	for(vector<int>::iterator i=segments.begin(); i!=segments.end(); i++)
	{
		if(*i==s)
		{
			int index = i-segments.begin();
			segments.erase(i);
			return index;
		}
	}
	return -1;
}

// Insert a segment at position 'index'
bool Way::addSegmentAt(int index, int s)
{
	vector<int>::iterator i = segments.begin() + index;
	if(s>0)
	{
		segments.insert(i,s);
		return true;
	}
	return false;
}

int Way::getSegment(int i)
{
	return (i>=0 && i<static_cast<int>(segments.size())) ?  (segments[i]) : -1;
}

void Way::toXML(std::ostream &strm)
{

	if (hasTags() || segments.size()) {
		strm << "  <way id='" << id << "'>" << endl;
		for(unsigned int count=0; count<segments.size(); count++)
			strm  << "    <seg id='" << segments[count] << "'/>" << endl;
		tagsToXML(strm);
		strm << "  </way>" << endl;
	} else {
		strm << "  <way id='" << id << "'/>" << endl;
	}

}

}
