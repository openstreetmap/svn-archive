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

	track->newSegment();

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
		track->addTrackpt(0, gpx_timestamp,
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

}
