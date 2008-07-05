//
// C++ Implementation: ExportGPX
//
// Description:
//
//
// Author: cbro <cbro@semperpax.com>, (C) 2008
//
// Copyright: See COPYING file that comes with this distribution
//
//

#include <QtGui>

#include "../ImportExport/ExportGPX.h"


ExportGPX::ExportGPX(MapDocument* doc)
 : IImportExport(doc)
{
}


ExportGPX::~ExportGPX()
{
}

// export
bool ExportGPX::export_(const QVector<MapFeature *>& featList)
{
	QDataStream ds(Device);
	QVector<TrackPoint*>	waypoints;
	QVector<TrackSegment*>	segments;

	if(! IImportExport::export_(featList) ) return false;

	bool OK = true;

	QDomDocument theXmlDoc;
	theXmlDoc.appendChild(theXmlDoc.createProcessingInstruction("xml", "version=\"1.0\""));

	QDomElement o = theXmlDoc.createElement("gpx");
	theXmlDoc.appendChild(o);
	o.setAttribute("version", "1.1");
	o.setAttribute("creator", "Merkaartor");
	o.setAttribute("xmlns", "http://www.topografix.com/GPX/1/1");
	o.setAttribute("xmlns:rmc", "urn:net:trekbuddy:1.0:nmea:rmc");

	for (int i=0; i<theFeatures.size(); ++i) {
		if (TrackSegment* S = dynamic_cast<TrackSegment*>(theFeatures[i]))
			segments.push_back(S);
		if (TrackPoint* P = dynamic_cast<TrackPoint*>(theFeatures[i]))
			if (!P->tagValue("_waypoint_","").isEmpty())
				waypoints.push_back(P);
	}

	for (int i=0; i < waypoints.size(); ++i) {
		waypoints[i]->toGPX(o);
	}

	QDomElement t = o.ownerDocument().createElement("trk");
	o.appendChild(t);

	for (int i=0; i < segments.size(); ++i)
		segments[i]->toXML(t);

	Device->write(theXmlDoc.toString().toUtf8());
	return OK;
}

