#ifndef FEATURECLASSIFICATION_H
#define FEATURECLASSIFICATION_H

#include "Object.h"

#include <iostream>
#include <map>
#include <string>
#include <vector>

namespace OSM
{

struct KeyVal
{
	std::string k, v;
	KeyVal()
	{
	}

	KeyVal(std::string k, std::string v)
	{
		this->k = k;
		this->v = v;
	}
};

class FeatureClassification
{
private:

	std::vector<KeyVal> areas, ways;

public:
	FeatureClassification()
	{
	}

	void addWayDef(const std::string& k, const std::string& v)
	{
		ways.push_back(KeyVal(k, v));
	}

	void addAreaDef(const std::string& k, const std::string& v)
	{
		areas.push_back(KeyVal(k, v));
	}

	std::string getFeatureClass(Object *object)
	{
		for (std::map<std::string, std::string>::iterator i =
				object->tags.begin(); i != object->tags.end(); i++)
		{
			for (std::vector<KeyVal>::iterator j = ways.begin(); j
					!= ways.end(); j++)
			{
				if (i->first == j->k && i->second == j->v)
				{
					return "way";
				}
			}
			for (std::vector<KeyVal>::iterator j = areas.begin(); j
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
};

}

#endif
