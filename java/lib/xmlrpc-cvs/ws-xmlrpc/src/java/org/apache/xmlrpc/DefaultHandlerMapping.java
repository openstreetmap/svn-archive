package org.apache.xmlrpc;

/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright(c) 2002 The Apache Software Foundation.  All rights
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

import java.util.Hashtable;

/**
 * Provide a default handler mapping, used by the XmlRpcServer. This
 * mapping supports the special handler name "$default" that will
 * handle otherwise unhandled requests.
 *
 * @author <a href="mailto:hannes@apache.org">Hannes Wallnoefer</a>
 * @author Daniel L. Rall
 * @author <a href="mailto:andrew@kungfoocoder.org">Andrew Evers</a>
 * @see org.apache.xmlrpc.XmlRpcServer
 * @since 1.2
 */
public class DefaultHandlerMapping
    implements XmlRpcHandlerMapping
{
    private Hashtable handlers;

    /**
     * Create a new mapping.
     */
    public DefaultHandlerMapping()
    {
        handlers = new Hashtable();
    }

    /**
     * Register a handler object with this name. Methods of this
     * objects will be callable over XML-RPC as
     * "handlername.methodname". For more information about XML-RPC
     * handlers see the <a href="../index.html#1a">main documentation
     * page</a>.
     *
     * @param handlername The name to identify the handler by.
     * @param handler The handler itself.
     */
    public void addHandler(String handlerName, Object handler)
    {
        if (handler instanceof XmlRpcHandler ||
                handler instanceof AuthenticatedXmlRpcHandler ||
                handler instanceof ContextXmlRpcHandler)
        {
            handlers.put(handlerName, handler);
        }
        else if (handler != null)
        {
            handlers.put(handlerName, new Invoker(handler));
        }
    }

    /**
     * Remove a handler object that was previously registered with
     * this server.
     *
     * @param handlerName The name identifying the handler to remove.
     */
    public void removeHandler(String handlerName)
    {
        handlers.remove(handlerName);
    }

    /**
     * Find the handler and its method name for a given method.
     * Implements the <code>XmlRpcHandlerMapping</code> interface.
     *
     * @param methodName The name of the XML-RPC method to find a
     * handler for (this is <i>not</i> the Java method name).
     * @return A handler object and method name.
     * @see org.apache.xmlrpc.XmlRpcHandlerMapping#getHandler(String)
     */
    public Object getHandler(String methodName)
        throws Exception
    {
        Object handler = null;
        String handlerName = null;
        int dot = methodName.lastIndexOf('.');
        if (dot > -1)
        {
            // The last portion of the XML-RPC method name is the Java
            // method name.
            handlerName = methodName.substring(0, dot);
            handler = handlers.get(handlerName);
        }

        if (handler == null)
        {
            handler = handlers.get("$default");

            if (handler == null)
            {
                if (dot > -1)
                {
                    throw new Exception("RPC handler object \""
                                        + handlerName + "\" not found and no "
                                        + "default handler registered");
                }
                else
                {
                    throw new Exception("RPC handler object not found for \""
                                        + methodName
                                        + "\": No default handler registered");
                }
            }
        }

        return handler;
    }
}
