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
 * Osmarender Tile Provider
 */
public class OsmarenderTileProvider
    extends RotatingTileProvider
{
    private final static String[] SUBDOMAINS = { "a", "b", "c" };

    public OsmarenderTileProvider()
    {
        url = "http://{0}.tah.openstreetmap.org/Tiles/tile/";
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.datatypes.RotatingTileProvider#getSubDomains()
     * {@inheritDoc}
     */
    @Override
    protected String[] getSubDomains()
    {
        return SUBDOMAINS;
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.datatypes.GenericTileProvider#getName()
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return "Osmarender";
    }

    /**
     * @see org.openstreetmap.fma.jtiledownloader.datatypes.GenericTileProvider#getMaxZoom()
     * {@inheritDoc}
     */
    @Override
    public int getMaxZoom()
    {
        return 17;
    }
}
