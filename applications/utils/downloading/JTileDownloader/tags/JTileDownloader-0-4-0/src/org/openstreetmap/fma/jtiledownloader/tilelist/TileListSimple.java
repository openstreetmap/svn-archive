package org.openstreetmap.fma.jtiledownloader.tilelist;

import java.util.Vector;

import org.openstreetmap.fma.jtiledownloader.Constants;

/**
 * Copyright 2008, Friedrich Maier 
 * 
 * This file is part of JTileDownloader. 
 * (see http://wiki.openstreetmap.org/index.php/JTileDownloader)
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
public class TileListSimple
    implements TileList, Constants
{

    Vector _tileList;

    /**
     * @see org.openstreetmap.fma.jtiledownloader.tilelist.TileList#getFileListToDownload()
     * {@inheritDoc}
     */
    public Vector getFileListToDownload()
    {
        return _tileList;
    }

    public void addTile(String tile)
    {
        if (_tileList == null)
        {
            _tileList = new Vector();
        }

        _tileList.add(tile);
    }

    /**
     * @return
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
