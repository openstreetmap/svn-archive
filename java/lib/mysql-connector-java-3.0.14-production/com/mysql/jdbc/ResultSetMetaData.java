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

import java.sql.SQLException;
import java.sql.Types;


/**
 * A ResultSetMetaData object can be used to find out about the types and
 * properties of the columns in a ResultSet
 *
 * @see java.sql.ResultSetMetaData
 * @author Mark Matthews
 * @version $Id: ResultSetMetaData.java,v 1.12.2.7 2004/04/06 18:01:41 mmatthew Exp $
 */
public class ResultSetMetaData implements java.sql.ResultSetMetaData {
    Field[] fields;

    /**
            * Initialise for a result with a tuple set and
     * a field descriptor set
     *
     * @param fields the array of field descriptors

     */
    public ResultSetMetaData(Field[] fields) {
        this.fields = fields;
    }

    /**
     * Is the column automatically numbered (and thus read-only)
     *
     * MySQL Auto-increment columns are not read only,
     * so to conform to the spec, this method returns false.
     *
     * @param column the first column is 1, the second is 2...
     * @return true if so
     * @throws java.sql.SQLException if a database access error occurs
     */
    public boolean isAutoIncrement(int column) throws java.sql.SQLException {
        Field f = getField(column);

        return f.isAutoIncrement();
    }

    /**
     * Does a column's case matter? ASSUMPTION: Any field that is
     * not obviously case insensitive is assumed to be case sensitive
     *
     * @param column the first column is 1, the second is 2...
     * @return true if so
     * @throws java.sql.SQLException if a database access error occurs
     */
    public boolean isCaseSensitive(int column) throws java.sql.SQLException {
    	Field field = getField(column);
    	
        int sqlType = field.getSQLType();

        switch (sqlType) {
        case Types.BIT:
        case Types.TINYINT:
        case Types.SMALLINT:
        case Types.INTEGER:
        case Types.BIGINT:
        case Types.FLOAT:
        case Types.REAL:
        case Types.DOUBLE:
        case Types.DATE:
        case Types.TIME:
        case Types.TIMESTAMP:
            return false;
        	
        case Types.CHAR:
        case Types.VARCHAR:
        	
        	return field.isBinary();
        	    	
        default:
            return true;
        }
    }

    /**
     * What's a column's table's catalog name?
     *
     * @param column the first column is 1, the second is 2...
     * @return catalog name, or "" if not applicable
     * @throws java.sql.SQLException if a database access error occurs
     */
    public String getCatalogName(int column) throws java.sql.SQLException {
        Field f = getField(column);

        String database = f.getDatabaseName();

        return (database == null) ? "" : database;
    }

    //--------------------------JDBC 2.0-----------------------------------

    /**
     * JDBC 2.0
     *
     * <p>Return the fully qualified name of the Java class whose instances
     * are manufactured if ResultSet.getObject() is called to retrieve a value
     * from the column.  ResultSet.getObject() may return a subClass of the
     * class returned by this method.
     *
     * @param column the column number to retrieve information for
     * @return the fully qualified name of the Java class whose instances
     * are manufactured if ResultSet.getObject() is called to retrieve a value
     * from the column.
     *
     * @throws SQLException if an error occurs
     */
    public String getColumnClassName(int column) throws SQLException {
        Field f = getField(column);

        // From JDBC-3.0 spec
        //
        //  JDBC Type Java Object Type
        //
        // CHAR 			String
        // VARCHAR 			String
        // LONGVARCHAR 		String
        // NUMERIC 			java.math.BigDecimal
        // DECIMAL 			java.math.BigDecimal
        // BIT 				Boolean
        // BOOLEAN 			Boolean
        // TINYINT 			Integer
        // SMALLINT 		Integer
        // INTEGER 			Integer
        // BIGINT 			Long
        // REAL 			Float
        // FLOAT 			Double
        // DOUBLE 			Double
        // BINARY 			byte[]
        // VARBINARY 		byte[]
        // LONGVARBINARY 	byte[]
        // DATE 			java.sql.Date
        // TIME 			java.sql.Time
        // TIMESTAMP 		java.sql.Timestamp
        // DISTINCT 		Object type of underlying type
        // CLOB 			Clob
        // BLOB 			Blob
        // ARRAY 			Array
        // STRUCT 			Struct or SQLData
        // REF 				Ref
        // DATALINK 		java.net.URL
        // JAVA_OBJECT 		underlying Java class
         
        switch (f.getSQLType()) {
        case Types.BIT:
        case Types.BOOLEAN:
            return "java.lang.Boolean";

        case Types.TINYINT:

            return "java.lang.Integer";
           
        case Types.SMALLINT:

            return "java.lang.Integer";

        case Types.INTEGER:

            if (f.isUnsigned()) {
                return "java.lang.Long";
            } else {
                return "java.lang.Integer";
            }

        case Types.BIGINT:
        	
            return "java.lang.Long";

        case Types.DECIMAL:
        case Types.NUMERIC:
        	
            return "java.math.BigDecimal";

        case Types.REAL:
        
            return "java.lang.Float";
        
        case Types.FLOAT:
        case Types.DOUBLE:
        	
            return "java.lang.Double";

        case Types.CHAR:
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
        	
            return "java.lang.String";

        case Types.BINARY:
        case Types.VARBINARY:
        case Types.LONGVARBINARY:

            if (!f.isBlob()) {
                return "java.lang.String";
            } else if (!f.isBinary()) {
                return "java.lang.String";
            } else {
                return "[B";
            }

        case Types.DATE:
        	
            return "java.sql.Date";

        case Types.TIME:
        	
            return "java.sql.Time";

        case Types.TIMESTAMP:
        	
            return "java.sql.Timestamp";

        default:
        	
            return "java.lang.Object";
        }
    }

    /**
     * What's the MySQL character set name for the given column?
     * 
     * @param column the first column is 1, the second is 2, etc.
     * 
     * @return the MySQL character set name for the given column
     * @throws SQLException if an invalid column index is given.
     */
    public String getColumnCharacterSet(int column) throws SQLException {
    	return getField(column).getCharacterSet();
    }
    
    /**
     * What's the Java character encoding name for the given column?
     * 
     * @param column the first column is 1, the second is 2, etc.
     * 
     * @return the Java character encoding name for the given column,
     * or null if no Java character encoding maps to the MySQL character set
     * for the given column.
     * 
     * @throws SQLException if an invalid column index is given.
     */
    public String getColumnCharacterEncoding(int column) throws SQLException {
    	String mysqlName = getColumnCharacterSet(column);
    	
    	String javaName = null;
    	
    	if (mysqlName != null) {
    		javaName = (String)CharsetMapping.MYSQL_TO_JAVA_CHARSET_MAP.get(mysqlName);
    	}
    	
    	return javaName;
    }
    
    /**
     * Whats the number of columns in the ResultSet?
     *
     * @return the number
     * @throws java.sql.SQLException if a database access error occurs
     */
    public int getColumnCount() throws java.sql.SQLException {
        return fields.length;
    }

    /**
     * What is the column's normal maximum width in characters?
     *
     * @param column the first column is 1, the second is 2, etc.
     * @return the maximum width
     * @throws java.sql.SQLException if a database access error occurs
     */
    public int getColumnDisplaySize(int column) throws java.sql.SQLException {
        return getField(column).getLength();
    }

    /**
     * What is the suggested column title for use in printouts and
     * displays?
     *
     * @param column the first column is 1, the second is 2, etc.
     * @return the column label
     * @throws java.sql.SQLException if a database access error occurs
     */
    public String getColumnLabel(int column) throws java.sql.SQLException {
        return getColumnName(column);
    }

    /**
     * What's a column's name?
     *
     * @param column the first column is 1, the second is 2, etc.
     * @return the column name
     * @throws java.sql.SQLException if a databvase access error occurs
     */
    public String getColumnName(int column) throws java.sql.SQLException {
        return getField(column).getName();
    }

    /**
     * What is a column's SQL Type? (java.sql.Type int)
     *
     * @param column the first column is 1, the second is 2, etc.
     * @return the java.sql.Type value
     * @throws java.sql.SQLException if a database access error occurs
     * @see java.sql.Types
     */
    public int getColumnType(int column) throws java.sql.SQLException {
        return getField(column).getSQLType();
    }

    /**
     * Whats is the column's data source specific type name?
     *
     * @param column the first column is 1, the second is 2, etc.
     * @return the type name
     * @throws java.sql.SQLException if a database access error occurs
     */
    public String getColumnTypeName(int column) throws java.sql.SQLException {
        int mysqlType = getField(column).getMysqlType();

        switch (mysqlType) {
        case MysqlDefs.FIELD_TYPE_DECIMAL:
            return "DECIMAL";

        case MysqlDefs.FIELD_TYPE_TINY:
            return "TINY";

        case MysqlDefs.FIELD_TYPE_SHORT:
            return "SHORT";

        case MysqlDefs.FIELD_TYPE_LONG:
            return "LONG";

        case MysqlDefs.FIELD_TYPE_FLOAT:
            return "FLOAT";

        case MysqlDefs.FIELD_TYPE_DOUBLE:
            return "DOUBLE";

        case MysqlDefs.FIELD_TYPE_NULL:
            return "NULL";

        case MysqlDefs.FIELD_TYPE_TIMESTAMP:
            return "TIMESTAMP";

        case MysqlDefs.FIELD_TYPE_LONGLONG:
            return "LONGLONG";

        case MysqlDefs.FIELD_TYPE_INT24:
            return "INT";

        case MysqlDefs.FIELD_TYPE_DATE:
            return "DATE";

        case MysqlDefs.FIELD_TYPE_TIME:
            return "TIME";

        case MysqlDefs.FIELD_TYPE_DATETIME:
            return "DATETIME";

        case MysqlDefs.FIELD_TYPE_TINY_BLOB:
            return "TINYBLOB";

        case MysqlDefs.FIELD_TYPE_MEDIUM_BLOB:
            return "MEDIUMBLOB";

        case MysqlDefs.FIELD_TYPE_LONG_BLOB:
            return "LONGBLOB";

        case MysqlDefs.FIELD_TYPE_BLOB:

            if (getField(column).isBinary()) {
                return "BLOB";
            } else {
                return "TEXT";
            }

        case MysqlDefs.FIELD_TYPE_VAR_STRING:
            return "VARCHAR";

        case MysqlDefs.FIELD_TYPE_STRING:
            return "CHAR";

        case MysqlDefs.FIELD_TYPE_ENUM:
            return "ENUM";

        case MysqlDefs.FIELD_TYPE_SET:
            return "SET";
           
        case MysqlDefs.FIELD_TYPE_YEAR:
        	return "YEAR";

        default:
            return "UNKNOWN";
        }
    }

    /**
     * Is the column a cash value?
     *
     * @param column the first column is 1, the second is 2...
     * @return true if its a cash column
     * @throws java.sql.SQLException if a database access error occurs
     */
    public boolean isCurrency(int column) throws java.sql.SQLException {
        return false;
    }

    /**
     * Will a write on this column definately succeed?
     *
     * @param column the first column is 1, the second is 2, etc..
     * @return true if so
     * @throws java.sql.SQLException if a database access error occurs
     */
    public boolean isDefinitelyWritable(int column)
        throws java.sql.SQLException {
        return isWritable(column);
    }

    /**
     * Can you put a NULL in this column?
     *
     * @param column the first column is 1, the second is 2...
     * @return one of the columnNullable values
     * @throws java.sql.SQLException if a database access error occurs
     */
    public int isNullable(int column) throws java.sql.SQLException {
        if (!getField(column).isNotNull()) {
            return java.sql.ResultSetMetaData.columnNullable;
        } else {
            return java.sql.ResultSetMetaData.columnNoNulls;
        }
    }

    /**
     * What is a column's number of decimal digits.
     *
     * @param column the first column is 1, the second is 2...
     * @return the precision
     * @throws java.sql.SQLException if a database access error occurs
     */
    public int getPrecision(int column) throws java.sql.SQLException {
        Field f = getField(column);

        if (isDecimalType(f.getSQLType())) {
            if (f.getDecimals() > 0) {
                return f.getLength() - 1 + f.getPrecisionAdjustFactor();
            }

            return f.getLength() + f.getPrecisionAdjustFactor();
        }

        return 0;
    }

    /**
     * Is the column definitely not writable?
     *
     * @param column the first column is 1, the second is 2, etc.
     * @return true if so
     * @throws java.sql.SQLException if a database access error occurs
     */
    public boolean isReadOnly(int column) throws java.sql.SQLException {
        return false;
    }

    /**
     * What is a column's number of digits to the right of the
     * decimal point?
     *
     * @param column the first column is 1, the second is 2...
     * @return the scale
     * @throws java.sql.SQLException if a database access error occurs
     */
    public int getScale(int column) throws java.sql.SQLException {
        Field f = getField(column);

        if (isDecimalType(f.getSQLType())) {
            return f.getDecimals();
        }

        return 0;
    }

    /**
     * What is a column's table's schema?  This relies on us knowing
     * the table name.
     *
     * The JDBC specification allows us to return "" if this is not
     * applicable.
     *
     * @param column the first column is 1, the second is 2...
     * @return the Schema
     * @throws java.sql.SQLException if a database access error occurs
     */
    public String getSchemaName(int column) throws java.sql.SQLException {
        return "";
    }

    /**
     * Can the column be used in a WHERE clause?  Basically for
     * this, I split the functions into two types: recognised
     * types (which are always useable), and OTHER types (which
     * may or may not be useable).  The OTHER types, for now, I
     * will assume they are useable.  We should really query the
     * catalog to see if they are useable.
     *
     * @param column the first column is 1, the second is 2...
     * @return true if they can be used in a WHERE clause
     * @throws java.sql.SQLException if a database access error occurs
     */
    public boolean isSearchable(int column) throws java.sql.SQLException {
        return true;
    }

    /**
     * Is the column a signed number?
     *
     * @param column the first column is 1, the second is 2...
     * @return true if so
     * @throws java.sql.SQLException if a database access error occurs
     */
    public boolean isSigned(int column) throws java.sql.SQLException {
        Field f = getField(column);
        int sqlType = f.getSQLType();

        switch (sqlType) {
        case Types.TINYINT:
        case Types.SMALLINT:
        case Types.INTEGER:
        case Types.BIGINT:
        case Types.FLOAT:
        case Types.REAL:
        case Types.DOUBLE:
        case Types.NUMERIC:
        case Types.DECIMAL:
            return !f.isUnsigned();

        case Types.DATE:
        case Types.TIME:
        case Types.TIMESTAMP:
            return false;

        default:
            return false;
        }
    }

    /**
     * Whats a column's table's name?
     *
     * @param column the first column is 1, the second is 2...
     * @return column name, or "" if not applicable
     * @throws java.sql.SQLException if a database access error occurs
     */
    public String getTableName(int column) throws java.sql.SQLException {
        return getField(column).getTableName();
    }

    /**
     * Is it possible for a write on the column to succeed?
     *
     * @param column the first column is 1, the second is 2, etc.
     * @return true if so
     * @throws java.sql.SQLException if a database access error occurs
     */
    public boolean isWritable(int column) throws java.sql.SQLException {
        return !isReadOnly(column);
    }

    // *********************************************************************
    //
    //                END OF PUBLIC INTERFACE
    //
    // *********************************************************************

    /**
     * Returns the field instance for the given column index
     *
     * @param columnIndex the column number to retrieve a field instance for
     * @return the field instance for the given column index
     *
     * @throws java.sql.SQLException if an error occurs
     */
    protected Field getField(int columnIndex) throws java.sql.SQLException {
        if ((columnIndex < 1) || (columnIndex > fields.length)) {
            throw new java.sql.SQLException("Column index out of range.",
                SQLError.SQL_STATE_INVALID_COLUMN_NUMBER);
        }

        return fields[columnIndex - 1];
    }

    /**
     * Checks if the SQL Type is a Decimal/Number Type
     * @param type SQL Type
     */
    private static final boolean isDecimalType(int type) {
        switch (type) {
        case Types.BIT:
        case Types.TINYINT:
        case Types.SMALLINT:
        case Types.INTEGER:
        case Types.BIGINT:
        case Types.FLOAT:
        case Types.REAL:
        case Types.DOUBLE:
        case Types.NUMERIC:
        case Types.DECIMAL:
            return true;
        }

        return false;
    }
}
