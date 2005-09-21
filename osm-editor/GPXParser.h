#ifndef GPXPARSER_H
#define GPXPARSER_H

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

// 19/03/05 convert to using the Qt XML parser. This might be seen to have
// reusability issues but:
//
// a) Try as I might I could not get libxml++ to compile on Windows/MinGW.
// Gave all sorts of weird linker errors that even posting to the libxml++
// mailing list could not solve....
//
// b) The new Qt4 is modularising its components, so it will be possible to
// link the XML bit only in a non GUI application
//
// c) Qt4 will be GPL for Windows, hence no issues on that platform.
//
// GPX parser
// Returns a list of FreemapComponents, ready for Freemap software.
// Licence LGPL

#include <qxml.h>
#include "Components.h"

namespace OpenStreetMap 
{

class GPXParser : public QXmlDefaultHandler
{
private:
	bool inDoc, inWpt, inTrk, inName, inTrkpt, inId,
		inType,  inTime, inSegment, foundSegType, inPolygon, osm;
	Components* components;
	Track* curTrack; 
	QString curName, curType; 
	double curLat, curLong;
	QString curTimestamp; // 10/04/05 timestamp now string
	int segStart, trkptCount;
	Polygon *curPolygon;
	int curSeg, curId;

public:
	bool startDocument();
	bool endDocument();
	bool startElement(const QString& , const QString&, const QString&,	
									const QXmlAttributes&);
	bool endElement(const QString&,const QString&, const QString&);
	bool characters(const QString& characters);

	void setOSM(bool osm) { this->osm = osm; }

	
	GPXParser();
	Components* getComponents() const { return components; }

	// it's the recipient of the components' responsibility to delete them!
	~GPXParser() {} 
};

}

#endif
