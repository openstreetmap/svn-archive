#include "Parser.h"
#include "Node.h"
#include "Way.h"
#include "Components.h"
#include <string>
#include <cstring>

using std::cerr;
using std::endl;

namespace OSM
{

Object* Parser::curObject = NULL;
int Parser::curID = 0;
bool Parser::inNode = false, Parser::inWay = false;
Components* Parser::components = NULL;
std::string Parser::error = "";

std::string Parser::getError()
{
	return error;
}

void Parser::startElement(void *d, const XML_Char* element,
		const XML_Char** attrs)
{
	double lat, lon;
	std::string tags;

	if (!strcmp(element, "node"))
	{
		curID = 0;
		inNode = true;
		int count = 0;
		while (attrs[count])
		{
			if (!strcmp(attrs[count], "lat"))
				lat = atof(attrs[count + 1]);
			if (!strcmp(attrs[count], "lon"))
				lon = atof(attrs[count + 1]);
			if (!strcmp(attrs[count], "id"))
				curID = atoi(attrs[count + 1]);
			count += 2;
		}

		curObject = new Node(curID, lat, lon);
		components->addNode((Node*) curObject);

	}
	else if (!strcmp(element, "way"))
	{
		curID = 0;
		inWay = true;
		for (int count = 0; attrs[count]; count += 2)
		{
			if (!strcmp(attrs[count], "id"))
				curID = atoi(attrs[count + 1]);
		}
		curObject = new Way(curID);
		components->addWay((Way*) curObject);
	}
	else if (!strcmp(element, "nd") && (inWay))
	{
		int nodeID;

		for (int count = 0; attrs[count]; count += 2)
		{
			if (!strcmp(attrs[count], "ref"))
			{
				nodeID = atoi(attrs[count + 1]);
				((Way*) curObject)->addNode(nodeID);
			}
		}

	}
	else if (!strcmp(element, "tag"))
	{
		std::string key = "", value = "";

		for (int count = 0; attrs[count]; count += 2)
		{
			if (!strcmp(attrs[count], "k"))
				key = attrs[count + 1];
			if (!strcmp(attrs[count], "v"))
				value = attrs[count + 1];
		}

		curObject->addTag(key, value);
	}
}

void Parser::endElement(void *d, const XML_Char* element)
{
	if (!strcmp(element, "node"))
	{
		inNode = false;
	}
	else if (!strcmp(element, "way"))
	{
		inWay = false;
	}
}

void Parser::characters(void*, const XML_Char* txt, int txtlen)
{
}

Components* Parser::parse(std::istream &in)
{
	int done, count = 0, n;
	char buf[4096];

	XML_Parser p = XML_ParserCreate(NULL);
	if (!p)
	{
		error = "Error creating parser";
		return NULL;
	}

	XML_SetElementHandler(p, Parser::startElement, Parser::endElement);
	XML_SetCharacterDataHandler(p, Parser::characters);
	components = new Components;

	// straight from example
	do
	{
		in.read(buf, 4096);
		n = in.gcount();
		done = (n != 4096);
		if (XML_Parse(p, buf, n, done) == XML_STATUS_ERROR)
		{
			error = "xml parsing error";
			delete components;
			return NULL;
		}
		count += n;
	} while (!done);
	XML_ParserFree(p);
	return components;
}

}
