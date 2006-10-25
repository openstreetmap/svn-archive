#ifndef MERKAARTOR_CREATENODEINTERATION_H_
#define MERKAARTOR_CREATENODEINTERATION_H_

#include "Interaction/Interaction.h"

class CreateNodeInteraction : public Interaction
{
	public:
		CreateNodeInteraction(MapView* aView);
		~CreateNodeInteraction(void);

		virtual void mouseReleaseEvent(QMouseEvent * event);
		virtual QCursor cursor() const;
};

#endif