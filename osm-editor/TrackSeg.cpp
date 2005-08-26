#include "TrackSeg.h"
#include "SRTMGeneral.h"

#include <ctime>

#include <iostream>
using std::endl;

using std::cerr;

namespace OpenStreetMap
{

bool RetrievedTrackPoint::operator==(const TrackPoint& tp) const
{
	for(int count=0; count<segs.size(); count++)
	{
		if(getPoint(count)==tp)
		{
			return true;
		}
	}
	return false;
}

void TrackPoint::toGPX(std::ostream& outfile)
{
	outfile << "<trkpt lat=\"" << lat << 
				"\" lon=\"" << lon << "\">"
				<< endl << "<time>"<<timestamp<<"</time>"<<endl
				<<"</trkpt>"<<endl;
}

// Determines whether two track points are connected.
// A maximum reasonable speed is supplied; if it is not possible to get from
// one track point to the next at the given speed, it's assumed that the two
// points are not connected.

bool TrackPoint::connected(const TrackPoint& pt, double speed)
{
	// Convert the points to grid refs
	EarthPoint gridref = ll_to_gr(lat,lon),
			   otherGridref = ll_to_gr(pt.lat,pt.lon);

	// Get distance in km
	double dx = (otherGridref.x - gridref.x) / 1000,
		   dy = (otherGridref.y - gridref.y) / 1000;

	double distKM = sqrt(dx*dx + dy*dy);

	// Parse the timestamps
	struct tm  this_tm,other_tm;
	
	strptime(timestamp,"%Y-%m-%dT%H:%M:%SZ",&this_tm);
	strptime(pt.timestamp,"%Y-%m-%dT%H:%M:%SZ",&other_tm);

	// Convert to secs since the year dot
	time_t time = mktime(&this_tm), othertime = mktime(&other_tm);

	// Get speed in KM/H
	double spd = distKM/ ((othertime - time) / 3600.0);

	// Is the speed less than the threshold speed?
	return spd<=speed;
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


int TrackSeg::findNearestTrackpoint(const EarthPoint& p,double limit,
				double *dst)
{
	double mindist=limit, dist;
	int pidx=-1;
	for(int count=0; count<points.size(); count++)
	{
		if((dist=OpenStreetMap::dist(p.y,p.x,
					points[count].lat,points[count].lon))<limit)
		{
			if(dist<mindist)
			{
				mindist=dist;
				pidx=count;
			}
		}
	}

	if(dst)
		*dst = mindist;

	return pidx;
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

	double dd;

	// Go through the points in the track from the second to the penultimate
	for(int count=1; count<points.size()-1; count++)
	{
		// Convert the point, and the points either side, to grid ref
		a = ll_to_gr(points[count].lat,points[count].lon);
		b = ll_to_gr(points[count-1].lat,points[count-1].lon);
		c = ll_to_gr(points[count+1].lat,points[count+1].lon);

		// Get the distances in thousandths of km
		da=dist(b.x,b.y,c.x,c.y);
		db=dist(a.x,a.y,c.x,c.y);
		dc=dist(a.x,a.y,b.x,b.y);

		// The minimum distance is used as the criterion
		dd=min(db,dc);

		// Work out the angle (uses cosine rule)
		angleA = getAngle(da,db,dc);

		cerr<<count<<" dc="<<dc<<" angleA="<<angleA<<" angle=" <<angle<<
				"distance*1000 " << distance*1000 << endl;

		// Distance -1 means don't do a distance check
		if(angleA>angle && (distance<0 || dd < distance*1000))
		{
			points.erase(points.begin()+count);
			count--;
			cerr<<"DELETING POINT"<<endl;
		}
	}
}

bool TrackSeg::addPoint(const TrackPoint& p, int pos)
{
	cout << "pos is : "<< pos <<endl;
	if(pos>=-2) cout<<"yes"<<endl;
	int ps=points.size();
	if(pos<ps) 
	{
		cout<<pos<<"is less than"<<ps<<endl;
	}
	else 
	{
		cout << pos << "is greater than" << ps << endl;
	}

	cout << "points.size()"  << ps<<endl;
	if(pos>=-2 && pos<ps)
	{
		cout << "Adding at     "  << (pos+1) << endl;
		vector<TrackPoint>::iterator i=points.begin()+pos+1;
		points.insert(i,p);
		return true;
	}
	return false;
}
		

}
