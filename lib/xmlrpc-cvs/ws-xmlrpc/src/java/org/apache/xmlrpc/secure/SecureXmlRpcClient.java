package org.apache.xmlrpc.secure;

/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights 
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

import java.net.*;
import java.io.*;
import java.util.*;
import org.xml.sax.*;

import org.apache.xmlrpc.XmlRpcClient;

/**
 * A multithreaded, reusable XML-RPC client object. Use this if you need a full-grown
 * HTTP client (e.g. for Proxy and Cookies support). If you don't need that, <code>XmlRpcClientLite</code>
 * may work better for you.
 */
public class SecureXmlRpcClient 
    extends XmlRpcClient
{
    /** 
     * Construct a XML-RPC client with this URL.
     */
    public SecureXmlRpcClient (URL url) {
        super(url);
    }

    /** 
     * Construct a XML-RPC client for the URL represented by this String.
     */
    public SecureXmlRpcClient (String url) throws MalformedURLException {
        super(url);
    }
   
    /** 
     * Construct a XML-RPC client for the specified hostname and port.
     */
    public SecureXmlRpcClient (String hostname, int port) throws MalformedURLException 
    {
        super("https://" + hostname + ':' + port + "/RPC2");
    }
    
    /**
     * This allows us to setup
     */
     public void setup() throws Exception
     {
         SecurityTool.setup();    
     }

    /** 
     * Just for testing.
     */
    public static void main (String args[]) throws Exception
    {
        // XmlRpc.setDebug (true);
        try {
            String url = args[0];
            String method = args[1];
            Vector v = new Vector ();
            for (int i=2; i<args.length; i++) try {
                v.addElement (new Integer (Integer.parseInt (args[i])));
            } catch (NumberFormatException nfx) {
                v.addElement (args[i]);
            }
            SecureXmlRpcClient client = new SecureXmlRpcClient (url);
            try {
                System.err.println (client.execute (method, v));
            } catch (Exception ex) {
                System.err.println ("Error: "+ex.getMessage());
            }
        } catch (Exception x) {
            System.err.println (x);
            System.err.println ("Usage: java " +
                                SecureXmlRpcClient.class.getName() +
                                " <url> <method> [args]");
            System.err.println ("Arguments are sent as integers or strings.");
        }
    }
}
