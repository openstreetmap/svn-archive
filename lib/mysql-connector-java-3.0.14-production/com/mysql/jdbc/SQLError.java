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

import java.util.Hashtable;


/**
 * SQLError is a utility class that maps MySQL error codes to X/Open error
 * codes as is required by the JDBC spec.
 *
 * @author Mark Matthews
 * @version $Id: SQLError.java,v 1.6.2.5 2004/03/05 19:04:35 mmatthew Exp $
 */
public class SQLError {
    
    public static final String SQL_STATE_TIMEOUT_EXPIRED = "S1T00";

    public static final String SQL_STATE_DRIVER_NOT_CAPABLE = "S1C00";

    public static final String SQL_STATE_ILLEGAL_ARGUMENT = "S1009";

    public static final String SQL_STATE_INVALID_COLUMN_NUMBER = "S1002";

    public static final String SQL_STATE_MEMORY_ALLOCATION_FAILURE = "S1001";

    public static final String SQL_STATE_GENERAL_ERROR = "S1000";

    public static final String SQL_STATE_NO_DEFAULT_FOR_COLUMN = "S0023";

    public static final String SQL_STATE_COLUMN_NOT_FOUND = "S0022";

    public static final String SQL_STATE_COLUMN_ALREADY_EXISTS = "S0021";

    public static final String SQL_STATE_INDEX_NOT_FOUND = "S0012";

    public static final String SQL_STATE_INDEX_ALREADY_EXISTS = "S0011";

    public static final String SQL_STATE_BASE_TABLE_NOT_FOUND = "S0002";

    public static final String SQL_STATE_BASE_TABLE_OR_VIEW_ALREADY_EXISTS = "S0001";

    public static final String SQL_STATE_BASE_TABLE_OR_VIEW_NOT_FOUND = "42S02";

    public static final String SQL_STATE_SYNTAX_ERROR = "42000";

    public static final String SQL_STATE_DATETIME_FIELD_OVERFLOW = "22008";

    public static final String SQL_STATE_NUMERIC_VALUE_OUT_OF_RANGE = "22003";

    public static final String SQL_STATE_INSERT_VALUE_LIST_NO_MATCH_COL_LIST = "21S01";

    public static final String SQL_STATE_DIVISION_BY_ZERO = "22012";

    public static final String SQL_STATE_INVALID_AUTH_SPEC = "28000";

    public static final String SQL_STATE_DEADLOCK = "41000";

    public static final String SQL_STATE_CONNECTION_FAIL_DURING_TX = "08007";

    public static final String SQL_STATE_CONNECTION_REJECTED = "08004";

    public static final String SQL_STATE_UNABLE_TO_CONNECT_TO_DATASOURCE = "08001";

    public static final String SQL_STATE_CONNECTION_IN_USE = "08002";

    public static final String SQL_STATE_CONNECTION_NOT_OPEN = "08003";

    public static final String SQL_STATE_WRONG_NO_OF_PARAMETERS = "07001";

    public static final String SQL_STATE_MORE_THAN_ONE_ROW_UPDATED_OR_DELETED = "01S04";

    public static final String SQL_STATE_NO_ROWS_UPDATED_OR_DELETED = "01S03";

    public static final String SQL_STATE_ERROR_IN_ROW = "01S01";

    public static final String SQL_STATE_COMMUNICATION_LINK_FAILURE = "08S01";

    public static final String SQL_STATE_INVALID_CONNECTION_ATTRIBUTE = "01S00";

    public static final String SQL_STATE_PRIVILEGE_NOT_REVOKED = "01006";

    public static final String SQL_STATE_DATE_TRUNCATED = "01004";

    public static final String SQL_STATE_DISCONNECT_ERROR = "01002";
    
    public static final String SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION = "23000";
    
    private static Hashtable mysqlToSqlState;
    private static Hashtable sqlStateMessages;

    static {
        sqlStateMessages = new Hashtable();
        sqlStateMessages.put(SQL_STATE_DISCONNECT_ERROR, "Disconnect error");
        sqlStateMessages.put(SQL_STATE_DATE_TRUNCATED, "Data truncated");
        sqlStateMessages.put(SQL_STATE_PRIVILEGE_NOT_REVOKED,
            "Privilege not revoked");
        sqlStateMessages.put(SQL_STATE_INVALID_CONNECTION_ATTRIBUTE,
            "Invalid connection string attribute");
        sqlStateMessages.put(SQL_STATE_ERROR_IN_ROW, "Error in row");
        sqlStateMessages.put(SQL_STATE_NO_ROWS_UPDATED_OR_DELETED,
            "No rows updated or deleted");
        sqlStateMessages.put(SQL_STATE_MORE_THAN_ONE_ROW_UPDATED_OR_DELETED,
            "More than one row updated or deleted");
        sqlStateMessages.put(SQL_STATE_WRONG_NO_OF_PARAMETERS,
            "Wrong number of parameters");
        sqlStateMessages.put(SQL_STATE_UNABLE_TO_CONNECT_TO_DATASOURCE,
            "Unable to connect to data source");
        sqlStateMessages.put(SQL_STATE_CONNECTION_IN_USE, "Connection in use");
        sqlStateMessages.put(SQL_STATE_CONNECTION_NOT_OPEN,
            "Connection not open");
        sqlStateMessages.put(SQL_STATE_CONNECTION_REJECTED,
            "Data source rejected establishment of connection");
        sqlStateMessages.put(SQL_STATE_CONNECTION_FAIL_DURING_TX,
            "Connection failure during transaction");
        sqlStateMessages.put(SQL_STATE_COMMUNICATION_LINK_FAILURE,
            "Communication link failure");
        sqlStateMessages.put(SQL_STATE_INSERT_VALUE_LIST_NO_MATCH_COL_LIST,
            "Insert value list does not match column list");
        sqlStateMessages.put(SQL_STATE_NUMERIC_VALUE_OUT_OF_RANGE,
            "Numeric value out of range");
        sqlStateMessages.put(SQL_STATE_DATETIME_FIELD_OVERFLOW,
            "Datetime field overflow");
        sqlStateMessages.put(SQL_STATE_DIVISION_BY_ZERO, "Division by zero");
        sqlStateMessages.put(SQL_STATE_DEADLOCK,
            "Deadlock found when trying to get lock; Try restarting transaction");
        sqlStateMessages.put(SQL_STATE_INVALID_AUTH_SPEC,
            "Invalid authorization specification");
        sqlStateMessages.put(SQL_STATE_SYNTAX_ERROR,
            "Syntax error or access violation");
        sqlStateMessages.put(SQL_STATE_BASE_TABLE_OR_VIEW_NOT_FOUND,
            "Base table or view not found");
        sqlStateMessages.put(SQL_STATE_BASE_TABLE_OR_VIEW_ALREADY_EXISTS,
            "Base table or view already exists");
        sqlStateMessages.put(SQL_STATE_BASE_TABLE_NOT_FOUND,
            "Base table not found");
        sqlStateMessages.put(SQL_STATE_INDEX_ALREADY_EXISTS,
            "Index already exists");
        sqlStateMessages.put(SQL_STATE_INDEX_NOT_FOUND, "Index not found");
        sqlStateMessages.put(SQL_STATE_COLUMN_ALREADY_EXISTS,
            "Column already exists");
        sqlStateMessages.put(SQL_STATE_COLUMN_NOT_FOUND, "Column not found");
        sqlStateMessages.put(SQL_STATE_NO_DEFAULT_FOR_COLUMN,
            "No default for column");
        sqlStateMessages.put(SQL_STATE_GENERAL_ERROR, "General error");
        sqlStateMessages.put(SQL_STATE_MEMORY_ALLOCATION_FAILURE,
            "Memory allocation failure");
        sqlStateMessages.put(SQL_STATE_INVALID_COLUMN_NUMBER,
            "Invalid column number");
        sqlStateMessages.put(SQL_STATE_ILLEGAL_ARGUMENT,
            "Invalid argument value");
        sqlStateMessages.put(SQL_STATE_DRIVER_NOT_CAPABLE, "Driver not capable");
        sqlStateMessages.put(SQL_STATE_TIMEOUT_EXPIRED, "Timeout expired");
        sqlStateMessages.put(SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION, "Duplicate key or integrity constraint violation");
        
        //
        // Map MySQL error codes to X/Open error codes
        //
        mysqlToSqlState = new Hashtable();

        //
        // Communications Errors
        //
        //		ER_CON_COUNT_ERROR 1040
        // ER_BAD_HOST_ERROR 1042
        // ER_HANDSHAKE_ERROR 1043
        // ER_UNKNOWN_COM_ERROR 1047
        // ER_IPSOCK_ERROR 1081
        //
        mysqlToSqlState.put(new Integer(1040), SQL_STATE_CONNECTION_REJECTED);
        mysqlToSqlState.put(new Integer(1042), SQL_STATE_CONNECTION_REJECTED);
        mysqlToSqlState.put(new Integer(1043), SQL_STATE_CONNECTION_REJECTED);
        mysqlToSqlState.put(new Integer(1047),
            SQL_STATE_COMMUNICATION_LINK_FAILURE);
        mysqlToSqlState.put(new Integer(1081),
            SQL_STATE_COMMUNICATION_LINK_FAILURE);

        // ER_HOST_IS_BLOCKED 1129
        // ER_HOST_NOT_PRIVILEGED 1130
        mysqlToSqlState.put(new Integer(1129), SQL_STATE_CONNECTION_REJECTED);
        mysqlToSqlState.put(new Integer(1130), SQL_STATE_CONNECTION_REJECTED);

        //
        // Authentication Errors
        //
        // ER_ACCESS_DENIED_ERROR 1045
        //
        mysqlToSqlState.put(new Integer(1045), SQL_STATE_INVALID_AUTH_SPEC);

        //
        // Resource errors
        //
        // ER_CANT_CREATE_FILE 1004
        // ER_CANT_CREATE_TABLE 1005
        // ER_CANT_LOCK 1015
        // ER_DISK_FULL 1021
        // ER_OUT_OF_RESOURCES 1041
        //
        // Out-of-memory errors
        //
        // ER_OUTOFMEMORY 1037
        // ER_OUT_OF_SORTMEMORY 1038
        //
        mysqlToSqlState.put(new Integer(1037),
            SQL_STATE_MEMORY_ALLOCATION_FAILURE);
        mysqlToSqlState.put(new Integer(1038),
            SQL_STATE_MEMORY_ALLOCATION_FAILURE);

        //
        // Syntax Errors
        //
        // ER_PARSE_ERROR 1064
        // ER_EMPTY_QUERY 1065
        //
        mysqlToSqlState.put(new Integer(1064), SQL_STATE_SYNTAX_ERROR);
        mysqlToSqlState.put(new Integer(1065), SQL_STATE_SYNTAX_ERROR);

        //
        // Invalid argument errors
        //
        // ER_WRONG_FIELD_WITH_GROUP 1055
        // ER_WRONG_GROUP_FIELD 1056
        // ER_WRONG_SUM_SELECT 1057
        // ER_TOO_LONG_IDENT 1059
        // ER_DUP_FIELDNAME 1060
        // ER_DUP_KEYNAME 1061
        // ER_DUP_ENTRY 1062
        // ER_WRONG_FIELD_SPEC 1063
        // ER_NONUNIQ_TABLE 1066
        // ER_INVALID_DEFAULT 1067
        // ER_MULTIPLE_PRI_KEY 1068
        // ER_TOO_MANY_KEYS 1069
        // ER_TOO_MANY_KEY_PARTS 1070
        // ER_TOO_LONG_KEY 1071
        // ER_KEY_COLUMN_DOES_NOT_EXIST 1072
        // ER_BLOB_USED_AS_KEY 1073
        // ER_TOO_BIG_FIELDLENGTH 1074
        // ER_WRONG_AUTO_KEY 1075
        // ER_NO_SUCH_INDEX 1082
        // ER_WRONG_FIELD_TERMINATORS 1083
        // ER_BLOBS_AND_NO_TERMINATED 1084
        //
        mysqlToSqlState.put(new Integer(1055), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(new Integer(1056), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(new Integer(1057), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(new Integer(1059), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(new Integer(1060), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(new Integer(1061), SQL_STATE_ILLEGAL_ARGUMENT);
        
        mysqlToSqlState.put(new Integer(1062), SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        
        mysqlToSqlState.put(new Integer(1063), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(new Integer(1066), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(new Integer(1067), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(new Integer(1068), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(new Integer(1069), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(new Integer(1070), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(new Integer(1071), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(new Integer(1072), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(new Integer(1073), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(new Integer(1074), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(new Integer(1075), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(new Integer(1082), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(new Integer(1083), SQL_STATE_ILLEGAL_ARGUMENT);
        mysqlToSqlState.put(new Integer(1084), SQL_STATE_ILLEGAL_ARGUMENT);

        //
        // ER_WRONG_VALUE_COUNT 1058
        //
        mysqlToSqlState.put(new Integer(1058),
            SQL_STATE_INSERT_VALUE_LIST_NO_MATCH_COL_LIST);

        // ER_CANT_CREATE_DB 1006
        // ER_DB_CREATE_EXISTS 1007
        // ER_DB_DROP_EXISTS 1008
        // ER_DB_DROP_DELETE 1009
        // ER_DB_DROP_RMDIR 1010
        // ER_CANT_DELETE_FILE 1011
        // ER_CANT_FIND_SYSTEM_REC 1012
        // ER_CANT_GET_STAT 1013
        // ER_CANT_GET_WD 1014
        // ER_UNEXPECTED_EOF 1039
        // ER_CANT_OPEN_FILE 1016
        // ER_FILE_NOT_FOUND 1017
        // ER_CANT_READ_DIR 1018
        // ER_CANT_SET_WD 1019
        // ER_CHECKREAD 1020
        // ER_DUP_KEY 1022
        // ER_ERROR_ON_CLOSE 1023
        // ER_ERROR_ON_READ 1024
        // ER_ERROR_ON_RENAME 1025
        // ER_ERROR_ON_WRITE 1026
        // ER_FILE_USED 1027
        // ER_FILSORT_ABORT 1028
        // ER_FORM_NOT_FOUND 1029
        // ER_GET_ERRNO 1030
        // ER_ILLEGAL_HA 1031
        // ER_KEY_NOT_FOUND 1032
        // ER_NOT_FORM_FILE 1033
        // ER_DBACCESS_DENIED_ERROR 1044
        // ER_NO_DB_ERROR 1046
        // ER_BAD_NULL_ERROR 1048
        // ER_BAD_DB_ERROR 1049
        // ER_TABLE_EXISTS_ERROR 1050
        // ER_BAD_TABLE_ERROR 1051
        mysqlToSqlState.put(new Integer(1051),
            SQL_STATE_BASE_TABLE_OR_VIEW_NOT_FOUND);

        // ER_NON_UNIQ_ERROR 1052
        // ER_BAD_FIELD_ERROR 1054
        mysqlToSqlState.put(new Integer(1054), SQL_STATE_COLUMN_NOT_FOUND);

        // ER_TEXTFILE_NOT_READABLE 1085
        // ER_FILE_EXISTS_ERROR 1086
        // ER_LOAD_INFO 1087
        // ER_ALTER_INFO 1088
        // ER_WRONG_SUB_KEY 1089
        // ER_CANT_REMOVE_ALL_FIELDS 1090
        // ER_CANT_DROP_FIELD_OR_KEY 1091
        // ER_INSERT_INFO 1092
        // ER_INSERT_TABLE_USED 1093
        // ER_LOCK_DEADLOCK 1213
        mysqlToSqlState.put(new Integer(1205), SQL_STATE_DEADLOCK);
        mysqlToSqlState.put(new Integer(1213), SQL_STATE_DEADLOCK);
        
        // ER_DUP_UNIQUE 1169
        // ER_NO_ROW_IS_REFERENCED 1216
        // ER_ROW_IS_REFERENCED 1217
        // 
        
        mysqlToSqlState.put(new Integer(1169), SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
        mysqlToSqlState.put(new Integer(1216), SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
		mysqlToSqlState.put(new Integer(1217), SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
    }

    static String get(String stateCode) {
        return (String) sqlStateMessages.get(stateCode);
    }

    /**
     * Map MySQL error codes to X/Open error codes
     *
     * @param errno the MySQL error code
     *
     * @return the corresponding X/Open error code
     */
    static String mysqlToXOpen(int errno) {
        Integer err = new Integer(errno);

        if (mysqlToSqlState.containsKey(err)) {
            return (String) mysqlToSqlState.get(err);
        } else {
            return SQL_STATE_GENERAL_ERROR;
        }
    }
}
