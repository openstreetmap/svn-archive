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
public class DownloadConfigurationUrlSquare
    extends DownloadConfiguration
{

    private String _pasteUrl = "";
    private int _radius = 5;

    private static final String PASTE_URL = "PasteUrl";
    private static final String RADIUS = "Radius";

    public static final String ID = "UrlSquare";

    /**
     * default constructor
     * 
     */
    public DownloadConfigurationUrlSquare()
    {
        super("tilesUrlSquare.xml");
    }

    /**
     * @param propertyFile
     */
    public DownloadConfigurationUrlSquare(String propertyFile)
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
        setTemplateProperty(prop, PASTE_URL, _pasteUrl);
        setTemplateProperty(prop, RADIUS, "" + _radius);
        setTemplateProperty(prop, TYPE, ID);
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

        _pasteUrl = prop.getProperty(PASTE_URL, "");
        _radius = Integer.parseInt(prop.getProperty(RADIUS, "5"));

        return prop;

    }

    /**
     * Getter for pasteUrl
     * @return the pasteUrl
     */
    public final String getPasteUrl()
    {
        return _pasteUrl;
    }

    /**
     * Setter for pasteUrl
     * @param pasteUrl the pasteUrl to set
     */
    public final void setPasteUrl(String pasteUrl)
    {
        _pasteUrl = pasteUrl;
    }

    /**
     * Getter for radius
     * @return the radius
     */
    public final int getRadius()
    {
        return _radius;
    }

    /**
     * Setter for radius
     * @param radius the radius to set
     */
    public final void setRadius(int radius)
    {
        _radius = radius;
    }

}
