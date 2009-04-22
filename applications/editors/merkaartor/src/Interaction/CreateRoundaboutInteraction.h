#ifndef INTERACTION_CREATEROUNDABOUTINTERACTION_H
#define INTERACTION_CREATEROUNDABOUTINTERACTION_H

#include <ui_CreateRoundaboutDock.h>
#include "Interaction/Interaction.h"
#include "Maps/Coord.h"

class QDockWidget;

class CreateRoundaboutInteraction : public Interaction
{
	Q_OBJECT

	public:
		CreateRoundaboutInteraction(MainWindow* Main, MapView* aView);
		~CreateRoundaboutInteraction();

		virtual void mousePressEvent(QMouseEvent * event);
		virtual void mouseMoveEvent(QMouseEvent* event);
		virtual void paintEvent(QPaintEvent* anEvent, QPainter& thePainter);
#ifndef Q_OS_SYMBIAN
		virtual QCursor cursor() const;
#endif
		
	private:
		void testIntersections(CommandList* L, Road* Left, int FromIdx, Road* Right, int RightIdx);
		MainWindow* Main;
		QDockWidget* theDock;
		Ui::CreateRoundaboutDock DockData;
		Coord Center;
		QPointF LastCursor;
		bool HaveCenter;
};

#endif // INTERACTION\CREATEROUNDABOUTINTERACTION_H
