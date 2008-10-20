package org.openstreetmap.fma.jtiledownloader;

import junit.framework.TestCase;

import org.openstreetmap.fma.jtiledownloader.tilelist.TileListSquare;

/**
 * developed by fma (http://wiki.openstreetmap.org/index.php/User:Fma)
 * license: http://creativecommons.org/licenses/by-nc-nd/3.0/
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
