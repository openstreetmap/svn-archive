#include "TrackPoint.h"

#include "Map/Projection.h"
#include "Utils/LineF.h"

#include <QtGui/QPainter>

TrackPoint::TrackPoint(const Coord& aCoord)
: Position(aCoord)
{
}

TrackPoint::~TrackPoint(void)
{
}

const Coord& TrackPoint::position() const
{
	return Position;
}

void TrackPoint::setPosition(const Coord& aCoord)
{
	Position = aCoord;
}

CoordBox TrackPoint::boundingBox() const
{
	return CoordBox(Position,Position);
}

void TrackPoint::draw(QPainter& thePainter, const Projection& theProjection)
{
	QPointF P(theProjection.project(Position));
	QRectF R(P-QPointF(2,2),QSize(4,4));
	if (lastUpdated() == MapFeature::OSMServerConflict)
		thePainter.fillRect(R,QColor(255,0,0));
	else if (theProjection.pixelPerM() > 1)
		thePainter.fillRect(R,QColor(0,0,0,128));
}

void TrackPoint::drawFocus(QPainter& thePainter, const Projection& theProjection)
{
	thePainter.setPen(QColor(0,0,255));
	QPointF P(theProjection.project(Position));
	QRectF R(P-QPointF(3,3),QSize(6,6));
	thePainter.drawRect(R);
}

double TrackPoint::pixelDistance(const QPointF& Target, double, const Projection& theProjection) const
{
	return distance(Target,theProjection.project(Position));
}

void TrackPoint::cascadedRemoveIfUsing(MapDocument*, MapFeature*, CommandList*, const std::vector<MapFeature*>&)
{
}




