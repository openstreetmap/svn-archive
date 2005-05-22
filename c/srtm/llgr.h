/*
    JEEPS functions (c) Alan Bleasby, see llgr.cpp, otherwise...

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
#ifndef LLGR_H
#define LLGR_H

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

GridRef ll_to_gr ( LatLon& ll );

void GPS_Math_LatLon_To_EN(double *E, double *N, double phi,
			   double lambda, double N0, double E0,
			   double phi0, double lambda0,
			   double F0, double a, double b);

LatLon gr_to_ll(const GridRef& gridref);

void GPS_Math_EN_To_LatLon(double E, double N, double *phi,
			   double *lambda, double N0, double E0,
			   double phi0, double lambda0,
			   double F0, double a, double b);

#endif
