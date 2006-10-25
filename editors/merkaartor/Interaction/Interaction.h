#ifndef MERKATOR_INTERACTION_H_
#define MERKATOR_INTERACTION_H_

class TrackPoint;
class MainWindow;
class Projection;
class TrackPoint;
class Way;

class QMouseEvent;
class QPaintEvent;
class QPainter;

#include "MapView.h"
#include "Map/MapDocument.h"
#include "Map/MapFeature.h"

#include <QtCore/QObject>
#include <QtGui/QCursor>
#include <QtGui/QMouseEvent>

class Interaction : public QObject
{
	Q_OBJECT
	public:
		Interaction(MapView* theView);
		virtual ~Interaction();

		virtual void mousePressEvent(QMouseEvent * event);
		virtual void mouseReleaseEvent(QMouseEvent * event);
		virtual void mouseMoveEvent(QMouseEvent* event);
		virtual void paintEvent(QPaintEvent* anEvent, QPainter& thePainter);

		virtual QCursor cursor() const;
		MapView* view();
		MapDocument* document();
		MainWindow* main();
		const Projection& projection() const;
	private:
		MapView* theView;
};

template<class FeatureType>
class GenericFeatureSnapInteraction : public Interaction
{
	public:
		GenericFeatureSnapInteraction(MapView* theView)
			: Interaction(theView), LastSnap(0)
		{
		}

		virtual void paintEvent(QPaintEvent* , QPainter& thePainter)
		{
			if (LastSnap)
				LastSnap->drawFocus(thePainter, projection());
		}
		virtual void mousePressEvent(QMouseEvent * event)
		{
			updateSnap(event);
			snapMousePressEvent(event,LastSnap);
		}
		virtual void mouseReleaseEvent(QMouseEvent * event)
		{
			updateSnap(event);
			snapMouseReleaseEvent(event,LastSnap);
		}
		virtual void mouseMoveEvent(QMouseEvent* event)
		{
			updateSnap(event);
			snapMouseMoveEvent(event, LastSnap);
		}
		virtual void snapMousePressEvent(QMouseEvent * , FeatureType*)
		{
		}
		virtual void snapMouseReleaseEvent(QMouseEvent * , FeatureType*)
		{
		}
		virtual void snapMouseMoveEvent(QMouseEvent* , FeatureType*)
		{
		}
	private:
		void updateSnap(QMouseEvent* event)
		{
			FeatureType* Prev = LastSnap;
			LastSnap = 0;
			double BestDistance = 5;
			for (VisibleFeatureIterator it(document()); !it.isEnd(); ++it)
			{
				FeatureType* Pt = dynamic_cast<FeatureType*>(it.get());
				if (Pt)
				{
					double Distance = Pt->pixelDistance(event->pos(), 5.01, projection());
					if (Distance < BestDistance)
					{
						BestDistance = Distance;
						LastSnap = Pt;
					}
				}
			}
			if (Prev != LastSnap)
				view()->update();
		}

		FeatureType* LastSnap;
};

typedef GenericFeatureSnapInteraction<MapFeature> FeatureSnapInteraction;
typedef GenericFeatureSnapInteraction<TrackPoint> TrackPointSnapInteraction;
typedef GenericFeatureSnapInteraction<Way> WaySnapInteraction;

#endif


