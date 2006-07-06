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

#include "GPSDevice2.h"
#include "gps.h"

#include "functions.h"
#include "Components2.h"

#include <map>
#include <iostream>
#include <cmath>
#include <sstream>
#include <cctype>

#include <float.h>

namespace OpenStreetMap 
{


GPSDevice2::GPSDevice2(const QString& mdl, const char* p)
{ 
	std::map<QString,int(*) (const char*, Components2*)> trackFuncs;
	trackFuncs["Garmin"] = GPSDevice2::garminGetTrack;
	// Other models can be added here as needed

	std::map<QString,int(*) (const char*, Components2*)> waypointFuncs;
	waypointFuncs["Garmin"] = GPSDevice2::garminGetWaypoints;
	// Other models can be added here as needed
	
	model=mdl; 
	trackFunc = trackFuncs[mdl];
  	waypointFunc = waypointFuncs[mdl];

	strcpy(port,p); 
}

Components2* GPSDevice2::getSurveyedComponents()
{
	Components2 *c = new Components2;
	if (!trackFunc(port, c) && !waypointFunc(port, c))
	{
		return c;
	}
	delete c;
	return NULL;
}

// Get track from a Garmin using jeeps.
int GPSDevice2::garminGetTrack(const char* port,Components2 *c)
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

		// 10/04/05 now timestamps are stored in trackpoints in GPX format
		mkgpxtime(gpx_timestamp, trackpts[count]->Time);
		/*
		c->addNewNode( 
						trackpts[count]->lat, 
						trackpts[count]->lon,
						"", "trackpoint",
						gpx_timestamp
					 );
					 */
		c->addTrackPoint( 
						trackpts[count]->lat, 
						trackpts[count]->lon,
						gpx_timestamp
					 );

		GPS_Track_Del(&trackpts[count]);

	}

	free(trackpts);
	return 0;
}	

int GPSDevice2::garminGetWaypoints(const char* port, Components2 *c)
{
	GPS_PWay *waypts;
	int nwaypoints;

	if(GPS_Init(port) < 0)
		return 1;

	/* The GPSBabel version of this function specifies a callback as a third
	 * parameter. From what I gather this is a progress function (to monitor
	 * the reading of the waypoints from the GPS? it's not clear) but if it's 
	 * NULL it won't attempt to call it. So just set it to NULL. 
	 */

	nwaypoints = GPS_Command_Get_Waypoint(port,&waypts, NULL);

	for(int count=0; count<nwaypoints; count++)
	{
		c->addNewNode( 
				waypts[count]->lat, 
				waypts[count]->lon,
				waypts[count]->ident, 
				GPSDevice2::garminToType(waypts[count]->smbl)
					);

		GPS_Way_Del(&waypts[count]);
	}
	free(waypts);
	return 0;
}

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

// Make a GPX timestamp from a plain Unix timestamp
void mkgpxtime (char *gpx_timestamp, time_t timestamp)
{
	strftime(gpx_timestamp,1024,"%Y-%m-%dT%H:%M:%SZ",gmtime(&timestamp));
}

// Anglicised versions of Garmin symbols :-)
// BTW did a good walk in the New Forest from the sprawling, skyscraper-
// dominated __CITY__ of Minstead yesterday, passing near the neighbouring 
// megapolises of Emery Down and Stoney Cross.... :-)
WaypointMap GPSDevice2::waypointMap[] = { { 10, "farm" },
					 { 11, "restaurant" },
					 { 13, "pub" },
					 { 18, "waypoint" },
					 { 151, "campsite" },
					 { 158, "car park" },
					 { 159, "country park" },
					 { 166, "caution" },
					 { 8198, "hamlet" },
					 { 8199, "village" },
					 { 8200, "small town" },
					 { 8201, "suburb" },
					 { 8202, "medium town" },
					 { 8203, "large town" },
					 { 8233, "bridge" },
					 { 8236, "church" },
					 { 8243, "tunnel" },
					 { 8246, "hill" },
					 { 16391,"mast" } };

QString GPSDevice2::garminToType(int smbl)
{
	for(int count=0; count<17; count++)
	{
		if(waypointMap[count].garmin == smbl)
			return waypointMap[count].type;
	}
	return "";
}
}
