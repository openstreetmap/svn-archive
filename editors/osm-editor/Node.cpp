#include "Node.h"
#include "NodeMetaDataHandler.h"

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
namespace OpenStreetMap
{

// 180306 updated for 0.3
int Node::toOSM(QTextStream& outfile, bool allUid)
{
    int sent_id = (osm_id>0 || (allUid&&osm_id)) ? osm_id: 0;
	outfile.precision(10); // 250306 in response to request
    outfile << "<node lat='" <<  lat <<
                "' lon='" <<  lon << "'";
    
    outfile << " id='" << sent_id << "' >" << endl;
    
    if(name != "")
        outfile << "<tag k='name' v='" << name << "'/>" << endl;

	// 180506 handle new style keys (ie. not class)
    if(type != "")
	{
		NodeMetaDataHandler mdh;
		NodeMetaData md = mdh.getMetaData(type);
		if(md.key!="" || md.value!="")
        	outfile << "<tag k='"<<md.key<<"' v='" << md.value << "'/>" << endl;
	}
    outfile << "</node>" << endl;
}

// 180306 updated for 0.3
QByteArray Node::toOSM()
{
    QByteArray xml;
    QTextStream str(xml, IO_WriteOnly);
    str<<"<osm version='0.3'>"<<endl;
    toOSM(str);
    str<<"</osm>"<<endl;
    str<<'\0';
    return xml;
}
// 180306 not changed as this is the old curl way of doing it
// 130506 took out the old curl way of doing it

// Used when creating segments from trackpoints
void Node::trackpointToNode()
{
    if(type=="trackpoint")
        type="node";
}

}
