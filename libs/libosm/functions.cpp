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

// Note. Anything to do with JEEPS has been put in this file. Using JEEPS
// in C++ is problematic; if you try and call JEEPS from two C++ source files
// then link them into the same application, you get errors. This seems to
// be due to non-standard use of global variables in JEEPS.

#include <map>
#include <iostream>
#include <cmath>
#include <sstream>
#include <cctype>

#include <float.h>

#include "functions.h"

namespace OSM
{
// Distance function
double dist (double x1, double y1, double x2, double y2)
{
	double dx=x1-x2, dy=y1-y2;
	return sqrt (dx*dx + dy*dy);
}


// find the distance from a point to a line
// based on theory at: 
// astronomy.swin.edu.au/~pbourke/geometry/pointline/
// given equation was proven starting with dot product

double distp (double px,double py,double x1,double y1, double x2, double y2)
{
	double u = ((px-x1)*(x2-x1)+(py-y1)*(y2-y1)) / (pow(x2-x1,2)+pow(y2-y1,2));
	double xintersection = x1+u*(x2-x1), yintersection=y1+u*(y2-y1);
	return (u>=0&&u<=1) ? dist(px,py,xintersection,yintersection) : DBL_MAX;
}

// Angle function (cosine rule)
double getAngle(double a, double b, double c)
{
	double d = (b*b+c*c-a*a) / (2*b*c);
	return acos(d);
}

}
