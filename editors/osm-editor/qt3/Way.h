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


using std::vector;


namespace OpenStreetMap
{

class Components2;

class Way
{
protected:
	vector<int> segments;
	QString type, name, ref;
	int osm_id;
	Components2 *components;
	bool area;
	std::map <QString,QString> tags;

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

	void setName(const QString& n) 
	{
		//name = n;
		tags["name"] = n;
	}
	void setRef(const QString& r) 
	{
		//ref = r;
		tags["ref"] = r;
	}

	// use tags
	void setType(const QString& t);
	void setType1(const QString& t) 
	{
		// use tags
		//type = t;
		RouteMetaDataHandler mdh;
		RouteMetaData md = mdh.getMetaData(t);
		tags["foot"] = md.foot;
		tags["horse"] = md.horse;
		tags["bicycle"] = md.bike;
		tags["motorcar"] = md.car;
		tags["highway"] = md.routeClass;
		if(md.railway!="")
			tags["railway"] = md.railway;
	}
	void setSegs();

	QString getName()
	{
		//return name;
		return tags["name"];
	}
	QString getType()
	{
		// use tags
		//return type;
		RouteMetaDataHandler mdh;
		RouteMetaData md;
		for(std::map<QString,QString>::iterator i=tags.begin(); i!=tags.end();
			i++)
		{
			md.parseKV(i->first, i->second);
		}
		QString t = mdh.getRouteType(md);
		return t;
	}
	QString getRef()
	{
		//return ref; // eg road number 
		return tags["ref"];
	}

	void wayToOSM(QTextStream&, bool allUid=false);

	int getOSMID()
	{
		return osm_id;
	}

	void setOSMID(int i);

	QByteArray toOSM();

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

	bool isArea() { return area; }

	Segment *getSegment(int i);

	int nSegments() { return segments.size(); }

	void setArea(bool a) { area=a; }

	Segment *longestSegment();

	void addTag(QString& k,const QString& v)
	{
		/*
		k = (k=="car") ? "motorcar" : k;
		k = (k=="bike") ? "bicycle" : k;
		*/
		tags[k] = v;
	}
};

typedef Way Area;

}

#endif
