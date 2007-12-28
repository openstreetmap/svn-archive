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
#ifndef SRTMGENERAL_H
#define SRTMGENERAL_H

#include <iostream>
#include <vector>
#include <map>
#include <string>
using std::cout;
using std::cerr;
using std::endl;
using std::vector;
using std::string;

#include "Map.h"


// these are fairly crap but get the thing working first then refine...
double min(double a,double b);
double max(double a,double b); 
double between(double a, double b, double c);
double slope_angle(double x1,double y1,double x2,double y2);

vector<vector<EarthPoint> > readcoast (char* filename,
				EarthPoint bottomLeft, EarthPoint topRight) throw(string);

struct LINE
{
	Point p[2];
};

struct LATLON_TILE
{
	EarthPoint origin;
	int top, left, right, bottom;
};





class Colour
{
public:
	int r,g,b;
	Colour(){}
	Colour(int r,int g, int b)
		{ this->r=r; this->g=g; this->b=b; }
};

class DrawSurface
{
public:
	virtual void drawContour(int x1,int y1,int x2,int y2,
								int r,int g,int b) = 0;

	virtual void drawAngleText(int fontsize, double angle, 
						int x,int y,int r,int g,int b, char*) = 0;

	virtual void heightShading(int,int,int,int,int,int,int,int,int,int,int) = 0;
};


#endif
