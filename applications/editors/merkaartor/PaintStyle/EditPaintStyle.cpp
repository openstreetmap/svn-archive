#include "PaintStyle/EditPaintStyle.h"
#include "Map/Painting.h"
#include "Map/Projection.h"
#include "Map/TrackPoint.h"
#include "Map/Relation.h"
#include "Map/Road.h"
#include "PaintStyle/TagSelector.h"
#include "Utils/LineF.h"

#include <QtCore/QFile>
#include <QtCore/QTextStream>
#include <QtGui/QPainter>
#include <QtGui/QPainterPath>
#include <QtXml/QDomDocument>
#include <QtXml/QDomNode>


#include <math.h>
#include <utility>

#define LOCALZOOM		0.05
#define REGIONALZOOM	0.01
#define GLOBALZOOM		0.002

//static bool localZoom(const Projection& theProjection)
//{
//	return theProjection.pixelPerM() < LOCALZOOM;
//}

static bool regionalZoom(const Projection& theProjection)
{
	return theProjection.pixelPerM() < REGIONALZOOM;
}

static bool globalZoom(const Projection& theProjection)
{
	return theProjection.pixelPerM() < GLOBALZOOM;
}

class EditPaintStylePrivate
{
	public:
		EditPaintStylePrivate(QPainter& P, const Projection& aProj)
			: thePainter(P), theProjection(aProj)
		{
			First.setP(this);
			Second.setP(this);
			Third.setP(this);
			Fourth.setP(this);
		}

		QPainter& thePainter;
		const Projection& theProjection;
		EPBackgroundLayer First;
		EPForegroundLayer Second;
		EPTouchupLayer Third;
		EPLabelLayer Fourth;
		bool isTrackPointVisible;
		bool isTrackSegmentVisible;
};

#define ALWAYS 10e6

/* Zoom boundaries : expressed in Pixel per Meter

   eg 0.01->ALWAYS means show a feature from a zoom level of 0.01 Pixel Per M,
   or 100 Meter per Pixel. For a screen of 1000px wide this is when viewing
   100km or less across.

   eg 0.2->ALWAYS means show a feature from a zoom level 0.2 Px/M or 5M/Px which
   is viewing 5km or less across a screen of 1000Px. */

void EPBackgroundLayer::setP(EditPaintStylePrivate* ap)
{
	p = ap;
}


void EPBackgroundLayer::draw(Road* R)
{
	FeaturePainter* paintsel = R->getEditPainter(p->theProjection.pixelPerM());
	if (paintsel)
		paintsel->drawBackground(R,p->thePainter,p->theProjection);
	else if (!globalZoom(p->theProjection) && !R->hasEditPainter())
	{
		QPen thePen(QColor(0,0,0),1);
		if (regionalZoom(p->theProjection))
			thePen = QPen(QColor(0x77,0x77,0x77),1);
		p->thePainter.setPen(thePen);
		p->thePainter.setBrush(Qt::NoBrush);
		p->thePainter.drawPath(R->getPath());
	}
}

void EPBackgroundLayer::draw(Relation* R)
{
	FeaturePainter* paintsel = R->getEditPainter(p->theProjection.pixelPerM());
	if (paintsel)
		paintsel->drawBackground(R,p->thePainter,p->theProjection);
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
	FeaturePainter* paintsel = R->getEditPainter(p->theProjection.pixelPerM());
	if (paintsel)
		paintsel->drawForeground(R,p->thePainter,p->theProjection);
}

void EPForegroundLayer::draw(Relation* R)
{
	FeaturePainter* paintsel = R->getEditPainter(p->theProjection.pixelPerM());
	if (paintsel)
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
	FeaturePainter* paintsel = R->getEditPainter(p->theProjection.pixelPerM());
	if (paintsel)
		paintsel->drawTouchup(R,p->thePainter,p->theProjection);
}

void EPTouchupLayer::draw(Relation* /* R */)
{
}

void EPTouchupLayer::draw(TrackPoint* Pt)
{
	FeaturePainter* paintsel = Pt->getEditPainter(p->theProjection.pixelPerM());
	if (paintsel)
		paintsel->drawTouchup(Pt,p->thePainter,p->theProjection);
	else if (!Pt->hasEditPainter()) {
		if (p->isTrackPointVisible || (Pt->lastUpdated() == MapFeature::Log && !p->isTrackSegmentVisible)) {
			bool Draw = p->theProjection.pixelPerM() > 1;
			if (!Draw && !Pt->sizeParents() && (p->theProjection.pixelPerM() > LOCALZOOM) )
				Draw = true;
			if (Pt->lastUpdated() == MapFeature::Log && !p->isTrackSegmentVisible)
				Draw = true;
			if (Draw)
			{
				QPointF P(p->theProjection.project(Pt->position()));

				if (Pt->findKey("_waypoint_") != Pt->tagSize()) {
					QRectF R(P-QPointF(4,4),QSize(8,8)); 
					p->thePainter.fillRect(R,QColor(255,0,0,128)); 
				}
				
				QRectF R(P-QPointF(2,2),QSize(4,4));
				p->thePainter.fillRect(R,QColor(0,0,0,128));
			}
		}
	}
}

void EPLabelLayer::setP(EditPaintStylePrivate* ap)
{
	p = ap;
}

void EPLabelLayer::draw(Road* R)
{
	FeaturePainter* paintsel = R->getEditPainter(p->theProjection.pixelPerM());
	if (paintsel)
		paintsel->drawLabel(R,p->thePainter,p->theProjection);
}

void EPLabelLayer::draw(Relation* /* R */)
{
}

void EPLabelLayer::draw(TrackPoint* Pt)
{
	FeaturePainter* paintsel = Pt->getEditPainter(p->theProjection.pixelPerM());
	if (paintsel)
		paintsel->drawLabel(Pt,p->thePainter,p->theProjection);
}

/* EDITPAINTSTYLE */

std::vector<FeaturePainter> EditPaintStyle::Painters;

EditPaintStyle::EditPaintStyle(QPainter& P, const Projection& theProjection)
{
	p = new EditPaintStylePrivate(P,theProjection);
	add(&p->First);
	add(&p->Second);
	add(&p->Third);
	add(&p->Fourth);

	p->isTrackPointVisible = M_PREFS->getTrackPointsVisible();
	p->isTrackSegmentVisible = M_PREFS->getTrackSegmentsVisible();
}

EditPaintStyle::~EditPaintStyle(void)
{
	delete p;
}



void savePainters(const QString& filename)
{
	QFile data(filename);
	if (data.open(QFile::WriteOnly | QFile::Truncate))
	{
		QTextStream out(&data);
		out << "<mapStyle>\n";
		for (unsigned int i=0; i<EditPaintStyle::Painters.size(); ++i)
		{
			QString s = EditPaintStyle::Painters[i].asXML();
			out << s;
		}
		out << "</mapStyle>\n";
	}
}

QColor toColor(const QString& s)
{
	return
		QColor(
			s.mid(1,2).toInt(0,16),
			s.mid(3,2).toInt(0,16),
			s.mid(5,2).toInt(0,16),
			s.mid(7,2).toInt(0,16));
}

void readFromNode(const QDomElement& e, FeaturePainter& FP)
{
	if (e.hasAttribute("zoomUnder") || e.hasAttribute("zoomUpper"))
		FP.zoomBoundary(e.attribute("zoomUnder","0").toDouble(),e.attribute("zoomUpper","10e6").toDouble());
	if (e.hasAttribute("foregroundColor"))
	{
		FP.foreground(
			toColor(e.attribute("foregroundColor")),e.attribute("foregroundScale").toDouble(),e.attribute("foregroundOffset").toDouble());
		if (e.hasAttribute("foregroundDashDown"))
			FP.foregroundDash(e.attribute("foregroundDashDown").toDouble(),e.attribute("foregroundDashUp").toDouble());
	}
	if (e.hasAttribute("backgroundColor"))
		FP.background(
			toColor(e.attribute("backgroundColor")),e.attribute("backgroundScale").toDouble(),e.attribute("backgroundOffset").toDouble());
	if (e.hasAttribute("touchupColor"))
	{
		FP.touchup(
			toColor(e.attribute("touchupColor")),e.attribute("touchupScale").toDouble(),e.attribute("touchupOffset").toDouble());
		if (e.hasAttribute("touchupDashDown"))
			FP.touchupDash(e.attribute("touchupDashDown").toDouble(),e.attribute("touchupDashUp").toDouble());
	}
	if (e.hasAttribute("fillColor"))
		FP.foregroundFill(toColor(e.attribute("fillColor")));
	if (e.hasAttribute("icon"))
		FP.setIcon(e.attribute("icon"),e.attribute("iconScale", "0.0").toDouble(),e.attribute("iconOffset", "0.0").toDouble());
	if (e.attribute("drawTrafficDirectionMarks") == "yes")
		FP.drawTrafficDirectionMarks();
	if (e.hasAttribute("labelColor"))
	{
		FP.label(
			toColor(e.attribute("labelColor")),e.attribute("labelScale").toDouble(),e.attribute("labelOffset").toDouble());
		FP.setLabelFont(e.attribute("labelFont"));
		FP.labelTag(e.attribute("labelTag"));
		if (e.hasAttribute("labelBackgroundColor"))
			FP.labelBackground(toColor(e.attribute("labelBackgroundColor")));
		if (e.hasAttribute("labelBackgroundTag"))
			FP.labelBackgroundTag(e.attribute("labelBackgroundTag"));
	}
	QDomNode n = e.firstChild();
	std::vector<std::pair<QString,QString> > Pairs;
	while (!n.isNull())
	{
		if (n.isElement())
		{
			QDomElement t = n.toElement();
			if (t.tagName() == "selector")
			{
				if (t.attribute("key") != "")
					Pairs.push_back(std::make_pair(t.attribute("key"),t.attribute("value")));
				else
				{
					FP.setSelector(t.attribute("expr"));
					return;
				}
			}
		}
		n = n.nextSibling();
	}
	if (Pairs.size() == 1)
		FP.setSelector(new TagSelectorIs(Pairs[0].first,Pairs[0].second));
	else if (Pairs.size())
	{
		bool Same = true;
		for (unsigned int i=1; i<Pairs.size(); ++i)
			if (Pairs[0].first != Pairs[i].first)
				Same = false;
		if (Same)
		{
			std::vector<QString> Options;
			for (unsigned int i=0; i<Pairs.size(); ++i)
				Options.push_back(Pairs[i].second);
			FP.setSelector(new TagSelectorIsOneOf(Pairs[0].first,Options));
		}
		else
		{
			std::vector<TagSelector*> Options;
			for (unsigned int i=0; i<Pairs.size(); ++i)
				Options.push_back(new TagSelectorIs(Pairs[i].first,Pairs[i].second));
			FP.setSelector(new TagSelectorOr(Options));
		}
	}
}

void loadPainters(const QString& filename)
{
	QDomDocument doc;
	QFile file(filename);
	if (!file.open(QIODevice::ReadOnly))
		return;
	if (!doc.setContent(&file))
	{
		file.close();
		return;
	}
	file.close();
	EditPaintStyle::Painters.clear();
	QDomElement docElem = doc.documentElement();
	QDomNode n = docElem.firstChild();
	while(!n.isNull())
	{
		QDomElement e = n.toElement(); // try to convert the node to an element.
		if(!e.isNull() && e.tagName() == "painter")
		{
			FeaturePainter FP;
			readFromNode(e,FP);
			EditPaintStyle::Painters.push_back(FP);
		}
		n = n.nextSibling();
	}
}

