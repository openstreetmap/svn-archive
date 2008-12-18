package com.bretth.osmosis.core.customdb.v0_6.impl;

import java.util.Iterator;

import com.bretth.osmosis.core.store.LongLongIndexElement;
import com.bretth.osmosis.core.store.ReleasableIterator;


/**
 * Iterates over a relational index iterator and returns all the id values
 * stored against the index elements.
 * 
 * @author Brett Henderson
 */
public class RelationalIndexValueIdIterator implements ReleasableIterator<Long> {
	private Iterator<LongLongIndexElement> source;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param source
	 *            The input source.
	 */
	public RelationalIndexValueIdIterator(Iterator<LongLongIndexElement> source) {
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
	public Long next() {
		return source.next().getValue();
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
