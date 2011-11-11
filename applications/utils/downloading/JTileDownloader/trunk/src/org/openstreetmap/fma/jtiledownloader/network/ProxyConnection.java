/*
 * Copyright 2008, Friedrich Maier
 * 
 * This file is part of JTileDownloader.
 * (see http://wiki.openstreetmap.org/index.php/JTileDownloader)
 *
 * JTileDownloader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JTileDownloader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy (see file COPYING.txt) of the GNU 
 * General Public License along with JTileDownloader.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.openstreetmap.fma.jtiledownloader.network;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.logging.Logger;

public class ProxyConnection
{
    private static final Logger log = Logger.getLogger(ProxyConnection.class.getName());
    
    private ProxyConnection(String host, int port)
    {
        setProxyData(host, port);
    }

    private ProxyConnection(String host, int port, String username, String passwort)
    {
        setProxyData(host, port);
        log.fine("Authenticator.setDefault...");
        Authenticator.setDefault(new ProxyAuth(username, passwort));
    }

    public static void setProxyData(String host, int port)
    {
        System.getProperties().put("http.proxySet", "true");
        log.config("http.proxyHost = " + host);
        System.getProperties().put("http.proxyHost", host);
        log.config("http.proxyPort = " + port);
        System.getProperties().put("http.proxyPort", String.valueOf(port));
    }
    
    public static void setProxyData(String host, int port, String username, String password ) {
        setProxyData(host, port);
        log.fine("Authenticator.setDefault...");
        Authenticator.setDefault(new ProxyAuth(username, password));
    }

    static private class ProxyAuth
        extends Authenticator
    {
        private String _username;
        private String _password;

        public ProxyAuth(String username, String passwort)
        {
            _username = username;
            _password = passwort;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication()
        {
//            log.config("user " + _username + ", pw " + _password);
            return (new PasswordAuthentication(_username, _password.toCharArray()));
        }
    }

}
