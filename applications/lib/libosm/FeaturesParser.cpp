#include "FeaturesParser.h"
#include "FeatureClassification.h"

#include <iostream>
#include <cstring>
using namespace std;

namespace OSM
{

 FeatureClassification* FeaturesParser::featureClassification=NULL;

 bool FeaturesParser::inDoc=false, FeaturesParser::inWays=false,
 	FeaturesParser::inAreas=false;

 std::string FeaturesParser::error="";

void FeaturesParser::startElement(void *d, const XML_Char* element,
		const XML_Char** attrs)
{
	if(!strcmp(element,"features"))
	{
		inDoc = true;
	}
	else if(inDoc)
	{
		if(!strcmp(element,"ways"))
		{
			inWays=true;
		}
		else if (!strcmp(element,"areas"))
		{
			inAreas=true;
		}
		else if(!strcmp(element,"condition") && (inWays||inAreas))
		{
			std::string key="", value="";
			int count=0;
			while(attrs[count])
			{
				if(!strcmp(attrs[count],"k"))
					key = attrs[count+1];
				else if (!strcmp(attrs[count],"v"))
					value = attrs[count+1];
				count++;
			}

			if(key!="" && value!="")
			{
				if(inWays)
					featureClassification->addWayDef(key,value);
				else if(inAreas)
					featureClassification->addAreaDef(key,value);
			}
		}
	}
}

void FeaturesParser::endElement(void *d, const XML_Char* element)
{
	if(inDoc && !strcmp(element,"features"))
	{
		inDoc = false;
	}
	else if(!strcmp(element,"ways"))
	{
		inWays=false;
	}
	else if (!strcmp(element,"areas"))
	{
		inAreas=false;
	}
}

FeatureClassification* FeaturesParser::parse(std::istream &in)
{
	int done, count=0, n;
	char buf[4096];

	XML_Parser p = XML_ParserCreate(NULL);
	if(!p)
	{
		error = "Error creating parser";
		return NULL;
	}

	XML_SetElementHandler(p,FeaturesParser::startElement,
						FeaturesParser::endElement);
	XML_SetCharacterDataHandler(p,FeaturesParser::characters);
	featureClassification = new FeatureClassification;

	// straight from example
	do
	{
		//in.read(buf,4096);
		in.getline(buf,sizeof(buf));
		//n = in.gcount();
		//done = (n!=4096);
		done = in.eof();

		if(XML_Parse(p,buf,strlen(buf),done) == XML_STATUS_ERROR)
		{
			error = "xml parsing error";
			delete featureClassification;
			return NULL;
		}
		count += n;
	} while (!done);
	XML_ParserFree(p);
	return featureClassification;
}
 void FeaturesParser::characters(void*, const XML_Char* txt,int txtlen)
 {
 }
}

