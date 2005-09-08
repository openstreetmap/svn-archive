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

#include <qregexp.h>

namespace OpenStreetMap 
{

GPXParser::GPXParser(  )
{
	inDoc = inWpt = inTrk = inName = inTrkpt = inType = 
	inTrkseg = inPolygon = inId = false;
	components = new Components;
	curSeg = curId = 0;
	curName = curType = curTimestamp = "";
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
		else if (element=="id" && inTrkseg)
		{
			inId = true;
			curId = 0;
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

	else if(inId && element=="id")
		inId = false;

	else if(inTime && element=="time")
		inTime=false;

	
	else if(inWpt && element=="wpt")
	{
			/*
		cerr<<"adding waypoint:" <<
				curName <<" " << curLat << " " << curLong << " "
				<< atoi(curType.ascii()) << endl;
				*/
		components->addWaypoint(Waypoint(curName,curLat,curLong,curType,curId));
		inWpt = false;
		curId = 0;
	}

	// If the segment had a type, add the segment to the segment table.
	else if (inTrkseg && element=="trkseg")
	{
		components->setSegId(curSeg,curId);
		components->setSegType(curSeg++,curType);
		inTrkseg = false;
	}

	return true;
}

bool GPXParser::characters(const QString& characters)
{
	QString chr=characters;
	if(characters==QString::null) chr="NULL";
	if(characters=="\n") return true;
	if(inName)
	{
		curName = chr;
		if(inTrk)
		{
			if(inTrkseg)
				components->setSegName(curSeg,curName);
			else
				components->setTrackID (curName);
		}
		else if (inWpt)
		{
			cerr << "PARSING: " << curName << "--> "; 
			// Parse a name of the form ID:name, as exported by Freemap.
			QRegExp regexp("^(\\d+):(.*)$");
			if(regexp.search(curName)>=0)
			{
				cerr << "THERE WAS A MATCH " << endl;
				curName = regexp.cap(2);
				if(curName==QString::null) {curName="NULL";}
				cerr << "curName now = " << curName << endl;
				curId = atoi( regexp.cap(1).ascii() );
				cerr << "curId now = " << curId << endl;
			}
			else
			{
				cerr << "NO MATCH" << endl;
			}
			
		}
	}
	else if(inType)
		curType = chr; 
	else if(inId)
		curId = atoi(chr.ascii());
	else if(inTime)
	{
		curTimestamp = chr; // 10/04/05 timestamp now string 
//		cerr<<curTimestamp<<endl;
	}

	return true;
}

}
////////////////////////////////////////////////////////////////////////////////



