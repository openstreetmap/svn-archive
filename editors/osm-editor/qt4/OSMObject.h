#ifndef OSMOBJECT_H
#define OSMOBJECT_H

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


#include <qstring.h>
//#include <q3cstring.h>
#include <qstringlist.h>
#include <vector>
#include <map>
#include <fstream>
#include "functions.h"
#include "EarthPoint.h"
#include <qtextstream.h>
#include "NodeMetaDataHandler.h"

#include <iostream>

#include <cmath>

using std::ostream;

namespace OpenStreetMap
{

class OSMObject
{
protected:
	int osm_id;
	std::map <QString,QString> tags;

public:
	OSMObject()
	{
		osm_id = 0;
		tags["name"] = "";
	}

	bool isFromOSM() 
	{ 
		return osm_id>0; 
	}

	void setOSMID(int i ) 
	{ 
		osm_id  = i; 
	}

	int getOSMID()
	{
		return osm_id;
	}

	void setName(const QString& n) 
	{
		//name = n;
		tags["name"] = n;
	}

	QString getName()
	{
		//return name;
		return tags["name"];
	}

	void setNote(const QString& n) 
	{
		//name = n;
		tags["note"] = n;
	}

	QString getNote()
	{
		//return name;
		return (tags.find("note")==tags.end()) ? "" : tags["note"];
	}

	void addTag(const QString& k,const QString& v)
	{
		tags[k] = v;
	}
	
	// 180806 don't write out 'no' tags by default
	void writeTags(QTextStream &outfile, bool doNos = false)
	{
		// 080706 all tags written out, not just those of interest to osmeditor2
		for(std::map<QString,QString>::iterator i=tags.begin(); 
						i!=tags.end(); i++)
		{
			if(i->second!="" && (i->second!="no" || doNos==true))
			{
				outfile << "<tag k='"<<i->first<<"' v='" << i->second << "'/>"
				<<endl;
			}
		}

   		if(tags.find("created_by")==tags.end()) 
   			outfile << "<tag k='created_by' v='osmeditor2'/>" << endl;
	}

	// 180306 updated for 0.3
	QByteArray toOSM()
	{
    	QByteArray xml;
		// No longer works in Qt4
    	//QTextStream str(xml, QIODevice::WriteOnly);
		//but this does
		QTextStream str(&xml);
    	str<<"<osm version='0.3'>"<<endl;
    	toOSM(str);
    	str<<"</osm>"<<endl;
    	str<<'\0';
    	return xml;
	}

	virtual void toOSM(QTextStream& strm,bool=false) = 0;

	void printTags()
	{
		
		// 080706 all tags written out, not just those of interest to osmeditor2
		for(std::map<QString,QString>::iterator i=tags.begin(); 
						i!=tags.end(); i++)
		{
			if(i->second!="")
			{
				cerr << "<tag k='"<<i->first.toAscii().constData()<<"' v='" << i->second.toAscii().constData() << "'/>"
				<<endl;
			}
		}

   		if(tags.find("created_by")==tags.end()) 
   			cerr << "<tag k='created_by' v='osmeditor2'/>" << endl;
	}
};

}
#endif
