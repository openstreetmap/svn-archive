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

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;


/**
 * Field is a class used to describe fields in a
 * ResultSet
 *
 * @author Mark Matthews
 * @version $Id: Field.java,v 1.15.2.9 2004/04/18 21:02:51 mmatthew Exp $
 */
public class Field {
    //~ Static fields/initializers ---------------------------------------------

    private static final int AUTO_INCREMENT_FLAG = 512;
    private Connection connection = null;
    private String charsetName = null;
    private String databaseName = null;
    private String defaultValue = null;
    private String fullName = null;
    private String fullNameWithDatabase = null;
    private String fullOriginalName = null;
    private String fullOriginalNameWithDatabase = null;
    private String name; // The Field name
    private String originalColumnName = null;
    private String originalTableName = null;
    private String tableName; // The Name of the Table
    private byte[] buffer;
    private int charsetIndex = 0;
    private int colDecimals;
    private int databaseNameLength = -1;

    // database name info
    private int databaseNameStart = -1;
    private int defaultValueLength = -1;

    // default value info - from COM_LIST_FIELDS execution
    private int defaultValueStart = -1;
    private int length; // Internal length of the field;
    private int mysqlType = -1; // the MySQL type
    private int nameLength;
    private int nameStart;
    private int originalColumnNameLength = -1;

    // column name info (before aliasing)
    private int originalColumnNameStart = -1;
    private int originalTableNameLength = -1;

    // table name info (before aliasing)
    private int originalTableNameStart = -1;
    private int precisionAdjustFactor = 0;
    private int sqlType = -1; // the java.sql.Type
    private int tableNameLength;
    private int tableNameStart;
    private short colFlag;

    //~ Constructors -----------------------------------------------------------

    /**
    * Constructor used by DatabaseMetaData methods.
    */
    Field(String tableName, String columnName, int jdbcType, int length) {
        this.tableName = tableName;
        this.name = columnName;
        this.length = length;
        sqlType = jdbcType;
        colFlag = 0;
        colDecimals = 0;
    }

    /**
     * Constructor used when communicating with pre 4.1 servers
     */
    Field(Connection conn, byte[] buffer, int nameStart, int nameLength,
        int tableNameStart, int tableNameLength, int length, int mysqlType,
        short colFlag, int colDecimals) throws SQLException {
        this(conn, buffer, -1, -1, tableNameStart, tableNameLength, -1, -1,
            nameStart, nameLength, -1, -1, length, mysqlType, colFlag,
            colDecimals, -1, -1, MysqlDefs.NO_CHARSET_INFO);
    }

    /**
     * Constructor used when communicating with 4.1 and newer
     * servers
     */
    Field(Connection conn, byte[] buffer, int databaseNameStart,
        int databaseNameLength, int tableNameStart, int tableNameLength,
        int originalTableNameStart, int originalTableNameLength, int nameStart,
        int nameLength, int originalColumnNameStart,
        int originalColumnNameLength, int length, int mysqlType, short colFlag,
        int colDecimals, int defaultValueStart, int defaultValueLength,
        int charsetIndex) throws SQLException {
        this.connection = conn;
        this.buffer = buffer;
        this.nameStart = nameStart;
        this.nameLength = nameLength;
        this.tableNameStart = tableNameStart;
        this.tableNameLength = tableNameLength;
        this.length = length;
        this.colFlag = colFlag;
        this.colDecimals = colDecimals;
        this.mysqlType = mysqlType;

        // 4.1 field info...
        this.databaseNameStart = databaseNameStart;
        this.databaseNameLength = databaseNameLength;

        this.originalTableNameStart = originalTableNameStart;
        this.originalTableNameLength = originalTableNameLength;

        this.originalColumnNameStart = originalColumnNameStart;
        this.originalColumnNameLength = originalColumnNameLength;

        this.defaultValueStart = defaultValueStart;
        this.defaultValueLength = defaultValueLength;

        // Map MySqlTypes to java.sql Types
        this.sqlType = MysqlDefs.mysqlToJavaType(mysqlType);

        // If we're not running 4.1 or newer, use the connection's
        // charset
        this.charsetIndex = charsetIndex;
        
        this.charsetName = this.connection.getCharsetNameForIndex(this.charsetIndex);
        
        boolean isBinary = isBinary();

        //
        // Handle TEXT type (special case), Fix proposed by Peter McKeown
        //
        if ((sqlType == java.sql.Types.LONGVARBINARY) && !isBinary) {
            sqlType = java.sql.Types.LONGVARCHAR;
        } else if ((sqlType == java.sql.Types.VARBINARY) && !isBinary) {
            sqlType = java.sql.Types.VARCHAR;
        }

        //
        // Handle odd values for 'M' for floating point/decimal numbers
        //
        if (!isUnsigned()) {
            switch (this.mysqlType) {
            case MysqlDefs.FIELD_TYPE_DECIMAL:
                this.precisionAdjustFactor = -1;

                break;

            case MysqlDefs.FIELD_TYPE_DOUBLE:
            case MysqlDefs.FIELD_TYPE_FLOAT:
                this.precisionAdjustFactor = 1;

                break;
            }
        } else {
            switch (this.mysqlType) {
            case MysqlDefs.FIELD_TYPE_DOUBLE:
            case MysqlDefs.FIELD_TYPE_FLOAT:
                this.precisionAdjustFactor = 1;

                break;
            }
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isAutoIncrement() {
        return ((colFlag & AUTO_INCREMENT_FLAG) > 0);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isBinary() {
        return ((colFlag & 128) > 0);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isBlob() {
        return ((colFlag & 16) > 0);
    }

    /**
     * Returns the character set (if known) for this
     * field.
     *
     * @return the character set
     */
    public String getCharacterSet() {
        return this.charsetName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param conn DOCUMENT ME!
     */
    public void setConnection(Connection conn) {
        this.connection = conn;
        
		this.charsetName = this.connection.getEncoding();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getDatabaseName() {
        if ((this.databaseName == null) && (this.databaseNameStart != -1)
                && (this.databaseNameLength != -1)) {
            this.databaseName = getStringFromBytes(this.databaseNameStart,
                    this.databaseNameLength);
        }

        return this.databaseName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getFullName() {
        if (fullName == null) {
            StringBuffer fullNameBuf = new StringBuffer(getTableName().length()
                    + 1 + getName().length());
            fullNameBuf.append(tableName);

            // much faster to append a char than a String
            fullNameBuf.append('.');
            fullNameBuf.append(name);
            fullName = fullNameBuf.toString();
            fullNameBuf = null;
        }

        return fullName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getFullOriginalName() {
        getOriginalName();

        if (this.originalColumnName == null) {
            return null; // we don't have this information
        }

        if (fullName == null) {
            StringBuffer fullOriginalNameBuf = new StringBuffer(getOriginalTableName()
                                                                    .length()
                    + 1 + getOriginalName().length());
            fullOriginalNameBuf.append(this.originalTableName);

            // much faster to append a char than a String
            fullOriginalNameBuf.append('.');
            fullOriginalNameBuf.append(this.originalColumnName);
            this.fullOriginalName = fullOriginalNameBuf.toString();
            fullOriginalNameBuf = null;
        }

        return this.fullOriginalName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getLength() {
        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isMultipleKey() {
        return ((colFlag & 8) > 0);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getMysqlType() {
        return mysqlType;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getName() {
        if (this.name == null) {
            this.name = getStringFromBytes(this.nameStart, this.nameLength);
        }

        return name;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getOriginalName() {
        if ((this.originalColumnName == null)
                && (this.originalColumnNameStart != -1)
                && (this.originalColumnNameLength != -1)) {
            this.originalColumnName = getStringFromBytes(this.originalColumnNameStart,
                    this.originalColumnNameLength);
        }

        return this.originalColumnName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getOriginalTableName() {
        if ((this.originalTableName == null)
                && (this.originalTableNameStart != -1)
                && (this.originalTableNameLength != -1)) {
            this.originalTableName = getStringFromBytes(this.originalTableNameStart,
                    this.originalTableNameLength);
        }

        return this.originalTableName;
    }

    /**
     * Returns amount of correction that
     * should be applied to the precision value.
     *
     * Different versions of MySQL report different
     * precision values.
     *
     * @return the amount to adjust precision value by.
     */
    public int getPrecisionAdjustFactor() {
        return this.precisionAdjustFactor;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isPrimaryKey() {
        return ((colFlag & 2) > 0);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getSQLType() {
        return sqlType;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getTable() {
        return getTableName();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getTableName() {
        if (tableName == null) {
            tableName = getStringFromBytes(tableNameStart, tableNameLength);
        }

        return tableName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isUniqueKey() {
        return ((colFlag & 4) > 0);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isUnsigned() {
        return ((colFlag & 32) > 0);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isZeroFill() {
        return ((colFlag & 64) > 0);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String toString() {
    	return this.getDatabaseName() + " . " +  this.getTableName() + "(" + this.getOriginalTableName() + ") . " + this.getName() + "(" + this.getOriginalName() + ")";
       
    }

    int getDecimals() {
        return colDecimals;
    }

    boolean isNotNull() {
        return ((colFlag & 1) > 0);
    }

    /**
     * Create a string with the correct charset encoding from the
     * byte-buffer that contains the data for this field
     */
    private String getStringFromBytes(int stringStart, int stringLength) {
        if ((stringStart == -1) || (stringLength == -1)) {
            return null;
        }

        String stringVal = null;

        if (connection != null) {
            if (connection.useUnicode()) {
            	
                String encoding = this.connection.getCharacterSetMetadata();
                
                if (encoding == null) {
                	encoding = connection.getEncoding();
                }
                
                if (encoding != null) {
                   
					SingleByteCharsetConverter converter = null;
					
					if (this.connection != null) {
						converter = this.connection.getCharsetConverter(encoding);
					}
                    
                    if (converter != null) { // we have a converter
                        stringVal = converter.toString(buffer, stringStart,
                                stringLength);
                    } else {
                        // we have no converter, use JVM converter 
                        byte[] stringBytes = new byte[stringLength];

                        int endIndex = stringStart + stringLength;
                        int pos = 0;

                        for (int i = stringStart; i < endIndex; i++) {
                            stringBytes[pos++] = buffer[i];
                        }

                        try {
                            stringVal = new String(stringBytes, encoding);
                        } catch (UnsupportedEncodingException ue) {
                            throw new RuntimeException(
                                "Unsupported character encoding '" + encoding
                                + "'");
                        }
                    }
                } else {
                    // we have no encoding, use JVM standard charset
                    stringVal = StringUtils.toAsciiString(buffer, stringStart,
                            stringLength);
                }
            } else {
                // we are not using unicode, so use JVM standard charset 
                stringVal = StringUtils.toAsciiString(buffer, stringStart,
                        stringLength);
            }
        } else {
            // we don't have a connection, so punt 
            stringVal = StringUtils.toAsciiString(buffer, stringStart,
                    stringLength);
        }

        return stringVal;
    }
}
