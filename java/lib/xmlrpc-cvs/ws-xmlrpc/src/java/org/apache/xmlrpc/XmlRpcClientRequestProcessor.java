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

import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.util.Vector;

/**
 * Process an XML-RPC client request into a byte array or directly onto
 * an OutputStream.
 *
 * @author <a href="mailto:hannes@apache.org">Hannes Wallnoefer</a>
 * @author <a href="mailto:andrew@kungfoocoder.org">Andrew Evers</a>
 * @version $Id: XmlRpcClientRequestProcessor.java,v 1.1 2002/12/05 08:49:24 aevers Exp $
 * @since 1.2
 */
public class XmlRpcClientRequestProcessor
{
    /**
     * Encode a request from the XmlClientRpcRequest implementation to an
     * output stream in the specified character encoding.
     *
     * @param request the request to encode.
     * @param encoding the Java name for the encoding to use.
     * @return byte [] the encoded request.
     */
    public void encodeRequest(XmlRpcClientRequest request, String encoding, OutputStream out)
    throws XmlRpcClientException, IOException
    {
        XmlWriter writer;

        writer = new XmlWriter(out, encoding);
       
        writer.startElement("methodCall");
        writer.startElement("methodName");
        writer.write(request.getMethodName());
        writer.endElement("methodName");
        writer.startElement("params");

        int l = request.getParameterCount();
        for (int i = 0; i < l; i++)
        {
            writer.startElement("param");
            writer.writeObject(request.getParameter(i));
            writer.endElement("param");
        }
        writer.endElement("params");
        writer.endElement("methodCall");
        writer.flush();
    }

    /**
     * Encode a request from the XmlRpcClientRequest implementation to a
     * byte array representing the XML-RPC call, in the specified character
     * encoding.
     *
     * @param request the request to encode.
     * @param encoding the Java name for the encoding to use.
     * @return byte [] the encoded request.
     */
    public byte [] encodeRequestBytes(XmlRpcClientRequest request, String encoding)
    throws XmlRpcClientException
    {
        ByteArrayOutputStream buffer;

        try
        {
            buffer = new ByteArrayOutputStream();
            encodeRequest(request, encoding, buffer);
            return buffer.toByteArray();
        }
        catch (IOException ioe)
        {
            throw new XmlRpcClientException("Error occured encoding XML-RPC request", ioe);
        }
    }

    /**
     * Called by the worker management framework to see if this object can be
     * re-used. Must attempt to clean up any state, and return true if it can
     * be re-used.
     *
     * @return boolean true if this objcet has been cleaned up and may be re-used.
     */
    protected boolean canReUse()
    {
        return true;
    }
}
