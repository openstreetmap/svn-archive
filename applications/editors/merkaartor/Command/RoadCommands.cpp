#include "RoadCommands.h"

#include "Map/Road.h"
#include "Map/TrackPoint.h"
#include "Map/MapLayer.h"
#include "Sync/DirtyList.h"

RoadAddTrackPointCommand::RoadAddTrackPointCommand(Road* R)
: Command (R), theLayer(0), oldLayer(0), theRoad(R), theTrackPoint(0), Position(0)
{
}

RoadAddTrackPointCommand::RoadAddTrackPointCommand(Road* R, TrackPoint* W, MapLayer* aLayer)
: Command (R), theLayer(aLayer), oldLayer(0), theRoad(R), theTrackPoint(W), Position(theRoad->size())
{
	redo();
}

RoadAddTrackPointCommand::RoadAddTrackPointCommand(Road* R, TrackPoint* W, unsigned int aPos, MapLayer* aLayer)
: Command(R), theLayer(aLayer), oldLayer(0), theRoad(R), theTrackPoint(W), Position(aPos)
{
	redo();
}

RoadAddTrackPointCommand::~RoadAddTrackPointCommand(void)
{
	if (oldLayer)
		oldLayer->decDirtyLevel(commandDirtyLevel);
}

void RoadAddTrackPointCommand::undo()
{
	Command::undo();
	theRoad->remove(Position);
	if (theLayer && oldLayer && (theLayer != oldLayer)) {
		theLayer->remove(theRoad);
		oldLayer->add(theRoad);
		decDirtyLevel(oldLayer);
	}
}

void RoadAddTrackPointCommand::redo()
{
	theRoad->add(theTrackPoint, Position);
	oldLayer = theRoad->layer();
	if (theLayer && oldLayer && (theLayer != oldLayer)) {
		oldLayer->remove(theRoad);
		incDirtyLevel(oldLayer);
		theLayer->add(theRoad);
	}
	Command::redo();
}

bool RoadAddTrackPointCommand::buildDirtyList(DirtyList& theList)
{
	if (!theRoad->layer())
		return theList.update(theRoad);
	if (theRoad->layer()->isUploadable() && theTrackPoint->layer()->isUploadable())
		return theList.update(theRoad);
	else
		return theList.noop(theRoad);
}

bool RoadAddTrackPointCommand::toXML(QDomElement& xParent) const
{
	bool OK = true;

	QDomElement e = xParent.ownerDocument().createElement("RoadAddTrackPointCommand");
	xParent.appendChild(e);

	e.setAttribute("xml:id", id());
	e.setAttribute("road", theRoad->xmlId());
	e.setAttribute("trackpoint", theTrackPoint->xmlId());
	e.setAttribute("pos", QString::number(Position));
	if (theLayer)
		e.setAttribute("layer", theLayer->id());
	if (oldLayer)
		e.setAttribute("oldlayer", oldLayer->id());

	Command::toXML(e);

	return OK;
}

RoadAddTrackPointCommand * RoadAddTrackPointCommand::fromXML(MapDocument * d, QDomElement e)
{
	RoadAddTrackPointCommand* a = new RoadAddTrackPointCommand();
	a->setId(e.attribute("xml:id"));
	if (e.hasAttribute("layer"))
		a->theLayer = d->getLayer(e.attribute("layer"));
	else
		a->theLayer = d->getDirtyLayer();
	if (e.hasAttribute("oldlayer"))
		a->oldLayer = d->getLayer(e.attribute("oldlayer"));
	else
		a->oldLayer = NULL;
	a->theRoad = MapFeature::getWayOrCreatePlaceHolder(d, a->theLayer, e.attribute("road"));
	a->theTrackPoint = MapFeature::getTrackPointOrCreatePlaceHolder(d, a->theLayer, e.attribute("trackpoint"));
	a->Position = e.attribute("pos").toUInt();

	Command::fromXML(d, e, a);

	return a;
}

/* ROADREMOVETRACKPOINTCOMMAND */

RoadRemoveTrackPointCommand::RoadRemoveTrackPointCommand(Road* R)
: Command(R), theLayer(0), oldLayer(0), Idx(0), theRoad(R), theTrackPoint(0)
{
}

RoadRemoveTrackPointCommand::RoadRemoveTrackPointCommand(Road* R, TrackPoint* W, MapLayer* aLayer)
: Command(R), theLayer(aLayer), oldLayer(0), Idx(R->find(W)), theRoad(R), theTrackPoint(W)
{
	redo();
}

RoadRemoveTrackPointCommand::RoadRemoveTrackPointCommand(Road* R, unsigned int anIdx, MapLayer* aLayer)
: Command(R), theLayer(aLayer), oldLayer(0), Idx(anIdx), theRoad(R), theTrackPoint(R->getNode(anIdx))
{
	redo();
}

RoadRemoveTrackPointCommand::~RoadRemoveTrackPointCommand(void)
{
	if (oldLayer)
		oldLayer->decDirtyLevel(commandDirtyLevel);
}

void RoadRemoveTrackPointCommand::undo()
{
	Command::undo();
	theRoad->add(theTrackPoint,Idx);
	if (theLayer && oldLayer && (theLayer != oldLayer)) {
		theLayer->remove(theRoad);
		oldLayer->add(theRoad);
		decDirtyLevel(oldLayer);
	}
}

void RoadRemoveTrackPointCommand::redo()
{
	theRoad->remove(Idx);
	oldLayer = theRoad->layer();
	if (theLayer && oldLayer && (theLayer != oldLayer)) {
		oldLayer->remove(theRoad);
		incDirtyLevel(oldLayer);
		theLayer->add(theRoad);
	}
	Command::redo();
}

bool RoadRemoveTrackPointCommand::buildDirtyList(DirtyList& theList)
{
	if (!theRoad->layer())
		return theList.update(theRoad);
	if (theRoad->layer()->isUploadable() && (theTrackPoint->layer()->isUploadable() || theTrackPoint->hasOSMId()))
		return theList.update(theRoad);
	else
		return theList.noop(theRoad);
}

bool RoadRemoveTrackPointCommand::toXML(QDomElement& xParent) const
{
	bool OK = true;

	QDomElement e = xParent.ownerDocument().createElement("RoadRemoveTrackPointCommand");
	xParent.appendChild(e);

	e.setAttribute("xml:id", id());
	e.setAttribute("road", theRoad->xmlId());
	e.setAttribute("trackpoint", theTrackPoint->xmlId());
	e.setAttribute("index", QString::number(Idx));
	if (theLayer)
		e.setAttribute("layer", theLayer->id());
	if (oldLayer)
		e.setAttribute("oldlayer", oldLayer->id());

	Command::toXML(e);

	return OK;
}

RoadRemoveTrackPointCommand * RoadRemoveTrackPointCommand::fromXML(MapDocument * d, QDomElement e)
{
	RoadRemoveTrackPointCommand* a = new RoadRemoveTrackPointCommand();
	a->setId(e.attribute("xml:id"));
	if (e.hasAttribute("layer"))
		a->theLayer = d->getLayer(e.attribute("layer"));
	else
		a->theLayer = d->getDirtyLayer();
	if (e.hasAttribute("oldlayer"))
		a->oldLayer = d->getLayer(e.attribute("oldlayer"));
	else
		a->oldLayer = NULL;
	a->theRoad = MapFeature::getWayOrCreatePlaceHolder(d, a->theLayer, e.attribute("road"));
	a->theTrackPoint = MapFeature::getTrackPointOrCreatePlaceHolder(d, a->theLayer, e.attribute("trackpoint"));
	a->Idx = e.attribute("index").toUInt();

	Command::fromXML(d, e, a);

	return a;
}




