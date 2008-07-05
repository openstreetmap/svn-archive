/**
 * 
 */
package org.openstreetmap.josm.plugins.openLayers;

import java.io.Serializable;
import java.net.*;
import java.util.List;
import java.util.Map;

import org.lobobrowser.util.Urls;

/**
 * The response from a connection
 */
public class HttpResponse implements Serializable {
    private static final long serialVersionUID = -8605486951415515445L;
    
    /** The status of the response */
    protected int status;
    /** The status text of the response */
    protected String statusText;
    /** The content of the response */
    protected byte[] responseBytes;
    /** The encoding of the response */
    protected String encoding;
    /** Whether this response has been already loaded */
    protected boolean loaded = false;
    
    /** Response headers are set in this map after a response is received. */
    protected Map<String, List<String>> responseHeadersMap;

    /** Response headers are set in this string after a response is received. */
    protected String responseHeaders;
    
    /**
     * Sets the information about this response: headers and encoding
     * @param c The connection
     */
    public synchronized void setConnectionInfo(URLConnection c)
    {
        encoding = Urls.getCharset(c);
        if (encoding == null)
            encoding = "ISO-8859-1";
        
        responseHeaders = getAllResponseHeaders(c);
        responseHeadersMap = c.getHeaderFields();
    }

    /**
     * Sets the state of this response
     * 
     * @param status The response status
     * @param statusMessage The status message
     * @param bytes The response bytes
     */
    public synchronized void changeState(int status, String statusMessage, byte[] bytes) {
	this.status = status;
        this.statusText = statusMessage;
        this.responseBytes = bytes;
    }
        
    /**
     * Returns the headers of the connection as a String
     * @param c The connection
     * @return All the headers as a String
     */
    private String getAllResponseHeaders(URLConnection c) {
        int idx = 0;
	String value;
	StringBuffer buf = new StringBuffer();
	while((value = c.getHeaderField(idx)) != null) {
	    String key = c.getHeaderFieldKey(idx);
	    if( key != null )
	    {
		buf.append(key);
		buf.append("=");
	    }
	    buf.append(value);
	    buf.append("\n");
	    idx++;
	}
	return buf.toString();
    }

    /**
     * Stores this response in cache
     * @param requestURL The URL requested
     */
    public void store(URL requestURL) {
	loaded = true;
	StorageManager.getInstance().put(requestURL, this);
    }
    
    /**
     * Looks up the requested URL in the cache
     * @param requestURL The requested URL 
     * @return The response, if available
     */
    public static HttpResponse lookup(URL requestURL) {
	return StorageManager.getInstance().get(requestURL);
    }
}