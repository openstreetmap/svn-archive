package org.apache.xmlrpc;

/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright(c) 2001,2002 The Apache Software Foundation.  All rights
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

import java.io.IOException;
import java.io.InputStream;
import java.util.EmptyStackException;
import java.util.Stack;

/**
 * A multithreaded, reusable XML-RPC server object. The name may be misleading
 * because this does not open any server sockets. Instead it is fed by passing
 * an XML-RPC input stream to the execute method. If you want to open a
 * HTTP listener, use the WebServer class instead.
 *
 * @author <a href="mailto:hannes@apache.org">Hannes Wallnoefer</a>
 * @author Daniel L. Rall
 * @author <a href="mailto:andrew@kungfoocoder.org">Andrew Evers</a>
 */
public class XmlRpcServer
{
    private Stack pool;
    private int nbrWorkers;

    /**
     * We want the <code>$default</code> handler to always be
     * available.
     */
    private DefaultHandlerMapping handlerMapping;

    /**
     * Construct a new XML-RPC server. You have to register handlers
     * to make it do something useful.
     */
    public XmlRpcServer()
    {
        pool = new Stack();
        nbrWorkers = 0;
        handlerMapping = new DefaultHandlerMapping();
    }

    /**
     * @see org.apache.xmlrpc.DefaultHandlerMapping#addHandler(String, Object)
     */
    public void addHandler(String handlerName, Object handler)
    {
        handlerMapping.addHandler(handlerName, handler);
    }

    /**
     * @see org.apache.xmlrpc.DefaultHandlerMapping#removeHandler(String)
     */
    public void removeHandler(String handlerName)
    {
        handlerMapping.removeHandler(handlerName);
    }

    /**
     * Return the current XmlRpcHandlerMapping.
     */
    public XmlRpcHandlerMapping getHandlerMapping()
    {
        return handlerMapping;
    }

    /**
     * Parse the request and execute the handler method, if one is
     * found. Returns the result as XML.  The calling Java code
     * doesn't need to know whether the call was successful or not
     * since this is all packed into the response. No context information
     * is passed.
     */
    public byte[] execute(InputStream is)
    {
        return execute(is, new DefaultXmlRpcContext(null, null, getHandlerMapping()));
    }

    /**
     * Parse the request and execute the handler method, if one is
     * found. If the invoked handler is AuthenticatedXmlRpcHandler,
     * use the credentials to authenticate the user. No context information
     * is passed.
     */
    public byte[] execute(InputStream is, String user, String password)
    {
        return execute(is, new DefaultXmlRpcContext(user, password, getHandlerMapping()));
    }
    
    /**
     * Parse the request and execute the handler method, if one is
     * found. If the invoked handler is AuthenticatedXmlRpcHandler,
     * use the credentials to authenticate the user. Context information
     * is passed to the worker, and may be passed to the request handler.
     */
    public byte[] execute(InputStream is, XmlRpcContext context)
    {
        XmlRpcWorker worker = getWorker();
        try
        {
            return worker.execute(is, context);
        }
        finally
        {
            pool.push(worker);
        }
    }

    /**
     * Hands out pooled workers.
     *
     * @return A worker (never <code>null</code>).
     * @throws RuntimeException If the server exceeds its maximum
     * number of allowed requests.
     */
    protected XmlRpcWorker getWorker()
    {
        try
        {
            return (XmlRpcWorker) pool.pop();
        }
        catch(EmptyStackException x)
        {
            int maxThreads = XmlRpc.getMaxThreads();
            if (nbrWorkers < maxThreads)
            {
                nbrWorkers += 1;
                if (nbrWorkers >= maxThreads * .95)
                {
                    System.out.println("95% of XML-RPC server threads in use");
                }
                return createWorker();
            }
            throw new RuntimeException("System overload: Maximum number of " +
                                       "concurrent requests (" + maxThreads +
                                       ") exceeded");
        }
    }

    protected XmlRpcWorker createWorker()
    {
        return new XmlRpcWorker(handlerMapping);
    }
}
