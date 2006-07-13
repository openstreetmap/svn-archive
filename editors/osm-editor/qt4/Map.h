/*
    Copyright (C) 2005 Nick Whitelegg, Hogweed Software, nick@hogweed.org 

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any yer version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    axg with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111 USA

 */
#ifndef MAP_H
#define MAP_H

#include "EarthPoint.h"
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
	EarthPoint bottomLeft;
	double scale;
	int width, height;
	bool gridref;

public:
	Map(double x,double y, double s, int w, int h)
		{ bottomLeft=EarthPoint(x,y); scale=s; width=w; height=h;
		  gridref=false; }

	ScreenPos getScreenPos(const EarthPoint& pos)
		{ return ScreenPos ((pos.x-bottomLeft.x)*scale,
						height-((pos.y-bottomLeft.y)*scale)); }

	ScreenPos getScreenPos(double x,double y)
		{ return getScreenPos(EarthPoint(x,y)); }

	EarthPoint getEarthPoint(const ScreenPos& pos)
		{ return getEarthPoint(pos.x,pos.y); }

	EarthPoint getEarthPoint(int x, int y)
		{ return EarthPoint( bottomLeft.x+(((double)x)/scale),
						bottomLeft.y+(((double)(height-y))/scale)); }

	void move(double edis,double ndis)
		{ bottomLeft.x += edis*1000; bottomLeft.y += ndis*1000; }

	void movePx(double xdis, double ydis)
		{ move(earthDist(xdis/1000), -earthDist(ydis/1000)); }

	EarthPoint getTopLeft() { return getEarthPoint(ScreenPos(0,0)); }
	EarthPoint getBottomRight() 
		{ return getEarthPoint(ScreenPos(width,height)); }
	EarthPoint getTopRight() { return getEarthPoint(ScreenPos(width,0)); }
	
	void rescale(double factor)
	{
		EarthPoint middle = getCentre (  );
		scale *= factor;
		bottomLeft.x = middle.x - (width/2)/scale;
		bottomLeft.y = middle.y - (height/2)/scale;
	}

	EarthPoint getCentre()
	{
		return getEarthPoint(ScreenPos(width/2,height/2));
	}

	EarthPoint getBottomLeft()
		{ return bottomLeft; }

	double getScale()
		{ return scale; }

	bool pt_within_map(const ScreenPos& pos)
		{ return pos.x>=0 && pos.y>=0 &&
			pos.x<width && pos.y<height; }
	
	bool pt_within_map(const EarthPoint& ep)
	{
		EarthPoint topRight=getTopRight();
		return ep.x>=bottomLeft.x && ep.y>=bottomLeft.y&&
			   ep.x<=topRight.x && ep.y<=topRight.y; 
	}

	int getWidth(){return width;}
	int getHeight(){return height;}

	double earthDist(double pixelDist)
		{ return pixelDist/scale; }

	void rescale(double factor,int w,int h)
	{
		EarthPoint middle = getCentre ( w,h );
		scale *= factor;
		centreAt (middle);
	}

	EarthPoint getCentre(int w,int h)
	{
		return getEarthPoint(ScreenPos(w/2,h/2));
	}

	void resize(int newWidth,int newHeight)
	{
		width = newWidth;
		height = newHeight;
	}

	void resizeTopLeft(int newWidth,int newHeight)
	{
		bottomLeft.y -= earthDist(newHeight-height); 
		resize(newWidth,newHeight);
	}

	void centreAt(const EarthPoint& centre)
	{
		bottomLeft.x = centre.x - (width/2)/scale;
		bottomLeft.y = centre.y - (height/2)/scale;
	}

	bool isGridRef() { return gridref; }
	void setGridRef(bool g) { gridref=g; }
};


#endif
