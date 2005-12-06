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
#include "OSMParser.h"

#include <iostream>
using namespace std;

namespace OpenStreetMap
{

OSMParser::OSMParser()
{
	curSeg = 0;
	inDoc = false;
	components = new Components;
}

bool OSMParser::startDocument()
{
	//cerr << "startDocument()" << endl;
	inDoc = true;
	return true;
}

bool OSMParser::endDocument()
{
	QString name="", type;
	inDoc = false;
	for(std::map<int,ReadNode>::iterator i=readNodes.begin(); 
					i!=readNodes.end(); i++)
	{
		if(i->second.inSeg == false)
		{
			//cerr << "Found a waypoint" << endl;
			type = "waypoint";

			QStringList tagList = QStringList::split(";" , i->second.tags);
			for(QStringList::Iterator j = tagList.begin();j!=tagList.end();j++)
			{
				if((*j).find("class")==0)
				{
					QStringList keyval = QStringList::split("=", *j);
					type = keyval[1];
				}
				else if((*j).find("name")==0)
				{
					QStringList keyval = QStringList::split("=", *j);
					name = keyval[1];
				}
			}
			//cerr << "name= " << name << endl;
			//cerr << "lat= " << i->second.lat << endl;
			//cerr << "lon= " << i->second.lon << endl;
			//cerr << "type= " << type << endl;
			Waypoint wp(name,i->second.lat,i->second.lon,type);
			wp.osm_id = i->first;
			components->addWaypoint(wp);
		}
	}
	return true;
}

bool OSMParser::startElement(const QString&, const QString&,
							const QString& element,
							const QXmlAttributes& attributes)
{
	double lat, lon;
	int uid, from, to;
	QString tags, type="", name="";

	//cerr << "startElement: element=" << element << endl;
	if(inDoc)
	{
		if(element=="node")
		{
			//cerr << "Found a node" << endl;
			for(int count=0; count<attributes.length(); count++)
			{
				if(attributes.qName(count)=="lat")
					lat = atof(attributes.value(count).ascii());
				else if(attributes.qName(count)=="lon")
					lon = atof(attributes.value(count).ascii());
				else if(attributes.qName(count)=="uid")
					uid = atoi(attributes.value(count).ascii());
				else if (attributes.qName(count)=="tags")
					tags = attributes.value(count);
			}
			//cerr << " lat= " << lat;
			//cerr << " lon= " << lon;
			//cerr << " tags= " << tags;
			//cerr << endl;

			readNodes[uid].lat = lat; 
			readNodes[uid].lon = lon; 
			readNodes[uid].tags = tags; 
		}
		else if(element=="segment")
		{
			//cerr << "Found a segment" << endl;
			uid=0;
			for(int count=0; count<attributes.length(); count++)
			{
				if(attributes.qName(count)=="from")
					from = atoi(attributes.value(count).ascii());
				else if(attributes.qName(count)=="to")
					to = atof(attributes.value(count).ascii());
				else if(attributes.qName(count)=="uid")
					uid = atoi(attributes.value(count).ascii());
				else if(attributes.qName(count)=="tags")
				{
					readTags(attributes.value(count), name, type);
				}
			}


			components->newSegment();

			//cerr << " from= " <<from; 
			//cerr << " to= " << to;
			//cerr << " type= " << type;
			//cerr << endl;

			readNodes[from].inSeg = readNodes[to].inSeg = true;
			components->addTrackpoint(curSeg, 
							TrackPoint
								(readNodes[from].lat,readNodes[from].lon,from));
			components->addTrackpoint(curSeg, 
							TrackPoint
								(readNodes[to].lat,readNodes[to].lon,to));
			components->getSeg(curSeg)->setOSMID(uid);
			components->getSeg(curSeg)->setName(name);
			components->setSegType(curSeg++,type);
		}
	}
	return true;
}

void OSMParser::readTags(const QString &tags,  QString& name,
							 QString& type)
{
	QStringList tagList = QStringList::split(";" , tags);
	QStringList keyval;
	RouteMetaData metaData;
	for(QStringList::Iterator i = tagList.begin(); i!=tagList.end(); i++)
	{
		keyval = QStringList::split("=", *i);
		if(keyval[0] == "foot")
			metaData.foot = keyval[1];
		else if(keyval[0] == "horse")
			metaData.horse = keyval[1];
		else if(keyval[0] == "bike")
			metaData.bike = keyval[1];
		else if(keyval[0] == "car")
			metaData.car = keyval[1];
		else if(keyval[0] == "class")
			metaData.routeClass = keyval[1];
		else if(keyval[0] == "name")
			name = keyval[1];
	}
	RouteMetaDataHandler handler;
	type = handler.getRouteType(metaData);
}


}
