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
#include "Segment.h"
#include <qtextstream.h>
#include <vector>

using std::vector;

namespace OpenStreetMap
{

class Way
{
private:
	vector<Segment*> segments;
	QString type, name;
	int osm_id;

public:
	Way()
	{
		osm_id = 0;
		name = type = "";
	}

	void setSegments(vector<Segment*>&);

	void setName(const QString& n) 
	{
		name = n;
	}
	void setType(const QString& t) 
	{
		type = t;
		// Segments take on the type of the parent way, if it has one
		if(type!="")
		{
			for(int count=0; count<segments.size(); count++)
			{
				segments[count]->setType(type);
				segments[count]->setWayStatus(true);
				segments[count]->setWayStatus(true);
			}
		}
	}

	QString getName()
	{
		return name;
	}
	QString getType()
	{
		return type;
	}

	void wayToOSM(QTextStream&, bool allUid=false);

	int getOSMID()
	{
		return osm_id;
	}

	void setOSMID(int i)
	{
		osm_id = i;
	}

	QByteArray toOSM();

	void addSegment (Segment *s)
	{
		// Segments take on the type of the parent way, if it has one
		if(type!="")
			s->setType(type);
		s->setWayStatus(true);
		segments.push_back(s);
	}
};

}

#endif
