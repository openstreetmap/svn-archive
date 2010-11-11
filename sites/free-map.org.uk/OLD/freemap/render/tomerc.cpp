#include <cmath>
#include "Map.h"
#include "tomerc.h"

EarthPoint lltomerc(const EarthPoint& ep)
{
	EarthPoint ep2;
	ep2.x = lonToMerc(ep.x);
	ep2.y = latToMerc(ep.y);
	return ep2;
}

EarthPoint merctoll(const EarthPoint& ep)
{
	EarthPoint ep2;
	ep2.x = mercToLon(ep.x);
	ep2.y = mercToLat(ep.y);
	return ep2;
}


// www.upcnet.es/~jgc2/docs/mercator.html
double lonToMerc(double lon)
{
    lon *= (M_PI/180);
	double a = 6378137;
	return a*lon;
}

double latToMerc(double lat)
{
	lat *= (M_PI/180);
	double a = 6378137;
	double b = 6356752.3142;
	double f=  (a-b)/a;
	double e = sqrt(2*f-pow(f,2));

	double res = a*log( tan(M_PI/4 + lat/2) *
				pow(( (1-e*sin(lat)) / (1 +e *sin(lat))) , e/2) );

	return res;
}

double mercToLon(double merc_x)
{
	return (merc_x/6378137.0)*(180.0/M_PI);
}

double mercToLat(double merc_y)
{
	double a = 6378137.0;
	double b = 6356752.3142;
	double t = 1.0 - b/a;
	double es=2*t -t*t;
	double e = sqrt(es);
	double lat = phi2(exp(-merc_y/a),e);
	return lat*(180.0/M_PI);
}

double phi2(double ts, double e)
{
	double eccnth=0.5*e;
	double phi = (M_PI/2) - 2.0*atan(ts);
	double dphi;
	double con;
	int i=15;
	do
	{
		con=e*sin(phi);
		dphi = (M_PI/2) - 2.0*atan(ts*pow((1.0-con)/(1.0+con),eccnth))-phi;
		phi+=dphi;
	}
	while(abs(dphi)>0.0000000001 && --i);
	return phi;
}
