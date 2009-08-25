/*
 * Copyright 2009, Friedrich Maier
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

package org.openstreetmap.fma.jtiledownloader.downloadjob;

import java.util.Properties;

public class DownloadConfigurationGPX
    extends DownloadConfiguration
{

    private String _gpxFile = "";
    private int _corridor = 0;

    private static final String GPX_FILE = "GpxFile";
    private static final String CORRIDOR = "Corridor";

    public static final String ID = "GPX";

    /**
     * default constructor
     * 
     */
    public DownloadConfigurationGPX()
    {
        super("tilesGPX.xml");
    }

    /**
     * @param propertyFile
     */
    public DownloadConfigurationGPX(String propertyFile)
    {
        super(propertyFile);
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.downloadjob.DownloadConfiguration#saveToFile()
     * {@inheritDoc}
     */
    @Override
    public Properties saveToFile()
    {
        Properties prop = super.saveToFile();
        setTemplateProperty(prop, TYPE, ID);
        setTemplateProperty(prop, GPX_FILE, _gpxFile);
        setTemplateProperty(prop, CORRIDOR, "" + _corridor);
        storeToXml(prop);
        return prop;
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.downloadjob.DownloadConfiguration#loadFromFile()
     * {@inheritDoc}
     */
    @Override
    public Properties loadFromFile()
    {
        Properties prop = super.loadFromFile();

        _gpxFile = prop.getProperty(GPX_FILE, "");
        _corridor = Integer.parseInt(prop.getProperty(CORRIDOR, "0"));

        return prop;

    }

    /**
     * Getter for gpxFile
     * @return the gpxFile
     */
    public final String getGpxFile()
    {
        return _gpxFile;
    }

    /**
     * Setter for gpxFile
     * @param gpxFile the gpxFile to set
     */
    public final void setGpxFile(String gpxFile)
    {
        _gpxFile = gpxFile;
    }

    /**
     * Getter for corridor
     * @return the corridor
     */
    public final int getCorridor()
    {
        return _corridor;
    }

    /**
     * Setter for corridor
     * @param corridor the corridor to set
     */
    public final void setCorridor(int corridor)
    {
        _corridor = corridor;
    }

}
