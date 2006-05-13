#include "Way.h"
#include "RouteMetaDataHandler.h"

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

void Way::setSegments(vector<Segment*>& s)
{
	segments.clear();

	for(int count=0; count<s.size(); count++)
	{
		addSegment(s[count]);
	}
}

void Way::wayToOSM(QTextStream &outfile, bool allUid)
{
    outfile << "<way";
    int sent_id = (osm_id>0 || (allUid&&osm_id)) ? osm_id : 0;
    outfile << " id='" << sent_id   << "'>" << endl;


    QString tags = "";

    //  avoid dumping the name too many times
    if(name!="")
        outfile << "<tag k='name' v='" << name << "' />" << endl;

	// 130506 use new style tags e.g. highway
    RouteMetaDataHandler mdh;
    RouteMetaData metaData = mdh.getMetaData(type);
    if(metaData.foot!="no")
       outfile << "<tag k='foot' v='" << metaData.foot << "' />" << endl;
   if(metaData.horse!="no")
       outfile << "<tag k='horse' v='" << metaData.horse << "' />" << endl;
   if(metaData.bike!="no")
       outfile << "<tag k='bicycle' v='" << metaData.bike << "' />" << endl;
   if(metaData.car!="no")
       outfile << "<tag k='motorcar' v='" << metaData.car << "' />" << endl;
   if(metaData.routeClass!="")
       outfile << "<tag k='highway' v='" << metaData.routeClass << "' />" << 
       endl;
	if(metaData.railway!="")
		outfile << "<tag k='railway' v='" << metaData.railway << 
			"' />" << endl;

   outfile << "<tag k='created_by' v='osmeditor2'/>" << endl;

	for(int count=0; count<segments.size(); count++)
	{
		outfile << "<seg id='" << segments[count]->getOSMID() << "' />" << endl;
	}

   outfile << "</way>" <<endl;

}
// 180306 updated to 0.3
QByteArray Way::toOSM()
{
    QByteArray xml;
    QTextStream str(xml, IO_WriteOnly);
    str<<"<osm version='0.3'>"<<endl;
    wayToOSM(str);
    str<<"</osm>"<<endl;
    str<<'\0';
    return xml;
}

// remove a segment - returns its position
int Way::removeSegment(Segment *s)
{
	for(vector<Segment*>::iterator i=segments.begin(); i!=segments.end(); i++)
	{
		if(*i==s)
		{
			int index = i-segments.begin();
			segments.erase(i);
			return index;
		}
	}
	return -1;
}

// Insert a segment at position 'index'
bool Way::addSegmentAt(int index, Segment *s)
{
	vector<Segment*>::iterator i = segments.begin() + index;
	if(i!=segments.end())
	{
		segments.insert(i,s);
		return true;
	}
	return false;
}

}
