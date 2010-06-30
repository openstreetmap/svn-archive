/*
 * Copyright 2010, Sven Strickroth <email@cs-ware.de>
 * 
 * This file is part of JTileDownloader.
 *
 * JTileDownloader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JTileDownloader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy (see file COPYING.txt) of the GNU 
 * General Public License along with JTileDownloader.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.openstreetmap.gui.jmapviewer;

import org.openstreetmap.fma.jtiledownloader.datatypes.TileProviderIf;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

/**
 * 
 */
public class JTileDownloaderTileSourceWrapper
    implements TileSource
{

    private TileProviderIf tileProvider = null;

    public JTileDownloaderTileSourceWrapper(TileProviderIf tileProvider)
    {
        this.tileProvider = tileProvider;
    }

    /**
     * @see org.openstreetmap.gui.jmapviewer.interfaces.TileSource#getMaxZoom()
     * {@inheritDoc}
     */
    public int getMaxZoom()
    {
        return tileProvider.getMaxZoom();
    }

    /**
     * @see org.openstreetmap.gui.jmapviewer.interfaces.TileSource#getMinZoom()
     * {@inheritDoc}
     */
    public int getMinZoom()
    {
        return tileProvider.getMinZoom();
    }

    /**
     * @see org.openstreetmap.gui.jmapviewer.interfaces.TileSource#getName()
     * {@inheritDoc}
     */
    public String getName()
    {
        return tileProvider.getName();
    }

    /**
     * @see org.openstreetmap.gui.jmapviewer.interfaces.TileSource#getTileType()
     * {@inheritDoc}
     */
    public String getTileType()
    {
        return tileProvider.getTileType();
    }

    /**
     * @see org.openstreetmap.gui.jmapviewer.interfaces.TileSource#getTileUpdate()
     * {@inheritDoc}
     */
    public TileUpdate getTileUpdate()
    {
        return TileUpdate.None;
    }

    /**
     * @see org.openstreetmap.gui.jmapviewer.interfaces.TileSource#getTileUrl(int, int, int)
     * {@inheritDoc}
     */
    public String getTileUrl(int zoom, int tilex, int tiley)
    {
        return tileProvider.getTileUrl(new org.openstreetmap.fma.jtiledownloader.datatypes.Tile(tilex, tiley, zoom));
    }

}
