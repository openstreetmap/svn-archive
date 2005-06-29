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
#include "Canvas.h"
#include "functions.h"
#include "llgr.h"
#include <cstdlib>
#include <sstream>

using namespace OpenStreetMap;

Canvas::Canvas(double e, double n, double scale,
			int w,int h,int sr) : map (e,n,scale, w,h) 
{
	image=gdImageCreateTrueColor(w,h);
	backg=gdImageColorAllocate(image,220,220,220);
	gdImageFilledRectangle(image,0,0,w,h,backg);
	shadingres=sr;
	map.setGridRef(true);
}

Canvas::~Canvas()
{
	gdImageDestroy(image);
}


void Canvas::draw()
{
	SRTMConGen congen(map,1);
	if(shadingres>0)
		congen.generateShading(this,shadingres);
	else
		congen.generate(this);
	drawCoast();
	gdImagePng(image,stdout);

}

void Canvas::drawCoast()
{
	try
	{
		EarthPoint bottomLeft = gr_to_ll(map.getBottomLeft()),
				   topRight = gr_to_ll(map.getTopRight());

		vector<vector<EarthPoint> > coastSegs=readcoast("data/coast",
								bottomLeft, topRight);
		int blue = gdImageColorAllocate(image,0,0,255);
		ScreenPos pt1, pt2;

		for(int seg=0; seg<coastSegs.size(); seg++)
		{
			if(!coastSegs[seg].empty())
			{
				pt1 = map.getScreenPos(ll_to_gr(coastSegs[seg][0]));
				for(int pt=1; pt<coastSegs[seg].size(); pt++)
				{
					pt2 = map.getScreenPos(ll_to_gr(coastSegs[seg][pt]));
					gdImageLine(image,pt1.x,pt1.y,pt2.x,pt2.y,blue);
					pt1 = pt2;
				}
			}
		}
	}
	catch (string e)
	{
		cerr<<"WARNING - UNABLE TO DRAW COAST: " << e<<endl;
	}
}
	
void Canvas::drawContour(int x1,int y1,int x2,int y2,int r,int g,int b)
{
	int colour=gdImageColorAllocate(image,r,g,b);
	gdImageLine(image,x1,y1,x2,y2,colour);
	gdImageSetThickness(image,1);
}

void Canvas::drawAngleText(int fontsize,double angle,int x,int y,int r,int g,
							int b,char* text)
{
	int brect[8];
	int colour=gdImageColorAllocate(image,r,g,b);
	gdImageStringFT(image, brect,colour, "data/luxisr.ttf",fontsize, 
					angle, x, y, text);
}

void Canvas::heightShading(int x1,int y1,int x2,int y2,int x3,int y3,
							int x4,int y4,int r,int g,int b)
{
	gdPoint points[4];
	points[0].x=x1;
	points[0].y=y1;
	points[1].x=x2;
	points[1].y=y2;
	points[2].x=x3;
	points[2].y=y3;
	points[3].x=x4;
	points[3].y=y4;
	gdImageFilledPolygon(image, points, 4, gdImageColorAllocate (image,r,g,b));
}
