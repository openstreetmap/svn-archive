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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ParameterMetaData;
import java.sql.Ref;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.TreeMap;


/**
 * A Connection represents a session with a specific database.  Within the
 * context of a Connection, SQL statements are executed and results are
 * returned.
 * 
 * <P>
 * A Connection's database is able to provide information describing its
 * tables, its supported SQL grammar, its stored procedures, the capabilities
 * of this connection, etc.  This information is obtained with the getMetaData
 * method.
 * </p>
 *
 * @author Mark Matthews
 * @version $Id: Connection.java,v 1.31.2.73 2004/05/28 11:36:50 mmatthew Exp $
 *
 * @see java.sql.Connection
 */
public class Connection implements java.sql.Connection {
    // The command used to "ping" the database.
    // Newer versions of MySQL server have a ping() command,
    // but this works for everything
    private static final String PING_COMMAND = "SELECT 1";

    /**
     * Map mysql transaction isolation level name to
     * java.sql.Connection.TRANSACTION_XXX
     */
    private static Map mapTransIsolationName2Value = null;

    /**
     * The mapping between MySQL charset names and Java charset names.
     * Initialized by loadCharacterSetMapping()
     */
    private static Map charsetMap;

    /** Table of multi-byte charsets. Initialized by loadCharacterSetMapping() */
    private static Map multibyteCharsetsMap;

    /** Default socket factory classname */
    private static final String DEFAULT_SOCKET_FACTORY = StandardSocketFactory.class
        .getName();

    static {
        loadCharacterSetMapping();
        mapTransIsolationName2Value = new HashMap(8);
        mapTransIsolationName2Value.put("READ-UNCOMMITED",
            new Integer(TRANSACTION_READ_UNCOMMITTED));
        mapTransIsolationName2Value.put("READ-UNCOMMITTED",
            new Integer(TRANSACTION_READ_UNCOMMITTED));
        mapTransIsolationName2Value.put("READ-COMMITTED",
            new Integer(TRANSACTION_READ_COMMITTED));
        mapTransIsolationName2Value.put("REPEATABLE-READ",
            new Integer(TRANSACTION_REPEATABLE_READ));
        mapTransIsolationName2Value.put("SERIALIZABLE",
            new Integer(TRANSACTION_SERIALIZABLE));
    }

    /**
     * Marker for character set converter not being available (not written,
     * multibyte, etc)  Used to prevent multiple instantiation requests.
     */
    private final static Object CHARSET_CONVERTER_NOT_AVAILABLE_MARKER = new Object();
    boolean parserKnowsUnicode = false;

    /** Internal DBMD to use for various database-version specific features */
    private DatabaseMetaData dbmd = null;

    /** The list of host(s) to try and connect to */
    private List hostList = null;

    /** A map of SQL to parsed prepared statement parameters. */
    private Map cachedPreparedStatementParams;

    /**
     * Holds cached mappings to charset converters to avoid static
     * synchronization and at the same time save memory (each charset
     * converter takes approx 65K of static data).
     */
    private Map charsetConverterMap = new HashMap(CharsetMapping.JAVA_TO_MYSQL_CHARSET_MAP
            .size());

    /** A map of statements that have had setMaxRows() called on them */
    private Map statementsUsingMaxRows;

    /**
     * The type map for UDTs (not implemented, but used by some third-party
     * vendors, most notably IBM WebSphere)
     */
    private Map typeMap;

    /** The I/O abstraction interface (network conn to MySQL server */
    private MysqlIO io = null;

    /** Mutex */
    private final Object mutex = new Object();

    /** The map of server variables that we retrieve at connection init. */
    private Map serverVariables = null;

    /** The driver instance that created us */
    private NonRegisteringDriver myDriver;

    /** Properties for this connection specified by user */
    private Properties props = null;

    /** The database we're currently using (called Catalog in JDBC terms). */
    private String database = null;

    /** If we're doing unicode character conversions, what encoding do we use? */
    private String encoding = null;

    /** The hostname we're connected to */
    private String host = null;

    /** The JDBC URL we're using */
    private String myURL = null;

    /** What does MySQL call this encoding? */
    private String mysqlEncodingName = null;
    private String negativeInfinityRep = MysqlDefs.MIN_DOUBLE_VAL_STRING;
    private String notANumberRep = MysqlDefs.NAN_VAL_STRING;

    /** The password we used */
    private String password = null;
    private String positiveInfinityRep = MysqlDefs.MAX_DOUBLE_VAL_STRING;

    /** Classname for socket factory */
    private String socketFactoryClassName = null;

    /** The user we're connected as */
    private String user = null;

    /** Where was the connection _explicitly_ closed by the application? */
    private Throwable explicitCloseLocation;

    /** If the connection was forced closed, why was it  forced closed? */
    private Throwable forcedCloseReason;
    private TimeZone defaultTimeZone;

    /** The timezone of the server */
    private TimeZone serverTimezone = null;

    /**
     * We need this 'bootstrapped', because 4.1 and newer will send fields back
     * with this even before we fill this dynamically from the server.
     */
    private String[] indexToCharsetMapping = CharsetMapping.INDEX_TO_CHARSET;

    /** Allow LOAD LOCAL INFILE (defaults to true) */
    private boolean allowLoadLocalInfile = true;

    /** Should we clear the input stream each query? */
    private boolean alwaysClearStream = false;

    /** Are we in autoCommit mode? */
    private boolean autoCommit = true;

    /** SHould we cache the parsing of prepared statements? */
    private boolean cachePreparedStatements = false;

    /** Should we capitalize mysql types */
    private boolean capitalizeDBMDTypes = false;

    /** Should we clobber streaming results on new queries, or issue an error? */
    private boolean clobberStreamingResults = false;

    /**
     * Should we continue processing batch commands if one fails. The JDBC spec
     * allows either way, so we let the user choose
     */
    private boolean continueBatchOnError = true;

    /** Should we do unicode character conversions? */
    private boolean doUnicode = false;

    /** When failed-over, set connection to read-only? */
    private boolean failOverReadOnly = true;

    /** Are we failed-over to a non-master host */
    private boolean failedOver = false;

    /** Does the server suuport isolation levels? */
    private boolean hasIsolationLevels = false;

    /** Does this version of MySQL support quoted identifiers? */
    private boolean hasQuotedIdentifiers = false;

    //
    // This is for the high availability :) routines
    //
    private boolean highAvailability = false;

    /** Ignore non-transactional table warning for rollback? */
    private boolean ignoreNonTxTables = false;

    /** Has this connection been closed? */
    private boolean isClosed = true;

    /** Should we tell MySQL that we're an interactive client? */
    private boolean isInteractiveClient = false;

    /** Is the server configured to use lower-case table names only? */
    private boolean lowerCaseTableNames = false;

    /** Has the max-rows setting been changed from the default? */
    private boolean maxRowsChanged = false;
    private boolean needsPing = false;
    private boolean negativeInfinityRepIsClipped = true;
    private boolean notANumberRepIsClipped = true;

    /** Do we expose sensitive information in exception and error messages? */
    private boolean paranoid = false;

    /** Should we do 'extra' sanity checks? */
    private boolean pedantic = false;
    private boolean positiveInfinityRepIsClipped = true;

    /** Should we retrieve 'info' messages from the server? */
    private boolean readInfoMsg = false;

    /** Are we in read-only mode? */
    private boolean readOnly = false;

    /**
     * If autoReconnect == true, should we attempt to reconnect at transaction
     * boundaries?
     */
    private boolean reconnectAtTxEnd = false;

    /** Do we relax the autoCommit semantics? (For enhydra, for example) */
    private boolean relaxAutoCommit = false;

    /** Do we need to correct endpoint rounding errors */
    private boolean strictFloatingPoint = false;

    /** Do we check all keys for updatable result sets? */
    private boolean strictUpdates = true;

    /** Are transactions supported by the MySQL server we are connected to? */
    private boolean transactionsSupported = false;

    /** Has ANSI_QUOTES been enabled on the server? */
    private boolean useAnsiQuotes = false;

    /** Should we use compression? */
    private boolean useCompression = false;

    /** Can we use the "ping" command rather than a query? */
    private boolean useFastPing = false;

    /** Should we tack on hostname in DBMD.getTable/ColumnPrivileges()? */
    private boolean useHostsInPrivileges = true;

    /** Should we use SSL? */
    private boolean useSSL = false;

    /**
     * Should we use stream lengths in prepared statements? (true by default ==
     * JDBC compliant)
     */
    private boolean useStreamLengthsInPrepStmts = true;

    /** Should we use timezone information? */
    private boolean useTimezone = false;

    /** Should we return PreparedStatements for UltraDev's stupid bug? */
    private boolean useUltraDevWorkAround = false;
    private boolean useUnbufferedInput = true;
    private double initialTimeout = 2.0D;

    /** How many hosts are in the host list? */
    private int hostListSize = 0;

    /** isolation level */
    private int isolationLevel = java.sql.Connection.TRANSACTION_READ_COMMITTED;

    /**
     * The largest packet we can send (changed once we know what the server
     * supports, we get this at connection init).
     */
    private int maxAllowedPacket = 65536;
    private int maxReconnects = 3;

    /**
     * The max rows that a result set can contain. Defaults to -1, which
     * according to the JDBC spec means "all".
     */
    private int maxRows = -1;
    private int netBufferLength = 16384;

    /** The port number we're connected to (defaults to 3306) */
    private int port = 3306;

    /**
     * If prepared statement caching is enabled, what should the threshold
     * length of the SQL to prepare should be in order to _not_ cache?
     */
    private int preparedStatementCacheMaxSqlSize = 256;

    /** If prepared statement caching is enabled, how many should we cache? */
    private int preparedStatementCacheSize = 25;

    /**
     * How many queries should we wait before we try to re-connect to the
     * master, when we are failing over to replicated hosts Defaults to 50
     */
    private int queriesBeforeRetryMaster = 50;

    /** What should we set the socket timeout to? */
    private int socketTimeout = 0; // infinite

    /** When did the last query finish? */
    private long lastQueryFinishedTime = 0;

    /** When did the master fail? */
    private long masterFailTimeMillis = 0L;

    /** Number of queries we've issued since the master failed */
    private long queriesIssuedFailedOver = 0;

    /**
     * How many seconds should we wait before retrying to connect to the master
     * if failed over? We fall back when either queriesBeforeRetryMaster or
     * secondsBeforeRetryMaster is reached.
     */
    private long secondsBeforeRetryMaster = 30L;

    /**
     * Creates a connection to a MySQL Server.
     *
     * @param host the hostname of the database server
     * @param port the port number the server is listening on
     * @param info a Properties[] list holding the user and password
     * @param database the database to connect to
     * @param url the URL of the connection
     * @param d the Driver instantation of the connection
     *
     * @exception java.sql.SQLException if a database access error occurs
     * @throws SQLException DOCUMENT ME!
     */
    Connection(String host, int port, Properties info, String database,
        String url, NonRegisteringDriver d) throws java.sql.SQLException {
        if (Driver.TRACE) {
            Object[] args = { host, new Integer(port), info, database, url, d };
            Debug.methodCall(this, "constructor", args);
        }

        this.defaultTimeZone = TimeZone.getDefault();

        this.serverVariables = new HashMap();

        if (host == null) {
            this.host = "localhost";
            hostList = new ArrayList();
            hostList.add(this.host);
        } else if (host.indexOf(",") != -1) {
            // multiple hosts separated by commas (failover)
            hostList = StringUtils.split(host, ",", true);
        } else {
            this.host = host;
            hostList = new ArrayList();
            hostList.add(this.host);
        }

        hostListSize = hostList.size();
        this.port = port;

        if (database == null) {
            database = "";
        }

        this.database = database;
        this.myURL = url;
        this.myDriver = d;
        this.user = info.getProperty("user");
        this.password = info.getProperty("password");

        if ((this.user == null) || this.user.equals("")) {
            this.user = "nobody";
        }

        if (this.password == null) {
            this.password = "";
        }

        this.props = info;
        initializeDriverProperties(info);

        if (Driver.DEBUG) {
            System.out.println("Connect: " + this.user + " to " + this.database);
        }

        try {
            createNewIO(false);
            this.dbmd = new DatabaseMetaData(this, this.database);
        } catch (java.sql.SQLException ex) {
            cleanup(ex);

            // don't clobber SQL exceptions
            throw ex;
        } catch (Exception ex) {
            cleanup(ex);

            StringBuffer mesg = new StringBuffer();

            if (!useParanoidErrorMessages()) {
                mesg.append("Cannot connect to MySQL server on ");
                mesg.append(this.host);
                mesg.append(":");
                mesg.append(this.port);
                mesg.append(".\n\n");
                mesg.append("Make sure that there is a MySQL server ");
                mesg.append("running on the machine/port you are trying ");
                mesg.append(
                    "to connect to and that the machine this software is "
                    + "running on ");
                mesg.append("is able to connect to this host/port "
                    + "(i.e. not firewalled). ");
                mesg.append(
                    "Also make sure that the server has not been started "
                    + "with the --skip-networking ");
                mesg.append("flag.\n\n");
            } else {
                mesg.append("Unable to connect to database.");
            }

            mesg.append("Underlying exception: \n\n");
            mesg.append(ex.getClass().getName());

            if (!this.paranoid) {
                mesg.append(Util.stackTraceToString(ex));
            }

            throw new java.sql.SQLException(mesg.toString(),
                SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE);
        }
    }

    /**
     * If a connection is in auto-commit mode, than all its SQL statements will
     * be executed and committed as individual transactions.  Otherwise, its
     * SQL statements are grouped into transactions that are terminated by
     * either commit() or rollback().  By default, new connections are in
     * auto- commit mode.  The commit occurs when the statement completes or
     * the next execute occurs, whichever comes first.  In the case of
     * statements returning a ResultSet, the statement completes when the last
     * row of the ResultSet has been retrieved or the ResultSet has been
     * closed.  In advanced cases, a single statement may return multiple
     * results as well as output parameter values.  Here the commit occurs
     * when all results and output param values have been retrieved.
     * 
     * <p>
     * <b>Note:</b> MySQL does not support transactions, so this method is a
     * no-op.
     * </p>
     *
     * @param autoCommit - true enables auto-commit; false disables it
     *
     * @exception java.sql.SQLException if a database access error occurs
     * @throws SQLException DOCUMENT ME!
     */
    public void setAutoCommit(boolean autoCommit) throws java.sql.SQLException {
        if (Driver.TRACE) {
            Object[] args = { new Boolean(autoCommit) };
            Debug.methodCall(this, "setAutoCommit", args);
        }

        checkClosed();

        if (this.transactionsSupported) {
            // this internal value must be set first as failover depends on it
            // being set to true to fail over (which is done by most
            // app servers and connection pools at the end of
            // a transaction), and the driver issues an implicit set
            // based on this value when it (re)-connects to a server
            // so the value holds across connections
            //
            this.autoCommit = autoCommit;

            //
            // This is to catch the 'edge' case of
            // autoCommit going from true -> false
            //
            if ((this.highAvailability || this.failedOver) && !this.autoCommit
                    && this.needsPing) {
                pingAndReconnect(true);
            }

            String sql = "SET autocommit=" + (autoCommit ? "1" : "0");
            execSQL(sql, -1, this.database);
        } else {
            if ((autoCommit == false) && (this.relaxAutoCommit == false)) {
                throw new SQLException("MySQL Versions Older than 3.23.15 "
                    + "do not support transactions",
                    SQLError.SQL_STATE_DRIVER_NOT_CAPABLE);
            } else {
                this.autoCommit = autoCommit;
            }
        }

        return;
    }

    /**
     * gets the current auto-commit state
     *
     * @return Current state of the auto-commit mode
     *
     * @exception java.sql.SQLException (why?)
     *
     * @see setAutoCommit
     */
    public boolean getAutoCommit() throws java.sql.SQLException {
        if (Driver.TRACE) {
            Object[] args = new Object[0];
            Debug.methodCall(this, "getAutoCommit", args);
            Debug.returnValue(this, "getAutoCommit",
                new Boolean(this.autoCommit));
        }

        return this.autoCommit;
    }

    /**
     * A sub-space of this Connection's database may be selected by setting a
     * catalog name.  If the driver does not support catalogs, it will
     * silently ignore this request
     * 
     * <p>
     * <b>Note:</b> MySQL's notion of catalogs are individual databases.
     * </p>
     *
     * @param catalog the database for this connection to use
     *
     * @throws java.sql.SQLException if a database access error occurs
     */
    public void setCatalog(String catalog) throws java.sql.SQLException {
        if (Driver.TRACE) {
            Object[] args = { catalog };
            Debug.methodCall(this, "setCatalog", args);
        }

        checkClosed();

        String quotedId = this.dbmd.getIdentifierQuoteString();

        if ((quotedId == null) || quotedId.equals(" ")) {
            quotedId = "";
        }

        StringBuffer query = new StringBuffer("USE ");
        query.append(quotedId);
        query.append(catalog);
        query.append(quotedId);

        execSQL(query.toString(), -1, catalog);
        this.database = catalog;
    }

    /**
     * Return the connections current catalog name, or null if no catalog name
     * is set, or we dont support catalogs.
     * 
     * <p>
     * <b>Note:</b> MySQL's notion of catalogs are individual databases.
     * </p>
     *
     * @return the current catalog name or null
     *
     * @exception java.sql.SQLException if a database access error occurs
     */
    public String getCatalog() throws java.sql.SQLException {
        if (Driver.TRACE) {
            Object[] args = new Object[0];
            Debug.methodCall(this, "getCatalog", args);
            Debug.returnValue(this, "getCatalog", this.database);
        }

        return this.database;
    }

    /**
     * Returns whether we clobber streaming results on new queries, or issue an
     * error?
     *
     * @return true if we should implicitly close streaming result sets upon
     *         receiving a new query
     */
    public boolean getClobberStreamingResults() {
        return this.clobberStreamingResults;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isClosed() {
        if (Driver.TRACE) {
            Object[] args = new Object[0];
            Debug.methodCall(this, "isClosed", args);
            Debug.returnValue(this, "isClosed", new Boolean(this.isClosed));
        }

        return this.isClosed;
    }

    /**
     * Returns the character encoding for this Connection
     *
     * @return the character encoding for this connection.
     */
    public String getEncoding() {
        return this.encoding;
    }

    /**
     * @see Connection#setHoldability(int)
     */
    public void setHoldability(int arg0) throws SQLException {
        // do nothing
    }

    /**
     * @see Connection#getHoldability()
     */
    public int getHoldability() throws SQLException {
        return ResultSet.CLOSE_CURSORS_AT_COMMIT;
    }

    /**
     * NOT JDBC-Compliant, but clients can use this method to determine how
     * long this connection has been idle. This time (reported in
     * milliseconds) is updated once a query has completed.
     *
     * @return number of ms that this connection has been idle, 0 if the driver
     *         is busy retrieving results.
     */
    public long getIdleFor() {
        if (this.lastQueryFinishedTime == 0) {
            return 0;
        } else {
            long now = System.currentTimeMillis();
            long idleTime = now - this.lastQueryFinishedTime;

            return idleTime;
        }
    }

    /**
     * Should we tell MySQL that we're an interactive client
     *
     * @return true if isInteractiveClient was set to true.
     */
    public boolean isInteractiveClient() {
        return isInteractiveClient;
    }

    /**
     * A connection's database is able to provide information describing its
     * tables, its supported SQL grammar, its stored procedures, the
     * capabilities of this connection, etc.  This information is made
     * available through a DatabaseMetaData object.
     *
     * @return a DatabaseMetaData object for this connection
     *
     * @exception java.sql.SQLException if a database access error occurs
     */
    public java.sql.DatabaseMetaData getMetaData() throws java.sql.SQLException {
        checkClosed();

        return new DatabaseMetaData(this, this.database);
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public String getNegativeInfinityRep() {
        return negativeInfinityRep;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public boolean isNegativeInfinityRepIsClipped() {
        return negativeInfinityRepIsClipped;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public String getNotANumberRep() {
        return notANumberRep;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public boolean isNotANumberRepIsClipped() {
        return notANumberRepIsClipped;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public String getPositiveInfinityRep() {
        return positiveInfinityRep;
    }

    /**
     * DOCUMENT ME!
     *
     * @return
     */
    public boolean isPositiveInfinityRepIsClipped() {
        return positiveInfinityRepIsClipped;
    }

    /**
     * Should the driver do profiling?
     *
     * @param flag set to true to enable profiling.
     *
     * @throws SQLException if the connection is closed.
     */
    public void setProfileSql(boolean flag) throws SQLException {
        // For re-connection
        this.props.setProperty("profileSql", String.valueOf(flag));
        getIO().setProfileSql(flag);
    }

    /**
     * You can put a connection in read-only mode as a hint to enable database
     * optimizations <B>Note:</B> setReadOnly cannot be called while in the
     * middle of a transaction
     *
     * @param readOnly - true enables read-only mode; false disables it
     *
     * @exception java.sql.SQLException if a database access error occurs
     */
    public void setReadOnly(boolean readOnly) throws java.sql.SQLException {
        if (Driver.TRACE) {
            Object[] args = { new Boolean(readOnly) };
            Debug.methodCall(this, "setReadOnly", args);
            Debug.returnValue(this, "setReadOnly", new Boolean(readOnly));
        }

        checkClosed();
        this.readOnly = readOnly;
    }

    /**
     * Tests to see if the connection is in Read Only Mode.  Note that we
     * cannot really put the database in read only mode, but we pretend we can
     * by returning the value of the readOnly flag
     *
     * @return true if the connection is read only
     *
     * @exception java.sql.SQLException if a database access error occurs
     */
    public boolean isReadOnly() throws java.sql.SQLException {
        if (Driver.TRACE) {
            Object[] args = new Object[0];
            Debug.methodCall(this, "isReadOnly", args);
            Debug.returnValue(this, "isReadOnly", new Boolean(this.readOnly));
        }

        return this.readOnly;
    }

    /**
     * @see Connection#setSavepoint()
     */
    public java.sql.Savepoint setSavepoint() throws SQLException {
        throw new NotImplemented();
    }

    /**
     * @see Connection#setSavepoint(String)
     */
    public java.sql.Savepoint setSavepoint(String arg0)
        throws SQLException {
        throw new NotImplemented();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public TimeZone getServerTimezone() {
        return this.serverTimezone;
    }

    /**
     * DOCUMENT ME!
     *
     * @param level DOCUMENT ME!
     *
     * @throws java.sql.SQLException DOCUMENT ME!
     * @throws SQLException DOCUMENT ME!
     */
    public void setTransactionIsolation(int level) throws java.sql.SQLException {
        if (Driver.TRACE) {
            Object[] args = { new Integer(level) };
            Debug.methodCall(this, "setTransactionIsolation", args);
        }

        checkClosed();

        if (this.hasIsolationLevels) {
            StringBuffer sql = new StringBuffer(
                    "SET SESSION TRANSACTION ISOLATION LEVEL ");

            switch (level) {
            case java.sql.Connection.TRANSACTION_NONE:
                throw new SQLException("Transaction isolation level "
                    + "NONE not supported by MySQL");

            case java.sql.Connection.TRANSACTION_READ_COMMITTED:
                sql.append("READ COMMITTED");

                break;

            case java.sql.Connection.TRANSACTION_READ_UNCOMMITTED:
                sql.append("READ UNCOMMITTED");

                break;

            case java.sql.Connection.TRANSACTION_REPEATABLE_READ:
                sql.append("REPEATABLE READ");

                break;

            case java.sql.Connection.TRANSACTION_SERIALIZABLE:
                sql.append("SERIALIZABLE");

                break;

            default:
                throw new SQLException("Unsupported transaction "
                    + "isolation level '" + level + "'", "S1C00");
            }

            execSQL(sql.toString(), -1, this.database);
            isolationLevel = level;
        } else {
            throw new java.sql.SQLException("Transaction Isolation Levels are "
                + "not supported on MySQL versions older than 3.23.36.", "S1C00");
        }
    }

    /**
     * Get this Connection's current transaction isolation mode.
     *
     * @return the current TRANSACTION_ mode value
     *
     * @exception java.sql.SQLException if a database access error occurs
     * @throws SQLException DOCUMENT ME!
     */
    public int getTransactionIsolation() throws java.sql.SQLException {
        if (Driver.TRACE) {
            Object[] args = new Object[0];
            Debug.methodCall(this, "getTransactionIsolation", args);
            Debug.returnValue(this, "getTransactionIsolation",
                new Integer(isolationLevel));
        }

        if (this.hasIsolationLevels) {
            java.sql.Statement stmt = null;
            java.sql.ResultSet rs = null;

            try {
                stmt = this.createStatement();

                if (stmt.getMaxRows() != 0) {
                    stmt.setMaxRows(0);
                }

                String query = null;

                if (this.io.versionMeetsMinimum(4, 0, 3)) {
                    query = "SHOW VARIABLES LIKE 'tx_isolation'";
                } else {
                    query = "SHOW VARIABLES LIKE 'transaction_isolation'";
                }

                rs = stmt.executeQuery(query);

                if (rs.next()) {
                    String s = rs.getString(2);

                    if (s != null) {
                        Integer intTI = (Integer) mapTransIsolationName2Value
                            .get(s);

                        if (intTI != null) {
                            return intTI.intValue();
                        }
                    }

                    throw new SQLException(
                        "Could not map transaction isolation '" + s
                        + " to a valid JDBC level.",
                        SQLError.SQL_STATE_GENERAL_ERROR);
                } else {
                    throw new SQLException("Could not retrieve transaction isolation level from server",
                        SQLError.SQL_STATE_GENERAL_ERROR);
                }
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (Exception ex) {
                        // ignore
                    }

                    rs = null;
                }

                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (Exception ex) {
                        // ignore
                    }

                    stmt = null;
                }
            }
        }

        return isolationLevel;
    }

    /**
     * JDBC 2.0 Install a type-map object as the default type-map for this
     * connection
     *
     * @param map the type mapping
     *
     * @throws SQLException if a database error occurs.
     */
    public void setTypeMap(java.util.Map map) throws SQLException {
        this.typeMap = map;
    }

    /**
     * JDBC 2.0 Get the type-map object associated with this connection. By
     * default, the map returned is empty.
     *
     * @return the type map
     *
     * @throws SQLException if a database error occurs
     */
    public synchronized java.util.Map getTypeMap() throws SQLException {
        if (this.typeMap == null) {
            this.typeMap = new HashMap();
        }

        return this.typeMap;
    }

    /**
     * The first warning reported by calls on this Connection is returned.
     * <B>Note:</B> Sebsequent warnings will be changed to this
     * java.sql.SQLWarning
     *
     * @return the first java.sql.SQLWarning or null
     *
     * @exception java.sql.SQLException if a database access error occurs
     */
    public java.sql.SQLWarning getWarnings() throws java.sql.SQLException {
        if (Driver.TRACE) {
            Object[] args = new Object[0];
            Debug.methodCall(this, "getWarnings", args);
            Debug.returnValue(this, "getWarnings", null);
        }

        return null;
    }

    /**
     * Allow use of LOAD LOCAL INFILE?
     *
     * @return true if allowLoadLocalInfile was set to true.
     */
    public boolean allowLoadLocalInfile() {
        return this.allowLoadLocalInfile;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean capitalizeDBMDTypes() {
        return this.capitalizeDBMDTypes;
    }

    /**
     * Changes the user on this connection by performing a re-authentication.
     * If authentication fails, the connection will remain under the context
     * of the current user.
     *
     * @param userName the username to authenticate with
     * @param newPassword the password to authenticate with
     *
     * @throws SQLException if authentication fails, or some other error occurs
     *         while performing the command.
     */
    public void changeUser(String userName, String newPassword)
        throws SQLException {
        if ((userName == null) || userName.equals("")) {
            userName = "";
        }

        if (newPassword == null) {
            newPassword = "";
        }

        this.io.changeUser(userName, newPassword, this.database);
        this.user = userName;
        this.password = newPassword;
    }

    /**
     * After this call, getWarnings returns null until a new warning is
     * reported for this connection.
     *
     * @exception java.sql.SQLException if a database access error occurs
     */
    public void clearWarnings() throws java.sql.SQLException {
        if (Driver.TRACE) {
            Object[] args = new Object[0];
            Debug.methodCall(this, "clearWarnings", args);
        }

        // firstWarning = null;
    }

    /**
     * In some cases, it is desirable to immediately release a Connection's
     * database and JDBC resources instead of waiting for them to be
     * automatically released (cant think why off the top of my head)
     * <B>Note:</B> A Connection is automatically closed when it is garbage
     * collected.  Certain fatal errors also result in a closed connection.
     *
     * @exception java.sql.SQLException if a database access error occurs
     */
    public void close() throws java.sql.SQLException {
        if (this.explicitCloseLocation == null) {
            this.explicitCloseLocation = new Throwable();
        }

        realClose(true, true);
    }

    /**
     * The method commit() makes all changes made since the previous
     * commit/rollback permanent and releases any database locks currently
     * held by the Connection.  This method should only be used when
     * auto-commit has been disabled.
     *
     * @exception java.sql.SQLException if a database access error occurs
     *
     * @see setAutoCommit
     */
    public void commit() throws java.sql.SQLException {
        if (Driver.TRACE) {
            Object[] args = new Object[0];
            Debug.methodCall(this, "commit", args);
        }

        checkClosed();

        try {
            // no-op if _relaxAutoCommit == true
            if (this.autoCommit && !this.relaxAutoCommit) {
                throw new SQLException("Can't call commit when autocommit=true",
                    SQLError.SQL_STATE_GENERAL_ERROR);
            } else if (this.transactionsSupported) {
                execSQL("commit", -1, this.database);
            }
        } finally {
            if (this.reconnectAtTxEnd) {
                pingAndReconnect(true);
            }
        }

        return;
    }

    //--------------------------JDBC 2.0-----------------------------

    /**
     * JDBC 2.0 Same as createStatement() above, but allows the default result
     * set type and result set concurrency type to be overridden.
     *
     * @param resultSetType a result set type, see ResultSet.TYPE_XXX
     * @param resultSetConcurrency a concurrency type, see ResultSet.CONCUR_XXX
     *
     * @return a new Statement object
     *
     * @exception SQLException if a database-access error occurs.
     */
    public java.sql.Statement createStatement(int resultSetType,
        int resultSetConcurrency) throws SQLException {
        checkClosed();

        Statement stmt = new com.mysql.jdbc.Statement(this, this.database);
        stmt.setResultSetType(resultSetType);
        stmt.setResultSetConcurrency(resultSetConcurrency);

        return stmt;
    }

    /**
     * SQL statements without parameters are normally executed using Statement
     * objects.  If the same SQL statement is executed many times, it is more
     * efficient to use a PreparedStatement
     *
     * @return a new Statement object
     *
     * @throws SQLException passed through from the constructor
     */
    public java.sql.Statement createStatement() throws SQLException {
        return createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
            java.sql.ResultSet.CONCUR_READ_ONLY);
    }

    /**
     * @see Connection#createStatement(int, int, int)
     */
    public java.sql.Statement createStatement(int resultSetType,
        int resultSetConcurrency, int resultSetHoldability)
        throws SQLException {
        if (this.pedantic) {
            if (resultSetHoldability != ResultSet.HOLD_CURSORS_OVER_COMMIT) {
                throw new SQLException("HOLD_CUSRORS_OVER_COMMIT is only supported holdability level",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        }

        return createStatement(resultSetType, resultSetConcurrency);
    }

    /**
     * Cleanup the connection.
     *
     * @throws Throwable if an error occurs during cleanup.
     */
    protected void finalize() throws Throwable {
        cleanup(null);
    }

    /**
     * Is the server configured to use lower-case table names only?
     *
     * @return true if lower_case_table_names is 'on'
     */
    public boolean lowerCaseTableNames() {
        return this.lowerCaseTableNames;
    }

    /**
     * A driver may convert the JDBC sql grammar into its system's native SQL
     * grammar prior to sending it; nativeSQL returns the native form of the
     * statement that the driver would have sent.
     *
     * @param sql a SQL statement that may contain one or more '?' parameter
     *        placeholders
     *
     * @return the native form of this statement
     *
     * @exception java.sql.SQLException if a database access error occurs
     */
    public String nativeSQL(String sql) throws java.sql.SQLException {
        if (Driver.TRACE) {
            Object[] args = { sql };
            Debug.methodCall(this, "nativeSQL", args);
            Debug.returnValue(this, "nativeSQL", sql);
        }

        return EscapeProcessor.escapeSQL(sql,
            getIO().versionMeetsMinimum(4, 0, 2));
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean parserKnowsUnicode() {
        return this.parserKnowsUnicode;
    }

    /**
     * DOCUMENT ME!
     *
     * @param sql DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws java.sql.SQLException DOCUMENT ME!
     */
    public java.sql.CallableStatement prepareCall(String sql)
        throws java.sql.SQLException {
        if (this.getUseUltraDevWorkAround()) {
            return new UltraDevWorkAround(prepareStatement(sql));
        } else {
            throw new java.sql.SQLException("Callable statments not "
                + "supported.", "S1C00");
        }
    }

    /**
     * JDBC 2.0 Same as prepareCall() above, but allows the default result set
     * type and result set concurrency type to be overridden.
     *
     * @param sql the SQL representing the callable statement
     * @param resultSetType a result set type, see ResultSet.TYPE_XXX
     * @param resultSetConcurrency a concurrency type, see ResultSet.CONCUR_XXX
     *
     * @return a new CallableStatement object containing the pre-compiled SQL
     *         statement
     *
     * @exception SQLException if a database-access error occurs.
     */
    public java.sql.CallableStatement prepareCall(String sql,
        int resultSetType, int resultSetConcurrency) throws SQLException {
        return prepareCall(sql);
    }

    /**
     * @see Connection#prepareCall(String, int, int, int)
     */
    public java.sql.CallableStatement prepareCall(String sql,
        int resultSetType, int resultSetConcurrency, int resultSetHoldability)
        throws SQLException {
        if (this.pedantic) {
            if (resultSetHoldability != ResultSet.HOLD_CURSORS_OVER_COMMIT) {
                throw new SQLException("HOLD_CUSRORS_OVER_COMMIT is only supported holdability level",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        }

        throw new NotImplemented();
    }

    /**
     * A SQL statement with or without IN parameters can be pre-compiled and
     * stored in a PreparedStatement object.  This object can then be used to
     * efficiently execute this statement multiple times.
     * 
     * <p>
     * <B>Note:</B> This method is optimized for handling parametric SQL
     * statements that benefit from precompilation if the driver supports
     * precompilation. In this case, the statement is not sent to the database
     * until the PreparedStatement is executed.  This has no direct effect on
     * users; however it does affect which method throws certain
     * java.sql.SQLExceptions
     * </p>
     * 
     * <p>
     * MySQL does not support precompilation of statements, so they are handled
     * by the driver.
     * </p>
     *
     * @param sql a SQL statement that may contain one or more '?' IN parameter
     *        placeholders
     *
     * @return a new PreparedStatement object containing the pre-compiled
     *         statement.
     *
     * @exception java.sql.SQLException if a database access error occurs.
     */
    public java.sql.PreparedStatement prepareStatement(String sql)
        throws java.sql.SQLException {
        return prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
            java.sql.ResultSet.CONCUR_READ_ONLY);
    }

    /**
     * JDBC 2.0 Same as prepareStatement() above, but allows the default result
     * set type and result set concurrency type to be overridden.
     *
     * @param sql the SQL query containing place holders
     * @param resultSetType a result set type, see ResultSet.TYPE_XXX
     * @param resultSetConcurrency a concurrency type, see ResultSet.CONCUR_XXX
     *
     * @return a new PreparedStatement object containing the pre-compiled SQL
     *         statement
     *
     * @exception SQLException if a database-access error occurs.
     */
    public synchronized java.sql.PreparedStatement prepareStatement(
        String sql, int resultSetType, int resultSetConcurrency)
        throws SQLException {
        checkClosed();

        PreparedStatement pStmt = null;

        if (this.cachePreparedStatements) {
            PreparedStatement.ParseInfo pStmtInfo = (PreparedStatement.ParseInfo) cachedPreparedStatementParams
                .get(sql);

            if (pStmtInfo == null) {
                pStmt = new com.mysql.jdbc.PreparedStatement(this, sql,
                        this.database);

                PreparedStatement.ParseInfo parseInfo = pStmt.getParseInfo();

                if (parseInfo.statementLength < this.preparedStatementCacheMaxSqlSize) {
                    if (this.cachedPreparedStatementParams.size() >= 25) {
                        Iterator oldestIter = this.cachedPreparedStatementParams.keySet()
                                                                                .iterator();
                        long lruTime = Long.MAX_VALUE;
                        String oldestSql = null;

                        while (oldestIter.hasNext()) {
                            String sqlKey = (String) oldestIter.next();
                            PreparedStatement.ParseInfo lruInfo = (PreparedStatement.ParseInfo) this.cachedPreparedStatementParams
                                .get(sqlKey);

                            if (lruInfo.lastUsed < lruTime) {
                                lruTime = lruInfo.lastUsed;
                                oldestSql = sqlKey;
                            }
                        }

                        if (oldestSql != null) {
                            this.cachedPreparedStatementParams.remove(oldestSql);
                        }
                    }

                    cachedPreparedStatementParams.put(sql, pStmt.getParseInfo());
                }
            } else {
                pStmtInfo.lastUsed = System.currentTimeMillis();
                pStmt = new com.mysql.jdbc.PreparedStatement(this, sql,
                        this.database, pStmtInfo);
            }
        } else {
            pStmt = new com.mysql.jdbc.PreparedStatement(this, sql,
                    this.database);
        }

        //
        // FIXME: Create warnings if can't create results of the given
        //        type or concurrency
        //
        pStmt.setResultSetType(resultSetType);
        pStmt.setResultSetConcurrency(resultSetConcurrency);

        return pStmt;
    }

    /**
     * @see Connection#prepareStatement(String, int, int, int)
     */
    public java.sql.PreparedStatement prepareStatement(String sql,
        int resultSetType, int resultSetConcurrency, int resultSetHoldability)
        throws SQLException {
        if (this.pedantic) {
            if (resultSetHoldability != ResultSet.HOLD_CURSORS_OVER_COMMIT) {
                throw new SQLException("HOLD_CUSRORS_OVER_COMMIT is only supported holdability level",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        }

        return prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    /**
     * @see Connection#prepareStatement(String, int)
     */
    public java.sql.PreparedStatement prepareStatement(String sql,
        int autoGenKeyIndex) throws SQLException {
        java.sql.PreparedStatement pStmt = prepareStatement(sql);

        ((com.mysql.jdbc.PreparedStatement) pStmt).setRetrieveGeneratedKeys(autoGenKeyIndex == Statement.RETURN_GENERATED_KEYS);

        return pStmt;
    }

    /**
     * @see Connection#prepareStatement(String, int[])
     */
    public java.sql.PreparedStatement prepareStatement(String sql,
        int[] autoGenKeyIndexes) throws SQLException {
        java.sql.PreparedStatement pStmt = prepareStatement(sql);

        ((com.mysql.jdbc.PreparedStatement) pStmt).setRetrieveGeneratedKeys((autoGenKeyIndexes != null)
            && (autoGenKeyIndexes.length > 0));

        return pStmt;
    }

    /**
     * @see Connection#prepareStatement(String, String[])
     */
    public java.sql.PreparedStatement prepareStatement(String sql,
        String[] autoGenKeyColNames) throws SQLException {
        java.sql.PreparedStatement pStmt = prepareStatement(sql);

        ((com.mysql.jdbc.PreparedStatement) pStmt).setRetrieveGeneratedKeys((autoGenKeyColNames != null)
            && (autoGenKeyColNames.length > 0));

        return pStmt;
    }

    /**
     * @see Connection#releaseSavepoint(Savepoint)
     */
    public void releaseSavepoint(Savepoint arg0) throws SQLException {
        throw new NotImplemented();
    }

    /**
     * Resets the server-side state of this connection. Doesn't work for MySQL
     * versions older than 4.0.6 or if isParanoid() is set (it will become  a
     * no-op in these cases). Usually only used from connection pooling code.
     *
     * @throws SQLException if the operation fails while resetting server
     *         state.
     */
    public void resetServerState() throws SQLException {
        if (!this.paranoid
                && ((this.io != null) & this.io.versionMeetsMinimum(4, 0, 6))) {
            changeUser(this.user, this.password);
        }
    }

    /**
     * The method rollback() drops all changes made since the previous
     * commit/rollback and releases any database locks currently held by the
     * Connection.
     *
     * @exception java.sql.SQLException if a database access error occurs
     * @throws SQLException DOCUMENT ME!
     *
     * @see commit
     */
    public void rollback() throws java.sql.SQLException {
        if (Driver.TRACE) {
            Object[] args = new Object[0];
            Debug.methodCall(this, "rollback", args);
        }

        checkClosed();

        try {
            // no-op if _relaxAutoCommit == true
            if (this.autoCommit && !this.relaxAutoCommit) {
                throw new SQLException("Can't call rollback when autocommit=true",
                    SQLError.SQL_STATE_GENERAL_ERROR);
            } else if (this.transactionsSupported) {
                try {
                    rollbackNoChecks();
                } catch (SQLException sqlEx) {
                    // We ignore non-transactional tables if told to do so
                    if (this.ignoreNonTxTables
                            && (sqlEx.getErrorCode() != MysqlDefs.ER_WARNING_NOT_COMPLETE_ROLLBACK)) {
                        throw sqlEx;
                    }
                }
            }
        } finally {
            if (this.reconnectAtTxEnd) {
                pingAndReconnect(true);
            }
        }
    }

    /**
     * @see Connection#rollback(Savepoint)
     */
    public void rollback(Savepoint arg0) throws SQLException {
        throw new NotImplemented();
    }

    /**
     * Used by MiniAdmin to shutdown a MySQL server
     *
     * @throws SQLException if the command can not be issued.
     */
    public void shutdownServer() throws SQLException {
        try {
            this.io.sendCommand(MysqlDefs.SHUTDOWN, null, null);
        } catch (Exception ex) {
            throw new SQLException("Unhandled exception '" + ex.toString()
                + "'", SQLError.SQL_STATE_GENERAL_ERROR);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean supportsIsolationLevel() {
        return this.hasIsolationLevels;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean supportsQuotedIdentifiers() {
        return this.hasQuotedIdentifiers;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean supportsTransactions() {
        return this.transactionsSupported;
    }

    /**
     * Should we use compression?
     *
     * @return should we use compression to communicate with the server?
     */
    public boolean useCompression() {
        return this.useCompression;
    }

    /**
     * Returns the paranoidErrorMessages.
     *
     * @return boolean if we should be paranoid about error messages.
     */
    public boolean useParanoidErrorMessages() {
        return paranoid;
    }

    /**
     * Should we use SSL?
     *
     * @return should we use SSL to communicate with the server?
     */
    public boolean useSSL() {
        return this.useSSL;
    }

    /**
     * Should we enable work-arounds for floating point rounding errors in the
     * server?
     *
     * @return should we use floating point work-arounds?
     */
    public boolean useStrictFloatingPoint() {
        return this.strictFloatingPoint;
    }

    /**
     * Returns the strictUpdates value.
     *
     * @return boolean
     */
    public boolean useStrictUpdates() {
        return strictUpdates;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean useTimezone() {
        return this.useTimezone;
    }

    /**
     * Should unicode character mapping be used ?
     *
     * @return should we use Unicode character mapping?
     */
    public boolean useUnicode() {
        return this.doUnicode;
    }

    /**
     * Returns the Java character encoding name for the given MySQL server
     * charset index
     *
     * @param charsetIndex
     *
     * @return the Java character encoding name for the given MySQL server
     *         charset index
     *
     * @throws SQLException if the character set index isn't known by the
     *         driver
     */
    protected String getCharsetNameForIndex(int charsetIndex)
        throws SQLException {
        String charsetName = null;

        if (charsetIndex != MysqlDefs.NO_CHARSET_INFO) {
            try {
                charsetName = this.indexToCharsetMapping[charsetIndex];
            } catch (ArrayIndexOutOfBoundsException outOfBoundsEx) {
                throw new SQLException(
                    "Unknown character set index for field '" + charsetIndex
                    + "' received from server.",
                    SQLError.SQL_STATE_GENERAL_ERROR);
            }

            // Punt
            if (charsetName == null) {
                charsetName = getEncoding();
            }
        } else {
            charsetName = getEncoding();
        }

        return charsetName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the defaultTimeZone.
     */
    protected TimeZone getDefaultTimeZone() {
        return defaultTimeZone;
    }

    /**
     * Returns the IO channel to the server
     *
     * @return the IO channel to the server
     *
     * @throws SQLException if the connection is closed.
     */
    protected MysqlIO getIO() throws SQLException {
        if ((this.io == null) || this.isClosed) {
            throw new SQLException("Operation not allowed on closed connection",
                "08003");
        }

        return this.io;
    }

    protected int getNetWriteTimeout() {
        String netWriteTimeoutStr = (String) this.serverVariables.get(
                "net_write_timeout");

        if (netWriteTimeoutStr != null) {
            try {
                return Integer.parseInt(netWriteTimeoutStr);
            } catch (NumberFormatException nfe) {
                return Integer.MAX_VALUE;
            }
        } else {
            return Integer.MAX_VALUE;
        }
    }

    /**
     * Is this connection using unbuffered input?
     *
     * @return whether or not to use buffered input streams
     */
    protected boolean isUsingUnbufferedInput() {
        return this.useUnbufferedInput;
    }

    /**
     * Creates an IO channel to the server
     *
     * @param isForReconnect is this request for a re-connect
     *
     * @return a new MysqlIO instance connected to a server
     *
     * @throws SQLException if a database access error occurs
     */
    protected com.mysql.jdbc.MysqlIO createNewIO(boolean isForReconnect)
        throws SQLException {
        MysqlIO newIo = null;

        if (!highAvailability && !this.failedOver) {
            for (int hostIndex = 0; hostIndex < hostListSize; hostIndex++) {
                try {
                    String newHostPortPair = (String) this.hostList.get(hostIndex);

                    int newPort = 3306;
                    
                    String[] hostPortPair = NonRegisteringDriver.parseHostPortPair(newHostPortPair);
                    String newHost = hostPortPair[NonRegisteringDriver.HOST_NAME_INDEX];
                	
                    if (newHost == null || newHost.trim().length() == 0) {
                    	newHost = "localhost";
                    }
                	
                	if (hostPortPair[NonRegisteringDriver.PORT_NUMBER_INDEX] != null) {
                		try {
                            newPort = Integer.parseInt(hostPortPair[NonRegisteringDriver.PORT_NUMBER_INDEX]);
                        } catch (NumberFormatException nfe) {
                            throw new SQLException(
                                "Illegal connection port value '"
                                + hostPortPair[NonRegisteringDriver.PORT_NUMBER_INDEX] + "'",
                                SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE);
                        }
                	}

                    this.io = new MysqlIO(newHost, newPort,
                            this.socketFactoryClassName, this.props, this,
                            this.socketTimeout);
                    this.io.doHandshake(this.user, this.password, this.database);
                    this.isClosed = false;

                    if (this.database.length() != 0) {
                        this.io.sendCommand(MysqlDefs.INIT_DB, this.database,
                            null);
                    }

                    // save state from old connection
                    boolean autoCommit = getAutoCommit();
                    int oldIsolationLevel = getTransactionIsolation();
                    boolean oldReadOnly = isReadOnly();
                    String oldCatalog = getCatalog();

                    // Server properties might be different
                    // from previous connection, so initialize
                    // again...
                    initializePropsFromServer(this.props);

                    if (isForReconnect) {
                        // Restore state from old connection
                        setAutoCommit(autoCommit);

                        if (this.hasIsolationLevels) {
                            setTransactionIsolation(oldIsolationLevel);
                        }

                        setCatalog(oldCatalog);
                    }

                    if (hostIndex != 0) {
                        setFailedOverState();
                    } else {
                        this.failedOver = false;

                        if (hostListSize > 1) {
                            setReadOnly(false);
                        } else {
                            setReadOnly(oldReadOnly);
                        }
                    }

                    break; // low-level connection succeeded
                } catch (SQLException sqlEx) {
                    if (this.io != null) {
                        this.io.forceClose();
                    }

                    String sqlState = sqlEx.getSQLState();

                    if ((sqlState == null)
                            || !sqlState.equals(
                                SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE)) {
                        throw sqlEx;
                    }

                    if ((hostListSize - 1) == hostIndex) {
                        throw sqlEx;
                    }
                } catch (Exception unknownException) {
                    if (this.io != null) {
                        this.io.forceClose();
                    }

                    if ((hostListSize - 1) == hostIndex) {
                        throw new SQLException(
                            "Unable to connect to any hosts due to exception: "
                            + unknownException.toString()
                            + (this.paranoid ? ""
                                             : Util.stackTraceToString(
                                unknownException)),
                            SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE);
                    }
                }
            }
        } else {
            double timeout = this.initialTimeout;
            boolean connectionGood = false;
            Exception connectionException = null;

            for (int hostIndex = 0;
                    (hostIndex < hostListSize) && !connectionGood;
                    hostIndex++) {
                for (int attemptCount = 0;
                        !connectionGood && (attemptCount < this.maxReconnects);
                        attemptCount++) {
                    try {
                        if (this.io != null) {
                            this.io.forceClose();
                        }

                        String newHostPortPair = (String) this.hostList.get(hostIndex);

                        int newPort = 3306;
                        
                        String[] hostPortPair = NonRegisteringDriver.parseHostPortPair(newHostPortPair);
                        String newHost = hostPortPair[NonRegisteringDriver.HOST_NAME_INDEX];
                    	
                        if (newHost == null || newHost.trim().length() == 0) {
                        	newHost = "localhost";
                        }
                    	
                    	if (hostPortPair[NonRegisteringDriver.PORT_NUMBER_INDEX] != null) {
                    		try {
                                newPort = Integer.parseInt(hostPortPair[NonRegisteringDriver.PORT_NUMBER_INDEX]);
                            } catch (NumberFormatException nfe) {
                                throw new SQLException(
                                    "Illegal connection port value '"
                                    + hostPortPair[NonRegisteringDriver.PORT_NUMBER_INDEX] + "'",
                                    SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE);
                            }
                    	}

                        this.io = new MysqlIO(newHost, newPort,
                                this.socketFactoryClassName, this.props, this,
                                this.socketTimeout);
                        this.io.doHandshake(this.user, this.password,
                            this.database);

                        if (this.database.length() != 0) {
                            this.io.sendCommand(MysqlDefs.INIT_DB,
                                this.database, null);
                        }

                        ping();
                        this.isClosed = false;

                        // save state from old connection
                        boolean autoCommit = getAutoCommit();
                        int oldIsolationLevel = getTransactionIsolation();
                        boolean oldReadOnly = isReadOnly();
                        String oldCatalog = getCatalog();

                        // Server properties might be different
                        // from previous connection, so initialize
                        // again...
                        initializePropsFromServer(this.props);

                        if (isForReconnect) {
                            // Restore state from old connection
                            setAutoCommit(autoCommit);

                            if (this.hasIsolationLevels) {
                                setTransactionIsolation(oldIsolationLevel);
                            }

                            setCatalog(oldCatalog);
                        }

                        connectionGood = true;

                        if (hostIndex != 0) {
                            setFailedOverState();
                        } else {
                            this.failedOver = false;

                            if (hostListSize > 1) {
                                setReadOnly(false);
                            } else {
                                setReadOnly(oldReadOnly);
                            }
                        }

                        break;
                    } catch (Exception EEE) {
                        connectionException = EEE;
                        connectionGood = false;
                    }

                    if (!connectionGood) {
                        try {
                            Thread.sleep((long) timeout * 1000);
                            timeout = timeout * 2;
                        } catch (InterruptedException IE) {
                            ;
                        }
                    }
                }

                if (!connectionGood) {
                    // We've really failed!
                    throw new SQLException(
                        "Server connection failure during transaction. Due to underlying exception: '"
                        + connectionException + "'."
                        + (this.paranoid ? ""
                                         : Util.stackTraceToString(
                            connectionException)) + "\nAttempted reconnect "
                        + this.maxReconnects + " times. Giving up.",
                        SQLError.SQL_STATE_UNABLE_TO_CONNECT_TO_DATASOURCE);
                }
            }
        }

        if (paranoid && !highAvailability && (hostListSize <= 1)) {
            password = null;
            user = null;
        }

        return newIo;
    }

    /**
     * Closes connection and frees resources.
     *
     * @param calledExplicitly is this being called from close()
     * @param issueRollback should a rollback() be issued?
     *
     * @throws SQLException if an error occurs
     */
    protected void realClose(boolean calledExplicitly, boolean issueRollback)
        throws SQLException {
        if (Driver.TRACE) {
            Object[] args = new Object[] {
                    new Boolean(calledExplicitly), new Boolean(issueRollback)
                };
            Debug.methodCall(this, "realClose", args);
        }

        SQLException sqlEx = null;

        if (!isClosed() && !getAutoCommit() && issueRollback) {
            try {
                rollback();
            } catch (SQLException ex) {
                sqlEx = ex;
            }
        }

        if (this.io != null) {
            try {
                this.io.quit();
            } catch (Exception e) {
                ;
            }

            this.io = null;
        }

        if (this.cachedPreparedStatementParams != null) {
            this.cachedPreparedStatementParams.clear();
            this.cachedPreparedStatementParams = null;
        }

        this.isClosed = true;

        if (sqlEx != null) {
            throw sqlEx;
        }
    }

    /**
     * Returns the locally mapped instance of a charset converter (to avoid
     * overhead of static synchronization).
     *
     * @param javaEncodingName the encoding name to retrieve
     *
     * @return a character converter, or null if one couldn't be mapped.
     */
    synchronized SingleByteCharsetConverter getCharsetConverter(
        String javaEncodingName) {
        SingleByteCharsetConverter converter = (SingleByteCharsetConverter) this.charsetConverterMap
            .get(javaEncodingName);

        if (converter == CHARSET_CONVERTER_NOT_AVAILABLE_MARKER) {
            return null;
        }

        if (converter == null) {
            try {
                converter = SingleByteCharsetConverter.getInstance(javaEncodingName);

                if (converter == null) {
                    this.charsetConverterMap.put(javaEncodingName,
                        CHARSET_CONVERTER_NOT_AVAILABLE_MARKER);
                }

                this.charsetConverterMap.put(javaEncodingName, converter);
            } catch (UnsupportedEncodingException unsupEncEx) {
                this.charsetConverterMap.put(javaEncodingName,
                    CHARSET_CONVERTER_NOT_AVAILABLE_MARKER);

                converter = null;
            }
        }

        return converter;
    }

    /**
     * Returns the maximum packet size the MySQL server will accept
     *
     * @return DOCUMENT ME!
     */
    int getMaxAllowedPacket() {
        return this.maxAllowedPacket;
    }

    /**
     * DOCUMENT ME!
     *
     * @return the max rows to return for statements (by default)
     */
    int getMaxRows() {
        return this.maxRows;
    }

    /**
     * Returns the Mutex all queries are locked against
     *
     * @return DOCUMENT ME!
     *
     * @throws SQLException DOCUMENT ME!
     */
    Object getMutex() throws SQLException {
        if (this.io == null) {
            throw new SQLException("Connection.close() has already been called. Invalid operation in this state.",
                "08003");
        }

        return this.mutex;
    }

    /**
     * Returns the packet buffer size the MySQL server reported upon connection
     *
     * @return DOCUMENT ME!
     */
    int getNetBufferLength() {
        return this.netBufferLength;
    }

    boolean isPedantic() {
        return this.pedantic;
    }

    void setReadInfoMsgEnabled(boolean flag) {
        this.readInfoMsg = flag;
    }

    boolean isReadInfoMsgEnabled() {
        return this.readInfoMsg;
    }

    int getServerMajorVersion() {
        return this.io.getServerMajorVersion();
    }

    int getServerMinorVersion() {
        return this.io.getServerMinorVersion();
    }

    int getServerSubMinorVersion() {
        return this.io.getServerSubMinorVersion();
    }

    String getServerVersion() {
        return this.io.getServerVersion();
    }

    String getURL() {
        return this.myURL;
    }

    /**
     * Set whether or not this connection should use SSL
     *
     * @param flag DOCUMENT ME!
     */
    void setUseSSL(boolean flag) {
        this.useSSL = flag;
    }

    String getUser() {
        return this.user;
    }

    boolean alwaysClearStream() {
        return this.alwaysClearStream;
    }

    boolean continueBatchOnError() {
        return this.continueBatchOnError;
    }

    /**
     * Send a query to the server.  Returns one of the ResultSet objects. This
     * is synchronized, so Statement's queries will be serialized.
     *
     * @param sql the SQL statement to be executed
     * @param maxRowsToRetreive DOCUMENT ME!
     * @param catalog DOCUMENT ME!
     *
     * @return a ResultSet holding the results
     *
     * @exception java.sql.SQLException if a database error occurs
     */
    ResultSet execSQL(String sql, int maxRowsToRetreive, String catalog)
        throws java.sql.SQLException {
        if (Driver.TRACE) {
            Object[] args = { sql, new Integer(maxRowsToRetreive) };
            Debug.methodCall(this, "execSQL", args);
        }

        return execSQL(sql, maxRowsToRetreive, null,
            java.sql.ResultSet.CONCUR_READ_ONLY, catalog);
    }

    ResultSet execSQL(String sql, int maxRows, int resultSetType,
        boolean streamResults, boolean queryIsSelectOnly, String catalog)
        throws SQLException {
        return execSQL(sql, maxRows, null, resultSetType, streamResults,
            queryIsSelectOnly, catalog);
    }

    ResultSet execSQL(String sql, int maxRows, Buffer packet, String catalog)
        throws java.sql.SQLException {
        return execSQL(sql, maxRows, packet,
            java.sql.ResultSet.CONCUR_READ_ONLY, catalog);
    }

    ResultSet execSQL(String sql, int maxRows, Buffer packet,
        int resultSetType, String catalog) throws java.sql.SQLException {
        return execSQL(sql, maxRows, packet, resultSetType, true, false, catalog);
    }

    ResultSet execSQL(String sql, int maxRows, Buffer packet,
        int resultSetType, boolean streamResults, boolean queryIsSelectOnly,
        String catalog) throws java.sql.SQLException {
        if (Driver.TRACE) {
            Object[] args = { sql, new Integer(maxRows), packet };
            Debug.methodCall(this, "execSQL", args);
        }

        if ((sql == null) || (sql.length() == 0)) {
            if (packet == null) {
                throw new SQLException("Query can not be null or empty",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        }

        //
        // Fall-back if the master is back online if we've
        // issued queriesBeforeRetryMaster queries since
        // we failed over
        //
        synchronized (this.mutex) {
            this.lastQueryFinishedTime = 0; // we're busy!

            if ((this.highAvailability || this.failedOver) && this.needsPing) {
                pingAndReconnect(false);
            }

            try {
                int realMaxRows = (maxRows == -1) ? MysqlDefs.MAX_ROWS : maxRows;

                if (packet == null) {
                    String encoding = null;

                    if (useUnicode()) {
                        encoding = getEncoding();
                    }

                    return this.io.sqlQuery(sql, realMaxRows, encoding, this,
                        resultSetType, streamResults, catalog);
                } else {
                    return this.io.sqlQueryDirect(packet, realMaxRows, this,
                        resultSetType, streamResults, catalog);
                }
            } catch (java.sql.SQLException sqlE) {
                // don't clobber SQL exceptions
                String sqlState = sqlE.getSQLState();

                if (this.highAvailability || this.failedOver) {
                    this.needsPing = true;
                } else if ((sqlState != null)
                        && sqlState.equals(
                            SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE)) {
                    cleanup(sqlE);
                }

                throw sqlE;
            } catch (Exception ex) {
                if (this.highAvailability || this.failedOver) {
                    this.needsPing = true;
                } else if (ex instanceof IOException) {
                    cleanup(ex);
                }

                String exceptionType = ex.getClass().getName();
                String exceptionMessage = ex.getMessage();

                if (!this.useParanoidErrorMessages()) {
                    exceptionMessage += "\n\nNested Stack Trace:\n";
                    exceptionMessage += Util.stackTraceToString(ex);
                }

                throw new java.sql.SQLException(
                    "Error during query: Unexpected Exception: "
                    + exceptionType + " message given: " + exceptionMessage,
                    SQLError.SQL_STATE_GENERAL_ERROR);
            } finally {
                this.lastQueryFinishedTime = System.currentTimeMillis();
            }
        }
    }

    /**
     * Has the maxRows value changed?
     *
     * @param stmt DOCUMENT ME!
     */
    void maxRowsChanged(Statement stmt) {
        synchronized (this.mutex) {
            if (this.statementsUsingMaxRows == null) {
                this.statementsUsingMaxRows = new HashMap();
            }

            this.statementsUsingMaxRows.put(stmt, stmt);

            this.maxRowsChanged = true;
        }
    }

    /**
     * Called by statements on their .close() to let the connection know when
     * it is safe to set the connection back to 'default' row limits.
     *
     * @param stmt the statement releasing it's max-rows requirement
     *
     * @throws SQLException if a database error occurs issuing the statement
     *         that sets the limit default.
     */
    void unsetMaxRows(Statement stmt) throws SQLException {
        synchronized (this.mutex) {
            if (this.statementsUsingMaxRows != null) {
                Object found = this.statementsUsingMaxRows.remove(stmt);

                if ((found != null)
                        && (this.statementsUsingMaxRows.size() == 0)) {
                    execSQL("SET OPTION SQL_SELECT_LIMIT=DEFAULT", -1,
                        this.database);
                    this.maxRowsChanged = false;
                }
            }
        }
    }

    boolean useAnsiQuotedIdentifiers() {
        return this.useAnsiQuotes;
    }

    boolean useHostsInPrivileges() {
        return this.useHostsInPrivileges;
    }

    /**
     * Has maxRows() been set?
     *
     * @return DOCUMENT ME!
     */
    boolean useMaxRows() {
        synchronized (this.mutex) {
            return this.maxRowsChanged;
        }
    }

    boolean useStreamLengthsInPrepStmts() {
        return this.useStreamLengthsInPrepStmts;
    }

    /**
     * Sets state for a failed-over connection
     *
     * @throws SQLException DOCUMENT ME!
     */
    private void setFailedOverState() throws SQLException {
        if (this.failOverReadOnly) {
            setReadOnly(true);
        }

        this.queriesIssuedFailedOver = 0;
        this.failedOver = true;
        this.masterFailTimeMillis = System.currentTimeMillis();
    }

    /**
     * Builds the map needed for 4.1.0 and newer servers that maps field-level
     * charset/collation info to a java character encoding name.
     *
     * @throws SQLException DOCUMENT ME!
     */
    private void buildCollationMapping() throws SQLException {
        if (this.io.versionMeetsMinimum(4, 1, 0)) {
            com.mysql.jdbc.Statement stmt = null;
            com.mysql.jdbc.ResultSet results = null;

            TreeMap sortedCollationMap = new TreeMap();

            try {
                stmt = (com.mysql.jdbc.Statement) createStatement();

                if (stmt.getMaxRows() != 0) {
                    stmt.setMaxRows(0);
                }

                results = (com.mysql.jdbc.ResultSet) stmt.executeQuery(
                        "SHOW COLLATION");

                while (results.next()) {
                    String charsetName = results.getString(2);
                    Integer charsetIndex = new Integer(results.getInt(3));

                    sortedCollationMap.put(charsetIndex, charsetName);
                }

                // Now, merge with what we already know
                int highestIndex = ((Integer) sortedCollationMap.lastKey())
                    .intValue();

                if (CharsetMapping.INDEX_TO_CHARSET.length > highestIndex) {
                    highestIndex = CharsetMapping.INDEX_TO_CHARSET.length;
                }

                this.indexToCharsetMapping = new String[highestIndex + 1];

                for (int i = 0; i < CharsetMapping.INDEX_TO_CHARSET.length;
                        i++) {
                    this.indexToCharsetMapping[i] = CharsetMapping.INDEX_TO_CHARSET[i];
                }

                for (Iterator indexIter = sortedCollationMap.entrySet()
                                                            .iterator();
                        indexIter.hasNext();) {
                    Map.Entry indexEntry = (Map.Entry) indexIter.next();

                    String mysqlCharsetName = (String) indexEntry.getValue();

                    this.indexToCharsetMapping[((Integer) indexEntry.getKey())
                    .intValue()] = (String) CharsetMapping.MYSQL_TO_JAVA_CHARSET_MAP
                        .get(mysqlCharsetName);
                }
            } catch (java.sql.SQLException e) {
                throw e;
            } finally {
                if (results != null) {
                    try {
                        results.close();
                    } catch (java.sql.SQLException sqlE) {
                        ;
                    }
                }

                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (java.sql.SQLException sqlE) {
                        ;
                    }
                }
            }
        } else {
            // Safety, we already do this as an initializer, but this makes 
            // the intent more clear
            this.indexToCharsetMapping = CharsetMapping.INDEX_TO_CHARSET;
        }
    }

    private void checkClosed() throws SQLException {
        if (this.isClosed) {
            StringBuffer exceptionMessage = new StringBuffer();

            exceptionMessage.append(
                "No operations allowed after connection closed.");

            if (!this.paranoid) {
                if (this.forcedCloseReason != null) {
                    exceptionMessage.append(
                        "\n\nConnection was closed due to the following exception:");
                    exceptionMessage.append(Util.stackTraceToString(
                            this.forcedCloseReason));
                } else if (this.explicitCloseLocation != null) {
                    exceptionMessage.append(
                        "\n\nConnection was closed explicitly by the application at the following location:");
                    exceptionMessage.append(Util.stackTraceToString(
                            this.explicitCloseLocation));
                }
            }

            throw new SQLException(exceptionMessage.toString(), "08003");
        }
    }

    /**
     * If useUnicode flag is set and explicit client character encoding isn't
     * specified then assign encoding from server if any.
     *
     * @throws SQLException DOCUMENT ME!
     */
    private void checkServerEncoding() throws SQLException {
        if (this.doUnicode && (this.encoding != null)) {
            // spec'd by client, don't map, but check
            return;
        }

        this.mysqlEncodingName = (String) this.serverVariables.get(
                "character_set");

        //if (this.mysqlEncodingName == null) {
        //    // must be 4.1.1 or newer?	
        //    this.mysqlEncodingName = (String) this.serverVariables.get(
        //            "character_set_server");
        //}
        String javaEncodingName = null;

        if (this.mysqlEncodingName != null) {
            javaEncodingName = (String) charsetMap.get(this.mysqlEncodingName
                    .toUpperCase());
        }

        //
        // First check if we can do the encoding ourselves
        //
        if (!this.doUnicode && (javaEncodingName != null)) {
            SingleByteCharsetConverter converter = getCharsetConverter(javaEncodingName);

            if (converter != null) { // we know how to convert this ourselves
                this.doUnicode = true; // force the issue
                this.encoding = javaEncodingName;

                return;
            }
        }

        //
        // Now, try and find a Java I/O converter that can do
        // the encoding for us
        //
        if (this.mysqlEncodingName != null) {
            if (javaEncodingName == null) {
                // We don't have a mapping for it, so try
                // and canonicalize the name....
                if (Character.isLowerCase(this.mysqlEncodingName.charAt(0))) {
                    char[] ach = this.mysqlEncodingName.toCharArray();
                    ach[0] = Character.toUpperCase(this.mysqlEncodingName
                            .charAt(0));
                    this.encoding = new String(ach);
                }
            }

            //
            // Attempt to use the encoding, and bail out if it
            // can't be used
            //
            try {
                "abc".getBytes(javaEncodingName);
                this.encoding = javaEncodingName;
                this.doUnicode = true;
            } catch (UnsupportedEncodingException UE) {
                throw new SQLException(
                    "The driver can not map the character encoding '"
                    + this.encoding + "' that your server is using "
                    + "to a character encoding your JVM understands. You "
                    + "can specify this mapping manually by adding \"useUnicode=true\" "
                    + "as well as \"characterEncoding=[an_encoding_your_jvm_understands]\" "
                    + "to your JDBC URL.",
                    SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE);
            }
        }
    }

    /**
     * Set transaction isolation level to the value received from server if
     * any. Is called by connectionInit(...)
     *
     * @throws SQLException DOCUMENT ME!
     */
    private void checkTransactionIsolationLevel() throws SQLException {
        String txIsolationName = null;

        if (this.io.versionMeetsMinimum(4, 0, 3)) {
            txIsolationName = "tx_isolation";
        } else {
            txIsolationName = "transaction_isolation";
        }

        String s = (String) this.serverVariables.get(txIsolationName);

        if (s != null) {
            Integer intTI = (Integer) mapTransIsolationName2Value.get(s);

            if (intTI != null) {
                isolationLevel = intTI.intValue();
            }
        }
    }

    /**
     * Destroys this connection and any underlying resources
     *
     * @param cleanupReason DOCUMENT ME!
     */
    private void cleanup(Throwable cleanupReason) {
        try {
            if ((this.io != null) && !isClosed()) {
                realClose(false, false);
            } else if (this.io != null) {
                this.io.forceClose();
            }
        } catch (SQLException sqlEx) {
            // ignore, we're going away.
        }

        this.isClosed = true;
        this.forcedCloseReason = cleanupReason;
    }

    /**
     * Sets up client character set for MySQL-4.1 and newer.  This
     * must be done before any further communication with the server!
     *
     * @return true if this routine actually configured the client character
     *         set, or false if the driver needs to use 'older' methods to
     *         detect the character set, as it is connected to a MySQL server
     *         older than 4.1.0
     *
     * @throws SQLException if an exception happens while sending 'SET NAMES'
     *         to the server, or the server sends character set  information
     *         that the client doesn't know about.
     */
    private boolean configureClientCharacterSet() throws SQLException {
        String realJavaEncoding = getEncoding();
        boolean characterSetAlreadyConfigured = false;

        try {
            if (this.io.versionMeetsMinimum(4, 1, 0)) {
            	
                characterSetAlreadyConfigured = true;

                this.doUnicode = true;
                
                configureCharsetProperties(this.props);
                
                realJavaEncoding = getEncoding(); // we need to do this again to grab this for 
                                                  // versions > 4.1.0

                try {
                    this.encoding = CharsetMapping.INDEX_TO_CHARSET[this.io.serverCharsetIndex];
                } catch (ArrayIndexOutOfBoundsException outOfBoundsEx) {
                    if (realJavaEncoding != null) {
                        // user knows best, try it
                        this.encoding = realJavaEncoding;
                    } else {
                        throw new SQLException(
                            "Unknown initial character set index '"
                            + this.io.serverCharsetIndex
                            + "' received from server. Initial client character set can be forced via the 'characterEncoding' property.",
                            SQLError.SQL_STATE_GENERAL_ERROR);
                    }
                }

                if (this.encoding == null) {
                    // punt?
                    this.encoding = "ISO8859_1";
                }

                //
                // Has the user has 'forced' the character encoding via
                // driver properties?
                //
                if (useUnicode() && (realJavaEncoding != null)) {
                    if ("ISO8859_2".equals(realJavaEncoding)
                            && (this.mysqlEncodingName == null)) {
                        throw new SQLException(
                            "Character encoding 'ISO8859_2' specified in JDBC URL which maps to multiple MySQL character encodings:"
                            + "\n\n" + "* 'latin2'\n" + "* 'czech'\n"
                            + "* 'hungarian'\n" + "* 'croat'\n"
                            + "\nSpecify one of the above encodings using the 'mysqlEncoding' connection property.",
                            SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE);
                    } else if ("ISO8859_13".equals(realJavaEncoding)
                            && (this.mysqlEncodingName == null)) {
                        throw new SQLException(
                            "Character encoding 'ISO8859_13' specified in JDBC URL which maps to multiple MySQL character encodings:"
                            + "\n\n" + "* 'latvian'\n" + "* 'latvian1'\n"
                            + "* 'estonia'\n"
                            + "\nSpecify one of the above encodings using the 'mysqlEncoding' connection property.",
                            SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE);
                    }

                    if (this.mysqlEncodingName == null) {
                        this.mysqlEncodingName = (String) CharsetMapping.JAVA_UC_TO_MYSQL_CHARSET_MAP
                            .get(realJavaEncoding.toUpperCase());
                    }

                    //
                    // Now, inform the server what character set we
                    // will be using from now-on...
                    //
                    if (realJavaEncoding.equalsIgnoreCase("UTF-8")
                            || realJavaEncoding.equalsIgnoreCase("UTF8")) {
                        // charset names are case-sensitive
                        execSQL("SET NAMES utf8", -1, this.database);

                        // Switch driver's encoding now, since the server
                        // knows what we're sending...
                        this.encoding = realJavaEncoding;
                    } else {
                        String namesEncoding = this.mysqlEncodingName;

                        if ("koi8_ru".equals(this.mysqlEncodingName)) {
                            // This has a _different_ name in 4.1...
                            namesEncoding = "ko18r";
                        }
                       
                        if (namesEncoding != null) {
                            execSQL("SET NAMES " + namesEncoding, -1,
                                this.database);
                        }

                        // Switch driver's encoding now, since the server
                        // knows what we're sending...
                        this.encoding = realJavaEncoding;
                    }
                }
                
                //
                // We know how to deal with any charset coming back from
                // the database, so tell the server not to do conversion
                // if the user hasn't 'forced' a result-set character set
                //
                
                if (this.characterSetResults == null) {
                	execSQL("SET character_set_results = NULL", -1, this.database);
                } else {
                	StringBuffer setBuf = new StringBuffer("SET character_set_results = ".length() + this.characterSetResults.length());
                	setBuf.append("SET character_set_results = ").append(this.characterSetResults);
                	
                	execSQL(setBuf.toString(), -1, this.database);
                }
            } else {
                // Use what the server has specified
                realJavaEncoding = this.encoding; // so we don't get 
                                                  // swapped out in the finally
                                                  // block....
            }

           
        } finally {
            // Failsafe, make sure that the driver's notion of character
            // encoding matches what the user has specified.
            this.encoding = realJavaEncoding;
        }

        return characterSetAlreadyConfigured;
    }
    
    /**
     * The character set we want results and result metadata
     * returned in (null == results in any charset, metadata
     * in UTF-8).
     */
    private String characterSetResults = null;
    
    /**
     * The character set we want results and result metadata
     * returned in (null == results in any charset, metadata
     * in UTF-8).
     */
    private String characterSetResultsOnServer = null;
    
    /**
     * For servers > 4.1.0, what character set is the metadata
     * returned in?
     */
    private String characterSetMetadata = null;

    /**
     * Configures the client's timezone if required.
     *
     * @throws SQLException if the timezone the server is configured to use
     *         can't be mapped to a Java timezone.
     */
    private void configureTimezone() throws SQLException {
        if (this.useTimezone && this.serverVariables.containsKey("timezone")) {
            // user can specify/override as property
            String canoncicalTimezone = this.props.getProperty("serverTimezone");

            if ((canoncicalTimezone == null)
                    || (canoncicalTimezone.length() == 0)) {
                String serverTimezoneStr = (String) this.serverVariables.get(
                        "timezone");

                try {
                    canoncicalTimezone = TimeUtil.getCanoncialTimezone(serverTimezoneStr);

                    if (canoncicalTimezone == null) {
                        throw new SQLException("Can't map timezone '"
                            + serverTimezoneStr + "' to "
                            + " canonical timezone.",
                            SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
                    }
                } catch (IllegalArgumentException iae) {
                    throw new SQLException(iae.getMessage(),
                        SQLError.SQL_STATE_GENERAL_ERROR);
                }
            }

            serverTimezone = TimeZone.getTimeZone(canoncicalTimezone);

            //
            // The Calendar class has the behavior of mapping
            // unknown timezones to 'GMT' instead of throwing an 
            // exception, so we must check for this...
            //
            if (!canoncicalTimezone.equalsIgnoreCase("GMT")
                    && serverTimezone.getID().equals("GMT")) {
                throw new SQLException("No timezone mapping entry for '"
                    + canoncicalTimezone + "'",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        }
    }

    private void detectFloatingPointStyle() throws SQLException {
        java.sql.Statement stmt = null;
        java.sql.ResultSet rs = null;

        try {
            stmt = createStatement();

            if (stmt.getMaxRows() != 0) {
                stmt.setMaxRows(0);
            }

            rs = stmt.executeQuery(
                    "select round('inf'), round('-inf'), round('nan')");

            if (rs.next()) {
                String posInf = rs.getString(1);

                if ("inf".equalsIgnoreCase(posInf)) {
                    this.positiveInfinityRep = "'inf'";
                    this.positiveInfinityRepIsClipped = false;
                }

                String negInf = rs.getString(2);

                if ("-inf".equalsIgnoreCase(negInf)) {
                    this.negativeInfinityRep = "'-inf'";
                    this.negativeInfinityRepIsClipped = false;
                }

                String nan = rs.getString(3);

                if ("nan".equalsIgnoreCase(nan)) {
                    this.notANumberRep = "'nan'";
                    this.notANumberRepIsClipped = false;
                }
            }

            rs.close();
            rs = null;

            stmt.close();
            stmt = null;
        } catch (SQLException sqlEx) {
            ; // ignore here, we default to lowest-common denominator
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) {
                    ; // ignore
                }

                rs = null;
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) {
                    ; // ignore
                }

                stmt = null;
            }
        }
    }

    /**
     * Initializes driver properties that come from URL or properties passed to
     * the driver manager.
     *
     * @param info DOCUMENT ME!
     *
     * @throws SQLException DOCUMENT ME!
     */
    private void initializeDriverProperties(Properties info)
        throws SQLException {
        this.socketFactoryClassName = info.getProperty("socketFactory",
                DEFAULT_SOCKET_FACTORY);

        this.failOverReadOnly = "TRUE".equalsIgnoreCase(info.getProperty(
                    "failOverReadOnly"));

        this.useUnbufferedInput = "TRUE".equalsIgnoreCase(info.getProperty(
                    "useUnbufferedInput"));

        if (info.getProperty("cachePrepStmts") != null) {
            this.cachePreparedStatements = info.getProperty("cachePrepStmts")
                                               .equalsIgnoreCase("TRUE");

            if (this.cachePreparedStatements) {
                if (info.getProperty("prepStmtCacheSize") != null) {
                    try {
                        this.preparedStatementCacheSize = Integer.parseInt(info
                                .getProperty("prepStmtCacheSize"));

                        if (this.preparedStatementCacheSize < 0) {
                            throw new SQLException("Connection property 'prepStmtCacheSize' must be a non-negative integer value.",
                                SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
                        }
                    } catch (NumberFormatException nfe) {
                        throw new SQLException("Connection property 'prepStmtCacheSize' must be a non-negative integer value.",
                            SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
                    }
                }

                if (info.getProperty("prepStmtCacheSqlLimit") != null) {
                    try {
                        this.preparedStatementCacheMaxSqlSize = Integer
                            .parseInt(info.getProperty("prepStmtCacheSqlLimit"));

                        if (this.preparedStatementCacheMaxSqlSize < 0) {
                            throw new SQLException("Connection property 'prepStmtCacheSqlLimit' must be a non-negative integer value.",
                                SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
                        }
                    } catch (NumberFormatException nfe) {
                        throw new SQLException("Connection property 'prepStmtCacheSqlLimit' must be a non-negative integer value.",
                            SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
                    }
                }

                this.cachedPreparedStatementParams = new HashMap(this.preparedStatementCacheSize);
            }
        }

        if (info.getProperty("alwaysClearStream") != null) {
            this.alwaysClearStream = info.getProperty("alwaysClearStream")
                                         .equalsIgnoreCase("TRUE");
        }

        if (info.getProperty("reconnectAtTxEnd") != null) {
            this.reconnectAtTxEnd = info.getProperty("reconnectAtTxEnd")
                                        .equalsIgnoreCase("TRUE");
        }

        if (info.getProperty("clobberStreamingResults") != null) {
            this.clobberStreamingResults = info.getProperty(
                    "clobberStreamingResults").equalsIgnoreCase("TRUE");
        }

        if (info.getProperty("strictUpdates") != null) {
            this.strictUpdates = info.getProperty("strictUpdates")
                                     .equalsIgnoreCase("TRUE");
        }

        if (info.getProperty("ignoreNonTxTables") != null) {
            this.ignoreNonTxTables = info.getProperty("ignoreNonTxTables")
                                         .equalsIgnoreCase("TRUE");
        }

        if (info.getProperty("secondsBeforeRetryMaster") != null) {
            String secondsBeforeRetryStr = info.getProperty(
                    "secondsBeforeRetryMaster");

            try {
                int seconds = Integer.parseInt(secondsBeforeRetryStr);

                if (seconds < 1) {
                    throw new SQLException("Illegal (< 1)  value '"
                        + secondsBeforeRetryStr
                        + "' for 'secondsBeforeRetryMaster'",
                        SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
                }

                this.secondsBeforeRetryMaster = seconds;
            } catch (NumberFormatException nfe) {
                throw new SQLException("Illegal non-numeric value '"
                    + secondsBeforeRetryStr
                    + "' for 'secondsBeforeRetryMaster'",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        }

        if (info.getProperty("queriesBeforeRetryMaster") != null) {
            String queriesBeforeRetryStr = info.getProperty(
                    "queriesBeforeRetryMaster");

            try {
                this.queriesBeforeRetryMaster = Integer.parseInt(queriesBeforeRetryStr);
            } catch (NumberFormatException nfe) {
                throw new SQLException("Illegal non-numeric value '"
                    + queriesBeforeRetryStr
                    + "' for 'queriesBeforeRetryMaster'",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        }

        if (info.getProperty("allowLoadLocalInfile") != null) {
            this.allowLoadLocalInfile = info.getProperty("allowLoadLocalInfile")
                                            .equalsIgnoreCase("TRUE");
        }

        if (info.getProperty("continueBatchOnError") != null) {
            this.continueBatchOnError = info.getProperty("continueBatchOnError")
                                            .equalsIgnoreCase("TRUE");
        }

        if (info.getProperty("pedantic") != null) {
            this.pedantic = info.getProperty("pedantic").equalsIgnoreCase("TRUE");
        }

        if (info.getProperty("useStreamLengthsInPrepStmts") != null) {
            this.useStreamLengthsInPrepStmts = info.getProperty(
                    "useStreamLengthsInPrepStmts").equalsIgnoreCase("TRUE");
        }

        if (info.getProperty("useTimezone") != null) {
            this.useTimezone = info.getProperty("useTimezone").equalsIgnoreCase("TRUE");
        }

        if (info.getProperty("relaxAutoCommit") != null) {
            this.relaxAutoCommit = info.getProperty("relaxAutoCommit")
                                       .equalsIgnoreCase("TRUE");
        } else if (info.getProperty("relaxAutocommit") != null) {
            this.relaxAutoCommit = info.getProperty("relaxAutocommit")
                                       .equalsIgnoreCase("TRUE");
        }

        if (info.getProperty("paranoid") != null) {
            this.paranoid = info.getProperty("paranoid").equalsIgnoreCase("TRUE");
        }

        if (info.getProperty("autoReconnect") != null) {
            this.highAvailability = info.getProperty("autoReconnect")
                                        .equalsIgnoreCase("TRUE");
        }

        if (info.getProperty("capitalizeTypeNames") != null) {
            this.capitalizeDBMDTypes = info.getProperty("capitalizeTypeNames")
                                           .equalsIgnoreCase("TRUE");
        }

        if (info.getProperty("ultraDevHack") != null) {
            this.useUltraDevWorkAround = info.getProperty("ultraDevHack")
                                             .equalsIgnoreCase("TRUE");
        }

        if (info.getProperty("strictFloatingPoint") != null) {
            this.strictFloatingPoint = info.getProperty("strictFloatingPoint")
                                           .equalsIgnoreCase("TRUE");
        }

        if (info.getProperty("useSSL") != null) {
            this.useSSL = info.getProperty("useSSL").equalsIgnoreCase("TRUE");
        }

        if (info.getProperty("useCompression") != null) {
            this.useCompression = info.getProperty("useCompression")
                                      .equalsIgnoreCase("TRUE");
        }

        if (info.getProperty("socketTimeout") != null) {
            try {
                int n = Integer.parseInt(info.getProperty("socketTimeout"));

                if (n < 0) {
                    throw new SQLException("socketTimeout can not " + "be < 0",
                        SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE);
                }

                this.socketTimeout = n;
            } catch (NumberFormatException NFE) {
                throw new SQLException("Illegal parameter '"
                    + info.getProperty("socketTimeout") + "' for socketTimeout",
                    SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE);
            }
        }

        if (this.highAvailability) {
            if (info.getProperty("maxReconnects") != null) {
                try {
                    int n = Integer.parseInt(info.getProperty("maxReconnects"));
                    this.maxReconnects = n;
                } catch (NumberFormatException NFE) {
                    throw new SQLException("Illegal parameter '"
                        + info.getProperty("maxReconnects")
                        + "' for maxReconnects",
                        SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE);
                }
            }

            if (info.getProperty("initialTimeout") != null) {
                try {
                    double n = Integer.parseInt(info.getProperty(
                                "initialTimeout"));
                    this.initialTimeout = n;
                } catch (NumberFormatException NFE) {
                    throw new SQLException("Illegal parameter '"
                        + info.getProperty("initialTimeout")
                        + "' for initialTimeout",
                        SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE);
                }
            }
        }

        if (info.getProperty("maxRows") != null) {
            try {
                int n = Integer.parseInt(info.getProperty("maxRows"));

                if (n == 0) {
                    n = -1;
                }

                // adjust so that it will become MysqlDefs.MAX_ROWS
                // in execSQL()
                this.maxRows = n;
                this.maxRowsChanged = true;
            } catch (NumberFormatException NFE) {
                throw new SQLException("Illegal parameter '"
                    + info.getProperty("maxRows") + "' for maxRows",
                    SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE);
            }
        }

        if (info.getProperty("useHostsInPrivileges") != null) {
            this.useHostsInPrivileges = info.getProperty("useHostsInPrivileges")
                                            .equalsIgnoreCase("TRUE");
        }

        if (info.getProperty("interactiveClient") != null) {
            this.isInteractiveClient = info.getProperty("interactiveClient")
                                           .equalsIgnoreCase("TRUE");
        }

        if (info.getProperty("useUnicode") != null) {
            this.doUnicode = info.getProperty("useUnicode").equalsIgnoreCase("TRUE");
        }
        
       
        if (this.doUnicode) {
            configureCharsetProperties(info);
        }
    }

    /**
     * Configures client-side properties for character set information.
     * 
	 * @param info the properties passed during connection
	 * 
	 * @throws SQLException if unable to configure the specified character set.
	 */
	private void configureCharsetProperties(Properties info) throws SQLException {
		if (info.getProperty("mysqlEncoding") != null) {
		    this.mysqlEncodingName = info.getProperty("mysqlEncoding");
		}

		if (info.getProperty("characterEncoding") != null) {
		    this.encoding = info.getProperty("characterEncoding");

		    // Attempt to use the encoding, and bail out if it
		    // can't be used
		    try {
		        String testString = "abc";
		        testString.getBytes(this.encoding);
		    } catch (UnsupportedEncodingException UE) {
		    	// Try the MySQL character encoding, then....
		    	String oldEncoding = this.encoding;
		    	
		    	this.encoding = (String)CharsetMapping.MYSQL_TO_JAVA_CHARSET_MAP.get(oldEncoding);
		    	
		    	if (this.encoding == null) {
		    		throw new SQLException("Java does not support the MySQL character encoding " +
		    				" "
			    			+ "encoding '" + oldEncoding + "'.",
		            	SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE);
		    	}
		    	
		    	try {
			        String testString = "abc";
			        testString.getBytes(this.encoding);
			    } catch (UnsupportedEncodingException encodingEx) {
			    	throw new SQLException("Unsupported character "
			    			+ "encoding '" + this.encoding + "'.",
		            	SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE);
			    }
		    }
		}
	}

	/**
     * Sets varying properties that depend on server information. Called once
     * we have connected to the server.
     *
     * @param info DOCUMENT ME!
     *
     * @throws SQLException DOCUMENT ME!
     */
    private void initializePropsFromServer(Properties info)
        throws SQLException {
        // We need to do this before any further data gets
        // sent to the server....
        boolean clientCharsetIsConfigured = configureClientCharacterSet();

        this.useFastPing = this.io.versionMeetsMinimum(3, 22, 1);

        this.parserKnowsUnicode = this.io.versionMeetsMinimum(4, 1, 0);

        detectFloatingPointStyle();

        this.serverVariables.clear();

        //
        // If version is greater than 3.21.22 get the server
        // variables, and do configurations based on them...
        //
        if (this.io.versionMeetsMinimum(3, 21, 22)) {
            loadServerVariables();

            buildCollationMapping();

            LicenseConfiguration.checkLicenseType(this.serverVariables);

            String lowerCaseTables = (String) serverVariables.get(
                    "lower_case_table_names");

            this.lowerCaseTableNames = "on".equalsIgnoreCase(lowerCaseTables)
                || "1".equalsIgnoreCase(lowerCaseTables)
                || "2".equalsIgnoreCase(lowerCaseTables);

            configureTimezone();

            if (this.serverVariables.containsKey("max_allowed_packet")) {
                this.maxAllowedPacket = Integer.parseInt((String) this.serverVariables
                        .get("max_allowed_packet"));
            }

            if (this.serverVariables.containsKey("net_buffer_length")) {
                this.netBufferLength = Integer.parseInt((String) this.serverVariables
                        .get("net_buffer_length"));
            }

            checkTransactionIsolationLevel();

            //
            // We only do this for servers older than 4.1.0, because
            // 4.1.0 and newer actually send the server charset
            // during the handshake, and that's handled at the
            // top of this method...
            //
            if (!clientCharsetIsConfigured) {
                checkServerEncoding();
            }

            this.io.checkForCharsetMismatch();

            if (this.serverVariables.containsKey("sql_mode")) {
                int sqlMode = 0;

                try {
                    sqlMode = Integer.parseInt((String) this.serverVariables
                            .get("sql_mode"));
                } catch (NumberFormatException nfe) {
                    sqlMode = 0;
                }

                if ((sqlMode & 4) > 0) {
                    this.useAnsiQuotes = true;
                } else {
                    this.useAnsiQuotes = false;
                }
            }
        }

        if (this.io.versionMeetsMinimum(3, 23, 15)) {
            this.transactionsSupported = true;
            setAutoCommit(true); // to override anything
                                 // the server is set to...reqd
                                 // by JDBC spec.
        } else {
            this.transactionsSupported = false;
        }

        if (this.io.versionMeetsMinimum(3, 23, 36)) {
            this.hasIsolationLevels = true;
        } else {
            this.hasIsolationLevels = false;
        }

        // Start logging perf/profile data if the user has requested it.
        String profileSql = info.getProperty("profileSql");

        if ((profileSql != null) && profileSql.trim().equalsIgnoreCase("true")) {
            this.io.setProfileSql(true);
        } else {
            this.io.setProfileSql(false);
        }

        this.hasQuotedIdentifiers = this.io.versionMeetsMinimum(3, 23, 6);

        // Set to what we've read from the server....
        this.io.resetMaxBuf();
        
        //
        // If we're using MySQL 4.1.0 or newer, we need to figure
        // out what character set metadata will be returned in,
        // and then map that to a Java encoding name.
        //
        if (this.io.versionMeetsMinimum(4, 1, 0)) {
        	String characterSetResultsOnServerMysql = (String)this.serverVariables.get("character_set_results");
        	
        	if (characterSetResultsOnServerMysql == null || StringUtils.startsWithIgnoreCaseAndWs(characterSetResultsOnServerMysql, "NULL")) {
        		String defaultMetadataCharsetMysql = (String)this.serverVariables.get("character_set_system");
        		String defaultMetadataCharset = null;
				
        		if (defaultMetadataCharsetMysql != null) {
        			defaultMetadataCharset = (String)CharsetMapping.MYSQL_TO_JAVA_CHARSET_MAP.get(defaultMetadataCharsetMysql);
        		} else {
        			defaultMetadataCharset = "UTF-8";
        		}
        		
        		this.characterSetMetadata = defaultMetadataCharset;
        	} else {
        		this.characterSetResultsOnServer = (String)CharsetMapping.MYSQL_TO_JAVA_CHARSET_MAP.get(characterSetResultsOnServerMysql);
        		this.characterSetMetadata = this.characterSetResultsOnServer;
        	}
        }
    }

    /**
     * Loads the mapping between MySQL character sets and Java character sets
     */
    private static void loadCharacterSetMapping() {
        multibyteCharsetsMap = new HashMap();

        Iterator multibyteCharsets = CharsetMapping.MULTIBYTE_CHARSETS.keySet()
                                                                      .iterator();

        while (multibyteCharsets.hasNext()) {
            String charset = ((String) multibyteCharsets.next()).toUpperCase();
            multibyteCharsetsMap.put(charset, charset);
        }

        //
        // Now change all server encodings to upper-case to "future-proof"
        // this mapping
        //
        Iterator keys = CharsetMapping.MYSQL_TO_JAVA_CHARSET_MAP.keySet()
                                                                .iterator();
        charsetMap = new HashMap();

        while (keys.hasNext()) {
            String mysqlCharsetName = ((String) keys.next()).trim();
            	String javaCharsetName = CharsetMapping.MYSQL_TO_JAVA_CHARSET_MAP.get(mysqlCharsetName)
                                                                             .toString()
                                                                             .trim();
            	charsetMap.put(mysqlCharsetName.toUpperCase(), javaCharsetName);
            	charsetMap.put(mysqlCharsetName, javaCharsetName);
        }
    }

    private boolean getUseUltraDevWorkAround() {
        return useUltraDevWorkAround;
    }

    /**
     * Loads the result of 'SHOW VARIABLES' into the serverVariables field so
     * that the driver can configure itself.
     *
     * @throws SQLException if the 'SHOW VARIABLES' query fails for any reason.
     */
    private void loadServerVariables() throws SQLException {
        com.mysql.jdbc.Statement stmt = null;
        com.mysql.jdbc.ResultSet results = null;

        try {
            stmt = (com.mysql.jdbc.Statement) createStatement();

            if (stmt.getMaxRows() != 0) {
                stmt.setMaxRows(0);
            }

            results = (com.mysql.jdbc.ResultSet) stmt.executeQuery(
                    "SHOW VARIABLES");

            while (results.next()) {
                this.serverVariables.put(results.getString(1),
                    results.getString(2));
            }
        } catch (java.sql.SQLException e) {
            throw e;
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (java.sql.SQLException sqlE) {
                    ;
                }
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (java.sql.SQLException sqlE) {
                    ;
                }
            }
        }
    }

    // *********************************************************************
    //
    //                END OF PUBLIC INTERFACE
    //
    // *********************************************************************

    /**
     * Detect if the connection is still good
     *
     * @throws Exception DOCUMENT ME!
     */
    private void ping() throws Exception {
        if (this.useFastPing) {
            this.io.sendCommand(MysqlDefs.PING, null, null);
        } else {
            this.io.sqlQuery(PING_COMMAND, MysqlDefs.MAX_ROWS, this.encoding,
                this, java.sql.ResultSet.CONCUR_READ_ONLY, false, this.database);
        }
    }

    private void pingAndReconnect(boolean ignoreAutoCommitSetting)
        throws SQLException {
        boolean localAutoCommit = this.autoCommit;

        // We use this to catch the 'edge' case
        // of autoReconnect going from true->false
        //
        if (ignoreAutoCommitSetting) {
            localAutoCommit = true;
        }

        if (this.failedOver && localAutoCommit) {
            this.queriesIssuedFailedOver++;

            if (shouldFallBack()) {
                createNewIO(true);

                String connectedHost = this.io.getHost();

                if ((connectedHost != null)
                        && this.hostList.get(0).equals(connectedHost)) {
                    this.failedOver = false;
                    this.queriesIssuedFailedOver = 0;
                    setReadOnly(false);
                }
            }
        }

        if ((this.highAvailability || this.failedOver) && localAutoCommit) {
            try {
                ping();
            } catch (Exception Ex) {
                createNewIO(true);
            }
        }

        this.needsPing = false;
    }

    private void rollbackNoChecks() throws SQLException {
        execSQL("rollback", -1, null);
    }

    /**
     * Should we try to connect back to the master? We try when we've been
     * failed over >= this.secondsBeforeRetryMaster _or_ we've issued >
     * this.queriesIssuedFailedOver
     *
     * @return DOCUMENT ME!
     */
    private boolean shouldFallBack() {
        long secondsSinceFailedOver = (System.currentTimeMillis()
            - this.masterFailTimeMillis) / 1000;

        return ((secondsSinceFailedOver >= this.secondsBeforeRetryMaster)
        || ((this.queriesIssuedFailedOver % this.queriesBeforeRetryMaster) == 0));
    }

    /**
     * Wrapper class for UltraDev CallableStatements that are really
     * PreparedStatments. Nice going, UltraDev developers.
     */
    class UltraDevWorkAround implements java.sql.CallableStatement {
        private java.sql.PreparedStatement delegate = null;

        UltraDevWorkAround(java.sql.PreparedStatement pstmt) {
            delegate = pstmt;
        }

        public void setArray(int p1, final java.sql.Array p2)
            throws java.sql.SQLException {
            delegate.setArray(p1, p2);
        }

        public java.sql.Array getArray(int p1) throws java.sql.SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getArray(String)
         */
        public java.sql.Array getArray(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        public void setAsciiStream(int p1, final java.io.InputStream p2, int p3)
            throws java.sql.SQLException {
            delegate.setAsciiStream(p1, p2, p3);
        }

        /**
         * @see CallableStatement#setAsciiStream(String, InputStream, int)
         */
        public void setAsciiStream(String arg0, InputStream arg1, int arg2)
            throws SQLException {
            throw new NotImplemented();
        }

        public void setBigDecimal(int p1, final java.math.BigDecimal p2)
            throws java.sql.SQLException {
            delegate.setBigDecimal(p1, p2);
        }

        /**
         * @see CallableStatement#setBigDecimal(String, BigDecimal)
         */
        public void setBigDecimal(String arg0, BigDecimal arg1)
            throws SQLException {
            throw new NotImplemented();
        }

        public java.math.BigDecimal getBigDecimal(int p1)
            throws java.sql.SQLException {
            throw new SQLException("Not supported");
        }

        public java.math.BigDecimal getBigDecimal(int p1, int p2)
            throws java.sql.SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getBigDecimal(String)
         */
        public BigDecimal getBigDecimal(String arg0) throws SQLException {
            return null;
        }

        public void setBinaryStream(int p1, final java.io.InputStream p2, int p3)
            throws java.sql.SQLException {
            delegate.setBinaryStream(p1, p2, p3);
        }

        /**
         * @see CallableStatement#setBinaryStream(String, InputStream, int)
         */
        public void setBinaryStream(String arg0, InputStream arg1, int arg2)
            throws SQLException {
            throw new NotImplemented();
        }

        public void setBlob(int p1, final java.sql.Blob p2)
            throws java.sql.SQLException {
            delegate.setBlob(p1, p2);
        }

        public java.sql.Blob getBlob(int p1) throws java.sql.SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getBlob(String)
         */
        public java.sql.Blob getBlob(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        public void setBoolean(int p1, boolean p2) throws java.sql.SQLException {
            delegate.setBoolean(p1, p2);
        }

        /**
         * @see CallableStatement#setBoolean(String, boolean)
         */
        public void setBoolean(String arg0, boolean arg1)
            throws SQLException {
            throw new NotImplemented();
        }

        public boolean getBoolean(int p1) throws java.sql.SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getBoolean(String)
         */
        public boolean getBoolean(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        public void setByte(int p1, byte p2) throws java.sql.SQLException {
            delegate.setByte(p1, p2);
        }

        /**
         * @see CallableStatement#setByte(String, byte)
         */
        public void setByte(String arg0, byte arg1) throws SQLException {
            throw new NotImplemented();
        }

        public byte getByte(int p1) throws java.sql.SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getByte(String)
         */
        public byte getByte(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        public void setBytes(int p1, byte[] p2) throws java.sql.SQLException {
            delegate.setBytes(p1, p2);
        }

        /**
         * @see CallableStatement#setBytes(String, byte[])
         */
        public void setBytes(String arg0, byte[] arg1)
            throws SQLException {
            throw new NotImplemented();
        }

        public byte[] getBytes(int p1) throws java.sql.SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getBytes(String)
         */
        public byte[] getBytes(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        public void setCharacterStream(int p1, final java.io.Reader p2, int p3)
            throws java.sql.SQLException {
            delegate.setCharacterStream(p1, p2, p3);
        }

        /**
         * @see CallableStatement#setCharacterStream(String, Reader, int)
         */
        public void setCharacterStream(String arg0, Reader arg1, int arg2)
            throws SQLException {
            throw new NotImplemented();
        }

        public void setClob(int p1, final java.sql.Clob p2)
            throws java.sql.SQLException {
            delegate.setClob(p1, p2);
        }

        public java.sql.Clob getClob(int p1) throws java.sql.SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getClob(String)
         */
        public Clob getClob(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        public java.sql.Connection getConnection() throws java.sql.SQLException {
            return delegate.getConnection();
        }

        public void setCursorName(java.lang.String p1)
            throws java.sql.SQLException {
            throw new SQLException("Not supported");
        }

        public void setDate(int p1, final java.sql.Date p2)
            throws java.sql.SQLException {
            delegate.setDate(p1, p2);
        }

        public void setDate(int p1, final java.sql.Date p2,
            final java.util.Calendar p3) throws java.sql.SQLException {
            delegate.setDate(p1, p2, p3);
        }

        /**
         * @see CallableStatement#setDate(String, Date, Calendar)
         */
        public void setDate(String arg0, Date arg1, Calendar arg2)
            throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @see CallableStatement#setDate(String, Date)
         */
        public void setDate(String arg0, Date arg1) throws SQLException {
            throw new NotImplemented();
        }

        public java.sql.Date getDate(int p1) throws java.sql.SQLException {
            throw new SQLException("Not supported");
        }

        public java.sql.Date getDate(int p1, final java.util.Calendar p2)
            throws java.sql.SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getDate(String, Calendar)
         */
        public Date getDate(String arg0, Calendar arg1)
            throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @see CallableStatement#getDate(String)
         */
        public Date getDate(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        public void setDouble(int p1, double p2) throws java.sql.SQLException {
            delegate.setDouble(p1, p2);
        }

        /**
         * @see CallableStatement#setDouble(String, double)
         */
        public void setDouble(String arg0, double arg1)
            throws SQLException {
            throw new NotImplemented();
        }

        public double getDouble(int p1) throws java.sql.SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getDouble(String)
         */
        public double getDouble(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        public void setEscapeProcessing(boolean p1)
            throws java.sql.SQLException {
            delegate.setEscapeProcessing(p1);
        }

        public void setFetchDirection(int p1) throws java.sql.SQLException {
            delegate.setFetchDirection(p1);
        }

        public int getFetchDirection() throws java.sql.SQLException {
            return delegate.getFetchDirection();
        }

        public void setFetchSize(int p1) throws java.sql.SQLException {
            delegate.setFetchSize(p1);
        }

        public int getFetchSize() throws java.sql.SQLException {
            return delegate.getFetchSize();
        }

        public void setFloat(int p1, float p2) throws java.sql.SQLException {
            delegate.setFloat(p1, p2);
        }

        /**
         * @see CallableStatement#setFloat(String, float)
         */
        public void setFloat(String arg0, float arg1) throws SQLException {
            throw new NotImplemented();
        }

        public float getFloat(int p1) throws java.sql.SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getFloat(String)
         */
        public float getFloat(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @see Statement#getGeneratedKeys()
         */
        public java.sql.ResultSet getGeneratedKeys() throws SQLException {
            return delegate.getGeneratedKeys();
        }

        public void setInt(int p1, int p2) throws java.sql.SQLException {
            delegate.setInt(p1, p2);
        }

        /**
         * @see CallableStatement#setInt(String, int)
         */
        public void setInt(String arg0, int arg1) throws SQLException {
            throw new NotImplemented();
        }

        public int getInt(int p1) throws java.sql.SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getInt(String)
         */
        public int getInt(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        public void setLong(int p1, long p2) throws java.sql.SQLException {
            delegate.setLong(p1, p2);
        }

        /**
         * @see CallableStatement#setLong(String, long)
         */
        public void setLong(String arg0, long arg1) throws SQLException {
            throw new NotImplemented();
        }

        public long getLong(int p1) throws java.sql.SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getLong(String)
         */
        public long getLong(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        public void setMaxFieldSize(int p1) throws java.sql.SQLException {
            delegate.setMaxFieldSize(p1);
        }

        public int getMaxFieldSize() throws java.sql.SQLException {
            return delegate.getMaxFieldSize();
        }

        public void setMaxRows(int p1) throws java.sql.SQLException {
            delegate.setMaxRows(p1);
        }

        public int getMaxRows() throws java.sql.SQLException {
            return delegate.getMaxRows();
        }

        public java.sql.ResultSetMetaData getMetaData()
            throws java.sql.SQLException {
            throw new SQLException("Not supported");
        }

        public boolean getMoreResults() throws java.sql.SQLException {
            return delegate.getMoreResults();
        }

        /**
         * @see Statement#getMoreResults(int)
         */
        public boolean getMoreResults(int arg0) throws SQLException {
            return delegate.getMoreResults();
        }

        public void setNull(int p1, int p2) throws java.sql.SQLException {
            delegate.setNull(p1, p2);
        }

        public void setNull(int p1, int p2, java.lang.String p3)
            throws java.sql.SQLException {
            delegate.setNull(p1, p2, p3);
        }

        /**
         * @see CallableStatement#setNull(String, int, String)
         */
        public void setNull(String arg0, int arg1, String arg2)
            throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @see CallableStatement#setNull(String, int)
         */
        public void setNull(String arg0, int arg1) throws SQLException {
            throw new NotImplemented();
        }

        public void setObject(int p1, final java.lang.Object p2)
            throws java.sql.SQLException {
            delegate.setObject(p1, p2);
        }

        public void setObject(int p1, final java.lang.Object p2, int p3)
            throws java.sql.SQLException {
            delegate.setObject(p1, p2, p3);
        }

        public void setObject(int p1, final java.lang.Object p2, int p3, int p4)
            throws java.sql.SQLException {
            delegate.setObject(p1, p2, p3, p4);
        }

        /**
         * @see CallableStatement#setObject(String, Object, int, int)
         */
        public void setObject(String arg0, Object arg1, int arg2, int arg3)
            throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @see CallableStatement#setObject(String, Object, int)
         */
        public void setObject(String arg0, Object arg1, int arg2)
            throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @see CallableStatement#setObject(String, Object)
         */
        public void setObject(String arg0, Object arg1)
            throws SQLException {
            throw new NotImplemented();
        }

        public java.lang.Object getObject(int p1) throws java.sql.SQLException {
            throw new SQLException("Not supported");
        }

        public java.lang.Object getObject(int p1, final java.util.Map p2)
            throws java.sql.SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getObject(String, Map)
         */
        public Object getObject(String arg0, Map arg1)
            throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @see CallableStatement#getObject(String)
         */
        public Object getObject(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @see PreparedStatement#getParameterMetaData()
         */
        public ParameterMetaData getParameterMetaData()
            throws SQLException {
            return delegate.getParameterMetaData();
        }

        public void setQueryTimeout(int p1) throws java.sql.SQLException {
            throw new SQLException("Not supported");
        }

        public int getQueryTimeout() throws java.sql.SQLException {
            return delegate.getQueryTimeout();
        }

        public void setRef(int p1, final java.sql.Ref p2)
            throws java.sql.SQLException {
            throw new SQLException("Not supported");
        }

        public java.sql.Ref getRef(int p1) throws java.sql.SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getRef(String)
         */
        public Ref getRef(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        public java.sql.ResultSet getResultSet() throws java.sql.SQLException {
            return delegate.getResultSet();
        }

        public int getResultSetConcurrency() throws java.sql.SQLException {
            return delegate.getResultSetConcurrency();
        }

        /**
         * @see Statement#getResultSetHoldability()
         */
        public int getResultSetHoldability() throws SQLException {
            return delegate.getResultSetHoldability();
        }

        public int getResultSetType() throws java.sql.SQLException {
            return delegate.getResultSetType();
        }

        public void setShort(int p1, short p2) throws java.sql.SQLException {
            delegate.setShort(p1, p2);
        }

        /**
         * @see CallableStatement#setShort(String, short)
         */
        public void setShort(String arg0, short arg1) throws SQLException {
            throw new NotImplemented();
        }

        public short getShort(int p1) throws java.sql.SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getShort(String)
         */
        public short getShort(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        public void setString(int p1, java.lang.String p2)
            throws java.sql.SQLException {
            delegate.setString(p1, p2);
        }

        /**
         * @see CallableStatement#setString(String, String)
         */
        public void setString(String arg0, String arg1)
            throws SQLException {
            throw new NotImplemented();
        }

        public java.lang.String getString(int p1) throws java.sql.SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getString(String)
         */
        public String getString(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        public void setTime(int p1, final java.sql.Time p2)
            throws java.sql.SQLException {
            delegate.setTime(p1, p2);
        }

        public void setTime(int p1, final java.sql.Time p2,
            final java.util.Calendar p3) throws java.sql.SQLException {
            delegate.setTime(p1, p2, p3);
        }

        /**
         * @see CallableStatement#setTime(String, Time, Calendar)
         */
        public void setTime(String arg0, Time arg1, Calendar arg2)
            throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @see CallableStatement#setTime(String, Time)
         */
        public void setTime(String arg0, Time arg1) throws SQLException {
            throw new NotImplemented();
        }

        public java.sql.Time getTime(int p1) throws java.sql.SQLException {
            throw new SQLException("Not supported");
        }

        public java.sql.Time getTime(int p1, final java.util.Calendar p2)
            throws java.sql.SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getTime(String, Calendar)
         */
        public Time getTime(String arg0, Calendar arg1)
            throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @see CallableStatement#getTime(String)
         */
        public Time getTime(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        public void setTimestamp(int p1, final java.sql.Timestamp p2)
            throws java.sql.SQLException {
            delegate.setTimestamp(p1, p2);
        }

        public void setTimestamp(int p1, final java.sql.Timestamp p2,
            final java.util.Calendar p3) throws java.sql.SQLException {
            delegate.setTimestamp(p1, p2, p3);
        }

        /**
         * @see CallableStatement#setTimestamp(String, Timestamp, Calendar)
         */
        public void setTimestamp(String arg0, Timestamp arg1, Calendar arg2)
            throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @see CallableStatement#setTimestamp(String, Timestamp)
         */
        public void setTimestamp(String arg0, Timestamp arg1)
            throws SQLException {
            throw new NotImplemented();
        }

        public java.sql.Timestamp getTimestamp(int p1)
            throws java.sql.SQLException {
            throw new SQLException("Not supported");
        }

        public java.sql.Timestamp getTimestamp(int p1,
            final java.util.Calendar p2) throws java.sql.SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getTimestamp(String, Calendar)
         */
        public Timestamp getTimestamp(String arg0, Calendar arg1)
            throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @see CallableStatement#getTimestamp(String)
         */
        public Timestamp getTimestamp(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @see CallableStatement#setURL(String, URL)
         */
        public void setURL(String arg0, URL arg1) throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @see PreparedStatement#setURL(int, URL)
         */
        public void setURL(int arg0, URL arg1) throws SQLException {
            delegate.setURL(arg0, arg1);
        }

        /**
         * @see CallableStatement#getURL(int)
         */
        public URL getURL(int arg0) throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @see CallableStatement#getURL(String)
         */
        public URL getURL(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @deprecated -- we know, but we need to override...
         */
        public void setUnicodeStream(int p1, final java.io.InputStream p2,
            int p3) throws java.sql.SQLException {
            delegate.setUnicodeStream(p1, p2, p3);
        }

        public int getUpdateCount() throws java.sql.SQLException {
            return delegate.getUpdateCount();
        }

        public java.sql.SQLWarning getWarnings() throws java.sql.SQLException {
            return delegate.getWarnings();
        }

        public void addBatch() throws java.sql.SQLException {
            delegate.addBatch();
        }

        public void addBatch(java.lang.String p1) throws java.sql.SQLException {
            delegate.addBatch(p1);
        }

        public void cancel() throws java.sql.SQLException {
            delegate.cancel();
        }

        public void clearBatch() throws java.sql.SQLException {
            delegate.clearBatch();
        }

        public void clearParameters() throws java.sql.SQLException {
            delegate.clearParameters();
        }

        public void clearWarnings() throws java.sql.SQLException {
            delegate.clearWarnings();
        }

        public void close() throws java.sql.SQLException {
            delegate.close();
        }

        public boolean execute() throws java.sql.SQLException {
            return delegate.execute();
        }

        public boolean execute(java.lang.String p1)
            throws java.sql.SQLException {
            return delegate.execute(p1);
        }

        /**
         * @see Statement#execute(String, int)
         */
        public boolean execute(String arg0, int arg1) throws SQLException {
            return delegate.execute(arg0, arg1);
        }

        /**
         * @see Statement#execute(String, int[])
         */
        public boolean execute(String arg0, int[] arg1)
            throws SQLException {
            return delegate.execute(arg0, arg1);
        }

        /**
         * @see Statement#execute(String, String[])
         */
        public boolean execute(String arg0, String[] arg1)
            throws SQLException {
            return delegate.execute(arg0, arg1);
        }

        public int[] executeBatch() throws java.sql.SQLException {
            return delegate.executeBatch();
        }

        public java.sql.ResultSet executeQuery() throws java.sql.SQLException {
            return delegate.executeQuery();
        }

        public java.sql.ResultSet executeQuery(java.lang.String p1)
            throws java.sql.SQLException {
            return delegate.executeQuery(p1);
        }

        public int executeUpdate() throws java.sql.SQLException {
            return delegate.executeUpdate();
        }

        public int executeUpdate(java.lang.String p1)
            throws java.sql.SQLException {
            return delegate.executeUpdate(p1);
        }

        /**
         * @see Statement#executeUpdate(String, int)
         */
        public int executeUpdate(String arg0, int arg1)
            throws SQLException {
            return delegate.executeUpdate(arg0, arg1);
        }

        /**
         * @see Statement#executeUpdate(String, int[])
         */
        public int executeUpdate(String arg0, int[] arg1)
            throws SQLException {
            return delegate.executeUpdate(arg0, arg1);
        }

        /**
         * @see Statement#executeUpdate(String, String[])
         */
        public int executeUpdate(String arg0, String[] arg1)
            throws SQLException {
            return delegate.executeUpdate(arg0, arg1);
        }

        public void registerOutParameter(int p1, int p2)
            throws java.sql.SQLException {
            throw new SQLException("Not supported");
        }

        public void registerOutParameter(int p1, int p2, int p3)
            throws java.sql.SQLException {
            throw new SQLException("Not supported");
        }

        public void registerOutParameter(int p1, int p2, java.lang.String p3)
            throws java.sql.SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#registerOutParameter(String, int, int)
         */
        public void registerOutParameter(String arg0, int arg1, int arg2)
            throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @see CallableStatement#registerOutParameter(String, int, String)
         */
        public void registerOutParameter(String arg0, int arg1, String arg2)
            throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @see CallableStatement#registerOutParameter(String, int)
         */
        public void registerOutParameter(String arg0, int arg1)
            throws SQLException {
            throw new NotImplemented();
        }

        public boolean wasNull() throws java.sql.SQLException {
            throw new SQLException("Not supported");
        }
    }
    
	/**
	 * @return Returns the characterSetMetadata.
	 */
	protected String getCharacterSetMetadata() {
		return characterSetMetadata;
	}
}
