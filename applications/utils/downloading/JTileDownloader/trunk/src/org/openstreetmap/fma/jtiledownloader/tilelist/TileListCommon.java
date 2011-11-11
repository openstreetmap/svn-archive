/*
 * Copyright 2008, Friedrich Maier
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

package org.openstreetmap.fma.jtiledownloader.tilelist;

import org.openstreetmap.fma.jtiledownloader.Constants;

/**
 * 
 */
public abstract class TileListCommon
    implements TileList
{
    private int[] _downloadZoomLevels;

    /**
     * @param lat
     * @param zoomLevel
     * @return tileY
     */
    protected final int calculateTileY(double lat, int zoomLevel)
    {
        if (lat < Constants.MIN_LAT)
        {
            lat = Constants.MIN_LAT;
        }
        if (lat > Constants.MAX_LAT)
        {
            lat = Constants.MAX_LAT;
        }
        int y = (int) Math.floor((1 - Math.log(Math.tan(lat * Math.PI / 180) + 1 / Math.cos(lat * Math.PI / 180)) / Math.PI) / 2 * (1 << zoomLevel));
        return y;
    }

    /**
     * @param lon
     * @param zoomLevel
     * @return tileX
     */
    protected final int calculateTileX(double lon, int zoomLevel)
    {
        if (lon < Constants.MIN_LON)
        {
            lon = Constants.MIN_LON;
        }
        if (lon > Constants.MAX_LON)
        {
            lon = Constants.MAX_LON;
        }

        int x = (int) Math.floor((lon + 180) / 360 * (1 << zoomLevel));
        return x;
    }

    /**
     * Getter for downloadZoomLevel
     * @return the downloadZoomLevel
     */
    public final int[] getDownloadZoomLevels()
    {
        return _downloadZoomLevels.clone();
    }

    /**
     * Setter for downloadZoomLevel
     * @param downloadZoomLevel the downloadZoomLevel to set
     */
    public final void setDownloadZoomLevels(int[] downloadZoomLevel)
    {
        _downloadZoomLevels = downloadZoomLevel.clone();
    }

}
