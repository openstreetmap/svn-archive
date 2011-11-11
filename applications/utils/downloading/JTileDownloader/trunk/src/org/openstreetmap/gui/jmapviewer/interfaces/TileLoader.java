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
     * A typical {@link #createTileLoaderJob(org.openstreetmap.gui.jmapviewer.interfaces.TileSource, int, int, int) } implementation
     * should create and return a new {@link Runnable} instance that performs the
     * load action.
     * 
     * @param tileLayerSource
     * @param tilex
     * @param tiley
     * @param zoom
     * @return {@link Runnable} implementation that performs the desired load
     *          action.
     */
    public Runnable createTileLoaderJob(TileSource tileLayerSource, int tilex, int tiley, int zoom);
}
