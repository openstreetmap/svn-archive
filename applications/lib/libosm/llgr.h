/*
 JEEPS functions (c) Alan Bleasby, see llgr.cpp, otherwise...

 Copyright (C) 2005 Nick Whitelegg, Hogweed Software, nick@hogweed.org

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111 USA

 */
#ifndef LLGR_H
#define LLGR_H

namespace OSM
{

class Point
{
public:
	double x, y;
};

class EarthPoint: public Point
{
public:
	EarthPoint()
	{
		x = y = 0;
	}
	EarthPoint(double x, double y)
	{
		this->x = x;
		this->y = y;
	}
};

EarthPoint wgs84_ll_to_gr(const EarthPoint& p);
EarthPoint gr_to_wgs84_ll(const EarthPoint& p);

}

#endif
