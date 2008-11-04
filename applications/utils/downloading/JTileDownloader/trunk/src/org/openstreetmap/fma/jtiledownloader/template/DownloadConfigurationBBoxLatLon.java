package org.openstreetmap.fma.jtiledownloader.template;

import java.util.Properties;

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
public class DownloadConfigurationBBoxLatLon
    extends DownloadConfiguration
    implements Constants
{

    private double _minLat = 0.0;
    private double _minLon = 0.0;
    private double _maxLat = 0.0;
    private double _maxLon = 0.0;

    private static final String MIN_LAT = "MinLat";
    private static final String MIN_LON = "MinLon";
    private static final String MAX_LAT = "MaxLat";
    private static final String MAX_LON = "MaxLon";

    /**
     * default constructor
     * 
     */
    public DownloadConfigurationBBoxLatLon()
    {
        super("tilesBBoxLatLon.xml");
    }

    /**
     * @param propertyFile
     */
    public DownloadConfigurationBBoxLatLon(String propertyFile)
    {
        super(propertyFile);
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.template.DownloadConfiguration#saveToFile()
     * {@inheritDoc}
     */
    public Properties saveToFile()
    {
        Properties prop = super.saveToFile();
        setTemplateProperty(prop, TYPE, "" + CONFIG_TYPE[TYPE_BOUNDINGBOX_LATLON]);
        setTemplateProperty(prop, MIN_LAT, "" + _minLat);
        setTemplateProperty(prop, MIN_LON, "" + _minLon);
        setTemplateProperty(prop, MAX_LAT, "" + _maxLat);
        setTemplateProperty(prop, MAX_LON, "" + _maxLon);
        storeToXml(prop);
        return prop;
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.template.DownloadConfiguration#loadFromFile()
     * {@inheritDoc}
     */
    public Properties loadFromFile()
    {
        Properties prop = super.loadFromFile();

        _minLat = Double.parseDouble(prop.getProperty(MIN_LAT, "0.0"));
        _minLon = Double.parseDouble(prop.getProperty(MIN_LON, "0.0"));
        _maxLat = Double.parseDouble(prop.getProperty(MAX_LAT, "0.0"));
        _maxLon = Double.parseDouble(prop.getProperty(MAX_LON, "0.0"));

        return prop;

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
