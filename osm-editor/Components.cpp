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

using std::endl;
using std::setw;
using std::setfill;
using std::setprecision;
using std::cerr;

namespace OpenStreetMap 
{


void Components::toGPX(const char* filename)
{
	std::ofstream outfile(filename);

	outfile<<"<gpx version=\"1.0\" " 
		   <<"creator=\"Hogweed Software Freemap::Components class\" " 
		   <<"xmlns=\"http://www.topografix.com/GPX/1/0\">" << endl;

	if(track)track->toGPX(outfile,segdefs);
	if(waypoints)waypoints->toGPX(outfile);

	outfile<<"</gpx>"<<endl;
}


void Components::clearAll()
{
	if(track){delete track; track=NULL;}
	if(waypoints){delete waypoints; waypoints=NULL; } 
}

void Components::addSegdef(int start, int end, const QString& type)
{
	segdefs.insert(findPlace(start),SegDef(start,end,type));
}

vector<SegDef>::iterator Components::findPlace(int start)
{	
	for(vector<SegDef>::iterator i=segdefs.begin(); i!=segdefs.end(); i++)
	{
		if(i->start > start)
			return i;
	}
	return segdefs.end();
}

void Components::printSegDefs()
{
	cout<<"SegDefs::print()"<<endl;
	for(vector<SegDef>::iterator i=segdefs.begin(); i!=segdefs.end(); i++)
	{
		cout << i->start << " " << i->end <<" " << i->type << endl;
	}
}

bool Components::deleteTrackpoints(int start,int end)
{
	if(track->deletePoints(start,end))
	{
		cerr<<"Deletion= "<< start << " " << end << endl;
		vector<SegDef>::iterator j;
		int deletedPoints = (end-start)+1;
		bool startSegment;

		for(vector<SegDef>::iterator i=segdefs.begin(); i!=segdefs.end(); i++)
		{
			startSegment = false;

			// The current segment contains the deletion start...
			if(start>=i->start && start<=i->end)
			{
				cerr<<"Segment start: " << i->start << " end " 
						<< i->end << " contains deletion start: " << start
						<< endl;

				i->end = (end > i->end) ? start-1: i->end - deletedPoints;
				startSegment=true;
			}

			if(i->start>=start && i->end<=end)
			{
				cerr<<"Whole of segment " 
					<< i->start << "," << i->end << "is in deletion" << endl;
				j=i-1;			
				segdefs.erase(i);
				i=j;
			}
			else if(i->start > start)
			{
				cerr<<"Reducing points in non-start segment" << endl;
				cerr<<"Old: " << i->start << "," << i->end << endl;
				i->start = (i->start < end) ? start:i->start - deletedPoints;
				i->end -= deletedPoints;
				cerr<<"New: " << i->start << "," << i->end << endl;
			}
		}
	}
	return false;	
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

bool Components::addTrackpoint(time_t timestamp,double lat,double lon)
{
	if(track)
	{
		track->addTrackpt(timestamp,lat,lon);
		return true;
	}
	return false;
}

Waypoint Components::getWaypoint(int i)
{
	return(waypoints)? waypoints->getWaypoint(i):Waypoint();
}

TrackPoint Components::getTrackpoint(int i)
{
	return(track)? track->getPoint(i):TrackPoint();
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
}
