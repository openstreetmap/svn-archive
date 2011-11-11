/*
 * Copyright 2009, Sven Strickroth <email@cs-ware.de>
 * 
 * parsePasteUrl by:
 * Copyright 2008, Friedrich Maier
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

import java.util.LinkedList;

import org.openstreetmap.fma.jtiledownloader.datatypes.GenericTileProvider;
import org.openstreetmap.fma.jtiledownloader.datatypes.TileProviderIf;
import org.openstreetmap.fma.jtiledownloader.tilelist.TileListUrlSquare;

/**
 * Class with helper methods
 */
public class Util
{
    /**
     * Returns valid array of zoomlevels to download
     * @param selectedTileProvider 
     * @param zoomLevelString 
     * @return int[] zoomlevels
     */
    public static int[] getOutputZoomLevelArray(TileProviderIf selectedTileProvider, String zoomLevelString)
    {
        int minZoom = selectedTileProvider == null ? 0 : selectedTileProvider.getMinZoom();
        int maxZoom = selectedTileProvider == null ? 20 : selectedTileProvider.getMaxZoom();
        LinkedList<Integer> zoomLevels = new LinkedList<Integer>();
        for (String zoomLevel : zoomLevelString.split(","))
        {
            int z1, z2;
            int p = zoomLevel.indexOf('-');
            if( p > 0 ) {
                z1 = Integer.parseInt(zoomLevel.substring(0, p).trim());
                z2 = Integer.parseInt(zoomLevel.substring(p + 1).trim());
            } else {
                z1 = Integer.parseInt(zoomLevel.trim());
                z2 = z1;
            }
            for( int selectedZoom = z1; selectedZoom <= z2; selectedZoom ++ ) {
                if (selectedZoom <= maxZoom && selectedZoom >= minZoom)
                {
                    if (!zoomLevels.contains(selectedZoom))
                    {
                        zoomLevels.add(selectedZoom);
                    }
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

    public static void parsePasteUrl(String url, TileListUrlSquare tileList)
    {
        //String pasteUrl = "http://www.openstreetmap.org/?lat=48.256&lon=13.0434&zoom=12&layers=0B0FT";
        if (url == null || url.length() == 0)
        {
            tileList.setLatitude(0);
            tileList.setLongitude(0);
            return;
        }

        try {
            int posLat = url.indexOf("lat=");
            String lat = url.substring(posLat);
            int posLon = url.indexOf("lon=");
            String lon = url.substring(posLon);

            int posAnd = lat.indexOf("&");
            lat = lat.substring(4, posAnd).replace(',', '.');
            posAnd = lon.indexOf("&");
            lon = lon.substring(4, posAnd).replace(',', '.');

            if( lat.length() > 0 && lon.length() > 0 ) {
                tileList.setLatitude(Double.parseDouble(lat));
                tileList.setLongitude(Double.parseDouble(lon));
            }
        } catch( NumberFormatException e ) {
            tileList.setLatitude(0);
            tileList.setLongitude(0);
        }
    }
}
