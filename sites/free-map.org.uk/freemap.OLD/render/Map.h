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
#include <iostream>
using namespace std;

struct ScreenPos
{
    int x,y;

    ScreenPos() { x=y=0; }
    ScreenPos(int x1,int y1) { x=x1; y=y1; }
};


class Map
{
private:
    EarthPoint bottomLeft, topRight;
    double scale;

public:
    Map(double scale) { this->scale=scale; }

    void setBBOX(double w,double s,double e,double n)
    {
        bottomLeft=EarthPoint(w,s);
        topRight=EarthPoint(e,n);
    }

    void print() { 
     EarthPoint p = getTopRight();
     cout << "bottomLeft: "<< bottomLeft.x << ","<<bottomLeft.y<<
     " topright:" << p.x<<","<<p.y << endl;
    }

    ScreenPos getScreenPos(const EarthPoint& pos)
        { 
            return ScreenPos (
                            (pos.x-bottomLeft.x)*scale, 
                            (topRight.y-pos.y)*scale
                          ); 
        }

    ScreenPos getScreenPos(double x,double y)
        { return getScreenPos(EarthPoint(x,y)); }

    EarthPoint getEarthPoint(const ScreenPos& pos)
        { return getEarthPoint(pos.x,pos.y); }

    EarthPoint getEarthPoint(int x, int y)
        { return EarthPoint( bottomLeft.x+(((double)x)/scale),
                       topRight.y-(((double)y)/scale)); }

    EarthPoint getTopLeft() { return EarthPoint(bottomLeft.x,topRight.y); }
    EarthPoint getBottomRight() 
        { return EarthPoint(topRight.x,bottomLeft.y); }
    EarthPoint getTopRight() { return topRight; }
    
    void extend(double factor)
    {
		double dx = topRight.x-bottomLeft.x,
			   dy = topRight.y-bottomLeft.y;
        bottomLeft.x -= dx * factor;
        bottomLeft.y -= dy * factor;
        topRight.x += dx * factor;
        topRight.y += dy * factor;
    }
         

    EarthPoint getBottomLeft()
        { return bottomLeft; }

    double getScale()
        { return scale; }

    bool pt_within_map(const EarthPoint& ep)
    {
        return ep.x>=bottomLeft.x && ep.y>=bottomLeft.y&&
               ep.x<=topRight.x && ep.y<=topRight.y; 
    }

    int getWidth(){return (topRight.x-bottomLeft.x)*scale;}
    int getHeight(){return (topRight.y-bottomLeft.y)*scale;}

    double earthDist(double pixelDist)
        { return pixelDist/scale; }

    EarthPoint getCentre(int w,int h)
    {
        return getEarthPoint(ScreenPos(w/2,h/2));
    }
};


#endif
