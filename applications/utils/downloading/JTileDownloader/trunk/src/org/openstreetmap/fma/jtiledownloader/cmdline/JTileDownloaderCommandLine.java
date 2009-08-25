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

package org.openstreetmap.fma.jtiledownloader.cmdline;

import java.util.HashMap;
import java.util.Vector;

import org.openstreetmap.fma.jtiledownloader.TileListDownloader;
import org.openstreetmap.fma.jtiledownloader.Util;
import org.openstreetmap.fma.jtiledownloader.datatypes.GenericTileProvider;
import org.openstreetmap.fma.jtiledownloader.datatypes.Tile;
import org.openstreetmap.fma.jtiledownloader.datatypes.TileDownloadError;
import org.openstreetmap.fma.jtiledownloader.datatypes.TileProviderIf;
import org.openstreetmap.fma.jtiledownloader.downloadjob.DownloadConfiguration;
import org.openstreetmap.fma.jtiledownloader.downloadjob.DownloadConfigurationBBoxLatLon;
import org.openstreetmap.fma.jtiledownloader.downloadjob.DownloadConfigurationBBoxXY;
import org.openstreetmap.fma.jtiledownloader.downloadjob.DownloadConfigurationGPX;
import org.openstreetmap.fma.jtiledownloader.downloadjob.DownloadConfigurationUrlSquare;
import org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileList;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileListBBoxLatLon;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileListCommonBBox;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileListCommonGPX;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileListUrlSquare;

public class JTileDownloaderCommandLine
    implements TileDownloaderListener
{

    private static final Object CMDLINE_DL = "DL";

    private final HashMap<String, String> _arguments;

    private DownloadConfiguration _downloadTemplateCommon;
    private DownloadConfiguration _downloadTemplate;
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

            _downloadTemplateCommon = new DownloadConfiguration(propertyFile);
            _downloadTemplateCommon.loadFromFile();

            _tileProvider = Util.getTileProvider(_downloadTemplateCommon.getTileServer());

            handleDownloadTemplate(_downloadTemplateCommon.getType(), propertyFile);
        }
    }

    /**
     * @param type
     */
    private void handleDownloadTemplate(String type, String propertyFile)
    {
        if (type.equalsIgnoreCase(DownloadConfigurationUrlSquare.ID))
        {
            handleUrlSquare(propertyFile);
        }
        else if (type.equalsIgnoreCase(DownloadConfigurationBBoxLatLon.ID))
        {
            handleBBoxLatLon(propertyFile);
        }
        else if (type.equalsIgnoreCase(DownloadConfigurationBBoxXY.ID))
        {
            handleBBoxXY(propertyFile);
        }
        else if (type.equalsIgnoreCase(DownloadConfigurationGPX.ID))
        {
            handleGPX(propertyFile);
        }
        else
        {
            log("File '" + propertyFile + "' contains an unknown format. Please specify a valid file!");
        }

    }

    /**
     * @param propertyFile
     */
    private void handleGPX(String propertyFile)
    {
        _downloadTemplate = new DownloadConfigurationGPX(propertyFile);
        _downloadTemplate.loadFromFile();

        _tileList = new TileListCommonGPX();

        ((TileListCommonGPX) _tileList).setDownloadZoomLevels(Util.getOutputZoomLevelArray(_tileProvider, _downloadTemplate.getOutputZoomLevels()));

        String gpxFile = ((DownloadConfigurationGPX) _downloadTemplate).getGpxFile();
        int corridor = ((DownloadConfigurationGPX) _downloadTemplate).getCorridor();
        ((TileListCommonGPX) _tileList).updateList(gpxFile, corridor);

        startDownload(_downloadTemplate.getTileServer());
    }

    /**
     * @param propertyFile
     */
    private void handleBBoxXY(String propertyFile)
    {
        _downloadTemplate = new DownloadConfigurationBBoxXY(propertyFile);
        _downloadTemplate.loadFromFile();

        _tileList = new TileListCommonBBox();

        ((TileListCommonBBox) _tileList).setDownloadZoomLevels(Util.getOutputZoomLevelArray(_tileProvider, _downloadTemplate.getOutputZoomLevels()));

        ((TileListCommonBBox) _tileList).initXTopLeft(((DownloadConfigurationBBoxXY) _downloadTemplate).getMinX());
        ((TileListCommonBBox) _tileList).initYTopLeft(((DownloadConfigurationBBoxXY) _downloadTemplate).getMinY());
        ((TileListCommonBBox) _tileList).initXBottomRight(((DownloadConfigurationBBoxXY) _downloadTemplate).getMaxX());
        ((TileListCommonBBox) _tileList).initYBottomRight(((DownloadConfigurationBBoxXY) _downloadTemplate).getMaxY());

        startDownload(_downloadTemplate.getTileServer());
    }

    /**
     * @param propertyFile
     */
    private void handleBBoxLatLon(String propertyFile)
    {
        _downloadTemplate = new DownloadConfigurationBBoxLatLon(propertyFile);
        _downloadTemplate.loadFromFile();

        _tileList = new TileListBBoxLatLon();

        ((TileListBBoxLatLon) _tileList).setMinLat(((DownloadConfigurationBBoxLatLon) _downloadTemplate).getMinLat());
        ((TileListBBoxLatLon) _tileList).setMaxLat(((DownloadConfigurationBBoxLatLon) _downloadTemplate).getMaxLat());
        ((TileListBBoxLatLon) _tileList).setMinLon(((DownloadConfigurationBBoxLatLon) _downloadTemplate).getMinLon());
        ((TileListBBoxLatLon) _tileList).setMaxLon(((DownloadConfigurationBBoxLatLon) _downloadTemplate).getMaxLon());

        ((TileListBBoxLatLon) _tileList).setDownloadZoomLevels(Util.getOutputZoomLevelArray(_tileProvider, _downloadTemplate.getOutputZoomLevels()));

        ((TileListBBoxLatLon) _tileList).calculateTileValuesXY();

        startDownload(_downloadTemplate.getTileServer());
    }

    /**
     * @param propertyFile
     */
    private void handleUrlSquare(String propertyFile)
    {
        _downloadTemplate = new DownloadConfigurationUrlSquare(propertyFile);
        _downloadTemplate.loadFromFile();

        _tileList = new TileListUrlSquare();

        String url = ((DownloadConfigurationUrlSquare) _downloadTemplate).getPasteUrl();
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

        ((TileListUrlSquare) _tileList).setLatitude(Double.parseDouble(lat));
        ((TileListUrlSquare) _tileList).setLongitude(Double.parseDouble(lon));

        ((TileListUrlSquare) _tileList).setRadius(((DownloadConfigurationUrlSquare) _downloadTemplate).getRadius() * 1000);
        ((TileListUrlSquare) _tileList).setDownloadZoomLevels(Util.getOutputZoomLevelArray(_tileProvider, _downloadTemplate.getOutputZoomLevels()));

        ((TileListUrlSquare) _tileList).calculateTileValuesXY();

        startDownload(_downloadTemplate.getTileServer());
    }

    /**
     * 
     */
    private void startDownload(String server)
    {
        _tld = new TileListDownloader(_downloadTemplate.getOutputLocation(), _tileList, new GenericTileProvider(server));
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
        log("JTileDownloader  Copyright (C) 2008  Friedrich Maier");
        log("This program comes with ABSOLUTELY NO WARRANTY.");
        log("This is free software, and you are welcome to redistribute it");
        log("under certain conditions");
        log("See file COPYING.txt and README.txt for details.");
        log("");
        log("");
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#downloadComplete(int)
     * {@inheritDoc}
     */
    public void downloadComplete(int errorCount, Vector<TileDownloadError> errorTileList, int updatedTileCount)
    {
        log("updated " + updatedTileCount + " tiles");
        log("download completed with " + errorCount + " errors");
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#downloadedTile(int, int, java.lang.String)
     * {@inheritDoc}
     */
    public void downloadedTile(int actCount, int maxCount, String path, boolean updatedTile)
    {
        log("downloaded tile " + actCount + "/" + maxCount + " to " + path + ": updated flag is " + updatedTile);
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

    /**
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#errorOccured(int, int, java.lang.String)
     * {@inheritDoc}
     */
    public void errorOccured(int actCount, int maxCount, Tile tile)
    {
        log("Error downloading tile " + actCount + "/" + maxCount + " from " + tile);
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#downloadStopped(int, int)
     * {@inheritDoc}
     */
    public void downloadStopped(int actCount, int maxCount)
    {
        log("Stopped download at  tile " + actCount + "/" + maxCount);
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener#setInfo(java.lang.String)
     * {@inheritDoc}
     */
    public void setInfo(String message)
    {
        log(message);
    }
}
