#include <expat.h>
#include <cstdio>
#include <cstdlib>
#include <iostream>
#include "Object.h"
#include "Components.h"

namespace OSM
{

class Parser
{
private:
	static Object *curObject;
	static int curID;
	static bool inNode, inSegment, inWay;
	static Components* components;

	static void startElement(void *d,const XML_Char* name,
		const XML_Char** attrs);
	static void endElement(void *d,const XML_Char* name);
	static void characters(void*, const XML_Char* txt,int txtlen);
public:
	static Components* parse(std::istream&);
};

}
