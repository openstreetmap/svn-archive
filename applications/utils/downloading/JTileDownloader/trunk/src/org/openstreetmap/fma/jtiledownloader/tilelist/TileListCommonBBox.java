package org.openstreetmap.fma.jtiledownloader.tilelist;

import java.util.Vector;

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
public class TileListCommonBBox
    extends TileListCommon
{
    private int _xTopLeft = 0;
    private int _yTopLeft = 0;
    private int _xBottomRight = 0;
    private int _yBottomRight = 0;

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
                String urlPathToFile = getTileServerBaseUrl() + getDownloadZoomLevel() + "/" + downloadTileXIndex + "/" + downloadTileYIndex + ".png";

                log("add " + urlPathToFile + " to download list.");
                tilesToDownload.addElement(urlPathToFile);
            }
        }
        log("finished");

        return tilesToDownload;

    }

    /**
     * @param minLat
     * @param minLon
     * @param maxLat
     * @param maxLon
     */
    public void calculateTileValuesXY(double minLat, double minLon, double maxLat, double maxLon)
    {
        setXTopLeft(calculateTileX(minLon, getDownloadZoomLevel()));
        setYTopLeft(calculateTileY(maxLat, getDownloadZoomLevel()));
        setXBottomRight(calculateTileX(maxLon, getDownloadZoomLevel()));
        setYBottomRight(calculateTileY(minLat, getDownloadZoomLevel()));

        log("XTopLeft=" + getXTopLeft());
        log("YTopLeft=" + getYTopLeft());
        log("XBottomRight=" + getXBottomRight());
        log("YBottomRight=" + getYBottomRight());

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
     * Getter for xTopLeft
     * @return the xTopLeft
     */
    public final int getXTopLeft()
    {
        return _xTopLeft;
    }

    /**
     * Setter for topLeft
     * @param topLeft the xTopLeft to set
     */
    public final void setXTopLeft(int topLeft)
    {
        _xTopLeft = topLeft;
    }

    /**
     * Getter for yTopLeft
     * @return the yTopLeft
     */
    public final int getYTopLeft()
    {
        return _yTopLeft;
    }

    /**
     * Setter for topLeft
     * @param topLeft the yTopLeft to set
     */
    public final void setYTopLeft(int topLeft)
    {
        _yTopLeft = topLeft;
    }

    /**
     * Getter for xBottomRight
     * @return the xBottomRight
     */
    public final int getXBottomRight()
    {
        return _xBottomRight;
    }

    /**
     * Setter for bottomRight
     * @param bottomRight the xBottomRight to set
     */
    public final void setXBottomRight(int bottomRight)
    {
        _xBottomRight = bottomRight;
    }

    /**
     * Getter for yBottomRight
     * @return the yBottomRight
     */
    public final int getYBottomRight()
    {
        return _yBottomRight;
    }

    /**
     * Setter for bottomRight
     * @param bottomRight the yBottomRight to set
     */
    public final void setYBottomRight(int bottomRight)
    {
        _yBottomRight = bottomRight;
    }

}
