#ifndef FEATURESPARSER_H
#define FEATURESPARSER_H

#include <expat.h>
#include <cstdio>
#include <cstdlib>
#include <iostream>
#include <string>
#include "FeatureClassification.h"

namespace OSM
{

class FeaturesParser
{
private:
 	static FeatureClassification* featureClassification;

 	static bool inDoc, inWays, inAreas;

	static std::string error;

	static void startElement(void *d,const XML_Char* name,
		const XML_Char** attrs);
	static void endElement(void *d,const XML_Char* name);
	static void characters(void*, const XML_Char* txt,int txtlen);
public:
	static FeatureClassification* parse(std::istream&);
	static std::string getError() { return error; }
};

}

#endif
