/*
 * Copyright 2009, Sven Strickroth <email@cs-ware.de>
 * 
 * Based on:
 * TileSource.java from Jan Peter Stotz (JMapViewer)
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

/**
 * TileProvider interface
 */
public interface TileProviderIf
{

    /**
     * Specifies the maximum zoom value. The number of zoom levels is [0..
     * {@link #getMaxZoom()}].
     * 
     * @return maximum zoom value
     */
    public int getMaxZoom();

    /**
     * Specifies the minimum zoom value. This value is usually 0. 
     * Only for maps that cover a certain region up to a limited zoom level 
     * this method should return a value different than 0.  
     * 
     * @return minimum zoom value - usually 0
     */
    public int getMinZoom();

    /**
     * A tile layer name has to be unique and has to consist only of characters
     * valid for filenames.
     * 
     * @return Name of the tile layer
     */
    public String getName();

    /**
     * Returns the tile server url
     * @return the tile server url
     */
    public String getTileServerUrl();

    /**
     * Constructs the tile url.
     * @param tile the tile
     * @return fully qualified url for downloading the specified tile image
     */
    public String getTileUrl(Tile tile);

    /**
     * Constructs the relative tile-image filename
     * @param tile
     * @return the relative path and filename of the image file
     */
    public String getTileFilename(Tile tile);

    /**
     * Specifies the tile image type. For tiles rendered by Mapnik or
     * Osmarenderer this is usually <code>"png"</code>.
     * 
     * @return file extension of the tile image type
     */
    public String getTileType();
}
