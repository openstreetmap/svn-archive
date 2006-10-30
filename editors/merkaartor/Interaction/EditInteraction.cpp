#include "EditInteraction.h"
#include "MainWindow.h"
#include "MapView.h"
#include "PropertiesDock.h"
#include "Command/Command.h"
#include "Command/DocumentCommands.h"
#include "Command/FeatureCommands.h"
#include "Interaction/CreateRoadInteraction.h"
#include "Interaction/MoveTrackPointInteraction.h"
#include "Map/MapDocument.h"
#include "Map/MapFeature.h"
#include "Map/Road.h"
#include "Map/TrackPoint.h"
#include "Map/Way.h"

#include <QtGui/QMouseEvent>

EditInteraction::EditInteraction(MapView* theView)
: FeatureSnapInteraction(theView), Panning(false)
{
	connect(main(),SIGNAL(remove_triggered()),this,SLOT(on_remove_triggered()));
	connect(main(),SIGNAL(move_triggered()),this,SLOT(on_move_triggered()));
	connect(main(),SIGNAL(add_triggered()),this,SLOT(on_add_triggered()));
}

EditInteraction::~EditInteraction(void)
{
	main()->editRemoveAction->setEnabled(false);
	main()->editMoveAction->setEnabled(false);
	main()->editAddAction->setEnabled(false);
}

void EditInteraction::paintEvent(QPaintEvent* anEvent, QPainter& thePainter)
{
	if (!Panning && view()->properties()->selection())
		view()->properties()->selection()->drawFocus(thePainter, projection());
	FeatureSnapInteraction::paintEvent(anEvent, thePainter);
}

void EditInteraction::snapMousePressEvent(QMouseEvent * event, MapFeature* aLast)
{
	main()->editRemoveAction->setEnabled(aLast != 0);
	main()->editMoveAction->setEnabled(dynamic_cast<TrackPoint*>(aLast) != 0);
	main()->editAddAction->setEnabled(dynamic_cast<Road*>(aLast) != 0);
	view()->properties()->setSelection(aLast);
	view()->update();
	if (!aLast)
	{
		Panning = true;
		LastPan = event->pos();
	}	
}

void EditInteraction::snapMouseReleaseEvent(QMouseEvent * , MapFeature* )
{
	if (Panning)
	{
		Panning = false;
	}
}

void EditInteraction::snapMouseMoveEvent(QMouseEvent* event, MapFeature* )
{
	if (Panning)
	{
		QPoint Delta = LastPan;
		Delta -= event->pos();
		view()->projection().panScreen(-Delta,view()->rect());
		view()->update();
		LastPan = event->pos();
	}
}

void EditInteraction::on_remove_triggered()
{
	MapFeature* Selection = view()->properties()->selection();
	if (Selection)
	{
		view()->properties()->setSelection(0);
		main()->editRemoveAction->setEnabled(false);
		std::vector<MapFeature*> Alternatives;
		CommandList* theList = new CommandList;
		for (FeatureIterator it(document()); !it.isEnd(); ++it)
			it.get()->cascadedRemoveIfUsing(document(), Selection, theList, Alternatives);
		theList->add(new RemoveFeatureCommand(document(), Selection));
		document()->history().add(theList);
		view()->update();
	}
}

void EditInteraction::on_move_triggered()
{
	view()->launch(new MoveTrackPointInteraction(view()));
}

void EditInteraction::on_add_triggered()
{
	view()->launch(new CreateRoadInteraction(view(),dynamic_cast<Road*>(view()->properties()->selection())));
}


