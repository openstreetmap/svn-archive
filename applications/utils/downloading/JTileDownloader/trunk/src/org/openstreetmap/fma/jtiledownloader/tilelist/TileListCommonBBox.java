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
    private int[] _xTopLeft = new int[] {0 };
    private int[] _yTopLeft = new int[] {0 };
    private int[] _xBottomRight = new int[] {0 };
    private int[] _yBottomRight = new int[] {0 };

    /**
     * @see org.openstreetmap.fma.jtiledownloader.tilelist.TileList#getFileListToDownload()
     * {@inheritDoc}
     */
    public Vector getFileListToDownload()
    {
        Vector tilesToDownload = new Vector();

        for (int indexZoomLevel = 0; indexZoomLevel < getDownloadZoomLevels().length; indexZoomLevel++)
        {
            long xStart = getMin(_xTopLeft[indexZoomLevel], _xBottomRight[indexZoomLevel]);
            long xEnd = getMax(_xTopLeft[indexZoomLevel], _xBottomRight[indexZoomLevel]);

            long yStart = getMin(_yTopLeft[indexZoomLevel], _yBottomRight[indexZoomLevel]);
            long yEnd = getMax(_yTopLeft[indexZoomLevel], _yBottomRight[indexZoomLevel]);

            for (long downloadTileXIndex = xStart; downloadTileXIndex <= xEnd; downloadTileXIndex++)
            {
                for (long downloadTileYIndex = yStart; downloadTileYIndex <= yEnd; downloadTileYIndex++)
                {
                    String urlPathToFile = getTileServerBaseUrl() + getDownloadZoomLevels()[indexZoomLevel] + "/" + downloadTileXIndex + "/" + downloadTileYIndex + ".png";

                    log("add " + urlPathToFile + " to download list.");
                    tilesToDownload.addElement(urlPathToFile);
                }
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
        int zoomLevelSize = getDownloadZoomLevels().length;

        _xTopLeft = new int[zoomLevelSize];
        _yTopLeft = new int[zoomLevelSize];
        _xBottomRight = new int[zoomLevelSize];
        _yBottomRight = new int[zoomLevelSize];

        for (int indexZoomLevel = 0; indexZoomLevel < zoomLevelSize; indexZoomLevel++)
        {
            setXTopLeft(calculateTileX(minLon, getDownloadZoomLevels()[indexZoomLevel]), indexZoomLevel);
            setYTopLeft(calculateTileY(maxLat, getDownloadZoomLevels()[indexZoomLevel]), indexZoomLevel);
            setXBottomRight(calculateTileX(maxLon, getDownloadZoomLevels()[indexZoomLevel]), indexZoomLevel);
            setYBottomRight(calculateTileY(minLat, getDownloadZoomLevels()[indexZoomLevel]), indexZoomLevel);

            log("XTopLeft=" + getXTopLeft()[indexZoomLevel]);
            log("YTopLeft=" + getYTopLeft()[indexZoomLevel]);
            log("XBottomRight=" + getXBottomRight()[indexZoomLevel]);
            log("YBottomRight=" + getYBottomRight()[indexZoomLevel]);
        }

    }

    /**
     * Setter for topLeft
     * @param value the xTopLeft to set
     */
    public final void initXTopLeft(int value, int[] zoomLevels)
    {
        _xTopLeft = new int[zoomLevels.length];
        for (int index = 0; index < zoomLevels.length; index++)
        {
            _xTopLeft[index] = value;

        }
    }

    /**
     * Setter for topLeft
     * @param value the xTopLeft to set
     */
    public final void initYTopLeft(int value, int[] zoomLevels)
    {
        _yTopLeft = new int[zoomLevels.length];
        for (int index = 0; index < zoomLevels.length; index++)
        {
            _yTopLeft[index] = value;

        }
    }

    /**
     * Setter for BottomRight
     * @param value the xBottomRight to set
     */
    public final void initXBottomRight(int value, int[] zoomLevels)
    {
        _xBottomRight = new int[zoomLevels.length];
        for (int index = 0; index < zoomLevels.length; index++)
        {
            _xBottomRight[index] = value;

        }
    }

    /**
     * Setter for BottomRight
     * @param value the xBottomRight to set
     */
    public final void initYBottomRight(int value, int[] zoomLevels)
    {
        _yBottomRight = new int[zoomLevels.length];
        for (int index = 0; index < zoomLevels.length; index++)
        {
            _yBottomRight[index] = value;

        }
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
    public final int[] getXTopLeft()
    {
        return _xTopLeft;
    }

    /**
     * Setter for topLeft
     * @param topLeft the xTopLeft to set
     */
    public final void setXTopLeft(int topLeft, int index)
    {
        _xTopLeft[index] = topLeft;
    }

    /**
     * Getter for yTopLeft
     * @return the yTopLeft
     */
    public final int[] getYTopLeft()
    {
        return _yTopLeft;
    }

    /**
     * Setter for topLeft
     * @param topLeft the yTopLeft to set
     */
    public final void setYTopLeft(int topLeft, int index)
    {
        _yTopLeft[index] = topLeft;
    }

    /**
     * Getter for xBottomRight
     * @return the xBottomRight
     */
    public final int[] getXBottomRight()
    {
        return _xBottomRight;
    }

    /**
     * Setter for bottomRight
     * @param bottomRight the xBottomRight to set
     */
    public final void setXBottomRight(int bottomRight, int index)
    {
        _xBottomRight[index] = bottomRight;
    }

    /**
     * Getter for yBottomRight
     * @return the yBottomRight
     */
    public final int[] getYBottomRight()
    {
        return _yBottomRight;
    }

    /**
     * Setter for bottomRight
     * @param bottomRight the yBottomRight to set
     */
    public final void setYBottomRight(int bottomRight, int index)
    {
        _yBottomRight[index] = bottomRight;
    }

    /**
     * @return
     */
    public int getTileCount()
    {
        int count = 0;
        for (int indexZoomLevels = 0; indexZoomLevels < getDownloadZoomLevels().length; indexZoomLevels++)
        {
            count += Integer.parseInt("" + (Math.abs(getXBottomRight()[indexZoomLevels] - getXTopLeft()[indexZoomLevels]) + 1) * (Math.abs(getYBottomRight()[indexZoomLevels] - getYTopLeft()[indexZoomLevels]) + 1));
        }

        return count;
    }

}
