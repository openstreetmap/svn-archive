#ifndef FEATURESPARSER_H
#define FEATURESPARSER_H

#include "FeatureClassification.h"

#include <expat.h>

#include <cstdio>
#include <cstdlib>
#include <iostream>
#include <string>

namespace OSM
{

class FeaturesParser
{
public:
	static FeatureClassification* parse(std::istream&);
	static std::string getError();

private:
	static FeatureClassification* featureClassification;

	static bool inDoc, inWays, inAreas;

	static std::string error;

	static void startElement(void *d, const XML_Char* name,
			const XML_Char** attrs);
	static void endElement(void *d, const XML_Char* name);
	static void characters(void*, const XML_Char* txt, int txtlen);
};

}

#endif
