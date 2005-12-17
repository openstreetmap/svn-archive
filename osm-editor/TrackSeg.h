#ifndef TRACKSEG_H
#define TRACKSEG_H

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


#include <qstring.h>
#include <qstringlist.h>
#include <vector>
#include <fstream>
#include "functions.h"
#include "EarthPoint.h"

#include <iostream>

using std::vector;
using std::ostream;

#include <cmath>

namespace OpenStreetMap
{

struct TrackPoint
{
	// 10/04/05 now storing the timestamp as the standard GPX format
	QString timestamp;
	double lat, lon;
	int osm_id;

	TrackPoint(){lat=lon=0; timestamp=""; osm_id = 0; }
	TrackPoint(double lt, double ln){lat=lt; lon=ln; timestamp=""; osm_id=0; }
	TrackPoint(const QString& t, double lt, double ln)
		{ timestamp=t; lat=lt; lon=ln; osm_id=0; }
	TrackPoint(double lt, double ln,int i)
		{lat=lt; lon=ln; timestamp=""; osm_id=i; }
	void toGPX(ostream&);
	int toOSM(ostream&);
	bool connected(const TrackPoint& pt, double , double);
	bool operator==(const TrackPoint& tp)
		{ return (fabs(lat-tp.lat)<0.000001) && (fabs(lon-tp.lon)<0.000001); }
	bool isFromOSM() { return osm_id>0; }
	void setOSMID(int i ) { osm_id  = i; }
};

class TrackSeg
{
private:
	QString name, type;
	int id, osm_id;
	vector<TrackPoint> points;
	bool altered;



public:
	TrackSeg() 
		{ name=""; type="track"; id=0; osm_id=0; altered=false; }
	 TrackSeg(const QString& i,const QString& t)
		{ name=i; type=t; id=0; osm_id=0; altered=false; }

	 void setName(const QString& i) { name=i; }
	 void setOSMID(int oid) { osm_id=oid; }
	 bool isFromOSM() { return osm_id > 0; }
	 bool shouldUpload() { return osm_id<=0 || altered; }
	 QString getName() { return name; }
	 QString getType() { return type; }
	 void setAltered(bool a) { altered=a; }
	 bool isAltered() { return altered; }
	 int getOSMID() { return osm_id; }

	
	bool isNew() { return !id; }
	/* UPDATE 151005 not present in Win */
	bool isCheckedOut() { return id>0; }
	bool isLocked() { return id<0; }
	/* END UPDATE */

	void setType(const QString& t) { type=t;  }
	void setId(int i) { cerr<<"TrakcSeg::setId: setting id to "<<i<<endl;id=i; }

	void addPoint(const QString& ts,double lat,double lon) 
		{ points.push_back(TrackPoint(ts,lat,lon)); }

	void addPoint(const TrackPoint& pt)
		{ points.push_back(pt); }

	bool addPoint(const TrackPoint& pt, int pos);
	
	
	void toGPX(ostream&);
	int nodesToOSM(ostream&);
	int newNodesToOSM(ostream& outfile);
	void segsToOSM(ostream&);
	void toOSM(ostream&);
	void uploadToOSM(const char*,const char*);
	void newUploadToOSM(const char*,const char*);
	int findNearestTrackpoint(const EarthPoint& p,double limit,double* = NULL);
	bool deletePoints(int start, int end);

	int nPoints() { return points.size(); }
	TrackPoint getPoint(int i) throw(QString); 

	EarthPoint getAveragePoint() throw(QString);
	void deleteExcessPoints (double angle, double distance);

};

struct PointInfo
{
	TrackSeg *seg;
	int	point;

	PointInfo(TrackSeg* s, int p ) { seg=s; point=p; }
};

class RetrievedTrackPoint
{
private:
	vector<PointInfo> segs;
public:
	int size() const { return segs.size(); }
	void add(TrackSeg* seg,int p) { segs.push_back(PointInfo(seg,p)); }
	int getPointIdx(int i) const { return segs[i].point; }
	TrackSeg *getSeg(int i) const { return segs[i].seg; }
	TrackPoint getPoint (int i) const
		{ return segs[i].seg->getPoint(segs[i].point); }
	bool operator==(const TrackPoint& tp) const;
	void print(){for(int count=0; count<size(); count++) 
					cout<<segs[count].point<<endl; }
	bool null() { return segs.size()==0; }
	static TrackSeg* commonSeg(RetrievedTrackPoint& rtp1,
							RetrievedTrackPoint& rtp2)
		{
			for(int count=0;count<rtp1.segs.size(); count++)
			{
				for(int count2=count; count2<rtp2.segs.size(); count2++)
				{
					if(rtp1.segs[count].seg==rtp2.segs[count2].seg)
						return rtp1.segs[count].seg;
				}
			}
			return NULL;
		}
};


}
#endif /* not TRACKSEG_H */
