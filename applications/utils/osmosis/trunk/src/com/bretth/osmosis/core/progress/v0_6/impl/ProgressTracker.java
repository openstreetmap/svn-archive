// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.progress.v0_6.impl;

/**
 * Maintains state about execution progress. It calculates when the next update
 * is due, and provides statistics on execution.
 * 
 * @author Brett Henderson
 */
public class ProgressTracker {
	
	private int interval;
	private boolean initialized;
	private long lastUpdateTimestamp;
	private long objectCount;
	private double objectsPerSecond;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param interval
	 *            The interval between logging progress reports in milliseconds.
	 */
	public ProgressTracker(int interval) {
		this.interval = interval;
		
		initialized = false;
	}
	
	
	/**
	 * Indicates if an update is due. This should be called once per object that
	 * is processed.
	 */
	public boolean updateRequired() {
		if (!initialized) {
			lastUpdateTimestamp = System.currentTimeMillis();
			objectCount = 0;
			objectsPerSecond = 0;
			
			initialized = true;
			
			return false;
			
		} else {
			long currentTimestamp;
			long duration;
			
			// Calculate the time since the last update.
			currentTimestamp = System.currentTimeMillis();
			duration = currentTimestamp - lastUpdateTimestamp;
			
			// Increment the processed object count.
			objectCount++;
			
			if (duration > interval || duration < 0) {
				lastUpdateTimestamp = currentTimestamp;
				
				// Calculate the number of objects processed per second.
				objectsPerSecond = (double) objectCount * 1000 / duration;
				
				// Reset the object count.
				objectCount = 0;
				
				return true;
				
			} else {
				return false;
			}
		}
	}
	
	
	/**
	 * Provides the number of objects processed per second. This only becomes
	 * valid after updateRequired returns true for the first time.
	 * 
	 * @return The number of objects processed per second in the last timing
	 *         interval.
	 */
	public double getObjectsPerSecond() {
		return objectsPerSecond;
	}
}
