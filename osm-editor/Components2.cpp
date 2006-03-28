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
// 180306 updated for 0.3    
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

    for(vector<Way*>::iterator i=ways.begin(); i!=ways.end(); i++)
        delete *i;
}

// 260306 distinguish trackpoints, they are only there for referencing the
// GPS trace - the user has to create nodes.
// Really, they should be put in a class of their own (on the TODO list)

Node *Components2::getNearestNode(double lat, double lon,double limit)
{
    double mindist=limit, dist;
    Node *nearest = NULL;
    for(int count=0; count<nodes.size(); count++)
    {
        if((dist=OpenStreetMap::dist(lat,lon,
                    nodes[count]->getLat(),nodes[count]->getLon()))<limit)
        {
            if(dist<mindist && nodes[count]->getType()!="trackpoint")
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

// find the segment which is common to two nodes
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

// get a vector of segments containing a node
vector<Segment*> Components2::getSegs(Node * n)
{
    vector<Segment*> foundSegs;

    for(int count=0; count<segments.size(); count++)
    {
        if(segments[count]->contains(n))
            foundSegs.push_back(segments[count]);
    }
    return foundSegs;
}

// gets the new nodes as XML
QByteArray Components2::getNewNodesXML()
{
    QByteArray xml;
    QTextStream stream (xml, IO_WriteOnly);
    stream<<"<osm version='0.3'>" << endl;
    for(int count=0; count<nodes.size(); count++)
    {
        if(nodes[count]->getOSMID()<0)
        {
            nodes[count]->toOSM(stream);
        }
    }
    stream<<"</osm>";
    return xml;
}

// gets the new segments as XML
QByteArray Components2::getNewSegmentsXML()
{
    QByteArray xml;
    QTextStream stream (xml, IO_WriteOnly);
    stream<<"<osm version='0.3'>" << endl;
    for(int count=0; count<segments.size(); count++)
    {
        if(segments[count]->getOSMID()<0)
        {
            segments[count]->segToOSM(stream);
        }
    }
    stream<<"</osm>" <<endl;
    return xml;
}

// gets the new nodes as a vector
vector<Node*> Components2::getNewNodes()
{
	vector<Node*> newNodes;
    for(int count=0; count<nodes.size(); count++)
    {
        if(nodes[count]->getOSMID()<0)
            newNodes.push_back(nodes[count]);
    }
	return newNodes;
}

// gets the new segments as a vector
vector<Segment*>  Components2::getNewSegments()
{
	vector<Segment*> newSegments;
    for(int count=0; count<segments.size(); count++)
    {
        if(segments[count]->getOSMID()<0)
			newSegments.push_back(segments[count]);
    }
	return newSegments;
}

// allocate new node IDs to new segments.
// This is called after the new nodes have been created in OSM when we do
// a batch upload of new nodes and segments.
// This is hacky in the extreme but is a stopgap measure as there will be
// a server routine to take a load of new nodes and segments and add them to
// the database.

void Components2::hackySetNodeIDs(QStringList& ids)
{
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
    }
}

// WARNING!! Very hacky!
// Assumes segment IDs will be 6-digit.
void Components2::hackySetSegIDs(const QString& segs)
{
    // Allocate the returned IDs to the new nodes in the segment
    int index = 0;
    for(int count=0; count<segments.size(); count++)
    {
        if(segments[count]->getOSMID()<0 && index<=segs.length()-6)
        {
            int id = atoi(segs.mid(index, 6).ascii());
            if(id>=100000&&id<=999999)
                segments[count]->setOSMID (id);
            index+=6;
        }
    }
}

// Merges these Components with another set
bool Components2::merge(Components2 *comp)
{
    int segIdx = minSegID(), nodeIdx = minNodeID();
    Node *curNode;
    Segment *curSeg;
	Way *curWay;
	TrackPoint *curTrackPoint;
    comp->rewindNodes();
    while(!comp->endNode())
    {
        curNode = comp->nextNode();
        if(curNode->getOSMID()<0)
            curNode->setOSMID(--nodeIdx);
        nodes.push_back(curNode);
    }
    comp->rewindSegments();
    while(!comp->endSegment())
    {
        curSeg = comp->nextSegment();
        if(curSeg->getOSMID()<0)
            curSeg->setOSMID(--segIdx);
        segments.push_back(curSeg);
    }
    comp->rewindWays();
    while(!comp->endWay())
    {
        curWay = comp->nextWay();
        if(curWay->getOSMID()<0)
            curWay->setOSMID(--segIdx);
       	ways.push_back(curWay);
    }
    comp->rewindTrackPoints();
    while(!comp->endTrackPoint())
    {
        curTrackPoint = comp->nextTrackPoint();
       	trackpoints.push_back(curTrackPoint);
    }

    nextNodeId = nodeIdx-1;
    nextSegId = segIdx-1;

    return true;
}

void Components2::toOSM(QTextStream &strm, bool allUid)
{
    strm << "<osm version='0.3'>" << endl;
    for(int count=0; count<nodes.size(); count++)
        nodes[count]->toOSM(strm,allUid);
    for(int count=0; count<segments.size(); count++)
        segments[count]->segToOSM(strm,allUid);
	cerr<<"size of ways=" << ways.size() << endl;
    for(int count=0; count<ways.size(); count++)
        ways[count]->wayToOSM(strm,allUid);
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

Node *Components2::addOSMNode(int id,double lat, double lon,const QString& name,
            const QString& type, const QString& timestamp)
{
    Node *newNode = new Node(id,lat,lon,name,type,timestamp);
    nodes.push_back(newNode);
    if(id-1<nextNodeId)
        nextNodeId = id-1;
    return newNode;
}

TrackPoint *Components2::addTrackPoint(double lat, double lon,
				const QString& timestamp)
{
    TrackPoint *newTP = new TrackPoint(lat,lon,timestamp);
    trackpoints.push_back(newTP);
    return newTP;
}

Segment * Components2::addOSMSegment (int id,Node *n1, Node *n2,
                                        const QString& name,const QString& type)
{
    n1->trackpointToNode();
    n2->trackpointToNode();
    Segment *seg = new Segment(id,n1,n2,name,type);
    segments.push_back(seg);
    if(id-1<nextSegId)
        nextSegId = id-1;
    return seg;
}

int Components2::minNodeID()
{
    int id=0;
    for(int count=0; count<nodes.size(); count++)
    {
        if(nodes[count]->getOSMID() < id)
            id = nodes[count]->getOSMID();
    }
    return id;
}

int Components2::minSegID()
{
    int id=0;
    for(int count=0; count<segments.size(); count++)
    {
        if(segments[count]->getOSMID() < id)
            id = segments[count]->getOSMID();
    }
    return id;
}

// splits a segment at a point defined by a node
// NB does dynamic allocation - the pair must be freed somewhere!
std::pair<Segment*,Segment*>*
	Components2::breakSegment(Segment *s, Node *newNode)
{
	std::pair<Segment*,Segment*>* p = new std::pair<Segment*,Segment*>;
	p->first = new Segment(s->firstNode(),newNode,"",s->getType());
	p->second =	new Segment(s->secondNode(),newNode,"",s->getType());

	segments.push_back(p->first);
	segments.push_back(p->second);
	deleteSegment(s);

	return p;
}

// Saves GPX
// saves trackpoints, and any node not of type "node" or
// "trackpoint" as a waypoint
// intended to be used immediately after reading the data from the GPS

void Components2::toGPX(QTextStream& stream)
{
	stream.precision(10); // 250306 in response to request

	stream << "<gpx version=\"1.0\" creator=\"osmeditor2\" "
			<< " xmlns=\"http://www.topografix.com/GPX/1/0\">"<<endl
			<< "<trk>" << endl << "<trkseg>" << endl;

	for(int count=0; count<nodes.size(); count++)
	{
		if(nodes[count]->getType()=="trackpoint")
		{
			stream << "<trkpt lat=\""
					<< nodes[count]->getLat()
				   << "\" lon=\""
				   << nodes[count]->getLon()
				   << "\">" << endl;

			QString timestamp = nodes[count]->getTimestamp();

			if(timestamp!="")
				stream << "<time>" << timestamp  << "</time> "<< endl;
	
			stream << "</trkpt>" << endl;
		}
	}
	stream << "</trkseg>" << endl << "</trk>" << endl;
	for(int count=0; count<nodes.size(); count++)
	{
		if(nodes[count]->getType()!="trackpoint" &&
			nodes[count]->getType()!="node")
		{
			stream << "<wpt lat=\""
					<< nodes[count]->getLat()
				   << "\" lon=\""
				   << nodes[count]->getLon()
				   << "\">" << endl;

			if(nodes[count]->getName()!="")
				stream << "<name>" << nodes[count]->getName() << "</name> "
						<< endl;
			if(nodes[count]->getType()!="")
				stream << "<type>" << nodes[count]->getType() << "</type> "
						<< endl;
	
			stream << "</wpt>" << endl;
		}
	}
	stream << "</gpx>" << endl;
}
}
