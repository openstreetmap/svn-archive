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

/**
 * EscapeProcessor performs all escape code processing as outlined
 * in the JDBC spec by JavaSoft.
 *
 * @author Mark Matthews
 * @version $Id: EscapeProcessor.java,v 1.9.2.9 2004/04/18 21:02:50 mmatthew Exp $
 */
package com.mysql.jdbc;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;


class EscapeProcessor {
    /**
     * Escape process one string
     *
     * @param SQL the SQL to escape process.
     * @param serverSupportsConvertFn does the server support CONVERT() or CAST()?
     * 
     * @return the SQL after it has been escape processed.
     */
    public static final String escapeSQL(String sql, boolean serverSupportsConvertFn)
        throws java.sql.SQLException {
        boolean replaceEscapeSequence = false;
        String escapeSequence = null;
        StringBuffer newSql = new StringBuffer();

        if (sql == null) {
            return null;
        }

        /*
         * Short circuit this code if we don't have a matching pair of
         * "{}". - Suggested by Ryan Gustafason
         */
        int beginBrace = sql.indexOf('{');
        int nextEndBrace = (beginBrace == -1) ? (-1)
                                              : sql.indexOf('}', beginBrace);

        if (nextEndBrace == -1) {
            return sql;
        }

        EscapeTokenizer escapeTokenizer = new EscapeTokenizer(sql);

        while (escapeTokenizer.hasMoreTokens()) {
            String token = escapeTokenizer.nextToken();

            if (token.startsWith("{")) { // It's an escape code

                if (!token.endsWith("}")) {
                    throw new java.sql.SQLException(
                        "Not a valid escape sequence: " + token);
                }

                if (token.length() > 2) {
                    int nestedBrace = token.indexOf('{', 2);

                    if (nestedBrace != -1) {
                        StringBuffer buf = new StringBuffer(token.substring(0, 1));

                        String remaining = escapeSQL(token.substring(1,
                                    token.length() - 1), serverSupportsConvertFn);

                        buf.append(remaining);

                        buf.append('}');

                        token = buf.toString();
                    }
                }
                 // nested escape code

				// Compare to tokens with _no_ whitespace
				String collapsedToken = removeWhitespace(token);
				
                /*
                 * Process the escape code
                 */
                if (StringUtils.startsWithIgnoreCase(collapsedToken, "{escape")) {
                    try {
                        StringTokenizer st = new StringTokenizer(token, " '");
                        st.nextToken(); // eat the "escape" token
                        escapeSequence = st.nextToken();

                        if (escapeSequence.length() < 3) {
                            throw new java.sql.SQLException(
                                "Syntax error for escape sequence '" + token
                                + "'", SQLError.SQL_STATE_SYNTAX_ERROR);
                        }

                        escapeSequence = escapeSequence.substring(1,
                                escapeSequence.length() - 1);
                        replaceEscapeSequence = true;
                    } catch (java.util.NoSuchElementException e) {
                        throw new java.sql.SQLException(
                            "Syntax error for escape sequence '" + token + "'",
                            SQLError.SQL_STATE_SYNTAX_ERROR);
                    }
                } else if (StringUtils.startsWithIgnoreCase(collapsedToken, "{fn")) {
                	
                	
                    
                    int startPos = token.toLowerCase().indexOf("fn ") + 3;
                    int endPos = token.length() - 1; // no }
                    
                    String fnToken = token.substring(startPos, endPos);
                    
                    // We need to handle 'convert' by ourselves
                    
                    if (StringUtils.startsWithIgnoreCaseAndWs(fnToken, "convert")) {
                    	newSql.append(processConvertToken(fnToken, serverSupportsConvertFn));
                    } else {
                    	// just pass functions right to the DB
                    	newSql.append(fnToken);
                    }
                } else if (StringUtils.startsWithIgnoreCase(collapsedToken, "{d")) {
                    int startPos = token.indexOf('\'') + 1;
                    int endPos = token.lastIndexOf('\''); // no }

                    if ((startPos == -1) || (endPos == -1)) {
                        throw new java.sql.SQLException(
                            "Syntax error for DATE escape sequence '" + token
                            + "'", SQLError.SQL_STATE_SYNTAX_ERROR);
                    }

                    String argument = token.substring(startPos, endPos);

                    try {
                        StringTokenizer st = new StringTokenizer(argument, " -");
                        String year4 = st.nextToken();
                        String month2 = st.nextToken();
                        String day2 = st.nextToken();
                        String dateString = "'" + year4 + "-" + month2 + "-"
                            + day2 + "'";
                        newSql.append(dateString);
                    } catch (java.util.NoSuchElementException e) {
                        throw new java.sql.SQLException(
                            "Syntax error for DATE escape sequence '"
                            + argument + "'", SQLError.SQL_STATE_SYNTAX_ERROR);
                    }
                } else if (StringUtils.startsWithIgnoreCase(collapsedToken, "{ts")) {
                    int startPos = token.indexOf('\'') + 1;
                    int endPos = token.lastIndexOf('\''); // no }

                    if ((startPos == -1) || (endPos == -1)) {
                        throw new java.sql.SQLException(
                            "Syntax error for TIMESTAMP escape sequence '"
                            + token + "'", SQLError.SQL_STATE_SYNTAX_ERROR);
                    }

                    String argument = token.substring(startPos, endPos);

                    try {
                        StringTokenizer st = new StringTokenizer(argument,
                                " .-:");
                        String year4 = st.nextToken();
                        String month2 = st.nextToken();
                        String day2 = st.nextToken();
                        String hour = st.nextToken();
                        String minute = st.nextToken();
                        String second = st.nextToken();

                        /*
                         * For now, we get the fractional seconds
                         * part, but we don't use it, as MySQL doesn't
                         * support it in it's TIMESTAMP data type
                         *
                        String fractionalSecond = "";

                        if (st.hasMoreTokens()) {
                            fractionalSecond = st.nextToken();
                        }
                        */
                        /*
                         * Use the full format because number format
                         * will not work for "between" clauses.
                         *
                         * Ref. Mysql Docs
                         *
                         * You can specify DATETIME, DATE and TIMESTAMP values
                         * using any of a common set of formats:
                         *
                         * As a string in either 'YYYY-MM-DD HH:MM:SS' or
                         * 'YY-MM-DD HH:MM:SS' format.
                         *
                         * Thanks to Craig Longman for pointing out this bug
                         */
                        newSql.append("'").append(year4).append("-")
                              .append(month2).append("-").append(day2)
                              .append(" ").append(hour).append(":")
                              .append(minute).append(":").append(second).append("'");
                    } catch (java.util.NoSuchElementException e) {
                        throw new java.sql.SQLException(
                            "Syntax error for TIMESTAMP escape sequence '"
                            + argument + "'", SQLError.SQL_STATE_SYNTAX_ERROR);
                    }
                } else if (StringUtils.startsWithIgnoreCase(collapsedToken, "{t")) {
                    int startPos = token.indexOf('\'') + 1;
                    int endPos = token.lastIndexOf('\''); // no }

                    if ((startPos == -1) || (endPos == -1)) {
                        throw new java.sql.SQLException(
                            "Syntax error for TIME escape sequence '" + token
                            + "'", SQLError.SQL_STATE_SYNTAX_ERROR);
                    }

                    String argument = token.substring(startPos, endPos);

                    try {
                        StringTokenizer st = new StringTokenizer(argument, " :");
                        String hour = st.nextToken();
                        String minute = st.nextToken();
                        String second = st.nextToken();
                        String timeString = "'" + hour + ":" + minute + ":"
                            + second + "'";
                        newSql.append(timeString);
                    } catch (java.util.NoSuchElementException e) {
                        throw new java.sql.SQLException(
                            "Syntax error for escape sequence '" + argument
                            + "'", SQLError.SQL_STATE_SYNTAX_ERROR);
                    }
                } else if (StringUtils.startsWithIgnoreCase(collapsedToken, "{call")
                        || StringUtils.startsWithIgnoreCase(collapsedToken, "{?=call")) {
                    throw new java.sql.SQLException(
                        "Stored procedures not supported: " + token, "S1C00");
                } else if (StringUtils.startsWithIgnoreCase(collapsedToken, "{oj")) {
                    // MySQL already handles this escape sequence
                    // because of ODBC. Cool.
                    newSql.append(token);
                }
            } else {
                newSql.append(token); // it's just part of the query
            }
        }

        String escapedSql = newSql.toString();

        //
        // FIXME: Let MySQL do this, however requires
        //        lightweight parsing of statement
        //
        if (replaceEscapeSequence) {
            String currentSql = escapedSql;

            while (currentSql.indexOf(escapeSequence) != -1) {
                int escapePos = currentSql.indexOf(escapeSequence);
                String lhs = currentSql.substring(0, escapePos);
                String rhs = currentSql.substring(escapePos + 1,
                        currentSql.length());
                currentSql = lhs + "\\" + rhs;
            }

            escapedSql = currentSql;
        }

        return escapedSql;
    }
    
    /**
     * Removes all whitespace from the given String. We use
     * this to make escape token comparison white-space ignorant.
     *  
     * @param toCollapse the string to remove the whitespace from
     * @return a string with _no_ whitespace.
     */
    private static String removeWhitespace(String toCollapse) {
    	if (toCollapse == null) {
    		return null;
    	}
    	
    	int length = toCollapse.length();
    	
    	StringBuffer collapsed = new StringBuffer(length);
    	
    	for (int i = 0; i < length; i++) {
    		char c = toCollapse.charAt(i);
    		
    		if (!Character.isWhitespace(c)) {
    			collapsed.append(c);
    		}
    	}
    	
    	return collapsed.toString();
    }
    
    private static Map JDBC_CONVERT_TO_MYSQL_TYPE_MAP;
    private static Map JDBC_NO_CONVERT_TO_MYSQL_EXPRESSION_MAP;
    
    static {
    	Map tempMap = new HashMap();
    	
    	tempMap.put("BIGINT", "0 + ?");
		tempMap.put("BINARY", "BINARY");
		tempMap.put("BIT", "0 + ?");
		tempMap.put("CHAR", "CHAR");
		tempMap.put("DATE", "DATE");
		tempMap.put("DECIMAL", "0.0 + ?");
		tempMap.put("DOUBLE", "0.0 + ?");
		tempMap.put("FLOAT", "0.0 + ?");
		tempMap.put("INTEGER", "0 + ?");
		tempMap.put("LONGVARBINARY", "BINARY");
		tempMap.put("LONGVARCHAR", "CONCAT(?)");
		tempMap.put("REAL", "0.0 + ?");
		tempMap.put("SMALLINT", "CONCAT(?)");
		tempMap.put("TIME", "TIME");
		tempMap.put("TIMESTAMP", "DATETIME");
		tempMap.put("TINYINT", "CONCAT(?)");
		tempMap.put("VARBINARY", "BINARY");
		tempMap.put("VARCHAR", "CONCAT(?)");
		
		JDBC_CONVERT_TO_MYSQL_TYPE_MAP = Collections.unmodifiableMap(tempMap);
		
		tempMap = new HashMap(JDBC_CONVERT_TO_MYSQL_TYPE_MAP);
		
		tempMap.put("BINARY", "CONCAT(?)");	
		tempMap.put("CHAR", "CONCAT(?)");
		tempMap.remove("DATE");	
		tempMap.put("LONGVARBINARY", "CONCAT(?)");	
		tempMap.remove("TIME");
		tempMap.remove("TIMESTAMP");
		tempMap.put("VARBINARY", "CONCAT(?)");
		
		JDBC_NO_CONVERT_TO_MYSQL_EXPRESSION_MAP = Collections.unmodifiableMap(tempMap);
				
    }
    
    /**
     * Re-writes {fn convert (expr, type)} as cast(expr AS type)
     * @param functionToken
     * @return
     * @throws SQLException
     */
    private static String processConvertToken(String functionToken, boolean serverSupportsConvertFn) throws SQLException {
    	// The JDBC spec requires these types:
    	//
    	// BIGINT
    	// BINARY
		// BIT
    	// CHAR
    	// DATE
    	// DECIMAL
    	// DOUBLE
    	// FLOAT
    	// INTEGER
    	// LONGVARBINARY
		// LONGVARCHAR
    	// REAL
    	// SMALLINT
    	// TIME
    	// TIMESTAMP
    	// TINYINT
    	// VARBINARY
		// VARCHAR
    	
    	// MySQL supports these types:
    	//
    	// BINARY
    	// CHAR
    	// DATE
    	// DATETIME
    	// SIGNED (integer)
    	// UNSIGNED (integer)
    	// TIME
    	
    	int firstIndexOfParen = functionToken.indexOf("(");
    	
    	if (firstIndexOfParen == -1) {
			throw new SQLException("Syntax error while processing {fn convert (... , ...)} token, missing opening parenthesis in token '" + functionToken + "'.", SQLError.SQL_STATE_SYNTAX_ERROR);
    	}
    	
    	int tokenLength = functionToken.length();
    	
    	int indexOfComma = functionToken.lastIndexOf(",");
    
    	if (indexOfComma == -1) {
			throw new SQLException("Syntax error while processing {fn convert (... , ...)} token, missing comma in token '" + functionToken + "'.", SQLError.SQL_STATE_SYNTAX_ERROR);
    	}
 
    	int indexOfCloseParen = functionToken.indexOf(')', indexOfComma);
    	
    	if (indexOfCloseParen == -1) {
    		throw new SQLException("Syntax error while processing {fn convert (... , ...)} token, missing closing parenthesis in token '" + functionToken + "'.", SQLError.SQL_STATE_SYNTAX_ERROR);
    		
    	}
    
    	String expression = functionToken.substring(firstIndexOfParen + 1, indexOfComma);
    	String type = functionToken.substring(indexOfComma + 1, indexOfCloseParen);
    	
    	String newType = null;
    	
    	if (serverSupportsConvertFn) {
    		newType = (String)JDBC_CONVERT_TO_MYSQL_TYPE_MAP.get(type.trim().toUpperCase());
    	} else {
    		newType = (String)JDBC_NO_CONVERT_TO_MYSQL_EXPRESSION_MAP.get(type.trim().toUpperCase());
    		
    		// We need a 'special' check here to give a better error message. If we're in this
    		// block, the version of MySQL we're connected to doesn't support CAST/CONVERT,
    		// so we can't re-write some data type conversions (date,time,timestamp, datetime)
    		
    		if (newType == null) {
    			throw new SQLException("Can't find conversion re-write for type '" + type + "' that is applicable for this server version while processing escape tokens.", SQLError.SQL_STATE_GENERAL_ERROR);
    		}
    	}
    	
    	if (newType == null) {
    		throw new SQLException("Unsupported conversion type '" + type.trim() + "' found while processing escape token.", SQLError.SQL_STATE_GENERAL_ERROR);
    	}
    	
    	int replaceIndex = newType.indexOf("?");
    	
    	if (replaceIndex != -1) {
    		StringBuffer convertRewrite = new StringBuffer(newType.substring(0, replaceIndex));
    		convertRewrite.append(expression);
    		convertRewrite.append(newType.substring(replaceIndex + 1, newType.length()));
    		
    		return convertRewrite.toString();
    	} else {
    	
    		StringBuffer castRewrite = new StringBuffer("CAST(");
    		castRewrite.append(expression);
    		castRewrite.append(" AS ");
    		castRewrite.append(newType);
    		castRewrite.append(")");
    	
    		return castRewrite.toString();
    	}
    }
}
