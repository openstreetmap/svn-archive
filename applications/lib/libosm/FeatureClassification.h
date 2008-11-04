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
	KeyVal(std::string k = std::string(), std::string v = std::string())
	{
		this->k = k;
		this->v = v;
	}
};

class FeatureClassification
{
public:
	FeatureClassification();

	void addWayDef(const std::string& k, const std::string& v);

	void addAreaDef(const std::string& k, const std::string& v);

	std::string getFeatureClass(Object *object) const;

private:
	std::vector<KeyVal> areas, ways;
};

}

#endif
