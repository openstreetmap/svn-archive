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

import java.sql.Types;


/**
 * MysqlDefs contains many values that are needed for communication with the
 * MySQL server.
 *
 * @author Mark Matthews
 * @version $Id: MysqlDefs.java,v 1.6.2.9 2004/04/27 16:12:27 mmatthew Exp $
 */
final class MysqlDefs {
    //
    // Constants defined from mysql
    //
    // DB Operations
    static final int SLEEP = 0;
    static final int QUIT = 1;
    static final int INIT_DB = 2;
    static final int QUERY = 3;
    static final int FIELD_LIST = 4;
    static final int CREATE_DB = 5;
    static final int DROP_DB = 6;
    static final int RELOAD = 7;
    static final int SHUTDOWN = 8;
    static final int STATISTICS = 9;
    static final int PROCESS_INFO = 10;
    static final int CONNECT = 11;
    static final int PROCESS_KILL = 12;
    static final int DEBUG = 13;
    static final int PING = 14;
    static final int TIME = 15;
    static final int DELAYED_INSERT = 16;
    static final int CHANGE_USER = 17;
    static final int COM_BINLOG_DUMP = 18;
    static final int COM_TABLE_DUMP = 19;
    static final int COM_CONNECT_OUT = 20;
    static final int COM_REGISTER_SLAVE = 21;
    static final int COM_PREPARE = 22;
    static final int COM_EXECUTE = 23;
    static final int COM_LONG_DATA = 24;

    // Data Types
    static final int FIELD_TYPE_DECIMAL = 0;
    static final int FIELD_TYPE_TINY = 1;
    static final int FIELD_TYPE_SHORT = 2;
    static final int FIELD_TYPE_LONG = 3;
    static final int FIELD_TYPE_FLOAT = 4;
    static final int FIELD_TYPE_DOUBLE = 5;
    static final int FIELD_TYPE_NULL = 6;
    static final int FIELD_TYPE_TIMESTAMP = 7;
    static final int FIELD_TYPE_LONGLONG = 8;
    static final int FIELD_TYPE_INT24 = 9;
    static final int FIELD_TYPE_DATE = 10;
    static final int FIELD_TYPE_TIME = 11;
    static final int FIELD_TYPE_DATETIME = 12;

    // Newer data types
    static final int FIELD_TYPE_YEAR = 13;
    static final int FIELD_TYPE_NEWDATE = 14;
    static final int FIELD_TYPE_ENUM = 247;
    static final int FIELD_TYPE_SET = 248;

    // Older data types
    static final int FIELD_TYPE_TINY_BLOB = 249;
    static final int FIELD_TYPE_MEDIUM_BLOB = 250;
    static final int FIELD_TYPE_LONG_BLOB = 251;
    static final int FIELD_TYPE_BLOB = 252;
    static final int FIELD_TYPE_VAR_STRING = 253;
    static final int FIELD_TYPE_STRING = 254;
    static final int ER_WARNING_NOT_COMPLETE_ROLLBACK = 1196;

    // Limitations
    static final int MAX_ROWS = 50000000; // From the MySQL FAQ
    static final String MIN_DOUBLE_VAL_STRING = "-1.7976931348623157E+308";
    static final String MAX_DOUBLE_VAL_STRING = "1.7976931348623157E+308";
    static final String MIN_FLOAT_VAL_STRING = "-3.402823466E+38";
    static final String MAX_FLOAT_VAL_STRING = "3.402823466E+38";
    static final String NAN_VAL_STRING = "0";
    static final int COM_CHANGE_USER = 17;

    /** 
     * Used to indicate that the server sent no field-level
     * character set information, so the driver should use
     * the connection-level character encoding instead.
     */
    public static final int NO_CHARSET_INFO = -1;

    /**
     * Maps the given MySQL type to the correct JDBC type.
     *
     * @param mysqlType DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    static int mysqlToJavaType(int mysqlType) {
        int jdbcType;

        switch (mysqlType) {
        case MysqlDefs.FIELD_TYPE_DECIMAL:
            jdbcType = Types.DECIMAL;

            break;

        case MysqlDefs.FIELD_TYPE_TINY:
            jdbcType = Types.TINYINT;

            break;

        case MysqlDefs.FIELD_TYPE_SHORT:
            jdbcType = Types.SMALLINT;

            break;

        case MysqlDefs.FIELD_TYPE_LONG:
            jdbcType = Types.INTEGER;

            break;

        case MysqlDefs.FIELD_TYPE_FLOAT:
            jdbcType = Types.REAL;

            break;

        case MysqlDefs.FIELD_TYPE_DOUBLE:
            jdbcType = Types.DOUBLE;

            break;

        case MysqlDefs.FIELD_TYPE_NULL:
            jdbcType = Types.NULL;

            break;

        case MysqlDefs.FIELD_TYPE_TIMESTAMP:
            jdbcType = Types.TIMESTAMP;

            break;

        case MysqlDefs.FIELD_TYPE_LONGLONG:
            jdbcType = Types.BIGINT;

            break;

        case MysqlDefs.FIELD_TYPE_INT24:
            jdbcType = Types.INTEGER;

            break;

        case MysqlDefs.FIELD_TYPE_DATE:
            jdbcType = Types.DATE;

            break;

        case MysqlDefs.FIELD_TYPE_TIME:
            jdbcType = Types.TIME;

            break;

        case MysqlDefs.FIELD_TYPE_DATETIME:
            jdbcType = Types.TIMESTAMP;

            break;

        case MysqlDefs.FIELD_TYPE_YEAR:
            jdbcType = Types.DATE;

            break;

        case MysqlDefs.FIELD_TYPE_NEWDATE:
            jdbcType = Types.DATE;

            break;

        case MysqlDefs.FIELD_TYPE_ENUM:
            jdbcType = Types.CHAR;

            break;

        case MysqlDefs.FIELD_TYPE_SET:
            jdbcType = Types.CHAR;

            break;

        case MysqlDefs.FIELD_TYPE_TINY_BLOB:
            jdbcType = Types.VARBINARY;

            break;

        case MysqlDefs.FIELD_TYPE_MEDIUM_BLOB:
            jdbcType = Types.LONGVARBINARY;

            break;

        case MysqlDefs.FIELD_TYPE_LONG_BLOB:
            jdbcType = Types.LONGVARBINARY;

            break;

        case MysqlDefs.FIELD_TYPE_BLOB:
            jdbcType = Types.LONGVARBINARY;

            break;

        case MysqlDefs.FIELD_TYPE_VAR_STRING:
            jdbcType = Types.VARCHAR;

            break;

        case MysqlDefs.FIELD_TYPE_STRING:
            jdbcType = Types.CHAR;

            break;

        default:
            jdbcType = Types.VARCHAR;
        }

        return jdbcType;
    }

    /**
     * Maps the given MySQL type to the correct JDBC type.
     * 
     * @param mysqlType the mysql type name to map.
     */
    static int mysqlToJavaType(String mysqlType) {
        if (mysqlType.equalsIgnoreCase("TINYINT")) {
            return mysqlToJavaType(FIELD_TYPE_TINY);
        } else if (mysqlType.equalsIgnoreCase("SMALLINT")) {
            return mysqlToJavaType(FIELD_TYPE_SHORT);
        } else if (mysqlType.equalsIgnoreCase("MEDIUMINT")) {
            return mysqlToJavaType(FIELD_TYPE_INT24);
        } else if (mysqlType.equalsIgnoreCase("INT") || mysqlType.equalsIgnoreCase("INTEGER")) {
            return mysqlToJavaType(FIELD_TYPE_LONG);
        } else if (mysqlType.equalsIgnoreCase("BIGINT")) {
            return mysqlToJavaType(FIELD_TYPE_LONGLONG);
        } else if (mysqlType.equalsIgnoreCase("INT24")) {
            return mysqlToJavaType(FIELD_TYPE_INT24);
        } else if (mysqlType.equalsIgnoreCase("REAL")) {
            return mysqlToJavaType(FIELD_TYPE_DOUBLE);
        } else if (mysqlType.equalsIgnoreCase("FLOAT")) {
            return mysqlToJavaType(FIELD_TYPE_FLOAT);
        } else if (mysqlType.equalsIgnoreCase("DECIMAL")) {
            return mysqlToJavaType(FIELD_TYPE_DECIMAL);
        } else if (mysqlType.equalsIgnoreCase("NUMERIC")) {
            return mysqlToJavaType(FIELD_TYPE_DECIMAL);
        } else if (mysqlType.equalsIgnoreCase("DOUBLE")) {
            return mysqlToJavaType(FIELD_TYPE_DOUBLE);
        } else if (mysqlType.equalsIgnoreCase("CHAR")) {
            return mysqlToJavaType(FIELD_TYPE_STRING);
        } else if (mysqlType.equalsIgnoreCase("VARCHAR")) {
            return mysqlToJavaType(FIELD_TYPE_VAR_STRING);
        } else if (mysqlType.equalsIgnoreCase("DATE")) {
            return mysqlToJavaType(FIELD_TYPE_DATE);
        } else if (mysqlType.equalsIgnoreCase("TIME")) {
            return mysqlToJavaType(FIELD_TYPE_TIME);
        } else if (mysqlType.equalsIgnoreCase("YEAR")) {
        	return mysqlToJavaType(FIELD_TYPE_YEAR);
        } else if (mysqlType.equalsIgnoreCase("TIMESTAMP")) {
            return mysqlToJavaType(FIELD_TYPE_TIMESTAMP);
        } else if (mysqlType.equalsIgnoreCase("DATETIME")) {
            return mysqlToJavaType(FIELD_TYPE_DATETIME);
        } else if (mysqlType.equalsIgnoreCase("TINYBLOB")) {
            return java.sql.Types.BINARY;
        } else if (mysqlType.equalsIgnoreCase("BLOB")) {
            return java.sql.Types.LONGVARBINARY;
        } else if (mysqlType.equalsIgnoreCase("MEDIUMBLOB")) {
            return java.sql.Types.LONGVARBINARY;
        } else if (mysqlType.equalsIgnoreCase("LONGBLOB")) {
            return java.sql.Types.LONGVARBINARY;
        } else if (mysqlType.equalsIgnoreCase("TINYTEXT")) {
            return java.sql.Types.VARCHAR;
        } else if (mysqlType.equalsIgnoreCase("TEXT")) {
            return java.sql.Types.LONGVARCHAR;
        } else if (mysqlType.equalsIgnoreCase("MEDIUMTEXT")) {
            return java.sql.Types.LONGVARCHAR;
        } else if (mysqlType.equalsIgnoreCase("LONGTEXT")) {
        	return java.sql.Types.LONGVARCHAR;
        } else if (mysqlType.equalsIgnoreCase("ENUM")) {
            return mysqlToJavaType(FIELD_TYPE_ENUM);
        } else if (mysqlType.equalsIgnoreCase("SET")) {
            return mysqlToJavaType(FIELD_TYPE_SET);
        }

        // Punt
        return java.sql.Types.OTHER;
    }
}
