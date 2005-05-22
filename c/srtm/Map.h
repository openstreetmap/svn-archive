/*
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
#ifndef MAP_H
#define MAP_H

#include "llgr.h"

struct ScreenPos
{
	int x,y;

	ScreenPos() { x=y=0; }
	ScreenPos(int x1,int y1) { x=x1; y=y1; }
};

class Map
{
private:
	GridRef topLeft;
	double scale;
	int width, height;

public:
	Map(double e, double n, double s, int w, int h)
		{ topLeft=GridRef(e,n); scale=s; width=w; height=h; }

	ScreenPos getScreenPos(const GridRef& pos)
		{ return ScreenPos ((pos.e-topLeft.e)*scale,
						(topLeft.n-pos.n)*scale); }

	ScreenPos getScreenPos(double e,double n)
		{ return getScreenPos(GridRef(e,n)); }

	GridRef getGridRef(const ScreenPos& pos)
		{ return GridRef( topLeft.e+(((double)pos.x)/scale),
						topLeft.n-(((double)pos.y)/scale)); }

	void move(double edis,double ndis)
		{ topLeft.e += edis*1000; topLeft.n += ndis*1000; }

	GridRef getBottomLeft() { return getGridRef(ScreenPos(0,height)); }
	GridRef getBottomRight() { return getGridRef(ScreenPos(width,height)); }
	GridRef getTopRight() { return getGridRef(ScreenPos(width,0)); }
	
	LatLon getBottomLeftLL() { return gr_to_ll(getBottomLeft()); }
	LatLon getTopRightLL() { return gr_to_ll(getTopRight()); }

	void rescale(double factor)
	{
		GridRef middle = getCentre (  );
		scale *= factor;
		topLeft.e = middle.e - (width/2)/scale;
		topLeft.n = middle.n + (height/2)/scale;
	}

	GridRef getCentre()
	{
		return getGridRef(ScreenPos(width/2,height/2));
	}

	GridRef getTopLeft()
		{ return topLeft; }

	double getScale()
		{ return scale; }

	bool pt_within_map(const ScreenPos& pos)
		{ return pos.x>=0 && pos.y>=0 &&
			pos.x<width && pos.y<height; }

	ScreenPos getScreenPos(LatLon& ll) { return getScreenPos(ll_to_gr(ll)); }

};


#endif
