#include "EditInteraction.h"
#include "MainWindow.h"
#include "MapView.h"
#include "PropertiesDock.h"
#include "Command/Command.h"
#include "Command/DocumentCommands.h"
#include "Command/FeatureCommands.h"
#include "Command/RoadCommands.h"
#include "Command/WayCommands.h"
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
	connect(main(),SIGNAL(reverse_triggered()), this,SLOT(on_reverse_triggered()));
}

EditInteraction::~EditInteraction(void)
{
	main()->editRemoveAction->setEnabled(false);
	main()->editMoveAction->setEnabled(false);
	main()->editAddAction->setEnabled(false);
	main()->editReverseAction->setEnabled(false);
}

void EditInteraction::paintEvent(QPaintEvent* anEvent, QPainter& thePainter)
{
	if (!Panning)
		for (unsigned int i=0; i<view()->properties()->size(); ++i)
			view()->properties()->selection(i)->drawFocus(thePainter, projection());
	FeatureSnapInteraction::paintEvent(anEvent, thePainter);
}

void EditInteraction::snapMousePressEvent(QMouseEvent * event, MapFeature* aLast)
{
	if (event->modifiers() & Qt::ControlModifier)
		view()->properties()->toggleSelection(aLast);
	else
		view()->properties()->setSelection(aLast);
	bool IsPoint = false;
	bool IsRoad = false;
	bool IsWay = false;
	if (view()->properties()->size() == 1)
	{
		IsPoint = dynamic_cast<TrackPoint*>(aLast) != 0;
		IsRoad = dynamic_cast<Road*>(aLast) != 0;
		IsWay = dynamic_cast<Way*>(aLast) != 0;
	}
	main()->editRemoveAction->setEnabled(view()->properties()->size() == 1);
	main()->editMoveAction->setEnabled(IsPoint);
	main()->editAddAction->setEnabled(IsRoad);
	main()->editReverseAction->setEnabled(IsRoad || IsWay);
	
	view()->update();
	if (!aLast)
	{
		Panning = true;
		LastPan = event->pos();
		activateSnap(false);
	}	
}

void EditInteraction::snapMouseReleaseEvent(QMouseEvent * , MapFeature* )
{
	if (Panning)
	{
		activateSnap(true);
		Panning = false;
		view()->invalidate();
	}
}

void EditInteraction::snapMouseMoveEvent(QMouseEvent* event, MapFeature* )
{
	if (Panning)
	{
		QPoint Delta = LastPan;
		Delta -= event->pos();
		view()->projection().panScreen(-Delta,view()->rect());
		view()->invalidate();
		LastPan = event->pos();
	}
}

void EditInteraction::on_remove_triggered()
{
	MapFeature* Selection = view()->properties()->selection(0);
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
	view()->launch(new CreateRoadInteraction(view(),dynamic_cast<Road*>(view()->properties()->selection(0))));
}

void EditInteraction::on_reverse_triggered()
{
	MapFeature* Selection = view()->properties()->selection(0);
	if (Road* R = dynamic_cast<Road*>(Selection))
	{
		std::vector<Way*> Ways;
		CommandList* theList = new CommandList;
		for (unsigned int i=R->size(); i; --i)
		{
			Way* W = R->get(i-1);
			Ways.push_back(W);
			theList->add(new RoadRemoveWayCommand(R,W));
			theList->add(new WaySetFromToCommand(W,W->to(),W->controlTo(),W->controlFrom(),W->from()));
		}
		for (unsigned int i=0; i<Ways.size(); ++i)
			theList->add(new RoadAddWayCommand(R,Ways[i]));
		document()->history().add(theList);
	}
	else if (Way* W = dynamic_cast<Way*>(Selection))
		document()->history().add(new WaySetFromToCommand(W,W->to(),W->controlTo(),W->controlFrom(),W->from()));
	view()->update();
}
