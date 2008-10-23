package org.openstreetmap.fma.jtiledownloader;

import org.openstreetmap.fma.jtiledownloader.datatypes.TileServer;

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
public class TileServerList
{
    private TileServer[] _tileServers;

    /**
     * 
     */
    public TileServerList()
    {
        super();

        _tileServers = new TileServer[5];

        _tileServers[0] = new TileServer("Osmarender", "http://tah.openstreetmap.org/Tiles/tile/");
        _tileServers[1] = new TileServer("Mapnik", "http://tile.openstreetmap.org/mapnik/");
        _tileServers[2] = new TileServer("Cyclemap (Cloudmade)", "http://c.andy.sandbox.cloudmade.com/tiles/cycle/");
        _tileServers[3] = new TileServer("Cyclemap (Thunderflames)", "http://thunderflames.org/tiles/cycle/");
        _tileServers[4] = new TileServer("OpenPisteMap", "http://openpistemap.org/tiles/contours/");
    }

    public TileServer[] getTileServerList()
    {

        return _tileServers;
    }
}
