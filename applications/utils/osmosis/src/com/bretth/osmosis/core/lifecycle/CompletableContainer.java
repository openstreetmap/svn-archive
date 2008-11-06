// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.lifecycle;

import java.util.ArrayList;
import java.util.List;



/**
 * A container for completable objects that require complete and release calls
 * to be performed as a unit.
 * 
 * @author Brett Henderson
 */
public class CompletableContainer implements Completable {
	private List<Completable> objects;
	
	
	/**
	 * Creates a new instance.
	 */
	public CompletableContainer() {
		objects = new ArrayList<Completable>();
	}
	
	
	/**
	 * Adds a new object to be managed. The object is returned to allow method
	 * chaining.
	 * 
	 * @param <T>
	 *            The type of object being stored.
	 * @param object
	 *            The object to be stored.
	 * @return The object that was stored.
	 */
	public <T extends Completable> T add(T object) {
		objects.add(object);
		
		return object;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void complete() {
		for (Completable object : objects) {
			object.complete();
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		for (Releasable object : objects) {
			object.release();
		}
	}
}
