/*
 * Copyright 2009, Sven Strickroth <email@cs-ware.de>
 * 
 * This file is part of JTileDownloader.
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

import java.util.Arrays;
import java.util.LinkedList;

import org.openstreetmap.fma.jtiledownloader.datatypes.GenericTileProvider;
import org.openstreetmap.fma.jtiledownloader.datatypes.TileProviderIf;

/**
 * Class with helper methods
 */
public class Util
{
    /**
     * Returns valid array of zoomlevels to download
     * @return int[] zoomlevels
     */
    public static int[] getOutputZoomLevelArray(TileProviderIf selectedTileProvider, String zoomLevelString)
    {
        LinkedList<Integer> zoomLevels = new LinkedList<Integer>();
        for (String zoomLevel : Arrays.asList(zoomLevelString.split(",")))
        {
            int selectedZoom = Integer.parseInt(zoomLevel.trim());
            if (selectedZoom <= selectedTileProvider.getMaxZoom() && selectedZoom >= selectedTileProvider.getMinZoom())
            {
                if (!zoomLevels.contains(selectedZoom))
                {
                    zoomLevels.add(selectedZoom);
                }
            }
        }
        int[] parsedLevels = new int[zoomLevels.size()];
        for (int i = 0; i < zoomLevels.size(); i++)
        {
            parsedLevels[i] = zoomLevels.get(i);
        }
        return parsedLevels;
    }

    /**
     * @param tileServer
     * @return tileProvider
     */
    public static TileProviderIf getTileProvider(String tileServer)
    {
        TileProviderIf[] _tileProviders = new TileProviderList().getTileProviderList();
        for (TileProviderIf tileProvider : _tileProviders)
        {
            if (tileProvider.getName().equalsIgnoreCase(tileServer))
            {
                return tileProvider;
            }
        }
        return new GenericTileProvider(tileServer);
    }

}
