/*
 * Copyright 2008, Friedrich Maier
 * 
 * This file is part of JTileDownloader.
 * (see http://wiki.openstreetmap.org/index.php/JTileDownloader)
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

public class TileDownloadResult
{
    public static final int CODE_OK = 0;
    public static final String MSG_OK = "OK";

    public static final int CODE_FILENOTFOUND = 1;
    public static final String MSG_FILENOTFOUND = "File not found";

    public static final int CODE_MALFORMED_URL_EXECPTION = 2;
    public static final String MSG_MALFORMED_URL_EXECPTION = "MalformedURLException";

    public static final int CODE_UNKNOWN_HOST_EXECPTION = 3;
    public static final String MSG_UNKNOWN_HOST_EXECPTION = "Unknown Host";

    public static final int CODE_HTTP_500 = 500;
    public static final String MSG_HTTP_500 = "Http 500 Error";

    public static final int CODE_UNKNOWN_ERROR = 9999;
    public static final String MSG_UNKNOWN_ERROR = "Not specified";

    private int _code = CODE_OK;
    private String _message = "";
    private boolean _updatedTile = false;

    /**
     * Setter for code
     * @param code the code to set
     */
    public void setCode(int code)
    {
        _code = code;
    }

    /**
     * Getter for code
     * @return the code
     */
    public int getCode()
    {
        return _code;
    }

    /**
     * Setter for message
     * @param message the message to set
     */
    public void setMessage(String message)
    {
        _message = message;
    }

    /**
     * Getter for message
     * @return the message
     */
    public String getMessage()
    {
        return _message;
    }

    /**
     * Setter for updatedTile
     * @param updatedTile the updatedTile to set
     */
    public void setUpdatedTile(boolean updatedTile)
    {
        _updatedTile = updatedTile;
    }

    /**
     * Getter for updatedTile
     * @return the updatedTile
     */
    public boolean isUpdatedTile()
    {
        return _updatedTile;
    }

}
