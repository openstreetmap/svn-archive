/**
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
package org.openstreetmap.fma.jtiledownloader.listener;

import java.util.ArrayList;

import org.openstreetmap.fma.jtiledownloader.datatypes.Tile;
import org.openstreetmap.fma.jtiledownloader.datatypes.TileDownloadError;

public interface TileDownloaderListener
{
    /**
     * @param actCount
     * @param maxCount
     * @param path
     * @param updatedTile 
     */
    void downloadedTile(int actCount, int maxCount, String path, int updatedCount, boolean updatedTile);

    /**
     * @param errorCount
     * @param errorTileList
     * @param updatedTileCount 
     */
    void downloadComplete(int errorCount, ArrayList<TileDownloadError> errorTileList, int updatedTileCount);

    /**
     * @param actCount
     * @param maxCount
     */
    void downloadStopped(int actCount, int maxCount);

    /**
     * @param actCount
     * @param maxCount
     */
    void downloadPaused(int actCount, int maxCount);

    /**
     * @param message
     */
    void setInfo(String message);

    /**
     * @param actCount
     * @param maxCount
     * @param tile
     */
    void errorOccured(int actCount, int maxCount, Tile tile);

}
