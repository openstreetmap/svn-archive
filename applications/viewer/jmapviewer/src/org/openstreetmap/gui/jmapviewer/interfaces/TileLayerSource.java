package org.openstreetmap.gui.jmapviewer.interfaces;

//License: GPL. Copyright 2008 by Jan Peter Stotz

/**
 * 
 * @author Jan Peter Stotz
 */
public interface TileLayerSource {

	/**
	 * Specifies the maximum zoom value. The number of zoom levels is [0..{@link #getMaxZoom()}] 
	 *  
	 * @return maximum zoom value
	 */
	public int getMaxZoom();
	
	/**
	 * @return Name of the tile layer 
	 */
	public String getName();
	
	/**
	 * @param zoom
	 * @param tilex
	 * @param tiley
	 * @return fully qualified url for downloading the specified tile image
	 */
	public String getTileUrl(int zoom, int tilex, int tiley);
}
