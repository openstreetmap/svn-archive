#include "Geocoder.h"

namespace OpenStreetMap
{

Geocoder::Geocoder()
{
	inLat = inLong = foundLat = foundLong = false;
	lat = lon = 0.0;
}

bool Geocoder::startElement(const QString&, const QString&,
							const QString& element,
							const QXmlAttributes& attributes)
{
	if(element=="geo:lat") 
	{
		inLat = true;
		foundLat = true;
	}
	else if(element=="geo:long")
	{
		inLong = true;
		foundLong = true;
	}
	return true;
}

bool Geocoder::endElement(const QString&, const QString&,
							const QString& element)
{
	if(element=="geo:lat") 
	{
		inLat = false;
	}
	else if(element=="geo:long")
	{
		inLong = false;
	}
	return true;
}

bool Geocoder::characters(const QString& characters)
{
	if(inLat)
	{
		lat=atof(characters.toAscii().constData());
	}
	else if (inLong)
	{
		lon=atof(characters.toAscii().constData());
	}
	return true;
}

}
