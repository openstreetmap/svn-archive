package org.apache.xmlrpc.secure.sunssl;

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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import java.security.Security;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import org.apache.xmlrpc.secure.SecurityTool;
import org.apache.xmlrpc.XmlRpcTransportFactory;
import org.apache.xmlrpc.DefaultXmlRpcTransport;
import org.apache.xmlrpc.XmlRpcTransport;

import com.sun.net.ssl.X509TrustManager;
import com.sun.net.ssl.HostnameVerifier;
import com.sun.net.ssl.SSLContext;
import com.sun.net.ssl.HttpsURLConnection;

/**
 * Interface from XML-RPC to the HTTPS transport based on the
 * @see javax.net.ssl.httpsURLConnection class.
 *
 * @author <a href="mailto:lmeader@ghsinc.com">Larry Meader</a>
 * @author <a href="mailto:cjackson@ghsinc.com">Chris Jackson</a> 
 * @author <a href="mailto:andrew@kungfoocoder.org">Andrew Evers</a>
 * @version $Id: SunSSLTransportFactory.java,v 1.2 2003/01/29 13:40:09 aevers Exp $
 * @since 1.2
 */
public class SunSSLTransportFactory implements XmlRpcTransportFactory
{
    protected URL url;
    protected String auth;

    public static final String TRANSPORT_TRUSTMANAGER = "hostnameverifier";
    public static final String TRANSPORT_HOSTNAMEVERIFIER = "trustmanager";

    // The openTrustManager trusts all certificates
    private static X509TrustManager openTrustManager = new X509TrustManager()
    {
        public boolean isClientTrusted(X509Certificate[] chain)
        {
            return true;
        }
 
        public boolean isServerTrusted(X509Certificate[] chain)
        {
            return true;
        }
 
        public X509Certificate[] getAcceptedIssuers() 
        {
            return null;
        }
    };

    // The openHostnameVerifier trusts all hostnames
    private static HostnameVerifier openHostnameVerifier = new HostnameVerifier() 
    {
        public boolean verify(String hostname, String session) 
        {
            return true;
        }
    };

    public static Properties getProperties()
    {
        Properties properties = new Properties();

        properties.setProperty(XmlRpcTransportFactory.TRANSPORT_URL, "(java.net.URL) - URL to connect to");
        properties.setProperty(XmlRpcTransportFactory.TRANSPORT_AUTH, "(java.lang.String) - HTTP Basic Authentication string (encoded).");
        properties.setProperty(TRANSPORT_TRUSTMANAGER, "(com.sun.net.ssl.X509TrustManager) - X.509 Trust Manager to use");
        properties.setProperty(TRANSPORT_HOSTNAMEVERIFIER, "(com.sun.net.ssl.HostnameVerifier) - Hostname verifier to use");

        return properties;
    }

    public SunSSLTransportFactory(Properties properties)
    throws GeneralSecurityException
    {
        X509TrustManager trustManager;
        HostnameVerifier hostnameVerifier;
        SSLContext sslContext;

        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

        url = (URL) properties.get(XmlRpcTransportFactory.TRANSPORT_URL);
        auth = properties.getProperty(XmlRpcTransportFactory.TRANSPORT_AUTH);

        trustManager = (X509TrustManager) properties.get(TRANSPORT_TRUSTMANAGER);
        if (trustManager == null)
        {
            trustManager = openTrustManager;
        }

        hostnameVerifier = (HostnameVerifier) properties.get(TRANSPORT_HOSTNAMEVERIFIER);
        if (hostnameVerifier == null)
        {
            hostnameVerifier = openHostnameVerifier;
        }  

        sslContext = SSLContext.getInstance(SecurityTool.getSecurityProtocol());
        X509TrustManager[] tmArray = new X509TrustManager[] { trustManager };
        sslContext.init(null, tmArray, new SecureRandom());

        // Set the default SocketFactory and HostnameVerifier
        // for javax.net.ssl.HttpsURLConnection
        if (sslContext != null) 
        {
            HttpsURLConnection.setDefaultSSLSocketFactory(
                sslContext.getSocketFactory());
        }
        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
    }

    public XmlRpcTransport createTransport()
    {
       return new DefaultXmlRpcTransport(url, auth);
    }

    public void setProperty(String propertyName, Object value)
    {
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
