// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.customdb.v0_5.impl;

import java.util.Iterator;

import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.LongLongIndexElement;


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
	
	public boolean hasNext() {
		return source.hasNext();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	
	public Long next() {
		return source.next().getValue();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	
	public void remove() {
		source.remove();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	
	public void release() {
		// Do nothing.
	}
}
