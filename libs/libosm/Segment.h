#ifndef SEGMENT_H
#define SEGMENT_H

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

namespace OSM
{

class Segment : public Object
{
private:
	int nodes[2];

public:
	Segment()
	{
		nodes[0] = nodes[1] = 0; 
	}

	Segment(int from, int to)
	{
		nodes[0] = from;
		nodes[1] = to;
	}

	Segment(int id,int from, int to)
	{
		nodes[0] = from;
		nodes[1] = to;
		this->id = id;
	}

	bool contains(int nodeid)
	{
		return nodes[0]==nodeid || nodes[1]==nodeid;
	}

	int firstNode() 
	{
		return nodes[0];
	}

	int secondNode() 
	{
		return nodes[1];
	}

	bool hasNodes()
	{
		return nodes[0] && nodes[1];
	}
};

}

#endif
