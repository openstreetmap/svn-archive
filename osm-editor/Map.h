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
	GridRef topLeft;
	double scale;

public:
	Map(double lat, double lon, double s)
		{ topLeft=ll_to_gr(lat,lon); scale=s; }

	ScreenPos getScreenPos(const GridRef& pos)
		{ return ScreenPos ((pos.e-topLeft.e)*scale,
						(topLeft.n-pos.n)*scale); }

	ScreenPos getScreenPos(double lat,double lon)
		{ return getScreenPos(ll_to_gr(lat,lon)); }

	GridRef getGridRef(const ScreenPos& pos)
		{ return GridRef(topLeft.e+((double)pos.x)/scale,
						  topLeft.n-((double)pos.y)/scale); }

	LatLon getLatLon(const ScreenPos& pos)
		{ return gr_to_ll(getGridRef(pos)); }

	void move(double edis,double ndis)
		{ topLeft.e += edis*1000; topLeft.n += ndis*1000; }

	void rescale(double factor,int w,int h)
	{
		GridRef middle = getGridRef ( ScreenPos (w/2,h/2) );
		scale *= factor;
		topLeft.e = middle.e - (w/2)/scale;
		topLeft.n = middle.n + (h/2)/scale;
	}

	GridRef getTopLeftGR()
		{ return topLeft; }

	LatLon getTopLeftLL()
		{ return gr_to_ll(topLeft); }

	double getScale()
		{ return scale; }
};
			
}

#endif
