#include "Parser.h"
#include <string>
#include <cstring>
#include <sstream>



int Parser::curID = 0;
bool Parser::inNode = false, Parser::inWay = false;
std::string Parser::error = "";
int Parser::wayCount = 1;
bool Parser::initialRun = true;
std::map<int,Node*>  Parser::nodes;
Way Parser::curWay;

std::string Parser::getError()
{
    return error;
}

void Parser::startElement(void *d, const XML_Char* element,
        const XML_Char** attrs)
{
    double lat, lon;
    int count=0;


    if(!strcmp(element,"osm"))
    {
        if(!initialRun)
            cout<<"<osm>"<<endl;
    }
    if (!strcmp(element, "node"))
    {
        curID = 0;
        inNode = true;
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
        // store the node if first run
        if(initialRun)
        {
            nodes[curID] = new Node(lat,lon); 
            ///cerr<<"Storing node: "<<curID<<endl;
        }
        // write the node straight out if second run
        else    
        {    
			cout.precision(8);
            cout << "<node id=\""<<curID<<"\" lat=\""<<lat<<"\" lon=\""
                    <<lon<<"\">" << endl;
        }
    }
    else if (!strcmp(element, "way"))
    {
        inWay = true;

        // do nothing on first run
        if(!initialRun)
        {    
            curWay.tags.clear();
            curWay.nds.clear();

            while (attrs[count])
            {
                if (!strcmp(attrs[count], "id"))
                    curWay.tags["osm_id"] = attrs[count+1];
                count += 2;
            }
        }
    }
    else if (!strcmp(element, "nd") && (inWay))
    {
        int ndID;

        for (int count = 0; attrs[count]; count += 2)
        {
            if (!strcmp(attrs[count], "ref"))
            {
                ndID = atoi(attrs[count + 1]);

                // Increase the count for this node if initial run
                if(initialRun && nodes[ndID])
                {
                    nodes[ndID]->count++;
                    /*
                    cerr<<"Increasing count of node: "<<ndID<<
                        " count now " <<nodes[ndID]->count<<endl;
                    */
                }
                else
                {
                    curWay.nds.push_back(ndID);
                }
            }
        }
    }
    else if (!strcmp(element, "tag"))
    {

        // write out tags (for node and way) in second run
        if(!initialRun)
        {
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

            if(inNode)
                cout<<"<tag k=\""<<key<<"\" v=\""<<value<<"\" />"<<endl;
            else if (inWay) // save as might be applied multiple times
            {
                curWay.tags[key] = value;
            }
        }
    }
}

void Parser::endElement(void *d, const XML_Char* element)
{
    if (!strcmp(element, "node"))
    {
        inNode = false;
        if(!initialRun)
            cout << "</node>\n";
    }
    else if (!strcmp(element, "way"))
    {
        inWay = false;
        if(!initialRun)
        {
            cout << "<way id=\""<<wayCount++<<"\">\n";
            for(int i=0; i<curWay.nds.size(); i++)
            {
                cout<<"<nd ref=\""<<curWay.nds[i]<<"\" />" << endl;

                // split the way if it\"s a node in more than 1 way and not
                // the end nodes
                if(i && i!=curWay.nds.size()-1 &&
                    nodes[curWay.nds[i]] && nodes[curWay.nds[i]]->count >= 2)
                {
                    writeCurrentTags(curWay.tags);//write the tags for the way
                    cout << "</way>\n";
                    cout << "<way id=\""<<wayCount++<<"\">\n";
                    cout<<"<nd ref=\""<<curWay.nds[i]<<"\" />" << endl;
                }
            }
            writeCurrentTags(curWay.tags);
            cout << "</way>" << endl;
        }
    }
    else if (!strcmp(element,"osm"))
    {
        if(initialRun)
            initialRun=false;
        else
        {
            cout<<"</osm>"<<endl;
            freeNodes();
        }
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

