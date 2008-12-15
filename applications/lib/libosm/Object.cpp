#include "Object.h"
#include <iostream>

using namespace std;

namespace OSM
{

vector<string> Object::getTags() const
{
	vector<string> t;

	for (map<string, string>::const_iterator i = m_tags.begin(); i
			!= m_tags.end(); i++)
	{
		t.push_back(i->first);
	}

	return t;
}

// 260107 converted ' to "
void Object::tagsToXML(ostream &strm)
{
	for (map<string, string>::const_iterator i = m_tags.begin(); i
			!= m_tags.end(); i++)
	{
		strm << "    <tag k=\"" << i->first << "\" v=\"" << i->second << "\"/>"
				<< endl;
	}
}

bool Object::hasTags() const
{
	return m_tags.size() > 0;
}

int Object::id() const
{
	return m_id;
}

void Object::setId(int id)
{
	m_id = id;
}

Object::Object(int id) :
	m_id(id)
{
}

void Object::setName(const string& n)
{
	//name = n;
	m_tags["name"] = n;
}

string Object::getName()
{
	//return name;
	return (m_tags.find("name") != m_tags.end()) ? m_tags["name"] : "";
}

bool Object::isFromOSM()
{
	return m_id > 0;
}

void Object::addTag(string key, string value)
{
	m_tags[key] = value;
}

string Object::getTag(const string& tag)
{
	return (m_tags.find(tag) != m_tags.end()) ? m_tags[tag] : "";
}

map<string, string> const &Object::tags() const
{
	return m_tags;
}

map<string, string> &Object::tags()
{
        return m_tags;
}


}
