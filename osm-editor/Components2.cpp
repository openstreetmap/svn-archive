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

#include "Components2.h"
#include <iostream>
#include <iomanip>
#include <string>
#include <sstream>

#include "curlstuff.h"

using std::endl;
using std::setw;
using std::setfill;
using std::setprecision;
using std::cerr;

using std::cout;

namespace OpenStreetMap 
{


void Components2::destroy()
{
	for(vector<Node*>::iterator i=nodes.begin(); i!=nodes.end(); i++)
		delete *i;

	for(vector<Segment*>::iterator i=segments.begin(); i!=segments.end(); i++)
		delete *i;
}

Node *Components2::getNearestNode(double lat, double lon,double limit)
{
	double mindist=limit, dist;
	Node *nearest = NULL;
	for(int count=0; count<nodes.size(); count++)
	{
		if((dist=OpenStreetMap::dist(lat,lon,
					nodes[count]->getLat(),nodes[count]->getLon()))<limit)
		{
			if(dist<mindist)
			{
				mindist=dist;
				nearest = nodes[count];
			}
		}
	}
	return nearest;
}

vector<Node*> Components2::getNearestNodes(double lat, double lon,double limit)
{
	vector<Node*> nearestNodes;	
	double dist;
	for(int count=0; count<nodes.size(); count++)
	{
		if((dist=OpenStreetMap::dist(lat,lon,
					nodes[count]->getLat(),nodes[count]->getLon()))<limit)
		{
			nearestNodes.push_back(nodes[count]);
		}
	}
	return nearestNodes;
}

Segment* Components2::getSeg(vector<Node*> &n1, vector<Node*> &n2)
{
	bool found;
	for(int count=0; count<segments.size(); count++)
	{
		found = false;

		for(int count2=0; count2<n1.size(); count2++)
		{
			if (segments[count]->contains(n1[count2]))
			{
				found=true;
				break;
			}
		}

		if(found)
		{
			for(int count2=0; count2<n2.size(); count2++)
			{
				if (segments[count]->contains(n2[count2]))
					return segments[count];
			}
		}
	}
	return NULL;
}

void Components2::newUploadToOSM(const char* username,const char* password)
{
	char *nonconst=NULL, *resp=NULL;

	int nPts = 0;

	QString xml="";
	QTextStream stream (&xml, IO_WriteOnly);
	stream<<"<osm version='0.2'>";
	for(int count=0; count<nodes.size(); count++)
	{
		if(nodes[count]->getOSMID()<0)
		{
			nodes[count]->toOSM(stream);
			nPts++;
		}
	}
	stream<<"</osm>";

	// Upload the nodes and receive a list of node IDs
	// Note we do not call Node::uploadToOSM() as we want to send all
	// of them as a batch (to save time)
	if(nPts)
	{
		cerr<<"Nodes: XML to be uploaded: "<<xml << endl;
		nonconst = new char[ strlen(xml.ascii()) + 1];	
		strcpy(nonconst,xml.ascii());

		cerr<<"Putting nodes to OSM"<<endl;
		QStringList ids = putToOSM(nonconst,
					"http://www.openstreetmap.org/api/0.2/newnode",
							username,password);

		cerr<<"done."<<endl;
		delete[] nonconst;

		// Allocate the returned IDs to the new nodes in the segment
		QStringList::Iterator i=ids.begin();
		for(int count=0; count<nodes.size(); count++)
		{
			if(nodes[count]->getOSMID()<0 && i!=ids.end() && 
				atoi((*i).ascii())) 
			{
				nodes[count]->setOSMID (atoi((*i).ascii()));
				i++;
			}
			cerr << "parsing response: count: " << count << 
						" allocated id: " << (*i) << endl;
		}

		// Upload the segments
		for(int count=0; count<segments.size(); count++)
		{
			// We have to upload one at a time as the newsegment API
			// does not yet have one ID per line
			if(segments[count]->getOSMID()<0)
				segments[count]->uploadToOSM(username,password);
		}
	}
}

// Merges these Components with another set
bool Components2::merge(Components2 *comp)
{
	comp->rewindNodes();
	while(!comp->endNode())
	{
		nodes.push_back(comp->nextNode());
	}
	comp->rewindSegments();
	while(!comp->endSegment())
	{
		segments.push_back(comp->nextSegment());
	}

	return true;
}

void Components2::toOSM(QTextStream &strm, bool allUid)
{
	strm << "<osm version='0.2'>";
	for(int count=0; count<nodes.size(); count++)
		nodes[count]->toOSM(strm,allUid);
	for(int count=0; count<segments.size(); count++)
		segments[count]->segToOSM(strm,allUid);
	strm << "</osm>";
}

void Components2::removeTrackPoints()
{
	for(vector<Node*>::iterator i=nodes.begin(); i!=nodes.end(); i++)
	{
		cerr<<(*i)->getType() << endl;
		if((*i)->getType()=="trackpoint")
		{
			delete *i;
			nodes.erase(i);
			i--;
		}
	}
}

// 'delete' a node
// note it doesn't actually erase it from memory - just removes it from
// the list
bool Components2::deleteNode(Node *n)
{
	for(vector<Node*>::iterator i=nodes.begin(); i!=nodes.end(); i++)
	{
		if((*i)==n)
		{
			nodes.erase(i);
			return true;
		}
	}
	return false;
}

// 'delete' a segment
// note it doesn't actually erase it from memory - just removes it from
// the list
bool Components2::deleteSegment(Segment *s)
{
	cerr<<"in deleteSegments()"<<endl;
	cerr<<"Segment: osmid=" << s->getOSMID() << endl;
	for(vector<Segment*>::iterator i=segments.begin(); i!=segments.end(); i++)
	{
		cerr<<"Trying segment: " << (*i)->getOSMID() << endl;
		if((*i)==s)
		{
			cerr<<"****FOUND THE SEGMENT****" << endl;
			segments.erase(i);
			return true;
		}
	}
	return false;
}

EarthPoint Components2::getAveragePoint() 
{
	EarthPoint avg;
	avg.x=avg.y=0.0;	

	for(int count=0; count<nodes.size(); count++)
	{
		avg.y += nodes[count]->getLat();
		avg.x += nodes[count]->getLon();
	}

	avg.y /= nodes.size();
	avg.x /= nodes.size();

	return avg;
}

}
