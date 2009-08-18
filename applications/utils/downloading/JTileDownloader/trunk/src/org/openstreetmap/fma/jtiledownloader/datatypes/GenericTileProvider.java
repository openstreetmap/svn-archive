package org.openstreetmap.fma.jtiledownloader.datatypes;

import java.io.File;

/**
 * Copyright 2009, Sven Strickroth <email@cs-ware.de>
 * 
 * This file is part of JTileDownloader.
 *
 *    JTileDownloader is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    JTileDownloader is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy (see file COPYING.txt) of the GNU 
 *    General Public License along with JTileDownloader.  
 *    If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Generic TileProvider
 */
public class GenericTileProvider
    implements TileProviderIf
{
    protected String url;

    protected GenericTileProvider()
    {}

    /**
     * @param url
     */
    public GenericTileProvider(String url)
    {
        this.url = url;
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.datatypes.TileProviderIf#getMaxZoom()
     * {@inheritDoc}
     */
    public int getMaxZoom()
    {
        return 18;
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.datatypes.TileProviderIf#getMinZoom()
     * {@inheritDoc}
     */
    public int getMinZoom()
    {
        return 0;
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.datatypes.TileProviderIf#getName()
     * {@inheritDoc}
     */
    public String getName()
    {
        return url;
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.datatypes.TileProviderIf#getTileType()
     * {@inheritDoc}
     */
    public String getTileType()
    {
        return "png";
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.datatypes.TileProviderIf#getTileUrl(int, int, int)
     * {@inheritDoc}
     */
    public String getTileUrl(Tile tile)
    {
        return getTileServerUrl() + "/" + tile.getZ() + "/" + tile.getX() + "/" + tile.getY() + "." + getTileType();
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.datatypes.TileProviderIf#getTileServerUrl()
     * {@inheritDoc}
     */
    public String getTileServerUrl()
    {
        return url;
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.datatypes.TileProviderIf#getTileFilename(org.openstreetmap.fma.jtiledownloader.datatypes.Tile)
     * {@inheritDoc}
     */
    public String getTileFilename(Tile tile)
    {
        return tile.getZ() + File.separator + tile.getX() + File.separator + tile.getY() + "." + getTileType();
    }
}
