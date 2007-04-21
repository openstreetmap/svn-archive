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
#ifndef CANVAS_H
#define CANVAS_H

#include "Map.h"
#include "SRTMConGen.h"
#include <gd.h>

using namespace std;


struct MapView
{
public:
	double scale;
	int defaultmode;
	double contourres;
	double shadingres;
	bool classified;
	bool unclassified;
	bool paths;
};

class Canvas : public OpenStreetMap::DrawSurface
{
private:
	gdImagePtr image;
	Map map;
	int backg;
	int shadingres;

public:
	Canvas(double,double,double,int=320,int=320,int=0);
	~Canvas();
	void draw();

	void drawContour(int,int,int,int,int,int,int);
	void drawAngleText(int,double,int,int,int,int,int,char*);
	void heightShading(int x1,int y1,int x2,int y2,int x3,int y3,
							int x4,int y4,int r,int g,int b);

	void drawCoast();

};

#endif
