package org.openstreetmap.fma.jtiledownloader.views.main;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openstreetmap.fma.jtiledownloader.Constants;
import org.openstreetmap.fma.jtiledownloader.TileListDownloader;
import org.openstreetmap.fma.jtiledownloader.config.AppConfiguration;
import org.openstreetmap.fma.jtiledownloader.datatypes.TileDownloadError;
import org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileList;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileListSimple;
import org.openstreetmap.fma.jtiledownloader.views.errortilelist.ErrorTileListView;
import org.openstreetmap.fma.jtiledownloader.views.preview.TilePreview;

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
    implements TileDownloaderListener, Constants
{
    private static final long serialVersionUID = 1L;

    private TileListDownloader _tileListDownloader;

    private AppConfiguration _appConfiguration;

    private TilePreview _tilePreview = null;

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

        setTitle("JTileDownloader" + " Version: " + VERSION);

        JTabbedPane tabbedPane = new JTabbedPane();

        _mainPanel = new MainPanel(getMainView());

        _mainPanel.getInputPanel().loadConfig();

        _updateTilesPanel = new UpdateTilesPanel(getAppConfiguration(), getMainView());
        _optionsPanel = new OptionsPanel(getAppConfiguration());
        _networkPanel = new NetworkPanel(getAppConfiguration());

        tabbedPane.addTab("Main", _mainPanel);
        tabbedPane.addTab("Update Tiles", _updateTilesPanel);
        tabbedPane.addTab("Options", _optionsPanel);
        tabbedPane.addTab("Network", _networkPanel);

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

            getMainPanel().valuesChanged();
            updateAppConfig();
            updateActualDownloadConfig();
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
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#downloadedTile(int, int, java.lang.String)
     * {@inheritDoc}
     */
    public void downloadedTile(final int actCount, final int maxCount, String filePathName)
    {
        System.out.println("downloadedTile: actCount=" + actCount + ", maxCount=" + maxCount + ",path=" + filePathName);

        if (_mainPanel != null)
        {
            _mainPanel.getProgressBar().setValue(actCount);
            _mainPanel.getProgressBar().setString("Download Tile " + actCount + "/" + maxCount);
        }

        if (getAppConfiguration().isShowTilePreview())
        {
            if (_tilePreview == null)
            {
                _tilePreview = new TilePreview();
                _tilePreview.setLocation(getX() + (getWidth() / 2) - (_tilePreview.getWidth() / 2), getY() + (getHeight() / 2) - (_tilePreview.getHeight() / 2));
            }
            if (!_tilePreview.isVisible())
            {
                _tilePreview.setVisible(true);
            }

            _tilePreview.showImage(filePathName);
        }

    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#downloadComplete()
     * {@inheritDoc}
     */
    public void downloadComplete(final int errorCount, Vector errorTileList)
    {

        _mainPanel.getProgressBar().setString("Completed with " + errorCount + " error(s)");

        _mainPanel.getButtonDownload().setEnabled(true);
        _mainPanel.getButtonExport().setEnabled(true);

        getTileListDownloader().setListener(null);
        setTileListDownloader(null);

        if (getAppConfiguration().isAutoCloseTilePreview())
        {
            if (_tilePreview != null)
            {
                try
                {
                    Thread.sleep(500);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                _tilePreview.setVisible(false);
                _tilePreview = null;
            }
        }

        if (errorTileList != null && errorTileList.size() > 0)
        {
            // TODO: show List of failed tiles
            ErrorTileListView view = new ErrorTileListView(this, errorTileList);
            view.setVisible(true);
            int exitCode = view.getExitCode();
            view = null;

            if (exitCode == ErrorTileListView.CODE_RETRY)
            {
                TileListSimple tiles = new TileListSimple();
                for (Enumeration enumeration = errorTileList.elements(); enumeration.hasMoreElements();)
                {
                    TileDownloadError tde = (TileDownloadError) enumeration.nextElement();
                    tiles.addTile(tde.getTile());
                }

                setTileListDownloader(createTileListDownloader(_mainPanel.getOutputfolder(), tiles));

                _mainPanel.getProgressBar().setMinimum(0);
                _mainPanel.getProgressBar().setMaximum(tiles.getElementCount());
                _mainPanel.getProgressBar().setStringPainted(true);
                _mainPanel.getProgressBar().setString("Retry download ...");

                getTileListDownloader().setListener(this);
                getTileListDownloader().start();

            }

        }

    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#waitResume(java.lang.String)
     * {@inheritDoc}
     */
    public void waitResume(String message)
    {
        _mainPanel.getProgressBar().setString(message);
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#waitWaitHttp500ErrorToResume(java.lang.String)
     * {@inheritDoc}
     */
    public void waitWaitHttp500ErrorToResume(String message)
    {
        _mainPanel.getProgressBar().setString(message);
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
        getAppConfiguration().setAutoCloseTilePreview(_optionsPanel.isAutoCloseTilePreview());
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
        _inputTabSelectedIndex = inputTabSelectedIndex;
    }

    /**
     * 
     */
    public void updateActualDownloadConfig()
    {
        _mainPanel.getInputPanel().saveConfig();

    }

}
