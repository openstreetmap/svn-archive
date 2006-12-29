#include "Object.h"

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

}
