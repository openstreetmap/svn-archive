package org.apache.xmlrpc;

/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "XML-RPC" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;
import java.util.StringTokenizer;

import org.apache.xmlrpc.util.HttpUtil;

/**
 * Interface from XML-RPC to a 'lite' HTTP implementation.  This class will use
 * the XmlRpcClientLite.auth member for the HTTP Basic authentication string.
 *
 * @author <a href="mailto:hannes@apache.org">Hannes Wallnoefer</a>
 * @author <a href="mailto:andrew@kungfoocoder.org">Andrew Evers</a>
 * @version $Id: LiteXmlRpcTransport.java,v 1.3 2003/05/01 16:53:15 rhoegg Exp $
 * @since 1.2
 */
class LiteXmlRpcTransport implements XmlRpcTransport
{
    String hostname;
    String host;
    protected String auth = null;
    int port;
    String uri;
    Socket socket = null;
    BufferedOutputStream output;
    BufferedInputStream input;
    boolean keepalive;
    byte[] buffer;

    /**
     * Create a new DefaultXmlRpcTransport with the specified URL.
     *
     * @param url the url to POST XML-RPC requests to.
     */
    public LiteXmlRpcTransport(URL url)
    {
        hostname = url.getHost();
        port = url.getPort();
        if (port < 1)
        {
            port = 80;
        }
        uri = url.getFile();
        if (uri == null || "".equals(uri))
        {
            uri = "/";
        }
        host = port == 80 ? hostname : hostname + ":" + port;        
    }

    public InputStream sendXmlRpc(byte [] request)
    throws IOException
    {
        try
        {
            if (socket == null)
            {
        	initConnection();
            }

            InputStream in = null;

           // send request to the server and get an input stream
           // from which to read the response
            try
            {
                in = sendRequest(request);
            }
            catch (IOException iox)
            {
                // if we get an exception while sending the request,
                // and the connection is a keepalive connection, it may
                // have been timed out by the server. Try again.
                if (keepalive)
                {
                    closeConnection();
                    initConnection();
                    in = sendRequest(request);
                }
                else
                {
                    throw iox;
                }
            }

            // eepalive is always false if XmlRpc.keepalive is false
            if (!keepalive)
            {
                closeConnection ();
            }

            return in;
        }
        catch (IOException iox)
        {
            // this is a lower level problem,  client could not talk to
            // server for some reason.
            throw iox;
        }
        catch (Exception x)
        {
            // same as above, but exception has to be converted to
            // IOException.
            if (XmlRpc.debug)
            {
                x.printStackTrace ();
            }

            String msg = x.getMessage ();
            if (msg == null || msg.length () == 0)
            {
                msg = x.toString ();
            }
            throw new IOException (msg);
        }
    }

    /**
     *
     * @throws IOException
     */
    protected void initConnection() throws IOException
    {
        final int retries = 3;
        final int delayMillis = 100;
        
        int tries = 0;
        
        socket = null;
        while (socket == null) {
            try {
                socket = new Socket(hostname, port);
            }
            catch (ConnectException e) {
                if (tries >= retries) {
                    throw e;
                } else {
                    // log.debug("ConnectException: " + e.getMessage() + ", waiting " + new Integer(delayMillis).toString() + " milliseconds and retrying");
                    try {
                        Thread.sleep(delayMillis);
                    }
                    catch (InterruptedException ignore) {
                    }
                }
            }
        }
        
        output = new BufferedOutputStream(socket.getOutputStream());
        input = new BufferedInputStream(socket.getInputStream());
    }

    /**
     *
     */
    protected void closeConnection ()
    {
        try
        {
            socket.close();
        }
        catch (Exception ignore)
        {
        }
        finally
        {
            socket = null;
        }
    }

    /**
     *
     * @param request
     * @return
     * @throws IOException
     */
    public InputStream sendRequest(byte[] request) throws IOException
    {
        output.write(("POST " + uri + " HTTP/1.0\r\n").getBytes());
        output.write(("User-Agent: " + XmlRpc.version + "\r\n").getBytes());
        output.write(("Host: " + host + "\r\n").getBytes());
        if (XmlRpc.getKeepAlive())
        {
            output.write("Connection: Keep-Alive\r\n".getBytes());
        }
        output.write("Content-Type: text/xml\r\n".getBytes());
        if (auth != null)
        {
            output.write(("Authorization: Basic " + auth + "\r\n")
                    .getBytes());
        }
        output.write(("Content-Length: " + request.length)
                .getBytes());
        output.write("\r\n\r\n".getBytes());
        output.write(request);
        output.flush();

        // start reading  server response headers
        String line = readLine();
        if (XmlRpc.debug)
        {
            System.out.println(line);
        }
        int contentLength = -1;
        try
        {
            StringTokenizer tokens = new StringTokenizer(line);
            String httpversion = tokens.nextToken();
            String statusCode = tokens.nextToken();
            String statusMsg = tokens.nextToken("\n\r");
            keepalive = XmlRpc.getKeepAlive()
                    && "HTTP/1.1".equals(httpversion);
            if (! "200".equals(statusCode))
            {
                throw new IOException("Unexpected Response from Server: "
                        + statusMsg);
            }
        }
        catch (IOException iox)
        {
            throw iox;
        }
        catch (Exception x)
        {
            // x.printStackTrace ();
            throw new IOException("Server returned invalid Response.");
        }
        do
        {
            line = readLine ();
            if (line != null)
            {
                if (XmlRpc.debug)
                {
                    System.out.println(line);
                }
                line = line.toLowerCase();
                if (line.startsWith("content-length:"))
                {
                    contentLength = Integer.parseInt(
                            line.substring(15).trim());
                }
                if (line.startsWith("connection:"))
                {
                    keepalive = XmlRpc.getKeepAlive()
                            && line.indexOf("keep-alive") > -1;
                }
            }
        }
        while (line != null && ! line.equals(""))
            ;
        return new ServerInputStream(input, contentLength);
    }

    /**
     * Sets Authentication for this client. This will be sent as Basic
     * Authentication header to the server as described in
     * <a href="http://www.ietf.org/rfc/rfc2617.txt">
     * http://www.ietf.org/rfc/rfc2617.txt</a>.
     */
    public void setBasicAuthentication(String user, String password)
    {
        auth = HttpUtil.encodeBasicAuthentication(user, password);
    }

    /**
     *
     * @return
     * @throws IOException
     */
    private String readLine() throws IOException
    {
        if (buffer == null)
        {
            buffer = new byte[2048];
        }
        int next;
        int count = 0;
        while (true)
        {
            next = input.read();
            if (next < 0 || next == '\n')
            {
                break;
            }
            if (next != '\r')
            {
                buffer[count++] = (byte) next;
            }
            if (count >= buffer.length)
            {
                throw new IOException ("HTTP Header too long");
            }
        }
        return new String(buffer, 0, count);
    }

    /**
     *
     * @throws Throwable
     */
    protected void finalize() throws Throwable
    {
        closeConnection ();
    }
}
