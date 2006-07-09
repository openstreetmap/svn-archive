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
#include <map>
#include <fstream>
#include "functions.h"
#include "EarthPoint.h"
#include <qtextstream.h>
#include "NodeMetaDataHandler.h"

#include <iostream>

#include <cmath>

using std::ostream;

namespace OpenStreetMap
{

class Node
{
private:
	double lat, lon;
	int osm_id;
	QString name, type, timestamp;
	std::map <QString,QString> tags;

public:
	Node()
	{
		lat=lon=0; 
		name=type=""; 
		osm_id = 0; 
		timestamp="";
		tags["name"] = "";
	}

	Node(double lt, double ln)
	{
		lat=lt; 
		lon=ln; 
		osm_id=0; 
		name=type=""; 
		timestamp="";
		tags["name"] = "";
	}
	Node(int i,double lt, double ln)
	{
		lat=lt; 
		lon=ln; 
		osm_id=i; 
		name=type=""; 
		timestamp="";
		tags["name"] = "";
	}

	Node(int i,double lt, double ln,const QString& n, const QString& t)
	{
		lat=lt; 
		lon=ln; 
		setName(n);	
		setType(t);	
		osm_id=i; 
		timestamp="";
	}

	Node(double lt, double ln,const QString& n, const QString& t)
	{
		lat=lt; 
		lon=ln; 
		setName(n);
		setType(t);	
		osm_id=0; 
		timestamp="";
	}

	Node(int i,double lt, double ln,const QString& n, const QString& t,
					const QString& ts)
	{
		lat=lt; 
		lon=ln; 
		setName(n);
		setType(t);
		osm_id=i; 
		timestamp=ts;
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
		//name = n;
		tags["name"] = n;
	}
	void setType(const QString& t) 
	{
		// Use tags
		NodeMetaDataHandler mdh;
		NodeMetaData md = mdh.getMetaData(t);
		if(md.key!="")
			tags[md.key] = md.value;
		//type = t;
	}
	QString getName()
	{
		//return name;
		return tags["name"];
	}
	QString getType();

	int getOSMID()
	{
		return osm_id;
	}


	double getLat() { return lat; }
	double getLon() { return lon; }
	QString getTimestamp() { return timestamp; }

	void setCoords(double lat,double lon)
		{ this->lat=lat; this->lon=lon; }
	void trackpointToNode();
	QByteArray toOSM();

	void addTag(const QString& k,const QString& v)
	{
		tags[k] = v;
	}
};

class TrackPoint
{
private:
	double lat, lon; 
	QString timestamp;

public:
	TrackPoint() { lat=lon=0;
				timestamp=""; }
	TrackPoint(double lt, double lg, const QString &ts)
	{
		lat=lt;
		lon=lg;
		timestamp=ts;
	}

	double getLat() { return lat; }
	double getLon() { return lon; }
	QString getTimestamp() { return timestamp; }

};

}
#endif
