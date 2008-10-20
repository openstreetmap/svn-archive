package org.openstreetmap.fma.jtiledownloader.datatypes;

/**
 * developed by fma (http://wiki.openstreetmap.org/index.php/User:Fma)
 * license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
public class YDirectory
{
    private String[] _tiles;
    private String _name;

    /**
     * Setter for tiles
     * @param tiles the tiles to set
     */
    public void setTiles(String[] tiles)
    {
        _tiles = tiles;
    }

    /**
     * Getter for tiles
     * @return the tiles
     */
    public String[] getTiles()
    {
        return _tiles;
    }

    /**
     * Setter for name
     * @param name the name to set
     */
    public void setName(String name)
    {
        _name = name;
    }

    /**
     * Getter for name
     * @return the name
     */
    public String getName()
    {
        return _name;
    }

}
