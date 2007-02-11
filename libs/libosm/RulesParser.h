#ifndef RULESPARSER_H
#define RULESPARSER_H

#include <expat.h>
#include <cstdio>
#include <cstdlib>
#include <iostream>
#include <string>
#include <vector>
#include "ElemStyles.h"
#include "ElemStyle.h"

namespace OSM
{

class RulesParser
{
private:
	static bool inDoc, inRule;
	static ElemStyle* curStyle; 
	static ElemStyles* elemStyles; 
	static std::string error;
	static std::vector<Rule> curRules;
	static bool curAnnotate;
	static int curLineWidth, curMinZoom;
	static std::string curIconSrc, curColour;

	static void startElement(void *d,const XML_Char* name,
		const XML_Char** attrs);
	static void endElement(void *d,const XML_Char* name);
	static void characters(void*, const XML_Char* txt,int txtlen);
public:
	static ElemStyles* parse(std::istream&);
	static std::string getError() { return error; }
};

}

#endif
