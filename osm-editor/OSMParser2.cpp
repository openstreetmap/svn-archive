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
	inDoc = inNode = inSegment = inWay = false;
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
	int from, to;
	QString tags; 

	if(inDoc)
	{
		if(element=="node")
		{
			curType = "node";
			curName = "";
			curID = 0;
			inNode = true;
			for(int count=0; count<attributes.length(); count++)
			{
				if(attributes.qName(count)=="lat")
					lat = atof(attributes.value(count).ascii());
				else if(attributes.qName(count)=="lon")
					lon = atof(attributes.value(count).ascii());
				else if(attributes.qName(count)=="id")
					curID = atoi(attributes.value(count).ascii());
			}
			
			readNodes[curID] = components->addOSMNode
					(curID,lat,lon,curName,curType);

		}
		else if(element=="segment")
		{
			curID=0;
			curType = "";
			curName = "";
			metaData.foot=metaData.horse=metaData.bike=metaData.car="no";
			metaData.routeClass="";
			inSegment = true;
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
				else if(attributes.qName(count)=="id")
				{
					curID  = atoi(attributes.value(count).ascii());
				}
			}

			if(readNodes[from]&&readNodes[to])
			{
				readSegments[curID] = components->addOSMSegment
						(curID,readNodes[from],readNodes[to],curName, curType);
			}
		}
		else if (element=="way")
		{
			cerr<<"**** found a way**** " << endl;
			curID=0;
			curType = "";
			curName = "";
			metaData.foot=metaData.horse=metaData.bike=metaData.car="no";
			metaData.routeClass="";
			inWay = true;
			for(int count=0; count<attributes.length(); count++)
			{
				if(attributes.qName(count)=="id")
				{
					curID =  atoi(attributes.value(count).ascii());
					cerr<<"Found an ID: " << curID << endl;
				}
			}
			curWay = new Way;
		}
		else if (element=="seg" && inWay)
		{
			int segID;

			for(int count=0; count<attributes.length(); count++)
			{
				if(attributes.qName(count)=="id")
				{
					segID  = atoi(attributes.value(count).ascii());
				}
			}

			if(readSegments[segID])
			{
				cerr<<"adding segment to way: " << segID << endl;
				curWay->addSegment(readSegments[segID]);
			}

		}
		else if (element=="tag")
		{
			QString key="", value="";

			for(int count=0; count<attributes.length(); count++)
			{
				if(attributes.qName(count)=="k")
					key = attributes.value(count);
				else if (attributes.qName(count)=="v")
					value = attributes.value(count);
			}
			

			if(inNode)
			{
				readNodeTags(key,value,curName,curType);
			}
			else if (inSegment)
			{
				if(key=="name")
					curName = value;
				else
					readSegTags(key,value,metaData);
			}
			else if (inWay)
			{
				if(key=="name")
					curName = value;
				else
					readSegTags(key,value,metaData);
			}
		}
	}
	return true;
}

bool OSMParser2::endElement(const QString&, const QString&,
							const QString& element)
{
	if(element=="node")
	{
		readNodes[curID]->setName(curName);
		readNodes[curID]->setType(curType);
		inNode = false;
	}
	else if (element=="segment")
	{
		RouteMetaDataHandler handler;
		readSegments[curID]->setName(curName);
		readSegments[curID]->setType(handler.getRouteType(metaData));
		inSegment = false;
	}
	else if (element=="way")
	{
		RouteMetaDataHandler handler;
		cerr<<"Adding way to components : type=" << handler.getRouteType(metaData) << endl;
		curWay->setOSMID(curID);
		curWay->setName(curName);
		curWay->setType(handler.getRouteType(metaData));
		components->addWay(curWay);
		inWay = false;
	}
	return true;
}

void OSMParser2::readSegTags(const QString &key, const QString& value,
							RouteMetaData& metaData)
{
	if(key == "foot")
		metaData.foot = value;
	else if(key == "horse")
		metaData.horse = value;
	else if(key == "bike")
		metaData.bike = value;
	else if(key == "car")
		metaData.car = value;
	else if(key == "class")
		metaData.routeClass = value;
}

void OSMParser2::readNodeTags(const QString& key, const QString& value,
									QString& name,QString &type)
{
	if(key=="name")
		name = value;
	else if (key=="class")
		type = value;
}

}
