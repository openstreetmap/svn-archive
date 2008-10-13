/* Copyright (c) 2008, Henrik Niehaus
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
package org.openstreetmap.josm.plugins.osb.api.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class HttpUtils {
    public static String get(String url, Map<String, String> headers, String charset) throws IOException {
        URL page = new URL(url);
        URLConnection con = page.openConnection();
        if(headers != null) {
            for (Iterator<Entry<String,String>> iterator = headers.entrySet().iterator(); iterator.hasNext();) {
                Entry<String, String> entry = iterator.next();
                con.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int length = -1;
        byte[] b = new byte[1024];
        InputStream in = con.getInputStream();
        while( (length = in.read(b)) > 0 ) {
            bos.write(b, 0, length);
        }
        
        return new String(bos.toByteArray(), charset);
    }
    
    public static HttpResponse getResponse(String url, Map<String, String> headers, String charset) throws IOException {
        URL page = new URL(url);
        URLConnection con = page.openConnection();
        if(headers != null) {
            for (Iterator<Entry<String,String>> iterator = headers.entrySet().iterator(); iterator.hasNext();) {
                Entry<String, String> entry = iterator.next();
                con.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int length = -1;
        byte[] b = new byte[1024];
        InputStream in = con.getInputStream();
        while( (length = in.read(b)) > 0 ) {
            bos.write(b, 0, length);
        }
        
        HttpResponse response = new HttpResponse(new String(bos.toByteArray(), charset), con.getHeaderFields());
        return response;
    }
    
    /**
     * 
     * @param url
     * @param headers
     * @param content the post body
     * @param responseCharset the expected charset of the response
     * @return
     * @throws IOException
     */
    public static String post(String url, Map<String, String> headers, String content, String responseCharset) throws IOException {
        // initialize the connection
        URL page = new URL(url);
        URLConnection con = page.openConnection();
        con.setDoOutput(true);
        if(headers != null) {
            for (Iterator<Entry<String,String>> iterator = headers.entrySet().iterator(); iterator.hasNext();) {
                Entry<String, String> entry = iterator.next();
                con.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        
        //send the post
        OutputStream os = con.getOutputStream();
        os.write(content.getBytes("UTF-8"));
        os.flush();
        
        // read the response
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int length = -1;
        byte[] b = new byte[1024];
        InputStream in = con.getInputStream();
        while( (length = in.read(b)) > 0 ) {
            bos.write(b, 0, length);
        }
        
        return new String(bos.toByteArray(), responseCharset);
    }
    
    /**
     * Adds a parameter to a given URI
     * @param uri
     * @param param
     * @param value
     * @return
     */
    public static String addParameter(String uri, String param, String value) {
        StringBuilder sb = new StringBuilder(uri);
        if(uri.contains("?")) {
            sb.append('&');
        } else {
            sb.append('?');
        }
        
        sb.append(param);
        sb.append('=');
        sb.append(value);
        
        return sb.toString();
    }

    public static Map<String, List<String>> head(String url, Map<String, String> headers, String charset) throws IOException {
        URL page = new URL(url);
        URLConnection con = page.openConnection();
        if(headers != null) {
            for (Iterator<Entry<String,String>> iterator = headers.entrySet().iterator(); iterator.hasNext();) {
                Entry<String, String> entry = iterator.next();
                con.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        
        return con.getHeaderFields();
    }
    
    public static String getHeaderField(Map<String, List<String>> headers, String headerField) {
        if(!headers.containsKey(headerField)) {
            return null;
        }
        
        List<String> value = headers.get(headerField);
        if(value.size() == 1) {
            return value.get(0);
        } else {
            throw new RuntimeException("Header contains several values and cannot be mapped to a single String");
        }
    }
}
