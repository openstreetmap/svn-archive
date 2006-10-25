#include "CreateNodeInteraction.h"

#include "MainWindow.h"
#include "Command/DocumentCommands.h"
#include "Map/Projection.h"
#include "Map/TrackPoint.h"

CreateNodeInteraction::CreateNodeInteraction(MapView* aView)
: Interaction(aView)
{
}

CreateNodeInteraction::~CreateNodeInteraction(void)
{
}

void CreateNodeInteraction::mouseReleaseEvent(QMouseEvent * event)
{
	Coord P = projection().inverse(event->pos());
	TrackPoint* N = new TrackPoint(P);
	document()->history().add(new AddFeatureCommand(main()->activeLayer(),N,true));
	view()->update();
}


QCursor CreateNodeInteraction::cursor() const
{
	return QCursor(Qt::CrossCursor);
}




