// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.container.v0_5;

import org.openstreetmap.osmosis.core.domain.v0_5.Way;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;


/**
 * Wraps a set of ways into way containers.
 * 
 * @author Brett Henderson
 */
public class WayContainerIterator implements ReleasableIterator<WayContainer> {
	private ReleasableIterator<Way> source;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param source The input source.
	 */
	public WayContainerIterator(ReleasableIterator<Way> source) {
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
	
	public WayContainer next() {
		return new WayContainer(source.next());
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
		source.release();
	}
}
