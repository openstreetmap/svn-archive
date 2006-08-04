#ifndef GEOCODER_H
#define GEOCODER_H

#include <qxml.h>
#include "EarthPoint.h"

namespace OpenStreetMap
{

class Geocoder : public QXmlDefaultHandler
{
private:
	bool inLat, inLong, foundLat, foundLong;
	double lat, lon;
public:
	Geocoder();
	bool startElement(const QString&, const QString&,
							const QString& element,
							const QXmlAttributes& attributes);
	bool endElement(const QString&, const QString&,
							const QString& element);
	bool characters(const QString& characters);
	EarthPoint getPoint() { return EarthPoint(lon,lat); }
	bool valid() { return foundLat && foundLong; }
};

}

#endif 
