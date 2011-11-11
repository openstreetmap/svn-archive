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

package org.openstreetmap.fma.jtiledownloader.views.main;

import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openstreetmap.fma.jtiledownloader.Constants;
import org.openstreetmap.fma.jtiledownloader.config.AppConfiguration;

public class JTileDownloaderMainView
    extends JFrame
{
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(JTileDownloaderMainView.class.getName());

    private MainPanel _mainPanel;
    private OptionsPanel _optionsPanel;
    private NetworkPanel _networkPanel;

    private UpdateTilesPanel _updateTilesPanel;

    public JTileDownloaderMainView()
    {
        super();

        generateView();

    }

    /**
     * 
     */
    private void generateView()
    {
        addWindowListener(new MainViewWindowListener());
        setResizable(false);

        setTitle("JTileDownloader" + " Version: " + Constants.VERSION);

        _mainPanel = new MainPanel(getMainView(), AppConfiguration.getInstance().getInputPanelIndex());

        _updateTilesPanel = new UpdateTilesPanel(_mainPanel);
        _optionsPanel = new OptionsPanel();
        _networkPanel = new NetworkPanel();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Main", _mainPanel);
        tabbedPane.addTab("Update Tiles", _updateTilesPanel);
        tabbedPane.addTab("Options", _optionsPanel);
        tabbedPane.addTab("Network", _networkPanel);
        tabbedPane.addTab("Info", new InfoPanel());

        tabbedPane.addChangeListener(new TabChangeListener());

        getContentPane().add(tabbedPane);//, constraints);

        pack();
        center();
    }

    /**
     * Centers the window on the screen
     */
    protected void center()
    {
        setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - getWidth()) / 2, (Toolkit.getDefaultToolkit().getScreenSize().height - getHeight()) / 2);
    }

    protected final JTileDownloaderMainView getMainView()
    {
        return this;
    }

    class MainViewWindowListener
        extends WindowAdapter
    {
        /**
         * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
         */
        @Override
        public void windowClosing(WindowEvent e)
        {
            log.fine("WindowEvent windowClosing");

            _mainPanel.saveAllConfigOptions();
            updateAppConfig();

            e.getWindow().dispose();
            System.exit(0);
        }

    }

    class TabChangeListener
        implements ChangeListener
    {

        /**
         * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
         */
        public void stateChanged(ChangeEvent e)
        {
            if (((JTabbedPane) e.getSource()).getSelectedIndex() == 1)
            {
                log.fine("changed to update tab");
                // selected update tab
                getUpdateTilesPanel().setFolder(getMainPanel().getOutputfolder());
                getUpdateTilesPanel().setTileServer(getMainPanel().getSelectedTileProvider().getName());
            }
            updateAppConfig();
        }
    }

    /**
     * Saves the selected AppConfigurations
     */
    public void updateAppConfig()
    {
        AppConfiguration.getInstance().setUseProxyServer(_networkPanel.isUseProxyServer());
        AppConfiguration.getInstance().setProxyServer(_networkPanel.getProxyServer());
        AppConfiguration.getInstance().setProxyServerPort(_networkPanel.getProxyServerPort());
        AppConfiguration.getInstance().setProxyServerRequiresAuthentitication(_networkPanel.isUseProxyServerAuth());
        AppConfiguration.getInstance().setProxyServerUser(_networkPanel.getProxyServerUser());
        AppConfiguration.getInstance().setProxyServerPassword(_networkPanel.getProxyServerPassword());
        AppConfiguration.getInstance().setDownloadThreads(_optionsPanel.getDownloadThreads());
        AppConfiguration.getInstance().setOverwriteExistingFiles(_optionsPanel.isOverwriteExistingFiles());
        AppConfiguration.getInstance().setTileServer(_mainPanel.getSelectedTileProvider().getName());
        AppConfiguration.getInstance().setLastZoom(_mainPanel.getOutputZoomLevelString());
        AppConfiguration.getInstance().setOutputFolder(_mainPanel.getOutputfolder());
        AppConfiguration.getInstance().setWaitAfterNrTiles(_optionsPanel.isWaitAfterNumberOfTiles());
        AppConfiguration.getInstance().setWaitSeconds(_optionsPanel.getWaitSeconds());
        AppConfiguration.getInstance().setWaitNrTiles(_optionsPanel.getWaitNrTiles());
        AppConfiguration.getInstance().setMinimumAgeInDays(_optionsPanel.getMinimumAgeInDays());
        AppConfiguration.getInstance().setSlippyMap_NoDownload(_optionsPanel.isSlippyMapNoDownload());
        AppConfiguration.getInstance().setSlippyMap_SaveTiles(_optionsPanel.isSlippyMapSaveTiles());
        AppConfiguration.getInstance().setTileSortingPolicy(_optionsPanel.getTileSortingPolicy());
        AppConfiguration.getInstance().saveToFile();
    }

    /**
     * Getter for mainPanel
     * @return the mainPanel
     */
    public final MainPanel getMainPanel()
    {
        return _mainPanel;
    }

    /**
     * Getter for optionsPanel
     * @return the optionsPanel
     */
    public final OptionsPanel getOptionsPanel()
    {
        return _optionsPanel;
    }

    /**
     * Getter for networkPanel
     * @return the networkPanel
     */
    public final NetworkPanel getNetworkPanel()
    {
        return _networkPanel;
    }

    /**
     * Getter for updateTilesPanel
     * @return the updateTilesPanel
     */
    public final UpdateTilesPanel getUpdateTilesPanel()
    {
        return _updateTilesPanel;
    }
}
