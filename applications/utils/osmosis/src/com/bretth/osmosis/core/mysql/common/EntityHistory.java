package com.bretth.osmosis.core.mysql.common;

import com.bretth.osmosis.core.store.ObjectReader;
import com.bretth.osmosis.core.store.ObjectWriter;
import com.bretth.osmosis.core.store.StoreClassRegister;
import com.bretth.osmosis.core.store.StoreReader;
import com.bretth.osmosis.core.store.StoreWriter;
import com.bretth.osmosis.core.store.Storeable;


/**
 * A data class representing a history record.
 * 
 * @author Brett Henderson
 * @param <T>
 *            The type of entity that this class stores history for.
 */
public class EntityHistory<T extends Storeable> implements Storeable {
	
	private T entity;
	private int version;
	private boolean visible;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param entity
	 *            The contained entity.
	 * @param version
	 *            The version of the history element.
	 * @param visible
	 *            The visible field.
	 */
	public EntityHistory(T entity, int version, boolean visible) {
		this.entity = entity;
		this.version = version;
		this.visible = visible;
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
	@SuppressWarnings("unchecked")
	public EntityHistory(StoreReader sr, StoreClassRegister scr) {
		entity = (T) new ObjectReader(sr, scr).readObject();
		
		version = sr.readInteger();
		visible = sr.readBoolean();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void store(StoreWriter sw, StoreClassRegister scr) {
		new ObjectWriter(sw, scr).writeObject(entity);
		sw.writeInteger(version);
		sw.writeBoolean(visible);
	}
	
	
	/**
	 * Gets the contained entity.
	 * 
	 * @return The entity.
	 */
	public T getEntity() {
		return entity;
	}
	
	
	/**
	 * Gets the version.
	 * 
	 * @return The version.
	 */
	public int getVersion() {
		return version;
	}
	
	
	/**
	 * Gets the visible flag.
	 * 
	 * @return The visible flag.
	 */
	public boolean isVisible() {
		return visible;
	}
}
