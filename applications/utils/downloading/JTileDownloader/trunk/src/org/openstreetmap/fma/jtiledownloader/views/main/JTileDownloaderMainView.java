package org.openstreetmap.fma.jtiledownloader.views.main;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import org.openstreetmap.fma.jtiledownloader.TileListDownloader;
import org.openstreetmap.fma.jtiledownloader.config.AppConfiguration;
import org.openstreetmap.fma.jtiledownloader.datatypes.TileDownloadError;
import org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener;
import org.openstreetmap.fma.jtiledownloader.template.DownloadConfigurationUrlSquare;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileList;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileListError;
import org.openstreetmap.fma.jtiledownloader.views.errortilelist.ErrorTileListView;
import org.openstreetmap.fma.jtiledownloader.views.preview.TilePreview;

/**
 * developed by fma (http://wiki.openstreetmap.org/index.php/User:Fma)
 * license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
public class JTileDownloaderMainView
    extends JFrame
    implements TileDownloaderListener
{
    private static final long serialVersionUID = 1L;

    private TileListDownloader _tileListDownloader;

    private DownloadConfigurationUrlSquare _downloadTemplate;
    private AppConfiguration _appConfiguration;

    private TilePreview _tilePreview = null;

    private MainPanel _mainPanel;
    private OptionsPanel _optionsPanel;
    private NetworkPanel _networkPanel;

    private UpdateTilesPanel _updateTilesPanel;

    public JTileDownloaderMainView()
    {
        super();

        setDownloadTemplate(new DownloadConfigurationUrlSquare());
        getDownloadTemplate().loadFromFile();

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

        setTitle("JTileDownloader");

        JTabbedPane tabbedPane = new JTabbedPane();

        _mainPanel = new MainPanel(getDownloadTemplate(), getMainView());
        _updateTilesPanel = new UpdateTilesPanel(_mainPanel.getOutputfolder());
        _optionsPanel = new OptionsPanel(getAppConfiguration());
        _networkPanel = new NetworkPanel(getAppConfiguration());

        tabbedPane.addTab("Main", _mainPanel);
        tabbedPane.addTab("Update Tiles", _updateTilesPanel);
        tabbedPane.addTab("Options", _optionsPanel);
        tabbedPane.addTab("Network", _networkPanel);

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

            updateConfigs();
            e.getWindow().dispose();
            System.exit(0);
        }

    }

    /**
     * Getter for downloadTemplate
     * @return the downloadTemplate
     */
    public final DownloadConfigurationUrlSquare getDownloadTemplate()
    {
        return _downloadTemplate;
    }

    /**
     * Setter for downloadTemplate
     * @param downloadTemplate the downloadTemplate to set
     */
    public final void setDownloadTemplate(DownloadConfigurationUrlSquare downloadTemplate)
    {
        _downloadTemplate = downloadTemplate;
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
                TileListError tiles = new TileListError();
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
    public void updateConfigs()
    {
        _downloadTemplate.setOutputLocation(_mainPanel.getOutputfolder());
        _downloadTemplate.setOutputZoomLevel(Integer.parseInt(_mainPanel.getOutputZoomLevel()));
        _downloadTemplate.setPasteUrl(_mainPanel.getPasteUrl());
        _downloadTemplate.setRadius(_mainPanel.getRadius());
        String altTileServer = _mainPanel.getAltTileServer();
        if (altTileServer == null || altTileServer.length() == 0)
        {
            _downloadTemplate.setTileServer("" + _mainPanel.getTileServer());
        }
        else
        {
            _downloadTemplate.setTileServer("" + altTileServer);
        }
        _downloadTemplate.saveToFile();

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

}
