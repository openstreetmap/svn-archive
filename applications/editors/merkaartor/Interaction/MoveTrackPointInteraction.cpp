#include "Interaction/MoveTrackPointInteraction.h"

#include "MapView.h"
#include "Command/DocumentCommands.h"
#include "Command/RoadCommands.h"
#include "Command/TrackPointCommands.h"
#include "Map/Coord.h"
#include "Map/MapDocument.h"
#include "Map/Projection.h"
#include "Map/TrackPoint.h"
#include "Utils/LineF.h"

#include <QtGui/QCursor>
#include <QtGui/QMouseEvent>
#include <QtGui/QPixmap>
#include <QMessageBox>

#include <vector>

MoveTrackPointInteraction::MoveTrackPointInteraction(MapView* aView)
: FeatureSnapInteraction(aView), StartDragPosition(0,0)
{
}

MoveTrackPointInteraction::~MoveTrackPointInteraction(void)
{
}

QCursor MoveTrackPointInteraction::cursor() const
{
	QPixmap pm(":/Icons/move.xpm");
	return QCursor(pm);
}


void MoveTrackPointInteraction::snapMousePressEvent(QMouseEvent * event, MapFeature* aLast)
{
	MapFeature* sel = aLast;
	if (view()->isSelectionLocked()) {
		sel = view()->properties()->selection(0);
		if (!sel)
			sel = aLast;
	}
	clearNoSnap();
	Moving.clear();
	OriginalPosition.clear();
	StartDragPosition = projection().inverse(event->pos());
	if (TrackPoint* Pt = dynamic_cast<TrackPoint*>(sel))
	{
		Moving.push_back(Pt);
		StartDragPosition = Pt->position();
	}
	else if (Road* R = dynamic_cast<Road*>(sel)) {
		for (unsigned int i=0; i<R->size(); ++i)
			if (std::find(Moving.begin(),Moving.end(),R->get(i)) == Moving.end())
				Moving.push_back(R->getNode(i));
		addToNoSnap(R);
	}
	for (unsigned int i=0; i<Moving.size(); ++i)
	{
		OriginalPosition.push_back(Moving[i]->position());
		addToNoSnap(Moving[i]);
	}
}

void MoveTrackPointInteraction::snapMouseReleaseEvent(QMouseEvent * event, MapFeature* Closer)
{
	if (Moving.size())
	{
		CommandList* theList = new CommandList(MainWindow::tr("Move Point %1").arg(Moving[0]->id()), Moving[0]);
		Coord Diff(calculateNewPosition(event,Closer, theList)-StartDragPosition);
		for (unsigned int i=0; i<Moving.size(); ++i)
		{
			Moving[i]->setPosition(OriginalPosition[i]);
			if (Moving[i]->layer()->isTrack())
				theList->add(new MoveTrackPointCommand(Moving[i],OriginalPosition[i]+Diff, Moving[i]->layer()));
			else
				theList->add(new MoveTrackPointCommand(Moving[i],OriginalPosition[i]+Diff, document()->getDirtyOrOriginLayer(Moving[i]->layer())));
		}
		
		// If moving a single node (not a track node), see if it got dropped onto another node
		if (Moving.size() == 1 && !Moving[0]->layer()->isTrack())
		{
			Coord newPos = OriginalPosition[0] + Diff;
			std::vector<TrackPoint*> samePosPts;
			for (VisibleFeatureIterator it(document()); !it.isEnd(); ++it)
			{
				TrackPoint* visPt = dynamic_cast<TrackPoint*>(it.get());
				if (visPt)
				{
					if (visPt == Moving[0])
						continue;

					if (visPt->position() == newPos)
					{
						samePosPts.push_back(visPt);
					}
				}
			}
			// Ensure the node being moved is at the end of the list.
			// (This is not the node that all nodes will be merged into,
			// they are always merged into a node that already was at that position.)
			samePosPts.push_back(Moving[0]);

			if (samePosPts.size() > 1)   // Ignore the node we're moving, see if there are more
			{
				int ret = QMessageBox::question(view(),
					MainWindow::tr("Nodes at the same position found."),
					MainWindow::tr("Do you want to merge all nodes at the drop position?"),
					QMessageBox::Yes | QMessageBox::No);
				if (ret == QMessageBox::Yes)
				{
					// Merge all nodes from the same position

					// from MainWindow::on_nodeMergeAction_triggered()
					// Merge all nodes into the first node that has been found (not the node being moved)
					MapFeature* F = samePosPts[0];
					// Make a separate undo command list for this action
					theList->setDescription(MainWindow::tr("Merge Nodes into %1").arg(F->id()));
					theList->setFeature(F);
					
					// from mergeNodes(theDocument, theList, theProperties);
					std::vector<MapFeature*> alt;
					TrackPoint* merged = samePosPts[0];
					alt.push_back(merged);
					for (unsigned int i = 1; i < samePosPts.size(); ++i) {
						MapFeature::mergeTags(document(), theList, merged, samePosPts[i]);
						theList->add(new RemoveFeatureCommand(document(), samePosPts[i], alt));
					}
					
					view()->properties()->setSelection(F);
				}
			}
		}
		
		document()->addHistory(theList);
		view()->invalidate(true, false);
	}
	Moving.clear();
	OriginalPosition.clear();
	clearNoSnap();
}

void MoveTrackPointInteraction::snapMouseMoveEvent(QMouseEvent* event, MapFeature* Closer)
{
	if (Moving.size())
	{
		Coord Diff = calculateNewPosition(event,Closer,0)-StartDragPosition;
		for (unsigned int i=0; i<Moving.size(); ++i)
			Moving[i]->setPosition(OriginalPosition[i]+Diff);
		view()->invalidate(true, false);
	}
}

Coord MoveTrackPointInteraction::calculateNewPosition(QMouseEvent *event, MapFeature *aLast, CommandList* theList)
{
	Coord TargetC = projection().inverse(event->pos());
	QPoint Target(TargetC.lat(),TargetC.lon());
	if (TrackPoint* Pt = dynamic_cast<TrackPoint*>(aLast))
		return Pt->position();
	else if (Road* R = dynamic_cast<Road*>(aLast))
	{
		LineF L1(R->getNode(0)->position(),R->getNode(1)->position());
		double Dist = L1.capDistance(TargetC);
		QPoint BestTarget = L1.project(Target).toPoint();
		unsigned int BestIdx = 1;
		for (unsigned int i=2; i<R->size(); ++i)
		{
			LineF L2(R->getNode(i-1)->position(),R->getNode(i)->position());
			double Dist2 = L2.capDistance(TargetC);
			if (Dist2 < Dist)
			{
				Dist = Dist2;
				BestTarget = L2.project(Target).toPoint();
				BestIdx = i;
			}
		}
		if (theList && (Moving.size() == 1))
			theList->add(new
				RoadAddTrackPointCommand(R,Moving[0],BestIdx,document()->getDirtyOrOriginLayer(R->layer())));
		return Coord(BestTarget.x(),BestTarget.y());
	}
	return projection().inverse(event->pos());
}
