#include "Interaction.h"

#include "MapView.h"
#include "Map/MapDocument.h"
#include "Map/Projection.h"
#include "Map/TrackPoint.h"

#include <QtGui/QMouseEvent>
#include <QtGui/QPainter>

#include <math.h>

Interaction::Interaction(MapView* aView)
: theView(aView)
{
}

Interaction::~Interaction()
{
}

MainWindow* Interaction::main()
{
	return theView->main();
}

MapView* Interaction::view()
{
	return theView;
}

MapDocument* Interaction::document()
{
	return theView->document();
}

const Projection& Interaction::projection() const
{
	return theView->projection();
}

void Interaction::mousePressEvent(QMouseEvent * )
{
}

void Interaction::mouseReleaseEvent(QMouseEvent * )
{
}

void Interaction::mouseMoveEvent(QMouseEvent* )
{
}

void Interaction::paintEvent(QPaintEvent*, QPainter&)
{
}

QCursor Interaction::cursor() const
{
	return QCursor(Qt::ArrowCursor);
}



