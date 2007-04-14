#include "Way.h"
#include "RouteMetaDataHandler.h"
#include "NodeMetaDataHandler.h"
#include "Components2.h"
#include <cfloat>
//Added by qt3to4:
#include <QTextStream>

/*
Copyright (C) 2006 Nick Whitelegg, Hogweed Software, nick@hogweed.org

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
namespace OpenStreetMap
{

void Way::setSegments(vector<Segment*>& s)
{
	segments.clear();

	for(int count=0; count<s.size(); count++)
	{
		if(s[count]->getOSMID())
			addSegment(s[count]);
	}
}

void Way::toOSM(QTextStream &outfile, bool allUid)
{
	//QString wayOrArea = (area) ? "area" : "way";
	QString wayOrArea = "way";

    outfile << "<" << wayOrArea;
    int sent_id = (osm_id>0 || (allUid&&osm_id)) ? osm_id : 0;
    outfile << " id='" << sent_id   << "'>" << endl;

	writeTags(outfile);

	for(int count=0; count<segments.size(); count++)
	{
		outfile << "<seg id='" << segments[count] << "' />" << endl;
	}

   	outfile << "</"<<wayOrArea<<">" <<endl;

}

// remove a segment - returns its position
int Way::removeSegment(Segment *s)
{
	for(vector<int>::iterator i=segments.begin(); i!=segments.end(); i++)
	{
		if(*i==s->getOSMID())
		{
			int index = i-segments.begin();
			segments.erase(i);
			s->setOSMID(0);
			s->setWayStatus(false);
			return index;
		}
	}
	return -1;
}

// Insert a segment at position 'index'
bool Way::addSegmentAt(int index, Segment *s)
{
	vector<int>::iterator i = segments.begin() + index;
	//050806 bug if(i!=segments.end() && s->getOSMID())
	if(s->getOSMID())
	{
		s->setWayID(osm_id);
		/* 090706 not anymore - see addSegment()
		if(type!="")
			s->setType(type);
		*/
		s->setWayStatus(true);
		segments.insert(i,s->getOSMID());
		return true;
	}
	return false;
}

// 090706 this can probably all go - see addSegment() - but keep all but
// setType() in for the moment to lessen chances of random bugs
void Way::setSegs()
{
		QString t=getType();
		Segment *curSeg;
		// Segments take on the type of the parent way, if it has one
		if(type!="")
		{
			for(int count=0; count<segments.size(); count++)
			{
				cerr << "getting segment: "  << segments[count] << endl;
				curSeg = components->getSegmentByID(segments[count]);
				if(curSeg!=NULL)
				{
					cerr << "curSeg is not NULL" << endl;
					cerr << "segment exists : id = " 
								<< curSeg->getOSMID() << endl;
					//curSeg->setType(type);
					curSeg->setWayStatus(true);
				}
			}
		}
}

void Way::setOSMID(int i)
{
		Segment *curSeg;
		cerr << "SETTING OSM ID TO " << i << endl;
		osm_id = i;
		for(int count=0; count<segments.size(); count++)
		{
			if((curSeg=components->getSegmentByID(segments[count]))!=NULL)
				curSeg->setWayID(i);
		}
}

Segment *Way::getSegment(int i)
{ 
	return (i>=0 && i<segments.size()) ?
			components->getSegmentByID(segments[i]) : NULL;
}

Segment *Way::longestSegment()
{
	double maxDist = DBL_MIN, dist;
	Segment *curSeg, *longestSeg = NULL;

	for(int count=0; count<segments.size(); count++)
	{
		if((curSeg=components->getSegmentByID(segments[count]))!=NULL)
		{
			if((dist=curSeg->length()) > maxDist)
			{
				maxDist = dist;
				longestSeg = curSeg;
			}
		}
	}
	return longestSeg;
}


}
