package org.apache.xmlrpc.secure;

/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights 
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

import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;

/**
 */
public class SecurityTool
    implements SecurityConstants
{
    /**
     * Class name of the security provider to be
     * used by the secure web server.
     */
    protected static String securityProviderClass;
    
    /**
     * The security protocol to be used by the
     * secure web server. Currently the options are
     * SSL and TLS.
     */
    private static String securityProtocol;

    /**
     * Password used to access the key store.
     */
    private static String keyStorePassword;

    /**
     * Format to be used for the key store. With
     * the Sun JSSE the standard "JKS" format is
     * available along with the "PKCS12" format.
     */
    private static String keyStoreType;
    
    /**
     * Path to the key store that will be used by
     * the secure web server.
     */
    private static String keyStore;

    /**
     * Password used to access the key store.
     */
    private static String trustStorePassword;

    /**
     * Format to be used for the key store. With
     * the Sun JSSE the standard "JKS" format is
     * available along with the "PKCS12" format.
     */
    private static String trustStoreType;
    
    /**
     * Path to the key store that will be used by
     * the secure web server.
     */
    private static String trustStore;

    /**
     * The type of key manager to be used by the
     * secure web server. With the Sun JSSE only
     * type available is the X509  type which
     * is implemented in the SunX509 classes.
     */
    private static String keyManagerType;

    /**
     * The protocol handler package to use for
     * the secure web server. This allows the URL
     * class to handle https streams.
     */
    private static String protocolHandlerPackages;

    public static void setup() 
        throws Exception
    {
        /*
         * We want to dynamically register the SunJSSE provider
         * because we don't want people to have to modify their
         * JVM setups manually.
         */
        Security.addProvider((Provider)Class.forName(
            SecurityTool.getSecurityProviderClass()).newInstance());
        
        /*
         * Set the packages that will provide the URL stream
         * handlers that can cope with TLS/SSL.
         */
        System.setProperty(PROTOCOL_HANDLER_PACKAGES,
            SecurityTool.getProtocolHandlerPackages());

        // Setup KeyStore

        System.setProperty(KEY_STORE_TYPE,
            SecurityTool.getKeyStoreType());
        
        System.setProperty(KEY_STORE,
            SecurityTool.getKeyStore());
        
        System.setProperty(KEY_STORE_PASSWORD,
            SecurityTool.getKeyStorePassword());

        // Setup TrustStore
        
        System.setProperty(TRUST_STORE_TYPE,
            SecurityTool.getTrustStoreType());
        
        System.setProperty(TRUST_STORE,
            SecurityTool.getTrustStore());
        
        System.setProperty(TRUST_STORE_PASSWORD,
            SecurityTool.getTrustStorePassword());
    }

    /**
     * Set the protocol handler packages.
     *
     * @param String protocol handler package.
     */
    public static void setProtocolHandlerPackages(String x)
    {
        protocolHandlerPackages = x;
    }        

    /**
     * Get the protocol handler packages.
     *
     * @param String protocol handler package.
     */
    public static String getProtocolHandlerPackages()
    {
        if (System.getProperty(PROTOCOL_HANDLER_PACKAGES) != null)
        {
            return System.getProperty(PROTOCOL_HANDLER_PACKAGES);
        }
        if (protocolHandlerPackages == null)
        {
            return DEFAULT_PROTOCOL_HANDLER_PACKAGES;
        }
        else
        {
            return protocolHandlerPackages;
        }
    }        

    /**
     * Set the security provider class.
     *
     * @param String name of security provider class.
     */
    public static void setSecurityProviderClass(String x)
    {
        securityProviderClass = x;
    }        

    /**
     * Get the security provider class.
     *
     * @return String name of security provider class.
     */
    public static String getSecurityProviderClass()
    {
        if (System.getProperty(SECURITY_PROVIDER_CLASS) != null)
        {
            return System.getProperty(SECURITY_PROVIDER_CLASS);
        }
        if (securityProviderClass == null)
        {
            return DEFAULT_SECURITY_PROVIDER_CLASS;
        }
        else
        {
            return securityProviderClass;
        }
    }

    /**
     * Set the key store password.
     *
     * @param String key store password.
     */
    public static void setKeyStorePassword(String x)
    {
        keyStorePassword = x;
    }

    /**
     * Set the security protocol.
     *
     * @param String security protocol.
     */
    public static void setSecurityProtocol(String x)
    {
        securityProtocol = x;
    }

    /**
     * Get the security protocol.
     *
     * @return String security protocol.
     */
    public static String getSecurityProtocol()
    {
        if (System.getProperty(SECURITY_PROTOCOL) != null)
        {
            return System.getProperty(SECURITY_PROTOCOL);
        }
        if (securityProtocol== null)
        {
            return DEFAULT_SECURITY_PROTOCOL;
        }
        else
        {
            return securityProtocol;
        }
    }

    /**
     * Set the key store location.
     *
     * @param String key store location.
     */
    public static void setKeyStore(String x)
    {
        keyStore = x;
    }        

    /**
     * Get the key store location.
     *
     * @return String key store location.
     */
    public static String getKeyStore()
    {
        if (System.getProperty(KEY_STORE) != null)
        {
            return System.getProperty(KEY_STORE);
        }
        if (keyStore == null)
        {
            return DEFAULT_KEY_STORE;
        }
        else
        {
            return keyStore;
        }
    }        

    /**
     * Set the key store format.
     *
     * @param String key store format.
     */
    public static void setKeyStoreType(String x)
    {
        keyStoreType = x;
    }        

    /**
     * Get the key store format.
     *
     * @return String key store format.
     */
    public static String getKeyStoreType()
    {
        if (System.getProperty(KEY_STORE_TYPE) != null)
        {
            return System.getProperty(KEY_STORE_TYPE);
        }
        if (keyStoreType == null)
        {
            /*
             * If the keystore type hasn't been specified
             * then let the system determine the default
             * type.
             */
            return KeyStore.getDefaultType();
        }
        else
        {
            return keyStoreType;
        }
    }        

    /**
     * Get the key store password.
     *
     * @return String key store password.
     */
    public static String getKeyStorePassword()
    {
        if (System.getProperty(KEY_STORE_PASSWORD) != null)
        {
            return System.getProperty(KEY_STORE_PASSWORD);
        }
        if (keyStorePassword == null)
        {
            return DEFAULT_KEY_STORE_PASSWORD;
        }
        else
        {
            return keyStorePassword;
        }
    }

    /**
     * Set the key store location.
     *
     * @param String key store location.
     */
    public static void setTrustStore(String x)
    {
        trustStore = x;
    }        

    /**
     * Get the key store location.
     *
     * @return String key store location.
     */
    public static String getTrustStore()
    {
        if (System.getProperty(TRUST_STORE) != null)
        {
            return System.getProperty(TRUST_STORE);
        }
        if (trustStore == null)
        {
            return DEFAULT_TRUST_STORE;
        }
        else
        {
            return trustStore;
        }
    }        

    /**
     * Set the key store format.
     *
     * @param String key store format.
     */
    public static void setTrustStoreType(String x)
    {
        trustStoreType = x;
    }        

    /**
     * Get the key store format.
     *
     * @return String key store format.
     */
    public static String getTrustStoreType()
    {
        if (System.getProperty(TRUST_STORE_TYPE) != null)
        {
            return System.getProperty(TRUST_STORE_TYPE);
        }
        if (trustStoreType == null)
        {
            /*
             * If the keystore type hasn't been specified
             * then let the system determine the default
             * type.
             */
            return KeyStore.getDefaultType();
        }
        else
        {
            return trustStoreType;
        }
    }        

    /**
     * Set the trust store password.
     *
     * @param String  trust store password.
     */
    public static void setTrustStorePassword(String x)
    {
        trustStorePassword = x;
    }        

    /**
     * Get the trust store password.
     *
     * @return String trust store password.
     */
    public static String getTrustStorePassword()
    {
        if (System.getProperty(TRUST_STORE_PASSWORD) != null)
        {
            return System.getProperty(TRUST_STORE_PASSWORD);
        }
        if (trustStorePassword == null)
        {
            return DEFAULT_TRUST_STORE_PASSWORD;
        }
        else
        {
            return trustStorePassword;
        }
    }

    /**
     * Set the key manager type.
     *
     * @param String key manager type.
     */
    public static void setKeyManagerType(String x)
    {
        keyManagerType = x;
    }        

    /**
     * Get the key manager type.
     *
     * @return String key manager type.
     */
    public static String getKeyManagerType()
    {
        if (System.getProperty(KEY_MANAGER_TYPE) != null)
        {
            return System.getProperty(KEY_MANAGER_TYPE);
        }
        if (keyManagerType == null)
        {
            return DEFAULT_KEY_MANAGER_TYPE;
        }
        else
        {
            return keyManagerType;
        }
    }        
}
