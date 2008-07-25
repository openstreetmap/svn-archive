package org.openstreetmap.gui.jmapviewer;

//License: GPL. Copyright 2008 by Jan Peter Stotz

/**
 * An extension to the {@link Runnable} interface adding the possibility to
 * {@link #stop()} it.
 * 
 * @author Jan Peter Stotz
 */
public interface Job extends Runnable {

	/**
	 * Allows to stop / cancel a job without having to interrupt the executing
	 * {@link Thread}.
	 */
	public void stop();
}
