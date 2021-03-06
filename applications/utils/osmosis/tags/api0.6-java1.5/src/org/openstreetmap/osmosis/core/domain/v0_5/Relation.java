// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.domain.v0_5;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.openstreetmap.osmosis.core.domain.common.TimestampContainer;
import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreReader;
import org.openstreetmap.osmosis.core.store.StoreWriter;
import org.openstreetmap.osmosis.core.util.IntAsChar;


/**
 * A data class representing a single OSM relation.
 * 
 * @author Brett Henderson
 */
public class Relation extends Entity implements Comparable<Relation> {
	private static final long serialVersionUID = 1L;
	
	
	private List<RelationMember> memberList;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 *            The unique identifier.
	 * @param timestamp
	 *            The last updated timestamp.
	 * @param user
	 *            The user that last modified this entity.
	 */
	public Relation(long id, Date timestamp, OsmUser user) {
		super(id, timestamp, user);
		
		memberList = new ArrayList<RelationMember>();
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 *            The unique identifier.
	 * @param timestampContainer
	 *            The container holding the timestamp in an alternative
	 *            timestamp representation.
	 * @param user
	 *            The user that last modified this entity.
	 */
	public Relation(long id, TimestampContainer timestampContainer, OsmUser user) {
		super(id, timestampContainer, user);
		
		memberList = new ArrayList<RelationMember>();
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param sr
	 *            The store to read state from.
	 * @param scr
	 *            Maintains the mapping between classes and their identifiers
	 *            within the store.
	 */
	public Relation(StoreReader sr, StoreClassRegister scr) {
		super(sr, scr);
		
		int nodeCount;
		
		nodeCount = sr.readCharacter();
		
		memberList = new ArrayList<RelationMember>();
		for (int i = 0; i < nodeCount; i++) {
			addMember(new RelationMember(sr, scr));
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	
	@Override
	public void store(StoreWriter sw, StoreClassRegister scr) {
		super.store(sw, scr);
		
		sw.writeCharacter(IntAsChar.intToChar(memberList.size()));
		for (RelationMember relationMember : memberList) {
			relationMember.store(sw, scr);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	
	@Override
	public EntityType getType() {
		return EntityType.Relation;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Relation) {
			return compareTo((Relation) o) == 0;
		} else {
			return false;
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	
	@Override
	public int hashCode() {
		/*
		 * As per the hashCode definition, this doesn't have to be unique it
		 * just has to return the same value for any two objects that compare
		 * equal. Using id will provide a good distribution of values but is
		 * simple to calculate.
		 */
		return (int) getId();
	}
	
	
	/**
	 * Compares this member list to the specified member list. The bigger list
	 * is considered bigger, if that is equal then each relation member is
	 * compared.
	 * 
	 * @param comparisonMemberList
	 *            The member list to compare to.
	 * @return 0 if equal, < 0 if considered "smaller", and > 0 if considered
	 *         "bigger".
	 */
	protected int compareMemberList(List<RelationMember> comparisonMemberList) {
		// The list with the most entities is considered bigger.
		if (memberList.size() != comparisonMemberList.size()) {
			return memberList.size() - comparisonMemberList.size();
		}
		
		// Check the individual node references.
		for (int i = 0; i < memberList.size(); i++) {
			int result = memberList.get(i).compareTo(comparisonMemberList.get(i));
			
			if (result != 0) {
				return result;
			}
		}
		
		// There are no differences.
		return 0;
	}


	/**
	 * Compares this relation to the specified relation. The relation comparison
	 * is based on a comparison of id, timestamp, and tags in that order.
	 * 
	 * @param comparisonRelation
	 *            The relation to compare to.
	 * @return 0 if equal, < 0 if considered "smaller", and > 0 if considered
	 *         "bigger".
	 */
	public int compareTo(Relation comparisonRelation) {
		int memberListResult;
		
		if (this.getId() < comparisonRelation.getId()) {
			return -1;
		}
		if (this.getId() > comparisonRelation.getId()) {
			return 1;
		}
		
		if (this.getTimestamp() == null && comparisonRelation.getTimestamp() != null) {
			return -1;
		}
		if (this.getTimestamp() != null && comparisonRelation.getTimestamp() == null) {
			return 1;
		}
		if (this.getTimestamp() != null && comparisonRelation.getTimestamp() != null) {
			int result;
			
			result = this.getTimestamp().compareTo(comparisonRelation.getTimestamp());
			
			if (result != 0) {
				return result;
			}
		}
		
		memberListResult = compareMemberList(
			comparisonRelation.memberList
		);
		
		if (memberListResult != 0) {
			return memberListResult;
		}
		
		return compareTags(comparisonRelation.getTagList());
	}
	
	
	/**
	 * Returns the attached list of relation members. The returned list is
	 * read-only.
	 * 
	 * @return The member list.
	 */
	public List<RelationMember> getMemberList() {
		return Collections.unmodifiableList(memberList);
	}
	
	
	/**
	 * Adds a new member.
	 * 
	 * @param member
	 *            The member to add.
	 */
	public void addMember(RelationMember member) {
		memberList.add(member);
	}
	
	
	/**
	 * Adds all members in the collection to the relation.
	 * 
	 * @param members
	 *            The collection of members to be added.
	 */
	public void addMembers(Collection<RelationMember> members) {
		memberList.addAll(members);
	}
}
