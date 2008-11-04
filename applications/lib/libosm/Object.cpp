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
		strm << "    <tag k=\"" << i->first << "\" v=\"" << i->second << "\"/>" << endl;
	}
}

bool Object::hasTags() const
{
	return tags.size() > 0;
}


int Object::id() const
{
	return m_id;
}

void Object::setId(int id)
{
	m_id = id;
}

Object::Object(int id) : m_id(id)
{
}

void Object::setName(const std::string& n)
{
	//name = n;
	tags["name"] = n;
}

std::string Object::getName()
{
	//return name;
	return (tags.find("name") != tags.end()) ? tags["name"] : "";
}

bool Object::isFromOSM()
{
	return m_id > 0;
}

void Object::addTag(std::string key, std::string value)
{
	tags[key] = value;
}

std::string Object::getTag(const std::string& tag)
{
	return (tags.find(tag) != tags.end()) ? tags[tag] : "";
}

}
