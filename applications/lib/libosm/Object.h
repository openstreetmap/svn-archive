#ifndef OSMOBJECT_H
#define OSMOBJECT_H

/*
 Copyright (C) 2006 Nick Whitelegg, Hogweed Software, nick@hogweed.org

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111 USA

 */

#include <vector>
#include <map>
#include <string>

// based on osmeditor2 code - changed qt stuff to std::b stuff

namespace OSM
{

class Object
{
protected:

public:
	std::map<std::string, std::string> tags;

	Object(int id = 0);

	int id() const;

	void setId(int id);

	void setName(const std::string& n);

	std::string getName();

	bool isFromOSM();

	void addTag(std::string key, std::string value);

	std::string getTag(const std::string& tag);

	std::vector<std::string> getTags();

	bool hasTags() const;

	void tagsToXML(std::ostream &strm);

private:
	int m_id;
};

}

#endif
