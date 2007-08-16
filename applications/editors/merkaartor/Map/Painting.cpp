#include "Map/Painting.h"
#include "Map/Projection.h"
#include "Map/TrackPoint.h"
#include "Map/Road.h"
#include "Map/Way.h"
#include "Utils/LineF.h"

#include <QtGui/QPainter>
#include <QtGui/QPainterPath>

#include <utility>

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

/// draws way with oneway markers
void draw(QPainter& thePainter, QPen& thePen, MapFeature::TrafficDirectionType TT, const QPointF& FromF, const QPointF& ToF, double theWidth, const Projection&)
{
	QPainterPath Path;
	Path.moveTo(FromF);
	Path.lineTo(ToF);
	double DistFromCenter = theWidth*2;
	if (distance(FromF,ToF) > std::max(40.0,DistFromCenter*2+4))
	{
		QPointF H(FromF+ToF);
		H *= 0.5;
		double A = angle(FromF-ToF);
		QPointF T(DistFromCenter*cos(A),DistFromCenter*sin(A));
		QPointF V1(theWidth*cos(A+3.141592/6),theWidth*sin(A+3.141592/6));
		QPointF V2(theWidth*cos(A-3.141592/6),theWidth*sin(A-3.141592/6));
//		MapFeature::TrafficDirectionType TT = W->trafficDirection();
		if ( (TT == MapFeature::OtherWay) || (TT == MapFeature::BothWays) )
		{
			thePainter.setPen(QColor(0,0,0));
			thePainter.drawLine(H+T,H+T-V1);
			thePainter.drawLine(H+T,H+T-V2);
		}
		if ( (TT == MapFeature::OneWay) || (TT == MapFeature::BothWays) )
		{
			thePainter.setPen(QColor(0,0,0));
			thePainter.drawLine(H-T,H-T+V1);
			thePainter.drawLine(H-T,H-T+V2);
		}
	}
	thePainter.strokePath(Path,thePen);
}

void draw(QPainter& thePainter, QPen& thePen, MapFeature::TrafficDirectionType TT, const Coord& From, const Coord& To, double theWidth, const Projection& theProjection)
{
	QPointF FromF(theProjection.project(From));
	QPointF ToF(theProjection.project(To));
	draw(thePainter,thePen,TT,FromF,ToF,theWidth,theProjection);
}

void draw(QPainter& thePainter, QPen& thePen, Way* W, double theWidth, const Projection& theProjection)
{
	// due to a bug in Qt 4.20
/*	Path.cubicTo(
		theProjection.project(W->controlFrom()->position()),
		theProjection.project(W->controlTo()->position()),
		theProjection.project(W->to()->position())); */
	if (W->controlFrom() && W->controlTo())
	{
		QPainterPath Path;
		QPointF FromF(theProjection.project(W->from()->position()));
		QPointF ToF(theProjection.project(W->to()->position()));
		Path.moveTo(FromF);
		buildCubicPath(Path,FromF,
			theProjection.project(W->controlFrom()->position()),
			theProjection.project(W->controlTo()->position()),
			ToF);
		thePainter.strokePath(Path,thePen);
	}
	else
		draw(thePainter, thePen,W->trafficDirection(), W->from()->position(), W->to()->position(), theWidth, theProjection);
}




void draw(QPainter& thePainter, QPen& thePen, Way* W, const Projection& theProjection)
{
	QPainterPath Path;
	QPointF FromF(theProjection.project(W->from()->position()));
	QPointF ToF(theProjection.project(W->to()->position()));
	Path.moveTo(FromF);
	// due to a bug in Qt 4.20
/*	Path.cubicTo(
		theProjection.project(W->controlFrom()->position()),
		theProjection.project(W->controlTo()->position()),
		theProjection.project(W->to()->position())); */
	if (W->controlFrom() && W->controlTo())
		buildCubicPath(Path,FromF,
			theProjection.project(W->controlFrom()->position()),
			theProjection.project(W->controlTo()->position()),
			ToF);
	else
		Path.lineTo(ToF);
	thePainter.strokePath(Path,thePen);
}

void drawPossibleArea(QPainter& thePainter, Road* R, const Projection& theProjection)
{
	if (!R->size() )
		return;
	bool Draw = true;
	QString LandUse = R->tagValue("landuse","");
	QString Leisure = R->tagValue("leisure","");
	if (LandUse == "reservoir")
		thePainter.setBrush(QBrush(QColor(0x22,0x22,0xcc,128),Qt::BDiagPattern));
	else if ( (LandUse == "forest") || (Leisure == "park") )
		thePainter.setBrush(QBrush(QColor(0x22,0xcc,0x22,128),Qt::BDiagPattern));
	else if (Leisure == "pitch")
		thePainter.setBrush(QBrush(QColor(0xcc,0x22,0x22,128),Qt::BDiagPattern));
	else
		Draw = false;

	if ( Draw )
	{
		thePainter.setPen(QPen(Qt::NoPen));
		QPainterPath Path;
		Path.moveTo(theProjection.project(R->get(0)->from()->position()));
		for (unsigned int i=0; i<R->size(); ++i)
			Path.lineTo(theProjection.project(R->get(i)->to()->position()));
		thePainter.drawPath(Path);
	}

}


