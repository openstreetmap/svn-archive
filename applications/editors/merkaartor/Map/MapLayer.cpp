#include "Map/MapLayer.h"
#include "Map/MapFeature.h"
#include "MapView.h"
#include "LayerWidget.h"

#include "QMapControl/mapadapter.h"
#include "QMapControl/osmmapadapter.h"
#include "QMapControl/wmsmapadapter.h"
#ifdef yahoo_illegal
	#include "QMapControl/yahoomapadapter.h"
#endif
#include "QMapControl/layer.h"
#include "QMapControl/layermanager.h"

#include <QtCore/QString>
#include <QMultiMap>

#include <algorithm>
#include <map>
#include <vector>

#define SAFE_DELETE(x) {delete (x); x = NULL;}

/* MAPLAYER */

class MapLayerPrivate
{
public:
	MapLayerPrivate()
		: RenderPriorityUpToDate(false)
	{
		layer_bg = NULL;
		theWidget = NULL;
		selected = false;
	}
	~MapLayerPrivate()
	{
		for (unsigned int i=0; i<Features.size(); ++i)
			delete Features[i];
	}
	std::vector<MapFeature*> Features;
	std::map<QString, MapFeature*> IdMap;
	QString Name;
	bool Visible;
	bool selected;
	LayerWidget* theWidget;

	ImageBackgroundType bgType;
	Layer* layer_bg;

	bool RenderPriorityUpToDate;
        MapDocument* theDocument;
 	double RenderPriorityForPixelPerM;

 	void sortRenderingPriority(double PixelPerM);
};

class SortAccordingToRenderingPriority
{
	public:
 		SortAccordingToRenderingPriority(double aPixelPerM)
 			: PixelPerM(aPixelPerM)
		{
		}
		bool operator()(MapFeature* A, MapFeature* B)
		{
 			return A->renderPriority(PixelPerM) < B->renderPriority(PixelPerM);
		}

 		double PixelPerM;
};

 void MapLayerPrivate::sortRenderingPriority(double aPixelPerM)
{
 	std::sort(Features.begin(),Features.end(),SortAccordingToRenderingPriority(aPixelPerM));
  	RenderPriorityUpToDate = true;
 	RenderPriorityForPixelPerM = aPixelPerM;
}

MapLayer::MapLayer(const QString& aName)
:  p(new MapLayerPrivate)
{
	p->Name = aName;
}

MapLayer::MapLayer(const MapLayer&)
: p(0)
{
}

MapLayer::~MapLayer()
{
	delete p;
}

void MapLayer::sortRenderingPriority(double aPixelPerM)
{
	if (!p->RenderPriorityUpToDate || (aPixelPerM != p->RenderPriorityForPixelPerM) )
		p->sortRenderingPriority(aPixelPerM);
}

void MapLayer::invalidateRenderPriority()
{
	p->RenderPriorityUpToDate = false;
}

void MapLayer::setName(const QString& s)
{
	p->Name = s;
}

const QString& MapLayer::name() const
{
	return p->Name;
}

bool MapLayer::isVisible() const
{
	return p->Visible;
}

void MapLayer::setSelected(bool b) {
	p->selected = b;
}

bool MapLayer::isSelected() const
{
	return p->selected;
}

void MapLayer::add(MapFeature* aFeature)
{
	p->Features.push_back(aFeature);
	notifyIdUpdate(aFeature->id(),aFeature);
	aFeature->setLayer(this);
	p->RenderPriorityUpToDate = false;
}

void MapLayer::add(MapFeature* aFeature, unsigned int Idx)
{
	add(aFeature);
	std::rotate(p->Features.begin()+Idx,p->Features.end()-1,p->Features.end());
	aFeature->setLayer(this);
	p->RenderPriorityUpToDate = false;
}

void MapLayer::notifyIdUpdate(const QString& id, MapFeature* aFeature)
{
	p->IdMap[id] = aFeature;
}

void MapLayer::remove(MapFeature* aFeature)
{
	std::vector<MapFeature*>::iterator i = std::find(p->Features.begin(),p->Features.end(), aFeature);
	if (i != p->Features.end())
	{
		p->Features.erase(i);
		aFeature->setLayer(0);
		notifyIdUpdate(aFeature->id(),0);
		p->RenderPriorityUpToDate = false;
	}
}

bool MapLayer::exists(MapFeature* F) const
{
	std::vector<MapFeature*>::iterator i = std::find(p->Features.begin(),p->Features.end(), F);
	return i != p->Features.end();
}

unsigned int MapLayer::size() const
{
	return p->Features.size();
}

void MapLayer::setDocument(MapDocument* aDocument)
{
    p->theDocument = aDocument;
}

MapDocument* MapLayer::getDocument()
{
    return p->theDocument;
}

MapFeature* MapLayer::get(unsigned int i)
{
	return p->Features[i];
}

MapFeature* MapLayer::get(const QString& id)
{
	std::map<QString, MapFeature*>::iterator i = p->IdMap.find(id);
	if (i != p->IdMap.end())
		return i->second;
	return 0;
}

const MapFeature* MapLayer::get(unsigned int i) const
{
	return p->Features[i];
}

LayerWidget* MapLayer::getWidget(void)
{
	return p->theWidget;
}

// DrawingMapLayer

DrawingMapLayer::DrawingMapLayer(const QString & aName)
	: MapLayer(aName)
{
	p->Visible = true;
}

DrawingMapLayer::~ DrawingMapLayer()
{
}

void DrawingMapLayer::setVisible(bool b)
{
	p->Visible = b;
}

LayerWidget* DrawingMapLayer::newWidget(void)
{
	delete p->theWidget;
	p->theWidget = new DrawingLayerWidget(this);
	return p->theWidget;
}

// ImageMapLayer

ImageMapLayer::ImageMapLayer(const QString & aName)
	: MapLayer(aName), layermanager(0)
{
	setMapAdapter(MerkaartorPreferences::instance()->getBgType());
	if (MerkaartorPreferences::instance()->getBgType() == Bg_None)
		p->Visible = false;
	else
		p->Visible = MerkaartorPreferences::instance()->getBgVisible();
}

ImageMapLayer::~ ImageMapLayer()
{
	SAFE_DELETE(p->layer_bg);
}

LayerWidget* ImageMapLayer::newWidget(void)
{
	p->theWidget = new ImageLayerWidget(this);
	return p->theWidget;
}

void ImageMapLayer::setVisible(bool b)
{
	p->Visible = b;
	if (p->bgType == Bg_None)
		p->Visible = false;
	else
	p->layer_bg->setVisible(b);
	MerkaartorPreferences::instance()->setBgVisible(p->Visible);
}

Layer* ImageMapLayer::imageLayer()
{
	return p->layer_bg;
}

void ImageMapLayer::setMapAdapter(ImageBackgroundType typ)
{
	int i;
	QStringList WmsServers;
	MapAdapter* mapadapter_bg;

	if (layermanager)
		if (layermanager->getLayers().size() > 0) {
			layermanager->removeLayer();
		}
		SAFE_DELETE(p->layer_bg);

		p->bgType = typ;
		MerkaartorPreferences::instance()->setBgType(typ);
		switch (p->bgType) {
			case Bg_None:
				setName("Background - None");
				p->Visible = false;
				break;

			case Bg_Wms:
				WmsServers = MerkaartorPreferences::instance()->getWmsServers();
				i = MerkaartorPreferences::instance()->getSelectedWmsServer();
				mapadapter_bg = new WMSMapAdapter(WmsServers[(i*6)+1], WmsServers[(i*6)+2], WmsServers[(i*6)+3],
						WmsServers[(i*6)+4], WmsServers[(i*6)+5], 256);
				p->layer_bg = new Layer("Custom Layer", mapadapter_bg, Layer::MapLayer);
				p->layer_bg->setVisible(p->Visible);

				setName("Background - WMS - " + WmsServers[(i*6)]);
				break;
			case Bg_OSM:
				mapadapter_bg = new OSMMapAdapter();
				p->layer_bg = new Layer("Custom Layer", mapadapter_bg, Layer::MapLayer);
				p->layer_bg->setVisible(p->Visible);

				setName("Background - OSM");
				break;
#ifdef yahoo_illegal
			case Bg_Yahoo_illegal:
				mapadapter_bg = new YahooMapAdapter("us.maps3.yimg.com", "/aerial.maps.yimg.com/png?v=1.7&t=a&s=256&x=%2&y=%3&z=%1");
				p->layer_bg = new Layer("Custom Layer", mapadapter_bg, Layer::MapLayer);
				p->layer_bg->setVisible(p->Visible);

				setName("Background - Yahoo");
				break;
#endif
		}
		if (layermanager)
			if (p->layer_bg) {
				layermanager->addLayer(p->layer_bg);
			}
}



