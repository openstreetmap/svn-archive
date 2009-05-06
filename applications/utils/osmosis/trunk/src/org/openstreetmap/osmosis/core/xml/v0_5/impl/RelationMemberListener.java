// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.xml.v0_5.impl;

import org.openstreetmap.osmosis.core.domain.v0_5.RelationMember;


/**
 * Provides the definition of a class receiving relation members.
 * 
 * @author Brett Henderson
 */
public interface RelationMemberListener {
	/**
	 * Processes the relation member.
	 * 
	 * @param relationMember
	 *            The relation member.
	 */
	void processRelationMember(RelationMember relationMember);
}
