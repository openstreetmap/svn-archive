#ifndef MERKATOR_PROJECTION_H_
#define MERKATOR_PROJECTION_H_

#include "Map/Coord.h"

#include <QtCore/QPointF>

class QRect;

class Projection
{
	public:
		Projection(void);
		~Projection(void);

		void setViewport(const CoordBox& Map, const QRect& Screen); 
		void panScreen(const QPoint& p);
		CoordBox viewport() const;
		QPointF project(const Coord& Map) const;
		double pixelPerM() const;
		double latAnglePerM() const;
		double lonAnglePerM(double Lat) const;
		Coord inverse(const QPointF& Screen) const;
		void zoom(double d, const QRect& Screen);
	private:
		double ScaleLat, DeltaLat, ScaleLon, DeltaLon;
		CoordBox Viewport;
};

#endif


