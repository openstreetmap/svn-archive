package org.openstreetmap.fma.jtiledownloader.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

    private boolean _overwriteExistingFiles = true;

    private boolean _waitAfterNrTiles = true;
    private int _waitSeconds = 5;
    private int _waitNrTiles = 50;

    private int _inputPanelIndex = 0;

    private static final String USE_PROXY_SERVER = "UseProxyServer";
    private static final String PROXY_SERVER = "ProxyServer";
    private static final String PROXY_SERVER_PORT = "ProxyServerPort";
    private static final String USE_PROXY_SERVER_AUTH = "UseProxyServerAuth";
    private static final String PROXY_SERVER_USER = "ProxyServerUser";

    private static final String SHOW_TILE_PREVIEW = "ShowTilePreview";
    private static final String AUTO_CLOSE_TILE_PREVIEW = "AutoCloseTilePreview";

    private static final String OVERWRITE_EXISTING_FILES = "OverwriteExistingFiles";

    private static final String WAIT_AFTER_NR_TILES = "WaitAfterNrTiles";
    private static final String WAIT_SECONDS = "WaitSeconds";
    private static final String WAIT_NR_TILES = "WaitNrTiles";

    private static final String INPUT_PANEL_INDEX = "InputPanelIndex";

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

        setProperty(prop, OVERWRITE_EXISTING_FILES, "" + isOverwriteExistingFiles());

        setProperty(prop, WAIT_AFTER_NR_TILES, "" + getWaitAfterNrTiles());
        setProperty(prop, WAIT_SECONDS, "" + getWaitSeconds());
        setProperty(prop, WAIT_NR_TILES, "" + getWaitNrTiles());

        setProperty(prop, INPUT_PANEL_INDEX, "" + getInputPanelIndex());

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

        setOverwriteExistingFiles(Boolean.valueOf(prop.getProperty(OVERWRITE_EXISTING_FILES, "true")).booleanValue());

        setWaitAfterNrTiles(Boolean.valueOf(prop.getProperty(WAIT_AFTER_NR_TILES, "true")).booleanValue());
        setWaitSeconds(Integer.parseInt(prop.getProperty(WAIT_SECONDS, "10")));
        setWaitNrTiles(Integer.parseInt(prop.getProperty(WAIT_NR_TILES, "50")));

        setInputPanelIndex(Integer.parseInt(prop.getProperty(INPUT_PANEL_INDEX, "0")));

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

    /**
     * Getter for inputPanelIndex
     * @return the inputPanelIndex
     */
    public final int getInputPanelIndex()
    {
        return _inputPanelIndex;
    }

    /**
     * Setter for inputPanelIndex
     * @param inputPanelIndex the inputPanelIndex to set
     */
    public final void setInputPanelIndex(int inputPanelIndex)
    {
        _inputPanelIndex = inputPanelIndex;
    }

    /**
     * Getter for overwriteExistingFiles
     * @return the overwriteExistingFiles
     */
    public final boolean isOverwriteExistingFiles()
    {
        return _overwriteExistingFiles;
    }

    /**
     * Setter for overwriteExistingFiles
     * @param overwriteExistingFiles the _verwriteExistingFiles to set
     */
    public final void setOverwriteExistingFiles(boolean overwriteExistingFiles)
    {
        _overwriteExistingFiles = overwriteExistingFiles;
    }

}
