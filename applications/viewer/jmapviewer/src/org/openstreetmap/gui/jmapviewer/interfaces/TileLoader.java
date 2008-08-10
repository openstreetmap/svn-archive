package org.openstreetmap.gui.jmapviewer.interfaces;

//License: GPL. Copyright 2008 by Jan Peter Stotz

/**
 * Interface for implementing a tile loader. Tiles are usually loaded via HTTP
 * or from a file.
 * 
 * @author Jan Peter Stotz
 */
public interface TileLoader {

	/**
	 * A typical {@link #createTileLoaderJob(int, int, int)} implementation
	 * should create and return a new {@link Job} instance that performs the
	 * load action.
	 * 
	 * @param tilex
	 * @param tiley
	 * @param zoom
	 * @returns {@link Job} implementation that performs the desired load
	 *          action.
	 */
	public Job createTileLoaderJob(int tilex, int tiley, int zoom);
}
