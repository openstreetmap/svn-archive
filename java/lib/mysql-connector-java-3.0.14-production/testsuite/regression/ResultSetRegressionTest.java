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
package testsuite.regression;

import com.mysql.jdbc.NotUpdatable;
import com.mysql.jdbc.SQLError;

import testsuite.BaseTestCase;

import java.io.Reader;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Properties;


/**
 * Regression test cases for the ResultSet class.
 *
 * @author Mark Matthews
 */
public class ResultSetRegressionTest extends BaseTestCase {
    /**
     * Creates a new ResultSetRegressionTest
     *
     * @param name the name of the test to run
     */
    public ResultSetRegressionTest(String name) {
        super(name);
    }

    /**
     * Runs all test cases in this test suite
     *
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ResultSetRegressionTest.class);
    }

    /**
     * Tests fix for BUG#2654, "Column 'column.table' not found" when "order
     * by" in query"
     *
     * @throws Exception if the test fails
     */
    public void testBug2654() throws Exception {
        if (false) { // this is currently a server-level bug

            try {
                this.stmt.executeUpdate("DROP TABLE IF EXISTS foo");
                this.stmt.executeUpdate("DROP TABLE IF EXISTS bar");

                this.stmt.executeUpdate("CREATE TABLE foo ("
                    + "  id tinyint(3) default NULL,"
                    + "  data varchar(255) default NULL"
                    + ") TYPE=MyISAM DEFAULT CHARSET=latin1");
                this.stmt.executeUpdate(
                    "INSERT INTO foo VALUES (1,'male'),(2,'female')");

                this.stmt.executeUpdate("CREATE TABLE bar ("
                    + "id tinyint(3) unsigned default NULL,"
                    + "data char(3) default '0'"
                    + ") TYPE=MyISAM DEFAULT CHARSET=latin1");

                this.stmt.executeUpdate(
                    "INSERT INTO bar VALUES (1,'yes'),(2,'no')");

                String statement = "select foo.id, foo.data, "
                    + "bar.data from foo, bar" + "	where "
                    + "foo.id = bar.id order by foo.id";

                String column = "foo.data";

                this.rs = this.stmt.executeQuery(statement);

                ResultSetMetaData rsmd = this.rs.getMetaData();
                System.out.println(rsmd.getTableName(1));
                System.out.println(rsmd.getColumnName(1));

                this.rs.next();

                String fooData = this.rs.getString(column);
            } finally {
                this.stmt.executeUpdate("DROP TABLE IF EXISTS foo");
                this.stmt.executeUpdate("DROP TABLE IF EXISTS bar");
            }
        }
    }

    /**
     * Tests for fix to BUG#1130
     *
     * @throws Exception if the test fails
     */
    public void testClobTruncate() throws Exception {
        try {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testClobTruncate");
            this.stmt.executeUpdate(
                "CREATE TABLE testClobTruncate (field1 TEXT)");
            this.stmt.executeUpdate(
                "INSERT INTO testClobTruncate VALUES ('abcdefg')");

            this.rs = this.stmt.executeQuery("SELECT * FROM testClobTruncate");
            this.rs.next();

            Clob clob = this.rs.getClob(1);
            clob.truncate(3);

            Reader reader = clob.getCharacterStream();
            char[] buf = new char[8];
            int charsRead = reader.read(buf);

            String clobAsString = new String(buf, 0, charsRead);

            assertTrue(clobAsString.equals("abc"));
        } finally {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testClobTruncate");
        }
    }

    /**
     * Tests that streaming result sets are registered correctly.
     *
     * @throws Exception if any errors occur
     */
    public void testClobberStreamingRS() throws Exception {
        try {
            Properties props = new Properties();
            props.setProperty("clobberStreamingResults", "true");

            Connection clobberConn = getConnectionWithProps(props);

            Statement clobberStmt = clobberConn.createStatement();

            clobberStmt.executeUpdate("DROP TABLE IF EXISTS StreamingClobber");
            clobberStmt.executeUpdate(
                "CREATE TABLE StreamingClobber ( DUMMYID "
                + " INTEGER NOT NULL, DUMMYNAME VARCHAR(32),PRIMARY KEY (DUMMYID) )");
            clobberStmt.executeUpdate(
                "INSERT INTO StreamingClobber (DUMMYID, DUMMYNAME) VALUES (0, NULL)");
            clobberStmt.executeUpdate(
                "INSERT INTO StreamingClobber (DUMMYID, DUMMYNAME) VALUES (1, 'nro 1')");
            clobberStmt.executeUpdate(
                "INSERT INTO StreamingClobber (DUMMYID, DUMMYNAME) VALUES (2, 'nro 2')");
            clobberStmt.executeUpdate(
                "INSERT INTO StreamingClobber (DUMMYID, DUMMYNAME) VALUES (3, 'nro 3')");

            Statement streamStmt = null;

            try {
                streamStmt = clobberConn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
                        java.sql.ResultSet.CONCUR_READ_ONLY);
                streamStmt.setFetchSize(Integer.MIN_VALUE);

                ResultSet rs = streamStmt.executeQuery(
                        "SELECT DUMMYID, DUMMYNAME "
                        + "FROM StreamingClobber ORDER BY DUMMYID");

                rs.next();

                // This should proceed normally, after the driver 
                // clears the input stream 
                clobberStmt.executeQuery("SHOW VARIABLES");
                rs.close();
            } finally {
                if (streamStmt != null) {
                    streamStmt.close();
                }
            }
        } finally {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS StreamingClobber");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testEmptyResultSetGet() throws Exception {
        try {
            ResultSet rs = this.stmt.executeQuery("SHOW VARIABLES LIKE 'foo'");
            System.out.println(rs.getInt(1));
        } catch (SQLException sqlEx) {
            assertTrue("Correct exception not thrown",
                SQLError.SQL_STATE_GENERAL_ERROR.equals(sqlEx.getSQLState()));
        }
    }

    /**
     * Checks fix for BUG#1592 -- cross-database updatable result sets are not
     * checked for updatability correctly.
     *
     * @throws Exception if the test fails.
     */
    public void testFixForBug1592() throws Exception {
        if (versionMeetsMinimum(4, 1)) {
            Statement updatableStmt = this.conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);

            try {
                updatableStmt.execute("SELECT * FROM mysql.user");

                this.rs = updatableStmt.getResultSet();
            } catch (SQLException sqlEx) {
                String message = sqlEx.getMessage();

                if ((message != null)
                        && (message.indexOf("Access denied") != -1)) {
                    System.err.println(
                        "WARN: Can't complete testFixForBug1592(), access to"
                        + " 'mysql' database not allowed");
                } else {
                    throw sqlEx;
                }
            }
        }
    }

    /**
     * Tests fix for BUG#2006, where 2 columns with same name in a result set
     * are returned via findColumn() in the wrong order...The JDBC spec
     * states,  that the _first_ matching column should be returned.
     *
     * @throws Exception if the test fails
     */
    public void testFixForBug2006() throws Exception {
        try {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testFixForBug2006_1");
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testFixForBug2006_2");
            this.stmt.executeUpdate(
                "CREATE TABLE testFixForBug2006_1 (key_field INT NOT NULL)");
            this.stmt.executeUpdate(
                "CREATE TABLE testFixForBug2006_2 (key_field INT NULL)");
            this.stmt.executeUpdate(
                "INSERT INTO testFixForBug2006_1 VALUES (1)");

            this.rs = this.stmt.executeQuery(
                    "SELECT testFixForBug2006_1.key_field, testFixForBug2006_2.key_field FROM testFixForBug2006_1 LEFT JOIN testFixForBug2006_2 USING(key_field)");

            ResultSetMetaData rsmd = this.rs.getMetaData();

            assertTrue(rsmd.getColumnName(1).equals(rsmd.getColumnName(2)));
            assertTrue(rsmd.isNullable(this.rs.findColumn("key_field")) == ResultSetMetaData.columnNoNulls);
            assertTrue(rsmd.isNullable(2) == ResultSetMetaData.columnNullable);
            assertTrue(rs.next());
            assertTrue(rs.getObject(1) != null);
            assertTrue(rs.getObject(2) == null);
        } finally {
            if (this.rs != null) {
                try {
                    this.rs.close();
                } catch (SQLException sqlEx) {
                    // ignore
                }

                this.rs = null;
            }

            this.stmt.executeUpdate("DROP TABLE IF EXISTS testFixForBug2006_1");
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testFixForBug2006_2");
        }
    }

    /**
     * Tests that ResultSet.getLong() does not truncate values.
     *
     * @throws Exception if any errors occur
     */
    public void testGetLongBug() throws Exception {
        stmt.executeUpdate("DROP TABLE IF EXISTS getLongBug");
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS getLongBug (int_col int, bigint_col bigint)");

        int intVal = 123456;
        long longVal1 = 123456789012345678L;
        long longVal2 = -2079305757640172711L;
        stmt.executeUpdate("INSERT INTO getLongBug " + "(int_col, bigint_col) "
            + "VALUES (" + intVal + ", " + longVal1 + "), " + "(" + intVal
            + ", " + longVal2 + ")");

        try {
            rs = stmt.executeQuery(
                    "SELECT int_col, bigint_col FROM getLongBug ORDER BY bigint_col DESC");
            rs.next();
            assertTrue("Values not decoded correctly",
                ((rs.getInt(1) == intVal) && (rs.getLong(2) == longVal1)));
            rs.next();
            assertTrue("Values not decoded correctly",
                ((rs.getInt(1) == intVal) && (rs.getLong(2) == longVal2)));
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception ex) {
                    // ignore
                }
            }

            stmt.executeUpdate("DROP TABLE IF EXISTS getLongBug");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testGetTimestampWithDate() throws Exception {
        try {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testGetTimestamp");
            this.stmt.executeUpdate("CREATE TABLE testGetTimestamp (d date)");
            this.stmt.executeUpdate(
                "INSERT INTO testGetTimestamp values (now())");

            this.rs = this.stmt.executeQuery("SELECT * FROM testGetTimestamp");
            this.rs.next();
            System.out.println(this.rs.getTimestamp(1));
        } finally {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testGetTimestamp");
        }
    }

    /**
     * Tests a bug where ResultSet.isBefireFirst() would return true when the
     * result set was empty (which is incorrect)
     *
     * @throws Exception if an error occurs.
     */
    public void testIsBeforeFirstOnEmpty() throws Exception {
        try {
            //Query with valid rows: isBeforeFirst() correctly returns True
            rs = stmt.executeQuery("SHOW VARIABLES LIKE 'version'");
            assertTrue("Non-empty search should return true", rs.isBeforeFirst());

            //Query with empty result: isBeforeFirst() falsely returns True
            //Sun's documentation says it should return false
            rs = stmt.executeQuery("SHOW VARIABLES LIKE 'garbage'");
            assertTrue("Empty search should return false ", !rs.isBeforeFirst());
        } finally {
            rs.close();
        }
    }

    /**
     * Tests a bug where ResultSet.isBefireFirst() would return true when the
     * result set was empty (which is incorrect)
     *
     * @throws Exception if an error occurs.
     */
    public void testMetaDataIsWritable() throws Exception {
        try {
            //Query with valid rows
            rs = stmt.executeQuery("SHOW VARIABLES LIKE 'version'");

            ResultSetMetaData rsmd = rs.getMetaData();

            int numColumns = rsmd.getColumnCount();

            for (int i = 1; i <= numColumns; i++) {
                assertTrue("rsmd.isWritable() should != rsmd.isReadOnly()",
                    rsmd.isWritable(i) != rsmd.isReadOnly(i));
            }
        } finally {
            rs.close();
        }
    }

    /**
     * Tests fix for bug # 496
     *
     * @throws Exception if an error happens.
     */
    public void testNextAndPrevious() throws Exception {
        try {
            stmt.executeUpdate("DROP TABLE IF EXISTS testNextAndPrevious");
            stmt.executeUpdate("CREATE TABLE testNextAndPrevious (field1 int)");
            stmt.executeUpdate("INSERT INTO testNextAndPrevious VALUES (1)");

            rs = stmt.executeQuery("SELECT * from testNextAndPrevious");

            System.out.println("Currently at row " + rs.getRow());
            rs.next();
            System.out.println("Value at row " + rs.getRow() + " is "
                + rs.getString(1));

            rs.previous();

            try {
                System.out.println("Value at row " + rs.getRow() + " is "
                    + rs.getString(1));
                fail(
                    "Should not be able to retrieve values with invalid cursor");
            } catch (SQLException sqlEx) {
                assertTrue(sqlEx.getMessage().startsWith("Before start"));
            }

            rs.next();

            rs.next();

            try {
                System.out.println("Value at row " + rs.getRow() + " is "
                    + rs.getString(1));
                fail(
                    "Should not be able to retrieve values with invalid cursor");
            } catch (SQLException sqlEx) {
                assertTrue(sqlEx.getMessage().startsWith("After end"));
            }
        } finally {
            stmt.executeUpdate("DROP TABLE IF EXISTS testNextAndPrevious");
        }
    }

    /**
     * Tests fix for BUG#1630 (not updatable exception turning into NPE on
     * second updateFoo() method call.
     *
     * @throws Exception if an unexpected exception is thrown.
     */
    public void testNotUpdatable() throws Exception {
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            String sQuery = "SHOW VARIABLES";
            pstmt = conn.prepareStatement(sQuery,
                    ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                rs.absolute(1);

                try {
                    rs.updateInt(1, 1);
                } catch (SQLException sqlEx) {
                    assertTrue(sqlEx instanceof NotUpdatable);
                }

                try {
                    rs.updateString(1, "1");
                } catch (SQLException sqlEx) {
                    assertTrue(sqlEx instanceof NotUpdatable);
                }
            }
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Tests that streaming result sets are registered correctly.
     *
     * @throws Exception if any errors occur
     */
    public void testStreamingRegBug() throws Exception {
        try {
            stmt.executeUpdate("DROP TABLE IF EXISTS StreamingRegBug");
            stmt.executeUpdate("CREATE TABLE StreamingRegBug ( DUMMYID "
                + " INTEGER NOT NULL, DUMMYNAME VARCHAR(32),PRIMARY KEY (DUMMYID) )");
            stmt.executeUpdate(
                "INSERT INTO StreamingRegBug (DUMMYID, DUMMYNAME) VALUES (0, NULL)");
            stmt.executeUpdate(
                "INSERT INTO StreamingRegBug (DUMMYID, DUMMYNAME) VALUES (1, 'nro 1')");
            stmt.executeUpdate(
                "INSERT INTO StreamingRegBug (DUMMYID, DUMMYNAME) VALUES (2, 'nro 2')");
            stmt.executeUpdate(
                "INSERT INTO StreamingRegBug (DUMMYID, DUMMYNAME) VALUES (3, 'nro 3')");

            Statement streamStmt = null;

            try {
                streamStmt = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
                        java.sql.ResultSet.CONCUR_READ_ONLY);
                streamStmt.setFetchSize(Integer.MIN_VALUE);

                ResultSet rs = streamStmt.executeQuery(
                        "SELECT DUMMYID, DUMMYNAME "
                        + "FROM StreamingRegBug ORDER BY DUMMYID");

                while (rs.next()) {
                    rs.getString(1);
                }

                rs.close(); // error occurs here
            } finally {
                if (streamStmt != null) {
                    streamStmt.close();
                }
            }
        } finally {
            stmt.executeUpdate("DROP TABLE IF EXISTS StreamingRegBug");
        }
    }

    /**
     * Tests that result sets can be updated when all parameters are correctly
     * set.
     *
     * @throws Exception if any errors occur
     */
    public void testUpdatability() throws Exception {
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        stmt.execute("DROP TABLE IF EXISTS updatabilityBug");
        stmt.execute("CREATE TABLE IF NOT EXISTS updatabilityBug ("
            + " id int(10) unsigned NOT NULL auto_increment,"
            + " field1 varchar(32) NOT NULL default '',"
            + " field2 varchar(128) NOT NULL default '',"
            + " field3 varchar(128) default NULL,"
            + " field4 varchar(128) default NULL,"
            + " field5 varchar(64) default NULL,"
            + " field6 int(10) unsigned default NULL,"
            + " field7 varchar(64) default NULL," + " PRIMARY KEY  (id)"
            + ") TYPE=InnoDB;");
        stmt.executeUpdate("insert into updatabilityBug (id) values (1)");

        try {
            String sQuery = " SELECT * FROM updatabilityBug WHERE id = ? ";
            pstmt = conn.prepareStatement(sQuery,
                    ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            conn.setAutoCommit(false);
            pstmt.setInt(1, 1);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                rs.absolute(1);
                rs.updateInt("id", 1);
                rs.updateString("field1", "1");
                rs.updateString("field2", "1");
                rs.updateString("field3", "1");
                rs.updateString("field4", "1");
                rs.updateString("field5", "1");
                rs.updateInt("field6", 1);
                rs.updateString("field7", "1");
                rs.updateRow();
            }

            conn.commit();
            conn.setAutoCommit(true);
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {
                    // ignore
                }
            }

            stmt.execute("DROP TABLE IF EXISTS updatabilityBug");
        }
    }

    /**
     * Test fixes for BUG#1071
     *
     * @throws Exception if the test fails.
     */
    public void testUpdatabilityAndEscaping() throws Exception {
        Properties props = new Properties();
        props.setProperty("useUnicode", "true");
        props.setProperty("characterEncoding", "big5");

        Connection updConn = getConnectionWithProps(props);
        Statement updStmt = updConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_UPDATABLE);

        try {
            updStmt.executeUpdate(
                "DROP TABLE IF EXISTS testUpdatesWithEscaping");
            updStmt.executeUpdate(
                "CREATE TABLE testUpdatesWithEscaping (field1 INT PRIMARY KEY, field2 VARCHAR(64))");
            updStmt.executeUpdate(
                "INSERT INTO testUpdatesWithEscaping VALUES (1, null)");

            String stringToUpdate = "\" \\ '";

            this.rs = updStmt.executeQuery(
                    "SELECT * from testUpdatesWithEscaping");

            this.rs.next();
            this.rs.updateString(2, stringToUpdate);
            this.rs.updateRow();

            assertTrue(stringToUpdate.equals(rs.getString(2)));
        } finally {
            updStmt.executeUpdate(
                "DROP TABLE IF EXISTS testUpdatesWithEscaping");
            updStmt.close();
            updConn.close();
        }
    }

    /**
     * Tests the fix for BUG#661 ... refreshRow() fails when primary key values
     * have escaped data in them.
     *
     * @throws Exception if an error occurs
     */
    public void testUpdatabilityWithQuotes() throws Exception {
        Statement updStmt = null;

        try {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testUpdWithQuotes");
            this.stmt.executeUpdate(
                "CREATE TABLE testUpdWithQuotes (keyField CHAR(32) PRIMARY KEY NOT NULL, field2 int)");

            PreparedStatement pStmt = this.conn.prepareStatement(
                    "INSERT INTO testUpdWithQuotes VALUES (?, ?)");
            pStmt.setString(1, "Abe's");
            pStmt.setInt(2, 1);
            pStmt.executeUpdate();

            updStmt = this.conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);

            this.rs = updStmt.executeQuery("SELECT * FROM testUpdWithQuotes");
            this.rs.next();
            this.rs.updateInt(2, 2);
            this.rs.updateRow();
        } finally {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testUpdWithQuotes");

            if (this.rs != null) {
                this.rs.close();
            }

            this.rs = null;

            if (updStmt != null) {
                updStmt.close();
            }

            updStmt = null;
        }
    }

    /**
     * Checks whether or not ResultSet.updateClob() is implemented
     *
     * @throws Exception if the test fails
     */
    public void testUpdateClob() throws Exception {
        Statement updatableStmt = this.conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE);

        try {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testUpdateClob");
            this.stmt.executeUpdate(
                "CREATE TABLE testUpdateClob(intField INT NOT NULL PRIMARY KEY, clobField TEXT)");
            this.stmt.executeUpdate(
                "INSERT INTO testUpdateClob VALUES (1, 'foo')");

            this.rs = updatableStmt.executeQuery(
                    "SELECT intField, clobField FROM testUpdateClob");
            this.rs.next();

            Clob clob = this.rs.getClob(2);

            clob.setString(1, "bar");

            this.rs.updateClob(2, clob);
            this.rs.updateRow();

            this.rs.moveToInsertRow();

            clob.setString(1, "baz");
            this.rs.updateInt(1, 2);
            this.rs.updateClob(2, clob);
            this.rs.insertRow();

            clob.setString(1, "bat");
            this.rs.updateInt(1, 3);
            this.rs.updateClob(2, clob);
            this.rs.insertRow();

            this.rs.close();

            this.rs = this.stmt.executeQuery(
                    "SELECT intField, clobField FROM testUpdateClob ORDER BY intField");

            this.rs.next();
            assertTrue((this.rs.getInt(1) == 1)
                && this.rs.getString(2).equals("bar"));

            this.rs.next();
            assertTrue((this.rs.getInt(1) == 2)
                && this.rs.getString(2).equals("baz"));

            this.rs.next();
            assertTrue((this.rs.getInt(1) == 3)
                && this.rs.getString(2).equals("bat"));
        } finally {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testUpdateClob");
        }
    }
}
