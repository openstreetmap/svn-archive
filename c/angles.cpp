/*
    Copyright (C) 2005 Nick Whitelegg, Hogweed Software, nick@hogweed.org 

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111 USA

 */

#include <iostream>
#include <cmath>
#include <cfloat>
using namespace std;

#include "Map.h"

struct Point
{
	double lat, lon;
	Point(){}
	Point(double lat,double lon) { this->lat=lat; this->lon=lon; }
	static double getDistance(Point,Point);
};


double Point::getDistance(Point a, Point b)
{
	double dlat=b.lat-a.lat;
	double dlon=b.lon-a.lon;
	return sqrt(dlat*dlat+dlon*dlon);	
}
		
Point getIntersection (Point o, Point a, Point b, double wa, double wb);
double slope_angle(double x1,double y1,double x2,double y2);
void findNearestA1B1 (Point *a1s, Point *b1s, Point *a1, Point *b1);

// Imagine two roads are defined by OA and OB, so that O is the point at which 
// they intersect and // A and B are the next points along each road.
// Given the three points O, A, and B and the width of each road (wa and wb) 
// this function // calculates the point at which the road bounds turn. 
// (O' in the diagram below; asterisks are the road boundries)

//
//
//                 B'   B
//                 *
//                 **
//                  **
//                   *
//    A'****         *
//         ********  *
//    A            ***O' 
//                        O
//

Point getIntersection (Point o, Point a, Point b, double wa, double wb)
{
	// Get the distance between the 3 points
	double oa = Point::getDistance(o,a), ob = Point::getDistance(o,b), ab=Point::getDistance(a,b);

	// The angle of intersection of the two roads
	double angleO = acos ( (ab*ab - ( oa*oa + ob*ob )) / 2*oa*ob ); // cosine law

	// Imagining a triangle made up of O, A, B, get the angles at A and B
	double angleA = acos ( (ob*ob - ( oa*oa + ab*ab )) / 2*ab*oa ); // cosine law
	double angleB = M_PI-(angleO+angleA);

	// Get the bearing of the line OA. 
	double bearingOA = 	slope_angle(o.lon,o.lat,a.lon,a.lat);
	// Get the bearing of the line OB. 
	double bearingOB = 	slope_angle(o.lon,o.lat,b.lon,b.lat);

	// Work out A', the two possibilities, each side of the road
	Point a1s[2];
	a1s[0] = Point ( a.lat - wa*sin(bearingOA), a.lon + wa*cos(bearingOA) );
	a1s[1] = Point ( a.lat + wa*sin(bearingOA), a.lon - wa*cos(bearingOA) );


	// Work out B', the two possibilities, each side of the road
	Point b1s[2];
	b1s[0] = Point ( b.lat - wb*sin(bearingOB), b.lon + wb*cos(bearingOB) );
	b1s[1] = Point ( b.lat + wb*sin(bearingOB), b.lon - wb*cos(bearingOB) );

	// Find the nearest A', B' pair - we will work with these
	Point a1, b1;
	findNearestA1B1 (a1s, b1s, &a1, &b1);


	// Length of A'B'
	double a1b1 = Point::getDistance (a1,b1);

	// Get the length of O'A', using the sine law
	double o1a1 =  (a1b1*sin(angleB)) / sin(angleO);

	// Work out O' using the bearing of the line OA.
	double o1a1_dlon = o1a1*sin(bearingOA);
	double o1a1_dlat = o1a1*cos(bearingOA);

	return Point ( a1.lat-o1a1_dlat, a1.lon-o1a1_dlon );
}
	

// slope_angle()
// Calculates the slope of a given line
// Upward slopes are positive; downward slopes are negative.
double slope_angle(double x1,double y1,double x2,double y2)
{
	double dy = y2-y1;
	double dx = x2-x1;
	double a = dx ? atan(dy/dx) : M_PI/2;
	return a; 
}

void findNearestA1B1 (Point *a1s, Point *b1s, Point *a1, Point *b1)
{
	double lowestDist = DBL_MAX, dist;
	for (int a=0; a<2; a++)
	{
		for(int b=0; b<2; b++)
		{
			if((dist=Point::getDistance(a1s[a],b1s[b])) < lowestDist)
			{
				lowestDist = dist;
				a1->lat = a1s[a].lat;
				a1->lon = a1s[a].lon;
				b1->lat = b1s[b].lat;
				b1->lon = b1s[b].lon;
			}
		}
	}
}

// main is licenced under GPL. All other code comes under the LGPL.
int main(int argc,char* argv[])
{
	// Define our three points
	Point o(51,-1), a(51,-1.02), b(51.01,-1.01);

	// The number of pixels per lat/lon unit
	double scale = 4000;

	// Define our road thicknesses in lat/lon units
	double wa = 5/scale, wb = 2/scale;
	
	Point intersection=getIntersection(o,a,b,wa,wb);

	cout << "Intersection: " <<intersection.lat << " " << intersection.lon << endl;

	return 0;
}
