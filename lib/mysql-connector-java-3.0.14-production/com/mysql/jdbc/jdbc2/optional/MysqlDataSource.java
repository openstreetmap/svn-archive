/*
   Copyright (C) 2002 MySQL AB

      This program is free software; you can redistribute it and/or modify
      it under the terms of the GNU General Public License as published by
      the Free Software Foundation; either version 2 of the License, or
      (at your option) any later version.

      This program is distributed in the hope that it will be useful,
      but WITHOUT ANY WARRANTY; without even the implied warranty of
      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
      GNU General Public License for more details.

      You should have received a copy of the GNU General Public License
      along with this program; if not, write to the Free Software
      Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 */
package com.mysql.jdbc.jdbc2.optional;

import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;
import javax.sql.DataSource;


/**
 * A JNDI DataSource for a Mysql JDBC connection
 *
 * @author Mark Matthews
 */
public class MysqlDataSource implements DataSource, Referenceable, Serializable {
    /** The driver to create connections with */
    protected static com.mysql.jdbc.Driver mysqlDriver = null;

    static {
        try {
            mysqlDriver = (com.mysql.jdbc.Driver) Class.forName(
                    "com.mysql.jdbc.Driver").newInstance();
        } catch (Exception E) {
            throw new RuntimeException(
                "Can not load Driver class com.mysql.jdbc.Driver");
        }
    }

    /** Log stream */
    protected transient PrintWriter logWriter = null;

    /** Database Name */
    protected String databaseName = null;

    /** Character Encoding */
    protected String encoding = null;

    /** Hostname */
    protected String hostName = null;

    /** Password */
    protected String password = null;

    /** The profileSql property */
    protected String profileSql = "false";

    /** The JDBC URL */
    protected String url = null;

    /** User name */
    protected String user = null;

    /** Should we construct the URL, or has it been set explicitly */
    protected boolean explicitUrl = false;

    /** Port number */
    protected int port = 3306;

    /**
     * Default no-arg constructor for Serialization
     */
    public MysqlDataSource() {
    }

    /**
     * Creates a new connection using the already configured username and
     * password.
     *
     * @return a connection to the database
     *
     * @throws SQLException if an error occurs
     */
    public java.sql.Connection getConnection() throws SQLException {
        return getConnection(user, password);
    }

    /**
     * Creates a new connection with the given username and password
     *
     * @param userID the user id to connect with
     * @param password the password to connect with
     *
     * @return a connection to the database
     *
     * @throws SQLException if an error occurs
     */
    public java.sql.Connection getConnection(String userID, String password)
        throws SQLException {
        Properties props = new Properties();

        if (userID == null) {
            userID = "";
        }

        if (password == null) {
            password = "";
        }

        props.put("user", userID);
        props.put("password", password);
        props.put("profileSql", getProfileSql());

        return getConnection(props);
    }

    /**
     * Sets the database name.
     *
     * @param dbName the name of the database
     */
    public void setDatabaseName(String dbName) {
        databaseName = dbName;
    }

    /**
     * Gets the name of the database
     *
     * @return the name of the database for this data source
     */
    public String getDatabaseName() {
        return (databaseName != null) ? databaseName : "";
    }

    /**
     * Sets the log writer for this data source.
     *
     * @see javax.sql.DataSource#setLogWriter(PrintWriter)
     */
    public void setLogWriter(PrintWriter output) throws SQLException {
        logWriter = output;
    }

    /**
     * Returns the log writer for this data source
     *
     * @return the log writer for this data source
     */
    public java.io.PrintWriter getLogWriter() {
        return logWriter;
    }

    /**
     * DOCUMENT ME!
     *
     * @param seconds DOCUMENT ME!
     *
     * @throws SQLException DOCUMENT ME!
     */
    public void setLoginTimeout(int seconds) throws SQLException {
    }

    /**
     * Returns the login timeout
     *
     * @return the login timeout
     */
    public int getLoginTimeout() {
        return 0;
    }

    /**
     * Sets the password
     *
     * @param pass the password
     */
    public void setPassword(String pass) {
        password = pass;
    }

    /**
     * Sets the database port.
     *
     * @param p the port
     */
    public void setPort(int p) {
        port = p;
    }

    /**
     * Returns the port number
     *
     * @return the port number
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port number
     *
     * @param p the port
     *
     * @see #setPort
     */
    public void setPortNumber(int p) {
        setPort(p);
    }

    /**
     * Returns the port number
     *
     * @return the port number
     */
    public int getPortNumber() {
        return getPort();
    }

    /**
     * Sets the profileSql property
     *
     * @param flag true/false
     */
    public void setProfileSql(String flag) {
        profileSql = flag;
    }

    /**
     * Returns the value for the profileSql property
     *
     * @return the value for the profileSql property
     */
    public String getProfileSql() {
        return profileSql;
    }

    /**
     * Required method to support this class as a <CODE>Referenceable</CODE>.
     *
     * @return a Reference to this data source
     *
     * @throws NamingException if a JNDI error occurs
     */
    public Reference getReference() throws NamingException {
        String factoryName = "com.mysql.jdbc.jdbc2.optional.MysqlDataSourceFactory";
        Reference ref = new Reference(getClass().getName(), factoryName, null);
        ref.add(new StringRefAddr("user", getUser()));
        ref.add(new StringRefAddr("password", password));
        ref.add(new StringRefAddr("serverName", getServerName()));
        ref.add(new StringRefAddr("port", "" + getPort()));
        ref.add(new StringRefAddr("databaseName", getDatabaseName()));
        ref.add(new StringRefAddr("profileSql", getProfileSql()));
        ref.add(new StringRefAddr("explicitUrl", String.valueOf(this.explicitUrl)));
        ref.add(new StringRefAddr("url", getUrl()));

        return ref;
    }

    /**
     * Sets the server name.
     *
     * @param serverName the server name
     */
    public void setServerName(String serverName) {
        hostName = serverName;
    }

    /**
     * Returns the name of the database server
     *
     * @return the name of the database server
     */
    public String getServerName() {
        return (hostName != null) ? hostName : "";
    }

    //
    // I've seen application servers use both formats
    // URL or url (doh)
    //

    /**
     * Sets the URL for this connection
     *
     * @param url the URL for this connection
     */
    public void setURL(String url) {
        setUrl(url);
    }

    /**
     * Returns the URL for this connection
     *
     * @return the URL for this connection
     */
    public String getURL() {
        return getUrl();
    }

    /**
     * This method is used by the app server to set the url string specified
     * within the datasource deployment descriptor.  It is discovered using
     * introspection and matches if property name in descriptor is "url".
     *
     * @param url url to be used within driver.connect
     */
    public void setUrl(String url) {
        this.url = url;
        explicitUrl = true;
    }

    /**
     * Returns  the   JDBC URL that will be used to create the database
     * connection.
     *
     * @return the URL for this connection
     */
    public String getUrl() {
        if (!explicitUrl) {
            String builtUrl = "jdbc:mysql://";
            builtUrl = builtUrl + getServerName() + ":" + getPort() + "/"
                + getDatabaseName();

            return builtUrl;
        } else {
            return this.url;
        }
    }

    /**
     * Sets the user ID.
     *
     * @param userID the User ID
     */
    public void setUser(String userID) {
        user = userID;
    }

    /**
     * Returns the  configured user for this connection
     *
     * @return the user for this connection
     */
    public String getUser() {
        return user;
    }

    /**
     * Creates a connection using the specified properties.
     *
     * @param props the properties to connect with
     *
     * @return a connection to the database
     *
     * @throws SQLException if an error occurs
     */
    protected java.sql.Connection getConnection(Properties props)
        throws SQLException {
        String jdbcUrlToUse = null;

        if (!explicitUrl) {
            StringBuffer jdbcUrl = new StringBuffer("jdbc:mysql://");

            if (hostName != null) {
                jdbcUrl.append(hostName);
            }

            jdbcUrl.append(":");
            jdbcUrl.append(port);
            jdbcUrl.append("/");

            if (databaseName != null) {
                jdbcUrl.append(databaseName);
            }

            jdbcUrlToUse = jdbcUrl.toString();
        } else {
            jdbcUrlToUse = this.url;
        }

        return mysqlDriver.connect(jdbcUrlToUse, props);
       
    }
}
