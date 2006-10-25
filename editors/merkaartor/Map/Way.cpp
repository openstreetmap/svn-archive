#include "Map/Way.h"
#include "Command/DocumentCommands.h"
#include "Map/Painting.h"
#include "Map/Projection.h"
#include "Map/TrackPoint.h"
#include "Utils/LineF.h"

#include <QtGui/QPainter>
#include <QtGui/QPainterPath>

#include <math.h>

#define DEFAULTWIDTH 4

Way::Way(TrackPoint* aFrom, TrackPoint* aC1, TrackPoint* aC2, TrackPoint* aTo)
: From(aFrom), To(aTo), ControlFrom(aC1), ControlTo(aC2)
{
}

Way::Way(TrackPoint* aFrom, TrackPoint* aTo)
: From(aFrom), To(aTo), ControlFrom(0), ControlTo(0)
{
}

Way::~Way(void)
{
}

void Way::cascadedRemoveIfUsing(MapDocument* theDocument, MapFeature* F, CommandList* theList)
{
	if ( (To == F) || (From == F) || (ControlFrom ==F) || (ControlTo == F) )
		theList->add(new RemoveFeatureCommand(theDocument,this));
}

double Way::width() const
{
	unsigned int i=findKey("width");
	if (i<tagSize())
		return tagValue(i).toDouble();
	return DEFAULTWIDTH;
}

void Way::setWidth(double w)
{
	setTag("width",QString::number(w));
}

CoordBox Way::boundingBox() const
{
	return CoordBox(From->position(),To->position());
}

void Way::setFromTo(TrackPoint* aFrom, TrackPoint* aTo)
{
	From = aFrom;
	To = aTo;
}

TrackPoint* Way::from()
{
	return From;
}

TrackPoint* Way::to()
{
	return To;
}

TrackPoint* Way::controlFrom()
{
	return ControlFrom;
}

TrackPoint* Way::controlTo()
{
	return ControlTo;
}


const TrackPoint* Way::from() const
{
	return From;
}

const TrackPoint* Way::to() const
{
	return To;
}


static double pixelDistance(const QPointF& Target, const QPointF& P1, const QPointF& P2, const QPointF& P3, const QPointF& P4)
{
	LineF L(P1,P4);
	double D2 = L.distance(P2);
	double D3 = L.distance(P3);
	if ( (D2 < 0.5) && (D3<0.5) )
		return L.distance(Target);
	else
	{
		QPointF H = (P2+P3)/2;
		QPointF L2 = (P1+P2)/2;
		QPointF R3 = (P3+P4)/2;
		QPointF L3 = (L2+H)/2;
		QPointF R2 = (H+R3)/2;
		QPointF L4 = (L3+R2)/2;
		double A = pixelDistance(Target,P1,L2,L3,L4);
		double B = pixelDistance(Target,L4,R2,R3,P4);
		return A<B?A:B;
	}
}


void Way::draw(QPainter& P, const Projection& theProjection)
{
	double WW = theProjection.pixelPerM()*width();
	if (WW<1)
		WW = 1;
	QPen TP;
	if (lastUpdated() == MapFeature::OSMServerConflict)
		TP = QPen(QBrush(QColor(0xff,0,0)),WW);
	else
		TP = QPen(QBrush(QColor(0xff,0x88,0x22,128)),WW);
	::draw(P,TP,this,theProjection);
}

void Way::drawFocus(QPainter& P, const Projection& theProjection)
{
	QPen TP(QBrush(QColor(0x00,0x00,0xff,128)),theProjection.pixelPerM()*width()/2+1);
	QPainterPath Path;
	::draw(P,TP,this,theProjection);
}


double Way::pixelDistance(const QPointF& Target, double ClearEndDistance, const Projection& theProjection) const
{
	QPointF F(theProjection.project(From->position()));
	QPointF T(theProjection.project(To->position()));
	if (distance(Target,F) < ClearEndDistance)
		return ClearEndDistance;
	if (distance(Target,T) < ClearEndDistance)
		return ClearEndDistance;
	if (ControlFrom && ControlTo)
	{
		QPointF CF(theProjection.project(ControlFrom->position()));
		QPointF CT(theProjection.project(ControlTo->position()));
		if (distance(Target,CF) < ClearEndDistance)
			return ClearEndDistance;
		if (distance(Target,CT) < ClearEndDistance)
			return ClearEndDistance;
		return ::pixelDistance(Target,F,CF,CT,T);
	}
	else
	{
		LineF L(F,T);
		return L.capDistance(Target);
	}
}
