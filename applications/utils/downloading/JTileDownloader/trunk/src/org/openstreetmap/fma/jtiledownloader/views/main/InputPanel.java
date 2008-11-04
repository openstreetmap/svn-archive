package org.openstreetmap.fma.jtiledownloader.views.main;

/**
 * Copyright 2008, Friedrich Maier 
 * 
 * This file is part of JTileDownloader.
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

import javax.swing.JPanel;

import org.openstreetmap.fma.jtiledownloader.tilelist.TileList;

/**
 * 
 */
public abstract class InputPanel
    extends JPanel
{

    private static final long serialVersionUID = 1L;
    private int _downloadZoomLevel = 0;
    private String _tileServerBaseUrl = "";
    private String _outputLocation = "tiles";

    /**
     * 
     */
    public InputPanel()
    {
        super();
    }

    /**
     * Getter for downloadZoomLevel
     * @return the downloadZoomLevel
     */
    public final int getDownloadZoomLevel()
    {
        return _downloadZoomLevel;
    }

    /**
     * Setter for downloadZoomLevel
     * @param downloadZoomLevel the downloadZoomLevel to set
     */
    public final void setDownloadZoomLevel(int downloadZoomLevel)
    {
        _downloadZoomLevel = downloadZoomLevel;
    }

    public abstract void updateAll();

    public abstract int getNumberOfTilesToDownload();

    public abstract TileList getTileList();

    public abstract void saveConfig();

    public abstract void loadConfig();

    /**
     * Getter for tileServerBaseUrl
     * @return the tileServerBaseUrl
     */
    public final String getTileServerBaseUrl()
    {
        return _tileServerBaseUrl;
    }

    /**
     * Setter for tileServerBaseUrl
     * @param tileServerBaseUrl the tileServerBaseUrl to set
     */
    public final void setTileServerBaseUrl(String tileServerBaseUrl)
    {
        _tileServerBaseUrl = tileServerBaseUrl;
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

}
