#include "Map/Projection.h"
#include "Map/Coord.h"

#include <QtCore/QRect>

#include <math.h>

// from wikipedia
#define EQUATORIALRADIUS 6378137
#define POLARRADIUS      6356752
#define PI 3.14159265

Projection::Projection(void)
: ScaleLat(1000000), DeltaLat(0), ScaleLon(1000000), DeltaLon(0),
  Viewport(Coord(0,0),Coord(0,0))
{
}

Projection::~Projection(void)
{
}

double Projection::pixelPerM() const
{
	double LatAngPerM = 1.0/EQUATORIALRADIUS;
	return LatAngPerM*ScaleLat;
}

double Projection::latAnglePerM() const
{
	double LengthOfOneDegreeLat = EQUATORIALRADIUS*PI/180;
	return 1/LengthOfOneDegreeLat;
}

double Projection::lonAnglePerM(double Lat) const
{
	double LengthOfOneDegreeLat = EQUATORIALRADIUS*PI/180;
	double LengthOfOneDegreeLon = LengthOfOneDegreeLat*fabs(cos(Lat));
	return 1/LengthOfOneDegreeLon;
}


void Projection::setViewport(const CoordBox& Map, const QRect& Screen)
{
	Coord Center(Map.center());
	double LengthOfOneDegreeLat = EQUATORIALRADIUS*PI/180;
	double LengthOfOneDegreeLon = LengthOfOneDegreeLat*fabs(cos(Center.lat()));
	double Aspect = LengthOfOneDegreeLon/LengthOfOneDegreeLat;
	ScaleLon = Screen.width()/Map.lonDiff()*.9;
	ScaleLat = Screen.height()/Map.latDiff()*.9;
	if (ScaleLon/Aspect > ScaleLat)
		ScaleLon = ScaleLat*Aspect;
	else
		ScaleLat = ScaleLon/Aspect;
	double PLon = Center.lon()*ScaleLon;
	double PLat = Center.lat()*ScaleLat;
	DeltaLon = Screen.width()/2 - PLon;
	DeltaLat = Screen.height()-(Screen.height()/2 - PLat);
	Viewport = CoordBox(inverse(Screen.bottomLeft()),inverse(Screen.topRight()));
}

void Projection::panScreen(const QPoint& p, const QRect& Screen)
{
	DeltaLon += p.x();
	DeltaLat += p.y();
	Viewport = CoordBox(inverse(Screen.bottomLeft()),inverse(Screen.topRight()));
}

QPointF Projection::project(const Coord& Map) const
{
	return QPointF(Map.lon()*ScaleLon + DeltaLon, - Map.lat()*ScaleLat + DeltaLat);
}

Coord Projection::inverse(const QPointF& Screen) const
{
	return Coord(-(Screen.y()-DeltaLat)/ScaleLat, (Screen.x()-DeltaLon)/ScaleLon );
}

CoordBox Projection::viewport() const
{
	return Viewport;
}

void Projection::zoom(double d, const QRect& Screen)
{
	Coord C = Viewport.center();
	double DLat = Viewport.latDiff()/(2*d);
	double DLon = Viewport.lonDiff()/(2*d);
	setViewport(CoordBox(Coord(C.lat()-DLat,C.lon()-DLon),Coord(C.lat()+DLat,C.lon()+DLon)), Screen);
}


