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
import java.util.Vector;

/**
 * Process an InputStream and produce an XmlRpcServerRequest.  This class
 * is NOT thread safe.
 *
 * @author <a href="mailto:andrew@kungfoocoder.org">Andrew Evers</a>
 * @author <a href="mailto:hannes@apache.org">Hannes Wallnoefer</a>
 * @author Daniel L. Rall
 * @since 1.2
 */
public class XmlRpcRequestProcessor extends XmlRpc
{
    private Vector requestParams;

    /**
     * Creates a new instance.
     */
    protected XmlRpcRequestProcessor()
    {
        requestParams = new Vector();
    }

    /**
     * Decode a request from an InputStream to the internal XmlRpcRequest
     * implementation. This method must read data from the specified stream and
     * return an XmlRpcRequest object, or throw an exception.
     *
     * @param is the stream to read the request from.
     * @returns XMLRpcRequest the request.
     * @throws ParseFailed if unable to parse the request.
     */
    public XmlRpcServerRequest decodeRequest(InputStream is)
    {
        long now = 0;

        if (XmlRpc.debug)
        {
            now = System.currentTimeMillis();
        }
        try
        {
            try
            {
                parse(is);
            }
            catch (Exception e)
            {
                throw new ParseFailed(e);
            }
            if (XmlRpc.debug)
            {
                System.out.println("XML-RPC method name: " + methodName);
                System.out.println("Request parameters: " + requestParams);
            }
            // check for errors from the XML parser
            if (errorLevel > NONE)
            {
                throw new ParseFailed(errorMsg);
            }

            return new XmlRpcRequest(methodName, (Vector) requestParams.clone());
        }
        finally
        {
            requestParams.removeAllElements();
            if (XmlRpc.debug)
            {
                System.out.println("Spent " + (System.currentTimeMillis() - now)
                        + " millis decoding request");
            }
        }
    }

    /**
     * Called when an object to be added to the argument list has been
     * parsed.
     *
     * @param what The parameter parsed from the request.
     */
    protected void objectParsed(Object what)
    {
        requestParams.addElement(what);
    }
}
