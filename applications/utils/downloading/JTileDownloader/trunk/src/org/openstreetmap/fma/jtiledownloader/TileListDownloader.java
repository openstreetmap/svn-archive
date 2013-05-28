/*
 * Copyright 2008, Friedrich Maier
 * Copyright 2009 - 2010, Sven Strickroth <email@cs-ware.de>
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

package org.openstreetmap.fma.jtiledownloader;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.ArrayList;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openstreetmap.fma.jtiledownloader.config.AppConfiguration;
import org.openstreetmap.fma.jtiledownloader.datatypes.Tile;
import org.openstreetmap.fma.jtiledownloader.datatypes.TileComparatorFactory;
import org.openstreetmap.fma.jtiledownloader.datatypes.TileDownloadError;
import org.openstreetmap.fma.jtiledownloader.datatypes.TileDownloadResult;
import org.openstreetmap.fma.jtiledownloader.datatypes.TileProviderIf;
import org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener;
import org.openstreetmap.fma.jtiledownloader.network.ProxyConnection;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileList;

public class TileListDownloader
{
    private static final Logger log = Logger.getLogger(TileListDownloader.class.getName());
    
    private LinkedList<Tile> _tilesToDownload;
    private String _downloadPath;
    private TileProviderIf _tileProvider;
    private ArrayList<TileListDownloaderThread> downloaderThreads = new ArrayList<TileListDownloaderThread>();

    private TileDownloaderListener _listener = null;

    private boolean paused = false;

    private int _numberOfTilesToDownload = 0;
    private int _errorCount = 0;
    private int _updatedTilesCount = 0;
    private LinkedList<TileDownloadError> _errorTileList = new LinkedList<TileDownloadError>();

    private static final int MAX_RETRIES = 5;

    /**
     * @param downloadPath
     * @param tilesToDownload
     * @param tileProvider 
     */
    public TileListDownloader(String downloadPath, TileList tilesToDownload, TileProviderIf tileProvider)
    {
        super();
        setDownloadPath(downloadPath);
        setTilesToDownload(tilesToDownload.getTileListToDownload());
        _tileProvider = tileProvider;

        if (AppConfiguration.getInstance().getUseProxyServer())
        {
            if (AppConfiguration.getInstance().isProxyServerRequiresAuthentitication())
            {
                ProxyConnection.setProxyData(AppConfiguration.getInstance().getProxyServer(),
                        Integer.parseInt(AppConfiguration.getInstance().getProxyServerPort()),
                        AppConfiguration.getInstance().getProxyServerUser(),
                        AppConfiguration.getInstance().getProxyServerPassword());
            }
            else
            {
                ProxyConnection.setProxyData(AppConfiguration.getInstance().getProxyServer(),
                        Integer.parseInt(AppConfiguration.getInstance().getProxyServerPort()));
            }
        }
    }

    public void start()
    {
        paused = false;
        for (int i = 0; i < AppConfiguration.getInstance().getDownloadThreads(); i++)
        {
            TileListDownloaderThread downloaderThread = new TileListDownloaderThread();
            downloaderThread.start();
            downloaderThreads.add(downloaderThread);
        }
    }

    public void abort()
    {
        for (TileListDownloaderThread downloaderThread : downloaderThreads)
        {
            downloaderThread.interrupt();
        }
        if (paused && runningThreads() == 0)
        {
            fireDownloadStoppedEvent();
        }
    }

    public void pause()
    {
        paused = true;
        for (TileListDownloaderThread downloaderThread : downloaderThreads)
        {
            downloaderThread.interrupt();
        }
    }

    synchronized private void increaseUpdatedCount()
    {
        _updatedTilesCount++;
    }

    synchronized private void addedTileDownloadError(TileDownloadResult result, Tile tileToDownload)
    {
        _errorCount++;
        TileDownloadError error = new TileDownloadError();
        error.setTile(tileToDownload);
        error.setResult(result);
        _errorTileList.add(error);
    }

    private TileDownloadResult doDownload(Tile tileToDownload)
    {
        TileDownloadResult result = new TileDownloadResult();

        /*if( 1 == 1 ) {
            log.info(tileToDownload.toString());
            result.setCode(TileDownloadResult.CODE_OK);
            result.setMessage(TileDownloadResult.MSG_OK);
            return result;
        }*/
        
        URL url = null;
        try
        {
            url = new URL(_tileProvider.getTileUrl(tileToDownload));
        }
        catch (MalformedURLException e)
        {
            result.setCode(TileDownloadResult.CODE_MALFORMED_URL_EXECPTION);
            result.setMessage(TileDownloadResult.MSG_MALFORMED_URL_EXECPTION);
            return result;
        }

        String fileName = getDownloadPath() + File.separator + _tileProvider.getTileFilename(tileToDownload);
        String filePath = getDownloadPath() + File.separator + tileToDownload.getPath();

        File testDir = new File(filePath);
        if (!testDir.exists())
        {
            log.info("Creating directory " + testDir.getPath());
            testDir.mkdirs();
        }

        for (int retries = 0; retries < MAX_RETRIES; retries++)
        {
            result = doSingleDownload(fileName, url);
            if (result.getCode() == TileDownloadResult.CODE_OK)
            {
                fireDownloadedTileEvent(fileName, result.isUpdatedTile());
                break;
            }
            else if (result.getCode() == TileDownloadResult.CODE_HTTP_500)
            {
                // HTTP-500 Error - retry again
                fireWaitHttp500ErrorToResume("HTTP/500 - wait 10 sec. to retry");
                try
                {
                    Thread.sleep(10000);
                }
                catch (InterruptedException e)
                {
                    retries = MAX_RETRIES;
                    Thread.currentThread().interrupt();
                }
            }
            else
            // unknown error
            {
                return result;
            }

        }

        return result;
    }

    /**
     * @param fileName
     * @param url
     * @return TileDownloadResult
     */
    private TileDownloadResult doSingleDownload(String fileName, URL url)
    {
        TileDownloadResult result = new TileDownloadResult();

        File file = new File(fileName);
        if (file.exists())
        {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR, -24 * AppConfiguration.getInstance().getMinimumAgeInDays());
            if (!AppConfiguration.getInstance().isOverwriteExistingFiles())
            {
                result.setCode(TileDownloadResult.CODE_OK);
                result.setMessage(TileDownloadResult.MSG_OK);
                return result;
            }
            else if (file.lastModified() >= cal.getTimeInMillis())
            {
                result.setCode(TileDownloadResult.CODE_OK);
                result.setMessage(TileDownloadResult.MSG_OK);
                return result;
            }
        }

        HttpURLConnection urlConnection = null;
        boolean imageNeedsToBeErased = false;
        try
        {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("User-Agent", "JTileDownloader/" + Constants.VERSION);
            urlConnection.setUseCaches(true);

            // iflastmodifiedsince would work like this and you would get a 304 response code
            // but it seems as if no tile server supports this so far
            urlConnection.setIfModifiedSince(file.lastModified());

            long lastModified = urlConnection.getLastModified();

            // do not overwrite file if not changed: required because setIfModifiedSince doesn't work for tile-servers atm
            // Mapnik-Servers do not send LastModified-headers...
            if( file.length() > 0 ) {
                if ((lastModified != 0 && file.lastModified() >= lastModified) || urlConnection.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED)
                {
                    result.setCode(TileDownloadResult.CODE_OK);
                    result.setMessage(TileDownloadResult.MSG_OK);
                    return result;
                }
            }

            InputStream inputStream = urlConnection.getInputStream();

            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
            imageNeedsToBeErased = true;
            int temp = inputStream.read();
            while (temp != -1)
            {
                bufferedOutputStream.write(temp);
                temp = inputStream.read();
            }
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
            imageNeedsToBeErased = false;
        }
        catch (FileNotFoundException e)
        {
            log.log(Level.SEVERE, "Error downloading tile", e);
            result.setCode(TileDownloadResult.CODE_FILENOTFOUND);
            result.setMessage(TileDownloadResult.MSG_FILENOTFOUND);
            return result;
        }
        catch (UnknownHostException e)
        {
            log.log(Level.SEVERE, "Could not find host for a tile", e);
            result.setCode(TileDownloadResult.CODE_UNKNOWN_HOST_EXECPTION);
            result.setMessage(TileDownloadResult.MSG_UNKNOWN_HOST_EXECPTION);
            return result;
        }
        catch (IOException e)
        {
            log.log(Level.SEVERE, "Error downloading tile", e);
            try
            {
                if (urlConnection != null && urlConnection.getResponseCode() == 500)
                {
                    result.setCode(TileDownloadResult.CODE_HTTP_500);
                    result.setMessage(TileDownloadResult.MSG_HTTP_500);
                    return result;
                }
                else
                {
                    result.setCode(TileDownloadResult.CODE_UNKNOWN_ERROR);
                    if (urlConnection != null && urlConnection.getResponseMessage().length() > 0)
                    {
                        result.setMessage(urlConnection.getResponseMessage());
                    }
                    else
                    {
                        result.setMessage(TileDownloadResult.MSG_UNKNOWN_ERROR);
                    }
                    return result;
                }
            }
            catch (IOException e1)
            {
                log.log(Level.SEVERE, "Error getting response message", e1);
                result.setCode(TileDownloadResult.CODE_UNKNOWN_ERROR);
                result.setMessage(TileDownloadResult.MSG_UNKNOWN_ERROR);
                return result;
            }
            catch (Throwable th)
            {
                log.log(Level.SEVERE, "Error getting response message", th);
                result.setCode(TileDownloadResult.CODE_UNKNOWN_ERROR);
                result.setMessage(TileDownloadResult.MSG_UNKNOWN_ERROR);
                return result;
            }
        }
        finally {
            if( imageNeedsToBeErased ) {
                log.info("Deleting incomplete tile " + file.getPath());
                try {
                    file.delete();
                } catch( Exception e ) {
                    log.log(Level.SEVERE, "Could not delete", e);
                }
            }
        }

        result.setCode(TileDownloadResult.CODE_OK);
        result.setMessage(TileDownloadResult.MSG_OK);
        result.setUpdatedTile(true);
        return result;
    }

    public void setListener(TileDownloaderListener listener)
    {
        _listener = listener;
    }

    /**
     * @param fileName
     * @param updatedTile 
     */
    private void fireDownloadedTileEvent(String fileName, boolean updatedTile)
    {
        if (updatedTile)
        {
            increaseUpdatedCount();
        }
        if (_listener != null)
        {
            _listener.downloadedTile(_numberOfTilesToDownload - _tilesToDownload.size(), _numberOfTilesToDownload, fileName, _updatedTilesCount, updatedTile);
        }
    }

    /**
     * @param tile
     */
    private void fireErrorOccuredEvent(Tile tile)
    {
        if (_listener != null)
        {
            _listener.errorOccured(_numberOfTilesToDownload - _tilesToDownload.size(), _numberOfTilesToDownload, tile);
        }
    }

    private void fireDownloadStoppedEvent()
    {
        if (_listener != null && isLastThread())
        {
            _listener.downloadStopped(_numberOfTilesToDownload - _tilesToDownload.size(), _numberOfTilesToDownload);
        }
    }

    private void fireDownloadPausedEvent()
    {
        if (_listener != null && isLastThread())
        {
            _listener.downloadPaused(_numberOfTilesToDownload - _tilesToDownload.size(), _numberOfTilesToDownload);
        }
    }

    private void fireDownloadCompleteEvent()
    {
        if (_listener != null && isLastThread())
        {
            _listener.downloadComplete(_errorCount, new ArrayList<TileDownloadError>(_errorTileList), _updatedTilesCount);
        }
    }

    synchronized private int runningThreads() {
        int runningThreads = 0;
        for (TileListDownloaderThread downloaderThread : downloaderThreads)
        {
            if (downloaderThread.isAlive()) {
                runningThreads++;
            }
        }
        return runningThreads;
    }

    /**
     * @return is the current thread is the last thread
     */
    private boolean isLastThread()
    {
        return (runningThreads() <= 1);
    }

    /**
     * 
     */
    private void fireWaitResume(String message)
    {
        if (_listener != null)
        {
            _listener.setInfo(message);
        }
    }

    /**
     * 
     */
    private void fireWaitHttp500ErrorToResume(String message)
    {
        if (_listener != null)
        {
            _listener.setInfo(message);
        }
    }

    /**
     * Setter for downloadPath
     * @param downloadPath the downloadPath to set
     */
    public void setDownloadPath(String downloadPath)
    {
        _downloadPath = downloadPath;
    }

    /**
     * Getter for downloadPath
     * @return the downloadPath
     */
    public String getDownloadPath()
    {
        return _downloadPath;
    }

    /**
     * Setter for tilesToDownload
     * @param tilesToDownload the tilesToDownload to set
     */
    public void setTilesToDownload(ArrayList<Tile> tilesToDownload)
    {
        if (tilesToDownload != null)
        {
            _tilesToDownload = new LinkedList<Tile>(tilesToDownload);
            int tileSortingPolicy = AppConfiguration.getInstance().getTileSortingPolicy();
            if( tileSortingPolicy > 0 ) {
                Collections.sort(_tilesToDownload, TileComparatorFactory.getComparator(tileSortingPolicy));
            }
        }
        else
        {
            _tilesToDownload = new LinkedList<Tile>();
        }
        _numberOfTilesToDownload = _tilesToDownload.size();
    }

    /**
     * Get tile to download
     * @return a tile
     */
    synchronized private Tile getTilesToDownload()
    {
        return _tilesToDownload.poll();
    }

    synchronized private void requeueTile(Tile tile)
    {
        _tilesToDownload.add(tile);
    }

    public class TileListDownloaderThread
        extends Thread
    {
        /**
         * @see java.lang.Thread#run()
         */
        @Override
        public void run()
        {
            Tile tileToDownload = null;
            int tilesDownloaded = 0;
            while ((tileToDownload = getTilesToDownload()) != null)
            {
                if (tilesDownloaded > 0 && AppConfiguration.getInstance().isWaitingAfterNrOfTiles())
                {
                    if ((tilesDownloaded) % (AppConfiguration.getInstance().getWaitNrTiles()) == 0)
                    {
                        try
                        {
                            int waitSeconds = AppConfiguration.getInstance().getWaitSeconds();
                            String waitMsg = "Waiting " + waitSeconds + " sec to resume";
                            log.info(waitMsg);
                            fireWaitResume(waitMsg);
                            Thread.sleep(waitSeconds * 1000);
                        }
                        catch (InterruptedException e)
                        {
                            interrupt();
                        }
                    }
                }

                if (interrupted())
                {
                    requeueTile(tileToDownload);
                    if (paused)
                    {
                        fireDownloadPausedEvent();
                    }
                    else
                    {
                        fireDownloadStoppedEvent();
                    }
                    return;
                }

                log.info("Downloading tile " + tileToDownload + " to " + getDownloadPath());

                TileDownloadResult result = doDownload(tileToDownload);

                if (result.getCode() != TileDownloadResult.CODE_OK)
                {
                    addedTileDownloadError(result, tileToDownload);
                    fireErrorOccuredEvent(tileToDownload);
                }

                tilesDownloaded++;
            }
            fireDownloadCompleteEvent();
        }
    }
}
