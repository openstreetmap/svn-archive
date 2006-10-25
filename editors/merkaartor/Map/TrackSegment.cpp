#include "Map/TrackSegment.h"
#include "Command/DocumentCommands.h"
#include "Map/Projection.h"
#include "Map/TrackPoint.h"
#include "Utils/LineF.h"

#include <QtGui/QPainter>

#include <vector>

class TrackSegmentPrivate
{
	public:
		std::vector<TrackPoint*> Points;
};

TrackSegment::TrackSegment(void)
{
	p = new TrackSegmentPrivate;
}

TrackSegment::TrackSegment(const TrackSegment&)
: p(0)
{
}

TrackSegment::~TrackSegment(void)
{
	delete p;
}

void TrackSegment::add(TrackPoint* aPoint)
{
	p->Points.push_back(aPoint);
}

unsigned int TrackSegment::size() const
{
	return p->Points.size();
}

void TrackSegment::draw(QPainter &P, const Projection &theProjection)
{
	P.setPen(QPen(QColor(128,128,128),1));
	for (unsigned int i=1; i<p->Points.size(); ++i)
	{
		P.drawLine(
			theProjection.project(p->Points[i-1]->position()),
			theProjection.project(p->Points[i]->position()) );
	}
}

void TrackSegment::drawFocus(QPainter &, const Projection &)
{
	// Can't be selection
}

CoordBox TrackSegment::boundingBox() const
{
	if (p->Points.size())
	{
		CoordBox Box(p->Points[0]->position(),p->Points[0]->position());
		for (unsigned int i=1; i<p->Points.size(); ++i)
			Box.merge(p->Points[i]->position());
		return Box;
	}
	return CoordBox(Coord(0,0),Coord(0,0));
}

double TrackSegment::pixelDistance(const QPointF& , double , const Projection&) const
{
	// unable to select that one
	return 1000000;
}

void TrackSegment::cascadedRemoveIfUsing(MapDocument* theDocument, MapFeature* aFeature, CommandList* theList)
{
	for (unsigned int i=0; i<p->Points.size(); ++i)
	{
		// TODO don't remove whole list, but just the point in the list
		if (p->Points[i] == aFeature)
		{
			theList->add(new RemoveFeatureCommand(theDocument,this));
			return;
/*			if (p->Points.size() == 1)
				theList->add(new RemoveFeatureCommand(theDocument,this));
			else
				theList->add(new   */
		}
	}
}


