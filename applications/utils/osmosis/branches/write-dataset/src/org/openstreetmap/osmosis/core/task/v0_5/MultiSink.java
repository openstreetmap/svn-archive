// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.task.v0_5;


/**
 * Defines the interface for tasks consuming multiple streams of data. It allows
 * a task to expose multiple sinks. Classes may choose to combine this with
 * other multi sink style interfaces where data streams of different types are
 * to be consumed.
 * 
 * @author Brett Henderson
 */
public interface MultiSink {
	/**
	 * Obtains one of the sinks exposed by the task.
	 * 
	 * @param instance
	 *            The index of the sink to be returned.
	 * @return The sink.
	 */
	Sink getSink(int instance);


	/**
	 * Returns the number of sinks provided by this task.
	 * 
	 * @return The number of sinks.
	 */
	int getSinkCount();
}
