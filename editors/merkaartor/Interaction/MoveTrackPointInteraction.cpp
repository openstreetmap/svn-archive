#include "Interaction/MoveTrackPointInteraction.h"

#include "MapView.h"
#include "Command/TrackPointCommands.h"
#include "Map/MapDocument.h"
#include "Map/Projection.h"
#include "Map/TrackPoint.h"

#include <QtGui/QMouseEvent>

MoveTrackPointInteraction::MoveTrackPointInteraction(MapView* aView)
: TrackPointSnapInteraction(aView), Moving(0), Orig(0,0)
{
}

MoveTrackPointInteraction::~MoveTrackPointInteraction(void)
{
}

void MoveTrackPointInteraction::snapMousePressEvent(QMouseEvent *, TrackPoint* aLast)
{
	Moving = aLast;
	if (Moving)
		Orig = Moving->position();
}

void MoveTrackPointInteraction::snapMouseReleaseEvent(QMouseEvent * event, TrackPoint*)
{
	if (Moving)
	{
		Moving->setPosition(Orig);
		Moving->setLastUpdated(MapFeature::User);
		document()->history().add(new MoveTrackPointCommand(Moving,projection().inverse(event->pos())));
		view()->update();
		Moving = 0;
	}
}

void MoveTrackPointInteraction::snapMouseMoveEvent(QMouseEvent* event, TrackPoint*)
{
	if (Moving)
	{
		Moving->setPosition(projection().inverse(event->pos()));
		view()->update();
	}
}




