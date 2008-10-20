package org.openstreetmap.fma.jtiledownloader.template;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * developed by fma (http://wiki.openstreetmap.org/index.php/User:Fma)
 * license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
public abstract class DownloadConfiguration
{
    private String _propertyFileName = "downloadConfig.xml";

    private int _outputZoomLevel = 12;
    private String _tileServer = "";
    private String _outputLocation = "";

    private static final String OUTPUT_ZOOM_LEVEL = "OutputZoomLevel";
    private static final String TILE_SERVER = "TileServer";
    private static final String OUTPUTLOCATION = "OutputLocation";

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

        setTemplateProperty(prop, OUTPUT_ZOOM_LEVEL, "" + _outputZoomLevel);
        setTemplateProperty(prop, TILE_SERVER, "" + _tileServer);
        setTemplateProperty(prop, OUTPUTLOCATION, "" + _outputLocation);

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

        _outputZoomLevel = Integer.parseInt(prop.getProperty(OUTPUT_ZOOM_LEVEL, "15"));
        _tileServer = prop.getProperty(TILE_SERVER, "");
        _outputLocation = prop.getProperty(OUTPUTLOCATION, "tiles");

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
     * Getter for outputZoomLevel
     * @return the outputZoomLevel
     */
    public final int getOutputZoomLevel()
    {
        return _outputZoomLevel;
    }

    /**
     * Setter for outputZoomLevel
     * @param outputZoomLevel the outputZoomLevel to set
     */
    public final void setOutputZoomLevel(int outputZoomLevel)
    {
        _outputZoomLevel = outputZoomLevel;
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

}
