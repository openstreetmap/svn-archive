// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pipeline.common;


/**
 * A thread implementation for launching a runnable task.
 * 
 * @author Brett Henderson
 */
public class TaskRunner extends Thread {
	/* package */ boolean successful;
	/* package */ Throwable exception;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param task
	 *            The task to be executed.
	 * @param name
	 *            The name of the thread.
	 */
	public TaskRunner(Runnable task, String name) {
		super(task, name);
		
		successful = true;
		
		// Set an exception handler to capture the details of any failure.
		setUncaughtExceptionHandler(
			new UncaughtExceptionHandler() {
				public void uncaughtException(Thread t, Throwable e) {
					successful = false;
					exception = e;
				}
			}
		);
	}
	
	
	/**
	 * This can be called after the thread has completed to determine if the
	 * thread terminated normally.
	 * 
	 * @return True if the thread terminated normally.
	 */
	public boolean isSuccessful() {
		return successful;
	}
	
	
	/**
	 * Returns the reason for abnormal termination.
	 * 
	 * @return The exception causing the failure.
	 */
	public Throwable getException() {
		return exception;
	}
}
