#ifndef MERKAARTOR_ROAD_H_
#define MERKAARTOR_ROAD_H_

#include "Map/MapFeature.h"

class RoadPrivate;
class Way;

class Road : public MapFeature
{
	public:
		Road(void);
		virtual ~Road();
	private:
		Road(const Road& other);

	public:
		virtual CoordBox boundingBox() const;
		virtual void draw(QPainter& P, const Projection& theProjection);
		virtual void drawFocus(QPainter& P, const Projection& theProjection);
		virtual double pixelDistance(const QPointF& Target, double ClearEndDistance, const Projection& theProjection) const;
		virtual void cascadedRemoveIfUsing(MapDocument* theDocument, MapFeature* aFeature, CommandList* theList);

		void add(Way* W);
		void erase(Way* W);
		unsigned int size() const;
		Way* get(unsigned int idx);
		const Way* get(unsigned int Idx) const;

		RoadPrivate* p;
};

#endif


