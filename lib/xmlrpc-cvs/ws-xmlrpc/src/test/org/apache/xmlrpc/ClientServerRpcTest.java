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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;
import java.util.Hashtable;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests XmlRpc run-time.
 *
 * @author Daniel L. Rall
 * @version $Id: ClientServerRpcTest.java,v 1.16 2004/06/17 01:40:14 dlr Exp $
 */
public class ClientServerRpcTest
    extends LocalServerRpcTest
{
    /**
     * The identifier or fully qualified class name of the SAX driver
     * to use.  This is generally <code>uk.co.wilson.xml.MinML</code>,
     * but could be changed to
     * <code>org.apache.xerces.parsers.SAXParser</code> for timing
     * comparisons.
     */
    private static final String SAX_DRIVER = "uk.co.wilson.xml.MinML";

    /**
     * The number of RPCs to make for each test.
     */
    private static final int NBR_REQUESTS = 1000;

    /**
     * The number of calls to batch in the multicall.
     */
    private static final int NUM_MULTICALLS = 10;

    private XmlRpcServer server;

    private XmlRpcClient client;

    private XmlRpcClientLite liteClient;

    /**
     * Constructor
     */
    public ClientServerRpcTest(String testName) 
    {
        super(testName);

        XmlRpc.setDebug(true);
        try
        {
            XmlRpc.setDriver(SAX_DRIVER);
        }
        catch (ClassNotFoundException e)
        {
            fail(e.toString());
        }

        // Server (only)
        server = new XmlRpcServer();
        server.addHandler(HANDLER_NAME, new TestHandler());

        InetAddress localhost = null;
        try
        {
            // localhost will be a random network interface on a
            // multi-homed host.
            localhost = InetAddress.getLocalHost();
        }
        catch (UnknownHostException e)
        {
            fail(e.toString());
        }

        // Setup system handler
        SystemHandler webServerSysHandler = new SystemHandler();
        webServerSysHandler.addSystemHandler("multicall", new MultiCall());

        // WebServer (contains its own XmlRpcServer instance)
        setUpWebServer();
        webServer.addHandler("system", webServerSysHandler);

        // XML-RPC client(s)
        try
        {
            String hostName = localhost.getHostName();
            client = new XmlRpcClient(hostName, SERVER_PORT);
            //liteClient = new XmlRpcClientLite(hostName, SERVER_PORT);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    /**
     * Return the Test
     */
    public static Test suite() 
    {
        return new TestSuite(ClientServerRpcTest.class);
    }

    /**
     * Setup the server and clients.
     */
    public void setUp() 
    {
        try
        {
            startWebServer();
        }
        catch (RuntimeException e)
        {
            e.printStackTrace();
            fail(e.toString());
        }
    }
   
    /**
     * Tear down the test.
     */
    public void tearDown() 
    {
        try
        {
            stopWebServer();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    /**
     * Tests server's RPC capabilities directly.
     */
    public void testServer()
    {
        try
        {
            long time = System.currentTimeMillis();
            for (int i = 0; i < NBR_REQUESTS; i++)
            {
                InputStream in =
                    new ByteArrayInputStream(RPC_REQUEST.getBytes());
                String response = new String(server.execute(in));
                assertTrue("Response did not contain " + REQUEST_PARAM_XML,
                           response.indexOf(REQUEST_PARAM_XML) != -1);
            }
            time = System.currentTimeMillis() - time;
            System.out.println("Total time elapsed for " + NBR_REQUESTS +
                               " iterations was " + time + " milliseconds, " +
                               "averaging " + (time / NBR_REQUESTS) +
                               " milliseconds per request");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Tests client/server RPC (via {@link WebServer}).
     */
    public void testRpc()
    {
        try
        {
            // Test the web server (which also tests the rpc server)
            // by connecting via the clients
            Vector params = new Vector();
            params.add(REQUEST_PARAM_VALUE);
            Object response = client.execute(HANDLER_NAME + ".echo", params);
            assertEquals(REQUEST_PARAM_VALUE, response);
            //params.removeAllElements();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testSystemMultiCall()
    {
        try
        {
            Vector calls = new Vector();

            for (int i = 0; i < NUM_MULTICALLS; i++)
            {
                Hashtable call = new Hashtable();
                Vector params = new Vector();

                params.add(REQUEST_PARAM_VALUE + i);
                call.put("methodName", HANDLER_NAME + ".echo");
                call.put("params", params);
 
                calls.addElement(call);
            }
 
            Vector paramWrapper = new Vector();
            paramWrapper.add(calls);
            
            Object response = client.execute("system.multicall", paramWrapper);

            for (int i = 0; i < NUM_MULTICALLS; i++)
            {
               Vector result = new Vector();
               result.add(REQUEST_PARAM_VALUE + i);

               assertEquals(result, ((Vector)response).elementAt(i));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }   
}
