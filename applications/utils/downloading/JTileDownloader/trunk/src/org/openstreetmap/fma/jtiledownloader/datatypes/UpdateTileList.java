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

import java.util.ArrayList;

public class UpdateTileList
{
    private int _zoomLevel;
    private ArrayList<YDirectory> _yDirectory;

    public void addYDirectory(YDirectory yDirectory)
    {
        if (_yDirectory == null)
        {
            _yDirectory = new ArrayList<YDirectory>();
        }
        _yDirectory.add(yDirectory);
    }

    /**
     * Getter for yDirectory
     * @return the yDirectory
     */
    public final ArrayList<YDirectory> getYDirectory()
    {
        return _yDirectory;
    }

    public int getFileCount()
    {
        if (_yDirectory == null)
        {
            return 0;
        }

        int count = 0;
        for (int index = 0; index < _yDirectory.size(); index++)
        {
            YDirectory yDir = _yDirectory.get(index);
            if (yDir.getTiles() != null)
            {
                count += yDir.getTiles().length;
            }
        }

        return count;
    }

    /**
     * Setter for zoomLevel
     * @param zoomLevel the zoomLevel to set
     */
    public void setZoomLevel(int zoomLevel)
    {
        _zoomLevel = zoomLevel;
    }

    /**
     * Getter for zoomLevel
     * @return the zoomLevel
     */
    public int getZoomLevel()
    {
        return _zoomLevel;
    }

}
