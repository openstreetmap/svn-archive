// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.store;

/**
 * Implementations provide functionality to serialise a Storeable implementation
 * to a store.
 * 
 * @author Brett Henderson
 */
public interface ObjectWriter {
	
	/**
	 * Writes an object to storage.
	 * 
	 * @param value
	 *            The object to be written.
	 */
	void writeObject(Storeable value);
}
