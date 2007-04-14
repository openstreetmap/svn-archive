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
	inDoc = inNode = inSegment = inWay = inArea = false;
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
						(curID,readNodes[from],readNodes[to]);
			}
		}
		else if (element=="way" || element=="area")
		{
			cerr<<"**** found a way**** " << endl;
			curID=0;
			curType = "";
			curName = "";
			curRef = "";
			metaData.foot=metaData.horse=metaData.bike=metaData.car="no";
			metaData.routeClass="";
			if(element=="way") inWay = true;
			if(element=="area") inArea = true;
			for(int count=0; count<attributes.length(); count++)
			{
				if(attributes.qName(count)=="id")
				{
					curID =  atoi(attributes.value(count).ascii());
					cerr<<"Found an ID: " << curID << endl;
				}
			}
			curWay = (element=="area") ? new Area(components):
										 new Way(components);
			curWay->setOSMID(curID);
		}
		else if (element=="seg" && (inWay||inArea))
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
			else
			{
				curWay->addSegmentID(segID);
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
			

			// Yes - readNodeTags() for an area - hacky I know. Just want a
			// quick and dirty proof of concept of areas for now.
			// This will be sorted out!
			if(inNode || inArea)
			{
				//readNodeTags(key,value,curName,curType);
				if(inNode && readNodes[curID])
				{
					cerr << "NODE: key=" << key << " value=" << value <<
							" curID=" << curID << endl;
					readNodes[curID]->addTag(key,value);
				}
				else if (inArea && curWay)
					curWay->addTag(key,value);
			}
			else if (inSegment)
			{
				if(key=="name")
					curName = value;
				/*
				else
					readSegTags(key,value,metaData);
					*/
				if(readSegments[curID])
					readSegments[curID]->addTag(key,value);
			}
			else if (inWay)
			{
				if(key=="name")
					curName = value;
				else if(key=="ref")
					curRef = value;
				/*
				else
					readSegTags(key,value,metaData);
					*/
				if(curWay)
				{
					cerr << "adding tag to current way: key=" << key <<
								" value=" << value << endl;
					curWay->addTag(key,value);
				}
			}
		}
	}
	return true;
}

bool OSMParser2::endElement(const QString&, const QString&,
							const QString& element)
{
	cerr << "endElement: element=" << element << endl;
	if(element=="node")
	{
		//readNodes[curID]->setName(curName);
		//readNodes[curID]->setType(curType);
		inNode = false;
	}
	else if (element=="segment")
	{
		RouteMetaDataHandler handler;
		//readSegments[curID]->setName(curName);
		//readSegments[curID]->setType(handler.getRouteType(metaData));
		inSegment = false;
	}
	else if (element=="way") 
	{
		RouteMetaDataHandler handler;
		//curWay->setName(curName);
		//curWay->setRef(curRef);
		//curWay->setType(handler.getRouteType(metaData));
		components->addWay(curWay);
		inWay = false;
	}
	else if (element=="area")
	{
		//curWay->setName(curName);
		//curWay->setType(curType);
		components->addArea(curWay);
		inArea = false;
	}
	return true;
}

// 130506 reads new style keys such as 'bicycle' and 'highway'
void OSMParser2::readSegTags(const QString &key, const QString& value,
							RouteMetaData& metaData)
{
	if(key == "foot")
		metaData.foot = value;
	else if(key == "horse")
		metaData.horse = value;
	else if(key == "bike" || key == "bicycle")
		metaData.bike = value;
	else if(key == "car" || key == "motorcar")
		metaData.car = value;
	else if(key == "class" || key == "highway")
		metaData.routeClass = value;
}

void OSMParser2::readNodeTags(const QString& key, const QString& value,
									QString& name,QString &type)
{
	if(key=="name")
		name = value;
	else if(key=="class")
		type = value;
	else if (nodeHandler.keyExists(key))
		type=nodeHandler.getNodeType(key,value);
}

}
