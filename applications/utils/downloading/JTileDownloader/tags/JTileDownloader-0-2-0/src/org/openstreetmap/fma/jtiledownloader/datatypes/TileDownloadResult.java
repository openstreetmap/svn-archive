package org.openstreetmap.fma.jtiledownloader.datatypes;

/**
 * developed by fma (http://wiki.openstreetmap.org/index.php/User:Fma)
 * license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
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

}
