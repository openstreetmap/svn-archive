/* Copyright (c) 2010, skobbler GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the project nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.openstreetmap.josm.plugins.mapdust.service.connector;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openstreetmap.josm.plugins.mapdust.service.connector.parser.MapdustParser;
import org.openstreetmap.josm.plugins.mapdust.service.connector.parser.MapdustParserException;
import org.openstreetmap.josm.plugins.mapdust.service.connector.response.MapdustGetBugResponse;
import org.openstreetmap.josm.plugins.mapdust.service.connector.response.MapdustGetBugsResponse;
import org.openstreetmap.josm.plugins.mapdust.service.connector.response.MapdustPostResponse;
import org.openstreetmap.josm.plugins.mapdust.service.value.BoundingBox;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustBug;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustBugFilter;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustComment;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustResponseStatusCode;
import org.openstreetmap.josm.plugins.mapdust.service.value.Paging;
import org.openstreetmap.josm.plugins.mapdust.util.Configuration;
import org.openstreetmap.josm.plugins.mapdust.util.http.HttpConnector;
import org.openstreetmap.josm.plugins.mapdust.util.http.HttpResponse;
import org.openstreetmap.josm.plugins.mapdust.util.retry.RetrySetup;


/**
 * The <code>MapdustConnector</code> object. Connects to the Mapdust service,
 * and executes the following Mapdust service methods: getBug, getBugs, addBug,
 * commentBug and changeBugStatus.
 *
 * @author Bea
 */
public class MapdustConnector {

    /** The <code>HttpConnector</code> object */
    private final HttpConnector httpConnector;

    /** The <code>MapdustParser</code> object */
    private final MapdustParser parser;

    /**
     * Builds a <code>MapdustConnector</code> object with the default settings.
     */
    public MapdustConnector() {
        httpConnector = new HttpConnector(RetrySetup.DEFAULT);
        parser = new MapdustParser();
    }

    /**
     * Builds a <code>MapdustConnector</code> object based on the given
     * arguments.
     *
     * @param httpConnector The <code>HttpConnector</code> object.
     * @param parser The <code>MapdustParser</code> object.
     */
    public MapdustConnector(HttpConnector httpConnector, MapdustParser parser) {
        this.httpConnector = httpConnector;
        this.parser = parser;
    }

    /**
     * Searches for the OSM MapDust bugs in the given bounding box. The method
     * executes the 'getBugs' MapDust service method, parses the obtained
     * response object and return a <code>MapdustGetBugsResponse</code> object
     * containing the pagination information and the array of bugs. In the case
     * if the response code is not 200,201 or 204, a corresponding exception
     * will be thrown.
     *
     * @param bBox The bounding box where the bugs are searched.
     * @param filter The MapDust bug filter. The bugs can be filtered based on
     * the status, type and description. This parameter is not required.
     * @return A <code>MapdustGetBugsResponse</code> object, containing the
     * pagination information and an array of <code>MapdustBugContent</code>
     * object.
     * @throws MapdustConnectorException In the case of an error.
     */
    public MapdustGetBugsResponse getBugs(BoundingBox bBox,
            MapdustBugFilter filter) throws MapdustConnectorException {
        /* execute GET method and get the response */
        HttpResponse httpResponse = null;
        try {
            httpResponse = executeGetBugs(bBox, filter);
        } catch (MalformedURLException e) {
            throw new MapdustConnectorException(e.getMessage(), e);
        } catch (IOException e) {
            throw new MapdustConnectorException(e.getMessage(), e);
        } catch (Exception e) {
            throw new MapdustConnectorException(e.getMessage(), e);
        }
        /* parse HttpResponse */
        MapdustGetBugsResponse result = null;
        try {
            /* verify status codes */
            handleStatusCode(httpResponse);
            result = (MapdustGetBugsResponse) getParser().parseResponse(
                            httpResponse.getContent(),
                            MapdustGetBugsResponse.class);
        } catch (MapdustConnectorException e) {
            throw new MapdustConnectorException(e.getMessage(), e);
        } catch (MapdustParserException e) {
            throw new MapdustConnectorException(e.getMessage(), e);
        }
        return result;
    }


    /**
     * Returns the OSM bug with the given id. If the <code>Paging</code> object
     * is set, then the comments of the bug will be paginated. The method
     * executes the 'getBug' MapDust service method, parses the obtained
     * response object and return a <code>MapdustGetBugResponse</code> object.
     * In the case if the response code is not 2
     *
     * @param id The id of the object
     * @param paging The <code>Paging</code> object
     * @return A <code>MapdustGetBugResponse</code> object.
     *
     * @throws MapdustConnectorException In the case of an error.
     */
    public MapdustGetBugResponse getBug(Long id, Paging paging)
            throws MapdustConnectorException {
        HttpResponse httpResponse = null;
        try {
            httpResponse = executeGetBug(id, paging);
        } catch (MalformedURLException e) {
            throw new MapdustConnectorException(e.getMessage(), e);
        } catch (IOException e) {
            throw new MapdustConnectorException(e.getMessage(), e);
        } catch (Exception e) {
            throw new MapdustConnectorException(e.getMessage(), e);
        }
        /* parse result */
        MapdustGetBugResponse result = null;
        try {
            handleStatusCode(httpResponse);
            result = (MapdustGetBugResponse) getParser().parseResponse(
                    httpResponse.getContent(), MapdustGetBugResponse.class);
        } catch (MapdustConnectorException e) {
            throw new MapdustConnectorException(e.getMessage(), e);
        } catch (MapdustParserException e) {
            throw new MapdustConnectorException(e.getMessage(), e);
        }
        return result;
    }

    /**
     * Creates a new OSM bug with the specified arguments. The method executes
     * the 'addBug' MapDust service method, parses the obtained response object
     * and return a <code>MapdustPostResponse</code> object containing the id of
     * the created comment. In the case if the response code is not 200,201 or
     * 204, a corresponding exception will be thrown.
     *
     * @param bug A <code>MapdustBug</code> object
     * @return A <code>MapdustPostResponse</code> object which contains the id
     * of the created object.
     *
     * @throws MapdustConnectorException In the case of an error
     */
    public MapdustPostResponse addBug(MapdustBug bug)
            throws MapdustConnectorException {
        HttpResponse httpResponse = null;
        try {
            httpResponse = executeAddBug(bug);
        } catch (MalformedURLException e) {
            throw new MapdustConnectorException(e.getMessage(), e);
        } catch (IOException e) {
            throw new MapdustConnectorException(e.getMessage(), e);
        } catch (Exception e) {
            throw new MapdustConnectorException(e.getMessage(), e);
        }

        /* parse result */
        MapdustPostResponse result = null;
        try {
            handleStatusCode(httpResponse);
            result = (MapdustPostResponse) getParser().parseResponse(
                    httpResponse.getContent(), MapdustPostResponse.class);
        } catch (MapdustConnectorException e) {
            throw new MapdustConnectorException(e.getMessage(), e);
        } catch (MapdustParserException e) {
            throw new MapdustConnectorException(e.getMessage(), e);
        }
        return result;
    }

    /**
     * Creates a new comment for the given bug. The method executes the
     * 'commentBug' MapDust service method, parses the obtained response object
     * and return a <code>MapdustPostResponse</code> object containing the id of
     * the created comment. In the case if the response code is not 200,201 or
     * 204, a corresponding exception will be thrown.
     *
     * @param comment A <code>MapdustComment</code> object
     * @return A <code>MapdustPostResponse</code> object which contains the id
     * of the created object.
     * @throws MapdustConnectorException In the case of an error
     */
    public MapdustPostResponse commentBug(MapdustComment comment)
            throws MapdustConnectorException {
        HttpResponse httpResponse = null;
        try {
            httpResponse = executeCommentBug(comment);
        } catch (MalformedURLException e) {
            throw new MapdustConnectorException(e.getMessage(), e);
        } catch (IOException e) {
            throw new MapdustConnectorException(e.getMessage(), e);
        } catch (Exception e) {
            throw new MapdustConnectorException(e.getMessage(), e);
        }
        /* parse result */
        MapdustPostResponse result = null;
        try {
            handleStatusCode(httpResponse);
            result = (MapdustPostResponse) getParser().parseResponse(
                    httpResponse.getContent(), MapdustPostResponse.class);
        } catch (MapdustConnectorException e) {
            throw new MapdustConnectorException(e.getMessage(), e);
        } catch (MapdustParserException e) {
            throw new MapdustConnectorException(e.getMessage(), e);
        }
        return result;
    }

    /**
     * Changes the status of a given bug. The method executes the
     * 'changeBugStatus' MapDust service method, parses the obtained response
     * object and return a <code>MapdustPostResponse</code> object containing
     * the id of the created comment. In the case if the response code is not
     * 200,201 or 204, a corresponding exception will be thrown.
     *
     * @param statusId The new value for the status. Possible values are: 1, 2
     * or 3.
     * @param comment A <code>MapdustComment</code> object
     * @return A <code>MapdustPostResponse</code> object which contains the id
     * of the created object.
     * @throws MapdustConnectorException In the case of an error
     */
    public MapdustPostResponse changeBugStatus(Integer statusId,
            MapdustComment comment) throws MapdustConnectorException {
        HttpResponse httpResponse = null;
        try {
            httpResponse = executeChangeBugStatus(statusId, comment);
        } catch (MalformedURLException e) {
            throw new MapdustConnectorException(e.getMessage(), e);
        } catch (IOException e) {
            throw new MapdustConnectorException(e.getMessage(), e);
        } catch (Exception e) {
            throw new MapdustConnectorException(e.getMessage(), e);
        }
        /* parse result */
        MapdustPostResponse result = null;
        try {
            handleStatusCode(httpResponse);
            result = (MapdustPostResponse) getParser().parseResponse(
                    httpResponse.getContent(), MapdustPostResponse.class);
        } catch (MapdustConnectorException e) {
            throw new MapdustConnectorException(e.getMessage(), e);
        } catch (MapdustParserException e) {
            throw new MapdustConnectorException(e.getMessage(), e);
        }
        return result;
    }

    /**
     * Executes the 'getBugs' MapDust service method.
     *
     * @param bBox The bounding box where the bugs are searched. This parameter
     * it is a required parameter.
     * @param filter The MapDust bug filter. The bugs can be filtered based on
     * the status, type and description. This parameter is not required.
     * @return A <code>HttpResponse</code> object containing the JSON response.
     * @throws MalformedURLException In the case if the format of the URL is
     * invalid
     * @throws IOException In the case of an IO error
     */
    private HttpResponse executeGetBugs(BoundingBox bBox,
            MapdustBugFilter filter) throws MalformedURLException, IOException {
        HttpResponse httpResponse = null;
        String mapdustUri = Configuration.getInstance().getMapdustUrl();
        String mapdustApiKey = Configuration.getInstance().getMapdustKey();
        String urlString = null;
        if (mapdustUri != null && mapdustApiKey != null) {
            urlString = mapdustUri;
            urlString += "/getBugs?";
            urlString += "key=" + mapdustApiKey;
            urlString += "&bbox=" + bBox.getMinLon();
            urlString += "," + bBox.getMinLat();
            urlString += "," + bBox.getMaxLon();
            urlString += "," + bBox.getMaxLat();
            if (filter != null) {
                if (filter.getStatuses() != null
                        && filter.getStatuses().size() > 0) {
                    String paramStatus = buildParameter(filter.getStatuses());
                    if (!paramStatus.isEmpty()) {
                        urlString += "&fs=" + paramStatus;
                    }
                }
                if (filter.getTypes() != null && filter.getTypes().size() > 0) {
                    String paramTypes = buildParameter(filter.getTypes());
                    if (!paramTypes.isEmpty()) {
                        urlString += "&ft=" + paramTypes;
                    }
                }
                if (filter.getDescr() != null && filter.getDescr()) {
                    urlString += "&idd=" + "0";
                }
            }
        }
        URL url = null;
        if (urlString != null) {
            url = new URL(urlString);
            httpResponse = getHttpConnector().executeGET(url);
        }
        return httpResponse;
    }

    /**
     * Executes the 'getBug' MapDust service method.
     *
     * @param id The id of the object
     * @param paging The <code>Paging</code> object
     * @return A <code>HttpResponse</code> containing the JSON response of the
     * Mapdust method.
     *
     * @throws MalformedURLException In the case if the format of the URL is
     * invalid
     * @throws IOException In the case of an IO error
     */
    private HttpResponse executeGetBug(Long id, Paging paging)
            throws MalformedURLException, IOException {
        HttpResponse httpResponse = null;
        String mapdustUri = Configuration.getInstance().getMapdustUrl();
        String mapdustApiKey = Configuration.getInstance().getMapdustKey();
        String urlString = null;
        if (mapdustUri != null && mapdustApiKey != null) {
            urlString = mapdustUri;
            urlString += "/getBug?";
            urlString += "key=" + mapdustApiKey;
            if (id != null) {
                urlString += "&id=" + id;
            }
            if (paging != null && paging.getItems() != null
                    && paging.getPage() != null) {
                urlString += "&items=" + paging.getItems();
                urlString += "&p=" + paging.getPage();
            }
        }
        URL url = null;
        if (urlString != null) {
            url = new URL(urlString);
            httpResponse = getHttpConnector().executeGET(url);
        }
        return httpResponse;
    }

    /**
     * Executes the 'addBug' MapDust service method.
     *
     * @param bug A <code>MapdustBug</code> object
     * @return A <code>HttpResponse</code> containing the JSON response of the
     * MapDust method.
     *
     * @throws MalformedURLException In the case if the format of the URL is
     * invalid
     * @throws IOException In the case of an IO error
     */
    private HttpResponse executeAddBug(MapdustBug bug)
            throws MalformedURLException, IOException {
        HttpResponse httpResponse = null;
        String mapdustUri = Configuration.getInstance().getMapdustUrl();
        String mapdustApiKey = Configuration.getInstance().getMapdustKey();
        String urlString = null;
        Map<String, String> requestParameters = new HashMap<String, String>();
        if (mapdustUri != null && mapdustApiKey != null) {
            urlString = mapdustUri;
            urlString += "/addBug";
            requestParameters.put("key", mapdustApiKey);
            String coordinatesStr = "" + bug.getLatLon().getX();
            coordinatesStr += "," + bug.getLatLon().getY();
            requestParameters.put("coordinates", coordinatesStr);
            requestParameters.put("type", bug.getType().getKey());
            requestParameters.put("description", bug.getDescription());
            requestParameters.put("nickname", bug.getNickname());
        }
        URL url = null;
        if (urlString != null) {
            url = new URL(urlString);
            httpResponse = getHttpConnector().executePOST(url, null,
                    requestParameters);
        }
        return httpResponse;
    }

    /**
     * Executes the 'commentBug' MapDust service method.
     *
     * @param comment The <code>MapdustComment</code> object
     * @return A <code>HttpResponse</code> containing the JSON response of the
     * MapDust method.
     * @throws MalformedURLException In the case if the format of the URL is
     * invalid
     * @throws IOException In the case of an IO error
     */
    private HttpResponse executeCommentBug(MapdustComment comment)
            throws MalformedURLException, IOException {
        HttpResponse httpResponse = null;
        String mapdustUri = Configuration.getInstance().getMapdustUrl();
        String mapdustApiKey = Configuration.getInstance().getMapdustKey();
        String urlString = null;
        Map<String, String> requestParameters = new HashMap<String, String>();
        if (mapdustUri != null && mapdustApiKey != null) {
            urlString = mapdustUri;
            urlString += "/commentBug";
            requestParameters.put("key", mapdustApiKey);
            requestParameters.put("id", comment.getBugId().toString());
            requestParameters.put("comment", comment.getCommentText());
            requestParameters.put("nickname", comment.getNickname());
        }
        URL url = null;
        if (urlString != null) {
            url = new URL(urlString);
            httpResponse = getHttpConnector().executePOST(url, null,
                    requestParameters);
        }
        return httpResponse;
    }

    /**
     * Executes the 'changeBugStatus' MapDust service method.
     *
     * @param statusId The id of the status.
     * @param comment A <code>MapdustComment</code> object.
     * @return A <code>HttpResponse</code> containing the JSON response of the
     * MapDust method.
     *
     * @throws MalformedURLException In the case if the format of the URL is
     * invalid
     * @throws IOException In the case of an IO error
     */
    private HttpResponse executeChangeBugStatus(Integer statusId,
            MapdustComment comment) throws MalformedURLException, IOException {
        HttpResponse httpResponse = null;
        String mapdustUri = Configuration.getInstance().getMapdustUrl();
        String mapdustApiKey = Configuration.getInstance().getMapdustKey();
        String urlString = null;
        Map<String, String> requestParameters = new HashMap<String, String>();
        if (mapdustUri != null && mapdustApiKey != null) {
            urlString = mapdustUri;
            urlString += "/changeBugStatus";
            requestParameters.put("key", mapdustApiKey);
            requestParameters.put("id", comment.getBugId().toString());
            requestParameters.put("status", statusId.toString());
            requestParameters.put("comment", comment.getCommentText());
            requestParameters.put("nickname", comment.getNickname());
        }
        URL url = null;
        if (urlString != null) {
            url = new URL(urlString);
            httpResponse = getHttpConnector().executePOST(url, null,
                    requestParameters);
        }
        return httpResponse;
    }

    /**
     * Builds a string containing the elements of the given list. The elements
     * will be separated by a comma. If the list does not contains any element
     * the returned result will be an empty string.
     *
     * @param list The list of objects.
     * @return a string
     */
    private String buildParameter(List<? extends Object> list) {
        StringBuffer sb = new StringBuffer();
        for (Object obj : list) {
            if (obj != null) {
                sb.append(obj).append(",");
            }
        }
        return sb.substring(0, sb.length() - 1);
    }

    /**
     * Handles the response codes of the given <code>HttpResponse</code> object.
     * If the response code is 200, 201 or 204, the method returns without any
     * exception. Otherwise a <code>MapdustConnectorException</code> will be
     * thrown with an appropriate message.
     *
     * @param httpResponse The <code>HttpResponse</code> method.
     * @throws MapdustConnectorException In the case if the status code is not
     * 200, 201 or 204.
     */
    private void handleStatusCode(HttpResponse httpResponse)
            throws MapdustConnectorException {
        String errorMessage = "";
        Integer statusCode = httpResponse.getStatusCode();
        String statusMessage = httpResponse.getStatusMessage();
        if (statusCode.equals(MapdustResponseStatusCode.Status200
                .getStatusCode())
                || statusCode.equals(MapdustResponseStatusCode.Status201
                        .getStatusCode())
                || statusCode.equals(MapdustResponseStatusCode.Status204
                        .getStatusCode())) {
            // no error
            return;
        }
        switch (statusCode) {
            case 400:
                errorMessage = statusMessage + " ";
                errorMessage += MapdustResponseStatusCode.Status400.getDescription();
                throw new MapdustConnectorException(errorMessage);
            case 401:
                errorMessage = statusMessage + " ";
                errorMessage += MapdustResponseStatusCode.Status401.getDescription();
                throw new MapdustConnectorException(errorMessage);
            case 403:
                errorMessage = statusMessage + " ";
                errorMessage += MapdustResponseStatusCode.Status403.getDescription();
                throw new MapdustConnectorException(errorMessage);
            case 404:
                errorMessage = statusMessage + " ";
                errorMessage += MapdustResponseStatusCode.Status404.getDescription();
                throw new MapdustConnectorException(errorMessage);
            case 405:
                errorMessage = statusMessage + " ";
                errorMessage += MapdustResponseStatusCode.Status405.getDescription();
                throw new MapdustConnectorException(errorMessage);
            case 500:
                errorMessage = statusMessage + " ";
                errorMessage += MapdustResponseStatusCode.Status500.getDescription();
                throw new MapdustConnectorException(errorMessage);
            case 601:
                errorMessage = statusMessage + " ";
                errorMessage += MapdustResponseStatusCode.Status601.getDescription();
                throw new MapdustConnectorException(errorMessage);
            case 602:
                errorMessage = statusMessage + " ";
                errorMessage += MapdustResponseStatusCode.Status602.getDescription();
                throw new MapdustConnectorException(errorMessage);
            default:
                throw new MapdustConnectorException(
                        MapdustResponseStatusCode.Status.getDescription());
        }
    }

    /**
     * Returns the <code>HttpConnector</code>
     *
     * @return the httpConnector
     */
    public HttpConnector getHttpConnector() {
        return httpConnector;
    }

    /**
     * Sets the <code>MapdustParser</code>
     *
     * @return the parser
     */
    public MapdustParser getParser() {
        return parser;
    }

}
