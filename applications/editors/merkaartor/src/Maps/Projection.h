#ifndef MERKATOR_PROJECTION_H_
#define MERKATOR_PROJECTION_H_

#include "Preferences/MerkaartorPreferences.h"
#include "Maps/Coord.h"

#include <QPointF>

#include "QMapControl/mapadapter.h"
#include "QMapControl/layermanager.h"

#include <geometry/geometries/cartesian2d.hpp>
#include <geometry/geometries/latlong.hpp>

#include <geometry/projections/projection.hpp>

#define LAYERMANAGER_OK (layermanager && layermanager->getLayer())

class QRect;
class LayerManager;
class TrackPoint;
class ProjectionPrivate;

typedef projection::projection<geometry::point_ll_deg, geometry::point_2d> ProjProjection;

class Projection
{
	public:
		Projection(void);
		virtual ~Projection(void);

		void setViewport(const CoordBox& Map, const QRect& Screen);
		void panScreen(const QPoint& p, const QRect& Screen);
		CoordBox viewport() const;
		double pixelPerM() const;
		double latAnglePerM() const;
		double lonAnglePerM(double Lat) const;
		QPoint project(const Coord& Map) const;
		QPoint project(TrackPoint* aNode) const;
		Coord inverse(const QPointF& Screen) const;
		void zoom(double d, const QPointF& Around, const QRect& Screen);
		void setCenter(Coord& Center, const QRect& Screen);
		void resize(QSize oldS, QSize newS);

		void setLayerManager(LayerManager* lm);

		virtual bool toXML(QDomElement xParent) const;
		void fromXML(QDomElement e, const QRect & Screen);

#ifndef _MOBILE
		static ProjProjection * getProjection(QString projString);
		bool setProjectionType(ProjectionType aProjectionType);

		QPointF projProject(const Coord& Map) const;
		Coord projInverse(const QPointF& Screen) const;
		static void projTransform(ProjProjection *srcdefn, 
						   ProjProjection *dstdefn, 
						   long point_count, int point_offset, double *x, double *y, double *z );
		void projTransformWGS84(long point_count, int point_offset, double *x, double *y, double *z );
		bool projIsLatLong();
		QRectF getProjectedViewport(QRect& screen);
#endif

	protected:
		double ScaleLat, ScaleLon;
		double DeltaLat, DeltaLon;
		double PixelPerM;
		CoordBox Viewport;
		QPoint screen_middle;
		LayerManager* layermanager;
#ifndef _MOBILE
		ProjProjection *theProj;
#endif

	private:
		ProjectionPrivate* p;

		void viewportRecalc(const QRect& Screen);
		void layerManagerSetViewport(const CoordBox& Map, const QRect& Screen);
		void layerManagerViewportRecalc(const QRect& Screen);
		QPointF screenToCoordinate(QPointF click) const;
		QPoint coordinateToScreen(QPointF click) const;
};


#endif


