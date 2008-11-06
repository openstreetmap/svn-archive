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
import java.util.Enumeration;
import java.util.Vector;

import org.openstreetmap.fma.jtiledownloader.datatypes.TileDownloadError;
import org.openstreetmap.fma.jtiledownloader.datatypes.TileDownloadResult;
import org.openstreetmap.fma.jtiledownloader.listener.TileDownloaderListener;
import org.openstreetmap.fma.jtiledownloader.network.ProxyConnection;
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
public class TileListDownloader
    extends Thread
{
    private Vector _tilesToDownload;
    private String _downloadPath;

    private TileDownloaderListener _listener = null;

    private boolean _useProxyServer = false;
    private boolean _useProxyServerAuth = false;
    private String _proxyServer = "localhost";
    private String _proxyServerUser = "local";
    private String _proxyServerPort = "8080";
    private String _proxyServerPassword = "pass";

    private boolean _waitAfterTiles = false;
    private int _waitAfterTilesAmount = 0;
    private int _waitAfterTilesSeconds = 1;

    /**
     * @param downloadPath
     * @param tilesToDownload
     */
    public TileListDownloader(String downloadPath, TileList tilesToDownload)
    {
        super();
        setDownloadPath(downloadPath);
        setTilesToDownload(tilesToDownload.getFileListToDownload());
    }

    /**
     * Getter for proxyServer
     * @return the proxyServer
     */
    public final String getProxyServer()
    {
        return _proxyServer;
    }

    /**
     * Setter for proxyServer
     * @param proxyServer the proxyServer to set
     */
    public final void setProxyServer(String proxyServer)
    {
        _proxyServer = proxyServer;
    }

    /**
     * Getter for useProxyServer
     * @return the useProxyServer
     */
    public final boolean isUseProxyServer()
    {
        return _useProxyServer;
    }

    /**
     * Setter for useProxyServer
     * @param useProxyServer the useProxyServer to set
     */
    public final void setUseProxyServer(boolean useProxyServer)
    {
        _useProxyServer = useProxyServer;
    }

    /**
     * Getter for useProxyServerAuth
     * @return the useProxyServerAuth
     */
    public final boolean isUseProxyServerAuth()
    {
        return _useProxyServerAuth;
    }

    /**
     * Setter for useProxyServerAuth
     * @param useProxyServerAuth the useProxyServerAuth to set
     */
    public final void setUseProxyServerAuth(boolean useProxyServerAuth)
    {
        _useProxyServerAuth = useProxyServerAuth;
    }

    /**
     * Getter for proxyServerUser
     * @return the proxyServerUser
     */
    public final String getProxyServerUser()
    {
        return _proxyServerUser;
    }

    /**
     * Setter for proxyServerUser
     * @param proxyServerUser the proxyServerUser to set
     */
    public final void setProxyServerUser(String proxyServerUser)
    {
        _proxyServerUser = proxyServerUser;
    }

    /**
     * Getter for proxyServerPort
     * @return the proxyServerPort
     */
    public final String getProxyServerPort()
    {
        return _proxyServerPort;
    }

    /**
     * Setter for proxyServerPort
     * @param proxyServerPort the proxyServerPort to set
     */
    public final void setProxyServerPort(String proxyServerPort)
    {
        _proxyServerPort = proxyServerPort;
    }

    /**
     * Getter for proxyServerPassword
     * @return the proxyServerPassword
     */
    public final String getProxyServerPassword()
    {
        return _proxyServerPassword;
    }

    /**
     * Setter for proxyServerPassword
     * @param proxyServerPassword the proxyServerPassword to set
     */
    public final void setProxyServerPassword(String proxyServerPassword)
    {
        _proxyServerPassword = proxyServerPassword;
    }

    /**
     * @see java.lang.Thread#run()
     * {@inheritDoc}
     */
    public void run()
    {
        Vector errorTileList = new Vector();

        if (getTilesToDownload() == null || getTilesToDownload().size() == 0)
        {
            return;
        }

        if (isUseProxyServer())
        {
            if (isUseProxyServerAuth())
            {
                new ProxyConnection(getProxyServer(), Integer.parseInt(getProxyServerPort()), getProxyServerUser(), getProxyServerPassword());
            }
            else
            {
                new ProxyConnection(getProxyServer(), Integer.parseInt(getProxyServerPort()));
            }

        }

        int errorCount = 0;
        int tileCounter = 0;
        for (Enumeration enumeration = getTilesToDownload().elements(); enumeration.hasMoreElements();)
        {
            String tileToDownload = (String) enumeration.nextElement();
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
            }

            if ((tileCounter < getNumberOfTilesToDownload(getTilesToDownload())) && isWaitAfterTiles())
            {
                if ((tileCounter) % (getWaitAfterTilesAmount()) == 0)
                {
                    try
                    {
                        int waitSeconds = getWaitAfterTilesSeconds();
                        String waitMsg = "Waiting " + waitSeconds + " sec to resume";
                        System.out.println(waitMsg);
                        fireWaitResume(waitMsg);
                        Thread.sleep(waitSeconds * 1000);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }

        }

        fireDownloadCompleteEvent(errorCount, errorTileList);

    }

    /**
     * @param tilesToDownload
     * @return
     */
    public final int getNumberOfTilesToDownload(Vector tilesToDownload)
    {
        if (tilesToDownload == null)
        {
            return 0;
        }
        return tilesToDownload.size();
    }

    private TileDownloadResult doDownload(String tileToDownload, int actDownloadCounter)
    {
        TileDownloadResult result = new TileDownloadResult();

        URL url = null;
        try
        {
            url = new URL(tileToDownload);
        }
        catch (MalformedURLException e)
        {
            result.setCode(TileDownloadResult.CODE_MALFORMED_URL_EXECPTION);
            result.setMessage(TileDownloadResult.MSG_MALFORMED_URL_EXECPTION);
            return result;
        }

        String fileName = getDownloadPath() + File.separator + getFileName(tileToDownload);
        String filePath = getDownloadPath() + File.separator + getFilePath(tileToDownload);

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
     * @param tileToDownload
     * @return
     */
    protected final String getFileName(String tileToDownload)
    {
        int posFileName = tileToDownload.lastIndexOf("/");
        int posTileXIndex = tileToDownload.lastIndexOf("/", posFileName - 1);
        int posZoomLevel = tileToDownload.lastIndexOf("/", posTileXIndex - 1);

        String fileName = tileToDownload.substring(posZoomLevel);

        return fileName;
    }

    /**
     * @param tileToDownload
     * @return
     */
    protected final String getFilePath(String tileToDownload)
    {
        String fileName = getFileName(tileToDownload);
        int posFileName = fileName.lastIndexOf("/");

        String path = fileName.substring(0, posFileName);

        return path;
    }

    /**
     * @param fileName
     * @param url
     * @return 0 = OK,  
     */
    private TileDownloadResult doSingleDownload(String fileName, URL url)
    {
        TileDownloadResult result = new TileDownloadResult();

        HttpURLConnection urlConnection = null;
        try
        {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setUseCaches(false);
            //            long lastModified = urlConnection.getLastModified();

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
     * 
     */
    private void fireDownloadCompleteEvent(int errorCount, Vector errorTileList)
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
            _listener.waitResume(message);
        }
    }

    /**
     * 
     */
    private void fireWaitHttp500ErrorToResume(String message)
    {
        if (_listener != null)
        {
            _listener.waitResume(message);
        }
    }

    /**
     * Getter for waitAfterTiles
     * @return the waitAfterTiles
     */
    public final boolean isWaitAfterTiles()
    {
        return _waitAfterTiles;
    }

    /**
     * Setter for waitAfterTiles
     * @param waitAfterTiles the waitAfterTiles to set
     */
    public final void setWaitAfterTiles(boolean waitAfterTiles)
    {
        _waitAfterTiles = waitAfterTiles;
    }

    /**
     * Getter for waitAfterTilesAmount
     * @return the waitAfterTilesAmount
     */
    public final int getWaitAfterTilesAmount()
    {
        return _waitAfterTilesAmount;
    }

    /**
     * Setter for waitAfterTilesAmount
     * @param waitAfterTilesAmount the waitAfterTilesAmount to set
     */
    public final void setWaitAfterTilesAmount(int waitAfterTilesAmount)
    {
        _waitAfterTilesAmount = waitAfterTilesAmount;
    }

    /**
     * Getter for waitAfterTilesSeconds
     * @return the waitAfterTilesSeconds
     */
    public final int getWaitAfterTilesSeconds()
    {
        return _waitAfterTilesSeconds;
    }

    /**
     * Setter for waitAfterTilesSeconds
     * @param waitAfterTilesSeconds the waitAfterTilesSeconds to set
     */
    public final void setWaitAfterTilesSeconds(int waitAfterTilesSeconds)
    {
        _waitAfterTilesSeconds = waitAfterTilesSeconds;
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
    public void setTilesToDownload(Vector tilesToDownload)
    {
        _tilesToDownload = tilesToDownload;
    }

    /**
     * Getter for tilesToDownload
     * @return the tilesToDownload
     */
    public Vector getTilesToDownload()
    {
        return _tilesToDownload;
    }
}
