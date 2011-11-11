/*
 * Copyright 2008, Friedrich Maier
 * Copyright 2009, Sven Strickroth <email@cs-ware.de>
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

package org.openstreetmap.fma.jtiledownloader.cmdline;

import java.util.HashMap;
import java.util.ArrayList;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openstreetmap.fma.jtiledownloader.TileListDownloader;
import org.openstreetmap.fma.jtiledownloader.config.*;
import org.openstreetmap.fma.jtiledownloader.datatypes.*;
import org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileList;

public class JTileDownloaderCommandLine
    implements TileDownloaderListener
{
    private static final Logger log = Logger.getLogger(JTileDownloaderCommandLine.class.getName());
    private static final String CMDLINE_DL = "DL";

    private final HashMap<String, String> _arguments;

    private DownloadJob _downloadJob;
    private TileList _tileList;
    private TileListDownloader _tld;
    private TileProviderIf _tileProvider;

    /**
     * @param arguments
     */
    public JTileDownloaderCommandLine(HashMap<String, String> arguments)
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
            String propertyFile = _arguments.get(CMDLINE_DL);

            _downloadJob = new DownloadJob(propertyFile);

            _tileProvider = _downloadJob.getTileProvider();

            handleDownloadTemplate(_downloadJob.getType());
        }
    }

    /**
     * @param type
     */
    private void handleDownloadTemplate(String type)
    {
        DownloadConfiguration _downloadTemplate = null;

        if (type.equalsIgnoreCase(DownloadConfigurationUrlSquare.ID))
        {
            _downloadTemplate = new DownloadConfigurationUrlSquare();
        }
        else if (type.equalsIgnoreCase(DownloadConfigurationBBoxLatLon.ID))
        {
            _downloadTemplate = new DownloadConfigurationBBoxLatLon();
        }
        else if (type.equalsIgnoreCase(DownloadConfigurationBBoxXY.ID))
        {
            _downloadTemplate = new DownloadConfigurationBBoxXY();
        }
        else if (type.equalsIgnoreCase(DownloadConfigurationGPX.ID))
        {
            _downloadTemplate = new DownloadConfigurationGPX();
        }
        else
        {
            log.severe("File contains an unknown format. Please specify a valid file!");
        }

        if (_downloadTemplate != null)
        {
            _downloadJob.loadDownloadConfig(_downloadTemplate);

            _tileList = _downloadTemplate.getTileList(_downloadJob);

            startDownload(_tileProvider);
        }
    }

    /**
     * 
     */
    private void startDownload(TileProviderIf tileProvider)
    {
        _tld = new TileListDownloader(_downloadJob.getOutputLocation(), _tileList, tileProvider);
        _tld.setListener(this);
        if (_tileList.getTileListToDownload().size() > 0)
        {
            _tld.start();
        }
    }

    /**
     * 
     */
    private void printStartUpMessage()
    {
        log.info("JTileDownloader  Copyright (C) 2008  Friedrich Maier");
        log.info("This program comes with ABSOLUTELY NO WARRANTY.");
        log.info("This is free software, and you are welcome to redistribute it");
        log.info("under certain conditions");
        log.info("See file COPYING.txt and README.txt for details.");
        log.info("");
        log.info("");
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#downloadComplete(int, java.util.ArrayList, int)
     */
    public void downloadComplete(int errorCount, ArrayList<TileDownloadError> errorTileList, int updatedTileCount)
    {
        log.log(Level.INFO, "updated {0} tiles", updatedTileCount);
        log.log(Level.INFO, "download completed with {0} errors", errorCount);
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#downloadedTile(int, int, java.lang.String, int, boolean) 
     */
    public void downloadedTile(int actCount, int maxCount, String path, int updatedCount, boolean updatedTile)
    {
        log.info("downloaded tile " + actCount + "/" + maxCount + " to " + path + ": updated flag is " + updatedTile);
    }

    /**
     * @param message 
     */
    public void waitResume(String message)
    {
        log.log(Level.INFO, "wait to resume: {0}", message);
    }

    /**
     * @param message 
     */
    public void waitWaitHttp500ErrorToResume(String message)
    {
        log.log(Level.WARNING, "http 500 error occured: {0}", message);
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#errorOccured(int, int, Tile)
     */
    public void errorOccured(int actCount, int maxCount, Tile tile)
    {
        log.warning("Error downloading tile " + actCount + "/" + maxCount + " from " + tile);
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#downloadStopped(int, int)
     */
    public void downloadStopped(int actCount, int maxCount)
    {
        log.info("Stopped download at  tile " + actCount + "/" + maxCount);
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#setInfo(java.lang.String)
     */
    public void setInfo(String message)
    {
        log.info(message);
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#downloadPaused(int, int)
     */
    public void downloadPaused(int actCount, int maxCount)
    {}
}
