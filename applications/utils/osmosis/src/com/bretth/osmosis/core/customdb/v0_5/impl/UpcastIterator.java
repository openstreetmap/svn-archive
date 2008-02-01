// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.customdb.v0_5.impl;

import com.bretth.osmosis.core.store.ReleasableIterator;


/**
 * A simple pass-through iterator that changes an object from a specific class
 * type to a more general class type.
 * 
 * @param <X>
 *            The generic class type of the destination data.
 * @param <Y>
 *            The more specific class type of the source data.
 * @author Brett Henderson
 */
public class UpcastIterator<X, Y extends X> implements ReleasableIterator<X> {
	
	private ReleasableIterator<Y> source;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param source
	 *            The input source.
	 */
	public UpcastIterator(ReleasableIterator<Y> source) {
		this.source = source;
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
	public X next() {
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
		source.release();
	}
}
