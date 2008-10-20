package org.openstreetmap.fma.jtiledownloader.template;

import java.util.Properties;

/**
 * developed by fma (http://wiki.openstreetmap.org/index.php/User:Fma)
 * license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
public class DownloadConfigurationUrlSquare
    extends DownloadConfiguration
{

    private String _pasteUrl = "";
    private int _radius = 5;
    private String _type = CONF_TYPE;

    private static final String CONF_TYPE = "UrlSquare";

    private static final String PASTE_URL = "PasteUrl";
    private static final String RADIUS = "Radius";
    private static final String TYPE = "Type";

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
        setTemplateProperty(prop, TYPE, "" + _type);
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
        _type = prop.getProperty(TYPE, CONF_TYPE);

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
