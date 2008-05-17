// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.task.v0_6;


/**
 * Extends the basic ChangeSource interface with the Runnable capability.
 * Runnable is not applied to the ChangeSource interface because tasks that act
 * as filters do not require Runnable capability.
 * 
 * @author Brett Henderson
 */
public interface RunnableChangeSource extends ChangeSource, Runnable {
	// This interface combines ChangeSource and Runnable but doesn't introduce
	// methods of its own.
}
