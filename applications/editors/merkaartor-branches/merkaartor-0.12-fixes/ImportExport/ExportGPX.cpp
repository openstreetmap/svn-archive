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
	QVector<TrackPoint*>	waypoints;
	QVector<TrackSegment*>	segments;
	QVector<MapLayer*>	tracks;

	if(! IImportExport::export_(featList) ) return false;

	bool OK = true;

	QDomDocument theXmlDoc;
	theXmlDoc.appendChild(theXmlDoc.createProcessingInstruction("xml", "version=\"1.0\""));

	QProgressDialog progress("Exporting GPX...", "Cancel", 0, 0);
	progress.setWindowModality(Qt::WindowModal);
	progress.setMaximum(progress.maximum() + featList.count());

	QDomElement o = theXmlDoc.createElement("gpx");
	theXmlDoc.appendChild(o);
	o.setAttribute("version", "1.1");
	o.setAttribute("creator", "Merkaartor");
	o.setAttribute("xmlns", "http://www.topografix.com/GPX/1/1");
	o.setAttribute("xmlns:rmc", "urn:net:trekbuddy:1.0:nmea:rmc");

	for (int i=0; i<theFeatures.size(); ++i) {
		if (TrackSegment* S = dynamic_cast<TrackSegment*>(theFeatures[i])) {
			segments.push_back(S);
			if (!tracks.contains(S->layer()))
				tracks.push_back(S->layer());
		}
		if (TrackPoint* P = dynamic_cast<TrackPoint*>(theFeatures[i]))
			if (!P->tagValue("_waypoint_","").isEmpty())
				waypoints.push_back(P);
	}

	for (int i=0; i < waypoints.size(); ++i) {
		waypoints[i]->toGPX(o, progress);
	}

	for (int i=0; i<tracks.size(); ++i) {
		QDomElement t = o.ownerDocument().createElement("trk");
		o.appendChild(t);

		QDomElement n = o.ownerDocument().createElement("name");
		t.appendChild(n);
		QDomText v = o.ownerDocument().createTextNode(tracks[i]->name());
		n.appendChild(v);

		for (int j=0; j < segments.size(); ++j)
			if (tracks[i]->exists(segments[j]))
				segments[j]->toXML(t, progress);
	}

	progress.setValue(progress.maximum());
	if (progress.wasCanceled())
		return false;

	Device->write(theXmlDoc.toString().toUtf8());
	return OK;
}

