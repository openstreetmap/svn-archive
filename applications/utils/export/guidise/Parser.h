#ifndef PARSER_H
#define PARSER_H

// based on the libosm parser 
// http://svn.openstreetmap.org/applications/lib/libosm/

#include <expat.h>

#include <cstdio>
#include <cstdlib>
#include <iostream>
#include <string>
#include <map>
#include <vector>

using std::cout;
using std::cerr;
using std::endl;


struct Node
{
	double lat, lon;
	int count;

	Node(double lat,double lon)
	{
		this->lat=lat;
		this->lon=lon;
		count=0;
	}
};

/**
 * Xml parser for OSM
 */
class Parser
{
public:
	static bool parse(XML_Parser p,std::istream &stream);

	/**
	 * @return An empty string if no error occurred in the last parse call,
	 * or a description of the error otherwise
	 */
	static std::string getError();

	static void startElement(void *d, const XML_Char* name,
			const XML_Char** attrs);
	static void endElement(void *d, const XML_Char* name);
	static void characters(void*, const XML_Char* txt, int txtlen);

private:

	static void writeCurrentTags(std::map<std::string,std::string>& tags);
	static void freeNodes();

	static int curID;
	static bool inNode, inWay;
	static std::map <int,Node*> nodes;
	static std::map<std::string,std::string> nodetags;
	static std::vector<Node*> waynodes;
	static std::string error;
	static double lon,lat;
};

long long getNodeGUID(double lon, double lat,std::map<std::string,std::string>
						&nodetags);
long long getWayGUID(std::vector<Node*> waynodes);

#endif
