package org.openstreetmap.fma.jtiledownloader.views.main;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openstreetmap.fma.jtiledownloader.Constants;
import org.openstreetmap.fma.jtiledownloader.TileListDownloader;
import org.openstreetmap.fma.jtiledownloader.config.AppConfiguration;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileList;

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
public class JTileDownloaderMainView
    extends JFrame
    implements Constants
{
    private static final long serialVersionUID = 1L;

    private TileListDownloader _tileListDownloader;

    private AppConfiguration _appConfiguration;

    private MainPanel _mainPanel;
    private OptionsPanel _optionsPanel;
    private NetworkPanel _networkPanel;

    private UpdateTilesPanel _updateTilesPanel;

    private int _inputTabSelectedIndex;

    public JTileDownloaderMainView()
    {
        super();

        setAppConfiguration(new AppConfiguration());
        getAppConfiguration().loadFromFile();

        generateView();

    }

    /**
     * 
     */
    private void generateView()
    {
        addWindowListener(new MainViewWindowListener());
        setResizable(false);

        setTitle("JTileDownloader" + " Version: " + VERSION);

        _mainPanel = new MainPanel(getMainView());
        int tabIndex = getAppConfiguration().getInputPanelIndex();
        if (tabIndex >= 0 && tabIndex < _mainPanel.getInputTabbedPane().getTabCount())
        {
            _mainPanel.getInputTabbedPane().setSelectedIndex(tabIndex);
            setInputTabSelectedIndex(tabIndex);
        }

        _updateTilesPanel = new UpdateTilesPanel(getAppConfiguration(), getMainView());
        _optionsPanel = new OptionsPanel(getAppConfiguration());
        _networkPanel = new NetworkPanel(getAppConfiguration());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Main", _mainPanel);
        tabbedPane.addTab("Update Tiles", _updateTilesPanel);
        tabbedPane.addTab("Options", _optionsPanel);
        tabbedPane.addTab("Network", _networkPanel);
        tabbedPane.addTab("Info", new InfoPanel());

        tabbedPane.addChangeListener(new TabChangeListener());

        getContentPane().add(tabbedPane);//, constraints);

        pack();
        setVisible(true);

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
         * {@inheritDoc}
         */
        public void windowClosing(WindowEvent e)
        {
            System.out.println("WindowEvent windowClosing");

            updateActualDownloadConfig();
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
         * {@inheritDoc}
         */
        public void stateChanged(ChangeEvent e)
        {
            if (((JTabbedPane) e.getSource()).getSelectedIndex() == 1)
            {
                System.out.println("changed to update tab");
                // selected update tab
                getUpdateTilesPanel().setFolder(getMainPanel().getOutputfolder());

                String tileServer = getMainPanel().getTileServer();
                String altTileServer = getMainPanel().getAltTileServer();
                if (altTileServer != null && altTileServer.length() > 0)
                {
                    tileServer = altTileServer;
                }

                getUpdateTilesPanel().setTileServer(tileServer);

            }

        }
    }

    /**
     * 
     */
    protected TileListDownloader createTileListDownloader(String outputFolder, TileList tilesToDownload)
    {
        TileListDownloader tld = new TileListDownloader(outputFolder, tilesToDownload);
        tld.setWaitAfterTiles(getAppConfiguration().getWaitAfterNrTiles());
        tld.setWaitAfterTilesAmount(getAppConfiguration().getWaitNrTiles());
        tld.setWaitAfterTilesSeconds(getAppConfiguration().getWaitSeconds());

        tld.setOverwriteExistingFiles(getAppConfiguration().isOverwriteExistingFiles());

        tld.setMinimumAgeInDays(getAppConfiguration().getMinimumAgeInDays());

        return tld;
    }

    /**
     * 
     */
    public void updateAppConfig()
    {

        getAppConfiguration().setUseProxyServer(_networkPanel.isUseProxyServer());
        getAppConfiguration().setProxyServer(_networkPanel.getProxyServer());
        getAppConfiguration().setProxyServerPort(_networkPanel.getProxyServerPort());
        getAppConfiguration().setUseProxyServerAuth(_networkPanel.isUseProxyServerAuth());
        getAppConfiguration().setProxyServerUser(_networkPanel.getProxyServerUser());
        getAppConfiguration().setShowTilePreview(_optionsPanel.isShowTilePreview());
        getAppConfiguration().setOverwriteExistingFiles(_optionsPanel.isOverwriteExistingFiles());
        getAppConfiguration().setWaitAfterNrTiles(_optionsPanel.isWaitAfterNumberOfTiles());
        getAppConfiguration().setWaitSeconds(_optionsPanel.getWaitSeconds());
        getAppConfiguration().setWaitNrTiles(_optionsPanel.getWaitNrTiles());
        getAppConfiguration().saveToFile();

    }

    /**
     * Getter for tileListDownloader
     * @return the tileListDownloader
     */
    protected final TileListDownloader getTileListDownloader()
    {
        return _tileListDownloader;
    }

    /**
     * Setter for tileListDownloader
     * @param tileListDownloader the tileListDownloader to set
     */
    protected final void setTileListDownloader(TileListDownloader tileListDownloader)
    {
        _tileListDownloader = tileListDownloader;
    }

    /**
     * Setter for appConfiguration
     * @param appConfiguration the appConfiguration to set
     */
    public void setAppConfiguration(AppConfiguration appConfiguration)
    {
        _appConfiguration = appConfiguration;
    }

    /**
     * Getter for appConfiguration
     * @return the appConfiguration
     */
    public AppConfiguration getAppConfiguration()
    {
        return _appConfiguration;
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

    /**
     * Getter for inputTabSelectedIndex
     * @return the inputTabSelectedIndex
     */
    public final int getInputTabSelectedIndex()
    {
        return _inputTabSelectedIndex;
    }

    /**
     * Setter for inputTabSelectedIndex
     * @param inputTabSelectedIndex the inputTabSelectedIndex to set
     */
    public final void setInputTabSelectedIndex(int inputTabSelectedIndex)
    {
        // save actual input panel
        updateActualDownloadConfig();

        //select new panel & load config
        _inputTabSelectedIndex = inputTabSelectedIndex;
        getAppConfiguration().setInputPanelIndex(inputTabSelectedIndex);
        getMainPanel().getInputPanel().loadConfig();
        getMainPanel().valuesChanged();

    }

    /**
     * 
     */
    public void updateActualDownloadConfig()
    {
        getMainPanel().valuesChanged();
        getMainPanel().getInputPanel().saveConfig();
    }

}
