#include "CreateNodeInteraction.h"

#include "MainWindow.h"
#include "PropertiesDock.h"
#include "Command/DocumentCommands.h"
#include "Command/RoadCommands.h"
#include "Maps/Projection.h"
#include "Maps/TrackPoint.h"
#include "Utils/LineF.h"

#include <QList>

CreateNodeInteraction::CreateNodeInteraction(MapView* aView)
: RoadSnapInteraction(aView)
{
}

CreateNodeInteraction::~CreateNodeInteraction(void)
{
}

QString CreateNodeInteraction::toHtml()
{
	QString help;
	//help = (MainWindow::tr("LEFT-CLICK to select; LEFT-DRAG to move"));

	QString desc;
	desc = QString("<big><b>%1</b></big><br/>").arg(MainWindow::tr("Create node Interaction"));
	desc += QString("<b>%1</b><br/>").arg(help);

	QString S =
	"<html><head/><body>"
	"<small><i>" + QString(metaObject()->className()) + "</i></small><br/>"
	+ desc;
	S += "</body></html>";

	return S;
}

void CreateNodeInteraction::snapMouseReleaseEvent(QMouseEvent * ev, Road* aRoad)
{
	if (ev->button() == Qt::LeftButton)
	{
		Coord P(XY_TO_COORD(ev->pos()));
		if (aRoad)
		{
			main()->properties()->setSelection(0);
			CommandList* theList  = new CommandList(MainWindow::tr("Create node in Road: %1").arg(aRoad->id()), aRoad);
			int SnapIdx = findSnapPointIndex(aRoad, P);
			TrackPoint* N = new TrackPoint(P);
			if (M_PREFS->apiVersionNum() < 0.6)
				N->setTag("created_by", QString("Merkaartor v%1%2").arg(STRINGIFY(VERSION)).arg(STRINGIFY(REVISION)));
			theList->add(new AddFeatureCommand(main()->document()->getDirtyOrOriginLayer(aRoad->layer()),N,true));
			theList->add(new RoadAddTrackPointCommand(aRoad,N,SnapIdx,main()->document()->getDirtyOrOriginLayer(aRoad->layer())));
			document()->addHistory(theList);
			main()->properties()->setSelection(N);
			view()->invalidate(true, false);
		}
		else
		{
			TrackPoint* N = new TrackPoint(P);
			if (M_PREFS->apiVersionNum() < 0.6)
				N->setTag("created_by", QString("Merkaartor v%1%2").arg(STRINGIFY(VERSION)).arg(STRINGIFY(REVISION)));
			CommandList* theList  = new CommandList(MainWindow::tr("Create point %1").arg(N->id()), aRoad);
			theList->add(new AddFeatureCommand(main()->document()->getDirtyOrOriginLayer(),N,true));
			document()->addHistory(theList);
			main()->properties()->setSelection(N);
			view()->invalidate(true, false);
		}
	}
}

#ifndef Q_OS_SYMBIAN
QCursor CreateNodeInteraction::cursor() const
{
	return QCursor(Qt::CrossCursor);
}
#endif




