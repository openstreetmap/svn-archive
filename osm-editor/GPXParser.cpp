/*
    Copyright (C) 2005 Nick Whitelegg, Hogweed Software, nick@hogweed.org 

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

#include "GPXParser.h"

#include <iostream>
#include <cstdlib>
using std::cout;
using std::endl;
using std::cerr;

namespace OpenStreetMap 
{

GPXParser::GPXParser(  )
{
	inDoc = inWpt = inTrk = inName = inTrkpt = inType = 
	inTrkseg = inPolygon = false;
	components = new Components;
	curSeg = 0;
}

bool GPXParser::startDocument()
{
	inDoc = true;
	return true;
}

bool GPXParser::endDocument()
{
	inDoc = false;
	return true;
}

bool GPXParser::startElement(const QString&,const QString&,
						const QString& element,
						const QXmlAttributes& attributes)
{
	if(inDoc)
	{
		if(element=="wpt")
		{
			inWpt=true;
		}
		else if (element=="trk")
		{
			inTrk=true;
		}
		else if (element=="trkseg")
		{
			inTrkseg=true;
			curType = "track";
			components->newSegment();
		}
		else if (element=="polygon")
		{
			inPolygon = true;
			curPolygon = new Polygon;
		}
		else if (element=="name" && (inWpt||inTrkpt||inTrk))
			inName=true;
		else if (element=="type" && (inWpt||inTrk||inTrkseg||inPolygon))
		{
			inType=true;
		}
		else if (element=="time" && (inWpt||inTrkpt))
		{
			inTime=true;
		}
		else if (element=="trkpt" && inTrk)
		{
			inTrkpt = true;
		}

		if(element=="wpt"||element=="trkpt"||element=="polypt")
		{
			for(int count=0; count<attributes.length(); count++)
			{
				if(attributes.qName(count)=="lat")
					curLat = atof (attributes.value(count).ascii());		
				else if(attributes.qName(count)=="lon")
					curLong = atof (attributes.value(count).ascii());
			}

			if(element=="polypt" && inPolygon)
			{
				curPolygon->addPoint(curLat,curLong);	
			}
		}
	}
	return true;
}

bool GPXParser::endElement(const QString&,const QString&,
						const QString&	element)
{
	if(inTrkpt && element=="trkpt")
	{
		components->addTrackpoint(curSeg,curTimestamp,curLat,curLong);
		inTrkpt = false;
	}

	else if(inTrk && element=="trk")
	{
		components->setTrackID (curName);
		inTrk = false;
	}

	else if(inPolygon && element=="polygon")
	{
		curPolygon->setType(curType);
		components->addPolygon(curPolygon);
		inPolygon=false;
	}
	else if(inName && element=="name")
		inName=false;

	else if(inType && element=="type")
		inType=false;

	else if(inTime && element=="time")
		inTime=false;

	
	else if(inWpt && element=="wpt")
	{
			/*
		cerr<<"adding waypoint:" <<
				curName <<" " << curLat << " " << curLong << " "
				<< atoi(curType.ascii()) << endl;
				*/
		components->addWaypoint(Waypoint(curName,curLat,curLong,curType));
		inWpt = false;
	}

	// If the segment had a type, add the segment to the segment table.
	else if (inTrkseg && element=="trkseg")
	{
		components->setSegType(curSeg++,curType);
		inTrkseg = false;
	}

	return true;
}

bool GPXParser::characters(const QString& characters)
{
	if(characters=="\n") return true;
	if(inName)
	{
		curName = characters;
	}
	else if(inType)
		curType = characters; 
	else if(inTime)
	{
		curTimestamp = characters; // 10/04/05 timestamp now string 
//		cerr<<curTimestamp<<endl;
	}

	return true;
}

}
////////////////////////////////////////////////////////////////////////////////



