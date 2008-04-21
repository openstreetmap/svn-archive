#include "Map/MapFeature.h"
#include "Map/Relation.h"
#include "Map/Road.h"
#include "Map/TrackPoint.h"
#include "Command/Command.h"
#include "Command/DocumentCommands.h"
#include "Command/FeatureCommands.h"
#include "Command/RelationCommands.h"
#include "Command/RoadCommands.h"
#include "Command/TrackPointCommands.h"
#include "Map/MapDocument.h"
#include "Map/MapLayer.h"
#include "PaintStyle/EditPaintStyle.h"

#include <QtCore/QUuid>

#include <algorithm>

static QString randomId()
{
	return QUuid::createUuid().toString();
}

void copyTags(MapFeature* Dest, MapFeature* Src)
{
	for (unsigned int i=0; i<Src->tagSize(); ++i)
		Dest->setTag(Src->tagKey(i),Src->tagValue(i));
}

class MapFeaturePrivate
{
	public:
		MapFeaturePrivate()
			: LastActor(MapFeature::User), theLayer(0),
			  	PixelPerMForPainter(-1), CurrentPainter(0), HasPainter(false),
				theFeature(0), LastPartNotification(0), Time(QDateTime::currentDateTime()) { }
		MapFeaturePrivate(const MapFeaturePrivate& other)
			: Tags(other.Tags), LastActor(MapFeature::User), theLayer(0),
			  	PixelPerMForPainter(-1), CurrentPainter(0), HasPainter(false),
				theFeature(0), LastPartNotification(0), Time(QDateTime::currentDateTime()) { }

		void updatePainters(double PixelPerM);

		mutable QString Id;
		std::vector<std::pair<QString, QString> > Tags;
		MapFeature::ActorType LastActor;
		MapLayer* theLayer;
		double PixelPerMForPainter;
		FeaturePainter* CurrentPainter;
		bool HasPainter;
		MapFeature* theFeature;
		std::vector<MapFeature*> Parents;
		unsigned int LastPartNotification;
		QDateTime Time;
		QString User;
};

MapFeature::MapFeature()
: p(0)
{
	 p = new MapFeaturePrivate;
	 p->theFeature = this;
}

MapFeature::MapFeature(const MapFeature& other)
{
	p = new MapFeaturePrivate(*other.p);
	p->theFeature = this;
}

MapFeature::~MapFeature(void)
{
	if (p->theLayer)
		p->theLayer->notifyIdUpdate(p->Id,0);
	delete p;
}

void MapFeature::setLayer(MapLayer* aLayer)
{
	p->theLayer = aLayer;
}

MapLayer* MapFeature::layer()
{
	return p->theLayer;
}

void MapFeature::setLastUpdated(MapFeature::ActorType A)
{
	p->LastActor = A;
}

MapFeature::ActorType MapFeature::lastUpdated() const
{
	return p->LastActor;
}

void MapFeature::setId(const QString& id)
{
	if (p->theLayer)
	{
		p->theLayer->notifyIdUpdate(p->Id,0);
		p->theLayer->notifyIdUpdate(id,this);
	}
	p->Id = id;
}

const QString& MapFeature::id() const
{
	if (p->Id == "")
	{
		p->Id = randomId();
		if (p->theLayer)
			p->theLayer->notifyIdUpdate(p->Id,const_cast<MapFeature*>(this));
	}
	return p->Id;
}

QString MapFeature::xmlId() const
{
	return stripToOSMId(id());
}

const QDateTime& MapFeature::time() const
{
	return p->Time;
}

void MapFeature::setTime(const QDateTime& time)
{
	p->Time = time;
	notifyChanges();
}

const QString& MapFeature::user() const
{
	return p->User;
}

void MapFeature::setUser(const QString& user)
{
	p->User = user;
	notifyChanges();
}

void MapFeature::setTag(unsigned int idx, const QString& k, const QString& v)
{
	p->PixelPerMForPainter = -1;
	for (unsigned int i=0; i<p->Tags.size(); ++i)
		if (p->Tags[i].first == k)
		{
			p->Tags[i].second = v;
			notifyChanges();
			return;
		}
	p->Tags.insert(p->Tags.begin() + idx, std::make_pair(k,v));
	if (p->theLayer)
  		p->theLayer->getDocument()->addToTagList(k, v);
	notifyChanges();
}

void MapFeature::setTag(const QString& k, const QString& v)
{
	p->PixelPerMForPainter = -1;
	for (unsigned int i=0; i<p->Tags.size(); ++i)
		if (p->Tags[i].first == k)
		{
			p->Tags[i].second = v;
			notifyChanges();
			return;
		}
	p->Tags.push_back(std::make_pair(k,v));
	if (p->theLayer)
  		p->theLayer->getDocument()->addToTagList(k, v);
	notifyChanges();
}

void MapFeature::clearTags()
{
	p->PixelPerMForPainter = -1;
	p->Tags.clear();
	notifyChanges();
}

void MapFeature::invalidatePainter()
{
	p->PixelPerMForPainter = -1;
}

void MapFeature::clearTag(const QString& k)
{
	for (unsigned int i=0; i<p->Tags.size(); ++i)
		if (p->Tags[i].first == k)
		{
			p->PixelPerMForPainter = -1;
			p->Tags.erase(p->Tags.begin()+i);
			notifyChanges();
			return;
		}
}

unsigned int MapFeature::tagSize() const
{
	return p->Tags.size();
}

QString MapFeature::tagValue(unsigned int i) const
{
	return p->Tags[i].second;
}

QString MapFeature::tagKey(unsigned int i) const
{
	return p->Tags[i].first;
}

unsigned int MapFeature::findKey(const QString &k) const
{
	for (unsigned int i=0; i<p->Tags.size(); ++i)
		if (p->Tags[i].first == k)
			return i;
	return p->Tags.size();
}

void MapFeature::removeTag(unsigned int idx)
{
	p->PixelPerMForPainter = -1;
	p->Tags.erase(p->Tags.begin()+idx);
	notifyChanges();
}

QString MapFeature::tagValue(const QString& k, const QString& Default) const
{
	for (unsigned int i=0; i<p->Tags.size(); ++i)
		if (p->Tags[i].first == k)
			return p->Tags[i].second;
	return Default;
}

void MapFeaturePrivate::updatePainters(double PixelPerM)
{
	CurrentPainter = 0;
	HasPainter = false;
	PixelPerMForPainter = PixelPerM;
	for (unsigned int i=0; i<EditPaintStyle::Painters.size(); ++i)
	{
		FeaturePainter* Current = &EditPaintStyle::Painters[i];
		if (Current->matchesTag(theFeature))
		{
			HasPainter = true;
			if (Current->matchesZoom(PixelPerM))
			{
				CurrentPainter = Current;
				return;
			}
		}
	}
}

FeaturePainter* MapFeature::getEditPainter(double PixelPerM) const
{
	if (p->PixelPerMForPainter != PixelPerM)
		p->updatePainters(PixelPerM);
	return p->CurrentPainter;
}

bool MapFeature::hasEditPainter() const
{
	return p->HasPainter;
}

void MapFeature::setParent(MapFeature* F)
{
	if (std::find(p->Parents.begin(),p->Parents.end(),F) == p->Parents.end())
		p->Parents.push_back(F);
}

void MapFeature::unsetParent(MapFeature* F)
{
	for (unsigned int i=0; i<p->Parents.size(); ++i)
		if (p->Parents[i] == F)
		{
			p->Parents.erase(p->Parents.begin()+i);
			return;
		}
}

unsigned int MapFeature::sizeParents() const
{
	return p->Parents.size();
}

MapFeature* MapFeature::getParent(unsigned int i)
{
	return p->Parents[i];
}

const MapFeature* MapFeature::getParent(unsigned int i) const
{
	return p->Parents[i];
}


void MapFeature::notifyChanges()
{
	static unsigned int Id = 0;
	notifyParents(++Id);
	if (p->theLayer)
		p->theLayer->invalidateRenderPriority();
}

void MapFeature::notifyParents(unsigned int Id)
{
	if (Id != p->LastPartNotification)
	{
		p->LastPartNotification = Id;
		for (unsigned int i=0; i<p->Parents.size(); ++i)
			p->Parents[i]->partChanged(this, Id);
	}
}

QString MapFeature::tagsToXML(unsigned int lvl)
{
	QString S;
	for (unsigned int i=0; i<tagSize(); ++i)
	{
		S += QString(lvl*2, ' ') + QString("<tag k=\"%1\" v=\"%2\"/>\n").arg(tagKey(i)).arg(tagValue(i));
	}
	return S;
}

bool MapFeature::tagsToXML(QDomElement xParent)
{
	bool OK = true;

	for (unsigned int i=0; i<tagSize(); ++i)
	{
		QDomElement e = xParent.ownerDocument().createElement("tag");
		xParent.appendChild(e);

		e.setAttribute("k", tagKey(i));
		e.setAttribute("v", tagValue(i));
	}

	return OK;
}

QString MapFeature::stripToOSMId(const QString& id)
{
	int f = id.lastIndexOf("_");
	if (f>0)
		return id.right(id.length()-(f+1));
	return id;
}

bool hasOSMId(const MapFeature* aFeature)
{
	QString id = aFeature->id();
	if (id.left(5) == "node_")
		return true;
	if (id.left(4) == "way_")
		return true;
	if (id.left(4) == "rel_")
		return true;
	return false;
}

void MapFeature::tagsFromXML(MapDocument* d, MapFeature * f, QDomElement e)
{
	QDomElement c = e.firstChildElement();
	while(!c.isNull()) {
		if (c.tagName() == "tag") {
			f->setTag(c.attribute("k"),c.attribute("v"));
	  		d->addToTagList(c.attribute("k"), c.attribute("v"));
		}
		c = c.nextSiblingElement();
	}
}

//Static
TrackPoint* MapFeature::getTrackPointOrCreatePlaceHolder(MapDocument *theDocument, MapLayer *theLayer, CommandList *theList, const QString& Id)
{
	TrackPoint* Part = dynamic_cast<TrackPoint*>(theDocument->getFeature("node_"+Id));
	if (!Part)
	{
		Part = new TrackPoint(Coord(0,0));
		Part->setId("node_"+Id);
		Part->setLastUpdated(MapFeature::NotYetDownloaded);
		if (theList)
			theList->add(new AddFeatureCommand(theLayer, Part, false));
	}
	return Part;
}

Road* MapFeature::getWayOrCreatePlaceHolder(MapDocument *theDocument, MapLayer *theLayer, CommandList *theList, const QString& Id)
{
	Road* Part = dynamic_cast<Road*>(theDocument->getFeature("way_"+Id));
	if (!Part)
	{
		Part = new Road;
		Part->setId("way_"+Id);
		Part->setLastUpdated(MapFeature::NotYetDownloaded);
		if (theList)
			theList->add(new AddFeatureCommand(theLayer, Part, false));
	}
	return Part;
}

Relation* MapFeature::getRelationOrCreatePlaceHolder(MapDocument *theDocument, MapLayer *theLayer, CommandList *theList, const QString& Id)
{
	Relation* Part = dynamic_cast<Relation*>(theDocument->getFeature("rel_"+Id));
	if (!Part)
	{
		Part = new Relation;
		Part->setId("rel_"+Id);
		Part->setLastUpdated(MapFeature::NotYetDownloaded);
		if (theList)
			theList->add(new AddFeatureCommand(theLayer, Part, false));
	}
	return Part;
}

QString MapFeature::toMainHtml(QString type)
{
	QString S;

	S += "<html><head/><body>";
	S += "<small><i>" + type + "</i></small><br/>";
	S += "<big><b>" + description() + "</b></big>";
	S += "<br/>";
	S += "<small><i>last: </i><b>" + time().toString(Qt::SystemLocaleDate) + "</b>";
	if (!user().isEmpty())
		S += " by <b>" + user() + "</b>";
	S += "</small>";
//	S += "<i>user: </i>" + user();
	S += "<hr/>";
	S += "%1";
	int f = id().lastIndexOf("_");
	if (f>0) {
		S += "<hr/>";
		S += "<a href='/api/0.5/" + type.toLower() + "/" + xmlId() + "/history'>History</a>";
		if (type == "Node") {
			S += "<br/>";
			S += "<a href='/api/0.5/" + type.toLower() + "/" + xmlId() + "/ways'>Referenced by ways</a>";
		}
		S += "<br/>";
		S += "<a href='/api/0.5/" + type.toLower() + "/" + xmlId() + "/relation'>Referenced by relation</a>";
	}
	S += "</body></html>";

	return S;
}
