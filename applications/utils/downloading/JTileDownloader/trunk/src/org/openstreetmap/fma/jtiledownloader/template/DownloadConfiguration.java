package org.openstreetmap.fma.jtiledownloader.template;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Properties;

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
public class DownloadConfiguration
{
    private String _propertyFileName = "downloadConfig.xml";

    private int[] _outputZoomLevels = new int[] { 12 };
    private String _tileServer = "";
    private String _outputLocation = "";
    private String _type = "";

    private static final String OUTPUT_ZOOM_LEVEL = "OutputZoomLevel";
    private static final String TILE_SERVER = "TileServer";
    private static final String OUTPUTLOCATION = "OutputLocation";

    public static final String TYPE = "Type";

    /**
     * constructor setting propertyFileName
     * 
     * @param propertyFileName
     */
    public DownloadConfiguration(String propertyFileName)
    {
        super();
        setPropertyFileName(propertyFileName);
    }

    public Properties saveToFile()
    {
        Properties prop = new Properties();

        setTemplateProperty(prop, OUTPUT_ZOOM_LEVEL, getOutputZoomLevelString(_outputZoomLevels));
        setTemplateProperty(prop, TILE_SERVER, "" + _tileServer);
        setTemplateProperty(prop, OUTPUTLOCATION, "" + _outputLocation);

        return prop;
    }

    /**
     * @param outputZoomLevel
     * @return
     */
    private String getOutputZoomLevelString(int[] outputZoomLevel)
    {
        if (outputZoomLevel == null || outputZoomLevel.length == 0)
        {
            return "";
        }

        String zoomLevels = "";
        for (int index = 0; index < outputZoomLevel.length; index++)
        {
            if (index > 0)
            {
                zoomLevels += ",";
            }
            zoomLevels += outputZoomLevel[index];
        }

        return zoomLevels;
    }

    /**
     * @param prop
     */
    protected void storeToXml(Properties prop)
    {
        try
        {
            prop.storeToXML(new FileOutputStream(getPropertyFileName()), null);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public Properties loadFromFile()
    {
        Properties prop = loadFromXml();

        _type = prop.getProperty(TYPE, "");
        _outputZoomLevels = getOutputZoomLevelArray(prop.getProperty(OUTPUT_ZOOM_LEVEL, "12"));
        _tileServer = prop.getProperty(TILE_SERVER, "");
        _outputLocation = prop.getProperty(OUTPUTLOCATION, "tiles");

        return prop;
    }

    /**
     * @param property
     * @return int[]
     */
    private int[] getOutputZoomLevelArray(String zoomLevelsString)
    {
        // HACK here
        LinkedList<Integer> zoomLevels = new LinkedList<Integer>();
        for (String zoomLevel : Arrays.asList(zoomLevelsString.split(",")))
        {
            try
            {
                int selectedZoom = Integer.parseInt(zoomLevel.trim());
                zoomLevels.add(selectedZoom);
            }
            catch (NumberFormatException e)
            {
                System.out.println("could not parse");
            }
        }
        int[] parsedLevels = new int[zoomLevels.size()];
        for (int i = 0; i < zoomLevels.size(); i++)
        {
            parsedLevels[i] = zoomLevels.get(i);
        }
        return parsedLevels;
    }

    /**
     * @return
     */
    private Properties loadFromXml()
    {
        Properties prop = new Properties();
        try
        {
            prop.loadFromXML(new FileInputStream(getPropertyFileName()));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return prop;
    }

    protected void setTemplateProperty(Properties prop, String key, String value)
    {
        System.out.println("setting property " + key + " to value " + value);
        prop.setProperty(key, value);
    }

    /**
     * Getter for outputZoomLevel
     * @return the outputZoomLevel
     */
    public final int[] getOutputZoomLevels()
    {
        return _outputZoomLevels;
    }

    /**
     * Setter for outputZoomLevel
     * @param outputZoomLevel the outputZoomLevel to set
     */
    public final void setOutputZoomLevels(int[] outputZoomLevel)
    {
        _outputZoomLevels = outputZoomLevel;
    }

    /**
     * Getter for tileServer
     * @return the tileServer
     */
    public final String getTileServer()
    {
        return _tileServer;
    }

    /**
     * Setter for tileServer
     * @param tileServer the tileServer to set
     */
    public final void setTileServer(String tileServer)
    {
        _tileServer = tileServer;
    }

    /**
     * Getter for outputLocation
     * @return the outputLocation
     */
    public final String getOutputLocation()
    {
        return _outputLocation;
    }

    /**
     * Setter for outputLocation
     * @param outputLocation the outputLocation to set
     */
    public final void setOutputLocation(String outputLocation)
    {
        _outputLocation = outputLocation;
    }

    /**
     * Setter for propertyFileName
     * @param propertyFileName the propertyFileName to set
     */
    public void setPropertyFileName(String propertyFileName)
    {
        _propertyFileName = propertyFileName;
    }

    /**
     * Getter for propertyFileName
     * @return the propertyFileName
     */
    public String getPropertyFileName()
    {
        return _propertyFileName;
    }

    /**
     * Getter for type
     * @return the type
     */
    public final String getType()
    {
        return _type;
    }

}
