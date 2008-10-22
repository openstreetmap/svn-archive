package org.openstreetmap.fma.jtiledownloader.tilelist;

import java.util.Vector;

/**
 * developed by fma (http://wiki.openstreetmap.org/index.php/User:Fma)
 * license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
public class TileListSimple
    implements TileList
{

    Vector _tileList;

    /**
     * @see org.openstreetmap.fma.jtiledownloader.tilelist.TileList#getFileListToDownload()
     * {@inheritDoc}
     */
    public Vector getFileListToDownload()
    {
        return _tileList;
    }

    public void addTile(String tile)
    {
        if (_tileList == null)
        {
            _tileList = new Vector();
        }

        _tileList.add(tile);
    }

    /**
     * @return
     */
    public int getElementCount()
    {
        if (_tileList == null)
        {
            return 0;
        }
        return _tileList.size();
    }

}
