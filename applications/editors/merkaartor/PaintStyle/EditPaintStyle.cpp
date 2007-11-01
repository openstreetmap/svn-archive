#include "PaintStyle/EditPaintStyle.h"
#include "Map/Projection.h"
#include "Map/TrackPoint.h"
#include "Map/Road.h"
#include "Utils/LineF.h"

#include <QtGui/QPainter>
#include <QtGui/QPainterPath>

#include <math.h>
#include <utility>

static bool localZoom(const Projection& theProjection)
{
	return theProjection.pixelPerM() < 0.25;
}

static bool regionalZoom(const Projection& theProjection)
{
	return theProjection.pixelPerM() < 0.05;
}

static bool globalZoom(const Projection& theProjection)
{
	return theProjection.pixelPerM() < 0.01;
}


/* FEATUREPAINTSELECTOR */

class EPBackgroundLayer : public PaintStyleLayer
{
	public:
		void setP(EditPaintStylePrivate* p);
		virtual void draw(Road* R);
		virtual void draw(TrackPoint* Pt);
	private:
		EditPaintStylePrivate* p;
};

class EPForegroundLayer : public PaintStyleLayer
{
	public:
		void setP(EditPaintStylePrivate* p);
		virtual void draw(Road* R);
		virtual void draw(TrackPoint* Pt);
	private:
		EditPaintStylePrivate* p;
};

class EPTouchupLayer : public PaintStyleLayer
{
	public:
		void setP(EditPaintStylePrivate* p);
		virtual void draw(Road* R);
		virtual void draw(TrackPoint* Pt);
	private:
		EditPaintStylePrivate* p;
};

class EditPaintStylePrivate
{
	public:
		EditPaintStylePrivate(QPainter& P, const Projection& aProj)
			: thePainter(P), theProjection(aProj)
		{
			First.setP(this);
			Second.setP(this);
			Third.setP(this);
			initPainters();
		}

		void initPainters();
		FeaturePainter *findPainter(Road *R);
		FeaturePainter *findPainter(TrackPoint *Pt);

		QPainter& thePainter;
		const Projection& theProjection;
		EPBackgroundLayer First;
		EPForegroundLayer Second;
		EPTouchupLayer Third;
		std::vector<FeaturePainter> Painters;
};

void EditPaintStylePrivate::initPainters()
{
	FeaturePainter MotorWay;
	MotorWay.background(QColor(0xff,0,0),1,0).foreground(QColor(0xff,0xff,0),0.5,0);
	MotorWay.selectOnTag("highway","motorway","motorway_link");
	MotorWay.drawTrafficDirectionMarks();
	Painters.push_back(MotorWay);

	FeaturePainter Trunk;
	Trunk.foreground(QColor(0xff,0,0),1,0);
	Trunk.selectOnTag("highway","trunk","trunk_link");
	Trunk.drawTrafficDirectionMarks();
	Painters.push_back(Trunk);

	FeaturePainter Primary;
	Primary.foreground(QColor(0,0xff,0),1,0);
	Primary.selectOnTag("highway","primary","primary_link").limitToZoom(FeaturePainter::GlobalZoom);
	Primary.drawTrafficDirectionMarks();
	Painters.push_back(Primary);

	FeaturePainter Secondary;
	Secondary.foreground(QColor(0xff,0xaa,0),1,0);
	Secondary.selectOnTag("highway","secondary","secondary_link").limitToZoom(FeaturePainter::RegionalZoom);
	Secondary.drawTrafficDirectionMarks();
	Painters.push_back(Secondary);

	FeaturePainter Tertiary;
	Tertiary.foreground(QColor(0xff,0x55,0x55),1,0);
	Tertiary.selectOnTag("highway","tertiary","tertiary_link").limitToZoom(FeaturePainter::RegionalZoom);
	Tertiary.drawTrafficDirectionMarks();
	Painters.push_back(Tertiary);

	FeaturePainter Cycleway;
	Cycleway.foreground(QColor(0,0,0xff),1,0).foregroundDash(2,2);
	Cycleway.selectOnTag("highway","cycleway").limitToZoom(FeaturePainter::LocalZoom);
	Painters.push_back(Cycleway);

	FeaturePainter Footway;
	Footway.foreground(QColor(0,0,0),1,0).foregroundDash(2,2);
	Footway.selectOnTag("highway","footway","track").limitToZoom(FeaturePainter::LocalZoom);
	Painters.push_back(Footway);

	FeaturePainter Pedestrian;
	Pedestrian.foreground(QColor(0xaa,0xaa,0xaa),1,0);
	Pedestrian.selectOnTag("highway","pedestrian").limitToZoom(FeaturePainter::LocalZoom);
	Painters.push_back(Pedestrian);

	FeaturePainter Residential;
	Residential.background(QColor(0x77,0x77,0x77),1,0).foreground(QColor(0xff,0xff,0xff),1,-2);
	Residential.selectOnTag("highway","residential","unclassified").limitToZoom(FeaturePainter::LocalZoom);
	Residential.drawTrafficDirectionMarks();
	Painters.push_back(Residential);

	FeaturePainter Railway;
	Railway.background(QColor(0,0,0),1,0).foreground(QColor(0xff,0xff,0xff),1,-3).touchup(QColor(0,0,0),1,-3).touchupDash(3,3);
	Railway.selectOnTag("railway","rail").limitToZoom(FeaturePainter::GlobalZoom);
	Painters.push_back(Railway);

	FeaturePainter Park;
	Park.foregroundFill(QColor(0x77,0xff,0x77,0x77)).foreground(QColor(0,0x77,0),0,1);
	Park.selectOnTag("leisure","park").limitToZoom(FeaturePainter::GlobalZoom);
	Painters.push_back(Park);

	FeaturePainter Pitch;
	Pitch.foregroundFill(QColor(0xff,0x77,0x77,0x77)).foreground(QColor(0x77,0,0),0,1);
	Pitch.selectOnTag("leisure","pitch").limitToZoom(FeaturePainter::GlobalZoom);
	Painters.push_back(Pitch);

	FeaturePainter Water;
	Water.foregroundFill(QColor(0x77,0x77,0xff,0x77)).foreground(QColor(0,0,0x77),0,1);
	Water.selectOnTag("natural","water").limitToZoom(FeaturePainter::GlobalZoom);
	Painters.push_back(Water);

	FeaturePainter Parking;
	Parking.trackPointIcon(":/Art/Mapnik/parking.png").limitToZoom(FeaturePainter::LocalZoom);
	Parking.foregroundFill(QColor(0xf6,0xee,0xb6,0x77));
	Parking.selectOnTag("amenity","parking");
	Parking.limitToZoom(FeaturePainter::LocalZoom);
	Painters.push_back(Parking);
}

FeaturePainter *EditPaintStylePrivate::findPainter(Road *R)
{
	double PixelPerM = theProjection.pixelPerM();
	for (unsigned int i=0; i < Painters.size(); ++i)
	{
		if (Painters[i].isHit(R,PixelPerM))
			return &Painters[i];
	}
	return 0;
}

FeaturePainter *EditPaintStylePrivate::findPainter(TrackPoint *Pt)
{
	double PixelPerM = theProjection.pixelPerM();
	for (unsigned int i=0; i < Painters.size(); ++i)
	{
		if (Painters[i].isHit(Pt, PixelPerM))
			return &Painters[i];
	}
	return 0;
}


void EPBackgroundLayer::setP(EditPaintStylePrivate* ap)
{
	p = ap;
}


void EPBackgroundLayer::draw(Road* R)
{
	if (p->theProjection.viewport().disjunctFrom(R->boundingBox())) return;
	if (FeaturePainter *paintsel = p->findPainter(R))
	{
		paintsel->drawBackground(R,p->thePainter,p->theProjection);
		return;
	}
	if (globalZoom(p->theProjection))
		return;
	QPen thePen(QColor(0,0,0),1);
	if (regionalZoom(p->theProjection))
		thePen = QPen(QColor(0x77,0x77,0x77),1);
	QPainterPath Path;
	buildPathFromRoad(R, p->theProjection, Path);
	p->thePainter.strokePath(Path,thePen);
}

void EPBackgroundLayer::draw(TrackPoint*)
{
}

void EPForegroundLayer::setP(EditPaintStylePrivate* ap)
{
	p = ap;
}

void EPForegroundLayer::draw(Road* R)
{
	if (p->theProjection.viewport().disjunctFrom(R->boundingBox())) return;
	if (FeaturePainter *paintsel = p->findPainter(R))
		paintsel->drawForeground(R,p->thePainter,p->theProjection);
}

void EPForegroundLayer::draw(TrackPoint*)
{
}

void EPTouchupLayer::setP(EditPaintStylePrivate* ap)
{
	p = ap;
}

void EPTouchupLayer::draw(Road* R)
{
	if (p->theProjection.viewport().disjunctFrom(R->boundingBox())) return;
	if (FeaturePainter *paintsel = p->findPainter(R))
		paintsel->drawTouchup(R,p->thePainter,p->theProjection);
}

void EPTouchupLayer::draw(TrackPoint* Pt)
{
	if (p->theProjection.viewport().disjunctFrom(Pt->boundingBox())) return;
	if (FeaturePainter *paintsel = p->findPainter(Pt))
		paintsel->drawTouchup(Pt,p->thePainter,p->theProjection);

	QPointF P(p->theProjection.project(Pt->position()));
	if (p->theProjection.pixelPerM() > 1)
	{
		QRectF R(P-QPointF(2,2),QSize(4,4));
		p->thePainter.fillRect(R,QColor(0,0,0,128));
	}
}

/* EDITPAINTSTYLE */


EditPaintStyle::EditPaintStyle(QPainter& P, const Projection& theProjection)
{
	p = new EditPaintStylePrivate(P,theProjection);
	add(&p->First);
	add(&p->Second);
	add(&p->Third);
}

EditPaintStyle::~EditPaintStyle(void)
{
	delete p;
}
