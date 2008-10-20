package org.openstreetmap.fma.jtiledownloader.datatypes;

/**
 * developed by fma (http://wiki.openstreetmap.org/index.php/User:Fma)
 * license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
public class TileDownloadError
{
    private String _tile = "";
    private TileDownloadResult result = new TileDownloadResult();

    /**
     * Setter for tile
     * @param tile the tile to set
     */
    public void setTile(String tile)
    {
        _tile = tile;
    }

    /**
     * Getter for tile
     * @return the tile
     */
    public String getTile()
    {
        return _tile;
    }

    /**
     * Setter for result
     * @param result the result to set
     */
    public void setResult(TileDownloadResult result)
    {
        this.result = result;
    }

    /**
     * Getter for result
     * @return the result
     */
    public TileDownloadResult getResult()
    {
        return result;
    }

}
