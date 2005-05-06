#ifndef MAP_H
#define MAP_H

#include "functions.h"

namespace OpenStreetMap
{

struct ScreenPos
{
	int x,y;

	ScreenPos() { x=y=0; }
	ScreenPos(int x1,int y1) { x=x1; y=y1; }
};

class Map
{
private:
	LatLon topLeft;
	double scale;

public:
	Map(double lat, double lon, double s)
		{ topLeft=LatLon(lat,lon); scale=s; }

	ScreenPos getScreenPos(const LatLon& pos)
		{ return ScreenPos ((pos.lon-topLeft.lon)*scale,
						(topLeft.lat-pos.lat)*scale); }

	ScreenPos getScreenPos(double lat,double lon)
		{ return getScreenPos(LatLon(lat,lon)); }

	LatLon getLatLon(const ScreenPos& pos)
		{ return LatLon(topLeft.lat-((double)pos.y)/scale,
						  topLeft.lon+((double)pos.x)/scale); }

	void move(double edis,double ndis)
		{ topLeft.lon += edis*1000; topLeft.lat += ndis*1000; }

	void rescale(double factor,int w,int h)
	{
		LatLon middle = getCentre ( w,h );
		scale *= factor;
		topLeft.lon = middle.lon - (w/2)/scale;
		topLeft.lat = middle.lat + (h/2)/scale;
	}

	LatLon getCentre(int w,int h)
	{
		return getLatLon(ScreenPos(w/2,h/2));
	}

	LatLon getTopLeftLL()
		{ return topLeft; }

	double getScale()
		{ return scale; }

};

}

#endif
