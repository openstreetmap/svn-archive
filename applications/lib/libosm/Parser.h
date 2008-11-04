#include <expat.h>
#include <cstdio>
#include <cstdlib>
#include <iostream>
#include <string>
#include "Object.h"
#include "Components.h"

namespace OSM
{

class Parser
{
private:
	static Object *curObject;
	static int curID;
	static bool inNode, inWay;
	static Components* components;
	static std::string error;

	static void startElement(void *d,const XML_Char* name,
		const XML_Char** attrs);
	static void endElement(void *d,const XML_Char* name);
	static void characters(void*, const XML_Char* txt,int txtlen);
public:
	static Components* parse(std::istream&);
	static std::string getError() { return error; }
};

}
