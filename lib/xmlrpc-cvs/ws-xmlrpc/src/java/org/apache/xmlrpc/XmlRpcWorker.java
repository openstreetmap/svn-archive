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

import java.io.InputStream;
import java.io.IOException;

/**
 * Tie together the XmlRequestProcessor and XmlResponseProcessor to handle
 * a request serially in a single thread.
 *
 * @author <a href="mailto:hannes@apache.org">Hannes Wallnoefer</a>
 * @author Daniel L. Rall
 * @author <a href="mailto:andrew@kungfoocoder.org">Andrew Evers</a>
 * @see org.apache.xmlrpc.XmlRpcServer
 * @since 1.2
 */
public class XmlRpcWorker
{
    protected XmlRpcRequestProcessor requestProcessor;
    protected XmlRpcResponseProcessor responseProcessor;
    protected XmlRpcHandlerMapping handlerMapping;

    /**
     * Create a new instance that will use the specified mapping.
     */
    public XmlRpcWorker(XmlRpcHandlerMapping handlerMapping)
    {
      requestProcessor = new XmlRpcRequestProcessor();
      responseProcessor = new XmlRpcResponseProcessor();
      this.handlerMapping = handlerMapping;
    }

    /**
     * Pass the specified request to the handler. The handler should be an
     * instance of {@link org.apache.xmlrpc.XmlRpcHandler} or
     * {@link org.apache.xmlrpc.AuthenticatedXmlRpcHandler}.
     *
     * @param handler the handler to call.
     * @param request the request information to use.
     * @param context the context information to use.
     * @return Object the result of calling the handler.
     * @throws ClassCastException if the handler is not of an appropriate type.
     * @throws NullPointerException if the handler is null.
     * @throws Exception if the handler throws an exception.
     */
    protected static Object invokeHandler(Object handler, XmlRpcServerRequest request, XmlRpcContext context)
        throws Exception
    {
        long now = 0;

        try
        {
            if (XmlRpc.debug)
            {
                now = System.currentTimeMillis();
            }
            if (handler == null)
            {
              throw new NullPointerException
                  ("Null handler passed to XmlRpcWorker.invokeHandler");
            }
            else if (handler instanceof ContextXmlRpcHandler)
            {
                return ((ContextXmlRpcHandler) handler).execute
                    (request.getMethodName(), request.getParameters(), context);
            }
            else if (handler instanceof XmlRpcHandler)
            {
                return ((XmlRpcHandler) handler).execute
                    (request.getMethodName(), request.getParameters());
            }
            else if (handler instanceof AuthenticatedXmlRpcHandler)
            {
                return ((AuthenticatedXmlRpcHandler) handler)
                    .execute(request.getMethodName(), request.getParameters(),
                             context.getUserName(), context.getPassword());
            }
            else
            {
               throw new ClassCastException("Handler class " +
                                            handler.getClass().getName() +
                                            " is not a valid XML-RPC handler");
            }
        }
        finally
        {
            if (XmlRpc.debug)
            {
                 System.out.println("Spent " + (System.currentTimeMillis() - now)
                         + " millis processing request");
            }
        }
    }

    /**
     * Decode, process and encode the response or exception for an XML-RPC
     * request. This method executes the handler method with the default context.
     */
    public byte[] execute(InputStream is, String user, String password)
    {
        return execute(is, defaultContext(user, password));
    }

    /**
     * Decode, process and encode the response or exception for an XML-RPC
     * request. This method executes will pass the specified context to the
     * handler if the handler supports context.
     *
     * @param is the InputStream to read the request from.
     * @param context the context for the request (may be null).
     * @return byte[] the response.
     * @throws org.apache.xmlrpc.ParseFailed if the request could not be parsed.
     * @throws org.apache.xmlrpc.AuthenticationFailed if the handler for the
     * specific method required authentication and insufficient credentials were
     * supplied.
     */
    public byte[] execute(InputStream is, XmlRpcContext context)
    {
        long now = 0;

        if (XmlRpc.debug)
        {
            now = System.currentTimeMillis();
        }

        try
        {
            XmlRpcServerRequest request = requestProcessor.decodeRequest(is);
            Object handler = handlerMapping.getHandler(request.
                                                       getMethodName());
            Object response = invokeHandler(handler, request, context);
            return responseProcessor.encodeResponse
                (response, requestProcessor.getEncoding());
        }
        catch (AuthenticationFailed alertCallerAuth)
        {
            throw alertCallerAuth;
        }
        catch (ParseFailed alertCallerParse)
        {
            throw alertCallerParse;
        }
        catch (Exception x)
        {
            if (XmlRpc.debug)
            {
                x.printStackTrace();
            }
            return responseProcessor.encodeException
                (x, requestProcessor.getEncoding());
        }
        finally
        {
            if (XmlRpc.debug)
            {
                System.out.println("Spent " + (System.currentTimeMillis() - now)
                                   + " millis in request/process/response");
            }
        }
    }

    /**
     * Factory method to return a default context object for the execute() method.
     * This method can be overridden to return a custom sub-class of XmlRpcContext.
     *
     * @param user the username of the user making the request.
     * @param password the password of the user making the request.
     * @return XmlRpcContext the context for the reqeust.
     */
    protected XmlRpcContext defaultContext(String user, String password)
    {
        return new DefaultXmlRpcContext(user, password, handlerMapping);
    }
}
