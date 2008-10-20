package org.openstreetmap.fma.jtiledownloader.cmdline;

import java.util.HashMap;
import java.util.Vector;

import org.openstreetmap.fma.jtiledownloader.TileListDownloader;
import org.openstreetmap.fma.jtiledownloader.config.AppConfiguration;
import org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener;
import org.openstreetmap.fma.jtiledownloader.template.DownloadConfigurationUrlSquare;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileListSquare;

/**
 * developed by fma (http://wiki.openstreetmap.org/index.php/User:Fma)
 * license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
public class JTileDownloaderCommandLine
    implements TileDownloaderListener
{

    private static final Object CMDLINE_DL = "DL";

    private final HashMap _arguments;

    private AppConfiguration _appConfiguration;
    private DownloadConfigurationUrlSquare _downloadTemplate;
    private TileListSquare _tileListSquare = new TileListSquare();
    private TileListDownloader _tld;

    /**
     * @param arguments
     */
    public JTileDownloaderCommandLine(HashMap arguments)
    {
        _arguments = arguments;
    }

    /**
     * 
     */
    public void start()
    {
        if (_arguments.containsKey(CMDLINE_DL))
        {
            String propertyFile = (String) _arguments.get(CMDLINE_DL);
            _downloadTemplate = new DownloadConfigurationUrlSquare(propertyFile);
        }
        else
        {
            _downloadTemplate = new DownloadConfigurationUrlSquare();
        }

        _downloadTemplate.loadFromFile();

        _appConfiguration = new AppConfiguration();
        _appConfiguration.loadFromFile();

        parsePasteUrl();
        _tileListSquare.setRadius(_downloadTemplate.getRadius() * 1000);
        _tileListSquare.setDownloadZoomLevel(_downloadTemplate.getOutputZoomLevel());
        _tileListSquare.setTileServerBaseUrl(_downloadTemplate.getTileServer());

        _tileListSquare.calculateTileValuesXY();

        _tld = new TileListDownloader(_downloadTemplate.getOutputLocation(), _tileListSquare);
        _tld.setWaitAfterTiles(_appConfiguration.getWaitAfterNrTiles());
        _tld.setWaitAfterTilesAmount(_appConfiguration.getWaitNrTiles());
        _tld.setWaitAfterTilesSeconds(_appConfiguration.getWaitSeconds());

        _tld.setListener(this);
        _tld.start();

    }

    /**
     * 
     */
    private void parsePasteUrl()
    {
        String url = _downloadTemplate.getPasteUrl();
        if (url == null || url.length() == 0)
        {
            return;
        }

        int posLat = url.indexOf("lat=");
        String lat = url.substring(posLat);
        int posLon = url.indexOf("lon=");
        String lon = url.substring(posLon);

        int posAnd = lat.indexOf("&");
        lat = lat.substring(4, posAnd);
        posAnd = lon.indexOf("&");
        lon = lon.substring(4, posAnd);

        _tileListSquare.setLatitude(Double.parseDouble(lat));
        _tileListSquare.setLongitude(Double.parseDouble(lon));

    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#downloadComplete(int)
     * {@inheritDoc}
     */
    public void downloadComplete(int errorCount, Vector errorTileList)
    {
        log("download completed with " + errorCount + " errors");
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#downloadedTile(int, int, java.lang.String)
     * {@inheritDoc}
     */
    public void downloadedTile(int actCount, int maxCount, String path)
    {
        log("downloaded tile " + actCount + "/" + maxCount + " to " + path);
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#waitResume(java.lang.String)
     * {@inheritDoc}
     */
    public void waitResume(String message)
    {
        log("wait to resume: " + message);
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#waitWaitHttp500ErrorToResume(java.lang.String)
     * {@inheritDoc}
     */
    public void waitWaitHttp500ErrorToResume(String message)
    {
        log("http 500 error occured: " + message);
    }

    /**
     * method to write to System.out
     * 
     * @param msg message to log
     */
    private static void log(String msg)
    {
        System.out.println(msg);
    }

}
