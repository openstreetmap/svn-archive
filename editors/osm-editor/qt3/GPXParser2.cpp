/*
    Copyright (C) 2006 Nick Whitelegg, Hogweed Software, nick@hogweed.org 

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

// Read GPX
// Track points become nodes of type "trackpoint"
// Waypoints become nodes of the appropriate type
// No attempt to parse track segments or anything else.

#include "GPXParser2.h"

#include <iostream>
#include <cstdlib>
using std::cout;
using std::endl;
using std::cerr;

#include <qregexp.h>

namespace OpenStreetMap 
{

GPXParser2::GPXParser2(  )
{
	inDoc = inWpt = inTrk = inName = inTrkpt = inType = false;
	components = new Components2;
	curName = curType = curTimestamp = "";
}

bool GPXParser2::startDocument()
{
	inDoc = true;
	return true;
}

bool GPXParser2::endDocument()
{
	inDoc = false;
	return true;
}

bool GPXParser2::startElement(const QString&,const QString&,
						const QString& element,
						const QXmlAttributes& attributes)
{
	if(inDoc)
	{
		// 28/10/05 changed round to only call newSegment() if trkseg
		// is encountered 
		if (element=="trk")
		{
			inTrk = true;
			cerr<<"inTrk=true"<<endl;
		}

		else if(element=="wpt")
		{
			inWpt=true;
			curType="waypoint";
			curName = "";
		}
		else if (element=="name" && (inWpt||inTrkpt))
		{
			inName=true;
		}
		else if (element=="type" && (inWpt||inTrkpt))
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
			curType="trackpoint";
			curName = "";
		}

		if(element=="wpt"||element=="trkpt")
		{
			for(int count=0; count<attributes.length(); count++)
			{
				if(attributes.qName(count)=="lat")
					curLat = atof (attributes.value(count).ascii());		
				else if(attributes.qName(count)=="lon")
					curLong = atof (attributes.value(count).ascii());
			}
		}
	}
	return true;
}

bool GPXParser2::endElement(const QString&,const QString&,
						const QString&	element)
{
	if(inTrkpt && element=="trkpt")
	{
		//components->addNewNode (curLat,curLong,"","trackpoint",curTimestamp);
		components->addTrackPoint (curLat,curLong,curTimestamp);
		inTrkpt = false;
	}

	else if(inTrk && element=="trk")
	{
		inTrk = false;
	}

	else if(inName && element=="name")
		inName=false;

	else if(inType && element=="type")
		inType=false;

	else if(inTime && element=="time")
		inTime=false;

	else if(inWpt && element=="wpt")
	{
		components->addNewNode(curLat,curLong,curName,curType,curTimestamp);
		inWpt = false;
	}

	return true;
}

bool GPXParser2::characters(const QString& characters)
{
	QString chr=characters;
	if(characters==QString::null) chr="";
	if(characters=="\n") return true;
	if(inName)
		curName = chr;
	else if(inType)
		curType = chr; 
	else if(inTime)
		curTimestamp = chr; // 10/04/05 timestamp now string 

	return true;
}

}
////////////////////////////////////////////////////////////////////////////////

