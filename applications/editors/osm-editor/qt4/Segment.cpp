#include "Segment.h"
#include "RouteMetaDataHandler.h"
//Added by qt3to4:
#include <QTextStream>

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

// 180306 updated to 0.3
void Segment::toOSM(QTextStream &outfile, bool allUid)
{
    outfile << "<segment from='"
                << nodes[0]->getOSMID() <<
                   "' to='"
                << nodes[1]->getOSMID() << "' ";
    int sent_id = (osm_id>0 || (allUid&&osm_id)) ? osm_id : 0;
    
    outfile << " id='" << sent_id   << "'>" << endl;

	writeTags(outfile);

   	outfile << "</segment>" <<endl;
}

// Upload an existing (or new) segment to OSM
// 130506 removed this old curl way of doing it

}
