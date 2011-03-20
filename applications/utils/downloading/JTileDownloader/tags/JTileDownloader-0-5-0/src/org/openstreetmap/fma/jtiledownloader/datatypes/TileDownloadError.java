/*
 * Copyright 2008, Friedrich Maier
 * 
 * This file is part of JTileDownloader.
 * (see http://wiki.openstreetmap.org/index.php/JTileDownloader)
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

public class TileDownloadError
{
    private Tile _tile;
    private TileDownloadResult result = new TileDownloadResult();

    /**
     * Setter for tile
     * @param tile the tile to set
     */
    public void setTile(Tile tile)
    {
        _tile = tile;
    }

    /**
     * Getter for tile
     * @return the tile
     */
    public Tile getTile()
    {
        return _tile;
    }

    /**
     * Setter for result
     * @param result the result to set
     */
    public void setResult(TileDownloadResult result)
    {
        this.result = result;
    }

    /**
     * Getter for result
     * @return the result
     */
    public TileDownloadResult getResult()
    {
        return result;
    }

}
