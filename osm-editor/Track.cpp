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
#include "Track.h"
#include "Segment.h"


using std::endl;

namespace OpenStreetMap
{

void Track::writeTrkpt(std::ostream& outfile, int count)
{
		outfile << "<trkpt lat=\"" << points[count].lat << 
				"\" lon=\"" << points[count].lon << "\">"
				<< endl << "<time>"<<points[count].timestamp<<"</time>"<<endl
				<<"</trkpt>"<<endl;
}

// Write a track to GPX.
// Quick whinge: I've been forced to endlessly check for array overflows
// in this method. This adds considerably to the code and reduces its 
// elegance significantly...
void Track::toGPX(std::ostream &outfile,const vector<SegDef> &segdefs)
{
	int segcount=0;
	bool clone=false;

	outfile<<"<trk>" << endl << "<name>" << id << "</name>" << endl;

	for(int count=0; count<points.size(); count++)
	{
		// Start of a defined path segment
		if(clone||(segcount<segdefs.size() && count == segdefs[segcount].start))
		{
			outfile << "<trkseg><extensions>" << endl
				    << "<type>" << segdefs[segcount].type << "</type>"<<endl
					<< "</extensions>" << endl;

			// Write the last track point if we're cloning
			if(clone)
			{
				writeTrkpt(outfile,count-1);
				clone=false;
			}
		}
		// Start of an undefined segment
		else if ( (segcount>0 && count==segdefs[segcount-1].end+1) ||
				  count==0)
		{
			outfile << "<trkseg>" << endl;
		}

		writeTrkpt(outfile,count);

		// End of a defined path segment
		if(segcount<segdefs.size() && count==segdefs[segcount].end)
		{
			outfile << "</trkseg>" << endl;
			segcount++;

			// If the new defined segment starts with the current point
			// (the same as the end point of the old defined segment)
			// we will clone the point between the two segments.
			if(segcount<segdefs.size() && count==segdefs[segcount].start)
				clone=true;
		}

		// End of an undefined segment
		else if ( count==points.size()-1 || 
				 (segcount<segdefs.size()&& count==segdefs[segcount].start-1))
		{
			outfile << "</trkseg>" << endl;
		}
	}
	outfile << "</trk>"<<endl;
}
bool Track::deletePoints(int start, int end)
{
	if(start>=0&&start<points.size()&&end>=0&&end<points.size())
	{
		vector<TrackPoint>::iterator i; 
		for(int count=0; count<(end-start)+1; count++)
		{
			i=points.begin()+start;	
			points.erase(i);
		}
		return true;
	}

	return false;
}

TrackPoint Track::getPoint(int i) throw (QString)
{ 
	if(i<0 || i>=points.size())
		throw QString("No track point at index " + i);
	
 	return points[i]; 
}

}
