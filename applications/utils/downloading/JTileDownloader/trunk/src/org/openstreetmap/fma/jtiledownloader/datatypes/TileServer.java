package org.openstreetmap.fma.jtiledownloader.datatypes;

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
public class TileServer
{
    private String _tileServerUrl;
    private String _tileServerName;

    /**
     * 
     */
    public TileServer()
    {
        super();
        _tileServerUrl = "";
        _tileServerName = "";
    }

    /**
     * @param tileServerUrl
     * @param tileServerName
     */
    public TileServer(String tileServerName, String tileServerUrl)
    {
        super();
        _tileServerUrl = tileServerUrl;
        _tileServerName = tileServerName;
    }

    /**
     * Getter for tileServerUrl
     * @return the tileServerUrl
     */
    public final String getTileServerUrl()
    {
        return _tileServerUrl;
    }

    /**
     * Setter for tileServerUrl
     * @param tileServerUrl the tileServerUrl to set
     */
    public final void setTileServerUrl(String tileServerUrl)
    {
        _tileServerUrl = tileServerUrl;
    }

    /**
     * Getter for tileServerName
     * @return the tileServerName
     */
    public final String getTileServerName()
    {
        return _tileServerName;
    }

    /**
     * Setter for tileServerName
     * @param tileServerName the tileServerName to set
     */
    public final void setTileServerName(String tileServerName)
    {
        _tileServerName = tileServerName;
    }

}
