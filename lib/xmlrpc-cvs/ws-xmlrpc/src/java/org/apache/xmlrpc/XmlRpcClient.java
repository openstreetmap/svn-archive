package org.apache.xmlrpc;

/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright(c) 2001 The Apache Software Foundation.  All rights
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
 *        Apache Software Foundation(http://www.apache.org/)."
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
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES(INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
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
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.EmptyStackException;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;
import org.xml.sax.AttributeList;
import org.xml.sax.SAXException;

/**
 * A multithreaded, reusable XML-RPC client object. Use this if you
 * need a full-grown HTTP client (e.g. for Proxy and Basic Auth
 * support). If you don't need that, <code>XmlRpcClientLite</code> may
 * work better for you.
 *
 * @author <a href="mailto:hannes@apache.org">Hannes Wallnoefer</a>
 * @author <a href="mailto:andrew@kungfoocoder.org">Andrew Evers</a>
 * @author <a href="mailto:rhoegg@isisnetworks.net">Ryan Hoegg</a>
 * @version $Id: XmlRpcClient.java,v 1.19 2003/01/29 00:57:14 rhoegg Exp $
 */
public class XmlRpcClient implements XmlRpcHandler
{
    protected URL url;
    
    // stored user and password for deprecated setBasicAuthentication method
    private String storedUser;
    private String storedPassword;

    // pool of worker instances
    protected Stack pool = new Stack();
    protected int workers = 0;
    protected int asyncWorkers = 0;
    protected XmlRpcTransportFactory transportFactory;

    // a queue of calls to be handled asynchronously
    private CallData first, last;

    /**
     * Construct a XML-RPC client with this URL and a specified transport
     * factory.
     */
    public XmlRpcClient(URL url, XmlRpcTransportFactory transportFactory)
    {
       this.url = url;
       this.transportFactory = transportFactory;
    }

    /**
     * Construct a XML-RPC client with this URL.
     */
    public XmlRpcClient(URL url)
    {
        this.url = url;
        if (XmlRpc.debug)
        {
            System.out.println("Created client to url space " + url);
        }
    }

    /**
     * Construct a XML-RPC client for the URL represented by this String.
     */
    public XmlRpcClient(String url) throws MalformedURLException
    {
        this(new URL(url));
    }

    /**
     * Construct a XML-RPC client for the specified hostname and port.
     */
    public XmlRpcClient(String hostname, int port) throws MalformedURLException
    {
        this(new URL("http://" + hostname + ':' + port + "/RPC2"));
    }

    /**
     * Return the URL for this XML-RPC client.
     */
    public URL getURL()
    {
        return url;
    }

    /**
     * Sets Authentication for this client. This will be sent as Basic
     * Authentication header to the server as described in
     * <a href="http://www.ietf.org/rfc/rfc2617.txt">
     * http://www.ietf.org/rfc/rfc2617.txt</a>.
     *
     * This method has been deprecated.  Furthermore, it has no
     * effect on the overloads for execute and executeAsync that
     * use an XmlRpcClientRequest or an XmlRpcTransport.
     *
     * @deprecated Authentication is now handled by each XmlRpcTransport
     *
     * @see DefaultXmlRpcTransport
     * @see LiteXmlRpcTransport
     * @see CommonsXmlRpcTransport
     */
    public void setBasicAuthentication(String user, String password)
    {
        /*
         * Store for use in execute and executeAsync
         *
         * Will be unnecessary once this method is removed.
         */
        storedUser = user;
        storedPassword = password;
    }

    /**
     * Generate an XML-RPC request and send it to the server. Parse the result
     * and return the corresponding Java object.
     *
     * @exception XmlRpcException: If the remote host returned a fault message.
     * @exception IOException: If the call could not be made because of lower
     *          level problems.
     */
    public Object execute(String method, Vector params)
            throws XmlRpcException, IOException
    {
        /* Setting user and password on transport if setBasicAuthentication was 
         * used and there is no XmlRpcTransportFactory.  As setBasicAuthentication 
         * is deprecated, this should be removed in a future version.
         */
        if ((storedUser != null) && (storedPassword != null) && (transportFactory == null))
        {
            DefaultXmlRpcTransport transport = createDefaultTransport();
            transport.setBasicAuthentication(storedUser, storedPassword);
            return execute(new XmlRpcRequest(method, params), transport);
        }
        else
        {
            return execute(new XmlRpcRequest(method, params));
        }
    }

    public Object execute(XmlRpcClientRequest request)
            throws XmlRpcException, IOException
    {
        return execute(request, createTransport());
    }

    public Object execute(XmlRpcClientRequest request, XmlRpcTransport transport)
            throws XmlRpcException, IOException
    {
        XmlRpcClientWorker worker = getWorker(false);
        try
        {
            Object retval = worker.execute(request, transport);
            return retval;
        }
        finally
        {
            releaseWorker(worker, false);
        }
    }
    /**
     * Generate an XML-RPC request and send it to the server in a new thread.
     * This method returns immediately.
     * If the callback parameter is not null, it will be called later to handle
     * the result or error when the call is finished.
     */
    public void executeAsync(String method, Vector params,
            AsyncCallback callback)
    {
        XmlRpcRequest request = new XmlRpcRequest(method, params);
        if ((storedUser != null) && (storedPassword != null) && (transportFactory == null))
        {
            DefaultXmlRpcTransport transport = createDefaultTransport();
            transport.setBasicAuthentication(storedUser, storedPassword);
            executeAsync(request, callback, transport);
        }
        else
        {
            executeAsync(request, callback);
        }
    }

    public void executeAsync(XmlRpcClientRequest request,
            AsyncCallback callback)
    {
        executeAsync(request, callback, null);
    }

    public void executeAsync(XmlRpcClientRequest request,
            AsyncCallback callback, XmlRpcTransport transport)
    {
        CallData call = new CallData(request, callback, transport);

        // if at least 4 threads are running, don't create any new ones,
        // just enqueue the request.
        if (asyncWorkers >= 4)
        {
            enqueue(call);
            return;
        }
        XmlRpcClientWorker worker = null;
        try
        {
            new XmlRpcClientAsyncThread(getWorker(true), call).start();
        }
        catch(IOException iox)
        {
            // make a queued worker that doesn't run immediately
            enqueue(call);
        }
    }

    class XmlRpcClientAsyncThread extends Thread
    {
        protected XmlRpcClientWorker worker;
        protected CallData call;

        protected XmlRpcClientAsyncThread(XmlRpcClientWorker worker, CallData initialCall)
        {
           this.worker = worker;
           this.call = initialCall;
        }

        public void run()
        {
            try
            {
                while (call != null)
                {
                    call = dequeue();
                    executeAsync(call.request, call.callback, call.transport);
                }
            }
            finally
            {
                releaseWorker(worker, true);
            }
        }

        /**
         * Execute an XML-RPC call and handle asyncronous callback.
         */
        void executeAsync(XmlRpcClientRequest request, AsyncCallback callback, XmlRpcTransport transport)
        {
            Object res = null;
            try
            {
                if (transport == null)
                {
                    transport = createTransport();
                }
                res = worker.execute(request, transport);
                // notify callback object
                if (callback != null)
                {
                    callback.handleResult(res, url, request.getMethodName());
                }
            }
            catch(Exception x)
            {
                if (callback != null)
                {
                    try
                    {
                        callback.handleError(x, url, request.getMethodName());
                    }
                    catch(Exception ignore)
                    {
                    }
                }
            }
        }
    }
    /**
     *
     * @param async
     * @return
     * @throws IOException
     */
    synchronized XmlRpcClientWorker getWorker(boolean async) throws IOException
    {
        try
        {
            XmlRpcClientWorker w = (XmlRpcClientWorker) pool.pop();
            if (async)
            {
                asyncWorkers += 1;
            }
            else
            {
                workers += 1;
            }
            return w;
        }
        catch(EmptyStackException x)
        {
            if (workers < XmlRpc.getMaxThreads())
            {
                if (async)
                {
                    asyncWorkers += 1;
                }
                else
                {
                    workers += 1;
                }
                return new XmlRpcClientWorker();
            }
            throw new IOException("XML-RPC System overload");
        }
    }

    /**
     * Release possibly big per-call object references to allow them to be
     * garbage collected
     */
    synchronized void releaseWorker(XmlRpcClientWorker w, boolean async)
    {
        if (pool.size() < 20)
        {
            pool.push(w);
        }
        if (async)
        {
            asyncWorkers -= 1;
        }
        else
        {
            workers -= 1;
        }
    }

    /**
     *
     * @param method
     * @param params
     * @param callback
     */
    synchronized void enqueue(CallData call)
    {
        if (last == null)
        {
            first = last = call;
        }
        else
        {
            last.next = call;
            last = call;
        }
    }

    /**
     *
     * @return
     */
    synchronized CallData dequeue()
    {
        if (first == null)
        {
            return null;
        }
        CallData call = first;
        if (first == last)
        {
            first = last = null;
        }
        else
        {
            first = first.next;
        }
        return call;
    }

    class CallData
    {
        XmlRpcClientRequest request;
        XmlRpcTransport transport;
        AsyncCallback callback;
        CallData next;

        /**
         * Make a call to be queued and then executed by the next free async
         * thread
         */
        public CallData(XmlRpcClientRequest request, AsyncCallback callback, XmlRpcTransport transport)
        {
            this.request = request;
            this.callback = callback;
            this.transport = transport;
            this.next = null;
        }
    }

    protected XmlRpcTransport createTransport() throws XmlRpcClientException
    {
        if (transportFactory == null)
        {
          return createDefaultTransport();
        }
        return transportFactory.createTransport();
    }
    
    private DefaultXmlRpcTransport createDefaultTransport() {
        return new DefaultXmlRpcTransport(url);
    }

    /**
     * Just for testing.
     */
    public static void main(String args[]) throws Exception
    {
        // XmlRpc.setDebug(true);
        // XmlRpc.setKeepAlive(true);
        try
        {
            String url = args[0];
            String method = args[1];
            Vector v = new Vector();
            for (int i = 2; i < args.length; i++)
            {
                try
                {
                    v.addElement(new Integer(Integer.parseInt(args[i])));
                }
                catch(NumberFormatException nfx)
                {
                    v.addElement(args[i]);
                }
            }
            XmlRpcClient client = new XmlRpcClient(url);
            try
            {
                System.out.println(client.execute(method, v));
            }
            catch(Exception ex)
            {
                System.err.println("Error: " + ex.getMessage());
            }
        }
        catch(Exception x)
        {
            System.err.println(x);
            System.err.println("Usage: java org.apache.xmlrpc.XmlRpcClient "
                    + "<url> <method> <arg> ....");
            System.err.println("Arguments are sent as integers or strings.");
        }
    }
}
