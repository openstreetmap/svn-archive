package org.apache.xmlrpc;

/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.Hashtable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.apache.xmlrpc.util.HttpUtil;

/**
 * Default XML-RPC transport factory, produces HTTP, HTTPS with SSL or TLS based on URI protocol.
 *
 * @author <a href="mailto:lmeader@ghsinc.com">Larry Meader</a>
 * @author <a href="mailto:cjackson@ghsinc.com">Chris Jackson</a>
 * @author <a href="mailto:andrew@kungfoocoder.org">Andrew Evers</a>
 * @version $Id: DefaultXmlRpcTransportFactory.java,v 1.4 2003/02/25 08:33:17 aevers Exp $
 * @since 1.2
 */
public class DefaultXmlRpcTransportFactory implements XmlRpcTransportFactory 
{
    // Default properties for new http transports
    protected URL url;
    protected String auth;

    protected static XmlRpcTransportFactory httpsTransportFactory;

    public static final String DEFAULT_HTTPS_PROVIDER = "comnetsun";

    private static Hashtable transports = new Hashtable (1);

    static
    {
        // A mapping of short identifiers to the fully qualified class names of
        // common transport factories. If more mappings are added here,
        // increase the size of the transports Hashtable used to store them.
        transports.put("comnetsun", "org.apache.xmlrpc.secure.sunssl.SunSSLTransportFactory");
    }

    public static void setHTTPSTransport(String transport, Properties properties)
        throws XmlRpcClientException
    {
        httpsTransportFactory = createTransportFactory(transport, properties);    
    }

    public static XmlRpcTransportFactory createTransportFactory(String transport, Properties properties)
        throws XmlRpcClientException
    {
        String transportFactoryClassName = null;
        Class transportFactoryClass;
        Constructor transportFactoryConstructor;
        Object transportFactoryInstance;

        try
        {
            transportFactoryClassName = (String) transports.get(transport);
            if (transportFactoryClassName == null)
            {
                // Identifier lookup failed, assuming we were provided
                // with the fully qualified class name.
                transportFactoryClassName = transport;
            }
            transportFactoryClass = Class.forName(transportFactoryClassName);

            transportFactoryConstructor = transportFactoryClass.getConstructor(
                XmlRpcTransportFactory.CONSTRUCTOR_SIGNATURE);
            transportFactoryInstance = transportFactoryConstructor.newInstance(
                new Object [] { properties });
            if (transportFactoryInstance instanceof XmlRpcTransportFactory)
            {
                return (XmlRpcTransportFactory) transportFactoryInstance;
            }
            else
            {
                throw new XmlRpcClientException("Class '" + 
                    transportFactoryClass.getName() + "' does not implement '" +
                    XmlRpcTransportFactory.class.getName() + "'", null);
            }
        }
        catch (ClassNotFoundException cnfe)
        {
            throw new XmlRpcClientException("Transport Factory not found: " +
                transportFactoryClassName, cnfe);
        }
        catch (NoSuchMethodException nsme)
        {
            throw new XmlRpcClientException("Transport Factory constructor not found: " +
                transportFactoryClassName + 
                XmlRpcTransportFactory.CONSTRUCTOR_SIGNATURE_STRING, nsme);
        }
        catch (IllegalAccessException iae)
        {
            throw new XmlRpcClientException("Unable to access Transport Factory constructor: " +
                transportFactoryClassName, iae);
        }
        catch (InstantiationException ie)
        {
            throw new XmlRpcClientException("Unable to instantiate Transport Factory: " +
                transportFactoryClassName, ie);
        }
        catch (InvocationTargetException ite)
        {
            throw new XmlRpcClientException("Error calling Transport Factory constructor: ",
                ite.getTargetException());
        }
    }
  
    public DefaultXmlRpcTransportFactory(URL url)
    {
        this.url = url;
    }
    
    /**
     * Contructor taking a Base64 encoded Basic Authentication string.
     *
     * @deprecated use setBasicAuthentication method instead
     */
    public DefaultXmlRpcTransportFactory(URL url, String auth)
    {
        this(url);
        this.auth = auth;
    }
    
    public XmlRpcTransport createTransport() 
    throws XmlRpcClientException
    {
        if ("https".equals(url.getProtocol()))
        {
            if (httpsTransportFactory == null)
            {
                Properties properties = new Properties();
 
                properties.put(XmlRpcTransportFactory.TRANSPORT_URL, url);
                properties.put(XmlRpcTransportFactory.TRANSPORT_AUTH, auth);
 
                setHTTPSTransport(DEFAULT_HTTPS_PROVIDER, properties);
            }
  
            return httpsTransportFactory.createTransport();
        }
         
        return new DefaultXmlRpcTransport(url);
    }
    
    /**
     * Sets Authentication for this client. This will be sent as Basic
     * Authentication header to the server as described in
     * <a href="http://www.ietf.org/rfc/rfc2617.txt">
     * http://www.ietf.org/rfc/rfc2617.txt</a>.
     */
    public void setBasicAuthentication(String user, String password)
    {
        setProperty(TRANSPORT_AUTH, HttpUtil.encodeBasicAuthentication(user, password));
    }

    public void setProperty(String propertyName, Object value)
    {
        if (httpsTransportFactory != null)
        {
            httpsTransportFactory.setProperty(propertyName, value);
        }
        if (TRANSPORT_AUTH.equals(propertyName))
        {
            auth = (String) value;
        }
        else if (TRANSPORT_URL.equals(propertyName))
        {
            url = (URL) value;
        }
    }
}
