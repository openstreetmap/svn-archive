package org.openstreetmap.gui.jmapviewer;

//License: GPL. Copyright 2008 by Jan Peter Stotz

/**
 * Implement this interface for creating your custom tile cache for
 * {@link JMapViewer}.
 * 
 * @author Jan Peter Stotz
 */
public interface TileCache {

	/**
	 * Retrieves a tile from the cache if present, otherwise <code>null</code>
	 * will be returned.
	 * 
	 * @param x
	 *            tile number on the x axis of the tile to be retrieved
	 * @param y
	 *            tile number on the y axis of the tile to be retrieved
	 * @param z
	 *            zoom level of the tile to be retrieved
	 * @return the requested tile or <code>null</code> if the tile is not
	 *         present in the cache
	 */
	public Tile getTile(int x, int y, int z);

	/**
	 * Adds a tile to the cache. How long after adding a tile can be retrieved
	 * via {@link #getTile(int, int, int)} is unspecified and depends on the
	 * implementation.
	 * 
	 * @param tile
	 */
	public void addTile(Tile tile);
}
