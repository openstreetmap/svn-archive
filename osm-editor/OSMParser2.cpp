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
#include "OSMParser2.h"

#include <iostream>
using namespace std;

namespace OpenStreetMap
{

OSMParser2::OSMParser2()
{
	inDoc = false;
	components = new Components2;
}

bool OSMParser2::startDocument()
{
	inDoc = true;
	return true;
}

bool OSMParser2::endDocument()
{
	return true;
}

bool OSMParser2::startElement(const QString&, const QString&,
							const QString& element,
							const QXmlAttributes& attributes)
{
	double lat, lon;
	int uid, from, to;
	QString tags, type="", name="";

	if(inDoc)
	{
		if(element=="node")
		{
			type = "node";
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

			readNodeTags (tags, name, type);

			readNodes[uid] = components->addOSMNode(uid,lat,lon,name,type);
		}
		else if(element=="segment")
		{
			uid=0;
			for(int count=0; count<attributes.length(); count++)
			{
				if(attributes.qName(count)=="from")
				{
					from = atoi(attributes.value(count).ascii());
				}
				else if(attributes.qName(count)=="to")
				{
					to = atof(attributes.value(count).ascii());
				}
				else if(attributes.qName(count)=="uid")
				{
					uid = atoi(attributes.value(count).ascii());
				}
				else if(attributes.qName(count)=="tags")
				{
					readSegTags(attributes.value(count), name, type);
				}
			}

			if(readNodes[from]&&readNodes[to])
			{
			components->addOSMSegment(uid,readNodes[from],readNodes[to],
										name, type);
			}
		}
	}
	return true;
}

void OSMParser2::readSegTags(const QString &tags,  QString& name,
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

void OSMParser2::readNodeTags(const QString& tags, QString& name,QString &type)
{
	QStringList tagList = QStringList::split(";" , tags);
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
}

}
