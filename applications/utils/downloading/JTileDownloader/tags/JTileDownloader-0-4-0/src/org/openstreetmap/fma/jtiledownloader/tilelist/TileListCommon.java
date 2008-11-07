package org.openstreetmap.fma.jtiledownloader.tilelist;

import org.openstreetmap.fma.jtiledownloader.Constants;

/**
 * Copyright 2008, Friedrich Maier 
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
 * 
 */
public abstract class TileListCommon
    implements TileList, Constants
{
    private int[] _downloadZoomLevels;
    private String _tileServerBaseUrl;

    /**
     * @param lat
     * @param zoomLevel
     * @return
     */
    public int calculateTileY(double lat, int zoomLevel)
    {
        if (lat < MIN_LAT)
        {
            lat = MIN_LAT;
        }
        if (lat > MAX_LAT)
        {
            lat = MAX_LAT;
        }
        int y = (int) Math.floor((1 - Math.log(Math.tan(lat * Math.PI / 180) + 1 / Math.cos(lat * Math.PI / 180)) / Math.PI) / 2 * (1 << zoomLevel));
        return y;
    }

    /**
     * @param lon
     * @param zoomLevel
     * @return
     */
    public int calculateTileX(double lon, int zoomLevel)
    {
        if (lon < MIN_LON)
        {
            lon = MIN_LON;
        }
        if (lon > MAX_LON)
        {
            lon = MAX_LON;
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
        return _downloadZoomLevels;
    }

    /**
     * Setter for downloadZoomLevel
     * @param downloadZoomLevel the downloadZoomLevel to set
     */
    public final void setDownloadZoomLevels(int[] downloadZoomLevel)
    {
        _downloadZoomLevels = downloadZoomLevel;
    }

    /**
     * Getter for tileServerBaseUrl
     * @return the tileServerBaseUrl
     */
    public final String getTileServerBaseUrl()
    {
        return _tileServerBaseUrl;
    }

    /**
     * Setter for tileServerBaseUrl
     * @param tileServerBaseUrl the tileServerBaseUrl to set
     */
    public final void setTileServerBaseUrl(String tileServerBaseUrl)
    {
        _tileServerBaseUrl = tileServerBaseUrl;
    }

    /**
     * method to write to System.out
     * 
     * @param msg message to log
     */
    public void log(String msg)
    {
        System.out.println(msg);
    }

}
