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

package org.openstreetmap.fma.jtiledownloader.config;

import java.util.Properties;

import org.openstreetmap.fma.jtiledownloader.Util;
import org.openstreetmap.fma.jtiledownloader.datatypes.DownloadJob;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileList;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileListCommonBBox;

public class DownloadConfigurationBBoxXY
    extends DownloadConfiguration
{

    private int _minX = 0;
    private int _minY = 0;
    private int _maxX = 0;
    private int _maxY = 0;

    private static final String MIN_X = "MinX";
    private static final String MIN_Y = "MinY";
    private static final String MAX_X = "MaxX";
    private static final String MAX_Y = "MaxY";

    public static final String ID = "BBoxXY";

    @Override
    public void save(Properties prop)
    {
        setTemplateProperty(prop, TYPE, ID);

        setTemplateProperty(prop, MIN_X, String.valueOf(_minX));
        setTemplateProperty(prop, MIN_Y, String.valueOf(_minY));
        setTemplateProperty(prop, MAX_X, String.valueOf(_maxX));
        setTemplateProperty(prop, MAX_Y, String.valueOf(_maxY));
    }

    @Override
    public void load(Properties prop)
    {
        _minX = Integer.parseInt(prop.getProperty(MIN_X, "0"));
        _minY = Integer.parseInt(prop.getProperty(MIN_Y, "0"));
        _maxX = Integer.parseInt(prop.getProperty(MAX_X, "0"));
        _maxY = Integer.parseInt(prop.getProperty(MAX_Y, "0"));
    }

    /**
    * Getter for minX
    * @return the minX
    */
    public final int getMinX()
    {
        return _minX;
    }

    /**
     * Setter for minX
     * @param minX the minX to set
     */
    public final void setMinX(int minX)
    {
        _minX = minX;
    }

    /**
     * Getter for minY
     * @return the minY
     */
    public final int getMinY()
    {
        return _minY;
    }

    /**
     * Setter for minY
     * @param minY the minY to set
     */
    public final void setMinY(int minY)
    {
        _minY = minY;
    }

    /**
     * Getter for maxX
     * @return the maxX
     */
    public final int getMaxX()
    {
        return _maxX;
    }

    /**
     * Setter for maxX
     * @param maxX the maxX to set
     */
    public final void setMaxX(int maxX)
    {
        _maxX = maxX;
    }

    /**
     * Getter for maxY
     * @return the maxY
     */
    public final int getMaxY()
    {
        return _maxY;
    }

    /**
     * Setter for maxY
     * @param maxY the maxY to set
     */
    public final void setMaxY(int maxY)
    {
        _maxY = maxY;
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.config.DownloadConfiguration#getType()
     */
    @Override
    public String getType()
    {
        return ID;
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.config.DownloadConfiguration#getTileList(DownloadJob)
     */
    @Override
    public TileList getTileList(DownloadJob downloadJob)
    {
        TileListCommonBBox tileList = new TileListCommonBBox();

        tileList.setDownloadZoomLevels(Util.getOutputZoomLevelArray(downloadJob.getTileProvider(), downloadJob.getOutputZoomLevels()));

        tileList.initXTopLeft(getMinX());
        tileList.initYTopLeft(getMinY());
        tileList.initXBottomRight(getMaxX());
        tileList.initYBottomRight(getMaxY());

        return tileList;
    }

}
