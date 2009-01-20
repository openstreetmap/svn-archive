// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.domain.v0_6;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.bretth.osmosis.core.domain.common.TimestampContainer;
import com.bretth.osmosis.core.store.StoreClassRegister;
import com.bretth.osmosis.core.store.StoreReader;
import com.bretth.osmosis.core.store.StoreWriter;


/**
 * Provides the ability to manipulate relations.
 * 
 * @author Brett Henderson
 */
public class RelationBuilder extends EntityBuilder<Relation> {
	private List<RelationMember> members;
	
	
	/**
	 * Creates a new instance.
	 */
	public RelationBuilder() {
		super();
		
		members = new ArrayList<RelationMember>();
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param entity
	 *            The entity to initialise to.
	 */
	public RelationBuilder(Relation entity) {
		this();
		
		initialize(entity);
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 *            The unique identifier.
	 * @param version
	 *            The version of the entity.
	 * @param timestamp
	 *            The last updated timestamp.
	 * @param user
	 *            The user that last modified this entity.
	 */
	public RelationBuilder(long id, int version, Date timestamp, OsmUser user) {
		this();
		
		initialize(id, version, timestamp, user);
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 *            The unique identifier.
	 * @param version
	 *            The version of the entity.
	 * @param timestampContainer
	 *            The container holding the timestamp in an alternative
	 *            timestamp representation.
	 * @param user
	 *            The user that last modified this entity.
	 */
	public RelationBuilder(long id, TimestampContainer timestampContainer, OsmUser user, int version) {
		this();
		
		initialize(id, version, timestampContainer, user);
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
	public RelationBuilder(StoreReader sr, StoreClassRegister scr) {
		this();
		
		initialize(new Relation(sr, scr));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void store(StoreWriter sw, StoreClassRegister scr) {
		buildEntity().store(sw, scr);
	}
	
	
	/**
	 * Initialises the state of this sub-class.
	 */
	private void initializeLocal() {
		members.clear();
	}
	
	
	/**
	 * Initializes the contents of the builder to the specified data.
	 * 
	 * @param relation
	 *            The entity to initialise to.
	 * @return This object allowing method chaining.
	 */
	public RelationBuilder initialize(Relation relation) {
		super.initialize(relation);
		initializeLocal();
		members.addAll(relation.getMembers());
		
		return this;
	}
	
	
	/**
	 * Initializes the contents of the builder to the specified data.
	 * 
	 * @param newId
	 *            The unique identifier.
	 * @param newVersion
	 *            The version of the entity.
	 * @param newTimestamp
	 *            The last updated timestamp.
	 * @param newUser
	 *            The user that last modified this entity.
	 */
	@Override
	public RelationBuilder initialize(long newId, int newVersion, Date newTimestamp, OsmUser newUser) {
		super.initialize(newId, newVersion, newTimestamp, newUser);
		initializeLocal();
		
		return this;
	}
	
	
	/**
	 * Initializes the contents of the builder to the specified data.
	 * 
	 * @param newId
	 *            The unique identifier.
	 * @param newVersion
	 *            The version of the entity.
	 * @param newTimestampContainer
	 *            The container holding the timestamp in an alternative
	 *            timestamp representation.
	 * @param newUser
	 *            The user that last modified this entity.
	 */
	@Override
	public RelationBuilder initialize(long newId, int newVersion, TimestampContainer newTimestampContainer, OsmUser newUser) {
		super.initialize(newId, newVersion, newTimestampContainer, newUser);
		initializeLocal();
		
		return this;
	}
	
	
	/**
	 * Obtains the members.
	 * 
	 * @return The members.
	 */
	public List<RelationMember> getMembers() {
		return members;
	}
	
	
	/**
	 * Remove all existing members.
	 * 
	 * @return This object allowing method chaining.
	 */
	public RelationBuilder clearMembers() {
		members.clear();
		
		return this;
	}
	
	
	/**
	 * Sets a new members value.
	 * 
	 * @param members
	 *            The new relation members.
	 * @return This object allowing method chaining.
	 */
	public RelationBuilder setMembers(List<RelationMember> members) {
		members.clear();
		members.addAll(members);
		
		return this;
	}
	
	
	/**
	 * Adds a new member.
	 * 
	 * @param member
	 *            The new member.
	 * @return This object allowing method chaining.
	 */
	public RelationBuilder addMember(RelationMember member) {
		members.add(member);
		
		return this;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Relation buildEntity() {
		return new Relation(id, version, timestampContainer, user, tags, members);
	}
}
