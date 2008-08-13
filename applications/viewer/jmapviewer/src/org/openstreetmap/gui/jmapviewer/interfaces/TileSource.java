package org.openstreetmap.gui.jmapviewer.interfaces;

import org.openstreetmap.gui.jmapviewer.JMapViewer;

//License: GPL. Copyright 2008 by Jan Peter Stotz

/**
 * 
 * @author Jan Peter Stotz
 */
public interface TileSource {

	/**
	 * Specifies the different mechanisms for detecting updated tiles
	 * respectively only download newer tiles than those stored locally.
	 * 
	 * <ul>
	 * <li>{@link #IfNoneMatch} Server provides ETag header entry for all tiles
	 * and <b>supports</b> conditional download via <code>If-None-Match</code>
	 * header entry.</li>
	 * <li>{@link #ETag} Server provides ETag header entry for all tiles but
	 * <b>does not support</b> conditional download via
	 * <code>If-None-Match</code> header entry.</li>
	 * <li>{@link #IfModifiedSince} Server provides Last-Modified header entry
	 * for all tiles and <b>supports</b> conditional download via
	 * <code>If-Modified-Since</code> header entry.</li>
	 * <li>{@link #LastModified} Server provides Last-Modified header entry for
	 * all tiles but <b>does not support</b> conditional download via
	 * <code>If-Modified-Since</code> header entry.</li>
	 * </ul>
	 * 
	 */
	public enum TileUpdateDetection {
		IfNoneMatch, ETag, IfModifiedSince, LastModified
	};

	/**
	 * Specifies the maximum zoom value. The number of zoom levels is [0..
	 * {@link #getMaxZoom()}].
	 * 
	 * @return maximum zoom value that has to be smaller or equal to
	 *         {@link JMapViewer#MAX_ZOOM}
	 */
	public int getMaxZoom();

	/**
	 * A tile layer name has to be unique and has to consist only of characters
	 * valid for filenames.
	 * 
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
