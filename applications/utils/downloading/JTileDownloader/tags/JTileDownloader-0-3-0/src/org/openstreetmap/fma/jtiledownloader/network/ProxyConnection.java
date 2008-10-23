package org.openstreetmap.fma.jtiledownloader.network;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * Copyright 2008, Friedrich Maier 
 * 
 * This file is part of JTileDownloader. 
 * (see http://wiki.openstreetmap.org/index.php/JTileDownloader)
 *
 *    JTileDownloader is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    JTileDownloader is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy (see file COPYING.txt) of the GNU 
 *    General Public License along with JTileDownloader.  
 *    If not, see <http://www.gnu.org/licenses/>.
 */
public class ProxyConnection
{
    public ProxyConnection(String host, int port)
    {
        setProxyData(host, port);
    }

    public ProxyConnection(String host, int port, String username, String passwort)
    {
        setProxyData(host, port);
        System.out.println("Authenticator.setDefault...");
        Authenticator.setDefault(new ProxyAuth(username, passwort));
    }

    private void setProxyData(String host, int port)
    {
        System.getProperties().put("http.proxySet", "true");
        System.out.println("http.proxyHost = " + host);
        System.getProperties().put("http.proxyHost", host);
        System.out.println("http.proxyPort = " + port);
        System.getProperties().put("http.proxyPort", String.valueOf(port));
    }

    private class ProxyAuth
        extends Authenticator
    {
        private String _username;
        private String _passwort;

        public ProxyAuth(String username, String passwort)
        {
            _username = username;
            _passwort = passwort;
        }

        protected PasswordAuthentication getPasswordAuthentication()
        {
            System.out.println("user " + _username + ", pw " + _passwort);
            return (new PasswordAuthentication(_username, _passwort.toCharArray()));
        }
    }

}