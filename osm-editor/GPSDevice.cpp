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

#include "GPSDevice.h"
#include "gps.h"

#include "Segment.h"
#include "functions.h"

#include <map>
#include <iostream>
#include <cmath>
#include <sstream>
#include <cctype>
namespace OpenStreetMap 
{


GPSDevice::GPSDevice(const QString& mdl, const char* p)
{ 
	std::map<QString,int(*) (const char*, Track*)> trackFuncs;
	trackFuncs["Garmin"] = GPSDevice::garminGetTrack;
	// Other models can be added here as needed

	std::map<QString,int(*) (const char*, Waypoints*)> waypointFuncs;
	waypointFuncs["Garmin"] = GPSDevice::garminGetWaypoints;
	// Other models can be added here as needed
	
	model=mdl; 
	trackFunc = trackFuncs[mdl];
  	waypointFunc = waypointFuncs[mdl];

	strcpy(port,p); 
}

Track* GPSDevice::getTrack()
{
	Track *t = new Track;
	std::cerr <<"calling trackFunc" << std::endl;
	int code=trackFunc(port, t);
	if(code)
	{
		delete t;
		return NULL;
	}

	return t;
}


Waypoints* GPSDevice::getWaypoints()
{
	Waypoints *w = new Waypoints;	
	int code = waypointFunc(port, w);
    if(code)
	{
		delete w;
		return NULL;
	}
	return w;	
}


// Get track from a Garmin using jeeps.
int GPSDevice::garminGetTrack(const char* port,Track* track)
{
	std::cerr << "garminGetTrack() " << std::endl;
	std::cerr << "port" << port << std::endl;

	int32_t ntrackpts;
	GPS_PTrack *trackpts;

	char gpx_timestamp[1024];

	std::cerr<< "calling GPS_Init"<<std::endl;
	if(GPS_Init(port) < 0)
	{
		std::cerr<<"error" << std::endl;
		return 1;
	}

	std::cerr << "init was successful" << std::endl;	
	ntrackpts = GPS_Command_Get_Track(port,&trackpts);

	// NB the first track point from a Garmin appears to contain nonsense 
	// information, so trash it. It's only the *first track point*. Switching
	// the GPS off and on again, or losing the signal, doesn't have the 
	// same effect.
	for(int count=1; count<ntrackpts; count++)
	{
		if(!count)
			track->setID(trackpts[count]->trk_ident);

		// 10/04/05 now timestamps are stored in trackpoints in GPX format
		mkgpxtime(gpx_timestamp, trackpts[count]->Time);
		track->addTrackpt(gpx_timestamp,
						  trackpts[count]->lat,
						  trackpts[count]->lon);

		GPS_Track_Del(&trackpts[count]);

	}

	free(trackpts);
	return 0;
}	

int GPSDevice::garminGetWaypoints(const char* port, Waypoints* waypoints)
{
	GPS_PWay *waypts;
	int nwaypoints;

	if(GPS_Init(port) < 0)
		return 1;

	nwaypoints = GPS_Command_Get_Waypoint(port,&waypts);

	for(int count=0; count<nwaypoints; count++)
	{
		waypoints->addWaypoint(Waypoint(
				waypts[count]->ident,
				waypts[count]->lat,
				waypts[count]->lon,
				Waypoint::garminToType(waypts[count]->smbl))
							);
		GPS_Way_Del(&waypts[count]);
	}
	free(waypts);
	return 0;
}

// NOTICE OF AUTHORSHIP: These functions are taken from the LGPL JEEPS library,
// author and licence info follows....


/********************************************************************
** @source JEEPS arithmetic/conversion functions
**
** @author Copyright (C) 1999 Alan Bleasby
** @version 1.0 
** @modified Dec 28 1999 Alan Bleasby. First version
** @@
** 
** This library is free software; you can redistribute it and/or
** modify it under the terms of the GNU Library General Public
** License as published by the Free Software Foundation; either
** version 2 of the License, or (at your option) any later version.
** 
** This library is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
** Library General Public License for more details.
** 
** You should have received a copy of the GNU Library General Public
** License along with this library; if not, write to the
** Free Software Foundation, Inc., 59 Temple Place - Suite 330,
** Boston, MA  02111-1307, USA.
********************************************************************/



// ll_to_gr()
// Slightly modified version of:
/* @func GPS_Math_Airy1830LatLonToNGEN **************************************
**
** Convert Airy 1830 datum latitude and longitude to UK Ordnance Survey
** National Grid Eastings and Northings
**
** @param [r] phi [double] WGS84 latitude     (deg)
** @param [r] lambda [double] WGS84 longitude (deg)
** @param [w] E [double *] NG easting (metres)
** @param [w] N [double *] NG northing (metres)
**
** @return [void]
*/

// Modifications:
// - Returns a GridRef structure.
// - Parameters renamed from "phi" and "lambda" to "lat" and "lng".

GridRef ll_to_gr ( double lat, double lng )
{
/*
    double N0      = -100000;
    double E0      =  400000;
    double F0      = 0.9996012717;
    double phi0    = 49.;
    double lambda0 = -2.;
    double a       = 6377563.396;
    double b       = 6356256.910;
	double e, n;
	double alat,alng,aht;

	// 30/04/05 transform WGS84 lat/lon to Airy lat/lon
//	::GPS_Math_WGS84_To_Known_Datum_M(lat,lng,30,&alat,&alng,&aht,86);

	alat=lat; alng=lng;

    ::GPS_Math_LatLon_To_EN(&e,&n,alat,alng,N0,E0,phi0,lambda0,F0,
							a,b);

    return GridRef(e,n);
	*/

// !!! everything now in lats and longs !!!
	return GridRef(lng,lat);
}

void		wgsToAiry(double &lat, double &lon)
{
	double lat1=lat,lon1=lon,lat2,lon2;
	double aht;
	::GPS_Math_WGS84_To_Known_Datum_M(lat,lon,30,&lat2,&lon2,&aht,86);
	lat=lat2;
	lon=lon2;
}

void		airyToWgs(double &lat, double &lon)
{
	double lat1=lat,lon1=lon,lat2,lon2;
	double aht;
 	::GPS_Math_Known_Datum_To_WGS84_M(lat1,lon1,0,&lat2,&lon2, &aht,78);	
	lat=lat2;
	lon=lon2;
}

GridRef ll_to_gr(const LatLon& pos)
{
	return ll_to_gr(pos.lat,pos.lon);
}

/* @func  GPS_Math_LatLon_To_EN **********************************
**
** Convert latitude and longitude to eastings and northings
** Standard Gauss-Kruger Transverse Mercator
**
** @param [w] E [double *] easting (metres)
** @param [w] N [double *] northing (metres)
** @param [r] phi [double] latitude (deg)
** @param [r] lambda [double] longitude (deg)
** @param [r] N0 [double] true northing origin (metres)
** @param [r] E0 [double] true easting  origin (metres)
** @param [r] phi0 [double] true latitude origin (deg)
** @param [r] lambda0 [double] true longitude origin (deg)
** @param [r] F0 [double] scale factor on central meridian
** @param [r] a [double] semi-major axis (metres)
** @param [r] b [double] semi-minor axis (metres)
**
** @return [void]
************************************************************************/
/*
void GPS_Math_LatLon_To_EN(double *E, double *N, double phi,
			   double lambda, double N0, double E0,
			   double phi0, double lambda0,
			   double F0, double a, double b)
{
    double esq;
    double n;
    double etasq;
    double nu;
    double rho;
    double M;
    double I;
    double II;
    double III;
    double IIIA;
    double IV;
    double V;
    double VI;
    
    double tmp;
    double tmp2;
    double fdf;
    double fde;
   
	// Replaced these deg/rad conversions (originally done with a function)
	// with simple multiplication
    phi0    *= (M_PI/180.0); 
    lambda0 *= (M_PI/180.0); 
    phi     *= (M_PI/180.0); 
    lambda  *= (M_PI/180.0); 
    
    esq = ((a*a)-(b*b)) / (a*a);
    n   = (a-b) / (a+b);
    
    tmp  = (double)1.0 - (esq * sin(phi) * sin(phi));
    nu   = a * F0 * pow(tmp,(double)-0.5);
    rho  = a * F0 * ((double)1.0 - esq) * pow(tmp,(double)-1.5);
    etasq = (nu / rho) - (double)1.0;

    fdf   = (double)5.0 / (double)4.0;
    tmp   = (double)1.0 + n + (fdf * n * n) + (fdf * n * n * n);
    tmp  *= (phi - phi0);
    tmp2  = (double)3.0*n + (double)3.0*n*n + ((double)21./(double)8.)*n*n*n;
    tmp2 *= (sin(phi-phi0) * cos(phi+phi0));
    tmp  -= tmp2;

    fde   = ((double)15.0 / (double)8.0);
    tmp2  = ((fde*n*n) + (fde*n*n*n)) * sin((double)2.0 * (phi-phi0));
    tmp2 *= cos((double)2.0 * (phi+phi0));
    tmp  += tmp2;
    
    tmp2  = ((double)35.0/(double)24.0) * n * n * n;
    tmp2 *= sin((double)3.0 * (phi-phi0));
    tmp2 *= cos((double)3.0 * (phi+phi0));
    tmp  -= tmp2;

    M     = b * F0 * tmp;
    I     = M + N0;
    II    = (nu / (double)2.0) * sin(phi) * cos(phi);
    III   = (nu / (double)24.0) * sin(phi) * cos(phi) * cos(phi) * cos(phi);
    III  *= ((double)5.0 - (tan(phi) * tan(phi)) + ((double)9.0 * etasq));
    IIIA  = (nu / (double)720.0) * sin(phi) * pow(cos(phi),(double)5.0);
    IIIA *= ((double)61.0 - ((double)58.0*tan(phi)*tan(phi)) +
	     pow(tan(phi),(double)4.0));
    IV    = nu * cos(phi);

    tmp   = pow(cos(phi),(double)3.0);
    tmp  *= ((nu/rho) - tan(phi) * tan(phi));
    V     = (nu/(double)6.0) * tmp;

    tmp   = (double)5.0 - ((double)18.0 * tan(phi) * tan(phi));
    tmp  += tan(phi)*tan(phi)*tan(phi)*tan(phi) + ((double)14.0 * etasq);
    tmp  -= ((double)58.0 * tan(phi) * tan(phi) * etasq);
    tmp2  = cos(phi)*cos(phi)*cos(phi)*cos(phi)*cos(phi) * tmp;
    VI    = (nu / (double)120.0) * tmp2;
    
    *N = I + II*(lambda-lambda0)*(lambda-lambda0) +
	     III*pow((lambda-lambda0),(double)4.0) +
	     IIIA*pow((lambda-lambda0),(double)6.0);

    *E = E0 + IV*(lambda-lambda0) + V*pow((lambda-lambda0),(double)3.0) +
	 VI * pow((lambda-lambda0),(double)5.0);

    return;
}
*/
// gr_to_ll()
// Slightly modified version of:
/* @func GPS_Math_NGENToAiry1830LatLon **************************************
**
** Convert  to UK Ordnance Survey National Grid Eastings and Northings to
** Airy 1830 datum latitude and longitude
**
** @param [r] E [double] NG easting (metres)
** @param [r] N [double] NG northing (metres)
** @param [w] phi [double *] Airy latitude     (deg)
** @param [w] lambda [double *] Airy longitude (deg)
**
** @return [void]
************************************************************************/

// Modifications:
// - Grid reference passed as a struct of type GridRef.
// - Lat/long parameters renamed from "phi" and "lambda" to "lat" and "lng".

LatLon gr_to_ll(const GridRef& gridref)
{
	LatLon pos;
/*
    double N0      = -100000;
    double E0      =  400000;
    double F0      = 0.9996012717;
    double phi0    = 49.;
    double lambda0 = -2.;
    double a       = 6377563.396;
    double b       = 6356256.910;
	double alat, alng, ht;

    ::GPS_Math_EN_To_LatLon
			(gridref.e,gridref.n,&alat,&alng,N0,E0,phi0, lambda0,F0, a,b);
   
	// 30/04/05 convert to WGS84 lat/lon rather than Airy
// 	::GPS_Math_Known_Datum_To_WGS84_M(alat,alng,0,&pos.lat,&pos.lon, &ht,78);	
	
	pos.lat=alat;
	pos.lon=alng;
*/

	// !!! everything now in lats/longs !!!
	pos.lon=gridref.e;
	pos.lat=gridref.n;	
    return pos;
}

/* @func  GPS_Math_EN_To_LatLon **************************************
**
** Convert Eastings and Northings to latitude and longitude
**
** @param [w] E [double] NG easting (metres)
** @param [w] N [double] NG northing (metres)
** @param [r] phi [double *] Airy latitude     (deg)
** @param [r] lambda [double *] Airy longitude (deg)
** @param [r] N0 [double] true northing origin (metres)
** @param [r] E0 [double] true easting  origin (metres)
** @param [r] phi0 [double] true latitude origin (deg)
** @param [r] lambda0 [double] true longitude origin (deg)
** @param [r] F0 [double] scale factor on central meridian
** @param [r] a [double] semi-major axis (metres)
** @param [r] b [double] semi-minor axis (metres)
**
** @return [void]
************************************************************************/
/*
void GPS_Math_EN_To_LatLon(double E, double N, double *phi,
			   double *lambda, double N0, double E0,
			   double phi0, double lambda0,
			   double F0, double a, double b)
{
    double esq;
    double n;
    double etasq;
    double nu;
    double rho;
    double M;
    double VII;
    double VIII;
    double IX;
    double X;
    double XI;
    double XII;
    double XIIA;
    double phix;
    double nphi=0.0;
    
    double tmp;
    double tmp2;
    double fdf;
    double fde;

	// Replaced these deg/rad conversions (originally done with a function)
	// with simple multiplication
    phi0    *= (M_PI/180.0); 
    lambda0 *= (M_PI/180.0); 

    n     = (a-b) / (a+b);
    fdf   = (double)5.0 / (double)4.0;
    fde   = ((double)15.0 / (double)8.0);

    esq = ((a*a)-(b*b)) / (a*a);


    phix = ((N-N0)/(a*F0)) + phi0;
    
    tmp  = (double)1.0 - (esq * sin(phix) * sin(phix));
    nu   = a * F0 * pow(tmp,(double)-0.5);
    rho  = a * F0 * ((double)1.0 - esq) * pow(tmp,(double)-1.5);
    etasq = (nu / rho) - (double)1.0;

    M = (double)-1e20;

    while(N-N0-M > (double)0.000001)
    {
	nphi = phix;
	
	tmp   = (double)1.0 + n + (fdf * n * n) + (fdf * n * n * n);
	tmp  *= (nphi - phi0);
	tmp2  = (double)3.0*n + (double)3.0*n*n +
	        ((double)21./(double)8.)*n*n*n;
	tmp2 *= (sin(nphi-phi0) * cos(nphi+phi0));
	tmp  -= tmp2;


	tmp2  = ((fde*n*n) + (fde*n*n*n)) * sin((double)2.0 * (nphi-phi0));
	tmp2 *= cos((double)2.0 * (nphi+phi0));
	tmp  += tmp2;
    
	tmp2  = ((double)35.0/(double)24.0) * n * n * n;
	tmp2 *= sin((double)3.0 * (nphi-phi0));
	tmp2 *= cos((double)3.0 * (nphi+phi0));
	tmp  -= tmp2;

	M     = b * F0 * tmp;

	if(N-N0-M > (double)0.000001)
	    phix = ((N-N0-M)/(a*F0)) + nphi;
    }
    

    VII  = tan(nphi) / ((double)2.0 * rho * nu);

    tmp  = (double)5.0 + (double)3.0 * tan(nphi) * tan(nphi) + etasq;
    tmp -= (double)9.0 * tan(nphi) * tan(nphi) * etasq;
    VIII = (tan(nphi)*tmp) / ((double)24.0 * rho * nu*nu*nu);

    tmp  = (double)61.0 + (double)90.0 * tan(nphi) * tan(nphi);
    tmp += (double)45.0 * pow(tan(nphi),(double)4.0);
    IX   = tan(nphi) / ((double)720.0 * rho * pow(nu,(double)5.0)) * tmp;

    X    = (double)1.0 / (cos(nphi) * nu);

    tmp  = (nu / rho) + (double)2.0 * tan(nphi) * tan(nphi);
    XI   = ((double)1.0 / (cos(nphi) * (double)6.0 * nu*nu*nu)) * tmp;

    tmp  = (double)5.0 + (double)28.0 * tan(nphi)*tan(nphi);
    tmp += (double)24.0 * pow(tan(nphi),(double)4.0);
    XII  = ((double)1.0 / ((double)120.0 * pow(nu,(double)5.0) * cos(nphi)))
	   * tmp;

    tmp  = (double)61.0 + (double)662.0 * tan(nphi) * tan(nphi);
    tmp += (double)1320.0 * pow(tan(nphi),(double)4.0);
    tmp += (double)720.0  * pow(tan(nphi),(double)6.0);
    XIIA = ((double)1.0 / (cos(nphi) * (double)5040.0 * pow(nu,(double)7.0)))
	   * tmp;

    *phi = nphi - VII*pow((E-E0),(double)2.0) + VIII*pow((E-E0),(double)4.0) -
	   IX*pow((E-E0),(double)6.0);
    
    *lambda = lambda0 + X*(E-E0) - XI*pow((E-E0),(double)3.0) +
	      XII*pow((E-E0),(double)5.0) - XIIA*pow((E-E0),(double)7.0);

	// Replaced these rad/deg conversions (originally done with a function)
	// with simple multiplication
    *phi   *= (180.0/M_PI); 
    *lambda *= (180.0/M_PI); 

    return;
}
*/
// The remainder of the functions are my own... licence notice below applies..

/*
    Copyright (C) 2004 Nick Whitelegg, Hogweed Software, nick@hogweed.org 

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
// Distance function
double dist (double x1, double y1, double x2, double y2)
{
	double dx=x1-x2, dy=y1-y2;
	return sqrt (dx*dx + dy*dy);
}

// Make a GPX timestamp from a plain Unix timestamp
void mkgpxtime (char *gpx_timestamp, time_t timestamp)
{
	strftime(gpx_timestamp,1024,"%Y-%m-%dT%H:%M:%SZ",gmtime(&timestamp));
}

}
