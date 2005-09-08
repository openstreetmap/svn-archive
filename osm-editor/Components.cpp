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

#include "Components.h"
#include <iostream>
#include <iomanip>
#include <string>
#include <sstream>

using std::endl;
using std::setw;
using std::setfill;
using std::setprecision;
using std::cerr;

using std::cout;

namespace OpenStreetMap 
{


void Components::toGPX(const char* filename)
{
	std::ofstream outfile(filename);
	doToGPX(outfile);
}

std::string Components::toGPX()
{
	std::ostringstream strm; 
	doToGPX(strm);
	return strm.str();
}

void Components::doToGPX(std::ostream &outfile)
{
	outfile<<"<gpx version=\"1.0\" " 
		   <<"creator=\"Hogweed Software Freemap::Components class\" " 
		   <<"xmlns=\"http://www.topografix.com/GPX/1/0\">" << endl;

	if(waypoints)waypoints->toGPX(outfile);
	if(track)track->toGPX(outfile);

	for(int count=0; count<polygons.size(); count++)
		polygons[count]->toGPX(outfile);

	outfile<<"</gpx>"<<endl;
}


void Components::clearAll()
{
	if(track){delete track; track=NULL;}
	if(waypoints){delete waypoints; waypoints=NULL; } 

	for(vector<Polygon*>::iterator i=polygons.begin(); i!=polygons.end(); i++)
		delete *i;
}

bool Components::addWaypoint(const Waypoint& w)
{
	if(waypoints)
	{
		waypoints->addWaypoint(w);
		return true;
	}
	return false;
}

bool Components::deleteWaypoint(int index)
{
	if(waypoints)
	{
		waypoints->deleteWaypoint(index);
		return true;
	}
	return false;
}

bool Components::addTrackpoint(int seg,
				const QString& timestamp,double lat,double lon)
{
	if(track && seg>=0 && seg<track->nSegs())
	{
		track->getSeg(seg)->addPoint(timestamp,lat,lon);
		return true;
	}
	return false;
}

Waypoint Components::getWaypoint(int i) throw(QString)
{
	if(!waypoints)
	{
		throw QString("No waypoints defined!");
	}
	return waypoints->getWaypoint(i);
}

bool Components::setTrackID(const char* i)
{
	if(track)
	{
		track->setID(i);
		return true;
	}
	return false;
}

Polygon *Components::getPolygon (int i)
{
	return (i>=0 && i<polygons.size()) ? polygons[i] : NULL;
}

// Merges these Components with another set
// Note that deep copying is done so the other Components can be subsequently
// trashed if desired.
bool Components::merge(Components *comp)
{
	if(!track)  { track=new Track; activeTrack = track; }
	if(!waypoints) waypoints = new Waypoints;

	track->copySegsFrom(comp->track);

	for(int count=0; count<comp->nWaypoints(); count++)
		waypoints->addWaypoint ( comp->getWaypoint(count) );

	return true;
}

}
