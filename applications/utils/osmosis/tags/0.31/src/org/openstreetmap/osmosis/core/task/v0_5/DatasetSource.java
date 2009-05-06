// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.task.v0_5;

import org.openstreetmap.osmosis.core.task.common.Task;


/**
 * Defines the interface for tasks producing datasets.
 * 
 * @author Brett Henderson
 */
public interface DatasetSource extends Task {
	
	/**
	 * Sets the dataset sink to send data to.
	 * 
	 * @param datasetSink
	 *            The sink for receiving all produced data.
	 */
	void setDatasetSink(DatasetSink datasetSink);
}
