#ifndef OSMPARSER_H
#define OSMPARSER_H

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

#include <qxml.h>
#include "Components2.h"
#include "RouteMetaDataHandler.h"
#include "NodeMetaDataHandler.h"

#include <map>

namespace OpenStreetMap 
{

class OSMParser2 : public QXmlDefaultHandler
{
private:
	bool inDoc, inNode, inSegment, inWay, inArea;
	Components2* components;
	std::map<int,Node*> readNodes;
	std::map<int,Segment*> readSegments;
	QString curName, curType, curRef;
	int curID;
	RouteMetaData metaData;
	Way *curWay;
	NodeMetaDataHandler nodeHandler;

public:
	bool startDocument();
	bool endDocument();
	bool startElement(const QString& , const QString&, const QString&,	
									const QXmlAttributes&);
	bool endElement(const QString&, const QString&,
							const QString& element);

	
	OSMParser2();
	Components2* getComponents() const { return components; }

	// it's the recipient of the components' responsibility to delete them!
	~OSMParser2() {} 
	static void readSegTags(const QString &key, const QString& value,
							RouteMetaData&);
	void readNodeTags(const QString& key, const QString& value,
									QString& name,QString &type);
};

}

#endif
