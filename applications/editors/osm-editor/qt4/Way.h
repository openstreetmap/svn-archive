#ifndef WAY_H
#define WAY_H

/*
    Copyright (C) 2006 Nick Whitelegg, Hogweed Software, nick@hogweed.org 

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

// 030706 change internal representation from Segment* to int (seg id)
#include "Segment.h"
#include "RouteMetaDataHandler.h"
#include <qtextstream.h>
#include <vector>
#include "OSMLinear.h"


using std::vector;


namespace OpenStreetMap
{

class Components2;

class Way : public OSMLinear
{
protected:
	vector<int> segments;
	QString type, name, ref;
	Components2 *components;
	bool area;

public:
	Way(Components2 *comp)
	{
		osm_id = 0;
		name = type = ref = "";
		components = comp;
		area = false;
		tags["name"] = tags["ref"] = "";
	}

	void setSegments(vector<Segment*>&);

	void setRef(const QString& r) 
	{
		//ref = r;
		tags["ref"] = r;
	}

	// use tags
	void setType(const QString& t) 
	{
		// use tags
		//type = t;
		if(isArea())
			setAreaType(t);
		else
			OSMLinear::setType(t);
		setSegs();
	}

	QString getType()
	{
		if(isArea())
			return getAreaType();
		else
			return OSMLinear::getType();
	}

	// horribly hacky. The relation of tags to high-level types probably
	// needs to be completely rewritten.
	QString getAreaType()
	{
		// Use tags
		//return type;
		NodeMetaDataHandler mdh;
		QString curType;
		for(std::map<QString,QString>::iterator i=tags.begin(); i!=tags.end();
			i++)
		{
			curType = mdh.getNodeType(i->first,i->second);
			if(curType!="" && curType!="node")
				return curType;
		}
		return "";
	}

	// horribly hacky
	void setAreaType(const QString& t) 
	{
		// Use tags
		NodeMetaDataHandler mdh;
		NodeMetaData md = mdh.getMetaData(t);
		if(md.key!="")
			tags[md.key] = md.value;
		//type = t;
	}

	void setSegs();

	QString getRef()
	{
		//return ref; // eg road number 
		return tags["ref"];
	}

	void toOSM(QTextStream&, bool allUid=false);


	void addSegment (Segment *s)
	{
		if(s->getOSMID())
		{
			// Segments take on the type of the parent way, if it has one
			/* 090706 not any more - no need for this - rendering code looks
			 * at the segment's parent way
			if(type!="")
				s->setType(type);
			*/
			s->setWayStatus(true);
			s->setWayID(osm_id);
			segments.push_back(s->getOSMID());
		}
	}

	// used by parsers when the segment is not in the current area
	void addSegmentID(int i)
	{
		segments.push_back(i);
	}

	int removeSegment(Segment *s);
	bool addSegmentAt(int index, Segment *s);

	void setComponents(Components2 *c) { components=c; }

	//bool isArea() { return area; }

	Segment *getSegment(int i);

	int nSegments() { return segments.size(); }

	//void setArea(bool a) { area=a; }

	Segment *longestSegment();
	
	void setOSMID(int);

	// fails to compile without this seemingly pointless code on
	// the version of g++ with mandrake 10.1. 
	QByteArray toOSM() { return OSMObject::toOSM(); }

	// This really *is* horrible. It will go when high level types to
	// Map Features is rewritten.
	bool isArea()
	{
		std::map<QString,QString>::iterator natural =
				tags.find("natural"),
				landuse = tags.find("landuse"), leisure =
				tags.find("leisure");

		return ((natural!=tags.end() && 
				(natural->second=="water" || natural->second=="heath"))
					||
				(landuse!=tags.end() && landuse->second=="wood") 
					||
				(leisure!=tags.end() && leisure->second=="park") 
				);
	}
};


}

#endif
