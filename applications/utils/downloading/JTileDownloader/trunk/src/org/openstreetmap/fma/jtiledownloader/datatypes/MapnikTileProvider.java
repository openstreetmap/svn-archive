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

package org.openstreetmap.fma.jtiledownloader.datatypes;

/**
 * Mapnik Tile Provider
 */
public class MapnikTileProvider
    extends RotatingTileProvider
{
    private final static String[] SUBDOMAINS = { "a", "b", "c" };

    public MapnikTileProvider()
    {
        url = "http://{0}.tile.openstreetmap.org/";
    }

    @Override
    protected String[] getSubDomains()
    {
        return SUBDOMAINS;
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.datatypes.GenericTileProvider#getName()
     */
    @Override
    public String getName()
    {
        return "Mapnik";
    }
}
