package org.apache.xmlrpc;

/* ====================================================================
 * The Apache Software License, Version 1.1
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
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
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

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import junit.framework.*;

/**
 * Abstract Test class to be extended by the tests for each Transport.
 * Guarantees that transports implement a base contract expected by the
 * XmlRpcClient classes
 *
 * @author <a href="mailto:rhoegg@isisnetworks.net">Ryan Hoegg</a>
 * @version $Id: XmlRpcTransportTest.java,v 1.2 2003/05/01 16:53:16 rhoegg Exp $
 */
abstract public class XmlRpcTransportTest
    extends LocalServerRpcTest
{
    /**
     * Constructor
     */
    public XmlRpcTransportTest(String testName)
    {
        super(testName);
    }
    
    /**
     * This should return a new instance of the XmlRpcTransport being tested.
     */
    abstract protected XmlRpcTransport getTransport(URL url);
    
    /**
     * This test is to enforce that every alternate implementation of 
     * XmlRpcTransport provides a minimum of the same functionality as
     * @link DefaultXmlRpcTransport.  We trust DefaultXmlRpcTransport
     * because it is used by default in XmlRpcClient, which is tested
     * in @link ClientServerRpcTest.
     */
    public void testSendXmlRpc() {
        
        try {
            setUpWebServer();
            startWebServer();
            URL testUrl = buildURL("localhost", SERVER_PORT);
            XmlRpcTransport controlTransport = new DefaultXmlRpcTransport(testUrl);
            XmlRpcTransport testTransport = getTransport(testUrl);
            InputStream controlResponse = controlTransport.sendXmlRpc(RPC_REQUEST.getBytes());
            InputStream testResponse = testTransport.sendXmlRpc(RPC_REQUEST.getBytes());
            assertTrue(
                "Response from XmlRpcTransport does not match that of DefaultXmlRpcTransport.",
                equalsInputStream(controlResponse, testResponse));
            stopWebServer();
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        catch (XmlRpcClientException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    private URL buildURL(String hostname, int port) throws MalformedURLException {
        return new URL("http://" + hostname + ':' + port + "/RPC2");
    }
    
    protected boolean equalsInputStream(InputStream is1, InputStream is2) throws IOException {
        BufferedInputStream stream1 = new BufferedInputStream(is1);
        BufferedInputStream stream2 = new BufferedInputStream(is2);
        int char1 = is1.read();
        int char2 = is2.read();
        while ((char1 != -1) && (char2 != -1) && (char1 == char2)) {
            char1 = is1.read();
            char2 = is2.read();
        }
        return char1 == char2;
    }
}
