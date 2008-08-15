#ifndef MAPLAYER_H_
#define MAPLAYER_H_

#include "Map/MapDocument.h"

class QString;
class QprogressDialog;

class MapFeature;
class MapLayerPrivate;
class MapAdapter;
class Layer;
class LayerManager;
class LayerWidget;
class WMSMapAdapter;
class TileMapAdapter;
class TrackSegment;

class MapLayer
{
public:
	MapLayer();
	MapLayer(const QString& aName);

private:
	MapLayer(const MapLayer& aLayer);

public:
	virtual ~MapLayer();

	void setName(const QString& aName);
	const QString& name() const;
	void setDescription(const QString& aDesc);
	const QString& description() const;
	bool isVisible() const;
	bool isSelected() const;
	bool isEnabled() const;

	void add(MapFeature* aFeature);
	void add(MapFeature* aFeature, unsigned int Idx);
	virtual void remove(MapFeature* aFeature);
	virtual void clear();
	bool exists(MapFeature* aFeature) const;
	unsigned int size() const;
	int get(MapFeature* aFeature);
	MapFeature* get(unsigned int i);
	const MapFeature* get(unsigned int i) const;
	MapFeature* get(const QString& id);
	void notifyIdUpdate(const QString& id, MapFeature* aFeature);
	void sortRenderingPriority(double PixelPerM);
	void invalidateRenderPriority();

	void setDocument(MapDocument* aDocument);
	MapDocument* getDocument();

	LayerWidget* getWidget(void);
	void deleteWidget(void);
	virtual void updateWidget() {};

	virtual void setVisible(bool b) = 0;
	virtual void setSelected(bool b);
	virtual void setEnabled(bool b);
	virtual LayerWidget* newWidget(void) = 0;

	virtual void setAlpha(const qreal alpha);
	virtual qreal getAlpha() const;

	void setId(const QString& id);
	const QString& id() const;

	virtual QString toMainHtml();
	virtual QString toHtml();
	virtual bool toXML(QDomElement xParent, QProgressDialog & progress) = 0;

	static CoordBox boundingBox(const MapLayer* theLayer);

	virtual const QString className() = 0;

	unsigned int incDirtyLevel(unsigned int inc=1);
	unsigned int decDirtyLevel(unsigned int inc=1);
	unsigned int getDirtyLevel();
	unsigned int setDirtyLevel(unsigned int newLevel);

	virtual bool canDelete();
	virtual bool isUploadable() {return true;};
	virtual bool isTrack() {return false;};

protected:
	MapLayerPrivate* p;
	mutable QString Id;
};

class DrawingMapLayer : public MapLayer
{
public:
	DrawingMapLayer(const QString& aName);
	virtual ~DrawingMapLayer();

	virtual void setVisible(bool b);
	virtual LayerWidget* newWidget(void);

	virtual bool toXML(QDomElement xParent, QProgressDialog & progress);
	static DrawingMapLayer* fromXML(MapDocument* d, const QDomElement e, QProgressDialog & progress);
	static DrawingMapLayer* doFromXML(DrawingMapLayer* l, MapDocument* d, const QDomElement e, QProgressDialog & progress);

	virtual const QString className() {return "DrawingMapLayer";};
};

class ImageMapLayer : public QObject, public MapLayer
{
Q_OBJECT
public:
	ImageMapLayer() : layermanager(0) {};
	ImageMapLayer(const QString& aName, LayerManager* aLayerMgr=NULL);
	virtual ~ImageMapLayer();

	Layer* imageLayer();
	void setMapAdapter(ImageBackgroundType typ);
	LayerManager* layermanager;

	virtual void setVisible(bool b);
	virtual LayerWidget* newWidget(void);
	virtual void updateWidget();

	virtual bool toXML(QDomElement xParent, QProgressDialog & progress);
	static ImageMapLayer* fromXML(MapDocument* d, const QDomElement e, QProgressDialog & progress);

	virtual const QString className() {return "ImageMapLayer";};

private:
	WMSMapAdapter* wmsa;
	TileMapAdapter* tmsa;
};

class TrackMapLayer : public QObject, public MapLayer
{
Q_OBJECT
public:
	TrackMapLayer(const QString& aName="", const QString& filaname="");
	virtual ~TrackMapLayer();

	virtual void setVisible(bool b);
	virtual LayerWidget* newWidget(void);

	virtual void extractLayer();
	virtual const QString getFilename();

	virtual bool toXML(QDomElement xParent, QProgressDialog & progress);
	static TrackMapLayer* fromXML(MapDocument* d, const QDomElement e, QProgressDialog & progress);

	virtual const QString className() {return "TrackMapLayer";};
	virtual bool isUploadable() {return true;};
	virtual bool isTrack() {return true;};

protected:
	QString Filename;
};

class DirtyMapLayer : public DrawingMapLayer
{
public:
	DirtyMapLayer(const QString& aName);
	virtual ~DirtyMapLayer();

	static DirtyMapLayer* fromXML(MapDocument* d, const QDomElement e, QProgressDialog & progress);

	virtual const QString className() {return "DirtyMapLayer";};
	virtual LayerWidget* newWidget(void);
};

class UploadedMapLayer : public DrawingMapLayer
{
public:
	UploadedMapLayer(const QString& aName);
	virtual ~UploadedMapLayer();

	static UploadedMapLayer* fromXML(MapDocument* d, const QDomElement e, QProgressDialog & progress);

	virtual const QString className() {return "UploadedMapLayer";};
	virtual LayerWidget* newWidget(void);
};

class ExtractedMapLayer : public DrawingMapLayer
{
public:
	ExtractedMapLayer(const QString& aName);
	virtual ~ExtractedMapLayer();

	static ExtractedMapLayer* fromXML(MapDocument* d, const QDomElement e, QProgressDialog & progress);

	virtual const QString className() {return "ExtractedMapLayer";};
	virtual LayerWidget* newWidget(void);

	virtual bool isUploadable() {return false;};
};

class DeletedMapLayer : public DrawingMapLayer
{
public:
	DeletedMapLayer(const QString& aName);
	virtual ~DeletedMapLayer();

	static DeletedMapLayer* fromXML(MapDocument* d, const QDomElement e, QProgressDialog & progress);

	virtual const QString className() {return "DeletedMapLayer";};
	virtual LayerWidget* newWidget(void);

	virtual bool isUploadable() {return false;};
};

#endif


