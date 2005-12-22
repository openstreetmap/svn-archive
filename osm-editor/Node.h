#ifndef NODE_H
#define NODE_H

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


#include <qstring.h>
#include <qcstring.h>
#include <qstringlist.h>
#include <vector>
#include <fstream>
#include "functions.h"
#include "EarthPoint.h"
#include <qtextstream.h>

#include <iostream>

using std::ostream;

namespace OpenStreetMap
{

class Node
{
private:
	double lat, lon;
	int osm_id;
	QString name, type;

public:
	Node()
	{
		lat=lon=0; 
		name=type=""; 
		osm_id = 0; 
	}

	Node(double lt, double ln)
	{
		lat=lt; 
		lon=ln; 
		osm_id=0; 
		name=type=""; 
	}
	Node(int i,double lt, double ln)
	{
		lat=lt; 
		lon=ln; 
		osm_id=i; 
		name=type=""; 
	}

	Node(int i,double lt, double ln,const QString& n, const QString& t)
	{
		lat=lt; 
		lon=ln; 
		name=n; 
		type=t; 
		osm_id=i; 
	}

	Node(double lt, double ln,const QString& n, const QString& t)
	{
		lat=lt; 
		lon=ln; 
		name=n; 
		type=t; 
		osm_id=0; 
	}

	int toOSM(QTextStream&,bool allUid=false);

	bool operator==(const Node& tp)
	{ 
		return (fabs(lat-tp.lat)<0.000001) && (fabs(lon-tp.lon)<0.000001); 
	}

	bool isFromOSM() 
	{ 
		return osm_id>0; 
	}

	void setOSMID(int i ) 
	{ 
		osm_id  = i; 
	}

	void setName(const QString& n) 
	{
		name = n;
	}
	void setType(const QString& t) 
	{
		type = t;
	}
	QString getName()
	{
		return name;
	}
	QString getType()
	{
		return type;
	}

	int getOSMID()
	{
		return osm_id;
	}

	void uploadToOSM(const char*,const char*);

	double getLat() { return lat; }
	double getLon() { return lon; }

	void setCoords(double lat,double lon)
		{ this->lat=lat; this->lon=lon; }
	void trackpointToNode();
	QByteArray toOSM();
};

}
#endif
