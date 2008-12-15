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

/**
 * Superclass for OSM objects -- nodes, ways, relations. Provides a unique ID,
 * a name and the ability to attach tags.
 */
class Object
{
protected:

public:
	/**
	 * Constructor
	 * @param id Object identifier, default is 0.
	 */
	Object(int id = 0);

	/**
	 * @return Object ID
	 */
	int id() const;

	/**
	 * Set a new object ID
	 * @param id New ID to store
	 */
	void setId(int id);

	/**
	 * Object name mutator
	 * @param name New object name
	 */
	void setName(const std::string& name);

	/**
	 * Object name accessor
	 * @return Name of the object
	 */
	std::string getName();

	/**
	 * @return True iff the ID is positive
	 * @todo FIXME: this method looks like a hack
	 */
	bool isFromOSM();

	/**
	 * Add a tag to the object
	 * @param key Tag key
	 * @param value Tag value
	 */
	void addTag(std::string key, std::string value);

	/**
	 * Accessor for the value of the tag with the given key
	 * @param key Tag key of the tag to query
	 * @return Value of the tag with the given key
	 */
	std::string getTag(const std::string& key);

	/**
	 * @return All tag keys attached to this object
	 */
	std::vector<std::string> getTags() const;

	/**
	 * @return All key/value pairs of the tags attached to this object
	 */
	std::map<std::string, std::string> const &tags() const;

        /**
         * @return All key/value pairs of the tags attached to this object
         */
        std::map<std::string, std::string> &tags();

	/**
	 * @return True if this object contains at least one tag
	 */
	bool hasTags() const;

	/**
	 * Writes an OSM xml representation of all tags attached to this object
	 * to the given stream
	 * @param strm Stream to write xml representation of the tags to
	 */
	void tagsToXML(std::ostream &strm);

private:
	/** Object ID */
	int m_id;

	/** Tags attached to this object */
	std::map<std::string, std::string> m_tags;
};

}

#endif
