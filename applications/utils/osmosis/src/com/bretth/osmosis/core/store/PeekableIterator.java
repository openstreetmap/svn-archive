// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.store;

import com.bretth.osmosis.core.lifecycle.ReleasableIterator;




/**
 * Wraps a releasable iterator and adds the ability to peek at the next value
 * without moving to the next record.
 * 
 * @author Brett Henderson
 * 
 * @param <T>
 *            The type of entity to retrieved.
 */
public class PeekableIterator<T> implements ReleasableIterator<T> {
	
	private ReleasableIterator<T> sourceIterator;
	private T nextValue;
	private boolean nextValueAvailable;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param sourceIterator
	 *            The underlying iterator providing source data. This will be
	 *            owned and released by this object.
	 */
	public PeekableIterator(ReleasableIterator<T> sourceIterator) {
		this.sourceIterator = sourceIterator;
		
		nextValue = null;
		nextValueAvailable = false;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasNext() {
		return nextValueAvailable || sourceIterator.hasNext();
	}
	
	
	/**
	 * Returns the next available entity without advancing to the next record.
	 * 
	 * @return The next available entity.
	 */
	public T peekNext() {
		if (!nextValueAvailable) {
			nextValue = sourceIterator.next();
			nextValueAvailable = true;
		}
		
		return nextValue;
	}
	
	 
	/**
	 * {@inheritDoc}
	 */
	public T next() {
		T result;
		
		result = peekNext();
		
		nextValue = null;
		nextValueAvailable = false;
		
		return result;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		sourceIterator.release();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
