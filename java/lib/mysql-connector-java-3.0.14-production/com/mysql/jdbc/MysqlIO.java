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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.SoftReference;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.ArrayList;
import java.util.Properties;
import java.util.zip.Deflater;
import java.util.zip.Inflater;


/**
 * This class is used by Connection for communicating with the MySQL server.
 *
 * @author Mark Matthews
 * @version $Id: MysqlIO.java,v 1.32.2.55 2004/05/27 17:47:57 mmatthew Exp $
 *
 * @see java.sql.Connection
 */
public class MysqlIO {
    static final int NULL_LENGTH = ~0;
    static final int COMP_HEADER_LENGTH = 3;
    static final int MIN_COMPRESS_LEN = 50;
    static final int HEADER_LENGTH = 4;
    private static int maxBufferSize = 65535;
    private static final int CLIENT_COMPRESS = 32; /* Can use compression
    protcol */
    private static final int CLIENT_CONNECT_WITH_DB = 8;
    private static final int CLIENT_FOUND_ROWS = 2;
    private static final int CLIENT_IGNORE_SPACE = 256; /* Ignore spaces
    before '(' */
    private static final int CLIENT_LOCAL_FILES = 128; /* Can use LOAD DATA
    LOCAL */

    /* Found instead of
       affected rows */
    private static final int CLIENT_LONG_FLAG = 4; /* Get all column flags */
    private static final int CLIENT_LONG_PASSWORD = 1; /* new more secure
    passwords */
    private static final int CLIENT_PROTOCOL_41 = 512; // for > 4.1.1
    private static final int CLIENT_INTERACTIVE = 1024;
    private static final int CLIENT_SSL = 2048;
    private static final int CLIENT_RESERVED = 16384; // for 4.1.0 only
    private static final int CLIENT_SECURE_CONNECTION = 32768;
    private static final String FALSE_SCRAMBLE = "xxxxxxxx";

    /**
     * We store the platform 'encoding' here, only used to avoid munging
     * filenames for LOAD DATA LOCAL INFILE...
     */
    private static String jvmPlatformCharset = null;

    static {
        OutputStreamWriter outWriter = null;

        //
        // Use the I/O system to get the encoding (if possible), to avoid
        // security restrictions on System.getProperty("file.encoding") in
        // applets (why is that restricted?)
        //
        try {
            outWriter = new OutputStreamWriter(new ByteArrayOutputStream());
            jvmPlatformCharset = outWriter.getEncoding();
        } finally {
            try {
            	if (outWriter != null) {
            		outWriter.close();
            	}
            } catch (IOException ioEx) {
                // ignore
            }
        }
    }

    //
    // Use this when reading in rows to avoid thousands of new()
    // calls, because the byte arrays just get copied out of the
    // packet anyway
    //
    private Buffer reusablePacket = null;
    private Buffer sendPacket = null;
    private Buffer sharedSendPacket = null;

    /** Data to the server */

    //private DataOutputStream     _Mysql_Output             = null;
    private BufferedOutputStream mysqlOutput = null;
    private com.mysql.jdbc.Connection connection;
    private Deflater deflater = null;
    private Inflater inflater = null;

    /** Buffered data from the server */

    //private BufferedInputStream  _Mysql_Buf_Input          = null;

    /** Buffered data to the server */

    //private BufferedOutputStream _Mysql_Buf_Output         = null;

    /** Data from the server */

    //private DataInputStream      _Mysql_Input              = null;
    private InputStream mysqlInput = null;
    private RowData streamingData = null;

    //
    // For SQL Warnings
    //
    private SQLWarning warningChain = null;

    /** The connection to the server */
    private Socket mysqlConnection = null;
    private SocketFactory socketFactory = null;

    //
    // Packet used for 'LOAD DATA LOCAL INFILE'
    //
    // We use a SoftReference, so that we don't penalize intermittent
    // use of this feature
    //
    private SoftReference loadFileBufRef;

    //
    // Used to send large packets to the server versions 4+
    // We use a SoftReference, so that we don't penalize intermittent
    // use of this feature
    //
    private SoftReference splitBufRef;
    private String host = null;
    private String seed;
    private String serverVersion = null;
    private String socketFactoryClassName = null;
    private byte[] packetHeaderBuf = new byte[4];
    private boolean clearStreamBeforeEachQuery = false;
    private boolean colDecimalNeedsBump = false; // do we need to increment the colDecimal flag?
    private boolean has41NewNewProt = false;

    /** Does the server support long column info? */
    private boolean hasLongColumnInfo = false;
    private boolean isInteractiveClient = false;

    /**
     * Does the character set of this connection match the character set of the
     * platform
     */
    private boolean platformDbCharsetMatches = true;
    private boolean profileSql = false;

    /** Should we use 4.1 protocol extensions? */
    private boolean use41Extensions = false;
    private boolean useCompression = false;
    private boolean useNewLargePackets = false;
    private boolean useNewUpdateCounts = false; // should we use the new larger update counts?
    private byte packetSequence = 0;
    private byte protocolVersion = 0;
    private int clientParam = 0;

    // changed once we've connected.
    private int maxAllowedPacket = 1024 * 1024;
    private int maxThreeBytes = 255 * 255 * 255;
    private int port = 3306;
    private int serverCapabilities;
    private int serverMajorVersion = 0;
    private int serverMinorVersion = 0;
    private int serverSubMinorVersion = 0;
	protected int serverCharsetIndex;
	private static final int MAX_QUERY_LENGTH_TO_LOG =  4 * 1024; // 4K

    /**
     * Constructor:  Connect to the MySQL server and setup a stream connection.
     *
     * @param host the hostname to connect to
     * @param port the port number that the server is listening on
     * @param socketFactoryClassName the socket factory to use
     * @param props the Properties from DriverManager.getConnection()
     * @param conn the Connection that is creating us
     * @param socketTimeout the timeout to set for the socket (0 means no
     *        timeout)
     *
     * @throws IOException if an IOException occurs during connect.
     * @throws java.sql.SQLException if a database access error occurs.
     */
    protected MysqlIO(String host, int port, String socketFactoryClassName,
        Properties props, com.mysql.jdbc.Connection conn, int socketTimeout)
        throws IOException, java.sql.SQLException {
        this.connection = conn;
        this.reusablePacket = new Buffer(this.connection.getNetBufferLength());
        this.port = port;
        this.host = host;
        this.socketFactoryClassName = socketFactoryClassName;
        this.socketFactory = createSocketFactory();
        this.mysqlConnection = socketFactory.connect(this.host, props);
        this.clearStreamBeforeEachQuery = this.connection.alwaysClearStream();

        if (socketTimeout != 0) {
            try {
                this.mysqlConnection.setSoTimeout(socketTimeout);
            } catch (Exception ex) {
                /* Ignore if the platform does not support it */
            }
        }

        this.mysqlConnection = this.socketFactory.beforeHandshake();

        if (!this.connection.isUsingUnbufferedInput()) {
            this.mysqlInput = new BufferedInputStream(this.mysqlConnection
                    .getInputStream(), 16384);
        } else {
            this.mysqlInput = this.mysqlConnection.getInputStream();
        }

        this.mysqlOutput = new BufferedOutputStream(this.mysqlConnection
                .getOutputStream(), 16384);
        this.isInteractiveClient = this.connection.isInteractiveClient();
    }

    /**
     * Should the driver generate SQL statement profiles?
     *
     * @param flag should the driver enable profiling?
     */
    protected void setProfileSql(boolean flag) {
        this.profileSql = flag;
    }

    /**
     * Build a result set. Delegates to buildResultSetWithRows() to build a
     * JDBC-version-specific ResultSet, given rows as byte data, and field
     * information.
     *
     * @param columnCount the number of columns in the result set
     * @param maxRows the maximum number of rows to read (-1 means all rows)
     * @param resultSetType the type of result set (CONCUR_UPDATABLE or
     *        READ_ONLY)
     * @param streamResults should the result set be read all at once, or
     *        streamed?
     * @param catalog the database name in use when the result set was created
     *
     * @return a result set
     *
     * @throws Exception if a database access error occurs
     */
    protected ResultSet getResultSet(long columnCount, int maxRows,
        int resultSetType, boolean streamResults, String catalog)
        throws Exception {
        Buffer packet; // The packet from the server
        Field[] fields = new Field[(int) columnCount];

        // Read in the column information
        for (int i = 0; i < columnCount; i++) {
            packet = readPacket();
            fields[i] = unpackField(packet, false);
        }

        packet = reuseAndReadPacket(this.reusablePacket);

        RowData rowData = null;

        if (!streamResults) {
            ArrayList rows = new ArrayList();

            // Now read the data
            byte[][] rowBytes = nextRow((int) columnCount);
            int rowCount = 0;

            if (rowBytes != null) {
                rows.add(rowBytes);
                rowCount = 1;
            }

            while ((rowBytes != null) && (rowCount < maxRows)) {
                rowBytes = nextRow((int) columnCount);

                if (rowBytes != null) {
                    rows.add(rowBytes);
                    rowCount++;
                } else {
                    if (Driver.TRACE) {
                        Debug.msg(this, "* NULL Row *");
                    }
                }
            }

            //
            // Clear any outstanding data left on the wire
            // when we've artifically limited the number of 
            // rows we retrieve (fix for BUG#1695)
            //
            if (rowCount <= maxRows) {
                clearInputStream();
            }

            if (Driver.TRACE) {
                Debug.msg(this,
                    "* Fetched " + rows.size() + " rows from server *");
            }

            rowData = new RowDataStatic(rows);
            reclaimLargeReusablePacket();
        } else {
            rowData = new RowDataDynamic(this, (int) columnCount);
            this.streamingData = rowData;
        }

        return buildResultSetWithRows(catalog, fields, rowData, resultSetType);
    }

    /**
     * Forcibly closes the underlying socket to MySQL.
     */
    protected final void forceClose() {
        try {
            if (this.mysqlInput != null) {
                this.mysqlInput.close();
            }
        } catch (IOException ioEx) {
            // we can't do anything constructive about this
            // Let the JVM clean it up later
            this.mysqlInput = null;
        }

        try {
            if (this.mysqlOutput != null) {
                this.mysqlOutput.close();
            }
        } catch (IOException ioEx) {
            // we can't do anything constructive about this
            // Let the JVM clean it up later
            this.mysqlOutput = null;
        }

        try {
            if (this.mysqlConnection != null) {
                this.mysqlConnection.close();
            }
        } catch (IOException ioEx) {
            // we can't do anything constructive about this
            // Let the JVM clean it up later
            this.mysqlConnection = null;
        }
    }

    /**
     * Re-authenticates as the given user and password
     *
     * @param userName DOCUMENT ME!
     * @param password DOCUMENT ME!
     *
     * @throws SQLException DOCUMENT ME!
     */
    protected void changeUser(String userName, String password, String database)
        throws SQLException {
        this.packetSequence = -1;

        int passwordLength = 16;
        int userLength = 0;

        if (userName != null) {
            userLength = userName.length();
        }

        int packLength = (userLength + passwordLength) + 7 + HEADER_LENGTH;

        if ((this.serverCapabilities & CLIENT_SECURE_CONNECTION) != 0) {
            Buffer changeUserPacket = new Buffer(packLength + 1);
            changeUserPacket.writeByte((byte) MysqlDefs.COM_CHANGE_USER);

            if (versionMeetsMinimum(4, 1, 1)) {
                secureAuth411(changeUserPacket, packLength, userName, password,
                    database, false);
            } else {
                secureAuth(changeUserPacket, packLength, userName, password,
                    database, false);
            }
        } else {
            // Passwords can be 16 chars long
            Buffer packet = new Buffer(packLength + 1);
            packet.writeByte((byte) MysqlDefs.COM_CHANGE_USER);

            // User/Password data
            packet.writeString(userName);

            if (this.protocolVersion > 9) {
                packet.writeString(Util.newCrypt(password, this.seed));
            } else {
                packet.writeString(Util.oldCrypt(password, this.seed));
            }

            if (((serverCapabilities & CLIENT_CONNECT_WITH_DB) != 0)
                    && (database != null) && (database.length() > 0)) {
                packet.writeString(database);
            }
            
            send(packet);
            checkErrorPacket();
        }
    }

    /**
     * Does the server send back extra column info?
     *
     * @return true if so
     */
    protected boolean hasLongColumnInfo() {
        return this.hasLongColumnInfo;
    }

    /**
     * Unpacks the Field information from the given packet. Understands pre 4.1
     * and post 4.1 server version field packet structures.
     *
     * @param packet the packet containing the field information
     * @param extractDefaultValues should default values be extracted?
     *
     * @return the unpacked field
     */
    protected final Field unpackField(Buffer packet,
        boolean extractDefaultValues) throws SQLException {
        if (this.use41Extensions) {
            // we only store the position of the string and
            // materialize only if needed...
            if (this.has41NewNewProt) {
                int catalogNameStart = packet.getPosition() + 1;
                int catalogNameLength = packet.fastSkipLenString();
            }

            int databaseNameStart = packet.getPosition() + 1;
            int databaseNameLength = packet.fastSkipLenString();

            int tableNameStart = packet.getPosition() + 1;
            int tableNameLength = packet.fastSkipLenString();

            // orgTableName is never used so skip
            int originalTableNameStart = packet.getPosition() + 1;
            int originalTableNameLength = packet.fastSkipLenString();

            // we only store the position again...
            int nameStart = packet.getPosition() + 1;
            int nameLength = packet.fastSkipLenString();

            // orgColName is not required so skip...
            int originalColumnNameStart = packet.getPosition() + 1;
            int originalColumnNameLength = packet.fastSkipLenString();

            packet.readByte();

            int charSetNumber = packet.readInt();

            int colLength = 0;

            if (this.has41NewNewProt) {
                // fixme
                colLength = (int) packet.readLong();
            } else {
                colLength = packet.readLongInt();
            }

            int colType = packet.readByte() & 0xff;

            short colFlag = 0;

            if (this.hasLongColumnInfo) {
                colFlag = (short) (packet.readInt());
            } else {
                colFlag = (short) (packet.readByte() & 0xff);
            }

            int colDecimals = packet.readByte() & 0xff;

            int defaultValueStart = -1;
            int defaultValueLength = -1;

            if (extractDefaultValues) {
                defaultValueStart = packet.getPosition() + 1;
                defaultValueLength = packet.fastSkipLenString();
            }

            Field field = new Field(this.connection, packet.getByteBuffer(),
                    databaseNameStart, databaseNameLength, tableNameStart,
                    tableNameLength, originalTableNameStart,
                    originalTableNameLength, nameStart, nameLength,
                    originalColumnNameStart, originalColumnNameLength,
                    colLength, colType, colFlag, colDecimals,
                    defaultValueStart, defaultValueLength, charSetNumber);

            return field;
        } else {
            int tableNameStart = packet.getPosition() + 1;
            int tableNameLength = packet.fastSkipLenString();
            int nameStart = packet.getPosition() + 1;
            int nameLength = packet.fastSkipLenString();
            int colLength = packet.readnBytes();
            int colType = packet.readnBytes();
            packet.readByte(); // We know it's currently 2

            short colFlag = 0;

            if (this.hasLongColumnInfo) {
                colFlag = (short) (packet.readInt());
            } else {
                colFlag = (short) (packet.readByte() & 0xff);
            }

            int colDecimals = (packet.readByte() & 0xff);

            if (this.colDecimalNeedsBump) {
                colDecimals++;
            }

            Field field = new Field(this.connection, packet.getBufferSource(),
                    nameStart, nameLength, tableNameStart, tableNameLength,
                    colLength, colType, colFlag, colDecimals);

            return field;
        }
    }

    /**
     * Determines if the database charset is the same as the platform charset
     */
    protected void checkForCharsetMismatch() {
        if (this.connection.useUnicode()
                && (this.connection.getEncoding() != null)) {
            String encodingToCheck = jvmPlatformCharset;

            if (encodingToCheck == null) {
                encodingToCheck = System.getProperty("file.encoding");
            }

            if (encodingToCheck == null) {
                this.platformDbCharsetMatches = false;
            } else {
                this.platformDbCharsetMatches = encodingToCheck.equals(this.connection
                        .getEncoding());
            }
        }
    }

    static int getMaxBuf() {
        return maxBufferSize;
    }

    /**
     * Get the major version of the MySQL server we are talking to.
     *
     * @return DOCUMENT ME!
     */
    final int getServerMajorVersion() {
        return this.serverMajorVersion;
    }

    /**
     * Get the minor version of the MySQL server we are talking to.
     *
     * @return DOCUMENT ME!
     */
    final int getServerMinorVersion() {
        return this.serverMinorVersion;
    }

    /**
     * Get the sub-minor version of the MySQL server we are talking to.
     *
     * @return DOCUMENT ME!
     */
    final int getServerSubMinorVersion() {
        return this.serverSubMinorVersion;
    }

    /**
     * Get the version string of the server we are talking to
     *
     * @return DOCUMENT ME!
     */
    String getServerVersion() {
        return this.serverVersion;
    }

    /**
     * Initialize communications with the MySQL server. Handles logging on, and
     * handling initial connection errors.
     *
     * @param user DOCUMENT ME!
     * @param password DOCUMENT ME!
     * @param database DOCUMENT ME!
     *
     * @throws java.sql.SQLException DOCUMENT ME!
     * @throws SQLException DOCUMENT ME!
     */
    void doHandshake(String user, String password, String database)
        throws java.sql.SQLException {
        // Read the first packet
        Buffer buf = readPacket();

        // Get the protocol version
        this.protocolVersion = buf.readByte();

        if (this.protocolVersion == -1) {
            try {
                this.mysqlConnection.close();
            } catch (Exception e) {
                ; // ignore
            }

            int errno = 2000;

            errno = buf.readInt();

            String serverErrorMessage = buf.readString();

            StringBuffer errorBuf = new StringBuffer(" message from server: \"");
            errorBuf.append(serverErrorMessage);
            errorBuf.append("\"");

            String xOpen = SQLError.mysqlToXOpen(errno);

            throw new SQLException(SQLError.get(xOpen) + ", "
                + errorBuf.toString(), xOpen, errno);
        }

        this.serverVersion = buf.readString();

        // Parse the server version into major/minor/subminor
        int point = this.serverVersion.indexOf(".");

        if (point != -1) {
            try {
                int n = Integer.parseInt(this.serverVersion.substring(0, point));
                this.serverMajorVersion = n;
            } catch (NumberFormatException NFE1) {
                ;
            }

            String remaining = this.serverVersion.substring(point + 1,
                    this.serverVersion.length());
            point = remaining.indexOf(".");

            if (point != -1) {
                try {
                    int n = Integer.parseInt(remaining.substring(0, point));
                    this.serverMinorVersion = n;
                } catch (NumberFormatException nfe) {
                    ;
                }

                remaining = remaining.substring(point + 1, remaining.length());

                int pos = 0;

                while (pos < remaining.length()) {
                    if ((remaining.charAt(pos) < '0')
                            || (remaining.charAt(pos) > '9')) {
                        break;
                    }

                    pos++;
                }

                try {
                    int n = Integer.parseInt(remaining.substring(0, pos));
                    this.serverSubMinorVersion = n;
                } catch (NumberFormatException nfe) {
                    ;
                }
            }
        }

        if (versionMeetsMinimum(4, 0, 8)) {
            this.maxThreeBytes = (256 * 256 * 256) - 1;
            this.useNewLargePackets = true;
        } else {
            this.maxThreeBytes = 255 * 255 * 255;
            this.useNewLargePackets = false;
        }

        this.colDecimalNeedsBump = versionMeetsMinimum(3, 23, 0);
        this.colDecimalNeedsBump = !versionMeetsMinimum(3, 23, 15); // guess? Not noted in changelog
        this.useNewUpdateCounts = versionMeetsMinimum(3, 22, 5);

        long threadId = buf.readLong();
        seed = buf.readString();

        if (Driver.TRACE) {
            Debug.msg(this, "Protocol Version: " + (int) this.protocolVersion);
            Debug.msg(this, "Server Version: " + this.serverVersion);
            Debug.msg(this, "Thread ID: " + threadId);
            Debug.msg(this, "Crypt Seed: " + seed);
        }

        this.serverCapabilities = 0;

        if (buf.getPosition() < buf.getBufLength()) {
            serverCapabilities = buf.readInt();
        }

        if (versionMeetsMinimum(4, 1, 1)) {
            int position = buf.getPosition();
            
            this.serverCharsetIndex = buf.readByte() & 0xff;
			//not used this.serverStatus = buf.readInt();
			
            buf.setPosition(position + 16);

            String seedPart2 = buf.readString();
            StringBuffer newSeed = new StringBuffer(20);
            newSeed.append(seed);
            newSeed.append(seedPart2);
            this.seed = newSeed.toString();
        }

        if (((serverCapabilities & CLIENT_COMPRESS) != 0)
                && this.connection.useCompression()) {
            clientParam |= CLIENT_COMPRESS;
        }

        if ((database != null) && (database.length() > 0)) {
            clientParam |= CLIENT_CONNECT_WITH_DB;
        }

        if (((serverCapabilities & CLIENT_SSL) == 0)
                && this.connection.useSSL()) {
            this.connection.setUseSSL(false);
        }

        if ((serverCapabilities & CLIENT_LONG_FLAG) != 0) {
            // We understand other column flags, as well
            clientParam |= CLIENT_LONG_FLAG;
            this.hasLongColumnInfo = true;
        }

        // return FOUND rows
        clientParam |= CLIENT_FOUND_ROWS;

        if (this.connection.allowLoadLocalInfile()) {
            clientParam |= CLIENT_LOCAL_FILES;
        }

        if (isInteractiveClient) {
            clientParam |= CLIENT_INTERACTIVE;
        }

        // Authenticate
        if (this.protocolVersion > 9) {
            clientParam |= CLIENT_LONG_PASSWORD; // for long passwords
        } else {
            clientParam &= ~CLIENT_LONG_PASSWORD;
        }

        //
        // 4.1 has some differences in the protocol
        //
        if (versionMeetsMinimum(4, 1, 0)) {
            if (versionMeetsMinimum(4, 1, 1)) {
                clientParam |= CLIENT_PROTOCOL_41;
                this.has41NewNewProt = true;
            } else {
                clientParam |= CLIENT_RESERVED;
                this.has41NewNewProt = false;
            }

            this.use41Extensions = true;
        }

        int passwordLength = 16;
        int userLength = 0;
        int databaseLength = 0;

        if (user != null) {
            userLength = user.length();
        }

        if (database != null) {
            databaseLength = database.length();
        }

        int packLength = (userLength + passwordLength + databaseLength) + 7
            + HEADER_LENGTH;
        Buffer packet = null;

        if (!connection.useSSL()) {
            if ((serverCapabilities & CLIENT_SECURE_CONNECTION) != 0) {
                clientParam |= CLIENT_SECURE_CONNECTION;

                if (versionMeetsMinimum(4, 1, 1)) {
                    secureAuth411(null, packLength, user, password, database,
                        true);
                } else {
                    secureAuth(null, packLength, user, password, database, true);
                }
            } else {
                packet = new Buffer(packLength);

                if ((clientParam & CLIENT_RESERVED) != 0) {
                    if (versionMeetsMinimum(4, 1, 1)) {
                        packet.writeLong(clientParam);
                        packet.writeLong(this.maxThreeBytes);

                        // charset, JDBC will connect as 'latin1',
                        // and use 'SET NAMES' to change to the desired
                        // charset after the connection is established.
                        packet.writeByte((byte) 8);

                        // Set of bytes reserved for future use.
                        packet.writeBytesNoNull(new byte[23]);
                    } else {
                        packet.writeLong(clientParam);
                        packet.writeLong(this.maxThreeBytes);
                    }
                } else {
                    packet.writeInt((int) clientParam);
                    packet.writeLongInt(this.maxThreeBytes);
                }

                // User/Password data
                packet.writeString(user);

                if (this.protocolVersion > 9) {
                    packet.writeString(Util.newCrypt(password, this.seed));
                } else {
                    packet.writeString(Util.oldCrypt(password, this.seed));
                }

                if (((serverCapabilities & CLIENT_CONNECT_WITH_DB) != 0)
                        && (database != null) && (database.length() > 0)) {
                    packet.writeString(database);
                }

                send(packet);
            }
        } else {
            boolean doSecureAuth = false;

            if ((serverCapabilities & CLIENT_SECURE_CONNECTION) != 0) {
                clientParam |= CLIENT_SECURE_CONNECTION;
                doSecureAuth = true;
            }

            clientParam |= CLIENT_SSL;
            packet = new Buffer(packLength);

            if ((clientParam & CLIENT_RESERVED) != 0) {
                packet.writeLong(clientParam);
            } else {
                packet.writeInt((int) clientParam);
            }

            send(packet);

            javax.net.ssl.SSLSocketFactory sslFact = (javax.net.ssl.SSLSocketFactory) javax.net.ssl.SSLSocketFactory
                .getDefault();

            try {
                this.mysqlConnection = sslFact.createSocket(this.mysqlConnection,
                        this.host, this.port, true);

                // need to force TLSv1, or else JSSE tries to do a SSLv2 handshake
                // which MySQL doesn't understand
                ((javax.net.ssl.SSLSocket) this.mysqlConnection)
                .setEnabledProtocols(new String[] { "TLSv1" });
                ((javax.net.ssl.SSLSocket) this.mysqlConnection).startHandshake();
                this.mysqlInput = new BufferedInputStream(this.mysqlConnection
                        .getInputStream(), 16384);
                this.mysqlOutput = new BufferedOutputStream(this.mysqlConnection
                        .getOutputStream(), 16384);
                this.mysqlOutput.flush();
            } catch (IOException ioEx) {
                StringBuffer message = new StringBuffer(SQLError.get(
                            SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE));
                message.append(": ");
                message.append(ioEx.getClass().getName());
                message.append(", underlying cause: ");
                message.append(ioEx.getMessage());

                if (!this.connection.useParanoidErrorMessages()) {
                    message.append(Util.stackTraceToString(ioEx));
                }

                throw new java.sql.SQLException(message.toString(),
                    SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE, 0);
            }

            packet.clear();

            if (doSecureAuth) {
                if (versionMeetsMinimum(4, 1, 1)) {
                    secureAuth411(null, packLength, user, password, database,
                        true);
                } else {
                    secureAuth(null, packLength, user, password, database, true);
                }
            } else {
                if ((clientParam & CLIENT_RESERVED) != 0) {
                    packet.writeLong(clientParam);
                    packet.writeLong(this.maxThreeBytes);
                } else {
                    packet.writeInt((int) clientParam);
                    packet.writeLongInt(this.maxThreeBytes);
                }

                // User/Password data
                packet.writeString(user);

                if (this.protocolVersion > 9) {
                    packet.writeString(Util.newCrypt(password, seed));
                } else {
                    packet.writeString(Util.oldCrypt(password, seed));
                }

                if (((serverCapabilities & CLIENT_CONNECT_WITH_DB) != 0)
                        && (database != null) && (database.length() > 0)) {
                    packet.writeString(database);
                }

                send(packet);
            }
        }

        // Check for errors, not for 4.1.1 or newer,
        // as the new auth protocol doesn't work that way
        // (see secureAuth411() for more details...)
        if (!versionMeetsMinimum(4, 1, 1)) {
            checkErrorPacket();
        }

        //
        // Can't enable compression until after handshake
        //
        if (((serverCapabilities & CLIENT_COMPRESS) != 0)
                && this.connection.useCompression()) {
            // The following matches with ZLIB's
            // compress()
            this.deflater = new Deflater();
            this.useCompression = true;
            this.mysqlInput = new CompressedInputStream(this.mysqlInput);
        }

        if (((serverCapabilities & CLIENT_CONNECT_WITH_DB) == 0)
                && (database != null) && (database.length() > 0)) {
            try {
                sendCommand(MysqlDefs.INIT_DB, database, null);
            } catch (Exception ex) {
                throw new SQLException(ex.toString()
                    + (this.connection.useParanoidErrorMessages() ? ""
                                                                  : Util
                    .stackTraceToString(ex)),
                    SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE);
            }
        }
    }

    /**
     * Retrieve one row from the MySQL server. Note: this method is not
     * thread-safe, but it is only called from methods that are guarded by
     * synchronizing on this object.
     *
     * @param columnCount DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    final byte[][] nextRow(int columnCount) throws Exception {
        // Get the next incoming packet, re-using the packet because
        // all the data we need gets copied out of it.
        Buffer rowPacket = checkErrorPacket();

        //
        // Didn't read an error, so re-position to beginning
        // of packet in order to read result set data
        //
        int offset = 0;

        //if (rowPacket.wasMultiPacket()) {
        //    if (this.useNewLargePackets) {
        //        offset = HEADER_LENGTH;
        //    } else {
        //        offset = HEADER_LENGTH + 1;
        //   }
        //}
        rowPacket.setPosition(rowPacket.getPosition() - 1);

        byte[][] rowData = new byte[columnCount][];

        if (!rowPacket.isLastDataPacket()) {
            for (int i = 0; i < columnCount; i++) {
                rowData[i] = rowPacket.readLenByteArray(offset);

                if (Driver.TRACE) {
                    if (rowData[i] == null) {
                        Debug.msg(this, "Field value: NULL");
                    } else {
                        Debug.msg(this, "Field value: " + rowData[i].toString());
                    }
                }
            }

            return rowData;
        }

        return null;
    }

    /**
     * Log-off of the MySQL server and close the socket.
     *
     * @throws SQLException DOCUMENT ME!
     */
    final void quit() throws SQLException {
        Buffer packet = new Buffer(6);
        this.packetSequence = -1;
        packet.writeByte((byte) MysqlDefs.QUIT);
        send(packet);
        forceClose();
    }

    /**
     * Returns the packet used for sending data (used by PreparedStatement)
     * Guarded by external synchronization on a mutex.
     *
     * @return A packet to send data with
     */
    Buffer getSharedSendPacket() {
        if (this.sharedSendPacket == null) {
            this.sharedSendPacket = new Buffer(this.connection
                    .getNetBufferLength());
        }

        return this.sharedSendPacket;
    }

    void closeStreamer(RowData streamer) throws SQLException {
        if (this.streamingData == null) {
            throw new SQLException("Attempt to close streaming result set "
                + streamer
                + " when no streaming  result set was registered. This is an internal error.");
        }

        if (streamer != this.streamingData) {
            throw new SQLException("Attempt to close streaming result set "
                + streamer + " that was not registered."
                + " Only one streaming result set may be open and in use per-connection. Ensure that you have called .close() on "
                + " any active result sets before attempting more queries.");
        }

        this.streamingData = null;
    }

    /**
     * Sets the buffer size to max-buf
     */
    void resetMaxBuf() {
        this.maxAllowedPacket = this.connection.getMaxAllowedPacket();
    }

    /**
     * Send a command to the MySQL server If data is to be sent with command,
     * it should be put in ExtraData Raw packets can be sent by setting
     * QueryPacket to something other than null.
     *
     * @param command DOCUMENT ME!
     * @param extraData DOCUMENT ME!
     * @param queryPacket DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     * @throws java.sql.SQLException DOCUMENT ME!
     */
    final Buffer sendCommand(int command, String extraData, Buffer queryPacket)
        throws Exception {
        checkForOutstandingStreamingData();

        try {
            if (this.clearStreamBeforeEachQuery) {
                clearInputStream();
            }

            //
            // PreparedStatements construct their own packets,
            // for efficiency's sake.
            //
            // If this is a generic query, we need to re-use
            // the sending packet.
            //
            if (queryPacket == null) {
                int packLength = HEADER_LENGTH + COMP_HEADER_LENGTH + 1
                    + ((extraData != null) ? extraData.length() : 0) + 2;

                if (this.sendPacket == null) {
                    this.sendPacket = new Buffer(packLength);
                }

                this.packetSequence = -1;
                this.sendPacket.clear();

                this.sendPacket.writeByte((byte) command);

                if ((command == MysqlDefs.INIT_DB)
                        || (command == MysqlDefs.CREATE_DB)
                        || (command == MysqlDefs.DROP_DB)
                        || (command == MysqlDefs.QUERY)) {
                    this.sendPacket.writeStringNoNull(extraData);
                } else if (command == MysqlDefs.PROCESS_KILL) {
                    long id = new Long(extraData).longValue();
                    this.sendPacket.writeLong(id);
                } else if ((command == MysqlDefs.RELOAD)
                        && (this.protocolVersion > 9)) {
                    Debug.msg(this, "Reload");

                    //Packet.writeByte(reloadParam);
                }

                send(this.sendPacket);
            } else {
                this.packetSequence = -1;
                send(queryPacket); // packet passed by PreparedStatement
            }
        } catch (SQLException sqlEx) {
            // don't wrap SQLExceptions
            throw sqlEx;
        } catch (Exception ex) {
            String underlyingMessage = ex.getMessage();

            throw new java.sql.SQLException(SQLError.get(
                    SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE) + ": "
                + ex.getClass().getName() + ", "
                + ((underlyingMessage != null) ? underlyingMessage
                                               : "no message given by JVM")
                + (this.connection.useParanoidErrorMessages() ? ""
                                                              : Util
                .stackTraceToString(ex)),
                SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE, 0);
        }

        return checkErrorPacket(command);
    }

    /**
     * Send a query specified in the String "Query" to the MySQL server. This
     * method uses the specified character encoding to get the bytes from the
     * query string.
     *
     * @param query DOCUMENT ME!
     * @param maxRows DOCUMENT ME!
     * @param characterEncoding DOCUMENT ME!
     * @param conn DOCUMENT ME!
     * @param resultSetType DOCUMENT ME!
     * @param streamResults DOCUMENT ME!
     * @param catalog DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    final ResultSet sqlQuery(String query, int maxRows,
        String characterEncoding, Connection conn, int resultSetType,
        boolean streamResults, String catalog) throws Exception {
        // We don't know exactly how many bytes we're going to get
        // from the query. Since we're dealing with Unicode, the
        // max is 2, so pad it (2 * query) + space for headers
        int packLength = HEADER_LENGTH + 1 + (query.length() * 2) + 2;

        if (this.sendPacket == null) {
            this.sendPacket = new Buffer(packLength);
        } else {
            this.sendPacket.clear();
        }

        this.sendPacket.writeByte((byte) MysqlDefs.QUERY);

        if (characterEncoding != null) {
            SingleByteCharsetConverter converter = this.connection
                .getCharsetConverter(characterEncoding);

            if (this.platformDbCharsetMatches) {
                this.sendPacket.writeStringNoNull(query, characterEncoding,
                    converter, this.connection.parserKnowsUnicode());
            } else {
                if (StringUtils.startsWithIgnoreCaseAndWs(query, "LOAD DATA")) {
                    this.sendPacket.writeBytesNoNull(query.getBytes());
                } else {
                    this.sendPacket.writeStringNoNull(query, characterEncoding,
                        converter, this.connection.parserKnowsUnicode());
                }
            }
        } else {
            this.sendPacket.writeStringNoNull(query);
        }

        return sqlQueryDirect(this.sendPacket, maxRows, conn, resultSetType,
            streamResults, catalog);
    }

    /**
     * Send a query stored in a packet directly to the server.
     *
     * @param queryPacket DOCUMENT ME!
     * @param maxRows DOCUMENT ME!
     * @param conn DOCUMENT ME!
     * @param resultSetType DOCUMENT ME!
     * @param streamResults DOCUMENT ME!
     * @param catalog DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    final ResultSet sqlQueryDirect(Buffer queryPacket, int maxRows,
        Connection conn, int resultSetType, boolean streamResults,
        String catalog) throws Exception {
        StringBuffer profileMsgBuf = null; // used if profiling
        long queryStartTime = 0;

        if (this.profileSql) {
            profileMsgBuf = new StringBuffer();
            queryStartTime = System.currentTimeMillis();

            byte[] queryBuf = queryPacket.getByteBuffer();

            int queryLength = queryPacket.getPosition();
            
            boolean queryTruncated = false;
            
            if (queryLength > MAX_QUERY_LENGTH_TO_LOG) {
            	queryLength = MAX_QUERY_LENGTH_TO_LOG;
            	
            	queryTruncated = true;
            }
            
            // Extract the actual query from the network packet
            String query = new String(queryBuf, 5,
                    (queryLength - 5));
            profileMsgBuf.append("Query\t\"");
            profileMsgBuf.append(query);
            
            if (queryTruncated) {
            	profileMsgBuf.append(" ... (long query truncated)");
            }
            
            profileMsgBuf.append("\"\texecution time:\t");
        }

        // Send query command and sql query string
        Buffer resultPacket = sendCommand(MysqlDefs.QUERY, null, queryPacket);

        if (this.profileSql) {
            long executionTime = System.currentTimeMillis() - queryStartTime;
            profileMsgBuf.append(executionTime);
            profileMsgBuf.append("\t");
        }

        resultPacket.setPosition(resultPacket.getPosition() - 1);

        long columnCount = resultPacket.readFieldLength();

        if (Driver.TRACE) {
            Debug.msg(this, "Column count: " + columnCount);
        }

        if (columnCount == 0) {
            if (this.profileSql) {
                System.err.println(profileMsgBuf.toString());
            }

            return buildResultSetWithUpdates(resultPacket);
        } else if (columnCount == Buffer.NULL_LENGTH) {
            String charEncoding = null;

            if (this.connection.useUnicode()) {
                charEncoding = this.connection.getEncoding();
            }

            String fileName = null;

            if (this.platformDbCharsetMatches) {
                fileName = ((charEncoding != null)
                    ? resultPacket.readString(charEncoding)
                    : resultPacket.readString());
            } else {
                fileName = resultPacket.readString();
            }

            return sendFileToServer(fileName);
        } else {
            long fetchStartTime = 0;

            if (this.profileSql) {
                fetchStartTime = System.currentTimeMillis();
            }

            com.mysql.jdbc.ResultSet results = getResultSet(columnCount,
                    maxRows, resultSetType, streamResults, catalog);

            if (this.profileSql) {
                long fetchElapsedTime = System.currentTimeMillis()
                    - fetchStartTime;
                profileMsgBuf.append("result set fetch time:\t");
                profileMsgBuf.append(fetchElapsedTime);
                System.err.println(profileMsgBuf.toString());
            }

            return results;
        }
    }

    /**
     * Returns the host this IO is connected to
     *
     * @return DOCUMENT ME!
     */
    String getHost() {
        return this.host;
    }

    /**
     * Does the version of the MySQL server we are connected to meet the given
     * minimums?
     *
     * @param major DOCUMENT ME!
     * @param minor DOCUMENT ME!
     * @param subminor DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    boolean versionMeetsMinimum(int major, int minor, int subminor) {
        if (getServerMajorVersion() >= major) {
            if (getServerMajorVersion() == major) {
                if (getServerMinorVersion() >= minor) {
                    if (getServerMinorVersion() == minor) {
                        return (getServerSubMinorVersion() >= subminor);
                    } else {
                        // newer than major.minor
                        return true;
                    }
                } else {
                    // older than major.minor
                    return false;
                }
            } else {
                // newer than major
                return true;
            }
        } else {
            return false;
        }
    }

    private final int readFully(InputStream in, byte[] b, int off, int len)
        throws IOException {
        if (len < 0) {
            throw new IndexOutOfBoundsException();
        }

        int n = 0;

        while (n < len) {
            int count = in.read(b, off + n, len - n);

            if (count < 0) {
                throw new EOFException();
            }

            n += count;
        }

        return n;
    }

    /**
     * Read one packet from the MySQL server
     *
     * @return DOCUMENT ME!
     *
     * @throws SQLException DOCUMENT ME!
     * @throws java.sql.SQLException DOCUMENT ME!
     */
    private final Buffer readPacket() throws SQLException {
        try {
            int lengthRead = readFully(mysqlInput, this.packetHeaderBuf, 0, 4);

            if (lengthRead < 4) {
                forceClose();
                throw new IOException("Unexpected end of input stream");
            }

            int packetLength = ((int) (this.packetHeaderBuf[0] & 0xff))
                + (((int) (this.packetHeaderBuf[1] & 0xff)) << 8)
                + (((int) (this.packetHeaderBuf[2] & 0xff)) << 16);

            byte multiPacketSeq = this.packetHeaderBuf[3];

            // Read data
            byte[] buffer = new byte[packetLength + 1];
            readFully(this.mysqlInput, buffer, 0, packetLength);
            buffer[packetLength] = 0;

            Buffer packet = new Buffer(buffer);

            return packet;
        } catch (IOException ioEx) {
            StringBuffer message = new StringBuffer(SQLError.get(
                        SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE));
            message.append(": ");
            message.append(ioEx.getClass().getName());
            message.append(", underlying cause: ");
            message.append(ioEx.getMessage());

            if (!this.connection.useParanoidErrorMessages()) {
                message.append(Util.stackTraceToString(ioEx));
            }

            throw new java.sql.SQLException(message.toString(),
                SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE, 0);
        }
    }

    private com.mysql.jdbc.ResultSet buildResultSetWithRows(String catalog,
        com.mysql.jdbc.Field[] fields, RowData rows, int resultSetConcurrency)
        throws SQLException {
        switch (resultSetConcurrency) {
        case java.sql.ResultSet.CONCUR_READ_ONLY:
            return new com.mysql.jdbc.ResultSet(catalog, fields, rows,
                this.connection);

        case java.sql.ResultSet.CONCUR_UPDATABLE:
            return new com.mysql.jdbc.UpdatableResultSet(catalog, fields, rows,
                this.connection);

        default:
            return new com.mysql.jdbc.ResultSet(catalog, fields, rows,
                this.connection);
        }
    }

    private com.mysql.jdbc.ResultSet buildResultSetWithUpdates(
        Buffer resultPacket) throws SQLException {
        long updateCount = -1;
        long updateID = -1;
        String info = null;

        try {
            if (this.useNewUpdateCounts) {
                updateCount = resultPacket.newReadLength();
                updateID = resultPacket.newReadLength();
            } else {
                updateCount = (long) resultPacket.readLength();
                updateID = (long) resultPacket.readLength();
            }

            if (this.connection.isReadInfoMsgEnabled()) {
                if (this.use41Extensions) {
                    int serverStatus = resultPacket.readInt();
                    int warningCount = resultPacket.readInt();

                    resultPacket.readByte(); // advance pointer
                }

                info = resultPacket.readString();
            }
        } catch (Exception ex) {
            throw new java.sql.SQLException(SQLError.get(
                    SQLError.SQL_STATE_GENERAL_ERROR) + ": "
                + ex.getClass().getName(), SQLError.SQL_STATE_GENERAL_ERROR, -1);
        }

        if (Driver.TRACE) {
            Debug.msg(this, "Update Count = " + updateCount);
        }

        ResultSet updateRs = new ResultSet(updateCount, updateID);

        if (info != null) {
            updateRs.setServerInfo(info);
        }

        return updateRs;
    }

    /**
     * Don't hold on to overly-large packets
     */
    private void reclaimLargeReusablePacket() {
        if ((this.reusablePacket != null)
                && (this.reusablePacket.getBufLength() > 1048576)) {
            this.reusablePacket = new Buffer(this.connection.getNetBufferLength());
        }
    }

    /**
     * Re-use a packet to read from the MySQL server
     *
     * @param reuse DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws SQLException DOCUMENT ME!
     * @throws SQLException DOCUMENT ME!
     */
    private final Buffer reuseAndReadPacket(Buffer reuse)
        throws SQLException {
        try {
            reuse.setWasMultiPacket(false);

            int lengthRead = readFully(mysqlInput, this.packetHeaderBuf, 0, 4);

            if (lengthRead < 4) {
                forceClose();
                throw new IOException("Unexpected end of input stream");
            }

            int packetLength = ((int) (this.packetHeaderBuf[0] & 0xff))
                + (((int) (this.packetHeaderBuf[1] & 0xff)) << 8)
                + (((int) (this.packetHeaderBuf[2] & 0xff)) << 16);

            byte multiPacketSeq = this.packetHeaderBuf[3];

            //byte multiPacketSeq = (byte) this.mysqlInput.read();
            // Set the Buffer to it's original state
            reuse.setPosition(0);
            reuse.setSendLength(0);

            // Do we need to re-alloc the byte buffer?
            //
            // Note: We actually check the length of the buffer,
            // rather than getBufLength(), because getBufLength() is not
            // necesarily the actual length of the byte array
            // used as the buffer
            if (reuse.getByteBuffer().length <= packetLength) {
                reuse.setByteBuffer(new byte[packetLength + 1]);
            }

            // Set the new length
            reuse.setBufLength(packetLength);

            // Read the data from the server
            readFully(this.mysqlInput, reuse.getByteBuffer(), 0, packetLength);

            boolean isMultiPacket = false;

            if (packetLength == maxThreeBytes) {
                reuse.setPosition((int) maxThreeBytes);

                int packetEndPoint = packetLength;

                // it's multi-packet
                isMultiPacket = true;

                lengthRead = readFully(mysqlInput, this.packetHeaderBuf, 0, 4);

                if (lengthRead < 4) {
                    forceClose();
                    throw new IOException("Unexpected end of input stream");
                }

                packetLength = ((int) (this.packetHeaderBuf[0] & 0xff))
                    + (((int) (this.packetHeaderBuf[1] & 0xff)) << 8)
                    + (((int) (this.packetHeaderBuf[2] & 0xff)) << 16);

                Buffer multiPacket = new Buffer(packetLength);
                boolean firstMultiPkt = true;

                while (true) {
                    if (!firstMultiPkt) {
                        lengthRead = readFully(mysqlInput,
                                this.packetHeaderBuf, 0, 4);

                        if (lengthRead < 4) {
                            forceClose();
                            throw new IOException(
                                "Unexpected end of input stream");
                        }

                        packetLength = ((int) (this.packetHeaderBuf[0] & 0xff))
                            + (((int) (this.packetHeaderBuf[1] & 0xff)) << 8)
                            + (((int) (this.packetHeaderBuf[2] & 0xff)) << 16);
                    } else {
                        firstMultiPkt = false;
                    }

                    if (!this.useNewLargePackets && (packetLength == 1)) {
                        clearInputStream();

                        break;
                    } else if (packetLength < this.maxThreeBytes) {
                        byte newPacketSeq = this.packetHeaderBuf[3];

                        if (newPacketSeq != (multiPacketSeq + 1)) {
                            throw new IOException(
                                "Packets received out of order");
                        }

                        multiPacketSeq = newPacketSeq;

                        // Set the Buffer to it's original state
                        multiPacket.setPosition(0);
                        multiPacket.setSendLength(0);

                        // Set the new length
                        multiPacket.setBufLength(packetLength);

                        // Read the data from the server
                        byte[] byteBuf = multiPacket.getByteBuffer();
                        int lengthToWrite = packetLength;

                        int bytesRead = readFully(this.mysqlInput, byteBuf, 0,
                                packetLength);

                        if (bytesRead != lengthToWrite) {
                            throw new SQLException(
                                "Short read from server, expected "
                                + lengthToWrite + " bytes, received only "
                                + bytesRead + ".",
                                SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE);
                        }

                        reuse.writeBytesNoNull(byteBuf, 0, lengthToWrite);

                        packetEndPoint += lengthToWrite;

                        break; // end of multipacket sequence
                    }

                    byte newPacketSeq = this.packetHeaderBuf[3];

                    if (newPacketSeq != (multiPacketSeq + 1)) {
                        throw new IOException("Packets received out of order");
                    }

                    multiPacketSeq = newPacketSeq;

                    // Set the Buffer to it's original state
                    multiPacket.setPosition(0);
                    multiPacket.setSendLength(0);

                    // Set the new length
                    multiPacket.setBufLength(packetLength);

                    // Read the data from the server
                    byte[] byteBuf = multiPacket.getByteBuffer();
                    int lengthToWrite = packetLength;

                    int bytesRead = readFully(this.mysqlInput, byteBuf, 0,
                            packetLength);

                    if (bytesRead != lengthToWrite) {
                        throw new SQLException(
                            "Short read from server, expected " + lengthToWrite
                            + " bytes, received only " + bytesRead + ".",
                            SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE);
                    }

                    reuse.writeBytesNoNull(byteBuf, 0, lengthToWrite);

                    packetEndPoint += lengthToWrite;
                }

                //reuse.writeByte((byte) 0);
                reuse.setPosition(0);
                reuse.setWasMultiPacket(true);
            }

            if (!isMultiPacket) {
                reuse.getByteBuffer()[packetLength] = 0; // Null-termination
            }

            return reuse;
        } catch (IOException ioEx) {
            StringBuffer message = new StringBuffer(SQLError.get(
                        SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE));
            message.append(": ");
            message.append(ioEx.getClass().getName());
            message.append(", underlying cause: ");
            message.append(ioEx.getMessage());

            if (!this.connection.useParanoidErrorMessages()) {
                message.append(Util.stackTraceToString(ioEx));
            }

            throw new java.sql.SQLException(message.toString(),
                SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE, 0);
        }
    }

    /**
     * Send a packet to the MySQL server
     *
     * @param packet DOCUMENT ME!
     *
     * @throws SQLException DOCUMENT ME!
     */
    private final void send(Buffer packet) throws SQLException {
        int l = packet.getPosition();
        send(packet, l);

        // 
        // Don't hold on to large packets
        //
        if (packet == this.sharedSendPacket) {
            reclaimLargeSharedSendPacket();
        }
    }

    private final void send(Buffer packet, int packetLen)
        throws SQLException {
        try {
            if (packetLen > this.maxAllowedPacket) {
                throw new PacketTooBigException(packetLen, this.maxAllowedPacket);
            }

            if ((serverMajorVersion >= 4) && (packetLen >= maxThreeBytes)) {
                sendSplitPackets(packet);
            } else {
                this.packetSequence++;

                Buffer packetToSend = packet;

                packetToSend.setPosition(0);

                if (this.useCompression) {
                    packetToSend = compressPacket(packet, 0, packetLen,
                            HEADER_LENGTH);
                    packetLen = packetToSend.getPosition();
                } else {
                    packetToSend.writeLongInt(packetLen - HEADER_LENGTH);
                    packetToSend.writeByte(this.packetSequence);
                }

                this.mysqlOutput.write(packetToSend.getByteBuffer(), 0,
                    packetLen);
                this.mysqlOutput.flush();
            }

            // 
            // Don't hold on to large packets
            //
            if (packet == this.sharedSendPacket) {
                reclaimLargeSharedSendPacket();
            }
        } catch (IOException ioEx) {
            StringBuffer message = new StringBuffer(SQLError.get(
                        SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE));
            message.append(": ");
            message.append(ioEx.getClass().getName());
            message.append(", underlying cause: ");
            message.append(ioEx.getMessage());

            if (!this.connection.useParanoidErrorMessages()) {
                message.append(Util.stackTraceToString(ioEx));
            }

            throw new java.sql.SQLException(message.toString(),
                SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE, 0);
        }
    }

    /**
     * Reads and sends a file to the server for LOAD DATA LOCAL INFILE
     *
     * @param fileName the file name to send.
     *
     * @return DOCUMENT ME!
     *
     * @throws SQLException DOCUMENT ME!
     */
    private final ResultSet sendFileToServer(String fileName)
        throws SQLException {
        Buffer filePacket = (loadFileBufRef == null) ? null
                                                     : (Buffer) (loadFileBufRef
            .get());

        int packetLength = Math.min(this.connection.getMaxAllowedPacket()
                - (HEADER_LENGTH * 3),
                alignPacketSize(this.connection.getMaxAllowedPacket() - 16, 4096)
                - (HEADER_LENGTH * 3));

        //
        // This packet may be _way_ too large to actually allocate,
        // unforunately, LOAD DATA LOCAL INFILE requires this setup...
        //
        try {
            if (filePacket == null) {
                filePacket = new Buffer((int) (packetLength + HEADER_LENGTH));
                loadFileBufRef = new SoftReference(filePacket);
            }
        } catch (OutOfMemoryError oom) {
            // Attempt to do this, but it might not work...
            // The server is expecting at least one packet, so we 
            // send an empty 'EOF' packet...
            this.reusablePacket.clear();
            send(this.reusablePacket);

            throw new SQLException("Unable to allocate packet of size '"
                + (packetLength + HEADER_LENGTH)
                + "' for LOAD DATA LOCAL INFILE. Either increase heap space available to your JVM, or adjust the MySQL server variable 'max_allowed_packet'",
                SQLError.SQL_STATE_MEMORY_ALLOCATION_FAILURE);
        }

        filePacket.clear();
        send(filePacket, 0);

        byte[] fileBuf = new byte[packetLength];

        BufferedInputStream fileIn = null;

        try {
            fileIn = new BufferedInputStream(new FileInputStream(fileName));

            int bytesRead = 0;

            while ((bytesRead = fileIn.read(fileBuf)) != -1) {
                filePacket.clear();
                filePacket.writeBytesNoNull(fileBuf, 0, bytesRead);
                send(filePacket);
            }
        } catch (IOException ioEx) {
            StringBuffer messageBuf = new StringBuffer("Unable to open file ");

            if (!this.connection.useParanoidErrorMessages()) {
                messageBuf.append("'");

                if (fileName != null) {
                    messageBuf.append(fileName);
                }

                messageBuf.append("'");
            }

            messageBuf.append("for 'LOAD DATA LOCAL INFILE' command.");

            if (!this.connection.useParanoidErrorMessages()) {
                messageBuf.append("Due to underlying IOException: ");
                messageBuf.append(Util.stackTraceToString(ioEx));
            }

            throw new SQLException(messageBuf.toString(),
                SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
        } finally {
            if (fileIn != null) {
                try {
                    fileIn.close();
                } catch (Exception ex) {
                    throw new SQLException("Unable to close local file during LOAD DATA LOCAL INFILE command",
                        SQLError.SQL_STATE_GENERAL_ERROR);
                }

                fileIn = null;
            } else {
                // file open failed, but server needs one packet
                filePacket.clear();
                send(filePacket);
            }
        }

        // send empty packet to mark EOF
        filePacket.clear();
        send(filePacket);

        Buffer resultPacket = checkErrorPacket();

        return buildResultSetWithUpdates(resultPacket);
    }

    /**
     * Checks for errors in the reply packet, and if none, returns the reply
     * packet, ready for reading
     *
     * @return DOCUMENT ME!
     *
     * @throws SQLException DOCUMENT ME!
     */
    private Buffer checkErrorPacket() throws SQLException {
        return checkErrorPacket(-1);
    }

    /**
     * Checks for errors in the reply packet, and if none, returns the reply
     * packet, ready for reading
     *
     * @param command the command being issued (if used)
     *
     * @return DOCUMENT ME!
     *
     * @throws SQLException if an error packet was received
     * @throws java.sql.SQLException DOCUMENT ME!
     */
    private Buffer checkErrorPacket(int command) throws SQLException {
        int statusCode = 0;
        Buffer resultPacket = null;

        try {
            // Check return value, if we get a java.io.EOFException,
            // the server has gone away. We'll pass it on up the
            // exception chain and let someone higher up decide
            // what to do (barf, reconnect, etc).
            resultPacket = reuseAndReadPacket(this.reusablePacket);
            statusCode = resultPacket.readByte();
        } catch (SQLException sqlEx) {
            // don't wrap SQLExceptions
            throw sqlEx;
        } catch (Exception fallThru) {
            String underlyingMessage = fallThru.getMessage();

            throw new java.sql.SQLException(SQLError.get(
                    SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE) + ": "
                + fallThru.getClass().getName() + ", "
                + ((underlyingMessage != null) ? underlyingMessage
                                               : "no message given by JVM")
                + (this.connection.useParanoidErrorMessages() ? ""
                                                              : Util
                .stackTraceToString(fallThru)),
                SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE, 0);
        }

        // Error handling
        if (statusCode == (byte) 0xff) {
            String serverErrorMessage;
            int errno = 2000;

            if (this.protocolVersion > 9) {
                errno = resultPacket.readInt();

                String xOpen = null;

                serverErrorMessage = resultPacket.readString();

                if (serverErrorMessage.startsWith("#")) {
                    // we have an SQLState
                    if (serverErrorMessage.length() > 6) {
                        xOpen = serverErrorMessage.substring(1, 6);
                        serverErrorMessage = serverErrorMessage.substring(6);

                        if (xOpen.equals("HY000")) {
                            xOpen = SQLError.mysqlToXOpen(errno);
                        }
                    } else {
                        xOpen = SQLError.mysqlToXOpen(errno);
                    }
                } else {
                    xOpen = SQLError.mysqlToXOpen(errno);
                }

                clearInputStream();

                StringBuffer errorBuf = new StringBuffer(
                        " message from server: \"");
                errorBuf.append(serverErrorMessage);
                errorBuf.append("\"");

                throw new SQLException(SQLError.get(xOpen) + ", "
                    + errorBuf.toString(), xOpen, errno);
            } else {
                serverErrorMessage = resultPacket.readString();
                clearInputStream();

                if (serverErrorMessage.indexOf("Unknown column") != -1) {
                    throw new java.sql.SQLException(SQLError.get(
                            SQLError.SQL_STATE_COLUMN_NOT_FOUND) + ", "
                        + serverErrorMessage,
                        SQLError.SQL_STATE_COLUMN_NOT_FOUND, -1);
                } else {
                    StringBuffer errorBuf = new StringBuffer(
                            " message from server: \"");
                    errorBuf.append(serverErrorMessage);
                    errorBuf.append("\"");

                    throw new java.sql.SQLException(SQLError.get(
                            SQLError.SQL_STATE_GENERAL_ERROR) + ", "
                        + errorBuf.toString(),
                        SQLError.SQL_STATE_GENERAL_ERROR, -1);
                }
            }
        }

        return resultPacket;
    }

    /**
     * Sends a large packet to the server as a series of smaller packets
     *
     * @param packet DOCUMENT ME!
     *
     * @throws SQLException DOCUMENT ME!
     * @throws SQLException DOCUMENT ME!
     */
    private final void sendSplitPackets(Buffer packet)
        throws SQLException {
        try {
            //
            // Big packets are handled by splitting them in packets of MAX_THREE_BYTES
            // length. The last packet is always a packet that is < MAX_THREE_BYTES.
            // (The last packet may even have a length of 0)
            //
            //
            // NB: Guarded by execSQL. If the driver changes architecture, this
            // will need to be synchronized in some other way
            //
            Buffer headerPacket = (splitBufRef == null) ? null
                                                        : (Buffer) (splitBufRef
                .get());

            //
            // Store this packet in a soft reference...It can be re-used if not GC'd (so clients
            // that use it frequently won't have to re-alloc the 16M buffer), but we don't
            // penalize infrequent users of large packets by keeping 16M allocated all of the time
            //
            if (headerPacket == null) {
                headerPacket = new Buffer((int) (maxThreeBytes + HEADER_LENGTH));
                splitBufRef = new SoftReference(headerPacket);
            }

            int len = packet.getPosition();
            int splitSize = (int) maxThreeBytes;
            int originalPacketPos = HEADER_LENGTH;
            byte[] origPacketBytes = packet.getByteBuffer();
            byte[] headerPacketBytes = headerPacket.getByteBuffer();

            if (Driver.DEBUG) {
                System.out.println("\n\nSending split packets for packet of "
                    + len + " bytes:\n");
            }

            while (len >= maxThreeBytes) {
                headerPacket.setPosition(0);
                headerPacket.writeLongInt(splitSize);
                this.packetSequence++;
                headerPacket.writeByte(this.packetSequence);
                System.arraycopy(origPacketBytes, originalPacketPos,
                    headerPacketBytes, 4, splitSize);
                this.mysqlOutput.write(headerPacketBytes, 0,
                    splitSize + HEADER_LENGTH);
                this.mysqlOutput.flush();

                if (Driver.DEBUG) {
                    System.out.print("  total packet length (header & data) "
                        + (splitSize + HEADER_LENGTH) + "\nheader: ");
                    headerPacket.dumpHeader();
                    System.out.println();
                    System.out.print("last eight bytes: ");
                    headerPacket.dumpNBytes(((splitSize + HEADER_LENGTH) - 8), 8);
                    System.out.println();
                }

                originalPacketPos += splitSize;
                len -= splitSize;
            }

            //
            // Write last packet
            //
            headerPacket.clear();
            headerPacket.setPosition(0);
            headerPacket.writeLongInt(len - HEADER_LENGTH);
            this.packetSequence++;
            headerPacket.writeByte(this.packetSequence);

            if (len != 0) {
                System.arraycopy(origPacketBytes, originalPacketPos,
                    headerPacketBytes, 4, len - HEADER_LENGTH);
            }

            this.mysqlOutput.write(headerPacket.getByteBuffer(), 0, len);
            this.mysqlOutput.flush();

            if (Driver.DEBUG) {
                System.out.print("  total packet length (header & data) " + len
                    + ",\nheader: ");
                headerPacket.dumpHeader();
                System.out.println();
                System.out.print("last packet bytes: ");
                headerPacket.dumpNBytes(0, len);
                System.out.println();
            }
        } catch (IOException ioEx) {
            StringBuffer message = new StringBuffer(SQLError.get(
                        SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE));
            message.append(": ");
            message.append(ioEx.getClass().getName());
            message.append(", underlying cause: ");
            message.append(ioEx.getMessage());

            if (!this.connection.useParanoidErrorMessages()) {
                message.append(Util.stackTraceToString(ioEx));
            }

            throw new java.sql.SQLException(message.toString(),
                SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE, 0);
        }
    }

    private int alignPacketSize(int a, int l) {
        return ((((a) + (l)) - 1) & ~((l) - 1));
    }

    private void checkForOutstandingStreamingData() throws SQLException {
        if (this.streamingData != null) {
            if (!this.connection.getClobberStreamingResults()) {
                throw new SQLException("Streaming result set "
                    + this.streamingData + " is still active."
                    + " Only one streaming result set may be open and in use per-connection. Ensure that you have called .close() on "
                    + " any active result sets before attempting more queries.");
            } else {
                // Close the result set
                this.streamingData.getOwner().realClose(false);

                // clear any pending data....
                clearInputStream();
            }
        }
    }

    private void clearInputStream() throws SQLException {
        try {
            int len = this.mysqlInput.available();

            while (len > 0) {
                this.mysqlInput.skip(len);
                len = this.mysqlInput.available();
            }
        } catch (IOException ioEx) {
            throw new SQLException("I/O error while clearing input stream of old results",
                SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE);
        }
    }

    private Buffer compressPacket(Buffer packet, int offset, int packetLen,
        int headerLength) throws SQLException {
        packet.writeLongInt(packetLen - headerLength);
        packet.writeByte((byte) 0); // wrapped packet has 0 packet seq.

        int lengthToWrite = 0;
        int compressedLength = 0;
        byte[] bytesToCompress = packet.getByteBuffer();
        byte[] compressedBytes = null;
        int offsetWrite = 0;

        if (true /*packetLen < MIN_COMPRESS_LEN*/) {
            lengthToWrite = packetLen;
            compressedBytes = packet.getByteBuffer();
            compressedLength = 0;
            offsetWrite = offset;
        } else {
            compressedBytes = new byte[bytesToCompress.length * 2];

            this.deflater.reset();
            this.deflater.setInput(bytesToCompress, offset, packetLen);
            this.deflater.finish();

            int compLen = this.deflater.deflate(compressedBytes);

            if (compLen > packetLen) {
                lengthToWrite = packetLen;
                compressedBytes = packet.getByteBuffer();
                compressedLength = 0;
                offsetWrite = offset;
            } else {
                lengthToWrite = compLen;
                headerLength += COMP_HEADER_LENGTH;
                compressedLength = packetLen;
            }
        }

        Buffer compressedPacket = new Buffer(packetLen + headerLength);

        compressedPacket.setPosition(0);
        compressedPacket.writeLongInt(lengthToWrite);
        compressedPacket.writeByte(this.packetSequence);
        compressedPacket.writeLongInt(compressedLength);
        compressedPacket.writeBytesNoNull(compressedBytes, offsetWrite,
            lengthToWrite);

        return compressedPacket;
    }

    private SocketFactory createSocketFactory() throws SQLException {
        try {
            if (socketFactoryClassName == null) {
                throw new SQLException("No name specified for socket factory",
                    SQLError.SQL_STATE_UNABLE_TO_CONNECT_TO_DATASOURCE);
            }

            return (SocketFactory) (Class.forName(socketFactoryClassName)
                                         .newInstance());
        } catch (Exception ex) {
            throw new SQLException("Could not create socket factory '"
                + socketFactoryClassName + "' due to underlying exception: "
                + ex.toString()
                + (this.connection.useParanoidErrorMessages() ? ""
                                                              : Util
                .stackTraceToString(ex)),
                SQLError.SQL_STATE_UNABLE_TO_CONNECT_TO_DATASOURCE);
        }
    }

    /**
     * Ensures that we don't hold on to overly-large send packets
     */
    private void reclaimLargeSharedSendPacket() {
        if ((this.sharedSendPacket != null)
                && (this.sharedSendPacket.getBufLength() > 1048576)) {
            this.sharedSendPacket = new Buffer(this.connection
                    .getNetBufferLength());
        }
    }

    /**
     * Secure authentication for 4.1 and newer servers.
     *
     * @param packet DOCUMENT ME!
     * @param packLength
     * @param user
     * @param password
     * @param database DOCUMENT ME!
     * @param writeClientParams
     *
     * @throws SQLException
     */
    private void secureAuth(Buffer packet, int packLength, String user,
        String password, String database, boolean writeClientParams)
        throws SQLException {
        // Passwords can be 16 chars long
        if (packet == null) {
            packet = new Buffer(packLength);
        }

        if (writeClientParams) {
            if (this.use41Extensions) {
                if (versionMeetsMinimum(4, 1, 1)) {
                    packet.writeLong(this.clientParam);
                    packet.writeLong(this.maxThreeBytes);

                    // charset, JDBC will connect as 'latin1',
                    // and use 'SET NAMES' to change to the desired
                    // charset after the connection is established.
                    packet.writeByte((byte) 8);

                    // Set of bytes reserved for future use.
                    packet.writeBytesNoNull(new byte[23]);
                } else {
                    packet.writeLong(this.clientParam);
                    packet.writeLong(this.maxThreeBytes);
                }
            } else {
                packet.writeInt((int) this.clientParam);
                packet.writeLongInt(this.maxThreeBytes);
            }
        }

        // User/Password data
        packet.writeString(user);

        if (password.length() != 0) {
            /* Prepare false scramble  */
            packet.writeString(FALSE_SCRAMBLE);
        } else {
            /* For empty password*/
            packet.writeString("");
        }

        if (((this.serverCapabilities & CLIENT_CONNECT_WITH_DB) != 0)
                && (database != null) && (database.length() > 0)) {
            packet.writeString(database);
        }

        send(packet);

        //
        // Don't continue stages if password is empty
        //
        if (password.length() > 0) {
            Buffer b = readPacket();

            b.setPosition(0);

            byte[] replyAsBytes = b.getByteBuffer();

            if ((replyAsBytes.length == 25) && (replyAsBytes[0] != 0)) {
                // Old passwords will have '*' at the first byte of hash */
                if (replyAsBytes[0] != '*') {
                    try {
                        /* Build full password hash as it is required to decode scramble */
                        byte[] buff = Security.passwordHashStage1(password);

                        /* Store copy as we'll need it later */
                        byte[] passwordHash = new byte[buff.length];
                        System.arraycopy(buff, 0, passwordHash, 0, buff.length);

                        /* Finally hash complete password using hash we got from server */
                        passwordHash = Security.passwordHashStage2(passwordHash,
                                replyAsBytes);

                        byte[] packetDataAfterSalt = new byte[replyAsBytes.length
                            - 5];

                        System.arraycopy(replyAsBytes, 4, packetDataAfterSalt,
                            0, replyAsBytes.length - 5);

                        byte[] mysqlScrambleBuff = new byte[20];

                        /* Decypt and store scramble 4 = hash for stage2 */
                        Security.passwordCrypt(packetDataAfterSalt,
                            mysqlScrambleBuff, passwordHash, 20);

                        /* Encode scramble with password. Recycle buffer */
                        Security.passwordCrypt(mysqlScrambleBuff, buff, buff, 20);

                        Buffer packet2 = new Buffer(25);
                        packet2.writeBytesNoNull(buff);

                        this.packetSequence++;

                        send(packet2, 24);
                    } catch (NoSuchAlgorithmException nse) {
                        throw new SQLException(
                            "Failed to create message digest 'SHA-1' for authentication. "
                            + " You must use a JDK that supports JCE to be able to use secure connection authentication",
                            SQLError.SQL_STATE_GENERAL_ERROR);
                    }
                } else {
                    try {
                        /* Create password to decode scramble */
                        byte[] passwordHash = Security.createKeyFromOldPassword(password);

                        /* Decypt and store scramble 4 = hash for stage2 */
                        byte[] netReadPos4 = new byte[replyAsBytes.length - 5];

                        System.arraycopy(replyAsBytes, 4, netReadPos4, 0,
                            replyAsBytes.length - 5);

                        byte[] mysqlScrambleBuff = new byte[20];

                        /* Decypt and store scramble 4 = hash for stage2 */
                        Security.passwordCrypt(netReadPos4, mysqlScrambleBuff,
                            passwordHash, 20);

                        /* Finally scramble decoded scramble with password */
                        String scrambledPassword = Util.scramble(new String(
                                    mysqlScrambleBuff), password);

                        Buffer packet2 = new Buffer(packLength);
                        packet2.writeString(scrambledPassword);
                        this.packetSequence++;

                        send(packet2, 24);
                    } catch (NoSuchAlgorithmException nse) {
                        throw new SQLException(
                            "Failed to create message digest 'SHA-1' for authentication. "
                            + " You must use a JDK that supports JCE to be able to use secure connection authentication",
                            SQLError.SQL_STATE_GENERAL_ERROR);
                    }
                }
            }
        }
    }

    /**
     * Secure authentication for 4.1.1 and newer servers.
     *
     * @param packet DOCUMENT ME!
     * @param packLength
     * @param user
     * @param password
     * @param database DOCUMENT ME!
     * @param writeClientParams
     *
     * @throws SQLException
     */
    private void secureAuth411(Buffer packet, int packLength, String user,
        String password, String database, boolean writeClientParams)
        throws SQLException {
        //	SERVER:  public_seed=create_random_string()
        //			 send(public_seed)
        //
        //	CLIENT:  recv(public_seed)
        //			 hash_stage1=sha1("password")
        //			 hash_stage2=sha1(hash_stage1)
        //			 reply=xor(hash_stage1, sha1(public_seed,hash_stage2)
        //
        //			 // this three steps are done in scramble()
        //
        //			 send(reply)
        //
        //
        //	SERVER:  recv(reply)
        //			 hash_stage1=xor(reply, sha1(public_seed,hash_stage2))
        //			 candidate_hash2=sha1(hash_stage1)
        //			 check(candidate_hash2==hash_stage2)
        // Passwords can be 16 chars long
        if (packet == null) {
            packet = new Buffer(packLength);
        }

        if (writeClientParams) {
            if (this.use41Extensions) {
                if (versionMeetsMinimum(4, 1, 1)) {
                    packet.writeLong(this.clientParam);
                    packet.writeLong(this.maxThreeBytes);

                    // charset, JDBC will connect as 'latin1',
                    // and use 'SET NAMES' to change to the desired
                    // charset after the connection is established.
                    packet.writeByte((byte) 8);

                    // Set of bytes reserved for future use.
                    packet.writeBytesNoNull(new byte[23]);
                } else {
                    packet.writeLong(this.clientParam);
                    packet.writeLong(this.maxThreeBytes);
                }
            } else {
                packet.writeInt((int) this.clientParam);
                packet.writeLongInt(this.maxThreeBytes);
            }
        }

        // User/Password data
        packet.writeString(user);

        if (password.length() != 0) {
            packet.writeByte((byte) 0x14);

            try {
                packet.writeBytesNoNull(Security.scramble411(password, this.seed));
            } catch (NoSuchAlgorithmException nse) {
                throw new SQLException(
                    "Failed to create message digest 'SHA-1' for authentication. "
                    + " You must use a JDK that supports JCE to be able to use secure connection authentication",
                    SQLError.SQL_STATE_GENERAL_ERROR);
            }
        } else {
            /* For empty password*/
            packet.writeByte((byte) 0);
        }

        if (((this.serverCapabilities & CLIENT_CONNECT_WITH_DB) != 0)
                && (database != null) && (database.length() > 0)) {
            packet.writeString(database);
        }

        send(packet);

        byte savePacketSequence = this.packetSequence++;

        Buffer reply = checkErrorPacket();

        if (reply.isLastDataPacket()) {
            /*
                  By sending this very specific reply server asks us to send scrambled
                  password in old format. The reply contains scramble_323.
            */
            this.packetSequence = ++savePacketSequence;
            packet.clear();

            String seed323 = this.seed.substring(0, 8);
            packet.writeString(Util.newCrypt(password, seed323));
            send(packet);

            /* Read what server thinks about out new auth message report */
            checkErrorPacket();
        }
    }

    /**
     * Secure authentication for 4.1.1 and newer servers.
     *
     * @param packLength
     * @param serverCapabilities DOCUMENT ME!
     * @param clientParam
     * @param user
     * @param password
     * @param database DOCUMENT ME!
     *
     * @throws SQLException
     */
    private void secureAuth411(int packLength, int serverCapabilities,
        long clientParam, String user, String password, String database)
        throws SQLException {
        //	SERVER:  public_seed=create_random_string()
        //			 send(public_seed)
        //
        //	CLIENT:  recv(public_seed)
        //			 hash_stage1=sha1("password")
        //			 hash_stage2=sha1(hash_stage1)
        //			 reply=xor(hash_stage1, sha1(public_seed,hash_stage2)
        //
        //			 // this three steps are done in scramble()
        //
        //			 send(reply)
        //
        //
        //	SERVER:  recv(reply)
        //			 hash_stage1=xor(reply, sha1(public_seed,hash_stage2))
        //			 candidate_hash2=sha1(hash_stage1)
        //			 check(candidate_hash2==hash_stage2)
        // Passwords can be 16 chars long
        Buffer packet = new Buffer(packLength);

        if (this.use41Extensions) {
            if (versionMeetsMinimum(4, 1, 1)) {
                packet.writeLong(this.clientParam);
                packet.writeLong(this.maxThreeBytes);

                // charset, JDBC will connect as 'latin1',
                // and use 'SET NAMES' to change to the desired
                // charset after the connection is established.
                packet.writeByte((byte) 8);

                // Set of bytes reserved for future use.
                packet.writeBytesNoNull(new byte[23]);
            } else {
                packet.writeLong(this.clientParam);
                packet.writeLong(this.maxThreeBytes);
            }
        } else {
            packet.writeInt((int) this.clientParam);
            packet.writeLongInt(this.maxThreeBytes);
        }

        // User/Password data
        packet.writeString(user);

        if (password.length() != 0) {
            packet.writeByte((byte) 0x14);

            try {
                packet.writeBytesNoNull(Security.scramble411(password, this.seed));
            } catch (NoSuchAlgorithmException nse) {
                throw new SQLException(
                    "Failed to create message digest 'SHA-1' for authentication. "
                    + " You must use a JDK that supports JCE to be able to use secure connection authentication",
                    SQLError.SQL_STATE_GENERAL_ERROR);
            }
        } else {
            /* For empty password*/
            packet.writeByte((byte) 0);
        }

        if (((serverCapabilities & CLIENT_CONNECT_WITH_DB) != 0)
                && (database != null) && (database.length() > 0)) {
            packet.writeString(database);
        }

        send(packet);

        byte savePacketSequence = this.packetSequence++;

        Buffer reply = checkErrorPacket();

        if (reply.isLastDataPacket()) {
            /*
              By sending this very specific reply server asks us to send scrambled
              password in old format. The reply contains scramble_323.
            */
            this.packetSequence = ++savePacketSequence;
            packet.clear();

            String seed323 = this.seed.substring(0, 8);
            packet.writeString(Util.newCrypt(password, seed323));
            send(packet);

            /* Read what server thinks about out new auth message report */
            checkErrorPacket();
        }
    }
}
