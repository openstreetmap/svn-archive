#include "RulesParser.h"
#include "LineElemStyle.h"
#include "AreaElemStyle.h"
#include "IconElemStyle.h"

#include <iostream>
using namespace std;

namespace OSM
{

 ElemStyle* RulesParser::curStyle=NULL;
 ElemStyles* RulesParser::elemStyles=NULL;
 int RulesParser::curLineWidth=0, RulesParser::curMinZoom=0;
 bool RulesParser::inDoc=false, RulesParser::inRule=false, 
 	RulesParser::curAnnotate=false;
 std::string RulesParser::error="", RulesParser::curIconSrc="",
 	RulesParser::curColour="";
 std::vector<Rule> RulesParser::curRules;

void RulesParser::startElement(void *d, const XML_Char* element,
		const XML_Char** attrs)
{
	if(!strcmp(element,"rules"))
	{
		inDoc = true;
	}
	else if(inDoc)
	{
		if(!strcmp(element,"rule"))
		{
			inRule=true;
			curRules.clear();
		}
		else if(!strcmp(element,"condition") && inRule)
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
				curRules.push_back(Rule(key,value));
		}
		else if(!strcmp(element,"icon") && inRule)
		{
			int count=0; 
			while(attrs[count])
			{
				if(!strcmp(attrs[count],"annotate") &&
					!strcmp(attrs[count+1],"true"))
				{
					curAnnotate=true;
				}
				else if(!strcmp(attrs[count],"src"))
					curIconSrc = attrs[count+1];
				count++;
			}
		}
		else if(!strcmp(element,"line") && inRule)
		{
			int count=0; 
			while(attrs[count])
			{
				if(!strcmp(attrs[count],"width"))
					curLineWidth=atoi(attrs[count+1]);
				else if(!strcmp(attrs[count],"colour"))
					curColour = attrs[count+1];
				count++;
			}
		}
		else if(!strcmp(element,"area") && inRule)
		{
			int count=0; 
			while(attrs[count])
			{
				if(!strcmp(attrs[count],"colour"))
					curColour = attrs[count+1];
				count++;
			}
		}
		else if(!strcmp(element,"zoom") && inRule)
		{
			curMinZoom = 0;
			int count=0; 
			while(attrs[count])
			{
				if(!strcmp(attrs[count],"min"))
					curMinZoom = atoi(attrs[count+1]);
				count++;
			}
		}
	}
}

void RulesParser::endElement(void *d, const XML_Char* element)
{
	if(inRule && !strcmp(element,"rule"))
	{
		inRule = false;
		elemStyles->add (curRules, curStyle);
	}
	else if ( !strcmp(element,"line"))
	{
		curStyle = new LineElemStyle(curLineWidth, curColour, curMinZoom);
	}
	else if ( !strcmp(element,"icon"))
	{
		curStyle = new IconElemStyle(curIconSrc,curAnnotate,curMinZoom);
	}
	else if ( !strcmp(element,"area"))
	{
		curStyle = new AreaElemStyle (curColour,curMinZoom);
	}
}

ElemStyles* RulesParser::parse(std::istream &in)
{
	int done, count=0, n;
	char buf[4096];

	XML_Parser p = XML_ParserCreate(NULL);
	if(!p)
	{
		error = "Error creating parser";
		return NULL; 
	}

	XML_SetElementHandler(p,RulesParser::startElement,
						RulesParser::endElement);
	XML_SetCharacterDataHandler(p,RulesParser::characters);
	elemStyles = new ElemStyles; 

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
			delete elemStyles; 
			return NULL;
		}
		count += n;
	} while (!done);
	XML_ParserFree(p);
	return elemStyles;
}
 void RulesParser::characters(void*, const XML_Char* txt,int txtlen)
 {
 }
}

