#ifndef SEGMENT_H
#define SEGMENT_H

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
#include "Node.h"
#include "OSMLinear.h"
#include <qtextstream.h>
#include "RouteMetaDataHandler.h"

namespace OpenStreetMap
{

class Segment : public OSMLinear
{
private:
	Node *nodes[2];
	QString type, name;
	int way_id;
	bool wayStatus;

public:
	Segment()
	{
		nodes[0] = nodes[1] = NULL;
		osm_id = 0;
		name = type = "";
		tags["name"] = "";
		wayStatus = false;
		way_id = 0;
	}

	Segment(Node *n1, Node *n2)
	{
		nodes[0] = n1;
		nodes[1] = n2;
		osm_id = 0;
		name = type = "";
		wayStatus = false;
		way_id = 0;
		tags["name"] = "";
	}

	Segment(int id,Node *n1, Node *n2)
	{
		nodes[0] = n1;
		nodes[1] = n2;
		osm_id = id;
		name = type = "";
		wayStatus = false;
		way_id = 0;
		tags["name"] = "";
	}

	Segment(int id,Node *n1, Node *n2, const QString& n,
					const QString& t)
	{
		nodes[0] = n1;
		nodes[1] = n2;
		osm_id = id;
		setName(n);
		setType(t);	
		wayStatus = false;
		way_id = 0;
	}
	Segment(Node *n1, Node *n2, const QString& n,
					const QString& t)
	{
		nodes[0] = n1;
		nodes[1] = n2;
		osm_id = 0;
		setName(n);
		setType(t);	
		wayStatus = false;
		way_id = 0;
	}

	void toOSM(QTextStream&, bool allUid=false);

	bool contains(Node *n)
	{
		return nodes[0]==n || nodes[1]==n;
	}

	Node* firstNode() 
	{
		return nodes[0];
	}

	Node* secondNode() 
	{
		return nodes[1];
	}


	bool hasNodes()
	{
		return nodes[0] && nodes[1];
	}

	void setWayStatus(bool ws)
	{
		wayStatus = ws;
	}

	// belongs to an actual osm way
	bool belongsToWay()
	{
		return way_id>0;
	}

	// belongs to a way - which may or may not have been added to osm
	bool getWayStatus()
	{
		return wayStatus;
	}

	void setWayID(int id)
	{
		way_id = id;
	}

	int getWayID()
	{
		return way_id;
	}

	double length()
	{
		return OpenStreetMap::dist(nodes[0]->getLat(),nodes[0]->getLon(),
									nodes[1]->getLat(),nodes[1]->getLon() );
	}
	
	// fails to compile without this seemingly pointless code on
	// the version of g++ with mandrake 10.1. 
	QByteArray toOSM() { return OSMObject::toOSM(); }
};

}

#endif
