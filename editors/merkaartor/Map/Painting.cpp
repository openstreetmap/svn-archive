#include "Map/Painting.h"
#include "Map/Projection.h"
#include "Map/TrackPoint.h"
#include "Map/Way.h"
#include "Utils/LineF.h"

#include <QtGui/QPainter>
#include <QtGui/QPainterPath>



static void buildCubicPath(QPainterPath& Path, const QPointF& P1, const QPointF& P2, const QPointF& P3, const QPointF& P4)
{
	LineF L(P1,P4);
	double D2 = L.distance(P2);
	double D3 = L.distance(P3);
	if ( (D2 < 0.5) && (D3<0.5) )
		Path.lineTo(P4);
	else
	{
		QPointF H = (P2+P3)/2;
		QPointF L2 = (P1+P2)/2;
		QPointF R3 = (P3+P4)/2;
		QPointF L3 = (L2+H)/2;
		QPointF R2 = (H+R3)/2;
		QPointF L4 = (L3+R2)/2;
		buildCubicPath(Path,P1,L2,L3,L4);
		buildCubicPath(Path,L4,R2,R3,P4);
		
	}
}

void draw(QPainter& thePainter, QPen& thePen, Way* W, const Projection& theProjection)
{
	QPainterPath Path;
	Path.moveTo(theProjection.project(W->from()->position()));
	// due to a bug in Qt 4.20
/*	Path.cubicTo(
		theProjection.project(W->controlFrom()->position()),
		theProjection.project(W->controlTo()->position()),
		theProjection.project(W->to()->position())); */
	if (W->controlFrom() && W->controlTo())
		buildCubicPath(Path,theProjection.project(W->from()->position()),
			theProjection.project(W->controlFrom()->position()),
			theProjection.project(W->controlTo()->position()),
			theProjection.project(W->to()->position()));
	else
		Path.lineTo(theProjection.project(W->to()->position()));
	thePainter.strokePath(Path,thePen);

}



