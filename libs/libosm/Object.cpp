#include "Object.h"
#include <iostream>

using std::endl;

namespace OSM
{

std::vector<std::string> Object::getTags()
{
	std::vector<std::string> t;

	for(std::map<std::string,std::string>::iterator i=tags.begin(); 
			i!=tags.end(); i++)
	{
		t.push_back(i->first);
	}

	return t;
}

// 260107 converted ' to "
void Object::tagsToXML(std::ostream &strm)
{
	for(std::map<std::string,std::string>::iterator i=tags.begin(); 
			i!=tags.end(); i++)
	{
		strm << "<tag k=\"" << i->first << "\" v=\"" << i->second << "\" />" << 
			endl;
	}
}

}
