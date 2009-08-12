#ifndef MERKATOR_PROJECTION_H_
#define MERKATOR_PROJECTION_H_

#include "Preferences/MerkaartorPreferences.h"
#include "Maps/Coord.h"

#include <QPointF>

#include "QMapControl/mapadapter.h"

#include <ggl/geometries/cartesian2d.hpp>
#include <ggl/geometries/latlong.hpp>

#include <ggl/projections/projection.hpp>

class QRect;
class TrackPoint;
class ProjectionPrivate;

typedef ggl::projection::projection<ggl::point_ll_deg, ggl::point_2d> ProjProjection;

class Projection
{
	public:
		Projection(void);
		virtual ~Projection(void);

		double latAnglePerM() const;
		double lonAnglePerM(double Lat) const;
		QPointF project(const Coord& Map) const;
		QPointF project(TrackPoint* aNode) const;
		Coord inverse(const QPointF& Screen) const;

#ifndef _MOBILE
		static ProjProjection * getProjection(QString projString);
		bool setProjectionType(ProjectionType aProjectionType);

		static void projTransform(ProjProjection *srcdefn, 
						   ProjProjection *dstdefn, 
						   long point_count, int point_offset, double *x, double *y, double *z );
		void projTransformToWGS84(long point_count, int point_offset, double *x, double *y, double *z );
		void projTransformFromWGS84(long point_count, int point_offset, double *x, double *y, double *z );
		bool projIsLatLong();
		QRectF getProjectedViewport(CoordBox& Viewport, QRect& screen);

		int projectionRevision() const;
#endif

	protected:
#ifndef _MOBILE
		ProjProjection *theProj;
		QPointF projProject(const Coord& Map) const;
		Coord projInverse(const QPointF& Screen) const;
#endif

	private:
		ProjectionPrivate* p;
};


#endif


