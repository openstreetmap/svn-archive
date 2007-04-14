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
//Added by qt3to4:
#include <QTextStream>


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

int Components2::getNearestTrackPoint (double lat,double lon,double limit)
{
    double mindist=limit, dist;
    int nearest = -1;
    for(int count=0; count<trackpoints.size(); count++)
    {
        if((dist=OpenStreetMap::dist(lat,lon,
                    trackpoints[count]->getLat(),
					trackpoints[count]->getLon()))<limit)
        {
            if(dist<mindist)
            {
                mindist=dist;
                nearest = count;
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
			if(nodes[count]->getType()=="node")
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
    QTextStream stream (xml, QIODevice::WriteOnly);
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
    QTextStream stream (xml, QIODevice::WriteOnly);
    stream<<"<osm version='0.3'>" << endl;
    for(int count=0; count<segments.size(); count++)
    {
        if(segments[count]->getOSMID()<0)
        {
            segments[count]->toOSM(stream);
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

// 130506 removed hacky crap for doing multiple nodes/segs at once - the
// scheduler should now handle this (at least it has been so far....)

// Merges these Components with another set
bool Components2::merge(Components2 *comp)
{
    int segIdx = minSegID(), nodeIdx = minNodeID();
    Node *curNode;
    Segment *curSeg;
	Way *curWay;
	Way *curArea;
	TrackPoint *curTrackPoint;
	for(int count=0; count<comp->nNodes(); count++)
    {
        curNode = comp->getNode(count); 
        if(curNode->getOSMID()<0)
            curNode->setOSMID(--nodeIdx);
        addNode(curNode);
    }
	for(int count=0; count<comp->nSegments(); count++)
    {
        curSeg = comp->getSegment(count);
        if(curSeg->getOSMID()<0)
            curSeg->setOSMID(--segIdx);
       	addSegment(curSeg);
    }
	for(int count=0; count<comp->nWays(); count++)
    {
        curWay = comp->getWay(count);
        if(curWay->getOSMID()<0)
            curWay->setOSMID(--segIdx);
		curWay->setComponents(this); // yeuch - hacky
       	addWay(curWay);
    }
	for(int count=0; count<comp->nAreas(); count++)
    {
        curArea = comp->getArea(count);
        if(curArea->getOSMID()<0)
            curArea->setOSMID(--segIdx);
		curArea->setComponents(this); // yeuch - hacky
       	addArea(curArea);
    }
	for(int count=0; count<comp->nTrackPoints(); count++)
    {
        curTrackPoint = comp->getTrackPoint(count);
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
        segments[count]->toOSM(strm,allUid);
    for(int count=0; count<ways.size(); count++)
        ways[count]->toOSM(strm,allUid);
    for(int count=0; count<areas.size(); count++)
        areas[count]->toOSM(strm,allUid);
    strm << "</osm>";
}

void Components2::removeTrackPoints()
{
    for(vector<Node*>::iterator i=nodes.begin(); i!=nodes.end(); i++)
    {
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
    for(vector<Segment*>::iterator i=segments.begin(); i!=segments.end(); i++)
    {
        if((*i)==s)
        {
            segments.erase(i);
            return true;
        }
    }
    return false;
}

// 'delete' a way 
// note it doesn't actually erase it from memory - just removes it from
// the list
bool Components2::deleteWay(Way *w)
{
    for(vector<Way*>::iterator i=ways.begin(); i!=ways.end(); i++)
    {
        if((*i)==w)
        {
            ways.erase(i);
			for(int count=0; count<segments.size(); count++)
			{
				if(segments[count]->getWayID()==w->getOSMID())
				{
					segments[count]->setWayID(0);
					segments[count]->setWayStatus(false);
				}
			}
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
EarthPoint Components2::getAverageTrackPoint()
{
    EarthPoint avg;
    avg.x=avg.y=0.0;       

    for(int count=0; count<trackpoints.size(); count++)
    {
        avg.y += trackpoints[count]->getLat();
        avg.x += trackpoints[count]->getLon();
    }

    avg.y /= trackpoints.size();
    avg.x /= trackpoints.size();

    return avg;
}

Node *Components2::addOSMNode(int id,double lat, double lon,const QString& name,
            const QString& type, const QString& timestamp)
{
    Node *newNode = new Node(id,lat,lon,name,type,timestamp);
    addNode(newNode);
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

Segment * Components2::addOSMSegment (int id,Node *n1, Node *n2)
{
    n1->trackpointToNode();
    n2->trackpointToNode();
    Segment *seg = new Segment(id,n1,n2);
	addSegment(seg);
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
	int wayID;
	std::pair<Segment*,Segment*>* p = new std::pair<Segment*,Segment*>;
	p->first = new Segment(s->firstNode(),newNode,"",s->getType());
	p->second =	new Segment(newNode,s->secondNode(),"",s->getType());

	segments.push_back(p->first);
	segments.push_back(p->second);

	// If the segment is in a way, remove it from the way and add the two
	// new segments to the way at the appropriate position
	
	// 180506 no longer do this, do separately as it makes code which also
	// uploads the changes to OSM easier

	deleteSegment(s);

	return p;
}

// Saves GPX
// saves trackpoints, and any node not of type "node" or
// "trackpoint" as a waypoint
// intended to be used immediately after reading the data from the GPS

void Components2::toGPX(QTextStream& stream)
{
	stream.setRealNumberPrecision(10); // 250306 in response to request

	stream << "<gpx version=\"1.0\" creator=\"osmeditor2\" "
			<< " xmlns=\"http://www.topografix.com/GPX/1/0\">"<<endl
			<< "<trk>" << endl << "<trkseg>" << endl;

	/*
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
	*/

	for(int count=0; count<trackpoints.size(); count++)
	{
			stream << "<trkpt lat=\""
					<< trackpoints[count]->getLat()
				   << "\" lon=\""
				   << trackpoints[count]->getLon()
				   << "\">" << endl;

			QString timestamp = trackpoints[count]->getTimestamp();

			if(timestamp!="")
				stream << "<time>" << timestamp  << "</time> "<< endl;
	
			stream << "</trkpt>" << endl;
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

Way * Components2::getWayByID(int id)
{
	for(int count=0; count<ways.size(); count++)
	{
		if((ways[count]->getOSMID()==id) && id)
			return ways[count];
	}
	return NULL;
}

Segment * Components2::getSegmentByID(int id)
{
	for(int count=0; count<segments.size(); count++)
	{
		if((segments[count]->getOSMID()==id) && id)
			return segments[count];
	}
	return NULL;
}

Segment *Components2::getNearestSegment(double lat, double lon,double limit)
{
    double mindist=limit, dist, lat1,lon1,lat2,lon2;
    Segment *nearest = NULL;
    for(int count=0; count<segments.size(); count++)
    {
		lat1 = segments[count]->firstNode()->getLat();
		lon1 = segments[count]->firstNode()->getLon();
		lat2 = segments[count]->secondNode()->getLat();
		lon2 = segments[count]->secondNode()->getLon();
	
		// Find distance from point to this segment
        if((dist=OpenStreetMap::distp(lon,lat,lon1,lat1,lon2,lat2))<limit)
        {
            if(dist<mindist)
            {
                mindist=dist;
                nearest = segments[count];
            }
        }
    }
    return nearest;
}

bool Components2::nodeExists(int id)
{
	if(id)
	{
		for(int count=0; count<nodes.size(); count++)
		{
			if(nodes[count]->getOSMID()==id)
				return true;
		}
	}
	return false;
}

bool Components2::segmentExists(int id)
{
	if(id)
	{
		for(int count=0; count<segments.size(); count++)
		{
			if(segments[count]->getOSMID()==id)
				return true;
		}
	}
	return false;
}

bool Components2::wayExists(int id)
{
	if(id)
	{
		for(int count=0; count<ways.size(); count++)
		{
			if(ways[count]->getOSMID()==id)
				return true;
		}
	}
	return false;
}

bool Components2::areaExists(int id)
{
	if(id)
	{
		for(int count=0; count<areas.size(); count++)
		{
			if(areas[count]->getOSMID()==id)
				return true;
		}
	}
	return false;
}

void Components2::deleteTrackPoints(int start, int end)
{
	if(start>=0 && end<trackpoints.size())
	{
    	for(int count=start; count<=end; count++)
    	{
			vector<TrackPoint*>::iterator i=trackpoints.begin()+start;
			delete *i;
			trackpoints.erase(i);
		}
    }
}

}
