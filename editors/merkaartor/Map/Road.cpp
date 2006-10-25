#include "Map/Road.h"

#include "Map/Painting.h"
#include "Map/Projection.h"
#include "Map/TrackPoint.h"
#include "Map/Way.h"
#include "Utils/LineF.h"

#include <QtGui/QPainter>

#include <algorithm>
#include <vector>

class RoadPrivate
{
	public:
		std::vector<Way*> Ways;
};

Road::Road(void)
: p(new RoadPrivate)
{
}

Road::Road(const Road& )
: p(0)
{
}

Road::~Road(void)
{
	delete p;
}

void Road::add(Way* W)
{
	if (std::find(p->Ways.begin(),p->Ways.end(),W) == p->Ways.end())
		p->Ways.push_back(W);
}

void Road::erase(Way* W)
{
	std::vector<Way*>::iterator i = std::find(p->Ways.begin(),p->Ways.end(),W);
	if (i != p->Ways.end())
		p->Ways.erase(i);
}

unsigned int Road::size() const
{
	return p->Ways.size();
}

Way* Road::get(unsigned int idx)
{
	return p->Ways[idx];
}

const Way* Road::get(unsigned int idx) const
{
	return p->Ways[idx];
}

CoordBox Road::boundingBox() const
{
	if (p->Ways.size())
	{
		CoordBox BBox(p->Ways[0]->boundingBox());
		for (unsigned int i=1; i<p->Ways.size(); ++i)
			BBox.merge(p->Ways[i]->boundingBox());
		return BBox;
	}
	return CoordBox(Coord(0,0),Coord(0,0));
}

static Coord half(Way* W)
{
	if (W->controlFrom() && W->controlTo())
	{
		double H = (W->controlFrom()->position().lat()+W->controlTo()->position().lat())/2;
		double L2 = (W->from()->position().lat()+W->controlFrom()->position().lat())/2;
		double R3 = (W->controlTo()->position().lat()+W->to()->position().lat())/2;
		double L3 = (L2+H)/2;
		double R2 = (H+R3)/2;
		double Lat = (L3+R2)/2;
		H = (W->controlFrom()->position().lon()+W->controlTo()->position().lon())/2;
		L2 = (W->from()->position().lon()+W->controlFrom()->position().lon())/2;
		R3 = (W->controlTo()->position().lon()+W->to()->position().lon())/2;
		L3 = (L2+H)/2;
		R2 = (H+R3)/2;
		double Lon = (L3+R2)/2;
		return Coord(Lat,Lon);
	}
	double Lat = 0.5*(W->from()->position().lat()+W->to()->position().lat());
	double Lon = 0.5*(W->from()->position().lon()+W->to()->position().lon());
	return Coord(Lat,Lon);
}

void Road::draw(QPainter& thePainter, const Projection& theProjection)
{
	thePainter.setBrush(QColor(0x22,0xff,0x22,128));
	thePainter.setPen(QColor(255,255,255,128));
	for (unsigned int i=0; i<p->Ways.size(); ++i)
	{
		Way* W = p->Ways[i];

		QPointF P(theProjection.project(half(W)));
		double Rad = theProjection.pixelPerM()*W->width();
		thePainter.drawEllipse(P.x()-Rad/2,P.y()-Rad/2,Rad,Rad);

		QPen TP;
		if (lastUpdated() == MapFeature::OSMServerConflict)
			TP = QPen(QBrush(QColor(0xff,0,0)),theProjection.pixelPerM()*W->width()/4);
		else
			TP = QPen(QBrush(QColor(0x22,0xff,0x22,128)),theProjection.pixelPerM()*W->width()/4);
		::draw(thePainter,TP,p->Ways[i],theProjection);
	}
}

void Road::drawFocus(QPainter& thePainter, const Projection& theProjection)
{
	QPen TP(QColor(0,0,255));
	thePainter.setPen(TP);
	thePainter.setBrush(QColor(0,0,255));
	for (unsigned int i=0; i<p->Ways.size(); ++i)
	{
		Way* W = p->Ways[i];
		QPointF P(theProjection.project(half(W)));
		double Rad = theProjection.pixelPerM()*W->width();
		thePainter.drawEllipse(P.x()-Rad/2,P.y()-Rad/2,Rad,Rad);
		::draw(thePainter,TP,W,theProjection);
	}
}

double Road::pixelDistance(const QPointF& Target, double ClearEndDistance, const Projection& theProjection) const
{
	double Best = 1000000;
	for (unsigned int i=0; i<p->Ways.size(); ++i)
	{
		double D = p->Ways[i]->pixelDistance(Target,ClearEndDistance,theProjection);
		if (D < ClearEndDistance)
			Best = D;
	}
	// always prefer us over ways
	if (Best<ClearEndDistance)
		Best*=0.99;
	return Best;
}

void Road::cascadedRemoveIfUsing(MapDocument* theDocument, MapFeature* aFeature, CommandList* theList)
{
	// TODO
}



