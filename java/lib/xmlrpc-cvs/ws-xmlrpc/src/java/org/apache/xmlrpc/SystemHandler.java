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

import java.util.Vector;

/**
 * Wraps calls to the XML-RPC standard system.* methods (such as
 * <code>system.multicall</code>).
 *
 * @author <a href="mailto:adam@megacz.com">Adam Megacz</a>
 * @author <a href="mailto:andrew@kungfoocoder.org">Andrew Evers</a>
 * @author Daniel L. Rall
 * @since 1.2
 */
public class SystemHandler
implements ContextXmlRpcHandler
{
    private DefaultHandlerMapping systemMapping = null;

    /**
     * Creates a new instance. This instance contains no system calls. Use the
     * addDefaultSystemHandlers() method to add the 'default' set of handlers,
     * or add handlers manually.
     */
    public SystemHandler()
    {
        this.systemMapping = new DefaultHandlerMapping();
    }

   /**
     * Creates a new instance that delegates calls via the
     * specified {@link org.apache.xmlrpc.XmlRpcHandlerMapping}. This
     * method will add the system.multicall handler when a non-null
     * handlerMapping is specified. The value itself is ignored.
     *
     * @deprecated use new SystemHandler() and addDefaultSystemHandlers() instead.
     */
    public SystemHandler(XmlRpcHandlerMapping handlerMapping)
    {
        this();
        if (handlerMapping != null)
        {
          addDefaultSystemHandlers();
        }
    }

    /**
     * Creates a new instance that delegates its multicalls via
     * the mapping used by the specified {@link org.apache.xmlrpc.XmlRpcServer}.
     * This method will add the default handlers when the specfied server's
     * getHandlerMapping() returns a non-null handler mapping.
     *
     * @param server The server to retrieve the XmlRpcHandlerMapping from.
     *
     * @deprecated use new SystemHandler() and addDefaultSystemHandlers() instead.
     */
    protected SystemHandler(XmlRpcServer server)
    {
        this(server.getHandlerMapping());
    }

    /**
     * Add the default system handlers. The default system handlers are:
     * <dl>
     *  <dt>system.multicall</dt>
     *  <dd>Make multiple XML-RPC calls in one request and receive multiple
     *  responses.</dd>
     * </dl>
     */
    public void addDefaultSystemHandlers()
    {
        addSystemHandler("multicall", new MultiCall());
    }

    /**
     * @see org.apache.xmlrpc.DefaultHandlerMapping#addHandler(String, Object)
     */
    public void addSystemHandler(String handlerName, ContextXmlRpcHandler handler)
    {
        systemMapping.addHandler(handlerName, handler);
    }

    /**
     * @see org.apache.xmlrpc.DefaultHandlerMapping#removeHandler(String)
     */
    public void removeSystemHandler(String handlerName)
    {
        systemMapping.removeHandler(handlerName);
    }

    /**
     * Execute a &lt;ignored&gt;.&lt;name&gt; call by calling the handler for
     * &lt;name&gt; in the the system handler mapping.
     */
    public Object execute(String method, Vector params, XmlRpcContext context)
            throws Exception
    {
        Object handler = null;
        String systemMethod = null;
        int dot = method.lastIndexOf('.');
        if (dot > -1)
        {
            // The last portion of the XML-RPC method name is the systen
	    // method name. 
	    systemMethod = method.substring(dot + 1);

            // Add the "." in at the end, the systemMapping will strip it off
            handler = systemMapping.getHandler(systemMethod + ".");
            if (handler != null)
            {
                return ((ContextXmlRpcHandler) handler).execute(systemMethod, params, context);
            }
        }

        throw new NoSuchMethodException("No method '" + method + "' registered.");
    }
}
