/*
 * Copyright 2009, Sven Strickroth <email@cs-ware.de>
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

package org.openstreetmap.fma.jtiledownloader.datatypes;

import java.io.File;

/**
 * Generic TileProvider
 */
public class GenericTileProvider
    implements TileProviderIf
{
    protected String name;
    protected String url;

    protected GenericTileProvider()
    {}

    /**
     * @param url
     */
    public GenericTileProvider(String url)
    {
        if (!url.endsWith("/"))
        {
            url = url + "/";
        }
        this.name = url;
        this.url = url;
    }

    /**
     * @param name
     * @param url
     */
    public GenericTileProvider(String name, String url)
    {
        this.name = name;
        this.url = url;
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.datatypes.TileProviderIf#getMaxZoom()
     */
    public int getMaxZoom()
    {
        return 18;
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.datatypes.TileProviderIf#getMinZoom()
     */
    public int getMinZoom()
    {
        return 0;
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.datatypes.TileProviderIf#getName()
     */
    public String getName()
    {
        return name;
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.datatypes.TileProviderIf#getTileType()
     */
    public String getTileType()
    {
        return "png";
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.datatypes.TileProviderIf#getTileUrl(Tile)
     */
    public String getTileUrl(Tile tile)
    {
        return getTileServerUrl() + tile.getZ() + "/" + tile.getX() + "/" + tile.getY() + "." + getTileType();
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.datatypes.TileProviderIf#getTileServerUrl()
     */
    public String getTileServerUrl()
    {
        return url;
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.datatypes.TileProviderIf#getTileFilename(org.openstreetmap.fma.jtiledownloader.datatypes.Tile)
     */
    public String getTileFilename(Tile tile)
    {
        return tile.getZ() + File.separator + tile.getX() + File.separator + tile.getY() + "." + getTileType();
    }
}
