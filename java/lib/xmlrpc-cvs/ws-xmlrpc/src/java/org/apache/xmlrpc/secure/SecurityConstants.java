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

public interface SecurityConstants
{
    /**
     * Default security provider class. If you are using
     * something like Cryptix then you would override
     * default with setSecurityProviderClass().
     */
    public final static String DEFAULT_SECURITY_PROVIDER_CLASS = 
        "com.sun.net.ssl.internal.ssl.Provider";

    public final static String SECURITY_PROVIDER_CLASS =
        "security.provider";

    /**
     * Default security protocol. You probably don't need to
     * override this default.
     */
    public final static String DEFAULT_SECURITY_PROTOCOL = "TLS";
    
    public final static String SECURITY_PROTOCOL = "security.protocol";

    /**
     * Default key store location. This is just for testing, you
     * will want to override this default in a production environment.
     */
    public final static String DEFAULT_KEY_STORE = "testkeys";
    
    public final static String KEY_STORE = "javax.net.ssl.keyStore";

    /**
     * Default key store format. You probably don't need to
     * override this default.
     */
    public final static String DEFAULT_KEY_STORE_TYPE = "JKS";

    public final static String KEY_STORE_TYPE = "javax.net.ssl.keyStoreType";

    /**
     * Default key store password. This default is only
     * used for testing because the sample key store provided
     * with the Sun JSSE uses this password. Do <strong>not</strong>
     * use this password in a production server.
     */
    public final static String DEFAULT_KEY_STORE_PASSWORD = "password";
    
    public final static String KEY_STORE_PASSWORD = "javax.net.ssl.keyStorePassword";

    /**
     * Default key store format. You probably don't need to
     * override this default.
     */
    public final static String DEFAULT_TRUST_STORE_TYPE = "JKS";

    public final static String TRUST_STORE_TYPE =
        "javax.net.ssl.trustStoreType";

    /**
     * Default key store location. This is just for testing, you
     * will want to override this default in a production environment.
     */
    public final static String DEFAULT_TRUST_STORE = "truststore";
    
    public final static String TRUST_STORE = "javax.net.ssl.trustStore";

    /**
     * Default key store password. This default is only
     * used for testing because the sample key store provided
     * with the Sun JSSE uses this password. Do <strong>not</strong>
     * use this password in a production server.
     */
    public final static String DEFAULT_TRUST_STORE_PASSWORD = "password";
    
    public final static String TRUST_STORE_PASSWORD =
        "javax.net.ssl.trustStorePassword";

    /**
     * Default key manager type. You probably don't need to
     * override this default.
     */
    public final static String DEFAULT_KEY_MANAGER_TYPE = "SunX509";

    public final static String KEY_MANAGER_TYPE = 
        "sun.ssl.keymanager.type";

    public final static String TRUST_MANAGER_TYPE =
        "sun.ssl.trustmanager.type";

    /**
     * Default protocol handler packages. Change this if you
     * are using something other than the Sun JSSE.
     */
    public final static String DEFAULT_PROTOCOL_HANDLER_PACKAGES = 
        "com.sun.net.ssl.internal.www.protocol";

    public final static String PROTOCOL_HANDLER_PACKAGES =
        "java.protocol.handler.pkgs";
}
