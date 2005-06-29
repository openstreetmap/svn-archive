#include "TrackSeg.h"

#include <iostream>
using std::endl;

using std::cerr;

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
	outfile<<"<trkseg>"<<endl;
	
	for(int count=0; count<points.size(); count++)
		points[count].toGPX(outfile);
	outfile <<"<extensions>"<<endl<<"<type>"
			<<type<<"</type>"<<endl;
	if(id!="")
		outfile<<"<name>"<<id<<"</name>"<<endl;
	outfile<<"</extensions>"<<endl;


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



SegPointInfo TrackSeg::findNearestTrackpoint(const EarthPoint& p,double limit)
{
	double dist;
	SegPointInfo a;
	a.point=-1;
	a.dist=limit;
	for(int count=0; count<points.size(); count++)
	{
		if((dist=OpenStreetMap::dist(p.y,p.x,
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
	if(i<0 || i>=points.size())
		throw QString("TrackSeg::getPoint(): index out of range");
	return points[i];
}

void TrackSeg::deleteExcessPoints (double angle, double distance)
{
	double da,db,dc, angleA;
	int count=0;
	EarthPoint a, b, c;
	for(int count=1; count<points.size()-1; count++)
	{
		a = ll_to_gr(points[count].lat,points[count].lon);
		b = ll_to_gr(points[count-1].lat,points[count-1].lon);
		c = ll_to_gr(points[count+1].lat,points[count+1].lon);
		da=dist(b.x,b.y,c.x,c.y);
		db=dist(a.x,a.y,c.x,c.y);
		dc=dist(a.x,a.y,b.x,b.y);
		angleA = getAngle(da,db,dc);
		cerr<<count<<" dc="<<dc<<" angleA="<<angleA<<" angle=" <<angle<<
				"distance*1000 " << distance*1000 << endl;

		// Distance -1 means don't do a distance check
		if(angleA>angle && (distance<0 || dc < distance*1000))
		{
			points.erase(points.begin()+count);
			count--;
			cerr<<"DELETING POINT"<<endl;
		}
	}
}
		

}
