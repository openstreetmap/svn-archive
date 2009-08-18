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

    private String _type = "";

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

        return prop;
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

        return prop;
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
