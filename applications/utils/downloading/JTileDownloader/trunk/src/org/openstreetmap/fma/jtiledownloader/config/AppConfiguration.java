package org.openstreetmap.fma.jtiledownloader.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * developed by fma (http://wiki.openstreetmap.org/index.php/User:Fma)
 * license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
public class AppConfiguration
{
    private static final String APP_CONFIG_PROPERTIES = "appConfig.xml";

    private boolean _useProxyServer = false;
    private String _proxyServer = "";
    private String _proxyServerPort = "";
    private boolean _useProxyServerAuth = false;
    private String _proxyServerUser = "";

    private boolean _showTilePreview = true;
    private boolean _autoCloseTilePreview = true;

    private boolean _waitAfterNrTiles = true;
    private int _waitSeconds = 5;
    private int _waitNrTiles = 50;

    private static final String USE_PROXY_SERVER = "UseProxyServer";
    private static final String PROXY_SERVER = "ProxyServer";
    private static final String PROXY_SERVER_PORT = "ProxyServerPort";
    private static final String USE_PROXY_SERVER_AUTH = "UseProxyServerAuth";
    private static final String PROXY_SERVER_USER = "ProxyServerUser";

    private static final String SHOW_TILE_PREVIEW = "ShowTilePreview";
    private static final String AUTO_CLOSE_TILE_PREVIEW = "AutoCloseTilePreview";

    private static final String WAIT_AFTER_NR_TILES = "WaitAfterNrTiles";
    private static final String WAIT_SECONDS = "WaitSeconds";
    private static final String WAIT_NR_TILES = "WaitNrTiles";

    public void saveToFile()
    {
        Properties prop = new Properties();

        setProperty(prop, USE_PROXY_SERVER, "" + _useProxyServer);
        setProperty(prop, PROXY_SERVER, "" + _proxyServer);
        setProperty(prop, PROXY_SERVER_PORT, "" + _proxyServerPort);
        setProperty(prop, USE_PROXY_SERVER_AUTH, "" + _useProxyServerAuth);
        setProperty(prop, PROXY_SERVER_USER, "" + _proxyServerUser);
        setProperty(prop, SHOW_TILE_PREVIEW, "" + isShowTilePreview());
        setProperty(prop, AUTO_CLOSE_TILE_PREVIEW, "" + isAutoCloseTilePreview());

        setProperty(prop, WAIT_AFTER_NR_TILES, "" + getWaitAfterNrTiles());
        setProperty(prop, WAIT_SECONDS, "" + getWaitSeconds());
        setProperty(prop, WAIT_NR_TILES, "" + getWaitNrTiles());

        try
        {
            prop.storeToXML(new FileOutputStream(APP_CONFIG_PROPERTIES), null);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void setProperty(Properties prop, String key, String value)
    {
        System.out.println("setting property " + key + " to value " + value);
        prop.setProperty(key, value);
    }

    public void loadFromFile()
    {
        Properties prop = new Properties();
        try
        {
            prop.loadFromXML(new FileInputStream(APP_CONFIG_PROPERTIES));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        _useProxyServer = Boolean.valueOf(prop.getProperty(USE_PROXY_SERVER, "false")).booleanValue();
        _proxyServer = prop.getProperty(PROXY_SERVER, "");
        _proxyServerPort = prop.getProperty(PROXY_SERVER_PORT, "");
        _useProxyServerAuth = Boolean.valueOf(prop.getProperty(USE_PROXY_SERVER_AUTH, "false")).booleanValue();
        _proxyServerUser = prop.getProperty(PROXY_SERVER_USER, "");
        setShowTilePreview(Boolean.valueOf(prop.getProperty(SHOW_TILE_PREVIEW, "true")).booleanValue());
        setAutoCloseTilePreview(Boolean.valueOf(prop.getProperty(AUTO_CLOSE_TILE_PREVIEW, "true")).booleanValue());

        setWaitAfterNrTiles(Boolean.valueOf(prop.getProperty(WAIT_AFTER_NR_TILES, "true")).booleanValue());
        setWaitSeconds(Integer.parseInt(prop.getProperty(WAIT_SECONDS, "10")));
        setWaitNrTiles(Integer.parseInt(prop.getProperty(WAIT_NR_TILES, "50")));

    }

    /**
     * Getter for useProxyServer
     * @return the useProxyServer
     */
    public final boolean getUseProxyServer()
    {
        return _useProxyServer;
    }

    /**
     * Setter for useProxyServer
     * @param useProxyServer the useProxyServer to set
     */
    public final void setUseProxyServer(boolean useProxyServer)
    {
        _useProxyServer = useProxyServer;
    }

    /**
     * Getter for proxyServer
     * @return the proxyServer
     */
    public final String getProxyServer()
    {
        return _proxyServer;
    }

    /**
     * Setter for proxyServer
     * @param proxyServer the proxyServer to set
     */
    public final void setProxyServer(String proxyServer)
    {
        _proxyServer = proxyServer;
    }

    /**
     * Getter for proxyServerPort
     * @return the proxyServerPort
     */
    public final String getProxyServerPort()
    {
        return _proxyServerPort;
    }

    /**
     * Setter for proxyServerPort
     * @param proxyServerPort the proxyServerPort to set
     */
    public final void setProxyServerPort(String proxyServerPort)
    {
        _proxyServerPort = proxyServerPort;
    }

    /**
     * Getter for useProxyServerAuth
     * @return the useProxyServerAuth
     */
    public final boolean getUseProxyServerAuth()
    {
        return _useProxyServerAuth;
    }

    /**
     * Setter for useProxyServerAuth
     * @param useProxyServerAuth the useProxyServerAuth to set
     */
    public final void setUseProxyServerAuth(boolean useProxyServerAuth)
    {
        _useProxyServerAuth = useProxyServerAuth;
    }

    /**
     * Getter for proxyServerUser
     * @return the proxyServerUser
     */
    public final String getProxyServerUser()
    {
        return _proxyServerUser;
    }

    /**
     * Setter for proxyServerUser
     * @param proxyServerUser the proxyServerUser to set
     */
    public final void setProxyServerUser(String proxyServerUser)
    {
        _proxyServerUser = proxyServerUser;
    }

    /**
     * Setter for showTilePreview
     * @param showTilePreview the showTilePreview to set
     */
    public void setShowTilePreview(boolean showTilePreview)
    {
        _showTilePreview = showTilePreview;
    }

    /**
     * Getter for showTilePreview
     * @return the showTilePreview
     */
    public boolean isShowTilePreview()
    {
        return _showTilePreview;
    }

    /**
     * Setter for autoCloseTilePreview
     * @param autoCloseTilePreview the autoCloseTilePreview to set
     */
    public void setAutoCloseTilePreview(boolean autoCloseTilePreview)
    {
        _autoCloseTilePreview = autoCloseTilePreview;
    }

    /**
     * Getter for autoCloseTilePreview
     * @return the autoCloseTilePreview
     */
    public boolean isAutoCloseTilePreview()
    {
        return _autoCloseTilePreview;
    }

    /**
     * Setter for waitAfterNrTiles
     * @param waitAfterNrTiles the waitAfterNrTiles to set
     */
    public void setWaitAfterNrTiles(boolean waitAfterNrTiles)
    {
        _waitAfterNrTiles = waitAfterNrTiles;
    }

    /**
     * Getter for waitAfterNrTiles
     * @return the waitAfterNrTiles
     */
    public boolean getWaitAfterNrTiles()
    {
        return _waitAfterNrTiles;
    }

    /**
     * Setter for waitSeconds
     * @param waitSeconds the waitSeconds to set
     */
    public void setWaitSeconds(int waitSeconds)
    {
        _waitSeconds = waitSeconds;
    }

    /**
     * Getter for waitSeconds
     * @return the waitSeconds
     */
    public int getWaitSeconds()
    {
        return _waitSeconds;
    }

    /**
     * Setter for waitNrTiles
     * @param waitNrTiles the waitNrTiles to set
     */
    public void setWaitNrTiles(int waitNrTiles)
    {
        _waitNrTiles = waitNrTiles;
    }

    /**
     * Getter for waitNrTiles
     * @return the waitNrTiles
     */
    public int getWaitNrTiles()
    {
        return _waitNrTiles;
    }

}
