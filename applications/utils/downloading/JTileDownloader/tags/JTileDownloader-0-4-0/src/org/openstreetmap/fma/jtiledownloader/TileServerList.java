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

        _tileServers = new TileServer[9];

        _tileServers[0] = new TileServer("Osmarender", "http://tah.openstreetmap.org/Tiles/tile/");
        _tileServers[1] = new TileServer("Mapnik", "http://tile.openstreetmap.org/mapnik/");
        _tileServers[2] = new TileServer("Cyclemap (CloudMade)", "http://c.andy.sandbox.cloudmade.com/tiles/cycle/");
        _tileServers[3] = new TileServer("Cyclemap (Thunderflames)", "http://thunderflames.org/tiles/cycle/");
        _tileServers[4] = new TileServer("OpenPisteMap", "http://openpistemap.org/tiles/contours/");
        _tileServers[5] = new TileServer("Maplint", "http://tah.openstreetmap.org/Tiles/maplint/");
        _tileServers[6] = new TileServer("CloudMade Web style", "http://tile.cloudmade.com/8bafab36916b5ce6b4395ede3cb9ddea/1/256/");
        _tileServers[7] = new TileServer("CloudMade Mobile style", "http://tile.cloudmade.com/8bafab36916b5ce6b4395ede3cb9ddea/2/256/");
        _tileServers[8] = new TileServer("CloudMade NoNames style", "http://tile.cloudmade.com/8bafab36916b5ce6b4395ede3cb9ddea/3/256/");
    }

    public TileServer[] getTileServerList()
    {

        return _tileServers;
    }
}
