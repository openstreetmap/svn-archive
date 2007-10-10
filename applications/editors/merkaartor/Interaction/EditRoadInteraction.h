#ifndef MERKAARTOR_EDITROADINTERACTION_H_
#define MERKAARTOR_EDITROADINTERACTION_H_

#include "Interaction/Interaction.h"

class Road;
class Way;

class EditRoadInteraction : public FeatureSnapInteraction
{
	public:
		EditRoadInteraction(MapView* aView);
		EditRoadInteraction(MapView* aView, Road* R);
		~EditRoadInteraction(void);

		virtual void paintEvent(QPaintEvent* , QPainter& thePainter);
		virtual void snapMouseReleaseEvent(QMouseEvent* anEvent, MapFeature* W);

	private:
		Road* Current;
};

#endif


