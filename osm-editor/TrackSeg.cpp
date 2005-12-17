#include "TrackSeg.h"
#include "SRTMGeneral.h"
#include "RouteMetaDataHandler.h"

#include <ctime>
#include <cmath>

#include <iostream>
#include <iomanip>
#include <sstream>
using std::endl;

using std::cerr;

using std::setprecision;

#include <qstringlist.h>
#include "curlstuff.h"

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
	outfile << "<trkpt lat=\"" << setprecision(10) << lat << 
				"\" lon=\"" << setprecision(10) << lon << "\">"
				<< endl << "<time>"<<timestamp<<"</time>"<<endl
				<<"</trkpt>"<<endl;
}

int TrackPoint::toOSM(std::ostream& outfile)
{
		outfile << "<node lat='" << setprecision(10) << lat << 
				"' lon='" << setprecision(10) << lon << "'";
		if(osm_id)
			outfile << " uid='" << osm_id << "'";
		outfile << " tags=''/>" << endl;
		return 1;
}

// Determines whether two track points are connected, for the purposes of 
// drawing the track. 
// They are determined NOT to be connected if both the time and distance
// between them is greater than the supplied threshold.

bool TrackPoint::connected(const TrackPoint& pt, double timeThreshold,
							double distThreshold)
{
	if(timestamp!="" && pt.timestamp!="")
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
		double spd = distKM/ ((fabs(othertime - time)) / 3600.0);

		// Is the speed less than the threshold speed or distance?
		return fabs(othertime-time) <= timeThreshold || distKM <= distThreshold;
	}
	// If one point has no timestamp, assume it's been inserted later, and
	// that they ARE connected.
	return true;
}


void TrackSeg::toGPX(std::ostream& outfile)
{
		outfile << "<trk>" << endl 
				<< "<number>" << osm_id << "</number>" << endl;
		if(name!="")
			outfile << "<name>" << name << "</name>" << endl;
		outfile << "<extensions>" << endl;
		RouteMetaDataHandler mdh;
		RouteMetaData metaData = mdh.getMetaData(type);
			/*
			outfile << "<foot>" << metaData.foot << "</foot>"<<endl;
			outfile << "<horse>" << metaData.horse << "</horse>"<<endl;
			outfile << "<bike>" << metaData.bike << "</bike>"<<endl;
			outfile << "<car>" << metaData.car << "</car>"<<endl;
			outfile << "<class>" << metaData.routeClass << "</class>"<<endl;
			*/
			outfile << "<property key=\"foot\" value=\"" << metaData.foot << 
					"\"/>"<<endl;
			outfile << "<property key=\"horse\" value=\"" << metaData.horse<< 
					"\"/>"<<endl;
			outfile << "<property key=\"bike\" value=\"" << metaData.bike << 
					"\"/>"<<endl;
			outfile << "<property key=\"car\" value=\"" << metaData.car << 
					"\"/>"<<endl;
			outfile << "<property key=\"class\" value=\"" << 
					metaData.routeClass << "\"/>"<<endl;
		outfile << "</extensions>" << endl;


	outfile<<"<trkseg>"<<endl;
	
	for(int count=0; count<points.size(); count++)
		points[count].toGPX(outfile);


	outfile<<"</trkseg>"<<endl;
	outfile << "</trk>" << endl;
}

void TrackSeg::toOSM(ostream& outfile)
{
	nodesToOSM(outfile);
	segsToOSM(outfile);
}

int TrackSeg::nodesToOSM(ostream& outfile)
{
	int nPts = 0;
	for(int count=0; count<points.size(); count++)
	{
		nPts += points[count].toOSM(outfile);
	}
	return nPts;
}

int TrackSeg::newNodesToOSM(ostream& outfile)
{
	int nPts = 0;
	for(int count=0; count<points.size(); count++)
	{
		if(!points[count].osm_id)
			nPts += points[count].toOSM(outfile);
	}
	return nPts;
}

void TrackSeg::segsToOSM(ostream& outfile)
{
	for(int count=1; count<points.size(); count++)
	{
		//temporary hack to avoid uploading 0 to 0 segs
		if(points[count-1].osm_id && points[count].osm_id)
		{
			outfile << "<segment from='"
				<< points[count-1].osm_id  << 
				   "' to='"
				<< points[count].osm_id << "' ";

			if(osm_id)
				outfile << " uid='" << osm_id   << "' ";

			QString tags;
			//  avoid dumping the name too many times
			if(name!="")
				tags += "name="+name+";";
			RouteMetaDataHandler mdh;
			RouteMetaData metaData = mdh.getMetaData(type);
			if(metaData.foot!="no")
				tags += "foot="+metaData.foot+";";
			if(metaData.horse!="no")
				tags += "horse="+metaData.horse+";";
			if(metaData.bike!="no")
				tags += "bike="+metaData.bike+";";
			if(metaData.car!="no")
				tags += "car="+metaData.car+";";
			if(metaData.routeClass!="")
				tags += "class="+metaData.routeClass;

			outfile << " tags='" << tags << "'/>" << endl;
		}
	}
}

void TrackSeg::newUploadToOSM(const char* username,const char* password)
{
	char *nonconst=NULL, *resp=NULL;

	// Only upload a new segment (one with OSM ID 0)
	if(!osm_id)
	{
		// Get the OSM XML of the nodes in the segment
		cerr<<"Here is the OSM code that would be uploaded:" << endl;
		toOSM(cerr);
		cerr<<"END." << endl;

		std::ostringstream str;
		str << "<osm version='0.2'>" << endl;
		int nPts = newNodesToOSM(str);
		str << "</osm>" << endl;


		// Upload the nodes and receive a list of node IDs
		if(nPts)
		{
			nonconst = new char[ strlen(str.str().c_str()) + 1];	
			strcpy(nonconst,str.str().c_str());

			cerr<<"Putting nodes to OSM"<<endl;
			QStringList ids = putToOSM(nonconst,
					"http://www.openstreetmap.org/api/0.2/newnode",
							username,password);

			cerr<<"done."<<endl;
			delete[] nonconst;

			// Allocate the returned IDs to the new nodes in the segment
			int count=0;
			for(QStringList::Iterator i = ids.begin(); i!=ids.end(); i++)
			{
				if(atoi((*i).ascii()) && !points[count].osm_id) 
					points[count++].setOSMID (atoi((*i).ascii()));
				cerr << "parsing response: count: " << count << 
						" current id: " << (*i) << endl;
			}
		}

		// Form the OSM XML for the segment
		std::ostringstream str2;
		str2 << "<osm version='0.2'>" << endl;
		segsToOSM(str2);
		str2 << "</osm>" << endl;
		cerr<<"segstoOSM returned: "<<str2.str() << endl;
		nonconst = new char[ strlen(str2.str().c_str()) + 1];	
		strcpy(nonconst,str2.str().c_str());

		// Upload the segment
		cerr<<" osm_id is : "<< osm_id << endl;
		QStringList ids=putToOSM(nonconst,
					"http://www.openstreetmap.org/api/0.2/newsegment",
					username,password);

		// Receive the allocated ID
		osm_id = atoi(ids[0]);
		delete[] nonconst;
	}
}

// Upload an existing segment to OSM

void TrackSeg::uploadToOSM(const char* username, const char* password)
{
	// Check it's an existing segment
	if(osm_id)
	{
		char *nonconst, *resp;
		std::ostringstream str2;
		str2 << "<osm version='0.2'>" << endl;
		segsToOSM(str2);
		str2 << "</osm>" << endl;
		cerr<<"segstoOSM returned: "<<str2.str() << endl;
		nonconst = new char[ strlen(str2.str().c_str()) + 1];	
		strcpy(nonconst,str2.str().c_str());
		char url[1024];	
		sprintf(url,"http://www.openstreetmap.org/api/0.2/segment/%d", 
							osm_id);
		resp = put_data(nonconst,url,username,password);
		if(resp) delete[] resp;
		delete[] nonconst;
		cerr<<"URL:" << url << endl;
		altered=false;
	}
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

EarthPoint TrackSeg::getAveragePoint() throw (QString)
{
	EarthPoint avg;

	if(points.size()==0)
		throw QString("Empty TrackSeg");

	for(int count=0; count<points.size(); count++)
	{
		avg.x += points[count].lon;
		avg.y += points[count].lat;
	}

	avg.x /= points.size();
	avg.y /= points.size();


	return avg;
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
