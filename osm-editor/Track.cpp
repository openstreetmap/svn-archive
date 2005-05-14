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
	for(int count=0; count<segs.size(); count++)
		delete segs[count];
}

// Write a track to GPX.
void Track::toGPX(std::ostream &outfile)
{
	outfile<<"<trk>" << endl << "<name>" << id << "</name>" << endl;

	for(int count=0; count<segs.size(); count++)
		segs[count]->toGPX(outfile);

	outfile<<"</trk>"<<endl;
}

vector<SegPointInfo> Track::findNearestSeg(const LatLon& p, double limit)
{
	vector<SegPointInfo>lowest;
	SegPointInfo a;
	double lowestDist = limit;

	for(int count=0; count<segs.size(); count++)
	{
		a=segs[count]->findNearestTrackpoint(p,limit);
		if(a.point>=0 && a.dist<=lowestDist)
		{
			a.seg=count;
			lowest.push_back(a);
			lowestDist=a.dist;
		}
	}
	return lowest;
}

bool Track::deletePoints(const LatLon& p1, const LatLon& p2, double limit)
{
	vector<SegPointInfo> a1 = findNearestSeg(p1,limit), a2 = findNearestSeg(p2,limit);
	for(int count=0; count<a1.size(); count++)
	{
		for(int count2=0; count2<a2.size(); count2++)
		{
			if(a1[count].seg==a2[count2].seg && a1[count].seg>=0)
			{
				segs[a1[count].seg]->deletePoints(a1[count].point,
													a2[count2].point);
				return true;
			}
		}
	}
	return false;
}

bool Track::segmentise(const QString& newType, const LatLon& p1,
						const LatLon& p2, double limit)
{
	vector<SegPointInfo> a1 = findNearestSeg(p1,limit), a2 = findNearestSeg(p2,limit);

	for(int count=0; count<a1.size(); count++)
	{
		for(int count2=0; count2<a2.size(); count2++)
		{
			if(a1[count].seg==a2[count2].seg && a1[count].seg>=0)
			{
				int start = (a1[count].point> a2[count2].point) ? 
								a2[count2].point : a1[count].point, 
					end = (a1[count].point>a2[count2].point) ? 
								a1[count].point: a2[count2].point;
		
				TrackSeg *newSeg=new TrackSeg, *postSeg=new TrackSeg, 
				 		*curSeg = segs[a1[count].seg];

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

bool Track::addTrackpt(int seg,const QString& t, double lat, double lon)
{ 
	if(seg>=0 && seg<segs.size())
	{
		segs[seg]->addPoint(t,lat,lon); 
		return true;
	}
	return false;
}
}
