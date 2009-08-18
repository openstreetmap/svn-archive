package org.openstreetmap.fma.jtiledownloader.views.main.inputpanel;

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

import org.openstreetmap.fma.jtiledownloader.GlobalConfigIf;
import org.openstreetmap.fma.jtiledownloader.template.DownloadConfiguration;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileList;

/**
 * 
 */
public abstract class InputPanel
    extends JPanel
{

    private static final long serialVersionUID = 1L;
    private final GlobalConfigIf _globalConfig;

    /**
     * Returns the name/title for the input panel
     * @return
     */
    public abstract String getInputName();

    /**
     * Returns the default name for the configfile (for DownloadConfig)
     * @return
     */
    public abstract String getConfigFileName();

    public boolean isDownloadOkay() {
        return true;
    }

    /**
     * 
     */
    public InputPanel(GlobalConfigIf globalConfig)
    {
        super();
        _globalConfig = globalConfig;
    }

    /**
     * Getter for downloadZoomLevel
     * @return the downloadZoomLevel
     */
    public final int[] getDownloadZoomLevel()
    {
        return _globalConfig.getOutputZoomLevelArray();
    }

    public abstract void updateAll();

    public abstract int getNumberOfTilesToDownload();

    public abstract TileList getTileList();

    public abstract void saveConfig();

    public abstract void loadConfig();

    public void updateNumberOfTiles()
    {
        int numberOfTiles = 0;
        numberOfTiles = getNumberOfTilesToDownload();
        _globalConfig.setNumberOfTiles(numberOfTiles);
    }

    /**
     * 
     */
    public void setCommonValues(DownloadConfiguration downloadConfig)
    {
     /*   setOutputLocation(downloadConfig.getOutputLocation());
        //getMainView().getMainPanel().getTextOutputFolder().setText(getOutputLocation());
        setDownloadZoomLevel(downloadConfig.getOutputZoomLevels());
        getMainPanel().initializeOutputZoomLevel(getDownloadZoomLevel());
        setTileServerBaseUrl(downloadConfig.getTileServer());
        getMainPanel().initializeTileServer(getTileServerBaseUrl());*/
    }

}
