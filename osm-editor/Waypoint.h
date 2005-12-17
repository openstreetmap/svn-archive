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
#ifndef WAYPOINT_H
#define WAYPOINT_H

#include <vector>
#include <fstream>
#include <qstring.h>

using std::vector;


namespace OpenStreetMap 
{

struct WaypointMap
{
	int garmin;
	QString type;
};

struct Waypoint
{
	double lat, lon;
	QString name; 
	QString type;
	int osm_id;
	bool altered;
		 
	static WaypointMap waypointMap[];

	Waypoint(){ lat=lon=0;name="none";type="waypoint"; osm_id=0;
				altered=false;}	
	Waypoint(const QString& nm, double lt, double ln, const QString& tp)
		{ name=nm; lat=lt; lon=ln; type=tp;  osm_id=0; altered=false; }
	static QString garminToType(int);
	int toOSM(std::ostream&);
	void uploadToOSM(const char*,const char*);
};

class Waypoints
{
private:
	vector<Waypoint> waypoints;

public:
	Waypoints() { }
	void addWaypoint(const Waypoint& wp) { waypoints.push_back(wp);}
	bool deleteWaypoint(int i);
	Waypoint getWaypoint(int i) throw (QString); 
	void toGPX(std::ostream&);
	int toOSM(std::ostream&);
	int newToOSM(std::ostream&);
	int size() { return waypoints.size(); }
	bool alterWaypoint(int,const QString&,const QString&);
	bool uploadToOSM(int,const char*,const char*);
	void newUploadToOSM(char*,char*);
};

}
#endif /* not WAYPOINT_H */
