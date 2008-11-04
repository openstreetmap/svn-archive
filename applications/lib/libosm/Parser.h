#include "Object.h"
#include "Components.h"

#include <expat.h>

#include <cstdio>
#include <cstdlib>
#include <iostream>
#include <string>

namespace OSM
{

class Parser
{
public:
	static Components* parse(std::istream&);
	static std::string getError();

private:
	static void startElement(void *d, const XML_Char* name,
			const XML_Char** attrs);
	static void endElement(void *d, const XML_Char* name);
	static void characters(void*, const XML_Char* txt, int txtlen);

	static Object *curObject;
	static int curID;
	static bool inNode, inWay;
	static Components* components;
	static std::string error;
};

}
