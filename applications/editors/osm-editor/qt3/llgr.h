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
#include "EarthPoint.h"

namespace OpenStreetMap
{


EarthPoint ll_to_gr ( const EarthPoint& ll );
EarthPoint ll_to_gr ( double lat, double lon );

void modGPS_Math_LatLon_To_EN(double *E, double *N, double phi,
			   double lambda, double N0, double E0,
			   double phi0, double lambda0,
			   double F0, double a, double b);

EarthPoint gr_to_ll(const EarthPoint& gridref);

void modGPS_Math_EN_To_LatLon(double E, double N, double *phi,
			   double *lambda, double N0, double E0,
			   double phi0, double lambda0,
			   double F0, double a, double b);

}

#endif
