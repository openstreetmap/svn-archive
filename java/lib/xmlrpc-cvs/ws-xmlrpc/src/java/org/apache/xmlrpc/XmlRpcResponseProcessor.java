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

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.util.Hashtable;

/**
 * Process an Object and produce byte array that represents the specified
 * encoding of the output as an XML-RPC response. This is NOT thread safe.
 *
 * @author <a href="mailto:andrew@kungfoocoder.org">Andrew Evers</a>
 * @author <a href="mailto:hannes@apache.org">Hannes Wallnoefer</a>
 * @author Daniel L. Rall
 * @since 1.2
 */
public class XmlRpcResponseProcessor
{
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    /**
     * Process a successful response, and return output in the
     * specified encoding.
     *
     * @param responseParam The response to process.
     * @param encoding The output encoding.
     * @return byte[] The XML-RPC response.
     */
    public byte[] encodeResponse(Object responseParam, String encoding)
        throws IOException, UnsupportedEncodingException, XmlRpcException
    {
        long now = 0;
        if (XmlRpc.debug)
        {
            now = System.currentTimeMillis();
        }

        try
        {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            XmlWriter writer = new XmlWriter(buffer, encoding);
            writeResponse(responseParam, writer);
            writer.flush();
            return buffer.toByteArray();
        }
        finally
        {
            if (XmlRpc.debug)
            {
                System.out.println("Spent " + (System.currentTimeMillis() - now)
                        + " millis encoding response");
            }
        }
    }

    /**
     * Process an exception, and return output in the specified
     * encoding.
     *
     * @param e The exception to process;
     * @param encoding The output encoding.
     * @param code The XML-RPC faultCode.
     * @return byte[] The XML-RPC response.
     */
    public byte[] encodeException(Exception x, String encoding, int code)
    {
        if (XmlRpc.debug)
        {
            x.printStackTrace();
        }
        // Ensure that if there is anything in the buffer, it
        // is cleared before continuing with the writing of exceptions.
        // It is possible that something is in the buffer
        // if there were an exception during the writeResponse()
        // call above.
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        XmlWriter writer = null;
        try
        {
            writer = new XmlWriter(buffer, encoding);
        }
        catch (UnsupportedEncodingException encx)
        {
            System.err.println("XmlRpcServer attempted to use "
                    + "unsupported encoding: " + encx);
            // NOTE: If we weren't already using the default
            // encoding, we could try it here.
        }
        catch (IOException iox)
        {
            System.err.println("XmlRpcServer experienced I/O error "
                    + "writing error response: " + iox);
        }

        String message = x.toString();
        // Retrieve XmlRpcException error code(if possible).
        try
        {
            writeError(code, message, writer);
            writer.flush();
        }
        catch (Exception e)
        {
            // Unlikely to occur, as we just sent a struct
            // with an int and a string.
            System.err.println("Unable to send error response to "
                    + "client: " + e);
        }

        return (writer != null ? buffer.toByteArray() : EMPTY_BYTE_ARRAY);
    }

     /**
     * Process an exception, and return output in the specified
     * encoding.
     *
     * @param e The exception to process;
     * @param encoding The output encoding.
     * @return byte[] The XML-RPC response.
     */
    public byte[] encodeException(Exception x, String encoding)
    {
        return encodeException(x, encoding, (x instanceof XmlRpcException) ? ((XmlRpcException) x).code : 0);
    }
     /**
      * Writes an XML-RPC response to the XML writer.
      */
    void writeResponse(Object param, XmlWriter writer)
        throws XmlRpcException, IOException
    {
        writer.startElement("methodResponse");
        // if (param == null) param = ""; // workaround for Frontier bug
        writer.startElement("params");
        writer.startElement("param");
        writer.writeObject(param);
        writer.endElement("param");
        writer.endElement("params");
        writer.endElement("methodResponse");
    }

    /**
     * Writes an XML-RPC error response to the XML writer.
     */
    void writeError(int code, String message, XmlWriter writer)
        throws XmlRpcException, IOException
    {
        // System.err.println("error: "+message);
        Hashtable h = new Hashtable();
        h.put("faultCode", new Integer(code));
        h.put("faultString", message);
        writer.startElement("methodResponse");
        writer.startElement("fault");
        writer.writeObject(h);
        writer.endElement("fault");
        writer.endElement("methodResponse");
    }
}
