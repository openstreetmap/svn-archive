/*
  * GZIPAwareGetMethod.java
  *
  * Created on Thu Nov 24 10:08:54 GMT 2005
  *
*/

package org.openstreetmap.util;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;


/**
  *
  * @author aleem
  */
public class GZIPAwareGetMethod extends GetMethod {

     /** Creates a new instance of GZIPAwareGetMethod */
     public GZIPAwareGetMethod() {
         super();
     }

     /**
      * Constructor specifying a URI.
      *
      * @param uri either an absolute or relative URI
      *
      * @since 1.0
      */
     public GZIPAwareGetMethod(String uri) {
         super(uri);
     }

     /**
      * Constructor specifying a URI and a tempDir.
      *
      * @param uri either an absolute or relative URI
      * @param tempDir directory to store temp files in
      *
      * @deprecated the client is responsible for disk I/O
      * @since 1.0
      */
//     public GZIPAwareGetMethod(String uri, String tempDir) {
  //       super(uri, tempDir);
    // }

     /**
      * Constructor specifying a URI, tempDir and tempFile.
      *
      * @param uri either an absolute or relative URI
      * @param tempDir directory to store temp files in
      * @param tempFile file to store temporary data in
      *
      * @deprecated the client is responsible for disk I/O
      * @since 1.0
      */
//     public GZIPAwareGetMethod(String uri, String tempDir, String tempFile) {
  //       super(uri, tempDir, tempFile);
    // }

     /**
      * Overrides method in {@link HttpMethodBase}.
      *
      * Notifies the server that we can process a GZIP-compressed response before
      * sending the request.
      *
      */
     public int execute(HttpState state, HttpConnection conn) throws HttpException, HttpRecoverableException,

IOException {
         // Tell the server that we can handle GZIP-compressed data in the response body
         addRequestHeader("Accept-Encoding", "gzip");

         return super.execute(state, conn);
     }

     /**
      * Overrides method in {@link GetMethod} to set the responseStream variable appropriately.
      *
      * If the response body was GZIP-compressed, responseStream will be set to a GZIPInputStream
      * wrapping the original InputStream used by the superclass.
      *
      */
     protected void readResponseBody(HttpState state, HttpConnection conn) throws IOException,
HttpException {
         super.readResponseBody(state, conn);

         Header contentEncodingHeader = getResponseHeader("Content-Encoding");

         if (contentEncodingHeader != null && contentEncodingHeader.getValue().equalsIgnoreCase("gzip"))
             setResponseStream(new GZIPInputStream(getResponseStream()));
     }

}

