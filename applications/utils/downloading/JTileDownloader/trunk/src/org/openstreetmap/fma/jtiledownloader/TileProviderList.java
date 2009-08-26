/*
 * Copyright 2009, Sven Strickroth <email@cs-ware.de>
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

import org.openstreetmap.fma.jtiledownloader.datatypes.GenericTileProvider;
import org.openstreetmap.fma.jtiledownloader.datatypes.MapnikTileProvider;
import org.openstreetmap.fma.jtiledownloader.datatypes.OsmarenderTileProvider;
import org.openstreetmap.fma.jtiledownloader.datatypes.TileProviderIf;

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
            new OsmarenderTileProvider(),
            new MapnikTileProvider(),
            new GenericTileProvider("Cyclemap (CloudMade)", "http://c.andy.sandbox.cloudmade.com/tiles/cycle/"),
            new GenericTileProvider("Cyclemap (Thunderflames)", "http://thunderflames.org/tiles/cycle/"),
            new GenericTileProvider("OpenStreetBrowser (Europe)", "http://www.openstreetbrowser.org/tiles/base/"),
            new GenericTileProvider("OpenPisteMap", "http://openpistemap.org/tiles/contours/"),
            new GenericTileProvider("Maplint", "http://tah.openstreetmap.org/Tiles/maplint/"),
            new GenericTileProvider("CloudMade Web style", "http://tile.cloudmade.com/8bafab36916b5ce6b4395ede3cb9ddea/1/256/"),
            new GenericTileProvider("CloudMade Mobile style", "http://tile.cloudmade.com/8bafab36916b5ce6b4395ede3cb9ddea/2/256/"),
            new GenericTileProvider("CloudMade NoNames style", "http://tile.cloudmade.com/8bafab36916b5ce6b4395ede3cb9ddea/3/256/")
        };
    }

    public TileProviderIf[] getTileProviderList()
    {
        return tileProviders.clone();
    }
}
