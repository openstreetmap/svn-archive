package org.openstreetmap.fma.jtiledownloader.network;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * developed by fma (http://wiki.openstreetmap.org/index.php/User:Fma)
 * license: http://creativecommons.org/licenses/by-nc-nd/3.0/
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