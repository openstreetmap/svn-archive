/*
 * Copyright 2008, Friedrich Maier
 * 
 * This file is part of JTileDownloader.
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

package org.openstreetmap.fma.jtiledownloader.views.main.inputpanel;

import javax.swing.JPanel;

import org.openstreetmap.fma.jtiledownloader.Util;
import org.openstreetmap.fma.jtiledownloader.config.DownloadConfigurationSaverIf;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileList;
import org.openstreetmap.fma.jtiledownloader.views.main.MainPanel;

/**
 * 
 */
public abstract class InputPanel
    extends JPanel
{

    private static final long serialVersionUID = 1L;
    protected final MainPanel _mainPanel;

    /**
     * Returns the name/title for the input panel
     * @return name of the panel
     */
    public abstract String getInputName();

    public boolean isDownloadOkay()
    {
        return true;
    }

    /**
     * @param mainPanel 
     */
    public InputPanel(MainPanel mainPanel)
    {
        super();
        _mainPanel = mainPanel;
    }

    /**
     * Getter for downloadZoomLevel
     * @return the downloadZoomLevel
     */
    public final int[] getDownloadZoomLevel()
    {
        return Util.getOutputZoomLevelArray(_mainPanel.getSelectedTileProvider(), _mainPanel.getOutputZoomLevelString());
    }

    public abstract void updateAll();

    public abstract int getNumberOfTilesToDownload();

    public abstract TileList getTileList();

    public abstract String getJobType();

    public abstract void loadConfig(DownloadConfigurationSaverIf configurationSave);

    public abstract void saveConfig(DownloadConfigurationSaverIf configurationSave);

    public void updateNumberOfTiles()
    {
        int numberOfTiles = 0;
        numberOfTiles = getNumberOfTilesToDownload();
        _mainPanel.setNumberOfTiles(numberOfTiles);
    }
}
