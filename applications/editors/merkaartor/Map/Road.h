#ifndef MERKAARTOR_ROAD_H_
#define MERKAARTOR_ROAD_H_

#include <vector>

#include "Map/MapDocument.h"
#include "Map/MapFeature.h"
#include "Map/MapLayer.h"

class RoadPrivate;
class TrackPoint;
class QProgressDialog;

class Road : public MapFeature
{
	public:
		Road(void);
		Road(const Road& other);
		virtual ~Road();
	private:
		void updateMeta() const;

	public:
		virtual QString getClass() const {return "Road";};

		virtual CoordBox boundingBox() const;
		virtual void draw(QPainter& P, const Projection& theProjection);
		virtual void drawFocus(QPainter& P, const Projection& theProjection);
		virtual void drawHover(QPainter& P, const Projection& theProjection);
		virtual double pixelDistance(const QPointF& Target, double ClearEndDistance, const Projection& theProjection) const;
		virtual void cascadedRemoveIfUsing(MapDocument* theDocument, MapFeature* aFeature, CommandList* theList, const std::vector<MapFeature*>& Alternatives);
		virtual bool notEverythingDownloaded() const;
		virtual QString description() const;
		virtual RenderPriority renderPriority(double aPixelPerM) const;

		void add(TrackPoint* Pt);
		void add(TrackPoint* Pt, unsigned int Idx);
		void remove(unsigned int Idx);
		unsigned int size() const;
		unsigned int find(TrackPoint* Pt) const;
		TrackPoint* get(unsigned int idx);
		const TrackPoint* get(unsigned int Idx) const;
		const std::vector<Coord>& smoothed() const;
		bool isClosed();

		virtual void partChanged(MapFeature* F, unsigned int ChangeId);
		virtual void setLayer(MapLayer* aLayer);

		double area() const;
		double distance() const;

		virtual bool deleteChildren(MapDocument* theDocument, CommandList* theList);

		virtual QString toXML(unsigned int lvl=0, QProgressDialog * progress=NULL);
		virtual bool toXML(QDomElement xParent, QProgressDialog & progress);
		static Road* fromXML(MapDocument* d, MapLayer* L, const QDomElement e);

		virtual QString toHtml();
	
		virtual void toBinary(QDataStream& ds);
		static Road* fromBinary(MapDocument* d, MapLayer* L, QDataStream& ds);

	protected:
		RoadPrivate* p;
};

MapFeature::TrafficDirectionType trafficDirection(const Road* R);
double widthOf(const Road* R);
unsigned int findSnapPointIndex(const Road* R, Coord& P);

#endif


