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
import org.openstreetmap.fma.jtiledownloader.tilelist.TileListBBoxLatLon;

public class DownloadConfigurationBBoxLatLon
    extends DownloadConfiguration
{
    private double _minLat = 0.0;
    private double _minLon = 0.0;
    private double _maxLat = 0.0;
    private double _maxLon = 0.0;

    private static final String MIN_LAT = "MinLat";
    private static final String MIN_LON = "MinLon";
    private static final String MAX_LAT = "MaxLat";
    private static final String MAX_LON = "MaxLon";

    public static final String ID = "BBoxLatLon";

    @Override
    public void save(Properties prop)
    {
        setTemplateProperty(prop, TYPE, ID);

        setTemplateProperty(prop, MIN_LAT, String.valueOf(_minLat));
        setTemplateProperty(prop, MIN_LON, String.valueOf(_minLon));
        setTemplateProperty(prop, MAX_LAT, String.valueOf(_maxLat));
        setTemplateProperty(prop, MAX_LON, String.valueOf(_maxLon));
    }

    @Override
    public void load(Properties prop)
    {
        _minLat = Double.parseDouble(prop.getProperty(MIN_LAT, "0.0"));
        _minLon = Double.parseDouble(prop.getProperty(MIN_LON, "0.0"));
        _maxLat = Double.parseDouble(prop.getProperty(MAX_LAT, "0.0"));
        _maxLon = Double.parseDouble(prop.getProperty(MAX_LON, "0.0"));
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
        TileListBBoxLatLon tileList = new TileListBBoxLatLon();

        tileList.setDownloadZoomLevels(Util.getOutputZoomLevelArray(downloadJob.getTileProvider(), downloadJob.getOutputZoomLevels()));

        tileList.setMinLat(getMinLat());
        tileList.setMaxLat(getMaxLat());
        tileList.setMinLon(getMinLon());
        tileList.setMaxLon(getMaxLon());

        tileList.calculateTileValuesXY();

        return tileList;
    }
}
