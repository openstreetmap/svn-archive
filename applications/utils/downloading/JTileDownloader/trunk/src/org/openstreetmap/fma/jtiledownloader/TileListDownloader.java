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
import java.util.Enumeration;
import java.util.Vector;

import org.openstreetmap.fma.jtiledownloader.config.AppConfiguration;
import org.openstreetmap.fma.jtiledownloader.datatypes.Tile;
import org.openstreetmap.fma.jtiledownloader.datatypes.TileDownloadError;
import org.openstreetmap.fma.jtiledownloader.datatypes.TileDownloadResult;
import org.openstreetmap.fma.jtiledownloader.datatypes.TileProviderIf;
import org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener;
import org.openstreetmap.fma.jtiledownloader.network.ProxyConnection;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileList;

/**
 * Copyright 2008, Friedrich Maier 
 * Copyright 2009, Sven Strickroth <email@cs-ware.de>
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
public class TileListDownloader
{
    private Vector<Tile> _tilesToDownload;
    private String _downloadPath;
    private TileProviderIf _tileProvider;
    private TileListDownloaderThread downloaderThread = null;

    private TileDownloaderListener _listener = null;

    /**
     * @param downloadPath
     * @param tilesToDownload
     */
    public TileListDownloader(String downloadPath, TileList tilesToDownload, TileProviderIf tileProvider)
    {
        super();
        setDownloadPath(downloadPath);
        setTilesToDownload(tilesToDownload.getTileListToDownload());
        _tileProvider = tileProvider;
    }

    public void start()
    {
        downloaderThread = new TileListDownloaderThread();
        downloaderThread.start();
    }

    public void abort()
    {
        if (downloaderThread != null)
        {
            downloaderThread.interrupt();
        }
    }

    /**
     * @param tilesToDownload
     * @return
     */
    public final int getNumberOfTilesToDownload(Vector<Tile> tilesToDownload)
    {
        if (tilesToDownload == null)
        {
            return 0;
        }
        return tilesToDownload.size();
    }

    private TileDownloadResult doDownload(Tile tileToDownload, int actDownloadCounter)
    {
        TileDownloadResult result = new TileDownloadResult();

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
            log("directory " + testDir.getPath() + " does not exist, so create it");
            testDir.mkdirs();
        }

        for (int retries = 0; retries < 5; retries++)
        {
            result = doSingleDownload(fileName, url);
            if (result.getCode() == TileDownloadResult.CODE_OK)
            {
                fireDownloadedTileEvent(fileName, actDownloadCounter, getNumberOfTilesToDownload(getTilesToDownload()));
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
                    e.printStackTrace();
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
     * @return 0 = OK,  
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
        try
        {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("User-Agent", "JTileDownloader/" + Constants.VERSION);
            urlConnection.setUseCaches(false);

            // iflastmodifiedsince would work like this and you would get a 304 response code
            // but it seems as if no tile server supports this so far
            urlConnection.setIfModifiedSince(file.lastModified());

            long lastModified = urlConnection.getLastModified();

            // do not overwrite file if not changed: required because setIfModifiedSince doesn't work for tile-servers atm
            // Mapnik-Servers do not send LastModified-headers...
            if (lastModified != 0 && file.lastModified() >= lastModified)
            {
                result.setCode(TileDownloadResult.CODE_OK);
                result.setMessage(TileDownloadResult.MSG_OK);
                return result;
            }

            //            Map headerFields = urlConnection.getHeaderFields();

            //            WebFile file = new WebFile("http://example.com/example.gif");
            //            String MIME = file.getMIMEType();
            //            Object content = file.getContent();
            //            if (MIME.startsWith("image") && content instanceof java.awt.Image)
            //            {
            //                java.awt.Image image = (java.awt.Image) content;
            //            }

            //            URLConnection conn = url.openConnection(); 
            //            String base64 = "Basic " + new sun.misc.BASE64Encoder(). 
            //                                       encode((user + ":" + passwd).getBytes() ); 
            //            conn.setRequestProperty( "Proxy-Authorization", 
            //              "Basic " + 
            //              new sun.misc.BASE64Encoder().encode((proxyUser + ":" + proxyPass).getBytes()) ); 
            //            conn.connect(); 
            //            InputStream in = conn.getInputStream();

            InputStream inputStream = urlConnection.getInputStream();

            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(new File(fileName)));
            int temp = inputStream.read();
            while (temp != -1)
            {
                bufferedOutputStream.write(temp);
                temp = inputStream.read();
            }
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            result.setCode(TileDownloadResult.CODE_FILENOTFOUND);
            result.setMessage(TileDownloadResult.MSG_FILENOTFOUND);
            return result;
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
            result.setCode(TileDownloadResult.CODE_UNKNOWN_HOST_EXECPTION);
            result.setMessage(TileDownloadResult.MSG_UNKNOWN_HOST_EXECPTION);
            return result;
        }
        catch (IOException e)
        {
            e.printStackTrace();
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
                e1.printStackTrace();
                result.setCode(TileDownloadResult.CODE_UNKNOWN_ERROR);
                result.setMessage(TileDownloadResult.MSG_UNKNOWN_ERROR);
                return result;
            }
            catch (Throwable th)
            {
                th.printStackTrace();
                result.setCode(TileDownloadResult.CODE_UNKNOWN_ERROR);
                result.setMessage(TileDownloadResult.MSG_UNKNOWN_ERROR);
                return result;
            }
        }

        result.setCode(TileDownloadResult.CODE_OK);
        result.setMessage(TileDownloadResult.MSG_OK);
        return result;
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

    public void setListener(TileDownloaderListener listener)
    {
        _listener = listener;
    }

    /**
     * @param fileName
     * @param actCount
     * @param maxCount
     */
    private void fireDownloadedTileEvent(String fileName, int actCount, int maxCount)
    {
        if (_listener != null)
        {
            _listener.downloadedTile(actCount, maxCount, fileName);
        }
    }

    /**
     * @param tile
     * @param actCount
     * @param maxCount
     */
    private void fireErrorOccuredEvent(Tile tile, int actCount, int maxCount)
    {
        if (_listener != null)
        {
            _listener.errorOccured(actCount, maxCount, tile);
        }
    }

    /**
    s     * @param actCount
     * @param maxCount
     */
    private void fireDownloadStoppedEvent(int actCount, int maxCount)
    {
        if (_listener != null)
        {
            _listener.downloadStopped(actCount, maxCount);
        }
    }

    /**
     * 
     */
    private void fireDownloadCompleteEvent(int errorCount, Vector<TileDownloadError> errorTileList)
    {
        if (_listener != null)
        {
            _listener.downloadComplete(errorCount, errorTileList);
        }
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
    public void setTilesToDownload(Vector<Tile> tilesToDownload)
    {
        _tilesToDownload = tilesToDownload;
    }

    /**
     * Getter for tilesToDownload
     * @return the tilesToDownload
     */
    public Vector<Tile> getTilesToDownload()
    {
        return _tilesToDownload;
    }

    public class TileListDownloaderThread
        extends Thread
    {
        /**
         * @see java.lang.Thread#run()
         * {@inheritDoc}
         */
        public void run()
        {
            Vector<TileDownloadError> errorTileList = new Vector<TileDownloadError>();

            if (getTilesToDownload() == null || getTilesToDownload().size() == 0)
            {
                return;
            }

            if (AppConfiguration.getInstance().getUseProxyServer())
            {
                if (AppConfiguration.getInstance().getUseProxyServerAuth())
                {
                    new ProxyConnection(AppConfiguration.getInstance().getProxyServer(), Integer.parseInt(AppConfiguration.getInstance().getProxyServerPort()), AppConfiguration.getInstance().getProxyServerUser(), AppConfiguration.getInstance().getProxyServerPassword());
                }
                else
                {
                    new ProxyConnection(AppConfiguration.getInstance().getProxyServer(), Integer.parseInt(AppConfiguration.getInstance().getProxyServerPort()));
                }
            }

            int errorCount = 0;
            int tileCounter = 0;
            for (Enumeration<Tile> enumeration = getTilesToDownload().elements(); enumeration.hasMoreElements();)
            {
                if (interrupted())
                {
                    fireDownloadStoppedEvent(tileCounter, getNumberOfTilesToDownload(getTilesToDownload()));
                    return;
                }

                Tile tileToDownload = enumeration.nextElement();
                System.out.println("try to download tile " + tileToDownload + " to " + getDownloadPath());
                tileCounter++;

                TileDownloadResult result = doDownload(tileToDownload, tileCounter);

                if (result.getCode() != TileDownloadResult.CODE_OK)
                {
                    errorCount++;
                    TileDownloadError error = new TileDownloadError();
                    error.setTile(tileToDownload);
                    error.setResult(result);
                    errorTileList.add(error);
                    fireErrorOccuredEvent(tileToDownload, tileCounter, getNumberOfTilesToDownload(getTilesToDownload()));
                }

                if ((tileCounter < getNumberOfTilesToDownload(getTilesToDownload())) && AppConfiguration.getInstance().getWaitAfterNrTiles())
                {
                    if ((tileCounter) % (AppConfiguration.getInstance().getWaitNrTiles()) == 0)
                    {
                        try
                        {
                            int waitSeconds = AppConfiguration.getInstance().getWaitSeconds();
                            String waitMsg = "Waiting " + waitSeconds + " sec to resume";
                            System.out.println(waitMsg);
                            fireWaitResume(waitMsg);
                            Thread.sleep(waitSeconds * 1000);
                        }
                        catch (InterruptedException e)
                        {
                            interrupt();
                        }
                    }
                }
            }
            fireDownloadCompleteEvent(errorCount, errorTileList);
        }
    }
}
