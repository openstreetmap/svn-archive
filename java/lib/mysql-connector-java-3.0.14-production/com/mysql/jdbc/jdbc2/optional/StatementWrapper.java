/*
   Copyright (C) 2004 MySQL AB
   
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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

import com.mysql.jdbc.SQLError;


/**
 * Wraps statements so that errors can be reported correctly to 
 * ConnectionEventListeners.
 *
 * @author Mark Matthews
 * 
 * @version $Id: StatementWrapper.java,v 1.1.2.2 2004/04/18 21:02:51 mmatthew Exp $
 */
class StatementWrapper extends WrapperBase implements Statement {
    protected Statement wrappedStmt;

    protected StatementWrapper(MysqlPooledConnection conn, Statement toWrap) {
        this.pooledConnection = conn;
        this.wrappedStmt = toWrap;
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#getConnection()
     */
    public Connection getConnection() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.getConnection();
            } else {
                throw new SQLException("Statement already closed",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }

        return null; // we actually never get here, but the compiler can't figure 

        // that out
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#setCursorName(java.lang.String)
     */
    public void setCursorName(String name) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                this.wrappedStmt.setCursorName(name);
            } else {
                throw new SQLException("Statement already closed",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#setEscapeProcessing(boolean)
     */
    public void setEscapeProcessing(boolean enable) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                this.wrappedStmt.setEscapeProcessing(enable);
            } else {
                throw new SQLException("Statement already closed",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#setFetchDirection(int)
     */
    public void setFetchDirection(int direction) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                this.wrappedStmt.setFetchDirection(direction);
            } else {
                throw new SQLException("Statement already closed",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#getFetchDirection()
     */
    public int getFetchDirection() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.getFetchDirection();
            } else {
                throw new SQLException("Statement already closed",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }

        return ResultSet.FETCH_FORWARD; // we actually never get here, but the compiler can't figure 

        // that out
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#setFetchSize(int)
     */
    public void setFetchSize(int rows) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                this.wrappedStmt.setFetchSize(rows);
            } else {
                throw new SQLException("Statement already closed",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#getFetchSize()
     */
    public int getFetchSize() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.getFetchSize();
            } else {
                throw new SQLException("Statement already closed",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }

        return 0; // we actually never get here, but the compiler can't figure 

        // that out
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#getGeneratedKeys()
     */
    public ResultSet getGeneratedKeys() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.getGeneratedKeys();
            } else {
                throw new SQLException("Statement already closed",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }

        return null; // we actually never get here, but the compiler can't figure 

        // that out
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#setMaxFieldSize(int)
     */
    public void setMaxFieldSize(int max) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                this.wrappedStmt.setMaxFieldSize(max);
            } else {
                throw new SQLException("Statement already closed",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#getMaxFieldSize()
     */
    public int getMaxFieldSize() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.getMaxFieldSize();
            } else {
                throw new SQLException("Statement already closed",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }

        return 0; // we actually never get here, but the compiler can't figure 

        // that out
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#setMaxRows(int)
     */
    public void setMaxRows(int max) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                this.wrappedStmt.setMaxRows(max);
            } else {
                throw new SQLException("Statement already closed",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#getMaxRows()
     */
    public int getMaxRows() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.getMaxRows();
            } else {
                throw new SQLException("Statement already closed",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }

        return 0; // we actually never get here, but the compiler can't figure 

        // that out
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#getMoreResults()
     */
    public boolean getMoreResults() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.getMoreResults();
            } else {
                throw new SQLException("Statement already closed",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }

        return false;
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#getMoreResults(int)
     */
    public boolean getMoreResults(int current) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.getMoreResults(current);
            } else {
                throw new SQLException("Statement already closed",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }

        return false;
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#setQueryTimeout(int)
     */
    public void setQueryTimeout(int seconds) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                this.wrappedStmt.setQueryTimeout(seconds);
            } else {
                throw new SQLException("Statement already closed",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#getQueryTimeout()
     */
    public int getQueryTimeout() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.getQueryTimeout();
            } else {
                throw new SQLException("Statement already closed",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }

        return 0;
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#getResultSet()
     */
    public ResultSet getResultSet() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.getResultSet();
            } else {
                throw new SQLException("Statement already closed",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }

        return null;
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#getResultSetConcurrency()
     */
    public int getResultSetConcurrency() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.getResultSetConcurrency();
            } else {
                throw new SQLException("Statement already closed",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }

        return 0;
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#getResultSetHoldability()
     */
    public int getResultSetHoldability() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.getResultSetHoldability();
            } else {
                throw new SQLException("Statement already closed",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }

        return Statement.CLOSE_CURRENT_RESULT;
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#getResultSetType()
     */
    public int getResultSetType() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.getResultSetType();
            } else {
                throw new SQLException("Statement already closed",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }

        return ResultSet.TYPE_FORWARD_ONLY;
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#getUpdateCount()
     */
    public int getUpdateCount() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.getUpdateCount();
            } else {
                throw new SQLException("Statement already closed",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }

        return -1;
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#getWarnings()
     */
    public SQLWarning getWarnings() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.getWarnings();
            } else {
                throw new SQLException("Statement already closed",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }

        return null;
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#addBatch(java.lang.String)
     */
    public void addBatch(String sql) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                this.wrappedStmt.addBatch(sql);
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#cancel()
     */
    public void cancel() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                this.wrappedStmt.cancel();
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#clearBatch()
     */
    public void clearBatch() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                this.wrappedStmt.clearBatch();
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#clearWarnings()
     */
    public void clearWarnings() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                this.wrappedStmt.clearWarnings();
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#close()
     */
    public void close() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                this.wrappedStmt.close();
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        } finally {
            this.wrappedStmt = null;
            this.pooledConnection = null;
        }
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#execute(java.lang.String, int)
     */
    public boolean execute(String sql, int autoGeneratedKeys)
        throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.execute(sql, autoGeneratedKeys);
            } else {
                throw new SQLException("Statement already closed",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }

        return false; // we actually never get here, but the compiler can't figure 

        // that out
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#execute(java.lang.String, int[])
     */
    public boolean execute(String sql, int[] columnIndexes)
        throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.execute(sql, columnIndexes);
            } else {
                throw new SQLException("Statement already closed",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }

        return false; // we actually never get here, but the compiler can't figure 

        // that out
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#execute(java.lang.String, java.lang.String[])
     */
    public boolean execute(String sql, String[] columnNames)
        throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.execute(sql, columnNames);
            } else {
                throw new SQLException("Statement already closed",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }

        return false; // we actually never get here, but the compiler can't figure 

        // that out
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#execute(java.lang.String)
     */
    public boolean execute(String sql) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.execute(sql);
            } else {
                throw new SQLException("Statement already closed",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }

        return false; // we actually never get here, but the compiler can't figure 

        // that out
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#executeBatch()
     */
    public int[] executeBatch() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.executeBatch();
            } else {
                throw new SQLException("Statement already closed",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }

        return null; // we actually never get here, but the compiler can't figure 

        // that out
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#executeQuery(java.lang.String)
     */
    public ResultSet executeQuery(String sql) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.executeQuery(sql);
            } else {
                throw new SQLException("Statement already closed",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }

        return null; // we actually never get here, but the compiler can't figure 

        // that out
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#executeUpdate(java.lang.String, int)
     */
    public int executeUpdate(String sql, int autoGeneratedKeys)
        throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.executeUpdate(sql, autoGeneratedKeys);
            } else {
                throw new SQLException("Statement already closed",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }

        return -1; // we actually never get here, but the compiler can't figure 

        // that out
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#executeUpdate(java.lang.String, int[])
     */
    public int executeUpdate(String sql, int[] columnIndexes)
        throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.executeUpdate(sql, columnIndexes);
            } else {
                throw new SQLException("Statement already closed",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }

        return -1; // we actually never get here, but the compiler can't figure 

        // that out
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#executeUpdate(java.lang.String, java.lang.String[])
     */
    public int executeUpdate(String sql, String[] columnNames)
        throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.executeUpdate(sql, columnNames);
            } else {
                throw new SQLException("Statement already closed",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }

        return -1; // we actually never get here, but the compiler can't figure 

        // that out
    }

    /* (non-Javadoc)
     * @see java.sql.Statement#executeUpdate(java.lang.String)
     */
    public int executeUpdate(String sql) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return this.wrappedStmt.executeUpdate(sql);
            } else {
                throw new SQLException("Statement already closed",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        } catch (SQLException sqlEx) {
            checkAndFireConnectionError(sqlEx);
        }

        return -1; // we actually never get here, but the compiler can't figure 

        // that out
    }
}
