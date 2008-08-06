package org.openstreetmap.gui.jmapviewer.interfaces;

//License: GPL. Copyright 2008 by Jan Peter Stotz

/**
 * Interface for implementing a tile loader. Tiles are usually loaded via HTTP
 * or from a file. The {@link TileLoader} implementation is responsible for
 * performing the loading action asynchronously by creating a new
 * {@link Runnable} or {@link Job}.
 * 
 * @author Jan Peter Stotz
 */
public interface TileLoader {

	/**
	 * A typical {@link #addLoadRequest(int, int, int)} implementation should
	 * create a new {@link Job} instance that performs the load action. This
	 * {@link Job} instance is then handed over to
	 * <code>map.jobDispatcher</code>.
	 * 
	 * @param tilex
	 * @param tiley
	 * @param zoom
	 */
	public void addLoadRequest(int tilex, int tiley, int zoom);
}
