package org.openstreetmap.josm.plugins.openLayers;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.*;
import java.net.*;
import java.util.EventObject;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.lobobrowser.html.*;
import org.lobobrowser.util.*;
import org.lobobrowser.util.io.IORoutines;
import org.w3c.dom.Document;

/**
 * Cacheable HTTP request
 * <p>
 * This class will perform all HTTP request, caching the responses whenever possible
 */
public class CacheableHttpRequest implements HttpRequest {
    private static final Logger logger = Logger.getLogger(CacheableHttpRequest.class.getName());

    protected final UserAgentContext context;
    protected final Proxy proxy;

    protected boolean isAsync;
    protected String requestMethod;
    protected String requestUserName;
    protected String requestPassword;

    protected URL requestURL;
    protected HttpResponse response;
    protected int readyState;

    /**
     * The <code>URLConnection</code> is assigned to this field while it is
     * ongoing.
     */
    protected URLConnection connection;

    public CacheableHttpRequest(UserAgentContext context, Proxy proxy) {
	this.context = context;
	this.proxy = proxy;
    }

    public synchronized int getReadyState() {
	return this.readyState;
    }

    public synchronized String getResponseText() {
	if( response == null ) return null;
	
	byte[] bytes = this.response.responseBytes;
	String encoding = this.response.encoding;
	
	try {
	    return bytes == null ? null : new String(bytes, encoding);
	} catch (UnsupportedEncodingException uee) {
	    logger.log(Level.WARNING, "getResponseText(): Charset '" + encoding + "' did not work. Retrying with ISO-8859-1.", uee);
	    try {
		return new String(bytes, "ISO-8859-1");
	    } catch (UnsupportedEncodingException uee2) {
		// Ignore this time
		return null;
	    }
	}
    }

    public synchronized Document getResponseXML() {
	if( response == null ) return null;

	byte[] bytes = this.response.responseBytes;
	if (bytes == null) return null;

	InputStream in = new ByteArrayInputStream(bytes);
	try {
	    return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
	} catch (Exception err) {
	    logger.log(Level.WARNING, "Unable to parse response as XML.", err);
	    return null;
	}
    }

    public synchronized byte[] getResponseBytes() {
	if( response == null ) return null;
	return this.response.responseBytes;
    }

    public synchronized Image getResponseImage() {
	if( response == null ) return null;

	byte[] bytes = this.response.responseBytes;
	if (bytes == null) return null;

	return Toolkit.getDefaultToolkit().createImage(bytes);
    }

    public synchronized int getStatus() {
	if( response == null ) return 0;
	return this.response.status;
    }

    public synchronized String getStatusText() {
	if( response == null ) return null;
	return this.response.statusText;
    }

    public synchronized String getAllResponseHeaders() {
	if( response == null ) return null;
	return this.response.responseHeaders;
    }

    public synchronized String getResponseHeader(String headerName) {
	if( response == null ) return null;
	Map headers = this.response.responseHeadersMap;
	return headers == null ? null : (String) headers.get(headerName);
    }

    public void open(String method, String url) throws IOException {
	this.open(method, url, true);
    }

    public void open(String method, URL url) throws IOException {
	this.open(method, url, true, null, null);
    }

    public void open(String method, URL url, boolean asyncFlag) throws IOException {
	this.open(method, url, asyncFlag, null, null);
    }

    public void open(String method, String url, boolean asyncFlag) throws IOException {
	URL urlObj = Urls.createURL(null, url);
	this.open(method, urlObj, asyncFlag, null);
    }

    public void open(String method, URL url, boolean asyncFlag, String userName) throws IOException {
	this.open(method, url, asyncFlag, userName, null);
    }

    public void abort() {
	URLConnection c;
	synchronized (this) {
	    c = this.connection;
	    response = null;
	}
	if (c instanceof HttpURLConnection) {
	    ((HttpURLConnection) c).disconnect();
	} else if (c != null) {
	    try {
		c.getInputStream().close();
	    } catch (IOException ioe) {
		ioe.printStackTrace();
	    }
	}
    }

    /**
     * Opens the request. Call {@link #send(String)} to complete it.
     * 
     * @param method The request method.
     * @param url The request URL.
     * @param asyncFlag Whether the request should be asynchronous.
     * @param userName The user name of the request (not supported.)
     * @param password The password of the request (not supported.)
     * @throws IOException If any error
     */
    public void open(final String method, final URL url, boolean asyncFlag, final String userName, final String password) throws IOException {
	this.abort();

	HttpResponse response = HttpResponse.lookup(url);
	URLConnection c = null;
	
	if( response == null )
	{
	    c = proxy == null || proxy == Proxy.NO_PROXY ? url.openConnection() : url.openConnection(proxy);
	    response = new HttpResponse();
	}
	    
	synchronized (this) {
	    this.connection = c;
	    this.isAsync = asyncFlag;
	    this.requestMethod = method;
	    this.requestUserName = userName;
	    this.requestPassword = password;
	    this.requestURL = url;
	    this.response = response;
	    
	    if( response.loaded )
		changeState(HttpRequest.STATE_LOADING);
	    else
		changeState(HttpRequest.STATE_LOADING, 0, null, null);
	}
    }

    /**
     * Sends POST content, if any, and causes the request to proceed.
     * <p>
     * In the case of asynchronous requests, a new thread is created.
     * 
     * @param content POST content or <code>null</code> if there's no such
     *        content.
     */
    public void send(final String content) throws IOException {
	final URL url = this.requestURL;
	if (url == null) {
	    throw new IOException("No URL has been provided.");
	}
	if (this.isAsync) {
	    // Should use a thread pool instead
	    new Thread("SimpleHttpRequest-" + url.getHost()) {
		@Override
        public void run() {
		    try {
			sendSync(content);
		    } catch (Throwable thrown) {
			logger.log(Level.WARNING,"send(): Error in asynchronous request on " + url, thrown);
		    }
		}
	    }.start();
	} else {
	    sendSync(content);
	}
    }

    /**
     * This is the charset used to post data provided to {@link #send(String)}.
     * It returns "UTF-8" unless overridden.
     */
    protected String getPostCharset() {
	return "UTF-8";
    }

    /**
     * This is a synchronous implementation of {@link #send(String)} method
     * functionality. It may be overridden to change the behavior of the class.
     * 
     * @param content POST content if any. It may be <code>null</code>.
     * @throws IOException
     */
    protected void sendSync(String content) throws IOException {
	if( response.loaded )
	{
	    // Response from cache
	    changeState(HttpRequest.STATE_LOADED);
	    changeState(HttpRequest.STATE_INTERACTIVE);
	    changeState(HttpRequest.STATE_COMPLETE);
	    return;
	}
	
	try {
	    URLConnection c;
	    synchronized (this) {
		c = this.connection;
	    }
	    c.setRequestProperty("User-Agent", this.context.getUserAgent());
	    int istatus = 0;
	    String istatusText = "";
	    InputStream err = null;
	    
	    if (c instanceof HttpURLConnection) {
		HttpURLConnection hc = (HttpURLConnection) c;
		String method = requestMethod.toUpperCase();
		hc.setRequestMethod(method);
		if ("POST".equals(method) && content != null) {
		    hc.setDoOutput(true);
		    byte[] contentBytes = content.getBytes(this.getPostCharset());
		    hc.setFixedLengthStreamingMode(contentBytes.length);
		    OutputStream out = hc.getOutputStream();
		    try {
			out.write(contentBytes);
		    } finally {
			out.flush();
		    }
		}
		istatus = hc.getResponseCode();
		istatusText = hc.getResponseMessage();
		err = hc.getErrorStream();
	    }

	    response.setConnectionInfo(c);
	    changeState(HttpRequest.STATE_LOADED, istatus, istatusText, null);
	    InputStream in = err == null ? c.getInputStream() : err;
	    int contentLength = c.getContentLength();
	    changeState(HttpRequest.STATE_INTERACTIVE, istatus, istatusText, null);
	    byte[] bytes = IORoutines.load(in, contentLength == -1 ? 4096 : contentLength);
	    changeState(HttpRequest.STATE_COMPLETE, istatus, istatusText, bytes);
	    response.store(requestURL);
	} finally {
	    synchronized (this) {
		this.connection = null;
	    }
	}
    }

    private final EventDispatch readyEvent = new EventDispatch();

    public void addReadyStateChangeListener( final ReadyStateChangeListener listener) {
	readyEvent.addListener(new GenericEventListener() {
	    public void processEvent(EventObject event) {
		listener.readyStateChanged();
	    }
	});
    }
    
    protected void changeState(int readyState, int status, String statusMessage, byte[] bytes) {
        synchronized (this) {
            this.readyState = readyState;
            this.response.changeState(status, statusMessage, bytes);
        }
        readyEvent.fireEvent(null);
    }

    protected void changeState(int readyState) {
        synchronized (this) {
            this.readyState = readyState;
        }
        readyEvent.fireEvent(null);
    }

}
