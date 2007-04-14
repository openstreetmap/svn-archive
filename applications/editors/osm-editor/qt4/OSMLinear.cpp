#include "OSMLinear.h"
#include "RouteMetaDataHandler.h"
//Added by qt3to4:
#include <QTextStream>

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
namespace OpenStreetMap
{

QString OSMLinear::getType()
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

void OSMLinear::setType(const QString& t)
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

}
