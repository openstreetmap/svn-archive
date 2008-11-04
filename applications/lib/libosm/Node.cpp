#include "Node.h"

using namespace OSM;

Node::Node(double lt, double ln) :
	Object(0), lat(lt), lon(ln)
{
}

Node::Node(int i, double lt, double ln) :
	Object(i), lat(lt), lon(ln)
{
}

bool Node::operator==(const Node& tp)
{
	return (fabs(lat - tp.lat) < 0.000001) && (fabs(lon - tp.lon) < 0.000001);
}

double Node::getLat()
{
	return lat;
}
double Node::getLon()
{
	return lon;
}

void Node::setCoords(double lat, double lon)
{
	this->lat = lat;
	this->lon = lon;
}

void Node::toXML(std::ostream &strm)
{
	std::streamsize old = strm.precision(15);
	if (hasTags())
	{
		strm << "  <node id='" << id() << "' lat='";
		strm << lat << "' lon='" << lon;
		strm << "'>" << std::endl;
		tagsToXML(strm);
		strm << "  </node>" << std::endl;
	}
	else
	{
		strm << "  <node id='" << id() << "' lat='";
		strm << lat << "' lon='" << lon;
		strm << "'/>" << std::endl;
	}
	strm.precision(old);
}
