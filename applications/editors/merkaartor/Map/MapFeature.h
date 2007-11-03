#ifndef MERKATOR_MAPFEATURE_H_
#define MERKATOR_MAPFEATURE_H_

#include "Map/Coord.h"
#include "PaintStyle/PaintStyle.h"

#include <QtCore/QString>

#include <vector>

class CommandList;
class MapDocument;
class MapLayer;
class Projection;

class QPointF;
class QPainter;

class MapFeaturePrivate;

class MapFeature
{
	public:
		typedef enum { User, UserResolved, OSMServer, OSMServerConflict, NotYetDownloaded } ActorType;
		typedef enum { UnknownDirection, BothWays, OneWay, OtherWay } TrafficDirectionType;
	public:
		MapFeature();
		MapFeature(const MapFeature& other);
		virtual ~MapFeature() = 0;

		virtual CoordBox boundingBox() const = 0;
		virtual void draw(QPainter& P, const Projection& theProjection) = 0;
		virtual void drawFocus(QPainter& P, const Projection& theProjection) = 0;
		virtual double pixelDistance(const QPointF& Target, double ClearEndDistance, const Projection& theProjection) const = 0;
		virtual void cascadedRemoveIfUsing(MapDocument* theDocument, MapFeature* aFeature, CommandList* theList, const std::vector<MapFeature*>& Alternatives) = 0;
		virtual bool notEverythingDownloaded() const = 0;

		void setId(const QString& id);
		const QString& id() const;
		ActorType lastUpdated() const;
		void setLastUpdated(ActorType A);
		void setLayer(MapLayer* aLayer);
		virtual QString description() const = 0;

		void setTag(const QString& k, const QString& v);
		void setTag(unsigned int idx, const QString& k, const QString& v);
		void clearTags();
		void clearTag(const QString& k);
		unsigned int tagSize() const;
		unsigned int findKey(const QString& k) const;
		QString tagValue(unsigned int i) const;
		QString tagValue(const QString& k, const QString& Default) const;
		QString tagKey(unsigned int i) const;
		void removeTag(unsigned int i);

		FeaturePainter* getEditPainter(FeaturePainter::ZoomType Zoom) const;
		bool hasEditPainter() const;

		void setParent(MapFeature* F);
		void unsetParent(MapFeature* F);
		unsigned int sizeParents() const;
		MapFeature* getParent(unsigned int i);
		virtual void addedToDocument() = 0;
		virtual void removedFromDocument() = 0;
		virtual void partChanged(MapFeature* F) = 0;
		void notifyParents();

	private:
		MapFeaturePrivate* p;
};

#endif


