//License: GPL. See README for details.
package org.openstreetmap.hot.sds;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.OsmApiException;
import org.openstreetmap.josm.io.OsmTransferCanceledException;
import org.openstreetmap.josm.io.ProgressInputStream;
import org.openstreetmap.josm.tools.CheckParameterUtil;

/**
 * Class that encapsulates the communications with the SDS API.
 *
 * This is modeled after JOSM's own OsmAPI class.
 * 
 */
public class SdsApi extends SdsConnection {
    /** max number of retries to send a request in case of HTTP 500 errors or timeouts */
    static public final int DEFAULT_MAX_NUM_RETRIES = 5;

    /** the collection of instantiated OSM APIs */
    private static HashMap<String, SdsApi> instances = new HashMap<String, SdsApi>();
    
    /**
     * replies the {@see OsmApi} for a given server URL
     *
     * @param serverUrl  the server URL
     * @return the OsmApi
     * @throws IllegalArgumentException thrown, if serverUrl is null
     *
     */
    static public SdsApi getSdsApi(String serverUrl) {
        SdsApi api = instances.get(serverUrl);
        if (api == null) {
            api = new SdsApi(serverUrl);
            instances.put(serverUrl,api);
        }
        return api;
    }
    /**
     * replies the {@see OsmApi} for the URL given by the preference <code>sds-server.url</code>
     *
     * @return the OsmApi
     *
     */
    static public SdsApi getSdsApi() {
        String serverUrl = Main.pref.get("sds-server.url", "http://datastore.hotosm.org");
        if (serverUrl == null)
            throw new IllegalStateException(tr("Preference ''{0}'' missing. Cannot initialize SdsApi.", "sds-server.url"));
        return getSdsApi(serverUrl);
    }

    /** the server URL */
    private String serverUrl;

    /**
     * API version used for server communications
     */
    private String version = null;

    /**
     * creates an OSM api for a specific server URL
     *
     * @param serverUrl the server URL. Must not be null
     * @exception IllegalArgumentException thrown, if serverUrl is null
     */
    protected SdsApi(String serverUrl)  {
        CheckParameterUtil.ensureParameterNotNull(serverUrl, "serverUrl");
        this.serverUrl = serverUrl;
    }

    /**
     * Returns the OSM protocol version we use to talk to the server.
     * @return protocol version, or null if not yet negotiated.
     */
    public String getVersion() {
        return version;
    }


    /**
     * Returns the base URL for API requests, including the negotiated version number.
     * @return base URL string
     */
    public String getBaseUrl() {
        StringBuffer rv = new StringBuffer(serverUrl);
        if (version != null) {
            rv.append("/");
            rv.append(version);
        }
        rv.append("/");
        // this works around a ruby (or lighttpd) bug where two consecutive slashes in
        // an URL will cause a "404 not found" response.
        int p; while ((p = rv.indexOf("//", 6)) > -1) { rv.delete(p, p + 1); }
        return rv.toString();
    }

    /**
     * Creates an OSM primitive on the server. The OsmPrimitive object passed in
     * is modified by giving it the server-assigned id.
     *
     * @param osm the primitive
     * @throws SdsTransferException if something goes wrong
     
    public void createPrimitive(IPrimitive osm, ProgressMonitor monitor) throws SdsTransferException {
        String ret = "";
        try {
            ensureValidChangeset();
            initialize(monitor);
            ret = sendRequest("PUT", OsmPrimitiveType.from(osm).getAPIName()+"/create", toXml(osm, true),monitor);
            osm.setOsmId(Long.parseLong(ret.trim()), 1);
            osm.setChangesetId(getChangeset().getId());
        } catch(NumberFormatException e){
            throw new SdsTransferException(tr("Unexpected format of ID replied by the server. Got ''{0}''.", ret));
        }
    }
    */

    /**
     * Modifies an OSM primitive on the server.
     *
     * @param osm the primitive. Must not be null.
     * @param monitor the progress monitor
     * @throws SdsTransferException if something goes wrong
     
    public void modifyPrimitive(IPrimitive osm, ProgressMonitor monitor) throws SdsTransferException {
        String ret = null;
        try {
            ensureValidChangeset();
            initialize(monitor);
            // normal mode (0.6 and up) returns new object version.
            ret = sendRequest("PUT", OsmPrimitiveType.from(osm).getAPIName()+"/" + osm.getId(), toXml(osm, true), monitor);
            osm.setOsmId(osm.getId(), Integer.parseInt(ret.trim()));
            osm.setChangesetId(getChangeset().getId());
            osm.setVisible(true);
        } catch(NumberFormatException e) {
            throw new SdsTransferException(tr("Unexpected format of new version of modified primitive ''{0}''. Got ''{1}''.", osm.getId(), ret));
        }
    }
    */

    /**
     * Deletes an OSM primitive on the server.
     * @param osm the primitive
     * @throws SdsTransferException if something goes wrong
     
    public void deletePrimitive(IPrimitive osm, ProgressMonitor monitor) throws SdsTransferException {
        ensureValidChangeset();
        initialize(monitor);
        // can't use a the individual DELETE method in the 0.6 API. Java doesn't allow
        // submitting a DELETE request with content, the 0.6 API requires it, however. Falling back
        // to diff upload.
        //
        uploadDiff(Collections.singleton(osm), monitor.createSubTaskMonitor(ProgressMonitor.ALL_TICKS, false));
    }
    */

    /**
     * Uploads a list of changes in "diff" form to the server.
     *
     * @param list the list of changed OSM Primitives
     * @param  monitor the progress monitor
     * @return 
     * @return list of processed primitives
     * @throws SdsTransferException 
     * @throws SdsTransferException if something is wrong
     
    public Collection<IPrimitive> uploadDiff(Collection<? extends IPrimitive> list, ProgressMonitor monitor) throws SdsTransferException {
        try {
            monitor.beginTask("", list.size() * 2);
            if (changeset == null)
                throw new SdsTransferException(tr("No changeset present for diff upload."));

            initialize(monitor);

            // prepare upload request
            //
            OsmChangeBuilder changeBuilder = new OsmChangeBuilder(changeset);
            monitor.subTask(tr("Preparing upload request..."));
            changeBuilder.start();
            changeBuilder.append(list);
            changeBuilder.finish();
            String diffUploadRequest = changeBuilder.getDocument();

            // Upload to the server
            //
            monitor.indeterminateSubTask(
                    trn("Uploading {0} object...", "Uploading {0} objects...", list.size(), list.size()));
            String diffUploadResponse = sendRequest("POST", "changeset/" + changeset.getId() + "/upload", diffUploadRequest,monitor);

            // Process the response from the server
            //
            DiffResultProcessor reader = new DiffResultProcessor(list);
            reader.parse(diffUploadResponse, monitor.createSubTaskMonitor(ProgressMonitor.ALL_TICKS, false));
            return reader.postProcess(
                    getChangeset(),
                    monitor.createSubTaskMonitor(ProgressMonitor.ALL_TICKS, false)
            );
        } catch(SdsTransferException e) {
            throw e;
        } catch(OsmDataParsingException e) {
            throw new SdsTransferException(e);
        } finally {
            monitor.finishTask();
        }
    }
    */
    
    public String requestShadowsFromSds(List<Long> nodes, List<Long> ways, List<Long> relations, ProgressMonitor pm) throws SdsTransferException {
    	
    	StringBuilder request = new StringBuilder();
    	String delim = "";
    	String comma = "";
    	
    	if (nodes != null && !nodes.isEmpty()) {
    		request.append(delim);
    		delim = "&";
    		comma = "";
    		request.append("nodes=");
    		for (long i : nodes) {
    			request.append(comma);
    			comma = ",";
    			request.append(i);
    		}
    	}
    	if (ways != null && !ways.isEmpty()) {
    		request.append(delim);
    		delim = "&";
    		comma = "";
    		request.append("ways=");
    		for (long i : ways) {
    			request.append(comma);
    			comma = ",";
    			request.append(i);
    		}
    	}
    	if (relations != null && !relations.isEmpty()) {
    		request.append(delim);
    		delim = "&";
    		comma = "";
    		request.append("relations=");
    		for (long i : relations) {
    			request.append(comma);
    			comma = ",";
    			request.append(i);
    		}
    	}
    	
    	return sendRequest("POST", "collectshadows", request.toString(), pm ,true);
   
    }

    private void sleepAndListen(int retry, ProgressMonitor monitor) throws SdsTransferException {
        System.out.print(tr("Waiting 10 seconds ... "));
        for(int i=0; i < 10; i++) {
            if (monitor != null) {
                monitor.setCustomText(tr("Starting retry {0} of {1} in {2} seconds ...", getMaxRetries() - retry,getMaxRetries(), 10-i));
            }
            if (cancel)
                throw new SdsTransferException();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {}
        }
        System.out.println(tr("OK - trying again."));
    }

    /**
     * Replies the max. number of retries in case of 5XX errors on the server
     *
     * @return the max number of retries
     */
    protected int getMaxRetries() {
        int ret = Main.pref.getInteger("osm-server.max-num-retries", DEFAULT_MAX_NUM_RETRIES);
        return Math.max(ret,0);
    }

    private String sendRequest(String requestMethod, String urlSuffix, String requestBody, ProgressMonitor monitor) throws SdsTransferException {
        return sendRequest(requestMethod, urlSuffix, requestBody, monitor, true, false);
    }

    private String sendRequest(String requestMethod, String urlSuffix, String requestBody, ProgressMonitor monitor, boolean doAuth) throws SdsTransferException {
        return sendRequest(requestMethod, urlSuffix, requestBody, monitor, doAuth, false);
    }
    
    public boolean updateSds(String message, ProgressMonitor pm) {
    	try {
			sendRequest("POST", "createshadows", message, pm);
		} catch (SdsTransferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return true;
    }

    /**
     * Generic method for sending requests to the OSM API.
     *
     * This method will automatically re-try any requests that are answered with a 5xx
     * error code, or that resulted in a timeout exception from the TCP layer.
     *
     * @param requestMethod The http method used when talking with the server.
     * @param urlSuffix The suffix to add at the server url, not including the version number,
     *    but including any object ids (e.g. "/way/1234/history").
     * @param requestBody the body of the HTTP request, if any.
     * @param monitor the progress monitor
     * @param doAuthenticate  set to true, if the request sent to the server shall include authentication
     * credentials;
     * @param fastFail true to request a short timeout
     *
     * @return the body of the HTTP response, if and only if the response code was "200 OK".
     * @exception SdsTransferException if the HTTP return code was not 200 (and retries have
     *    been exhausted), or rewrapping a Java exception.
     */
    private String sendRequest(String requestMethod, String urlSuffix, String requestBody, ProgressMonitor monitor, boolean doAuthenticate, boolean fastFail) throws SdsTransferException {
        StringBuffer responseBody = new StringBuffer();
        int retries = getMaxRetries();

        while(true) { // the retry loop
            try {
                URL url = new URL(new URL(getBaseUrl()), urlSuffix);
                System.out.print(requestMethod + " " + url + "... ");
                activeConnection = (HttpURLConnection)url.openConnection();
                activeConnection.setConnectTimeout(fastFail ? 1000 : Main.pref.getInteger("socket.timeout.connect",15)*1000);
                activeConnection.setRequestMethod(requestMethod);
                if (doAuthenticate) {
                    addAuth(activeConnection);
                }

                if (requestMethod.equals("PUT") || requestMethod.equals("POST") || requestMethod.equals("DELETE")) {
                    activeConnection.setDoOutput(true);
                    activeConnection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                    OutputStream out = activeConnection.getOutputStream();

                    // It seems that certain bits of the Ruby API are very unhappy upon
                    // receipt of a PUT/POST message without a Content-length header,
                    // even if the request has no payload.
                    // Since Java will not generate a Content-length header unless
                    // we use the output stream, we create an output stream for PUT/POST
                    // even if there is no payload.
                    if (requestBody != null) {
                        BufferedWriter bwr = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                        bwr.write(requestBody);
                        bwr.flush();
                    }
                    out.close();
                }

                activeConnection.connect();
                System.out.println(activeConnection.getResponseMessage());
                int retCode = activeConnection.getResponseCode();

                if (retCode >= 500) {
                    if (retries-- > 0) {
                        sleepAndListen(retries, monitor);
                        System.out.println(tr("Starting retry {0} of {1}.", getMaxRetries() - retries,getMaxRetries()));
                        continue;
                    }
                }

                // populate return fields.
                responseBody.setLength(0);

                // If the API returned an error code like 403 forbidden, getInputStream
                // will fail with an IOException.
                InputStream i = null;
                try {
                    i = activeConnection.getInputStream();
                } catch (IOException ioe) {
                    i = activeConnection.getErrorStream();
                }
                if (i != null) {
                    // the input stream can be null if both the input and the error stream
                    // are null. Seems to be the case if the OSM server replies a 401
                    // Unauthorized, see #3887.
                    //
                    BufferedReader in = new BufferedReader(new InputStreamReader(i));
                    String s;
                    while((s = in.readLine()) != null) {
                        responseBody.append(s);
                        responseBody.append("\n");
                    }
                }
                String errorHeader = null;
                // Look for a detailed error message from the server
                if (activeConnection.getHeaderField("Error") != null) {
                    errorHeader = activeConnection.getHeaderField("Error");
                    System.err.println("Error header: " + errorHeader);
                } else if (retCode != 200 && responseBody.length()>0) {
                    System.err.println("Error body: " + responseBody);
                }
                activeConnection.disconnect();

                errorHeader = errorHeader == null? null : errorHeader.trim();
                String errorBody = responseBody.length() == 0? null : responseBody.toString().trim();
                switch(retCode) {
                case HttpURLConnection.HTTP_OK:
                    return responseBody.toString();
                case HttpURLConnection.HTTP_FORBIDDEN:
                    throw new SdsTransferException("FORBIDDEN");
                default:
                    throw new SdsTransferException(errorHeader + errorBody);
                }
            } catch (UnknownHostException e) {
                throw new SdsTransferException(e);
            } catch (SocketTimeoutException e) {
                if (retries-- > 0) {
                    continue;
                }
                throw new SdsTransferException(e);
            } catch (ConnectException e) {
                if (retries-- > 0) {
                    continue;
                }
                throw new SdsTransferException(e);
            } catch(IOException e){
                throw new SdsTransferException(e);
            } catch(SdsTransferException e) {
                throw e;
            }
        }
    }
    
    protected InputStream getInputStream(String urlStr, ProgressMonitor progressMonitor) throws SdsTransferException {
        urlStr = getBaseUrl() + urlStr;
    	try {
            URL url = null;
            try {
                url = new URL(urlStr.replace(" ", "%20"));
            } catch(MalformedURLException e) {
                throw new SdsTransferException(e);
            }
            try {
                activeConnection = (HttpURLConnection)url.openConnection();
            } catch(Exception e) {
                throw new SdsTransferException(tr("Failed to open connection to API {0}.", url.toExternalForm()), e);
            }
            if (cancel) {
                activeConnection.disconnect();
                return null;
            }

            addAuth(activeConnection);

            if (cancel)
                throw new SdsTransferException();
            if (Main.pref.getBoolean("osm-server.use-compression", true)) {
                activeConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
            }

            activeConnection.setConnectTimeout(Main.pref.getInteger("socket.timeout.connect",15)*1000);

            try {
                System.out.println("GET " + url);
                activeConnection.connect();
            } catch (Exception e) {
                e.printStackTrace();
                throw new SdsTransferException(tr("Could not connect to the OSM server. Please check your internet connection."), e);
            }
            try {
                if (activeConnection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED)
                    throw new OsmApiException(HttpURLConnection.HTTP_UNAUTHORIZED,null,null);

                if (activeConnection.getResponseCode() == HttpURLConnection.HTTP_PROXY_AUTH)
                    throw new OsmTransferCanceledException();

                String encoding = activeConnection.getContentEncoding();
                if (activeConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    String errorHeader = activeConnection.getHeaderField("Error");
                    StringBuilder errorBody = new StringBuilder();
                    try
                    {
                        InputStream i = FixEncoding(activeConnection.getErrorStream(), encoding);
                        if (i != null) {
                            BufferedReader in = new BufferedReader(new InputStreamReader(i));
                            String s;
                            while((s = in.readLine()) != null) {
                                errorBody.append(s);
                                errorBody.append("\n");
                            }
                        }
                    }
                    catch(Exception e) {
                        errorBody.append(tr("Reading error text failed."));
                    }

                    throw new OsmApiException(activeConnection.getResponseCode(), errorHeader, errorBody.toString());
                }

                return FixEncoding(new ProgressInputStream(activeConnection, progressMonitor), encoding);
            } catch(Exception e) {
                if (e instanceof SdsTransferException)
                    throw (SdsTransferException)e;
                else
                    throw new SdsTransferException(e);

            }
        } finally {
            progressMonitor.invalidate();
        }
    }

    private InputStream FixEncoding(InputStream stream, String encoding) throws IOException
    {
        if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
            stream = new GZIPInputStream(stream);
        }
        else if (encoding != null && encoding.equalsIgnoreCase("deflate")) {
            stream = new InflaterInputStream(stream, new Inflater(true));
        }
        return stream;
    }


}
