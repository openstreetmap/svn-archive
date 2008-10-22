package org.openstreetmap.fma.jtiledownloader;

import org.openstreetmap.fma.jtiledownloader.datatypes.TileServer;

/**
 * 
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
