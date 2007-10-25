package com.bretth.osmosis.core.task.v0_5;

import com.bretth.osmosis.core.task.common.Task;


/**
 * Defines the interface for tasks producing multiple change streams of OSM data.
 * 
 * @author Brett Henderson
 */
public interface MultiChangeSource extends Task {
	
	/**
	 * Retrieves a specific change source that can then have a change sink
	 * attached.
	 * 
	 * @param index
	 *            The index of the change source to retrieve.
	 * @return The requested index.
	 */
	ChangeSource getChangeSource(int index);
	
	
	/**
	 * Indicates the number of change sources that the task provides.
	 * 
	 * @return The number of change sources.
	 */
	int getChangeSourceCount();
}
