// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.task.v0_5;


/**
 * Extends the basic DatasetSource interface with the Runnable capability.
 * Runnable is not applied to the DatasetSource interface because tasks that act
 * as filters do not require Runnable capability.
 * 
 * @author Brett Henderson
 */
public interface RunnableDatasetSource extends DatasetSource, Runnable {
	// This interface combines DatasetSource and Runnable but doesn't introduce
	// methods of its own.
}
