#include "FeatureClassification.h"

using namespace OSM;

FeatureClassification::FeatureClassification()
{
}

void FeatureClassification::addWayDef(const std::string& k,
		const std::string& v)
{
	ways.push_back(KeyVal(k, v));
}

void FeatureClassification::addAreaDef(const std::string& k,
		const std::string& v)
{
	areas.push_back(KeyVal(k, v));
}

std::string FeatureClassification::getFeatureClass(Object *object) const
{
	for (std::map<std::string, std::string>::iterator i = object->tags.begin(); i
			!= object->tags.end(); i++)
	{
		for (std::vector<KeyVal>::const_iterator j = ways.begin(); j
				!= ways.end(); j++)
		{
			if (i->first == j->k && i->second == j->v)
			{
				return "way";
			}
		}
		for (std::vector<KeyVal>::const_iterator j = areas.begin(); j
				!= areas.end(); j++)
		{
			if (i->first == j->k && i->second == j->v)
			{
				return "area";
			}
		}
	}
	return "unknown";
}
