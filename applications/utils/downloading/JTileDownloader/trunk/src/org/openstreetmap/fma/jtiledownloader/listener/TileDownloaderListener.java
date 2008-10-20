package org.openstreetmap.fma.jtiledownloader.listener;

import java.util.Vector;

/**
 * developed by fma (http://wiki.openstreetmap.org/index.php/User:Fma)
 * license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
public interface TileDownloaderListener
{
    /**
     * @param actCount
     * @param maxCount
     * @param path
     */
    void downloadedTile(int actCount, int maxCount, String path);

    /**
    * 
    */
    void downloadComplete(int errorCount, Vector errorTileList);

    /**
     * 
     */
    void waitResume(String message);

    /**
     * 
     */
    void waitWaitHttp500ErrorToResume(String message);

}
