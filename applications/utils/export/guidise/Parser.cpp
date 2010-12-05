#include "Parser.h"
#include <string>
#include <cstring>
#include <sstream>
#include <cmath>
#include "ccoord/OSRef.h"
#include "ccoord/LatLng.h"



int Parser::curID = 0;
bool Parser::inNode = false, Parser::inWay = false;
std::string Parser::error = "";
std::map<int,Node*>  Parser::nodes;
std::map<std::string,std::string>  Parser::nodetags;
std::vector<Node*> Parser::waynodes;
double Parser::lon,Parser::lat;

std::string Parser::getError()
{
    return error;
}

void Parser::startElement(void *d, const XML_Char* element,
        const XML_Char** attrs)
{
    int count=0;


    if(!strcmp(element,"osm"))
    {
        cout<<"<osm>"<<endl;
    }
    if (!strcmp(element, "node"))
    {
        curID = 0;
        inNode = true;
		nodetags.clear();
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
        nodes[curID] = new Node(lat,lon); 
        cout.precision(8);
        cout << "<node id=\""<<curID<<"\" lat=\""<<lat<<"\" lon=\""
                <<lon<<"\">" << endl;
    }
    else if (!strcmp(element, "way"))
    {
        inWay = true;

		
        while (attrs[count])
        {
            if (!strcmp(attrs[count], "id"))
                curID = atoi(attrs[count + 1]);
            count += 2;
        }
		waynodes.clear();
		cout <<"<way id='" << curID<<"'>" << endl;
    }
    else if (!strcmp(element, "nd") && (inWay))
    {
        int ndID;

        for (int count = 0; attrs[count]; count += 2)
        {
            if (!strcmp(attrs[count], "ref"))
            {
                ndID = atoi(attrs[count + 1]);
            	cout<<"<nd ref=\""<<ndID<<"\" />" << endl;
				if(nodes[ndID])
                	waynodes.push_back(nodes[ndID]);
            }
        }
    }
    else if (!strcmp(element, "tag"))
    {

        // write out tags (for node and way) in second run
        std::string key = "", value = "";

        for (int count = 0; attrs[count]; count += 2)
        {
            if (!strcmp(attrs[count], "k"))
                key = attrs[count + 1];
            if (!strcmp(attrs[count], "v"))
                value = attrs[count + 1];

        }

        int idx=value.find("&");
        while(idx>=0)
        {
            value=value.replace(idx,1,"&amp;");
            idx=value.find("&",idx+1);
        }
        idx=value.find("\"");
        while(idx>=0)
        {
            value=value.replace(idx,1,"&quot;");
            idx=value.find("\"",idx);
        }
        idx=value.find("'");
        while(idx>=0)
        {
            value=value.replace(idx,1,"&apos;");
            idx=value.find("'");
        }

        cout<<"<tag k=\""<<key<<"\" v=\""<<value<<"\" />"<<endl;
		if(inNode)
			nodetags[key] = value;
    }
}

void Parser::endElement(void *d, const XML_Char* element)
{
    if (!strcmp(element, "node"))
    {
        inNode = false;
		long long guid=getNodeGUID(lon,lat,nodetags);
		if(guid>0LL)
			cout << "<tag k='guid' v='"<<guid<<"' />" << endl;
        cout << "</node>\n";
    }
    else if (!strcmp(element, "way"))
    {
        inWay = false;
		long long guid = getWayGUID(waynodes);
		cout << "<tag k='guid' v='"<<guid<<"' />" << endl;
        cout << "</way>" << endl;
    }
    else if (!strcmp(element,"osm"))
    {
        cout<<"</osm>"<<endl;
        freeNodes();
    }
}

void Parser::characters(void*, const XML_Char* txt, int txtlen)
{
}

void Parser::writeCurrentTags(std::map<std::string,std::string>& tags)
{
    std::map<std::string,std::string>::iterator i=tags.begin();
    while(i != tags.end())
    {
        cout<<"<tag k=\"" << i->first << "\" v=\""<<i->second<<"\" />"<<endl;
        i++;
    }
}

void Parser::freeNodes()
{
    for(std::map<int,Node*>::iterator i=nodes.begin(); i!=nodes.end(); i++)
        delete i->second;
}


bool Parser::parse(XML_Parser p,std::istream &in)
{
    int done, count = 0, n;
    char buf[4096];


    // straight from example
    do
    {
        in.read(buf, 4096);
        n = in.gcount();
        done = (n != 4096);
        if (XML_Parse(p, buf, n, done) == XML_STATUS_ERROR)
        {
            XML_Error errorCode = XML_GetErrorCode(p);
            int errorLine = XML_GetCurrentLineNumber(p);
            int errorCol = XML_GetCurrentColumnNumber(p);
            const XML_LChar *errorString = XML_ErrorString(errorCode);
                std::stringstream errorDesc;
            errorDesc << "XML parsing error at line " 
                << errorLine << ":" << errorCol;
            errorDesc << ": " << errorString;
            error = errorDesc.str();
            return false;
        }
        count += n;
    } while (!done);

    error = "";
    return true;
}

// node GUID composed of:
// rightmost 8 bits - type 
// next 16 bits - easting in tens of metres
// final 17 bits - northing in tens of metres

long long getNodeGUID(double lon, double lat,std::map<std::string,std::string>
						&nodetags)
{
	char types[7][2][1024] = { 
			{ "place","city" },
			 { "place","town" },
			 { "place","village" },
			 { "place","hamlet" },
			 { "amenity","pub" },
			 { "natural","peak" },
			 { "amenity","restaurant" }
			};
	long long type = -1;
	for(int i=0; i<7; i++)
	{
		if(nodetags[types[i][0]]==types[i][1])
		{
			type=(long long)i;
			break;
		}
	}
	if(type<0LL)
		return 0LL;
	LatLng ll(lat,lon);
	ll.toOSGB36();
	OSRef gr=ll.toOSRef();
	long long easting=(long long)(gr.getEasting()/10);
	long long northing=(long long)(gr.getNorthing()/10);
	easting<<=8;
	northing<<=24;
	return northing|easting|type;
}

// way GUID composed of:
// rightmost 4 bits - bearing (16 point scale) (w'most to e'most)
// next 8 bits - distance in tenths of km (w'most to e'most)
// next 16 bits - easting in tens of metres (westernmost point)
// final 17 bits - northing in tens of metres (westernmost point)

long long getWayGUID(std::vector<Node*> waynodes)
{
	Node *east, *west;
	west = (waynodes[0]->lon < waynodes[waynodes.size()-1]->lon ) ?
		waynodes[0] : waynodes[waynodes.size()-1];
	east = (west==waynodes[0]) ? waynodes[waynodes.size()-1]:waynodes[0];
	LatLng llWest(west->lat,west->lon), llEast(east->lat,east->lon);
	llWest.toOSGB36();
	llEast.toOSGB36();
	OSRef grWest = llWest.toOSRef(), grEast = llEast.toOSRef();
	double dy=grEast.getNorthing()/10-grWest.getNorthing()/10,
			dx=grEast.getEasting()/10-grWest.getEasting()/10;
	double bearing = atan2(dy,dx);
	bearing *= (8.0/M_PI); // convert to 16 compass points - 4 bits
	if(bearing<0) bearing+=16;
	// dist in tenths of km - up to max length 25.6 - 8 bits
	double dist= (sqrt((dx*dx)+(dy*dy)))/10;
	long long easting=(long long)(grWest.getEasting()/10), 
		northing=(long long)(grWest.getNorthing()/10);
	easting<<=12; 
	northing<<=28;
	return northing|easting|(long long)dist|(long long)bearing;
}

