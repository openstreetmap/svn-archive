#ifndef MERKAARTOR_CREATEROADINTERACTION_H_
#define MERKAARTOR_CREATEROADINTERACTION_H_

#include "Interaction/Interaction.h"

class Road;
class Way;

class CreateRoadInteraction : public WaySnapInteraction
{
	public:
		CreateRoadInteraction(MapView* aView);
		CreateRoadInteraction(MapView* aView, Road* R);
		~CreateRoadInteraction(void);

		virtual void paintEvent(QPaintEvent* , QPainter& thePainter);
		virtual void snapMouseReleaseEvent(QMouseEvent* anEvent, Way* W);

	private:
		Road* Current;
};

#endif


