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

import com.mysql.jdbc.SQLError;

import testsuite.BaseTestCase;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;


/**
 * Regression tests for the Statement class
 *
 * @author Mark Matthews
 */
public class StatementRegressionTest extends BaseTestCase {
    /*Each row in this table is to be converted into a single REPLACE statement.
     If the value is zero, a new record is to be created using then autoincrement
     feature. If the value is non-zero, the existing row of that value is to be
     replace with, obviously, the same key. I expect one Generated Key for each
     zero value - but I would accept one key for each value, with non-zero values
     coming back as themselves.
     */
    static final int[][] tests = {
        { 0 }, //generate 1
        { 1, 0, 0 }, //update 1, generate 2, 3
        { 2, 0, 0, }, //update 2, generate 3, 4
    };
    static int nextID = 1; //The next ID we expected to generate
    static int count = 0;

    /**
     * Constructor for StatementRegressionTest.
     *
     * @param name the name of the test to run
     */
    public StatementRegressionTest(String name) {
        super(name);
    }

    /**
     * Runs all test cases in this test suite
     *
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(StatementRegressionTest.class);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception ...
     */
    public void testBug1774() throws Exception {
        try {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug1774");
            this.stmt.executeUpdate(
                "CREATE TABLE testBug1774 (field1 VARCHAR(255))");

            PreparedStatement pStmt = this.conn.prepareStatement(
                    "INSERT INTO testBug1774 VALUES (?)");

            String testString = "The word contains \" character";

            pStmt.setString(1, testString);
            pStmt.executeUpdate();

            this.rs = this.stmt.executeQuery("SELECT * FROM testBug1774");
            this.rs.next();
            assertTrue(this.rs.getString(1).equals(testString));
        } finally {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug1774");
        }
    }

    /**
     * Tests fix for BUG#1901 -- PreparedStatement.setObject(int, Object, int,
     * int) doesn't support CLOB or BLOB types.
     *
     * @throws Exception if this test fails for any reason
     */
    public void testBug1901() throws Exception {
        try {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug1901");
            this.stmt.executeUpdate(
                "CREATE TABLE testBug1901 (field1 VARCHAR(255))");
            this.stmt.executeUpdate("INSERT INTO testBug1901 VALUES ('aaa')");

            this.rs = this.stmt.executeQuery("SELECT field1 FROM testBug1901");
            this.rs.next();

            Clob valueAsClob = this.rs.getClob(1);
            Blob valueAsBlob = this.rs.getBlob(1);

            PreparedStatement pStmt = this.conn.prepareStatement(
                    "INSERT INTO testBug1901 VALUES (?)");
            pStmt.setObject(1, valueAsClob, java.sql.Types.CLOB, 0);
            pStmt.executeUpdate();
            pStmt.setObject(1, valueAsBlob, java.sql.Types.BLOB, 0);
            pStmt.executeUpdate();
        } finally {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug1901");
        }
    }

    /**
     * Test fix for BUG#1933 -- Driver property 'maxRows' has no effect.
     *
     * @throws Exception if the test fails.
     */
    public void testBug1933() throws Exception {
        Connection maxRowsConn = null;
        PreparedStatement maxRowsPrepStmt = null;
        Statement maxRowsStmt = null;

        try {
            Properties props = new Properties();

            props.setProperty("maxRows", "1");

            maxRowsConn = getConnectionWithProps(props);

            maxRowsStmt = maxRowsConn.createStatement();

            assertTrue(maxRowsStmt.getMaxRows() == 1);

            this.rs = maxRowsStmt.executeQuery("SELECT 1 UNION SELECT 2");

            this.rs.next();

            maxRowsPrepStmt = maxRowsConn.prepareStatement(
                    "SELECT 1 UNION SELECT 2");

            assertTrue(maxRowsPrepStmt.getMaxRows() == 1);

            this.rs = maxRowsPrepStmt.executeQuery();

            this.rs.next();

            assertTrue(!this.rs.next());
        } finally {
            maxRowsConn.close();
        }
    }

    /**
     * Tests fix for BUG#1958 - Improper bounds checking on
     * PreparedStatement.setFoo().
     *
     * @throws Exception if the test fails.
     */
    public void testBug1958() throws Exception {
        PreparedStatement pStmt = null;

        try {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug1958");
            this.stmt.executeUpdate("CREATE TABLE testBug1958 (field1 int)");

            pStmt = this.conn.prepareStatement(
                    "SELECT * FROM testBug1958 WHERE field1 IN (?, ?, ?)");

            try {
                pStmt.setInt(4, 1);
            } catch (SQLException sqlEx) {
                assertTrue(SQLError.SQL_STATE_ILLEGAL_ARGUMENT.equals(
                        sqlEx.getSQLState()));
            }
        } finally {
            if (pStmt != null) {
                pStmt.close();
            }

            this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug1958");
        }
    }

    /**
     * Tests fix for BUG#3103 -- java.util.Date not accepted as parameter to
     * PreparedStatement.setObject().
     *
     * @throws Exception if the test fails
     *
     * @deprecated uses deprecated methods of Date class
     */
    public void testBug3103() throws Exception {
        try {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug3103");
            this.stmt.executeUpdate(
                "CREATE TABLE testBug3103 (field1 DATETIME)");

            PreparedStatement pStmt = this.conn.prepareStatement(
                    "INSERT INTO testBug3103 VALUES (?)");

            java.util.Date utilDate = new java.util.Date();

            pStmt.setObject(1, utilDate);
            pStmt.executeUpdate();

            this.rs = this.stmt.executeQuery("SELECT field1 FROM testBug3103");
            this.rs.next();

            java.util.Date retrUtilDate = new java.util.Date(this.rs.getTimestamp(
                        1).getTime());

            // We can only compare on the day/month/year hour/minute/second 
            // interval, because the timestamp has added milliseconds to the
            // internal date...
            assertTrue("Dates not equal",
                (utilDate.getMonth() == retrUtilDate.getMonth())
                && (utilDate.getDate() == retrUtilDate.getDate())
                && (utilDate.getYear() == retrUtilDate.getYear())
                && (utilDate.getHours() == retrUtilDate.getHours())
                && (utilDate.getMinutes() == retrUtilDate.getMinutes())
                && (utilDate.getSeconds() == retrUtilDate.getSeconds()));
        } finally {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug3103");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception ...
     */
    public void testBug3520() throws Exception {
        try {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS t");
            this.stmt.executeUpdate("CREATE TABLE t (s1 int,primary key (s1))");
            this.stmt.executeUpdate("INSERT INTO t VALUES (1)");
            this.stmt.executeUpdate("INSERT INTO t VALUES (1)");
        } catch (SQLException sqlEx) {
            System.out.println(sqlEx.getSQLState());
        } finally {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS t");
        }
    }

    /**
     * Test fix for BUG#3557 -- UpdatableResultSet not picking up default
     * values
     *
     * @throws Exception if test fails.
     */
    public void testBug3557() throws Exception {
        try {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug3557");

            this.stmt.executeUpdate("CREATE TABLE testBug3557 ( "
                + "`a` varchar(255) NOT NULL default 'XYZ', "
                + "`b` varchar(255) default '123', " + "PRIMARY KEY  (`a`))");

            Statement updStmt = this.conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            this.rs = updStmt.executeQuery("SELECT * FROM testBug3557");

            assertTrue(this.rs.getConcurrency() == ResultSet.CONCUR_UPDATABLE);

            this.rs.moveToInsertRow();

            assertTrue("XYZ".equals(this.rs.getObject(1)));
            assertTrue("123".equals(this.rs.getObject(2)));
        } finally {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug3557");
        }
    }

    /**
     *
     *
     * @throws SQLException ...
     */
    public void testBug3620() throws SQLException {
        long epsillon = 3000; // 3 seconds time difference

        try {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug3620");
            this.stmt.executeUpdate(
                "CREATE TABLE testBug3620 (field1 TIMESTAMP)");

            PreparedStatement tsPstmt = this.conn.prepareStatement(
                    "INSERT INTO testBug3620 VALUES (?)");

            Calendar pointInTime = Calendar.getInstance();
            pointInTime.set(2004, 02, 29, 10, 0, 0);

            long pointInTimeOffset = pointInTime.getTimeZone().getRawOffset();

            java.sql.Timestamp ts = new java.sql.Timestamp(pointInTime.getTime()
                                                                      .getTime());

            tsPstmt.setTimestamp(1, ts);
            tsPstmt.executeUpdate();

            String tsValueAsString = getSingleValue("testBug3620", "field1",
                    null).toString();

            System.out.println("Timestamp as string with no calendar: "
                + tsValueAsString.toString());

            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

            this.stmt.executeUpdate("DELETE FROM testBug3620");

            Properties props = new Properties();
            props.put("useTimezone", "true");

            Connection tzConn = getConnectionWithProps(props);

            Statement tsStmt = tzConn.createStatement();

            tsPstmt = tzConn.prepareStatement(
                    "INSERT INTO testBug3620 VALUES (?)");

            tsPstmt.setTimestamp(1, ts, cal);
            tsPstmt.executeUpdate();

            tsValueAsString = getSingleValue("testBug3620", "field1", null)
                                  .toString();

            Timestamp tsValueAsTimestamp = (Timestamp) getSingleValue("testBug3620",
                    "field1", null);

            System.out.println("Timestamp as string with UTC calendar: "
                + tsValueAsString.toString());
            System.out.println("Timestamp as Timestamp with UTC calendar: "
                + tsValueAsTimestamp);

            this.rs = tsStmt.executeQuery("SELECT field1 FROM testBug3620");
            this.rs.next();

            Timestamp tsValueUTC = this.rs.getTimestamp(1, cal);
            
            //
            // We use this testcase with other vendors, JDBC spec
            // requires result set fields can only be read once,
            // although MySQL doesn't require this ;)
            //
            this.rs = tsStmt.executeQuery("SELECT field1 FROM testBug3620");
            this.rs.next();
            
            Timestamp tsValueStmtNoCal = this.rs.getTimestamp(1);
            
            System.out.println(
                "Timestamp specifying UTC calendar from normal statement: "
                + tsValueUTC.toString());

            PreparedStatement tsPstmtRetr = tzConn.prepareStatement(
                    "SELECT field1 FROM testBug3620");

            this.rs = tsPstmtRetr.executeQuery();
            this.rs.next();

            Timestamp tsValuePstmtUTC = this.rs.getTimestamp(1, cal);

            System.out.println(
                "Timestamp specifying UTC calendar from prepared statement: "
                + tsValuePstmtUTC.toString());

            //
            // We use this testcase with other vendors, JDBC spec
            // requires result set fields can only be read once,
            // although MySQL doesn't require this ;)
            //
            
            this.rs = tsPstmtRetr.executeQuery();
            this.rs.next();
            
            Timestamp tsValuePstmtNoCal = this.rs.getTimestamp(1);

            
            System.out.println(
                "Timestamp specifying no calendar from prepared statement: "
                + tsValuePstmtNoCal.toString());

            long stmtDeltaTWithCal = (ts.getTime() - tsValueStmtNoCal.getTime());

            assertTrue(
                "Difference between original timestamp and timestamp retrieved using java.sql.Statement "
            		+ "set in database using UTC calendar is not ~= "
                + pointInTimeOffset,
                (Math.abs(stmtDeltaTWithCal - pointInTimeOffset) < epsillon));
            
            long pStmtDeltaTWithCal = (ts.getTime() - tsValuePstmtNoCal.getTime());

            assertTrue(
                "Difference between original timestamp and timestamp retrieved using java.sql.PreparedStatement "
            		+ "set in database using UTC calendar is not ~= "
                + pointInTimeOffset,
                (Math.abs(pStmtDeltaTWithCal - pointInTimeOffset) < epsillon));
            
            System.out.println(
                "Difference between original ts and ts with no calendar: "
                + (ts.getTime() - tsValuePstmtNoCal.getTime())
                + ", offset should be " + pointInTimeOffset);
        } finally {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug3620");
        }
    }

    /**
     * Tests BUG#3873 - PreparedStatement.executeBatch() not returning all generated keys
     * (even though that's not JDBC compliant).
     * 
     * @throws Exception if the test fails
     */
    public void testBug3873() throws Exception {
    	PreparedStatement batchStmt = null;
    	
    	try {
    		this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug3873");
    		this.stmt.executeUpdate("CREATE TABLE testBug3873 (keyField INT NOT NULL PRIMARY KEY AUTO_INCREMENT, dataField VARCHAR(32))");
    		batchStmt = this.conn.prepareStatement("INSERT INTO testBug3873 (dataField) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
    		batchStmt.setString(1, "abc");
    		batchStmt.addBatch();
    		batchStmt.setString(1, "def");
    		batchStmt.addBatch();
    		batchStmt.setString(1, "ghi");
    		batchStmt.addBatch();
    		
    		int[] updateCounts = batchStmt.executeBatch();
    		
    		this.rs = batchStmt.getGeneratedKeys();
    		
    		while (this.rs.next()) {
    			System.out.println(this.rs.getInt(1));
    		}
    		
    		this.rs = batchStmt.getGeneratedKeys();
    		assertTrue(this.rs.next());
    		assertTrue(1 == this.rs.getInt(1));
    		assertTrue(this.rs.next());
    		assertTrue(2 == this.rs.getInt(1));
    		assertTrue(this.rs.next());
    		assertTrue(3 == this.rs.getInt(1));
    		assertTrue(!this.rs.next());
    		
    	} finally {
    		if (batchStmt != null) {
    			batchStmt.close();
    		}
    		
    		this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug3873");
    	}
    }
    /**
     * Tests that you can close a statement twice without an NPE.
     *
     * @throws Exception if an error occurs.
     */
    public void testCloseTwice() throws Exception {
        Statement closeMe = this.conn.createStatement();
        closeMe.close();
        closeMe.close();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testGetGeneratedKeysAllCases() throws Exception {
        System.out.println("Using Statement.executeUpdate()\n");

        try {
            createGGKTables();

            //Do the tests
            for (int i = 0; i < tests.length; i++) {
                doGGKTestStatement(tests[i], true);
            }
        } finally {
            dropGGKTables();
        }

        nextID = 1;
        count = 0;

        System.out.println("Using Statement.execute()\n");

        try {
            createGGKTables();

            //Do the tests
            for (int i = 0; i < tests.length; i++) {
                doGGKTestStatement(tests[i], false);
            }
        } finally {
            dropGGKTables();
        }

        nextID = 1;
        count = 0;

        System.out.println("Using PreparedStatement.executeUpdate()\n");

        try {
            createGGKTables();

            //Do the tests
            for (int i = 0; i < tests.length; i++) {
                doGGKTestPreparedStatement(tests[i], true);
            }
        } finally {
            dropGGKTables();
        }

        nextID = 1;
        count = 0;

        System.out.println("Using PreparedStatement.execute()\n");

        try {
            createGGKTables();

            //Do the tests
            for (int i = 0; i < tests.length; i++) {
                doGGKTestPreparedStatement(tests[i], false);
            }
        } finally {
            dropGGKTables();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception ...
     */
    public void testLimitAndMaxRows() throws Exception {
        try {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testMaxRowsAndLimit");
            this.stmt.executeUpdate(
                "CREATE TABLE testMaxRowsAndLimit(limitField INT)");

            for (int i = 0; i < 500; i++) {
                this.stmt.executeUpdate(
                    "INSERT INTO testMaxRowsAndLimit VALUES (" + i + ")");
            }

            this.stmt.setMaxRows(250);
            this.stmt.executeQuery("SELECT limitField FROM testMaxRowsAndLimit");
        } finally {
            this.stmt.setMaxRows(0);

            this.stmt.executeUpdate("DROP TABLE IF EXISTS testMaxRowsAndLimit");
        }
    }

    /**
     * Tests that 'LOAD DATA LOCAL INFILE' works
     *
     * @throws Exception if any errors occur
     */
    public void testLoadData() throws Exception {
        try {
            int maxAllowedPacket = 1048576;

            stmt.executeUpdate("DROP TABLE IF EXISTS loadDataRegress");
            stmt.executeUpdate(
                "CREATE TABLE loadDataRegress (field1 int, field2 int)");

            File tempFile = File.createTempFile("mysql", ".txt");

            //tempFile.deleteOnExit();
            System.out.println(tempFile);

            Writer out = new FileWriter(tempFile);

            int count = 0;
            int rowCount = 128; //maxAllowedPacket * 4;

            for (int i = 0; i < rowCount; i++) {
                out.write((count++) + "\t" + (count++) + "\n");
            }

            out.close();

            StringBuffer fileNameBuf = null;

            if (File.separatorChar == '\\') {
                fileNameBuf = new StringBuffer();

                String fileName = tempFile.getAbsolutePath();
                int fileNameLength = fileName.length();

                for (int i = 0; i < fileNameLength; i++) {
                    char c = fileName.charAt(i);

                    if (c == '\\') {
                        fileNameBuf.append("/");
                    } else {
                        fileNameBuf.append(c);
                    }
                }
            } else {
                fileNameBuf = new StringBuffer(tempFile.getAbsolutePath());
            }

            int updateCount = stmt.executeUpdate("LOAD DATA LOCAL INFILE '"
                    + fileNameBuf.toString() + "' INTO TABLE loadDataRegress");
            assertTrue(updateCount == rowCount);
        } finally {
            stmt.executeUpdate("DROP TABLE IF EXISTS loadDataRegress");
        }
    }

    /**
     * Tests fix for BUG#1658
     *
     * @throws Exception if the fix for parameter bounds checking doesn't work.
     */
    public void testParameterBoundsCheck() throws Exception {
        PreparedStatement pstmt = this.conn.prepareStatement(
                "UPDATE FOO	SET f1=?, f2=?,f3=?,f4 WHERE f5=?");

        pstmt.setString(1, "");
        pstmt.setString(2, "");

        try {
            pstmt.setString(25, "");
            pstmt.setInt(26, 1);
        } catch (SQLException sqlEx) {
            assertTrue(SQLError.SQL_STATE_ILLEGAL_ARGUMENT.equals(
                    sqlEx.getSQLState()));
        }
    }

    /**
     * Tests fix for BUG#1511
     *
     * @throws Exception if the quoteid parsing fix in PreparedStatement
     *         doesn't work.
     */
    public void testQuotedIdRecognition() throws Exception {
        try {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testQuotedId");
            this.stmt.executeUpdate(
                "CREATE TABLE testQuotedId (col1 VARCHAR(32))");

            PreparedStatement pStmt = this.conn.prepareStatement(
                    "SELECT * FROM testQuotedId FROM WHERE col1='ABC`DEF' or col1=?");
            pStmt.setString(1, "foo");
        } finally {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testQuotedId");
        }
    }

    /**
     * Tests PreparedStatement.setCharacterStream() to ensure it accepts > 4K
     * streams
     *
     * @throws Exception if an error occurs.
     */
    public void testSetCharacterStream() throws Exception {
        try {
            stmt.executeUpdate("DROP TABLE IF EXISTS charStreamRegressTest");
            stmt.executeUpdate(
                "CREATE TABLE charStreamRegressTest(field1 text)");

            pstmt = conn.prepareStatement(
                    "INSERT INTO charStreamRegressTest VALUES (?)");

            char[] charBuf = new char[16384];

            for (int i = 0; i < charBuf.length; i++) {
                charBuf[i] = 'A';
            }

            CharArrayReader reader = new CharArrayReader(charBuf);

            pstmt.setCharacterStream(1, reader, charBuf.length);
            pstmt.executeUpdate();

            rs = stmt.executeQuery("SELECT field1 FROM charStreamRegressTest");

            rs.next();

            String result = rs.getString(1);

            assertTrue(result.length() == charBuf.length);

            stmt.execute("TRUNCATE TABLE charStreamRegressTest");

            // Test that EOF is not thrown
            reader = new CharArrayReader(charBuf);
            pstmt.setCharacterStream(1, reader, (charBuf.length * 2));
            pstmt.executeUpdate();

            rs = stmt.executeQuery("SELECT field1 FROM charStreamRegressTest");

            rs.next();

            result = rs.getString(1);

            assertTrue("Retrieved value of length " + result.length()
                + " != length of inserted value " + charBuf.length,
                result.length() == charBuf.length);

            // Test single quotes inside identifers
            stmt.executeUpdate("DROP TABLE IF EXISTS `charStream'RegressTest`");
            stmt.executeUpdate(
                "CREATE TABLE `charStream'RegressTest`(field1 text)");

            pstmt = conn.prepareStatement(
                    "INSERT INTO `charStream'RegressTest` VALUES (?)");

            reader = new CharArrayReader(charBuf);
            pstmt.setCharacterStream(1, reader, (charBuf.length * 2));
            pstmt.executeUpdate();

            rs = stmt.executeQuery(
                    "SELECT field1 FROM `charStream'RegressTest`");

            rs.next();

            result = rs.getString(1);

            assertTrue("Retrieved value of length " + result.length()
                + " != length of inserted value " + charBuf.length,
                result.length() == charBuf.length);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception ex) {
                    // ignore
                }

                rs = null;
            }

            stmt.executeUpdate("DROP TABLE IF EXISTS `charStream'RegressTest`");
            stmt.executeUpdate("DROP TABLE IF EXISTS charStreamRegressTest");
        }
    }

    /**
     * Tests a bug where Statement.setFetchSize() does not work for values
     * other than 0 or Integer.MIN_VALUE
     *
     * @throws Exception if any errors occur
     */
    public void testSetFetchSize() throws Exception {
        int oldFetchSize = stmt.getFetchSize();

        try {
            stmt.setFetchSize(10);
        } finally {
            stmt.setFetchSize(oldFetchSize);
        }
    }

    /**
     * Tests fix for BUG#907
     *
     * @throws Exception if an error occurs
     */
    public void testSetMaxRows() throws Exception {
        Statement maxRowsStmt = null;

        try {
            maxRowsStmt = this.conn.createStatement();
            maxRowsStmt.setMaxRows(1);
            maxRowsStmt.executeQuery("SELECT 1");
        } finally {
            if (maxRowsStmt != null) {
                maxRowsStmt.close();
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testUpdatableStream() throws Exception {
        try {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS updateStreamTest");
            this.stmt.executeUpdate(
                "CREATE TABLE updateStreamTest (keyField INT NOT NULL AUTO_INCREMENT PRIMARY KEY, field1 BLOB)");

            int streamLength = 16385;
            byte[] streamData = new byte[streamLength];

            /* create an updatable statement */
            Statement updStmt = this.conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);

            /* fill the resultset with some values */
            ResultSet updRs = updStmt.executeQuery(
                    "SELECT * FROM updateStreamTest");

            /* move to insertRow */
            updRs.moveToInsertRow();

            /* update the table */
            updRs.updateBinaryStream("field1",
                new ByteArrayInputStream(streamData), streamLength);

            updRs.insertRow();
        } finally {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS updateStreamTest");
        }
    }

    private void createGGKTables() throws Exception {
        //Delete and recreate table
        dropGGKTables();

        this.stmt.executeUpdate("CREATE TABLE testggk ("
            + "id INT AUTO_INCREMENT NOT NULL PRIMARY KEY,"
            + "val INT NOT NULL" + ")");
    }

    private void doGGKTestPreparedStatement(int[] values, boolean useUpdate)
        throws Exception {
        //Generate the the multiple replace command
        StringBuffer cmd = new StringBuffer("REPLACE INTO testggk VALUES ");
        int newKeys = 0;

        for (int i = 0; i < values.length; i++) {
            cmd.append("(");

            if (values[i] == 0) {
                cmd.append("NULL");
                newKeys += 1;
            } else {
                cmd.append(values[i]);
            }

            cmd.append(", ");
            cmd.append(count++);
            cmd.append("), ");
        }

        cmd.setLength(cmd.length() - 2); //trim the final ", "

        //execute and print it
        System.out.println(cmd.toString());

        PreparedStatement pStmt = this.conn.prepareStatement(cmd.toString(),
                Statement.RETURN_GENERATED_KEYS);

        if (useUpdate) {
            pStmt.executeUpdate();
        } else {
            pStmt.execute();
        }

        //print out what actually happened
        System.out.println("Expect " + newKeys
            + " generated keys, starting from " + nextID);

        ResultSet rs = pStmt.getGeneratedKeys();
        StringBuffer res = new StringBuffer("Got keys");

        int[] generatedKeys = new int[newKeys];
        int i = 0;

        while (rs.next()) {
            if (i < generatedKeys.length) {
                generatedKeys[i++] = rs.getInt(1);
            }

            res.append(" " + rs.getInt(1));
        }

        int numberOfGeneratedKeys = i;

        assertTrue(
            "Didn't retrieve expected number of generated keys, expected "
            + newKeys + ", found " + numberOfGeneratedKeys,
            numberOfGeneratedKeys == newKeys);
        assertTrue("Keys didn't start with correct sequence: ",
            generatedKeys[0] == nextID);

        System.out.println(res.toString());

        //Read and print the new state of the table
        rs = stmt.executeQuery("SELECT id, val FROM testggk");
        System.out.println("New table contents ");

        while (rs.next())
            System.out.println("Id " + rs.getString(1) + " val "
                + rs.getString(2));

        //Tidy up
        System.out.println("");
        nextID += newKeys;
    }

    /**
                    	  */
    private void doGGKTestStatement(int[] values, boolean useUpdate)
        throws Exception {
        //Generate the the multiple replace command
        StringBuffer cmd = new StringBuffer("REPLACE INTO testggk VALUES ");
        int newKeys = 0;

        for (int i = 0; i < values.length; i++) {
            cmd.append("(");

            if (values[i] == 0) {
                cmd.append("NULL");
                newKeys += 1;
            } else {
                cmd.append(values[i]);
            }

            cmd.append(", ");
            cmd.append(count++);
            cmd.append("), ");
        }

        cmd.setLength(cmd.length() - 2); //trim the final ", "

        //execute and print it
        System.out.println(cmd.toString());

        if (useUpdate) {
            stmt.executeUpdate(cmd.toString(), Statement.RETURN_GENERATED_KEYS);
        } else {
            stmt.execute(cmd.toString(), Statement.RETURN_GENERATED_KEYS);
        }

        //print out what actually happened
        System.out.println("Expect " + newKeys
            + " generated keys, starting from " + nextID);

        ResultSet rs = stmt.getGeneratedKeys();
        StringBuffer res = new StringBuffer("Got keys");

        int[] generatedKeys = new int[newKeys];
        int i = 0;

        while (rs.next()) {
            if (i < generatedKeys.length) {
                generatedKeys[i++] = rs.getInt(1);
            }

            res.append(" " + rs.getInt(1));
        }

        int numberOfGeneratedKeys = i;

        assertTrue(
            "Didn't retrieve expected number of generated keys, expected "
            + newKeys + ", found " + numberOfGeneratedKeys,
            numberOfGeneratedKeys == newKeys);
        assertTrue("Keys didn't start with correct sequence: ",
            generatedKeys[0] == nextID);

        System.out.println(res.toString());

        //Read and print the new state of the table
        rs = stmt.executeQuery("SELECT id, val FROM testggk");
        System.out.println("New table contents ");

        while (rs.next())
            System.out.println("Id " + rs.getString(1) + " val "
                + rs.getString(2));

        //Tidy up
        System.out.println("");
        nextID += newKeys;
    }

    private void dropGGKTables() throws Exception {
        this.stmt.executeUpdate("DROP TABLE IF EXISTS testggk");
    }
}
