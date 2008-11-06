#include "Object.h"
#include "Components.h"

#include <expat.h>

#include <cstdio>
#include <cstdlib>
#include <iostream>
#include <string>

namespace OSM
{

/**
 * Xml parser for OSM
 */
class Parser
{
public:
	/**
	 * Parses xml provided by the given stream and returns the OSM components contained
	 * @param stream Input stream containing OSM xml (e.g. read from an OSM server or a local file)
	 * @return The OSM components contained in the stream
	 */
	static Components* parse(std::istream &stream);

	/**
	 * @return An empty string if no error occurred in the last parse call,
	 * or a description of the error otherwise
	 */
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
