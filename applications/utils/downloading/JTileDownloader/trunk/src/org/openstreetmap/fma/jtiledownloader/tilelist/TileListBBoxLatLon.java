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
public class TileListBBoxLatLon
    implements TileList, Constants
{
    private int _xTopLeft = 0;
    private int _yTopLeft = 0;
    private int _xBottomRight = 0;
    private int _yBottomRight = 0;

    private int _downloadZoomLevel;
    private String _tileServerBaseUrl;
    private double _minLat;
    private double _minLon;
    private double _maxLat;
    private double _maxLon;

    public void calculateTileValuesXY()
    {

        log("minLat=" + _minLat);
        log("minLon=" + _minLon);
        log("maxLat=" + _maxLat);
        log("maxLon=" + _maxLon);

        _xTopLeft = calculateTileX(_minLat, _downloadZoomLevel);
        _yTopLeft = calculateTileY(_minLon, _downloadZoomLevel);
        _xBottomRight = calculateTileX(_maxLat, _downloadZoomLevel);
        _yBottomRight = calculateTileY(_maxLon, _downloadZoomLevel);

        log("_xTopLeft=" + _xTopLeft);
        log("_yTopLeft=" + _yTopLeft);
        log("_xBottomRight=" + _xBottomRight);
        log("_yBottomRight=" + _yBottomRight);
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.tilelist.TileList#getFileListToDownload()
     * {@inheritDoc}
     */
    public Vector getFileListToDownload()
    {
        Vector tilesToDownload = new Vector();

        long xStart = getMin(_xTopLeft, _xBottomRight);
        long xEnd = getMax(_xTopLeft, _xBottomRight);

        long yStart = getMin(_yTopLeft, _yBottomRight);
        long yEnd = getMax(_yTopLeft, _yBottomRight);

        for (long downloadTileXIndex = xStart; downloadTileXIndex <= xEnd; downloadTileXIndex++)
        {
            for (long downloadTileYIndex = yStart; downloadTileYIndex <= yEnd; downloadTileYIndex++)
            {
                String urlPathToFile = getTileServerBaseUrl() + _downloadZoomLevel + "/" + downloadTileXIndex + "/" + downloadTileYIndex + ".png";

                log("add " + urlPathToFile + " to download list.");
                tilesToDownload.addElement(urlPathToFile);
            }
        }
        log("finished");

        return tilesToDownload;

    }

    /**
     * @param topLeft
     * @param bottomRight
     * @return
     */
    private long getMax(long topLeft, long bottomRight)
    {
        if (topLeft > bottomRight)
        {
            return topLeft;
        }
        return bottomRight;
    }

    /**
     * @param topLeft
     * @param bottomRight
     * @return
     */
    private long getMin(long topLeft, long bottomRight)
    {
        if (topLeft > bottomRight)
        {
            return bottomRight;
        }
        return topLeft;
    }

    /**
     * @param lat
     * @param zoomLevel
     * @return
     */
    private static int calculateTileY(double lat, int zoomLevel)
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
    private static int calculateTileX(double lon, int zoomLevel)
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
     * method to write to System.out
     * 
     * @param msg message to log
     */
    private static void log(String msg)
    {
        System.out.println(msg);
    }

    /**
     * Getter for downloadZoomLevel
     * @return the downloadZoomLevel
     */
    protected final int getDownloadZoomLevel()
    {
        return _downloadZoomLevel;
    }

    /**
     * Setter for downloadZoomLevel
     * @param downloadZoomLevel the downloadZoomLevel to set
     */
    public final void setDownloadZoomLevel(int downloadZoomLevel)
    {
        _downloadZoomLevel = downloadZoomLevel;
    }

    /**
     * Getter for tileServerBaseUrl
     * @return the tileServerBaseUrl
     */
    protected final String getTileServerBaseUrl()
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
     * Getter for xTopLeft
     * @return the xTopLeft
     */
    public final long getXTopLeft()
    {
        return _xTopLeft;
    }

    /**
     * Getter for yTopLeft
     * @return the yTopLeft
     */
    public final long getYTopLeft()
    {
        return _yTopLeft;
    }

    /**
     * Getter for xBottomRight
     * @return the xBottomRight
     */
    public final long getXBottomRight()
    {
        return _xBottomRight;
    }

    /**
     * Getter for yBottomRight
     * @return the yBottomRight
     */
    public final long getYBottomRight()
    {
        return _yBottomRight;
    }

    /**
     * Getter for minLat
     * @return the minLat
     */
    public final double getMinLat()
    {
        return _minLat;
    }

    /**
     * Setter for minLat
     * @param minLat the minLat to set
     */
    public final void setMinLat(double minLat)
    {
        _minLat = minLat;
    }

    /**
     * Getter for minLon
     * @return the minLon
     */
    public final double getMinLon()
    {
        return _minLon;
    }

    /**
     * Setter for minLon
     * @param minLon the minLon to set
     */
    public final void setMinLon(double minLon)
    {
        _minLon = minLon;
    }

    /**
     * Getter for maxLat
     * @return the maxLat
     */
    public final double getMaxLat()
    {
        return _maxLat;
    }

    /**
     * Setter for maxLat
     * @param maxLat the maxLat to set
     */
    public final void setMaxLat(double maxLat)
    {
        _maxLat = maxLat;
    }

    /**
     * Getter for maxLon
     * @return the maxLon
     */
    public final double getMaxLon()
    {
        return _maxLon;
    }

    /**
     * Setter for maxLon
     * @param maxLon the maxLon to set
     */
    public final void setMaxLon(double maxLon)
    {
        _maxLon = maxLon;
    }

}
