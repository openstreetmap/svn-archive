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

void Track::toGPX(std::ostream &outfile,const vector<SegDef> &segdefs)
{
	int segcount=0;

	outfile << "points.size()" << points.size()<<endl;
	outfile<<"<trk>" << endl << "<name>" << id << "</name>" << endl;

	for(int count=0; count<points.size(); count++)
	{
		// Start of a defined path segment
		if(segdefs.size() && count == segdefs[segcount].start)
		{
			outfile << "<trkseg><extensions>" << endl
				    << "<type>" << segdefs[segcount].type << "</type>"<<endl
					<< "</extensions>" << endl;
		}
		// Start of an undefined segment
		else if ( (segcount>0 && count==segdefs[segcount-1].end+1) ||
				  count==0)
		{
			outfile << "<trkseg>" << endl;
		}

		outfile << "<trkpt lat=\"" << points[count].lat << 
				"\" lon=\"" << points[count].lon << "\">"
				<< endl << "<time>"<<points[count].timestamp<<"</time>"<<endl
				<<"</trkpt>"<<endl;

		// End of a defined path segment
		if(segdefs.size() && count==segdefs[segcount].end)
		{
			outfile << "</trkseg>" << endl;
			segcount++;
		}
		// End of an undefined segment
		else if ( (segcount<segdefs.size()&&count==segdefs[segcount].start-1) 
					|| count==points.size()-1)
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

}
