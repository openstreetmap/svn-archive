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
#ifndef SEGMENT_H
#define SEGMENT_H

#include "Track.h"
#include <vector>
using std::vector;

namespace OpenStreetMap 
{

struct SegDef
{
	SegDef(){}
	SegDef(int s,int e,const QString& t) { start=s; end=e; type=t; }
	int start,
		end;
	QString	type;
};

class Segment : public vector<TrackPoint>
{
private:
	int id; 
	QString type;
	
public:
	Segment(int, Track*, const SegDef&);
};

}

#endif
