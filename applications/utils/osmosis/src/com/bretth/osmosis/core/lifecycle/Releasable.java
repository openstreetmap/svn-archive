// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.lifecycle;


/**
 * Classes that hold heavyweight resources that can't wait for garbage
 * collection should implement this interface. It provides a release method that
 * should be called by all clients when the class is no longer required. This
 * release method is guaranteed not to throw exceptions and should always be
 * called within a finally clause.
 * 
 * @author Brett Henderson
 */
public interface Releasable {
	/**
	 * Performs resource cleanup tasks such as closing files, or database
	 * connections. This must be called after all processing is complete and may
	 * be called multiple times. Implementations must call release on any nested
	 * Releasable objects. It should be called within a finally block to ensure
	 * it is called in exception scenarios.
	 */
	public void release();
}
