package org.openstreetmap.fma.jtiledownloader.tilelist;

import java.util.Vector;

/**
 * developed by fma (http://wiki.openstreetmap.org/index.php/User:Fma)
 * license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
public interface TileList
{

    /**
     * @return {@link Vector} containing Strings
     */
    public abstract Vector getFileListToDownload();

}