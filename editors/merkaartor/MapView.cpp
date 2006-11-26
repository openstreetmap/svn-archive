#include "MapView.h"
#include "MainWindow.h"
#include "Map/MapDocument.h"
#include "Map/MapFeature.h"
#include "Interaction/EditInteraction.h"
#include "Interaction/Interaction.h"

#include <QtCore/QTime>
#include <QtGui/QMainWindow>
#include <QtGui/QMouseEvent>
#include <QtGui/QPainter>
#include <QtGui/QStatusBar>

MapView::MapView(MainWindow* aMain)
: Main(aMain), theDocument(0), theInteraction(0)
{
	setMouseTracking(true);
	setAttribute(Qt::WA_OpaquePaintEvent);
}

MapView::~MapView(void)
{
}

MainWindow* MapView::main()
{
	return Main;
}

PropertiesDock* MapView::properties()
{
	return Main->properties();
}

void MapView::setDocument(MapDocument* aDoc)
{
	theDocument = aDoc;
}

MapDocument* MapView::document()
{
	return theDocument;
}

void MapView::paintEvent(QPaintEvent* anEvent)
{
	QTime Start(QTime::currentTime());
	QPainter P(this);
	P.setRenderHint(QPainter::Antialiasing);
	P.fillRect(rect(),QBrush(QColor(255,255,255)));
	if (theDocument)
	{
/*		for (unsigned int i=0; i<theDocument->numLayers(); ++i)
		{
			MapLayer* theLayer = theDocument->layer(i);
			for (unsigned int j=0; j<theLayer->size(); ++j)
				theLayer->get(i)->draw(P,projection());
		} */
		for (VisibleFeatureIterator i(theDocument); !i.isEnd(); ++i)
			i.get()->draw(P,projection());
	}
	if (theInteraction)
		theInteraction->paintEvent(anEvent,P);
	QTime Stop(QTime::currentTime());
	main()->statusBar()->clearMessage();
	main()->statusBar()->showMessage(QString("Paint took %1ms").arg(Start.msecsTo(Stop)));
}

void MapView::mousePressEvent(QMouseEvent * event)
{
	if (theInteraction)
		theInteraction->mousePressEvent(event);
}

void MapView::mouseReleaseEvent(QMouseEvent * event)
{
	if (theInteraction)
		theInteraction->mouseReleaseEvent(event);
}

void MapView::mouseMoveEvent(QMouseEvent* anEvent)
{
	if (!updatesEnabled()) return;
	if (theInteraction)
		theInteraction->mouseMoveEvent(anEvent);
}

void MapView::wheelEvent(QWheelEvent* ev)
{
	int Steps = ev->delta()/120;
	if (Steps > 0)
	{
		for (int i=0; i<Steps; ++i)
			projection().zoom(0.75,rect());
		update();
	}
	else if (Steps < 0)
	{
		for (int i=0; i<-Steps; ++i)
			projection().zoom(1/0.75,rect());
		update();
	}
}

void MapView::launch(Interaction* anInteraction)
{
	if (theInteraction)
		delete theInteraction;
	theInteraction = anInteraction;
	if (theInteraction)
		setCursor(theInteraction->cursor());
	else
	{
		setCursor(QCursor(Qt::ArrowCursor));
		launch(new EditInteraction(this));
	}
}

Interaction* MapView::interaction()
{
	return theInteraction;
}

Projection& MapView::projection()
{
	return theProjection;
}



