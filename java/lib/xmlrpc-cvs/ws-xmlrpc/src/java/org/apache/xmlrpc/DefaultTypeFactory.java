package org.apache.xmlrpc;

/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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

import java.text.ParseException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.DecoderException;
import org.apache.xmlrpc.util.DateTool;

/**
 * The default implementation of the <code>TypeFactory</code>
 * interface.  Provides the following mappings:
 *
 * <table cellpadding="3" cellspacing="2" border="1" width="100%">
 *   <tr><th>XML-RPC data type</th>         <th>Java class</th></tr>
 *   <tr><td>&lt;i4&gt; or &lt;int&gt;</td> <td>java.lang.Integer</td></tr>
 *   <tr><td>&lt;boolean&gt;</td>           <td>java.lang.Boolean</td></tr>
 *   <tr><td>&lt;string&gt;</td>            <td>java.lang.String</td></tr>
 *   <tr><td>&lt;double&gt;</td>            <td>java.lang.Double</td></tr>
 *   <tr><td>&lt;dateTime.iso8601&gt;</td>  <td>java.util.Date</td></tr>
 *   <tr><td>&lt;base64&gt;</td>            <td>byte[ ]</td></tr> 
 * </table>
 *
 * @author <a href="mailto:andrew@kungfoocoder.org">Andrew Evers</a>
 * @see org.apache.xmlrpc.TypeFactory
 * @since 1.2
 */
public class DefaultTypeFactory
    implements TypeFactory
{
    /**
     * Thread-safe wrapper for the <code>DateFormat</code> object used
     * to parse date/time values.
     */
    private static DateTool dateTool = new DateTool();
    private static final Base64 base64Codec = new Base64();

    /**
     * Creates a new instance.
     */
    public DefaultTypeFactory()
    {
    }

    public Object createInteger(String cdata)
    {
        return new Integer(cdata.trim());
    }

    public Object createBoolean(String cdata)
    {
        return ("1".equals(cdata.trim ())
               ? Boolean.TRUE : Boolean.FALSE);
    }

    public Object createDouble(String cdata)
    {
        return new Double(cdata.trim ());

    }

    public Object createDate(String cdata)
    {
        try
        {
            return dateTool.parse(cdata.trim());
        }
        catch (ParseException p)
        {
            throw new RuntimeException(p.getMessage());
        }
    }

    public Object createBase64(String cdata)
    {
        try
        {
            return base64Codec.decode((Object) cdata.getBytes());
        }
        catch (DecoderException e) {
            //TODO: consider throwing an exception here?
            return new byte[0];
        }
    }

    public Object createString(String cdata)
    {
        return cdata;
    }
}
