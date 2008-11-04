namespace OSM
{

double dist(double x1, double y1, double x2, double y2);
double distp(double px, double py, double x1, double y1, double x2, double y2);
double getAngle(double a, double b, double c);

class Mercator;

class LatLon
{
public:
	double lat, lon;
	LatLon(double lat, double lon)
	{
		this->lat = lat;
		this->lon = lon;
	}

	Mercator toMercator();
};

class Mercator
{
public:
	double e, n;
	Mercator(double e, double n)
	{
		this->e = e;
		this->n = n;
	}

	LatLon toLatLon();
};

}
