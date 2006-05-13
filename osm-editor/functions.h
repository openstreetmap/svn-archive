/*
    Copyright (C) 1999 Alan Bleasby (gridref<->lat/long
	conversion functions), 2004 Nick Whitelegg, Hogweed Software
	(remainder of this file)

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

// 13/03/05 changed Location to GridRef

#ifndef FUNCTIONS_H
#define FUNCTIONS_H

#include <vector>
using std::vector;

#include <ctime>

#include "EarthPoint.h"

namespace OpenStreetMap 
{


void		wgsToAiry(double &lat, double &lon);
void		airyToWgs(double &lat, double &lon);

struct GridRef
{
	double e, n;

	GridRef() { e=n=-1; }
	bool isNullLoc() { return e==-1 && n==-1; }
	GridRef(double e1,double n1) { e=e1; n=n1; }
};

struct LatLon 
{
	double lat, lon;

	LatLon() { }
	LatLon(double lt,double ln) { lat=lt; lon=ln; }
};


const int SEG_FOOTPATH = 0, 
	  	  SEG_PERMISSIVE_FOOTPATH = 1,
	  	  SEG_CYCLEPATH = 4, 
		  SEG_BRIDLEWAY = 16,
		  SEG_PERMISSIVE_BRIDLEWAY = 17,
		  SEG_FWPBR = 18,
		  SEG_A_ROAD = 128,
		  SEG_B_ROAD = 129,
		  SEG_MINOR_ROAD = 130,
		  SEG_ESTATE_ROAD = 131,
		  SEG_BYWAY = 132;

enum { POLYGON_WOOD , POLYGON_LAKE , POLYGON_HEATH, POLYGON_URBAN,
		  POLYGON_ACCESS_AREA };

const int WAYPOINT_FARM = 10,
	  	  WAYPOINT_RESTAURANT = 11,
		  WAYPOINT_PUB = 13,
		  WAYPOINT_GENERIC = 18,
		  WAYPOINT_CAMPSITE = 151,
		  WAYPOINT_CAR_PARK = 158,
		  WAYPOINT_CAUTION = 166,
		  WAYPOINT_HAMLET = 8198,
		  WAYPOINT_VILLAGE = 8199,
		  WAYPOINT_SMALL_TOWN = 8200,
		  WAYPOINT_SUBURB = 8201,
		  WAYPOINT_MEDIUM_TOWN = 8202,
		  WAYPOINT_LARGE_TOWN = 8203,
		  WAYPOINT_BRIDGE = 8233,
		  WAYPOINT_CHURCH = 8236,
		  WAYPOINT_TUNNEL = 8243,
		  WAYPOINT_SUMMIT = 8246,
		  WAYPOINT_MAST = 16391,
		  WAYPOINT_LOCALITY = 24576,
		  WAYPOINT_AMENITY = 24577,
		  WAYPOINT_VIEWPOINT = 24578,
		  WAYPOINT_STATION = 24579,
	  	  WAYPOINT_POINT_OF_INTEREST = 24580,
		  WAYPOINT_TEASHOP = 24581;

EarthPoint ll_to_gr ( double lat, double lng );
EarthPoint ll_to_gr ( const EarthPoint& );
void GPS_Math_LatLon_To_EN(double *E, double *N, double phi,
			   double lambda, double N0, double E0,
			   double phi0, double lambda0,
			   double F0, double a, double b);
EarthPoint gr_to_ll(const EarthPoint& gridref);
void GPS_Math_EN_To_LatLon(double E, double N, double *phi,
			   double *lambda, double N0, double E0,
			   double phi0, double lambda0,
			   double F0, double a, double b);


double dist (double, double, double, double);
double getAngle(double o, double a, double b);
double distp (double px,double py,double x1,double y1, double x2, double y2);

void mkgpxtime (char *, time_t );

}
#endif // FUNCTIONS_H
