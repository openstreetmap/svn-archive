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
#include "Waypoint.h"

using std::endl;
namespace OpenStreetMap 
{

// Anglicised versions of Garmin symbols :-)
// BTW did a good walk in the New Forest from the sprawling, skyscraper-
// dominated __CITY__ of Minstead yesterday, passing near the neighbouring 
// megapolises of Emery Down and Stoney Cross.... :-)
WaypointMap Waypoint::waypointMap[] = { { 10, "farm" },
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

QString Waypoint::garminToType(int smbl)
{
	for(int count=0; count<17; count++)
	{
		if(waypointMap[count].garmin == smbl)
			return waypointMap[count].type;
	}
	return "";
}

void Waypoints::toGPX(std::ostream& outfile)
{
	QString name="";
	for(int count=0; count<waypoints.size(); count++)
	{
			
		if(waypoints[count].id)
			name.sprintf("%d:%s",waypoints[count].id,
				waypoints[count].name.ascii());
		else
			name = waypoints[count].name;
			

		outfile << "<wpt lat=\"" << waypoints[count].lat << 
				"\" lon=\"" << waypoints[count].lon << "\">"
				<< endl << "<name>"<<name<<"</name>"<<endl
			<<"<type>"<<waypoints[count].type<<"</type>"<<endl<<"</wpt>"<<endl;
	}
}

bool Waypoints::alterWaypoint(int idx, const QString& newName,
								const QString& newType)
{
	if(idx>=0 && idx<waypoints.size())
	{
		waypoints[idx].name = newName;
		waypoints[idx].type = newType;
		return true;
	}
	return false;
}

Waypoint Waypoints::getWaypoint(int i) throw (QString)
{ 
	if(i<0 || i>=waypoints.size())
	{
		// corrected + operator bug 17/09/05
		QString error;
		error.sprintf("No waypoint at index %d", i);
		throw error;
	}
	
 	return waypoints[i]; 
}

bool Waypoints::deleteWaypoint(int index)
{
	if(index>=0&&index<waypoints.size())
	{
		vector<Waypoint>::iterator i = waypoints.begin()+index;
		waypoints.erase(i);
		return true;
	}

	return false;
}

}
