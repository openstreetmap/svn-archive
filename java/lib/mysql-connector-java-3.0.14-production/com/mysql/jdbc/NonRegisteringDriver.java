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
package com.mysql.jdbc;

import java.sql.DriverPropertyInfo;
import java.sql.SQLException;

import java.util.Properties;
import java.util.StringTokenizer;


/**
 * The Java SQL framework allows for multiple database drivers.  Each driver
 * should supply a class that implements the Driver interface
 * 
 * <p>
 * The DriverManager will try to load as many drivers as it can find and then
 * for any given connection request, it will ask each driver in turn to try to
 * connect to the target URL.
 * </p>
 * 
 * <p>
 * It is strongly recommended that each Driver class should be small and
 * standalone so that the Driver class can be loaded and queried without
 * bringing in vast quantities of supporting code.
 * </p>
 * 
 * <p>
 * When a Driver class is loaded, it should create an instance of itself and
 * register it with the DriverManager.  This means that a user can load and
 * register a driver by doing Class.forName("foo.bah.Driver")
 * </p>
 *
 * @author Mark Matthews
 * @version $Id: NonRegisteringDriver.java,v 1.1.2.16 2004/05/28 11:36:50 mmatthew Exp $
 *
 * @see org.gjt.mm.mysql.Connection
 * @see java.sql.Driver
 */
public class NonRegisteringDriver implements java.sql.Driver {
    /** Should the driver generate debugging output? */
    public static final boolean DEBUG = false;

    /** Should the driver generate method-call traces? */
    public static final boolean TRACE = false;

    /**
     * Construct a new driver and register it with DriverManager
     *
     * @throws java.sql.SQLException if a database error occurs.
     */
    public NonRegisteringDriver() throws java.sql.SQLException {
        // Required for Class.forName().newInstance()
    }

    /**
     * Gets the drivers major version number
     *
     * @return the drivers major version number
     */
    public int getMajorVersion() {
        return getMajorVersionInternal();
    }

    /**
     * Get the drivers minor version number
     *
     * @return the drivers minor version number
     */
    public int getMinorVersion() {
        return getMinorVersionInternal();
    }

    /**
     * The getPropertyInfo method is intended to allow a generic GUI tool to
     * discover what properties it should prompt a human for in order to get
     * enough information to connect to a database.
     * 
     * <p>
     * Note that depending on the values the human has supplied so far,
     * additional values may become necessary, so it may be necessary to
     * iterate through several calls to getPropertyInfo
     * </p>
     *
     * @param url the Url of the database to connect to
     * @param info a proposed list of tag/value pairs that will be sent on
     *        connect open.
     *
     * @return An array of DriverPropertyInfo objects describing possible
     *         properties.  This array may be an empty array if no properties
     *         are required
     *
     * @exception java.sql.SQLException if a database-access error occurs
     *
     * @see java.sql.Driver#getPropertyInfo
     */
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
        throws java.sql.SQLException {
        if (info == null) {
            info = new Properties();
        }

        if ((url != null) && url.startsWith("jdbc:mysql://")) {
            info = parseURL(url, info);
        }

        DriverPropertyInfo hostProp = new DriverPropertyInfo("HOST",
                info.getProperty("HOST"));
        hostProp.required = true;
        hostProp.description = "Hostname of MySQL Server";

        DriverPropertyInfo portProp = new DriverPropertyInfo("PORT",
                info.getProperty("PORT", "3306"));
        portProp.required = false;
        portProp.description = "Port number of MySQL Server";

        DriverPropertyInfo dbProp = new DriverPropertyInfo("DBNAME",
                info.getProperty("DBNAME"));
        dbProp.required = false;
        dbProp.description = "Database name";

        DriverPropertyInfo userProp = new DriverPropertyInfo("user",
                info.getProperty("user"));
        userProp.required = true;
        userProp.description = "Username to authenticate as";

        DriverPropertyInfo passwordProp = new DriverPropertyInfo("password",
                info.getProperty("password"));
        passwordProp.required = true;
        passwordProp.description = "Password to use for authentication";

        DriverPropertyInfo autoReconnect = new DriverPropertyInfo("autoReconnect",
                info.getProperty("autoReconnect", "false"));
        autoReconnect.required = false;
        autoReconnect.choices = new String[] { "true", "false" };
        autoReconnect.description = "Should the driver try to re-establish bad connections?";

        DriverPropertyInfo maxReconnects = new DriverPropertyInfo("maxReconnects",
                info.getProperty("maxReconnects", "3"));
        maxReconnects.required = false;
        maxReconnects.description = "Maximum number of reconnects to attempt if autoReconnect is true";
        ;

        DriverPropertyInfo initialTimeout = new DriverPropertyInfo("initialTimeout",
                info.getProperty("initialTimeout", "2"));
        initialTimeout.required = false;
        initialTimeout.description = "Initial timeout (seconds) to wait between failed connections";

        DriverPropertyInfo profileSql = new DriverPropertyInfo("profileSql",
                info.getProperty("profileSql", "false"));
        profileSql.required = false;
        profileSql.choices = new String[] { "true", "false" };
        profileSql.description = "Trace queries and their execution/fetch times on STDERR (true/false) defaults to false";
        ;

        DriverPropertyInfo socketTimeout = new DriverPropertyInfo("socketTimeout",
                info.getProperty("socketTimeout", "0"));
        socketTimeout.required = false;
        socketTimeout.description = "Timeout on network socket operations (0 means no timeout)";
        ;

        DriverPropertyInfo useSSL = new DriverPropertyInfo("useSSL",
                info.getProperty("useSSL", "false"));
        useSSL.required = false;
        useSSL.choices = new String[] { "true", "false" };
        useSSL.description = "Use SSL when communicating with the server?";
        ;

        DriverPropertyInfo useCompression = new DriverPropertyInfo("useCompression",
                info.getProperty("useCompression", "false"));
        useCompression.required = false;
        useCompression.choices = new String[] { "true", "false" };
        useCompression.description = "Use zlib compression when communicating with the server?";
        ;

        DriverPropertyInfo paranoid = new DriverPropertyInfo("paranoid",
                info.getProperty("paranoid", "false"));
        paranoid.required = false;
        paranoid.choices = new String[] { "true", "false" };
        paranoid.description = "Expose sensitive information in error messages and clear "
            + "data structures holding sensitiven data when possible?";
        ;

        DriverPropertyInfo useHostsInPrivileges = new DriverPropertyInfo("useHostsInPrivileges",
                info.getProperty("useHostsInPrivileges", "true"));
        useHostsInPrivileges.required = false;
        useHostsInPrivileges.choices = new String[] { "true", "false" };
        useHostsInPrivileges.description = "Add '@hostname' to users in DatabaseMetaData.getColumn/TablePrivileges()";
        ;

        DriverPropertyInfo interactiveClient = new DriverPropertyInfo("interactiveClient",
                info.getProperty("interactiveClient", "false"));
        interactiveClient.required = false;
        interactiveClient.choices = new String[] { "true", "false" };
        interactiveClient.description = "Set the CLIENT_INTERACTIVE flag, which tells MySQL "
            + "to timeout connections based on INTERACTIVE_TIMEOUT instead of WAIT_TIMEOUT";
        ;

        DriverPropertyInfo useTimezone = new DriverPropertyInfo("useTimezone",
                info.getProperty("useTimezone", "false"));
        useTimezone.required = false;
        useTimezone.choices = new String[] { "true", "false" };
        useTimezone.description = "Convert time/date types between client and server timezones";

        DriverPropertyInfo serverTimezone = new DriverPropertyInfo("serverTimezone",
                info.getProperty("serverTimezone", ""));
        serverTimezone.required = false;
        serverTimezone.description = "Override detection/mapping of timezone. Used when timezone from server doesn't map to Java timezone";

        DriverPropertyInfo connectTimeout = new DriverPropertyInfo("connectTimeout",
                info.getProperty("connectTimeout", "0"));
        connectTimeout.required = false;
        connectTimeout.description = "Timeout for socket connect (in milliseconds), with 0 being no timeout. Only works on JDK-1.4 or newer. Defaults to '0'.";

        DriverPropertyInfo queriesBeforeRetryMaster = new DriverPropertyInfo("queriesBeforeRetryMaster",
                info.getProperty("queriesBeforeRetryMaster", "50"));
        queriesBeforeRetryMaster.required = false;
        queriesBeforeRetryMaster.description = "Number of queries to issue before falling back to master when failed over "
            + "(when using multi-host failover). Whichever condition is met first, "
            + "'queriesBeforeRetryMaster' or 'secondsBeforeRetryMaster' will cause an "
            + "attempt to be made to reconnect to the master. Defaults to 50.";
        ;

        DriverPropertyInfo secondsBeforeRetryMaster = new DriverPropertyInfo("secondsBeforeRetryMaster",
                info.getProperty("secondsBeforeRetryMaster", "30"));
        secondsBeforeRetryMaster.required = false;
        secondsBeforeRetryMaster.description = "How long should the driver wait, when failed over, before attempting "
            + "to reconnect to the master server? Whichever condition is met first, "
            + "'queriesBeforeRetryMaster' or 'secondsBeforeRetryMaster' will cause an "
            + "attempt to be made to reconnect to the master. Time in seconds, defaults to 30";

        DriverPropertyInfo useStreamLengthsInPrepStmts = new DriverPropertyInfo("useStreamLengthsInPrepStmts",
                info.getProperty("useStreamLengthsInPrepStmts", "true"));
        useStreamLengthsInPrepStmts.required = false;
        useStreamLengthsInPrepStmts.choices = new String[] { "true", "false" };
        useStreamLengthsInPrepStmts.description = "Honor stream length parameter in "
            + "PreparedStatement/ResultSet.setXXXStream() method calls (defaults to 'true')";

        DriverPropertyInfo continueBatchOnError = new DriverPropertyInfo("continueBatchOnError",
                info.getProperty("continueBatchOnError", "true"));
        continueBatchOnError.required = false;
        continueBatchOnError.choices = new String[] { "true", "false" };
        continueBatchOnError.description = "Should the driver continue processing batch commands if "
            + "one statement fails. The JDBC spec allows either way (defaults to 'true').";

        DriverPropertyInfo allowLoadLocalInfile = new DriverPropertyInfo("allowLoadLocalInfile",
                info.getProperty("allowLoadLocalInfile", "true"));
        allowLoadLocalInfile.required = false;
        allowLoadLocalInfile.choices = new String[] { "true", "false" };
        allowLoadLocalInfile.description = "Should the driver allow use of 'LOAD DATA LOCAL INFILE...' (defaults to 'true').";

        DriverPropertyInfo strictUpdates = new DriverPropertyInfo("strictUpdates",
                info.getProperty("strictUpdates", "true"));
        strictUpdates.required = false;
        strictUpdates.choices = new String[] { "true", "false" };
        strictUpdates.description = "Should the driver do strict checking (all primary keys selected) of updatable result sets?...' (defaults to 'true').";

        DriverPropertyInfo ignoreNonTxTables = new DriverPropertyInfo("ignoreNonTxTables",
                info.getProperty("ignoreNonTxTables", "false"));
        ignoreNonTxTables.required = false;
        ignoreNonTxTables.choices = new String[] { "true", "false" };
        ignoreNonTxTables.description = "Ignore non-transactional table warning for rollback? (defaults to 'false').";

        DriverPropertyInfo clobberStreamingResults = new DriverPropertyInfo("clobberStreamingResults",
                info.getProperty("clobberStreamingResults", "false"));
        clobberStreamingResults.required = false;
        clobberStreamingResults.choices = new String[] { "true", "false" };
        clobberStreamingResults.description = "This will cause a 'streaming' ResultSet to be automatically closed, "
            + "and any oustanding data still streaming from the server to be discarded if another query is executed "
            + "before all the data has been read from the server.";

        DriverPropertyInfo reconnectAtTxEnd = new DriverPropertyInfo("reconnectAtTxEnd",
                info.getProperty("reconnectAtTxEnd", "false"));
        reconnectAtTxEnd.required = false;
        reconnectAtTxEnd.choices = new String[] { "true", "false" };
        reconnectAtTxEnd.description = "If autoReconnect is set to true, should the driver attempt reconnections"
            + "at the end of every transaction? (true/false, defaults to false)";

        DriverPropertyInfo alwaysClearStream = new DriverPropertyInfo("alwaysClearStream",
                info.getProperty("alwaysClearStream", "false"));
        alwaysClearStream.required = false;
        alwaysClearStream.choices = new String[] { "true", "false" };
        alwaysClearStream.description = "Should the driver clear any remaining data from the input stream before issuing"
            + " a query? Normally not needed (approx 1-2%	perf. penalty, true/false, defaults to false)";

        DriverPropertyInfo cachePrepStmts = new DriverPropertyInfo("cachePrepStmts",
                info.getProperty("cachePrepStmts", "false"));
        cachePrepStmts.required = false;
        cachePrepStmts.choices = new String[] { "true", "false" };
        cachePrepStmts.description = "Should the driver cache the parsing stage of PreparedStatements (true/false, default is 'false')";

        DriverPropertyInfo prepStmtCacheSize = new DriverPropertyInfo("prepStmtCacheSize",
                info.getProperty("prepStmtCacheSize", "25"));
        prepStmtCacheSize.required = false;
        prepStmtCacheSize.description = "If prepared statement caching is enabled, "
            + "how many prepared statements should be cached? (default is '25')";

        DriverPropertyInfo prepStmtCacheSqlLimit = new DriverPropertyInfo("prepStmtCacheSqlLimit",
                info.getProperty("prepStmtCacheSqlLimit", "256"));
        prepStmtCacheSqlLimit.required = false;
        prepStmtCacheSqlLimit.description = "If prepared statement caching is enabled, "
            + "what's the largest SQL the driver will cache the parsing for? (in chars, default is '256')";

        DriverPropertyInfo useUnbufferedInput = new DriverPropertyInfo("useUnbufferedInput",
                info.getProperty("useUnbufferedInput", "true"));
        useUnbufferedInput.required = false;
        useUnbufferedInput.description = "Don't use BufferedInputStream for reading data from the server true/false (default is 'true')";

        DriverPropertyInfo[] dpi = {
            hostProp, portProp, dbProp, userProp, passwordProp, autoReconnect,
            maxReconnects, initialTimeout, profileSql, socketTimeout, useSSL,
            paranoid, useHostsInPrivileges, interactiveClient, useCompression,
            useTimezone, serverTimezone, connectTimeout,
            secondsBeforeRetryMaster, queriesBeforeRetryMaster,
            useStreamLengthsInPrepStmts, continueBatchOnError,
            allowLoadLocalInfile, strictUpdates, ignoreNonTxTables,
            reconnectAtTxEnd, alwaysClearStream, cachePrepStmts,
            prepStmtCacheSize, prepStmtCacheSqlLimit, useUnbufferedInput
        };

        return dpi;
    }

    /**
     * Typically, drivers will return true if they understand the subprotocol
     * specified in the URL and false if they don't.  This driver's protocols
     * start with jdbc:mysql:
     *
     * @param url the URL of the driver
     *
     * @return true if this driver accepts the given URL
     *
     * @exception java.sql.SQLException if a database-access error occurs
     *
     * @see java.sql.Driver#acceptsURL
     */
    public boolean acceptsURL(String url) throws java.sql.SQLException {
        return (parseURL(url, null) != null);
    }

    /**
     * Try to make a database connection to the given URL.  The driver should
     * return "null" if it realizes it is the wrong kind of driver to connect
     * to the given URL.  This will be common, as when the JDBC driverManager
     * is asked to connect to a given URL, it passes the URL to each loaded
     * driver in turn.
     * 
     * <p>
     * The driver should raise an java.sql.SQLException if it is the right
     * driver to connect to the given URL, but has trouble connecting to the
     * database.
     * </p>
     * 
     * <p>
     * The java.util.Properties argument can be used to pass arbitrary string
     * tag/value pairs as connection arguments.
     * </p>
     * 
     * <p>
     * My protocol takes the form:
     * <PRE>
     *    jdbc:mysql://host:port/database
     * </PRE>
     * </p>
     *
     * @param url the URL of the database to connect to
     * @param info a list of arbitrary tag/value pairs as connection arguments
     *
     * @return a connection to the URL or null if it isnt us
     *
     * @exception java.sql.SQLException if a database access error occurs
     * @throws SQLException DOCUMENT ME!
     *
     * @see java.sql.Driver#connect
     */
    public java.sql.Connection connect(String url, Properties info)
        throws java.sql.SQLException {
        Properties props = null;

        if ((props = parseURL(url, info)) == null) {
            return null;
        } else {
            try {
                Connection newConn = new com.mysql.jdbc.Connection(host(props),
                        port(props), props, database(props), url, this);

                return (java.sql.Connection) newConn;
            } catch (SQLException sqlEx) {
                // Don't wrap SQLExceptions, throw 
                // them un-changed.
                throw sqlEx;
            } catch (Exception ex) {
                throw new SQLException(
                    "Cannot load connection class because of underlying exception: '"
                    + ex.toString() + "'.",
                    SQLError.SQL_STATE_UNABLE_TO_CONNECT_TO_DATASOURCE);
            }
        }
    }

    //
    // return the database name property
    //

    /**
     * Returns the database property from <code>props</code>
     *
     * @param props the Properties to look for the database property.
     *
     * @return the database name.
     */
    public String database(Properties props) {
        return props.getProperty("DBNAME");
    }

    /**
     * Returns the hostname property
     *
     * @param props the java.util.Properties instance to retrieve the hostname
     *        from.
     *
     * @return the hostname
     */
    public String host(Properties props) {
        return props.getProperty("HOST", "localhost");
    }

    /**
     * Report whether the driver is a genuine JDBC compliant driver.  A driver
     * may only report "true" here if it passes the JDBC compliance tests,
     * otherwise it is required to return false.  JDBC compliance requires
     * full support for the JDBC API and full support for SQL 92 Entry Level.
     * 
     * <p>
     * MySQL is not SQL92 compliant
     * </p>
     *
     * @return is this driver JDBC compliant?
     */
    public boolean jdbcCompliant() {
        return false;
    }

    /**
     * Returns the port number property
     *
     * @param props the properties to get the port number from
     *
     * @return the port number
     */
    public int port(Properties props) {
        return Integer.parseInt(props.getProperty("PORT", "3306"));
    }

    //
    // return the value of any property this driver knows about
    //

    /**
     * Returns the given property from <code>props</code>
     *
     * @param name the property name
     * @param props the property instance to look in
     *
     * @return the property value, or null if not found.
     */
    public String property(String name, Properties props) {
        return props.getProperty(name);
    }

    /**
     * Gets the drivers major version number
     *
     * @return the drivers major version number
     */
    static int getMajorVersionInternal() {
        return safeIntParse("3");
    }

    /**
     * Get the drivers minor version number
     *
     * @return the drivers minor version number
     */
    static int getMinorVersionInternal() {
        return safeIntParse("0");
    }

    /**
     * Constructs a new DriverURL, splitting the specified URL into its
     * component parts
     *
     * @param url JDBC URL to parse
     * @param defaults Default properties
     *
     * @return Properties with elements added from the url
     *
     * @exception java.sql.SQLException
     */
    public Properties parseURL(String url, Properties defaults)
        throws java.sql.SQLException {
        Properties urlProps = (defaults != null) ? defaults
                                                 : new Properties(defaults);

        if (url == null) {
            return null;
        } else {
            /*
             * Parse parameters after the ? in the URL and remove
             * them from the original URL.
             */
            int index = url.indexOf("?");

            if (index != -1) {
                String paramString = url.substring(index + 1, url.length());
                url = url.substring(0, index);

                StringTokenizer queryParams = new StringTokenizer(paramString,
                        "&");

                while (queryParams.hasMoreTokens()) {
                    StringTokenizer vp = new StringTokenizer(queryParams
                            .nextToken(), "=");
                    String param = "";

                    if (vp.hasMoreTokens()) {
                        param = vp.nextToken();
                    }

                    String value = "";

                    if (vp.hasMoreTokens()) {
                        value = vp.nextToken();
                    }

                    if ((value.length() > 0) && (param.length() > 0)) {
                        urlProps.put(param, value);
                    }
                }
            }
        }

        if (!StringUtils.startsWithIgnoreCase(url, "jdbc:mysql://")) {
            return null;
        }

        url = url.substring(13);

        String hostStuff = null;

        int slashIndex = url.indexOf("/");

        if (slashIndex != -1) {
            hostStuff = url.substring(0, slashIndex);

            if ((slashIndex + 1) < url.length()) {
                urlProps.put("DBNAME",
                    url.substring((slashIndex + 1), url.length()));
            }
        } else {
            return null;
        }

        if ((hostStuff != null) && (hostStuff.length() > 0)) {
            if (hostStuff.indexOf(",") == -1) {
            	String[] hostPortPair = parseHostPortPair(hostStuff);
            	
            	if (hostPortPair[HOST_NAME_INDEX] != null) {
            		urlProps.put("HOST", hostPortPair[HOST_NAME_INDEX]);
            	}
            	
            	if (hostPortPair[PORT_NUMBER_INDEX] != null) {
            		urlProps.put("PORT", hostPortPair[PORT_NUMBER_INDEX]);
            	}
            } else {
                urlProps.put("HOST", hostStuff);
            }
        }

        return urlProps;
    }

    private static int safeIntParse(String intAsString) {
        try {
            return Integer.parseInt(intAsString);
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }
    
    /**
     * Parses hostPortPair in the form of [host][:port] into an array, with 
     * the element of index HOST_NAME_INDEX being the host (or null if not specified),
     * and the element of index PORT_NUMBER_INDEX being the port (or null if not specified).
     * 
     * @param hostPortPair host and port in form of of [host][:port]
     * @return array containing host and port as Strings
     * @throws SQLException if a parse error occurs
     */
    protected static String[] parseHostPortPair(String hostPortPair) throws SQLException {
    	int portIndex = hostPortPair.indexOf(":");

    	String[] splitValues = new String[2];
    	
    	String hostname = null;
    	
        if (portIndex != -1) {
            if ((portIndex + 1) < hostPortPair.length()) {
                String portAsString = hostPortPair.substring(portIndex
                        + 1);
                hostname = hostPortPair.substring(0, portIndex);

                splitValues[HOST_NAME_INDEX] = hostname;
                
                splitValues[PORT_NUMBER_INDEX] = portAsString;

            } else {
                throw new SQLException("Must specify port after ':' in connection string",
                    SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE);
            }
        } else {
        	splitValues[HOST_NAME_INDEX] = hostPortPair;
        	splitValues[PORT_NUMBER_INDEX] = null;
        }
        
        return splitValues;
    }
    
    /**
     * Index for hostname coming out of parseHostPortPair().
     */
    protected final static int HOST_NAME_INDEX = 0;
    
    /**
     * Index for port # coming out of parseHostPortPair().
     */
    protected final static int PORT_NUMBER_INDEX = 1;
}
