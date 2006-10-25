#include "Map/MapFeature.h"

#include <QtCore/QUuid>

static QString randomId()
{
	return QUuid::createUuid().toString(); 
}

MapFeature::MapFeature()
: LastActor(MapFeature::User)
{
	setTag("created_by","Merkaartor");
}

MapFeature::~MapFeature(void)
{
}

void MapFeature::setLastUpdated(MapFeature::ActorType A)
{
	LastActor = A;
}

MapFeature::ActorType MapFeature::lastUpdated() const
{
	return LastActor;
}

void MapFeature::setId(const QString& id)
{
	Id = id;
}

const QString& MapFeature::id() const
{
	if (Id == "")
		Id = randomId();
	return Id;
}

void MapFeature::setTag(unsigned int idx, const QString& k, const QString& v)
{
	Tags[idx] = std::make_pair(k,v);
}

void MapFeature::setTag(const QString& k, const QString& v)
{
	for (unsigned int i=0; i<Tags.size(); ++i)
		if (Tags[i].first == k)
		{
			Tags[i].second = v;
			return;
		}
	Tags.push_back(std::make_pair(k,v));
}

void MapFeature::clearTags()
{
	Tags.clear();
}

void MapFeature::clearTag(const QString& k)
{
	for (unsigned int i=0; i<Tags.size(); ++i)
		if (Tags[i].first == k)
		{
			Tags.erase(Tags.begin()+i);
			return;
		}
}

unsigned int MapFeature::tagSize() const
{
	return Tags.size();
}

QString MapFeature::tagValue(unsigned int i) const
{
	return Tags[i].second;
}

QString MapFeature::tagKey(unsigned int i) const
{
	return Tags[i].first;
}

unsigned int MapFeature::findKey(const QString &k) const
{
	for (unsigned int i=0; i<Tags.size(); ++i)
		if (Tags[i].first == k)
			return i;
	return Tags.size();
}

void MapFeature::removeTag(unsigned int idx)
{
	Tags.erase(Tags.begin()+idx);
}

QString MapFeature::tagValue(const QString& k, const QString& Default) const
{
	for (unsigned int i=0; i<Tags.size(); ++i)
		if (Tags[i].first == k)
			return Tags[i].second;
	return Default;
}


