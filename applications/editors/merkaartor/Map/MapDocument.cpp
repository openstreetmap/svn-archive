#include "Map/MapDocument.h"
#include "Command/Command.h"
#include "Map/MapFeature.h"

#include "QMapControl/mapadapter.h"
#include "QMapControl/yahoomapadapter.h"
#include "QMapControl/layer.h"
#include "QMapControl/layermanager.h"

#include <QtCore/QString>
#include <QMultiMap>

#include <algorithm>
#include <map>
#include <vector>

/* MAPLAYER */

class MapLayerPrivate
{
public:
	MapLayerPrivate()
		: RenderPriorityUpToDate(false)
	{
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

	MapLayer::MapLayerType LayerType;
	MapAdapter* mapadapter_yahoo;
	Layer* layer_yahoo;

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

MapLayer::MapLayer(const QString& aName, enum MapLayerType layertype)
: p(new MapLayerPrivate)
{
	p->Name = aName;
	p->LayerType = layertype;
	layermanager = NULL;

	switch (p->LayerType) {
	    case MapLayer::ImageLayer:
		p->mapadapter_yahoo = new YahooMapAdapter("us.maps3.yimg.com", "/aerial.maps.yimg.com/png?v=1.7&t=a&s=256&x=%2&y=%3&z=%1");
		p->layer_yahoo = new Layer("Custom Layer", p->mapadapter_yahoo, Layer::MapLayer);
		p->layer_yahoo->setVisible(false);

		p->Visible = false;
		break;
	default:
        	p->Visible = true;

	}
}

MapLayer::MapLayer(const MapLayer&)
: p(0)
{
}

MapLayer::~MapLayer()
{
	switch (p->LayerType) {
		case MapLayer::ImageLayer:
//			delete p->mapadapter_yahoo;
			delete p->layer_yahoo;
			break;
	}
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

void MapLayer::setVisible(bool b)
{
	p->Visible = b;
	switch (p->LayerType) {
	    case MapLayer::ImageLayer:
            p->layer_yahoo->setVisible(b);
            break;
        default:
            break;
	}
}

MapLayer::MapLayerType MapLayer::type()
{
    return p->LayerType;
}

Layer* MapLayer::imageLayer()
{
    return p->layer_yahoo;
}

bool MapLayer::isVisible() const
{
	return p->Visible;
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


/* MAPDOCUMENT */

class MapDocumentPrivate
{
public:
	~MapDocumentPrivate()
	{
		History.cleanup();
		for (unsigned int i=0; i<Layers.size(); ++i)
			delete Layers[i];
	}
	CommandHistory History;
	std::vector<MapLayer*> Layers;
        QMultiMap<QString, QString> tagList;
};

MapDocument::MapDocument()
: p(new MapDocumentPrivate)
{
	bgLayer = new MapLayer("Background imagery", MapLayer::ImageLayer);
	add(bgLayer);
	add(new MapLayer("Generic layer", MapLayer::DrawingLayer));
}

MapDocument::MapDocument(const MapDocument&)
: p(0)
{
}

MapDocument::~MapDocument()
{
	delete p;
}

void MapDocument::clear()
{
	delete p;
	p = new MapDocumentPrivate;
	bgLayer = new MapLayer("Background imagery", MapLayer::ImageLayer);
	add(bgLayer);
	add(new MapLayer("Generic layer", MapLayer::DrawingLayer));
}

CommandHistory& MapDocument::history()
{
	return p->History;
}

const CommandHistory& MapDocument::history() const
{
	return p->History;
}

void MapDocument::add(MapLayer* aLayer)
{
	p->Layers.push_back(aLayer);
    aLayer->setDocument(this);
}

void MapDocument::addToTagList(QString k, QString v)
{
	if (!p->tagList.contains(k, v)) {
    	p->tagList.insert(k, v);
	}
}

QStringList MapDocument::getTagList()
{
	return p->tagList.uniqueKeys();
}

QStringList MapDocument::getTagValueList(QString k)
{
	return p->tagList.values(k);
}

void MapDocument::remove(MapLayer* aLayer)
{
	std::vector<MapLayer*>::iterator i = std::find(p->Layers.begin(),p->Layers.end(), aLayer);
	if (i != p->Layers.end())
		p->Layers.erase(i);
}

bool MapDocument::exists(MapFeature* F) const
{
	for (unsigned int i=0; i<p->Layers.size(); ++i)
		if (p->Layers[i]->exists(F)) return true;
	return false;
}

unsigned int MapDocument::numLayers() const
{
	return p->Layers.size();
}

MapLayer* MapDocument::layer(unsigned int i)
{
	return p->Layers[i];
}

const MapLayer* MapDocument::layer(unsigned int i) const
{
	return p->Layers[i];
}

MapFeature* MapDocument::get(const QString& id)
{
	for (unsigned int i=0; i<p->Layers.size(); ++i)
	{
		MapFeature* F = p->Layers[i]->get(id);
		if (F) return F;
	}
	return 0;
}

/* VISIBLEFEATUREITERATOR */

VisibleFeatureIterator::VisibleFeatureIterator(MapDocument *aDoc)
: theDocument(aDoc), Layer(0), Idx(0)
{
	while (Layer < theDocument->numLayers())
	{
		MapLayer* L = theDocument->layer(Layer);
		if (L->isVisible() && L->size())
			break;
		++Layer;
	}
}

MapFeature* VisibleFeatureIterator::get()
{
	return theDocument->layer(Layer)->get(Idx);
}

bool VisibleFeatureIterator::isEnd() const
{
	return Layer >= theDocument->numLayers();
}

VisibleFeatureIterator& VisibleFeatureIterator::operator++()
{
	++Idx;
	if (Idx >= theDocument->layer(Layer)->size())
	{
		Idx = 0;
		++Layer;
		while (Layer < theDocument->numLayers())
		{
			MapLayer* L = theDocument->layer(Layer);
			if (L->isVisible() && L->size())
				break;
			++Layer;
		}
	}
	return *this;
}

MapLayer* VisibleFeatureIterator::layer()
{
	return theDocument->layer(Layer);
}

unsigned int VisibleFeatureIterator::index()
{
	return Idx;
}


/* FEATUREITERATOR */

FeatureIterator::FeatureIterator(MapDocument *aDoc)
: theDocument(aDoc), Layer(0), Idx(0)
{
	while (Layer < theDocument->numLayers())
	{
		if (theDocument->layer(Layer)->size())
			break;
		++Layer;
	}
}

MapFeature* FeatureIterator::get()
{
	return theDocument->layer(Layer)->get(Idx);
}

bool FeatureIterator::isEnd() const
{
	return Layer >= theDocument->numLayers();
}

FeatureIterator& FeatureIterator::operator++()
{
	++Idx;
	if (Idx >= theDocument->layer(Layer)->size())
	{
		Idx = 0;
		++Layer;
		while (Layer < theDocument->numLayers())
		{
			if (theDocument->layer(Layer)->size())
				break;
			++Layer;
		}
	}
	return *this;
}

MapLayer* FeatureIterator::layer()
{
	return theDocument->layer(Layer);
}

unsigned int FeatureIterator::index()
{
	return Idx;
}





/* RELATED */

static CoordBox boundingBox(const MapLayer* theLayer)
{
	CoordBox Box(theLayer->get(0)->boundingBox());
	for (unsigned int i=1; i<theLayer->size(); ++i)
		Box.merge(theLayer->get(i)->boundingBox());
	return Box;
}

std::pair<bool,CoordBox> boundingBox(const MapDocument* theDocument)
{
	unsigned int First;
	for (First = 0; First < theDocument->numLayers(); ++First)
		if (theDocument->layer(First)->size())
			break;
	if (First == theDocument->numLayers())
		return std::make_pair(false,CoordBox(Coord(0,0),Coord(0,0)));
	CoordBox BBox(boundingBox(theDocument->layer(First)));
	for (unsigned int i=First+1; i<theDocument->numLayers(); ++i)
		if (theDocument->layer(i)->size())
			BBox.merge(boundingBox(theDocument->layer(i)));
	return std::make_pair(true,BBox);
}

bool hasUnsavedChanges(const MapDocument& aDoc)
{
	return aDoc.history().index();
}
