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
#include <sstream>
#include "curlstuff.h"
#include <qstringlist.h>

#include <iostream>
using namespace std; 
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

// output in OSM node format
// tags will be 'class=[the node's type]'

int Waypoint::toOSM(std::ostream& outfile)
{
	outfile << "<node lat='" << lat << "' lon='" << lon << 
					"' tags='";
	if(name != "")
		outfile << "name=" << name << ";";
	outfile << "class=" << type << "' ";
	if(osm_id)
		outfile << "uid='" << osm_id << "' ";
	outfile << "/>";
	return 1;
}

void Waypoint::uploadToOSM(const char* username, const char* password)
{
	char *nonconst, *resp;

		if(osm_id)
		{
			std::ostringstream str;
			str<<"<osm version='0.2'>"<<endl;
			toOSM(str);
			str<<"</osm>"<<endl;
			nonconst = new char[ strlen(str.str().c_str()) + 1];	
			strcpy(nonconst,str.str().c_str());

			char url[1024];	
			sprintf(url,"http://www.openstreetmap.org/api/0.2/node/%d", 
							osm_id);
			cerr<<"URL:" << url << endl;
			delete[] nonconst;

			resp = put_data(nonconst,url,username,password);
			if(resp)
			{
				altered = false;
				delete[] resp;
			}
		}
}	

void Waypoints::toGPX(std::ostream& outfile)
{
	QString name="";
	for(int count=0; count<waypoints.size(); count++)
	{
			
		name = waypoints[count].name;
			

		outfile << "<wpt lat=\"" << waypoints[count].lat << 
				"\" lon=\"" << waypoints[count].lon << "\">"
				<< endl << "<name>"<<name<<"</name>"<<endl
			<<"<type>"<<waypoints[count].type<<"</type>"<<endl<<"</wpt>"<<endl;
	}
}

int Waypoints::toOSM(std::ostream& outfile)
{
	int nWpts=0;
	outfile << "<osm version='0.2'>" << endl;
	for(int count=0; count<waypoints.size(); count++)
		nWpts += waypoints[count].toOSM(outfile);
	outfile << "</osm>" << endl;
	return nWpts;
}

// new waypoints only
int Waypoints::newToOSM(std::ostream& outfile)
{
	int nWpts=0;
	outfile << "<osm version='0.2'>" << endl;
	for(int count=0; count<waypoints.size(); count++)
	{
		if(!waypoints[count].osm_id)
			nWpts += waypoints[count].toOSM(outfile);
	}
	outfile << "</osm>" << endl;
	return nWpts;
}

void Waypoints::newUploadToOSM(char* username,char* password)
{
	char *nonconst, *resp;


	// new points
	std::ostringstream str;
	int nWpts = newToOSM(str);
	cerr << "Here are the uploaded waypoints:" << str.str() << endl;
	if(nWpts)
	{
		
		nonconst = new char[ strlen(str.str().c_str()) + 1];	
		strcpy(nonconst,str.str().c_str());

		QStringList ids = putToOSM(nonconst,
					"http://www.openstreetmap.org/api/0.2/newnode",
					username,password);
		delete[] nonconst;

		int count=0;
		for(QStringList::Iterator i = ids.begin(); i!=ids.end(); i++)
		{
			if(atoi((*i).ascii())  && !waypoints[count].osm_id)
				waypoints[count++].osm_id = atoi((*i).ascii());
			cerr << "parsing response: count: " << count << 
						" current id: " << (*i) << endl;
		}
	}
}

bool Waypoints::alterWaypoint(int idx, const QString& newName,
								const QString& newType)
{
	if(idx>=0 && idx<waypoints.size())
	{
		waypoints[idx].name = newName;
		waypoints[idx].type = newType;
		waypoints[idx].altered=true;
		return true;
	}
	return false;
}

bool Waypoints::uploadToOSM(int idx, const char * username, 
								const char * password)
{
	if(idx>=0 && idx<waypoints.size())
	{
		waypoints[idx].uploadToOSM(username,password);
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
