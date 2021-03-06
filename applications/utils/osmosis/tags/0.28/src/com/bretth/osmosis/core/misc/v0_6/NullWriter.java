// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.misc.v0_6;

import com.bretth.osmosis.core.container.v0_6.EntityContainer;
import com.bretth.osmosis.core.task.v0_6.Sink;


/**
 * An OSM data sink that discards all data sent to it. This is primarily
 * intended for benchmarking purposes.
 * 
 * @author Brett Henderson
 */
public class NullWriter implements Sink {
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		// Discard the data.
	}
	
	
	/**
	 * Flushes all changes to file.
	 */
	public void complete() {
		// Nothing to do.
	}
	
	
	/**
	 * Cleans up any open file handles.
	 */
	public void release() {
		// Nothing to do.
	}
}
