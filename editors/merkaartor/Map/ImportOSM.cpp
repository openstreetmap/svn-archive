#include "Map/ImportOSM.h"

#include "Command/Command.h"
#include "Command/DocumentCommands.h"
#include "Command/FeatureCommands.h"
#include "Command/TrackPointCommands.h"
#include "Command/WayCommands.h"
#include "Map/MapDocument.h"
#include "Map/Road.h"
#include "Map/TrackPoint.h"
#include "Map/TrackSegment.h"
#include "Map/Way.h"

#include <QtCore/QBuffer>
#include <QtCore/QDateTime>
#include <QtCore/QEventLoop>
#include <QtCore/QFile>
#include <QtGui/QMessageBox>
#include <QtGui/QProgressDialog>
#include <QtXml/QDomDocument>

/*
 * Forward decls
 */
static void loadTags(const QDomElement& Root, MapFeature* W);
static void loadTags(const QDomElement& Root, MapFeature* W, CommandList* theList);


static void importNode(const QDomElement& Root, MapDocument* theDocument, MapLayer* theLayer, MapLayer* conflictLayer, CommandList* theList)
{
	double Lat = Root.attribute("lat").toDouble();
	double Lon = Root.attribute("lon").toDouble();
	QString id = "node_"+Root.attribute("id");
//	QDateTime dt(QDateTime::fromString(Root.attribute("timestamp","yyyy-MM-dd HH:mm:ss")));
	TrackPoint* Pt = dynamic_cast<TrackPoint*>(theDocument->get(id));
	if (Pt)
	{
		if (Pt->lastUpdated() == MapFeature::User)
		{
			// conflict
			Pt->setLastUpdated(MapFeature::UserResolved);
			Pt = new TrackPoint(Coord(angToRad(Lat),angToRad(Lon)));
			Pt->setId("conflict_"+id);
			Pt->setLastUpdated(MapFeature::OSMServerConflict);
			theList->add(new AddFeatureCommand(conflictLayer, Pt, false));
		}
		else if (Pt->lastUpdated() != MapFeature::UserResolved)
		{
			theList->add(new MoveTrackPointCommand(Pt,Coord(angToRad(Lat),angToRad(Lon))));
			loadTags(Root, Pt, theList);
		}
	}
	else
	{
		Pt = new TrackPoint(Coord(angToRad(Lat),angToRad(Lon)));
		Pt->setId(id);
		loadTags(Root, Pt);
		Pt->setLastUpdated(MapFeature::OSMServer);
		theList->add(new AddFeatureCommand(theLayer,Pt, false));
	}
}

static void loadTags(const QDomElement& Root, MapFeature* W)
{
	for(QDomNode n = Root.firstChild(); !n.isNull(); n = n.nextSibling())
	{
		QDomElement t = n.toElement();
		if (!t.isNull())
		{
			if (t.tagName() == "tag")
				W->setTag(t.attribute("k"),t.attribute("v"));
		}
	}
}

static void loadTags(const QDomElement& Root, MapFeature* W, CommandList* theList)
{
	theList->add(new ClearTagsCommand(W));
	for(QDomNode n = Root.firstChild(); !n.isNull(); n = n.nextSibling())
	{
		QDomElement t = n.toElement();
		if (!t.isNull())
		{
			if (t.tagName() == "tag")
				theList->add(new SetTagCommand(W, t.attribute("k"),t.attribute("v")));
		}
	}
}

static void importSegment(const QDomElement& Root, MapDocument* theDocument, MapLayer* theLayer, MapLayer* conflictLayer, CommandList* theList)
{
	TrackPoint* From = dynamic_cast<TrackPoint*>(theDocument->get("node_"+Root.attribute("from")));
	TrackPoint* To = dynamic_cast<TrackPoint*>(theDocument->get("node_"+Root.attribute("to")));
	QString id = "segment_"+Root.attribute("id");
	if (From && To)
	{
		Way* W = dynamic_cast<Way*>(theDocument->get(id));
		if (W)
		{
			if (W->lastUpdated() == MapFeature::User)
			{
				W->setLastUpdated(MapFeature::UserResolved);
				// conflict
				TrackPoint* Conflict = dynamic_cast<TrackPoint*>(theDocument->get("conflict_node_"+Root.attribute("from")));
				if (Conflict) From = Conflict;
				Conflict = dynamic_cast<TrackPoint*>(theDocument->get("conflict_node_"+Root.attribute("to")));
				if (Conflict) To = Conflict;
				Way* W = new Way(From,To);
				W->setId("conflict_"+id);
				loadTags(Root,W);
				theList->add(new AddFeatureCommand(conflictLayer,W, false));
				W->setLastUpdated(MapFeature::OSMServerConflict);
			}
			else if (W->lastUpdated() != MapFeature::UserResolved)
			{
				theList->add(new WaySetFromToCommand(W,From,To));
				loadTags(Root,W, theList);
			}
		}
		else
		{
			W = new Way(From,To);
			W->setId(id);
			loadTags(Root,W);
			theList->add(new AddFeatureCommand(theLayer,W, false));
			W->setLastUpdated(MapFeature::OSMServer);
		}
	}
}


static void importWay(const QDomElement& Root, MapDocument* theDocument, MapLayer* theLayer, MapLayer* conflictLayer, CommandList* theList)
{
	std::vector<Way*> Segments;
	for(QDomNode n = Root.firstChild(); !n.isNull(); n = n.nextSibling())
	{
		QDomElement t = n.toElement();
		if (!t.isNull())
		{
			if (t.tagName() == "seg")
			{
				Way* Part = dynamic_cast<Way*>(theDocument->get("segment_"+t.attribute("id")));
				if (Part)
					Segments.push_back(Part);
			}
		}
	}
	QString id = "way_"+Root.attribute("id");
	if (Segments.size())
	{
		Road* R = dynamic_cast<Road*>(theDocument->get(id));
		if (R)
		{
			if (R->lastUpdated() == MapFeature::User)
			{
				R->setLastUpdated(MapFeature::UserResolved);
				// conflict
/*				TrackPoint* Conflict = dynamic_cast<TrackPoint*>(theDocument->get("conflict_node_"+Root.attribute("from")));
				if (Conflict) From = Conflict;
				Conflict = dynamic_cast<TrackPoint*>(theDocument->get("conflict_node_"+Root.attribute("to")));
				if (Conflict) To = Conflict;
				Way* W = new Way(From,To);
				W->setId("conflict_"+id);
				loadSegmentTags(Root,W);
				theList->add(new AddFeatureCommand(conflictLayer,W, false));
				W->setLastUpdated(MapFeature::OSMServerConflict); */
			}
			else if (R->lastUpdated() != MapFeature::UserResolved)
			{
/*				theList->add(new WaySetFromToCommand(W,From,To)); */
				loadTags(Root,R, theList);
				
			}
		}
		else
		{
			R = new Road;
			for (unsigned int i=0; i<Segments.size(); ++i)
			{
				R->add(Segments[i]);
				Segments[i]->addAsPartOf(R);
			}
			R->setId(id);
			loadTags(Root,R);
			theList->add(new AddFeatureCommand(theLayer,R, false));
			R->setLastUpdated(MapFeature::OSMServer);
		}
	}
}


static void importOSM(QProgressDialog* dlg, const QDomElement& Root, MapDocument* theDocument, MapLayer* theLayer, MapLayer* conflictLayer, CommandList* theList)
{
	unsigned int Count = 0;
	for(QDomNode n = Root.firstChild(); !n.isNull(); n = n.nextSibling())
		++Count;
	unsigned int Done = 0;
	dlg->setMaximum(Count);
	QEventLoop ev;
	for(QDomNode n = Root.firstChild(); !n.isNull(); n = n.nextSibling())
	{
		QDomElement t = n.toElement();
		if (!t.isNull())
		{
			if (t.tagName() == "node")
				importNode(t,theDocument, theLayer, conflictLayer, theList);
			else if (t.tagName() == "segment")
				importSegment(t,theDocument, theLayer, conflictLayer, theList);
			else if (t.tagName() == "way")
				importWay(t,theDocument, theLayer, conflictLayer, theList);
		}
		++Done;
		dlg->setValue(Done);
		ev.processEvents();
		if (dlg->wasCanceled()) return;
	}
}

bool importOSM(QWidget* aParent, QIODevice& File, MapDocument* theDocument, MapLayer* theLayer)
{
	QDomDocument DomDoc;
	QString ErrorStr;
	int ErrorLine;
	int ErrorColumn;
	QProgressDialog* dlg = new QProgressDialog(aParent);
	dlg->setWindowModality(Qt::ApplicationModal);
	dlg->setMinimumDuration(0);
	dlg->setLabelText("Parsing XML");
	dlg->show();
	if (!DomDoc.setContent(&File, true, &ErrorStr, &ErrorLine,&ErrorColumn))
	{
		File.close();
		QMessageBox::warning(aParent,"Parse error",
			QString("Parse error at line %1, column %2:\n%3")
                                  .arg(ErrorLine)
                                  .arg(ErrorColumn)
                                  .arg(ErrorStr));
		return false;
	}
	QDomElement root = DomDoc.documentElement();
	if (root.tagName() != "osm")
	{
		QMessageBox::information(aParent, "Parse error","Root is not an osm node");
		return false;
	}
	CommandList* theList = new CommandList;
	theDocument->add(theLayer);
	MapLayer* conflictLayer = new MapLayer("Conflicts from "+theLayer->name());
	importOSM(dlg, root, theDocument, theLayer, conflictLayer, theList);
	bool WasCanceled = dlg->wasCanceled();
	delete dlg;
	if (theList->empty() || WasCanceled)
	{
		theDocument->remove(theLayer);
		delete theLayer;
		delete conflictLayer;
		delete theList;
	}
	else
	{
		theDocument->history().add(theList);
		if (conflictLayer->size())
			theDocument->add(conflictLayer);
		else
			delete conflictLayer;
	}
	return true;
}

bool importOSM(QWidget* aParent, const QString& aFilename, MapDocument* theDocument, MapLayer* theLayer)
{
	QFile File(aFilename);
	if (!File.open(QIODevice::ReadOnly))
		 return false;
	return importOSM(aParent, File, theDocument, theLayer);
}

bool importOSM(QWidget* aParent, QByteArray& Content, MapDocument* theDocument, MapLayer* theLayer)
{
	QBuffer File(&Content);
	return importOSM(aParent, File, theDocument, theLayer);
}



