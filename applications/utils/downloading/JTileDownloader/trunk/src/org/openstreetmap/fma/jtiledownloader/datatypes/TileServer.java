package org.openstreetmap.fma.jtiledownloader.datatypes;

/**
 * developed by fma (http://wiki.openstreetmap.org/index.php/User:Fma)
 * license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
public class TileServer
{
    private String _tileServerUrl;
    private String _tileServerName;

    /**
     * 
     */
    public TileServer()
    {
        super();
        _tileServerUrl = "";
        _tileServerName = "";
    }

    /**
     * @param tileServerUrl
     * @param tileServerName
     */
    public TileServer(String tileServerName, String tileServerUrl)
    {
        super();
        _tileServerUrl = tileServerUrl;
        _tileServerName = tileServerName;
    }

    /**
     * Getter for tileServerUrl
     * @return the tileServerUrl
     */
    public final String getTileServerUrl()
    {
        return _tileServerUrl;
    }

    /**
     * Setter for tileServerUrl
     * @param tileServerUrl the tileServerUrl to set
     */
    public final void setTileServerUrl(String tileServerUrl)
    {
        _tileServerUrl = tileServerUrl;
    }

    /**
     * Getter for tileServerName
     * @return the tileServerName
     */
    public final String getTileServerName()
    {
        return _tileServerName;
    }

    /**
     * Setter for tileServerName
     * @param tileServerName the tileServerName to set
     */
    public final void setTileServerName(String tileServerName)
    {
        _tileServerName = tileServerName;
    }

}
