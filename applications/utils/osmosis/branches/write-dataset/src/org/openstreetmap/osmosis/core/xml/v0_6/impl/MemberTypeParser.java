// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.xml.v0_6.impl;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;


/**
 * Parses the xml representation of a relation member type into an entity type
 * object.
 * 
 * @author Brett Henderson
 */
public class MemberTypeParser {
	
	private static final Map<String, EntityType> memberTypeMap = new HashMap<String, EntityType>();
	
	static {
		memberTypeMap.put("node", EntityType.Node);
		memberTypeMap.put("way", EntityType.Way);
		memberTypeMap.put("relation", EntityType.Relation);
	}
	
	
	/**
	 * Parses the database representation of a relation member type into an
	 * entity type object.
	 * 
	 * @param memberType
	 *            The database value of member type.
	 * @return A strongly typed entity type.
	 */
	public EntityType parse(String memberType) {
		if (memberTypeMap.containsKey(memberType)) {
			return memberTypeMap.get(memberType);
		} else {
			throw new OsmosisRuntimeException("The member type " + memberType + " is not recognised.");
		}
	}
}
