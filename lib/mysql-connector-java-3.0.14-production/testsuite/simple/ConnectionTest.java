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
package testsuite.simple;

import com.mysql.jdbc.SQLError;

import testsuite.BaseTestCase;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Properties;


/**
 * Tests java.sql.Connection functionality  ConnectionTest.java,v 1.1
 * 2002/12/06 22:01:05 mmatthew Exp
 *
 * @author Mark Matthews
 */
public class ConnectionTest extends BaseTestCase {
    /**
     * Constructor for ConnectionTest.
     *
     * @param name the name of the test to run
     */
    public ConnectionTest(String name) {
        super(name);
    }

    /**
     * Runs all test cases in this test suite
     *
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ConnectionTest.class);
    }

    /**
     * Tests catalog functionality
     *
     * @throws Exception if an error occurs
     */
    public void testCatalog() throws Exception {
        String currentCatalog = this.conn.getCatalog();
        this.conn.setCatalog(currentCatalog);
        assertTrue(currentCatalog.equals(this.conn.getCatalog()));
    }

    /**
     * Tests a cluster connection for failover, requires a two-node cluster URL
     * specfied in com.mysql.jdbc.testsuite.ClusterUrl system proeprty.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testClusterConnection() throws Exception {
        String url = System.getProperty("com.mysql.jdbc.testsuite.ClusterUrl");

        if ((url != null) && (url.length() > 0)) {
            Object versionNumObj = getSingleValueWithQuery(
                    "SHOW VARIABLES LIKE 'version'");

            if ((versionNumObj != null)
                    && (versionNumObj.toString().indexOf("cluster") != -1)) {
                Connection clusterConn = null;
                Statement clusterStmt = null;

                try {
                    DriverManager.getConnection(url);

                    clusterStmt = clusterConn.createStatement();
                    clusterStmt.executeQuery(
                        "DROP TABLE IF EXISTS testClusterConn");
                    clusterStmt.executeQuery(
                        "CREATE TABLE testClusterConn (field1 INT) TYPE=ndbcluster");
                    clusterStmt.executeQuery(
                        "INSERT INTO testClusterConn VALUES (1)");

                    clusterConn.setAutoCommit(false);

                    clusterStmt.executeQuery("SELECT * FROM testClusterConn");
                    clusterStmt.executeUpdate(
                        "UPDATE testClusterConn SET field1=4");

                    // Kill the connection
                    String connectionId = getSingleValueWithQuery(
                            "SELECT CONNECTION_ID()").toString();

                    System.out.println(
                        "Please kill the MySQL server now and press return...");
                    System.in.read();

                    System.out.println("Waiting for TCP/IP timeout...");
                    Thread.sleep(10);

                    System.out.println("Attempting auto reconnect");

                    try {
                        clusterConn.setAutoCommit(true);
                        clusterConn.setAutoCommit(false);
                    } catch (SQLException sqlEx) {
                        System.out.println(sqlEx);
                    }

                    //
                    // Test that this 'new' connection is not read-only
                    //
                    clusterStmt.executeUpdate(
                        "UPDATE testClusterConn SET field1=5");

                    ResultSet rs = clusterStmt.executeQuery(
                            "SELECT * FROM testClusterConn WHERE field1=5");

                    assertTrue("One row should be returned", rs.next());
                } finally {
                    if (clusterStmt != null) {
                        clusterStmt.executeQuery(
                            "DROP TABLE IF EXISTS testClusterConn");
                        clusterStmt.close();
                    }

                    if (clusterConn != null) {
                        clusterConn.close();
                    }
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testDeadlockDetection() throws Exception {
        try {
            this.rs = this.stmt.executeQuery(
                    "SHOW VARIABLES LIKE 'innodb_lock_wait_timeout'");
            this.rs.next();

            int timeoutSecs = this.rs.getInt(2);

            this.stmt.executeUpdate("DROP TABLE IF EXISTS t1");
            this.stmt.executeUpdate(
                "CREATE TABLE t1 (id INTEGER, x INTEGER) TYPE=INNODB");
            this.stmt.executeUpdate("INSERT INTO t1 VALUES(0, 0)");
            this.conn.setAutoCommit(false);
            this.conn.createStatement().executeQuery("SELECT * FROM t1 WHERE id=0 FOR UPDATE");

            Connection deadlockConn = getConnectionWithProps(new Properties());
            deadlockConn.setAutoCommit(false);

            // The following query should hang because con1 is locking the page
            deadlockConn.createStatement().executeUpdate("UPDATE t1 SET x=2 WHERE id=0");
            deadlockConn.commit();

            Thread.sleep(timeoutSecs * 2 * 1000);
        } catch (SQLException sqlEx) {
            System.out.println(
                "Caught SQLException due to deadlock/lock timeout");
            System.out.println("SQLState: " + sqlEx.getSQLState());
            System.out.println("Vendor error: " + sqlEx.getErrorCode());

            //
            // Check whether the driver thinks it really is deadlock...
            //
            assertTrue(SQLError.SQL_STATE_DEADLOCK.equals(sqlEx.getSQLState()));
            assertTrue(sqlEx.getErrorCode() == 1205);
        } finally {
            this.conn.setAutoCommit(true);
            this.stmt.executeUpdate("DROP TABLE IF EXISTS t1");
        }
    }

    /**
     * Tests isolation level functionality
     *
     * @throws Exception if an error occurs
     */
    public void testIsolationLevel() throws Exception {
        int[] isolationLevels = new int[] {
                Connection.TRANSACTION_NONE,
                Connection.TRANSACTION_READ_COMMITTED,
                Connection.TRANSACTION_READ_UNCOMMITTED,
                Connection.TRANSACTION_REPEATABLE_READ,
                Connection.TRANSACTION_SERIALIZABLE
            };

        DatabaseMetaData dbmd = this.conn.getMetaData();

        for (int i = 0; i < isolationLevels.length; i++) {
            if (dbmd.supportsTransactionIsolationLevel(isolationLevels[i])) {
                this.conn.setTransactionIsolation(isolationLevels[i]);

                assertTrue("Transaction isolation level that was set was not returned",
                    this.conn.getTransactionIsolation() == isolationLevels[i]);
            }
        }
    }

    /**
     * Tests setting profileSql on/off in the span of one connection.
     *
     * @throws Exception if an error occurs.
     */
    public void testSetProfileSql() throws Exception {
        ((com.mysql.jdbc.Connection) this.conn).setProfileSql(false);
        stmt.executeQuery("SELECT 1");
        ((com.mysql.jdbc.Connection) this.conn).setProfileSql(true);
        stmt.executeQuery("SELECT 1");
    }
}
