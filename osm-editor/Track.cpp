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
#include "Track.h"

#include <cstdio>

using std::endl;


#include <iostream>
using namespace std;

namespace OpenStreetMap
{

Track::~Track()
{
	removeSegs();
}

Track::Track(Track* t)
{
	id=t->id;
	copySegsFrom(t);
}

void Track::copySegsFrom(Track* t)
{
	for(int count=0; count<t->segs.size(); count++)
		segs.push_back(new TrackSeg(*(t->segs[count])));
}

void Track::removeSegs()
{
	for(vector<TrackSeg*>::iterator i=segs.begin(); i!=segs.end(); i++)
	{
		delete *i;
		segs.erase(i);
		i--;
	}
}

void Track::removeSegs(const QString& type)
{
	for(vector<TrackSeg*>::iterator i=segs.begin(); i!=segs.end(); i++)
	{
		if((*i)->getType()==type)
		{
			delete *i;
			segs.erase(i);
			i--;
		}
	}
}

// Write a track to GPX.
void Track::toGPX(std::ostream &outfile)
{
	outfile<<"<trk>" << endl << "<name>" << id << "</name>" << endl;

	for(int count=0; count<segs.size(); count++)
		segs[count]->toGPX(outfile);

	outfile<<"</trk>"<<endl;
}

TrackSeg * Track::findNearestSeg(const EarthPoint& p, double limit)
{
	double lowestDist = 9999, dist;
	int pidx;
	TrackSeg *lowest = NULL;
	TrackPoint pt;

	for(int count=0; count<segs.size(); count++)
	{
		pidx = segs[count]->findNearestTrackpoint(p,9999,&dist);
		if(dist<lowestDist)
		{
			lowest=segs[count];
			lowestDist=dist;
			cout << "**Updating**"<<endl;
		}
	}
	return lowest;
}

bool Track::deletePoints(const RetrievedTrackPoint& p1, const RetrievedTrackPoint& p2,
								double limit)
{
	for(int count=0; count<p1.size(); count++)
	{
		for(int count2=0; count2<p2.size(); count2++)
		{
			if(p1.getSeg(count)==p2.getSeg(count2))
			{
				p1.getSeg(count)->deletePoints(p1.getPointIdx(count),
													p2.getPointIdx(count2));
				return true;
			}
		}
	}
	return false;
}


bool Track::segmentise(const QString& newType, const RetrievedTrackPoint& p1,
						const RetrievedTrackPoint& p2, double limit)
{

	for(int count=0; count<p1.size(); count++)
	{
		for(int count2=0; count2<p2.size(); count2++)
		{	
			if(p1.getSeg(count)==p2.getSeg(count2))
			{
				int start = (p1.getPointIdx(count)> p2.getPointIdx(count2)) ? 
								p2.getPointIdx(count2) : p1.getPointIdx(count), 
					end = (p1.getPointIdx(count)>p2.getPointIdx(count2)) ? 
								p1.getPointIdx(count): p2.getPointIdx(count2);
		
				TrackSeg *newSeg=new TrackSeg, *postSeg=new TrackSeg, 
				 		*curSeg = p1.getSeg(count);

				for(int count=start; count<=end; count++)
					newSeg->addPoint(curSeg->getPoint(count));

				for(int count=end; count<curSeg->nPoints(); count++)
					postSeg->addPoint(curSeg->getPoint(count));
		
				postSeg->setType(curSeg->getType());
				newSeg->setType(newType);		

				curSeg->deletePoints(start+1,curSeg->nPoints()-1);

				segs.push_back(newSeg);
				segs.push_back(postSeg);

				return true;
			}
		}
	}
	return false;
}

bool Track::formNewSeg(const QString& newType, const RetrievedTrackPoint& a1,
						const RetrievedTrackPoint& a2, double limit)
{
	if(a1.size() && a2.size())
	{
		TrackSeg *newSeg = new TrackSeg;
		newSeg->addPoint ( a1.getPoint(0) );
		newSeg->addPoint ( a2.getPoint(0) );
		newSeg->setType(newType);
		segs.push_back(newSeg);
	}
}

bool Track::linkNewPoint(const RetrievedTrackPoint& a1, const RetrievedTrackPoint& a2, 
				const RetrievedTrackPoint & a3,double limit)
{
	for(int count=0; count<a1.size(); count++)
	{
		for(int count2=0; count2<a2.size(); count2++)
		{
			if(a1.getSeg(count)==a2.getSeg(count2) && a3.size()>0)
			{
				TrackPoint p = a3.getPoint(0);
				int i = (a2.getPointIdx(count2) > a1.getPointIdx(count)) ?
							a2.getPointIdx(count2) : a2.getPointIdx(count2)-1; 
				//cout << "i is: " << i << endl;
				cout << "a2.getPointIdx(count2):" << a2.getPointIdx(count2)
													 <<endl;
				cout << "a.getPointIdx(count):" << a1.getPointIdx(count)
													 <<endl;

				a2.getSeg(count2)->addPoint(p,i);
			}
		}
	}
}
				
bool Track::hasPoints()
{
	for(int count=0; count<segs.size(); count++)
	{
		if(segs[count]->nPoints())
			return true;
	}
	return false;
}

bool Track::setSegType(int i,const QString& t)
{ 
	if(i>=0 && i<segs.size())
	{
		segs[i]->setType(t); 
		return true;
	}
	return false;
}
bool Track::setSegId(int i,int id)
{ 
	if(i>=0 && i<segs.size())
	{
		segs[i]->setId(id); 
		return true;
	}
	return false;
}
bool Track::setSegName(int i,const QString& t)
{ 
	if(i>=0 && i<segs.size())
	{
		segs[i]->setName(t); 
		return true;
	}
	return false;
}

bool Track::addTrackpt(int seg,const QString& t, double lat, double lon)
{ 
	if(seg>=0 && seg<segs.size())
	{
		segs[seg]->addPoint(t,lat,lon); 
		return true;
	}
	return false;
}

void Track::deleteExcessPoints (double angle, double distance)
{
	for(int count=0; count<segs.size(); count++)
		segs[count]->deleteExcessPoints (angle, distance);
}

RetrievedTrackPoint Track::findNearestTrackpoint
	(const EarthPoint &pt, double limit)
{
	RetrievedTrackPoint rtp; 
	int tpidx;

	for(int count=0; count<segs.size(); count++)
	{
		tpidx = segs[count]->findNearestTrackpoint(pt,limit);
		if(tpidx>=0)
		{
			rtp.add(segs[count],tpidx);
		}
	}

	return rtp;
}

	


}
