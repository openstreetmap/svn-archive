package org.openstreetmap.fma.jtiledownloader;

import junit.framework.TestCase;

import org.openstreetmap.fma.jtiledownloader.tilelist.TileListSquare;

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
public class TileListDownloaderTest
    extends TestCase
{
    TileListDownloader _dl;

    /**
     * @see junit.framework.TestCase#setUp()
     * {@inheritDoc}
     */
    protected void setUp()
        throws Exception
    {
        super.setUp();

        _dl = new TileListDownloader("path", new TileListSquare());
    }

    /**
     * 
     */
    public void testGetFileName()
    {
        assertEquals("/13/12345/9876.png", _dl.getFileName("http://url.osm/a.tah.xxx/13/12345/9876.png"));
    }

    /**
     * 
     */
    public void testGetFilePath()
    {
        assertEquals("/13/12345", _dl.getFilePath("http://url.osm/a.tah.xxx/13/12345/9876.png"));
    }

}
