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
#include "SRTMGeneral.h"


double min(double a,double b) { return (a<b) ? a:b; }
double max(double a,double b) { return (a>b) ? a:b; }
double between(double a, double b, double c) 
{ return a>=min(b,c) && a<=max(b,c); }


// Returns the slope angle of a contour line; 
// always in the range -90 -> 0 -> +90.
// 08/02/05 made more generalised by passing parameters as x1,x2,y1,y2
// rather than the line array.
double slope_angle(double x1,double y1,double x2,double y2)
{
	double dy = y2-y1;
	double dx = x2-x1;
	double a = dx ? atan(dy/dx) : M_PI/2;

	// minus to convert computer coord scheme (origin top left) to mathematical 
	// scheme (origin bottom left)
	cerr<<"-a="<<(-a*(180/M_PI))<<endl;
	return -a;  
}
