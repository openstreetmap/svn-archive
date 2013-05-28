/*
 * Copyright 2009 - 2010, Sven Strickroth <email@cs-ware.de>
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

package org.openstreetmap.fma.jtiledownloader;

import org.openstreetmap.fma.jtiledownloader.datatypes.*;

public class TileProviderList
{
    private TileProviderIf[] tileProviders;

    /**
     * Sets up the tileProviderList
     */
    public TileProviderList()
    {
        super();
        tileProviders = new TileProviderIf[] {
            new MapnikTileProvider(),
            new ThunderforestTileProvider("OpenCycleMap", "tile.opencyclemap.org/cycle/"),
            new ThunderforestTileProvider("Thunderforest Transport", "tile2.opencyclemap.org/transport/"),
            new ThunderforestTileProvider("Thunderforest Landscape", "tile3.opencyclemap.org/landscape/"),
            new ThunderforestTileProvider("Thunderforest Outdoors", "tile.thunderforest.com/outdoors/"),
            new GenericTileProvider("OpenStreetBrowser (Europe)", "http://www.openstreetbrowser.org/tiles/base/"),
            new GenericTileProvider("OpenPisteMap", "http://openpistemap.org/tiles/contours/"),
            new MapSurferTileProvider(),
            new MapSurferProfileTileProvider(),
            new OsmFrTileProvider(),
            new GenericTileProvider("CloudMade Web style", "http://tile.cloudmade.com/8bafab36916b5ce6b4395ede3cb9ddea/1/256/"),
            new GenericTileProvider("CloudMade Mobile style", "http://tile.cloudmade.com/8bafab36916b5ce6b4395ede3cb9ddea/2/256/"),
            new GenericTileProvider("CloudMade NoNames style", "http://tile.cloudmade.com/8bafab36916b5ce6b4395ede3cb9ddea/3/256/"),
            new GenericTileProvider("CloudMade Night style", "http://tile.cloudmade.com/BC9A493B41014CAABB98F0471D759707/999/256/")
        };
    }

    public TileProviderIf[] getTileProviderList()
    {
        return tileProviders.clone();
    }
}
