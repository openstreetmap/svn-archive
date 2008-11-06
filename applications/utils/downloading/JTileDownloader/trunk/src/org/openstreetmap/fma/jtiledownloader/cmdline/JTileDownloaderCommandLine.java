package org.openstreetmap.fma.jtiledownloader.cmdline;

import java.util.HashMap;
import java.util.Vector;

import org.openstreetmap.fma.jtiledownloader.TileListDownloader;
import org.openstreetmap.fma.jtiledownloader.config.AppConfiguration;
import org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener;
import org.openstreetmap.fma.jtiledownloader.template.DownloadConfigurationUrlSquare;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileListUrlSquare;

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
public class JTileDownloaderCommandLine
    implements TileDownloaderListener
{

    private static final Object CMDLINE_DL = "DL";

    private final HashMap _arguments;

    private AppConfiguration _appConfiguration;
    private DownloadConfigurationUrlSquare _downloadTemplate;
    private TileListUrlSquare _tileListSquare = new TileListUrlSquare();
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
        printStartUpMessage();

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
        _tileListSquare.setDownloadZoomLevels(_downloadTemplate.getOutputZoomLevels());
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
    private void printStartUpMessage()
    {
        System.out.println("JTileDownloader  Copyright (C) 2008  Friedrich Maier");
        System.out.println("This program comes with ABSOLUTELY NO WARRANTY.");
        System.out.println("This is free software, and you are welcome to redistribute it");
        System.out.println("under certain conditions");
        System.out.println("See file COPYING.txt and README.txt for details.");
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
