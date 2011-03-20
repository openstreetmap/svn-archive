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

package org.openstreetmap.fma.jtiledownloader.tilelist;

import java.util.Vector;

import org.openstreetmap.fma.jtiledownloader.datatypes.Tile;

public class TileListSimple
    implements TileList
{

    Vector<Tile> _tileList;

    /**
     * @see org.openstreetmap.fma.jtiledownloader.tilelist.TileList#getTileListToDownload()
     */
    public Vector<Tile> getTileListToDownload()
    {
        return _tileList;
    }

    public void addTile(Tile tile)
    {
        if (_tileList == null)
        {
            _tileList = new Vector<Tile>();
        }

        _tileList.add(tile);
    }

    /**
     * @return the number of tiles
     */
    public int getElementCount()
    {
        if (_tileList == null)
        {
            return 0;
        }
        return _tileList.size();
    }

}
