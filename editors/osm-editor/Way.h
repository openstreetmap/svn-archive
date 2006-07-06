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

public:
	Way(Components2 *comp)
	{
		osm_id = 0;
		name = type = ref = "";
		components = comp;
		area = false;
	}

	void setSegments(vector<Segment*>&);

	void setName(const QString& n) 
	{
		name = n;
	}
	void setRef(const QString& r) 
	{
		ref = r;
	}
	void setType(const QString& t); 

	QString getName()
	{
		return name;
	}
	QString getType()
	{
		return type;
	}
	QString getRef()
	{
		return ref; // eg road number 
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
			if(type!="")
				s->setType(type);
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

	int Way::removeSegment(Segment *s);
	bool addSegmentAt(int index, Segment *s);

	void setComponents(Components2 *c) { components=c; }

	bool isArea() { return area; }

	Segment *getSegment(int i);

	int nSegments() { return segments.size(); }

	void setArea(bool a) { area=a; }
};

typedef Way Area;

}

#endif
