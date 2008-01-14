package com.bretth.osmosis.core.store;

import java.util.Iterator;


/**
 * Simple wrapper around a standard iterator to allow it to be used as a
 * releasable iterator.
 * 
 * @param <T>
 *            The iterator type.
 * @author Brett Henderson
 */
public class ReleasableAdaptorForIterator<T> implements ReleasableIterator<T> {
	
	private Iterator<T> source;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param source
	 *            The input source.
	 */
	public ReleasableAdaptorForIterator(Iterator<T> source) {
		this.source= source;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasNext() {
		return source.hasNext();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public T next() {
		return source.next();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove() {
		source.remove();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		// Do nothing.
	}
}
