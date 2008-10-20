package org.openstreetmap.fma.jtiledownloader.datatypes;

import java.util.Vector;

/**
 * developed by fma (http://wiki.openstreetmap.org/index.php/User:Fma)
 * license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
public class UpdateTileList
{
    private String _zoomLevel;
    private Vector _yDirectory;

    public void addYDirectory(YDirectory yDirectory)
    {
        if (_yDirectory == null)
        {
            _yDirectory = new Vector();
        }
        _yDirectory.add(yDirectory);
    }

    /**
     * Getter for yDirectory
     * @return the yDirectory
     */
    protected final Vector getYDirectory()
    {
        return _yDirectory;
    }

    public int getFileCount()
    {
        if (_yDirectory == null)
        {
            return 0;
        }

        int count = 0;
        for (int index = 0; index < _yDirectory.size(); index++)
        {
            YDirectory yDir = (YDirectory) _yDirectory.elementAt(index);
            if (yDir.getTiles() != null)
            {
                count += yDir.getTiles().length;
            }
        }

        return count;
    }

    /**
     * Setter for zoomLevel
     * @param zoomLevel the zoomLevel to set
     */
    public void setZoomLevel(String zoomLevel)
    {
        _zoomLevel = zoomLevel;
    }

    /**
     * Getter for zoomLevel
     * @return the zoomLevel
     */
    public String getZoomLevel()
    {
        return _zoomLevel;
    }

}
