// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.domain.v0_6;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import com.bretth.osmosis.core.domain.common.SimpleTimestampContainer;
import com.bretth.osmosis.core.domain.common.TimestampContainer;
import com.bretth.osmosis.core.store.Storeable;
import com.bretth.osmosis.core.util.LongAsInt;


/**
 * Provides facilities to specify the contents of entity and create new
 * instances. Entities themselves are immutable to support concurrent access,
 * this class provides a means of manipulating them.
 * 
 * @author Brett Henderson
 * 
 * @param <T> The type of entity to be built.
 */
public abstract class EntityBuilder<T extends Entity> implements Storeable {
	
	protected TimestampContainer dummyTimestampContainer;
	protected long id;
	protected int version;
	protected TimestampContainer timestampContainer;
	protected OsmUser user;
	protected Collection<Tag> tags;
	
	
	/**
	 * Creates a new instance.
	 */
	public EntityBuilder() {
		tags = new ArrayList<Tag>();
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param entity
	 *            The entity to initialise to.
	 */
	public EntityBuilder(Entity entity) {
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
	public EntityBuilder(long id, int version, Date timestamp, OsmUser user) {
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
	public EntityBuilder(long id, int version, TimestampContainer timestampContainer, OsmUser user) {
		this();
		
		initialize(id, version, timestampContainer, user);
	}
	
	
	/**
	 * Initializes the contents of the builder to the specified data.
	 * 
	 * @param entity
	 *            The entity to initialise to.
	 * @return This object allowing method chaining.
	 */
	protected EntityBuilder<T> initialize(Entity entity) {
		// Delegate to the more specific method.
		initialize(entity.getId(), entity.getVersion(), entity.getTimestampContainer(), entity.getUser());
		
		tags.addAll(entity.getTags());
		
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
	 * @return This object allowing method chaining.
	 */
	protected EntityBuilder<T> initialize(long newId, int newVersion, Date newTimestamp, OsmUser newUser) {
		// Delegate to the more specific method.
		initialize(newId, newVersion, new SimpleTimestampContainer(newTimestamp), newUser);
		
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
	 * @return This object allowing method chaining.
	 */
	protected EntityBuilder<T> initialize(long newId, int newVersion, TimestampContainer newTimestampContainer, OsmUser newUser) {
		this.id = LongAsInt.longToInt(newId);
		this.timestampContainer = newTimestampContainer;
		this.user = newUser;
		this.version = newVersion;
		
		this.tags.clear();
		
		return this;
	}
	
	
	/**
	 * Sets a new id value.
	 * 
	 * @param newId
	 *            The new id.
	 * @return This object allowing method chaining.
	 */
	public EntityBuilder<T> setId(long newId) {
		this.id = newId;
		
		return this;
	}
	
	
	/**
	 * Gets the current id value.
	 * 
	 * @return The id.
	 */
	public long getId() {
		return id;
	}
	
	
	/**
	 * Sets a new version value.
	 * 
	 * @param newVersion
	 *            The new version.
	 * @return This object allowing method chaining.
	 */
	public EntityBuilder<T> setVersion(int newVersion) {
		this.version = newVersion;
		
		return this;
	}
	
	
	/**
	 * Gets the current version value.
	 * 
	 * @return The id.
	 */
	public int getVersion() {
		return version;
	}
	
	
	/**
	 * Sets a new timestamp value.
	 * 
	 * @param timestamp
	 *            The new timestamp.
	 * @return This object allowing method chaining.
	 */
	public EntityBuilder<T> setTimestamp(Date timestamp) {
		this.timestampContainer = new SimpleTimestampContainer(timestamp);
		
		return this;
	}
	
	
	/**
	 * Gets the current timestamp value.
	 * 
	 * @return The timestamp.
	 */
	public Date getTimestamp() {
		return timestampContainer.getTimestamp();
	}
	
	
	/**
	 * Sets a new timestamp value.
	 * 
	 * @param timestampContainer
	 *            The timestamp wrapped within a container.
	 * @return This object allowing method chaining.
	 */
	public EntityBuilder<T> setTimestamp(TimestampContainer timestampContainer) {
		this.timestampContainer = timestampContainer;
		
		return this;
	}
	
	
	/**
	 * Gets the current timestamp value.
	 * 
	 * @return The timestamp container holding the current timestamp.
	 */
	public TimestampContainer getTimestampContainer() {
		return timestampContainer;
	}
	
	
	/**
	 * Sets a new user value.
	 * 
	 * @param newUser
	 *            The new user.
	 * @return This object allowing method chaining.
	 */
	public EntityBuilder<T> setUser(OsmUser newUser) {
		this.user = newUser;
		
		return this;
	}
	
	
	/**
	 * Gets the current user value.
	 * 
	 * @return The user.
	 */
	public OsmUser getUser() {
		return user;
	}
	
	
	/**
	 * Obtains the tags.
	 * 
	 * @return The tags.
	 */
	public Collection<Tag> getTags() {
		return tags;
	}
	
	
	/**
	 * Remove all existing tags.
	 * 
	 * @return This object allowing method chaining.
	 */
	public EntityBuilder<T> clearTags() {
		tags.clear();
		
		return this;
	}
	
	
	/**
	 * Sets a new tags value.
	 * 
	 * @param newTags
	 *            The new tags.
	 * @return This object allowing method chaining.
	 */
	public EntityBuilder<T> setTags(Collection<Tag> newTags) {
		tags.clear();
		tags.addAll(newTags);
		
		return this;
	}
	
	
	/**
	 * Adds a new tag.
	 * 
	 * @param tag
	 *            The new tag.
	 * @return This object allowing method chaining.
	 */
	public EntityBuilder<T> addTag(Tag tag) {
		tags.add(tag);
		
		return this;
	}
	
	
	/**
	 * Builds a new entity instance based on the current data.
	 * 
	 * @return The new entity instance.
	 */
	public abstract T buildEntity();
}
