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

import com.sun.net.ssl.KeyManager;
import com.sun.net.ssl.KeyManagerFactory;
import com.sun.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import org.apache.xmlrpc.AuthDemo;
import org.apache.xmlrpc.Echo;
import org.apache.xmlrpc.WebServer;
import org.apache.xmlrpc.XmlRpc;
import org.apache.xmlrpc.XmlRpcServer;

/**
 * A minimal web server that exclusively handles XML-RPC requests
 * over a secure channel.
 *
 * Standard security properties must be set before the SecureWebserver
 * can be used. The SecurityTool takes care of retrieving these
 * values, but the parent application must set the necessary
 * values before anything will work.
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @version $Id: SecureWebServer.java,v 1.5 2002/08/26 17:41:57 dlr Exp $
 */
public class SecureWebServer 
    extends WebServer
    implements SecurityConstants
{
    /**
     * Creates a secure web server configured to run on the specified
     * port number.
     *
     * @param int port number of secure web server.
     * @see #SecureWebServer(int, InetAddress)
     */
    public SecureWebServer(int port)
    {
        this(port, null);
    }

    /**
     * Creates a secure web server configured to run on the specified
     * port number and IP address.
     *
     * @param int port number of the secure web server
     * @param addr The IP address to bind to.
     * @see org.apache.xmlrpc.WebServer#WebServer(int, InetAddress)
     */
    public SecureWebServer(int port, InetAddress addr)
    {
        super(port, addr);
    }


    /**
     * Creates a secure web server at the specified port number and IP
     * address.
     */
    public SecureWebServer(int port, InetAddress addr, XmlRpcServer xmlrpc)
    {
        super(port, addr, xmlrpc);
    }

    /**
     * @see org.apache.xmlrpc.WebServer#createServerSocket(int port, int backlog, InetAddress add)
     */
    protected ServerSocket createServerSocket(int port, int backlog, InetAddress add)
        throws Exception
    {
        SecurityTool.setup();
    
        SSLContext context = SSLContext.getInstance(SecurityTool.getSecurityProtocol());
          
        KeyManagerFactory keyManagerFactory = 
            KeyManagerFactory.getInstance(SecurityTool.getKeyManagerType());
            
        KeyStore keyStore = KeyStore.getInstance(SecurityTool.getKeyStoreType());
            
        keyStore.load(new FileInputStream(SecurityTool.getKeyStore()), 
            SecurityTool.getKeyStorePassword().toCharArray());
            
        keyManagerFactory.init(keyStore, SecurityTool.getKeyStorePassword().toCharArray());
            
        context.init(keyManagerFactory.getKeyManagers(), null, null);
        SSLServerSocketFactory sslSrvFact = context.getServerSocketFactory();
        return (SSLServerSocket) sslSrvFact.createServerSocket(port);
    }

    /**
     * This <em>can</em> be called from command line, but you'll have to 
     * edit and recompile to change the server port or handler objects. 
     *
     * @see org.apache.xmlrpc.WebServer#addDefaultHandlers()
     */
    public static void main(String[] argv)
    {
        int p = determinePort(argv, 10000);
        XmlRpc.setKeepAlive (true);
        SecureWebServer webserver = new SecureWebServer (p);

        try
        {
            webserver.addDefaultHandlers();
            webserver.start();
        }
        catch (Exception e)
        {
            System.err.println("Error running secure web server");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
