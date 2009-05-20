// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.store;

import java.util.Iterator;

import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;

/**
 * A releasable iterator implementation for reading data from an underlying
 * non-releasable iterator. Note that the release method has nothing to do in this
 * implementation.
 * 
 * @param <T>
 *            The data type to be iterated.
 * @author Brett Henderson
 */
public class IteratorReleasableIterator<T> implements ReleasableIterator<T>{
	
	private Iterator<T> iterator;


	/**
	 * Creates a new instance.
	 * 
	 * @param iterator
	 *            The underlying iterator to read from.
	 */
	public IteratorReleasableIterator(Iterator<T> iterator) {
		this.iterator = iterator;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	
	public boolean hasNext() {
		return iterator.hasNext();
	}
	
	/**
	 * {@inheritDoc}
	 */
	
	public T next() {
		return iterator.next();
	}
	
	/**
	 * {@inheritDoc}
	 */
	
	public void remove() {
		iterator.remove();
	}
	
	/**
	 * {@inheritDoc}
	 */
	
	public void release() {
		// Do nothing.
	}
}
