// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.container.v0_6;

import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.store.Storeable;


/**
 * Implementations of this class allow data entities to be processed without the
 * caller knowing their type.
 * 
 * @author Brett Henderson
 */
public abstract class EntityContainer implements Storeable {
	/**
	 * Calls the appropriate process method with the contained entity.
	 * 
	 * @param processor
	 *            The processor to invoke.
	 */
	public abstract void process(EntityProcessor processor);
	
	
	/**
	 * Returns the contained entity.
	 * 
	 * @return The entity.
	 */
	public abstract Entity getEntity();
}
