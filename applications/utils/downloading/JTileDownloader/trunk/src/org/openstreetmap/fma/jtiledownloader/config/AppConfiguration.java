/*
 * Copyright 2008, Friedrich Maier
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

package org.openstreetmap.fma.jtiledownloader.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class AppConfiguration
    implements DownloadConfigurationSaverIf
{
    private static final String APP_CONFIG_PROPERTIES = "appConfig.xml";

    private static AppConfiguration instance = new AppConfiguration();

    private Properties prop = new Properties();

    private boolean _useProxyServer = false;
    private String _proxyServer = "";
    private String _proxyServerPort = "";
    private boolean _proxyServerRequireesAuthentitication = false;
    private String _proxyServerUser = "";
    private String _proxyServerPassword = "";

    private boolean _showTilePreview = true;

    private String _tileServer = "";
    private String _lastZoom = "12";
    private String _outputFolder = "tiles";

    private boolean _overwriteExistingFiles = true;

    private int _minimumAgeInDays = 7;

    private boolean _waitAfterNrTiles = true;
    private int _waitSeconds = 5;
    private int _waitNrTiles = 50;

    private int _inputPanelIndex = 0;

    private static final String USE_PROXY_SERVER = "UseProxyServer";
    private static final String PROXY_SERVER = "ProxyServer";
    private static final String PROXY_SERVER_PORT = "ProxyServerPort";
    private static final String USE_PROXY_SERVER_AUTH = "UseProxyServerAuth";
    private static final String PROXY_SERVER_USER = "ProxyServerUser";
    private static final String PROXY_SERVER_PASSWORD = "ProxyServerPassword";

    private static final String SHOW_TILE_PREVIEW = "ShowTilePreview";

    private static final String TILE_SERVER = "TileServer";
    private static final String LAST_ZOOM = "LastZoom";
    private static final String LAST_OUTPUTFOLDER = "LastOutoutFolder";

    private static final String OVERWRITE_EXISTING_FILES = "OverwriteExistingFiles";

    private static final String MINIMUM_AGE_IN_DAYS = "MinimumAgeInDays";

    private static final String WAIT_AFTER_NR_TILES = "WaitAfterNrTiles";
    private static final String WAIT_SECONDS = "WaitSeconds";
    private static final String WAIT_NR_TILES = "WaitNrTiles";

    private static final String INPUT_PANEL_INDEX = "InputPanelIndex";

    private AppConfiguration()
    {
        loadFromFile();
    }

    public static AppConfiguration getInstance()
    {
        return instance;
    }

    public void saveToFile()
    {
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

    private void loadFromFile()
    {
        try
        {
            prop.loadFromXML(new FileInputStream(APP_CONFIG_PROPERTIES));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        _useProxyServer = Boolean.valueOf(prop.getProperty(USE_PROXY_SERVER, String.valueOf(_useProxyServer))).booleanValue();
        _proxyServer = prop.getProperty(PROXY_SERVER, _proxyServer);
        _proxyServerPort = prop.getProperty(PROXY_SERVER_PORT, _proxyServerPort);
        _proxyServerRequireesAuthentitication = Boolean.valueOf(prop.getProperty(USE_PROXY_SERVER_AUTH, String.valueOf(_proxyServerRequireesAuthentitication))).booleanValue();
        _proxyServerUser = prop.getProperty(PROXY_SERVER_USER, _proxyServerUser);
        _proxyServerPassword = prop.getProperty(PROXY_SERVER_PASSWORD, _proxyServerPassword);

        setShowTilePreview(Boolean.valueOf(prop.getProperty(SHOW_TILE_PREVIEW, String.valueOf(isShowTilePreview()))).booleanValue());

        setTileServer(prop.getProperty(TILE_SERVER, _tileServer));
        setLastZoom(prop.getProperty(LAST_ZOOM, _lastZoom));
        setOutputFolder(prop.getProperty(LAST_OUTPUTFOLDER, _outputFolder));

        setOverwriteExistingFiles(Boolean.valueOf(prop.getProperty(OVERWRITE_EXISTING_FILES, String.valueOf(isOverwriteExistingFiles()))).booleanValue());

        setMinimumAgeInDays(Integer.parseInt(prop.getProperty(MINIMUM_AGE_IN_DAYS, String.valueOf(getMinimumAgeInDays()))));

        setWaitAfterNrTiles(Boolean.valueOf(prop.getProperty(WAIT_AFTER_NR_TILES, String.valueOf(isWaitingAfterNrOfTiles()))).booleanValue());
        setWaitSeconds(Integer.parseInt(prop.getProperty(WAIT_SECONDS, String.valueOf(getWaitSeconds()))));
        setWaitNrTiles(Integer.parseInt(prop.getProperty(WAIT_NR_TILES, String.valueOf(getWaitNrTiles()))));

        setInputPanelIndex(Integer.parseInt(prop.getProperty(INPUT_PANEL_INDEX, String.valueOf(getInputPanelIndex()))));
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
        setProperty(prop, USE_PROXY_SERVER, "" + _useProxyServer);
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
        setProperty(prop, PROXY_SERVER, "" + _proxyServer);
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
        setProperty(prop, PROXY_SERVER_PORT, "" + _proxyServerPort);
    }

    /**
     * Getter for isProxyServerRequiresAuthentitication
     * @return the isProxyServerRequiresAuthentitication
     */
    public final boolean isProxyServerRequiresAuthentitication()
    {
        return _proxyServerRequireesAuthentitication;
    }

    /**
     * Setter for setProxyServerRequiresAuthentitication
     * @param setProxyServerRequiresAuthentitication the useProxyServerAuthentitication to set
     */
    public final void setProxyServerRequiresAuthentitication(boolean useProxyServerAuthentitication)
    {
        _proxyServerRequireesAuthentitication = useProxyServerAuthentitication;
        setProperty(prop, USE_PROXY_SERVER_AUTH, "" + _proxyServerRequireesAuthentitication);
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
        setProperty(prop, PROXY_SERVER_USER, "" + _proxyServerUser);
    }

    /**
     * Getter for proxyServerPassword
     * @return the proxyServerPassword
     */
    public final String getProxyServerPassword()
    {
        return _proxyServerPassword;
    }

    /**
     * Setter for proxyServerPassword
     * @param proxyServerPassword the proxyServerPassword to set
     */
    public final void setProxyServerPassword(String proxyServerPassword)
    {
        _proxyServerPassword = proxyServerPassword;
        setProperty(prop, PROXY_SERVER_PASSWORD, "" + _proxyServerPassword);
    }

    /**
     * Setter for showTilePreview
     * @param showTilePreview the showTilePreview to set
     */
    public void setShowTilePreview(boolean showTilePreview)
    {
        _showTilePreview = showTilePreview;
        setProperty(prop, SHOW_TILE_PREVIEW, "" + _showTilePreview);
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
     * Setter for waitAfterNrTiles
     * @param waitAfterNrTiles the waitAfterNrTiles to set
     */
    public void setWaitAfterNrTiles(boolean waitAfterNrTiles)
    {
        _waitAfterNrTiles = waitAfterNrTiles;
        setProperty(prop, WAIT_AFTER_NR_TILES, "" + _waitAfterNrTiles);
    }

    /**
     * Getter for isWaitingAfterNrOfTiles
     * @return the isWaitingAfterNrOfTiles
     */
    public boolean isWaitingAfterNrOfTiles()
    {
        return _waitAfterNrTiles;
    }

    /**
     * Setter for waitSeconds
     * @param waitSeconds the waitSeconds to set
     */
    public void setWaitSeconds(int waitSeconds)
    {
        if (waitSeconds > 0)
        {
            _waitSeconds = waitSeconds;
        }
        setProperty(prop, WAIT_SECONDS, "" + _waitSeconds);
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
        if (waitNrTiles > 0)
        {
            _waitNrTiles = waitNrTiles;
        }
        setProperty(prop, WAIT_AFTER_NR_TILES, "" + _waitNrTiles);
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
        setProperty(prop, INPUT_PANEL_INDEX, "" + _inputPanelIndex);
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
        setProperty(prop, OVERWRITE_EXISTING_FILES, "" + _overwriteExistingFiles);
    }

    /**
     * Getter for minimumAgeInDays
     @return the minimumAgeInDays
     */
    public int getMinimumAgeInDays()
    {
        return _minimumAgeInDays;
    }

    /**
     * Setter for minimumAgeInDays
     * @param minimumAgeInDays the minimumAgeInDays to set
     */
    public void setMinimumAgeInDays(int minimumAgeInDays)
    {
        if (minimumAgeInDays >= 0)
        {
            _minimumAgeInDays = minimumAgeInDays;
        }
        setProperty(prop, MINIMUM_AGE_IN_DAYS, "" + _minimumAgeInDays);
    }

    public void loadDownloadConfig(DownloadConfiguration config)
    {
        Properties downloadProps = new Properties();
        for (Object property : prop.keySet())
        {
            String propertyName = (String) property;
            if (propertyName.startsWith(config.getType() + "."))
            {
                downloadProps.setProperty(propertyName.substring(config.getType().length() + 1), prop.getProperty(propertyName));
            }
        }
        config.load(downloadProps);
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.config.DownloadConfigurationSaverIf#saveDownloadConfig(org.openstreetmap.fma.jtiledownloader.config.DownloadConfiguration)
     * {@inheritDoc}
     */
    public void saveDownloadConfig(DownloadConfiguration config)
    {
        Properties downloadProps = new Properties();
        config.save(downloadProps);
        for (Object property : downloadProps.keySet())
        {
            String propertyName = (String) property;
            if (!propertyName.equals(DownloadConfiguration.TYPE))
            {
                prop.setProperty(config.getType() + "." + propertyName, downloadProps.getProperty(propertyName));
            }
        }
    }

    /**
     * Getter for lastZoom
     * @return the lastZoom
     */
    public String getLastZoom()
    {
        return _lastZoom;
    }

    /**
     * Setter for lastZoom
     * @param lastZoom the lastZoom to set
     */
    public void setLastZoom(String lastZoom)
    {
        _lastZoom = lastZoom;
        setProperty(prop, LAST_ZOOM, _lastZoom);
    }

    /**
     * Getter for outputFolder
     * @return the outputFolder
     */
    public String getOutputFolder()
    {
        return _outputFolder;
    }

    /**
     * Setter for outputFolder
     * @param outputFolder the outputFolder to set
     */
    public void setOutputFolder(String outputFolder)
    {
        _outputFolder = outputFolder;
        setProperty(prop, LAST_OUTPUTFOLDER, _outputFolder);
    }

    /**
     * Getter for tileServer
     * @return the tileServer
     */
    public String getTileServer()
    {
        return _tileServer;
    }

    /**
     * Setter for tileServer
     * @param tileServer the tileServer to set
     */
    public void setTileServer(String tileServer)
    {
        _tileServer = tileServer;
        setProperty(prop, TILE_SERVER, _tileServer);
    }
}
