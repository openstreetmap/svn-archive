#include "TrackSeg.h"

#include <iostream>
using std::endl;

namespace OpenStreetMap
{

void TrackPoint::toGPX(std::ostream& outfile)
{
	outfile << "<trkpt lat=\"" << lat << 
				"\" lon=\"" << lon << "\">"
				<< endl << "<time>"<<timestamp<<"</time>"<<endl
				<<"</trkpt>"<<endl;
}

void TrackSeg::toGPX(std::ostream& outfile)
{
	outfile<<"<trkseg>"<<endl<<"<extensions>"<<endl<<"<type>"
			<<type<<"</type>"<<endl<<"</extensions>"<<endl;

	for(int count=0; count<points.size(); count++)
		points[count].toGPX(outfile);

	outfile<<"</trkseg>"<<endl;
}

bool TrackSeg::deletePoints(int start, int end)
{
	if(start>=0&&start<points.size()&&end>=0&&end<points.size())
	{
		vector<TrackPoint>::iterator i; 
		for(int count=0; count<(end-start)+1; count++)
		{
			i=points.begin()+start;	
			points.erase(i);
		}
		return true;
	}

	return false;
}



SegPointInfo TrackSeg::findNearestTrackpoint(const LatLon& p,double limit)
{
	double dist;
	SegPointInfo a;
	a.point=-1;
	a.dist=limit;
	for(int count=0; count<points.size(); count++)
	{
		if((dist=OpenStreetMap::dist(p.lat,p.lon,
					points[count].lat,points[count].lon))<limit)
		{
			if(dist<a.dist)
			{
				a.dist=dist;
				a.point=count;
			}
		}
	}

	return a;
}

TrackPoint TrackSeg::getPoint (int i) throw (QString)
{
	if(i<0 | i>=points.size())
		throw QString("TrackSeg::getPoint(): index out of range");
	return points[i];
}

}
