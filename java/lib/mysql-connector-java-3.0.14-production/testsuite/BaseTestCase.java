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
package testsuite;

import junit.framework.TestCase;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Properties;


/**
 * Base class for all test cases. Creates connections,  statements, etc. and
 * closes them.
 *
 * @author Mark Matthews
 * @version $Id: BaseTestCase.java,v 1.8.2.5 2004/04/27 16:15:58 mmatthew Exp $
 */
public abstract class BaseTestCase extends TestCase {
    /**
     * JDBC URL, initialized from com.mysql.jdbc.testsuite.url system property,
     * or defaults to jdbc:mysql:///test
     */
    protected static String dbUrl = "jdbc:mysql:///test";
    private final static String ADMIN_CONNECTION_PROPERTY_NAME = "com.mysql.jdbc.testsuite.admin-url";

    /**
     * Connection to server, initialized in setUp() Cleaned up in tearDown().
     */
    protected Connection conn = null;

    /**
     * PreparedStatement to be used in tests, not initialized. Cleaned up in
     * tearDown().
     */
    protected PreparedStatement pstmt = null;

    /**
     * ResultSet to be used in tests, not initialized. Cleaned up in
     * tearDown().
     */
    protected ResultSet rs = null;

    /**
     * Statement to be used in tests, initialized in setUp(). Cleaned up in
     * tearDown().
     */
    protected Statement stmt = null;

    /**
     * Creates a new BaseTestCase object.
     *
     * @param name The name of the JUnit test case
     */
    public BaseTestCase(String name) {
        super(name);

        String newDbUrl = System.getProperty("com.mysql.jdbc.testsuite.url");

        if ((newDbUrl != null) && (newDbUrl.trim().length() != 0)) {
            dbUrl = newDbUrl;
        }
    }

    /**
     * Creates resources used by all tests.
     *
     * @throws Exception if an error occurs.
     */
    public void setUp() throws Exception {
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        this.conn = DriverManager.getConnection(dbUrl);
        this.stmt = conn.createStatement();
    }

    /**
     * Destroys resources created during the test case.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void tearDown() throws Exception {
        if (this.rs != null) {
            try {
                this.rs.close();
            } catch (SQLException SQLE) {
                ;
            }
        }

        if (this.stmt != null) {
            try {
                this.stmt.close();
            } catch (SQLException SQLE) {
                ;
            }
        }

        if (this.pstmt != null) {
            try {
                this.pstmt.close();
            } catch (SQLException SQLE) {
                ;
            }
        }

        if (this.conn != null) {
            try {
                this.conn.close();
            } catch (SQLException SQLE) {
                ;
            }
        }
    }

    protected Connection getAdminConnection() throws SQLException {
        return getAdminConnectionWithProps(new Properties());
    }

    protected boolean isAdminConnectionConfigured() {
        return System.getProperty(ADMIN_CONNECTION_PROPERTY_NAME) != null;
    }

    protected Connection getAdminConnectionWithProps(Properties props)
        throws SQLException {
        String adminUrl = System.getProperty(ADMIN_CONNECTION_PROPERTY_NAME);

        if (adminUrl != null) {
            return DriverManager.getConnection(adminUrl, props);
        } else {
            return null;
        }
    }

    /**
     * Returns a new connection with the given properties
     *
     * @param props the properties to use (the URL will come from the standard
     *        for this testcase).
     *
     * @return a new connection using the given properties.
     *
     * @throws SQLException DOCUMENT ME!
     */
    protected Connection getConnectionWithProps(Properties props)
        throws SQLException {
        return DriverManager.getConnection(dbUrl, props);
    }

    /**
     * Returns the named MySQL variable from the currently connected server.
     *
     * @param variableName the name of the variable to return
     *
     * @return the value of the given variable, or NULL if it doesn't exist
     *
     * @throws SQLException if an error occurs
     */
    protected String getMysqlVariable(String variableName)
        throws SQLException {
        Object value = getSingleValueWithQuery("SHOW VARIABLES LIKE '"
                + variableName + "'");

        if (value != null) {
            return value.toString();
        } else {
            return null;
        }
    }

    protected Object getSingleValue(String tableName, String columnName,
        String whereClause) throws SQLException {
        return getSingleValueWithQuery("SELECT " + columnName + " FROM "
            + tableName + ((whereClause == null) ? "" : whereClause));
    }

    protected Object getSingleValueWithQuery(String query)
        throws SQLException {
        ResultSet valueRs = null;

        try {
            valueRs = this.stmt.executeQuery(query);

            if (!valueRs.next()) {
                return null;
            }

            return valueRs.getObject(1);
        } finally {
            if (valueRs != null) {
                valueRs.close();
            }
        }
    }

    /**
     * Checks whether a certain system property is defined, in order to
     * run/not-run certain tests
     *
     * @param propName the property name to check for
     *
     * @return true if the property is defined.
     */
    protected boolean runTestIfSysPropDefined(String propName) {
        String prop = System.getProperty(propName);

        return (prop != null) && (prop.length() > 0);
    }

    /**
     * Checks whether the database we're connected to meets the given version
     * minimum
     *
     * @param major the major version to meet
     * @param minor the minor version to meet
     *
     * @return boolean if the major/minor is met
     *
     * @throws SQLException if an error occurs.
     */
    protected boolean versionMeetsMinimum(int major, int minor)
        throws SQLException {
        DatabaseMetaData dbmd = this.conn.getMetaData();

        return ((dbmd.getDatabaseMajorVersion() >= major)
        && (dbmd.getDatabaseMinorVersion() >= minor));
    }
}
