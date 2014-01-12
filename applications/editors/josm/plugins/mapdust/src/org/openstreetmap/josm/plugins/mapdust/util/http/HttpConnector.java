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
package org.openstreetmap.josm.plugins.mapdust.util.http;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import org.openstreetmap.josm.plugins.mapdust.util.retry.RetryAgent;
import org.openstreetmap.josm.plugins.mapdust.util.retry.RetrySetup;


/**
 * General connector to a given REST web service.
 *
 * @author Bea
 *
 */
public class HttpConnector {

    /** The timeout */
    private final int timeout = 20000;

    /** The <code>RetrySetup</code> */
    private final RetrySetup retrySetup;

    /** The <code>HttpURLConnection</code> object */
    protected HttpURLConnection connection;

    /**
     * Builds a new <code>HttpConnector</code> object based on the given
     * argument.
     *
     * @param retrySetup The <code>RetrySetup</code> object.
     */
    public HttpConnector(RetrySetup retrySetup) {
        this.retrySetup = retrySetup;
    }

    /**
     * Executes a HTTP GET method, based on the given URL. Returns a
     * <code>HttpResponse</code> object containing the response code , response
     * message and the response content.
     *
     * @param url The URL
     * @return a <code>HttpResponse</code> object.
     *
     * @throws IOException In the case of an error
     */
    public HttpResponse executeGET(final URL url) throws IOException {
        HttpResponse response;
        RetryAgent<Boolean> agent = new RetryAgent<Boolean>(retrySetup) {

            @Override
            protected Boolean target() throws IOException {
                /* 1: set up the connection */
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(timeout);
                connection.setReadTimeout(timeout);
                /* 2: connect */
                connection.connect();
                return true;
            }

            @Override
            protected void cleanup() throws IOException {
                /* No cleanup is needed. */
            }
        };
        try {
            agent.run();
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
                throw new IOException(e);

        }
        /* 3: build the response */
        int responseCode = connection.getResponseCode();
        String responseMessage = connection.getResponseMessage();
        String responseContent = null;
        if (responseCode == 200 || responseCode == 201 || responseCode == 204) {
            responseContent = readContent(connection.getContent());
        }
        response = new HttpResponse(responseCode, responseMessage, responseContent);
        return response;

    }

    /**
     * Executes a HTTP POST method based on the given arguments.
     *
     * @param url The URL
     * @param requestHeaders A map of request headers
     * @param requestParameters A map of request parameters
     * @return a <code>HttpResponse</code> object.
     * @throws IOException In the case of an error
     */
    public HttpResponse executePOST(final URL url,
            final Map<String, String> requestHeaders,
            final Map<String, String> requestParameters) throws IOException {
        HttpResponse response = null;
        RetryAgent<Boolean> agent = new RetryAgent<Boolean>(retrySetup) {

            @Override
            protected Boolean target() throws IOException {
                /* 1: set up the connection */
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(timeout);
                connection.setReadTimeout(timeout);
                /* set the request header */
                if (requestHeaders != null) {
                    for (String key : requestHeaders.keySet()) {
                        connection.addRequestProperty(key,
                                requestHeaders.get(key));
                    }
                }
                final StringBuffer sbEncodeParameters = new StringBuffer();
                if (requestParameters != null) {
                    connection.setDoOutput(true);
                    sbEncodeParameters
                            .append(encodeParameters(requestParameters));
                }

                /* 2: connect */
                connection.connect();

                /* 3: write content */
                if (sbEncodeParameters.length() > 0) {
                    OutputStreamWriter out =
                            new OutputStreamWriter(connection.getOutputStream());

                    out.write(sbEncodeParameters.toString());
                    out.close();
                }

                return true;
            }

            @Override
            protected void cleanup() throws IOException {
                /* No cleanup is needed. */
            }
        };
        try {
            agent.run();
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
                throw new IOException(e);

        }
        /* 3: build the response */
        int responseCode = connection.getResponseCode();
        String responseMessage = connection.getResponseMessage();
        String responseContent = null;
        if (responseCode == 200 || responseCode == 201 || responseCode == 204) {
            responseContent = readContent(connection.getContent());
        }
        response = new HttpResponse(responseCode, responseMessage, responseContent);
        return response;
    }

    /**
     * Reads the content of the given object.
     *
     * @param obj An object
     * @return A string containing the content of the object.
     *
     * @throws IOException In the case of an error
     */
    protected String readContent(Object obj) throws IOException {
        String result = "";
        InputStreamReader in = new InputStreamReader((InputStream) obj);
        BufferedReader buff = new BufferedReader(in);
        String line = "";
        while ((line = buff.readLine()) != null) {
            result += line;
        }
        return result;
    }

    /**
     * Encodes with 'utf-8' the given map of parameters.
     *
     * @param postParameters A map of POST parameters
     * @return An encoded string.
     */
    protected String encodeParameters(Map<String, String> postParameters) {
        StringBuffer buf = new StringBuffer();
        int i = 0;
        for (String key : postParameters.keySet()) {
            if (i != 0) {
                buf.append("&");
            }
            try {
                buf.append(key).append("=").append(URLEncoder.encode(
                        postParameters.get(key), "utf-8"));
            } catch (java.io.UnsupportedEncodingException neverHappen) {}
            i++;
        }
        return buf.toString();
    }

}
