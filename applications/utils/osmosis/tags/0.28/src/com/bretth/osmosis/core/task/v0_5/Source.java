// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.task.v0_5;

import com.bretth.osmosis.core.task.common.Task;


/**
 * Defines the interface for tasks producing OSM data types.
 * 
 * @author Brett Henderson
 */
public interface Source extends Task {
	
	/**
	 * Sets the osm sink to send data to.
	 * 
	 * @param sink
	 *            The sink for receiving all produced data.
	 */
	void setSink(Sink sink);
}
