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
import java.io.InputStream;

import org.xml.sax.AttributeList;
import org.xml.sax.SAXException;

/**
 * Process an XML-RPC server response from a byte array or an
 * InputStream into an Object. Optionally throw the result object
 * if it is an exception.
 *
 * @author <a href="mailto:hannes@apache.org">Hannes Wallnoefer</a>
 * @author <a href="mailto:andrew@kungfoocoder.org">Andrew Evers</a>
 * @version $Id: XmlRpcClientResponseProcessor.java,v 1.1 2002/12/05 08:49:24 aevers Exp $
 * @since 1.2
 */
public class XmlRpcClientResponseProcessor extends XmlRpc
{
    /** The result of the XML-RPC operation. Possibly an XmlRpcException */
    protected Object result;

    /** Set to true if a fault occured on the server. */
    protected boolean fault;

    /**
     * Decode an XML-RPC response from the specified InputStream. This
     * method will parse the input and return an Object. The result
     * will be an XmlRpcException if the server returned a fault, or
     * an Object if the server returned a result.
     *
     * @param is the stream to read from.
     * @return Object an XmlRpcException if an error occured, or the response.
     */
    public Object decodeResponse(InputStream is)
    throws XmlRpcClientException
    {
        result = null;
        fault = false;
        try
        {
            parse(is);
            if (fault)
            {
                return decodeException(result);
            }
            else
            {
                return result;
            }
        }
        catch (Exception x)
        {
            throw new XmlRpcClientException("Error decoding XML-RPC response", x);
        }
    }

    /**
     * Decode an exception from the result returned from the remote server.
     * This method both returns and throws an XmlRpcException. If it returns an
     * XmlRpcException then that is the exception thrown on the remote side. If
     * it throws an exception then an exception occured locally when decoding
     * the response
     *
     * @param result the result from the remote XML-RPC server.
     * @throws XmlRpcClientException if the result could not be processed.
     * @return XmlRpcException the processed response from the server.
     */
    protected XmlRpcException decodeException(Object result)
    throws XmlRpcClientException
    {
        Hashtable exceptionData;
        
        try
        {
            exceptionData = (Hashtable) result; 
            return new XmlRpcException(
                Integer.parseInt(exceptionData.get("faultCode").toString()),
                (String) exceptionData.get("faultString")
            );
        }
        catch (Exception x)
        {
            throw new XmlRpcClientException("Error decoding XML-RPC exception response", x);
        }
    }

    protected void objectParsed(Object what)
    {
        result = what;
    }

    /**
     * Overrides method in XmlRpc to handle fault repsonses.
     */
    public void startElement(String name, AttributeList atts)
            throws SAXException
    {
        if ("fault".equals(name))
        {
            fault = true;
        }
        else
        {
            super.startElement(name, atts);
        }
    }

    /**
     * Called by the worker management framework to see if this worker can be
     * re-used. Must attempt to clean up any state, and return true if it can
     * be re-used.
     *
     * @return boolean true if this worker has been cleaned up and may be re-used.
     */
    protected boolean canReUse()
    {
        result = null;
        fault = false;
        return true;
    }
}
