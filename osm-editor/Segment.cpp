#include "Segment.h"
#include "RouteMetaDataHandler.h"
#include "curlstuff.h"

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

void Segment::toOSM(QTextStream& outfile)
{
    nodes[0]->toOSM(outfile);
    nodes[1]->toOSM(outfile);
    segToOSM(outfile);
}
// 180306 updated to 0.3
void Segment::segToOSM(QTextStream &outfile, bool allUid)
{
    outfile << "<segment from='"
                << nodes[0]->getOSMID() <<
                   "' to='"
                << nodes[1]->getOSMID() << "' ";
    int sent_id = (osm_id>0 || (allUid&&osm_id)) ? osm_id : 0;
    
    outfile << " id='" << sent_id   << "'>" << endl;

    QString tags = "";

    //  avoid dumping the name too many times
    if(name!="")
        outfile << "<tag k='name' v='" << name << "' />" << endl;

	// Only write out the type if the segment does not belong to a way;
	// if it does, the segment will be assumed to have the same type as the way
	if(!wayStatus)
	{
    	RouteMetaDataHandler mdh;
    	RouteMetaData metaData = mdh.getMetaData(type);
    	if(metaData.foot!="no")
       		outfile << "<tag k='foot' v='" << metaData.foot << "' />" << endl;
   		if(metaData.horse!="no")
       		outfile << "<tag k='horse' v='" << metaData.horse << "' />" << endl;
   		if(metaData.bike!="no")
       		outfile << "<tag k='bike' v='" << metaData.bike << "' />" << endl;
   		if(metaData.car!="no")
       		outfile << "<tag k='car' v='" << metaData.car << "' />" << endl;
   		if(metaData.routeClass!="")
       		outfile << "<tag k='class' v='" << metaData.routeClass << "' />" << 
       		endl;
	}


   outfile << "</segment>" <<endl;


}
// 180306 updated to 0.3
QByteArray Segment::toOSM()
{
    QByteArray xml;
    QTextStream str(xml, IO_WriteOnly);
    str<<"<osm version='0.3'>"<<endl;
    segToOSM(str);
    str<<"</osm>"<<endl;
    str<<'\0';
    return xml;
}

// Upload an existing (or new) segment to OSM

void Segment::uploadToOSM(const char* username, const char* password)
{
    char *nonconst, *resp;
    QString xml="";
    QTextStream str2(&xml, IO_WriteOnly);
    str2 << "<osm version='0.2'>" << endl;
    segToOSM(str2);
    str2 << "</osm>" << endl;
    cerr<<"segstoOSM returned: "<<xml << endl;
    nonconst = new char[ strlen(xml.ascii()) + 1]; 
    strcpy(nonconst,xml.ascii());
    char url[1024];
    // Check it's an existing segment
    if(osm_id>0)
    {
        sprintf(url,"http://www.openstreetmap.org/api/0.2/segment/%d",
                            osm_id);
        resp = put_data(nonconst,url,username,password);
        if(resp) delete[] resp;
        cerr<<"URL:" << url << endl;
    }
    else
    {
        cerr<<"***ADDING A NEW SEGMENT***"<<endl;
        QStringList ids = putToOSM(nonconst,
                    "http://www.openstreetmap.org/api/0.2/newsegment",
                            username,password);
        if(atoi(ids[0].ascii()))
            osm_id = atoi(ids[0].ascii());
    }
    delete[] nonconst;
}

}
