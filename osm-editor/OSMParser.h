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
#include "Components.h"
#include "RouteMetaDataHandler.h"

#include <map>

namespace OpenStreetMap 
{

struct ReadNode
{
	double lat,lon;
	QString tags;
	bool inSeg;

	ReadNode() { inSeg=false; }
};

class OSMParser : public QXmlDefaultHandler
{
private:
	bool inDoc;
	int curSeg;
	Components* components;
	std::map<int,ReadNode> readNodes;

public:
	bool startDocument();
	bool endDocument();
	bool startElement(const QString& , const QString&, const QString&,	
									const QXmlAttributes&);


	
	OSMParser();
	Components* getComponents() const { return components; }

	// it's the recipient of the components' responsibility to delete them!
	~OSMParser() {} 
	void readTags(const QString &tags, QString&, QString&);
};

}

#endif
