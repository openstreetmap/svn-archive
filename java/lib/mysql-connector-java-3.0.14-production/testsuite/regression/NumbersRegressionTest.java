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

import testsuite.BaseTestCase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;


/**
 * Tests various number-handling issues that have arrisen in the JDBC driver at
 * one time or another.
 *
 * @author Mark Matthews
 */
public class NumbersRegressionTest extends BaseTestCase {
    /**
     * Constructor for NumbersRegressionTest.
     *
     * @param name the test name
     */
    public NumbersRegressionTest(String name) {
        super(name);
    }

    /**
     * Runs all test cases in this test suite
     *
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(NumbersRegressionTest.class);
    }

    /**
     * Tests that BIGINT retrieval works correctly
     *
     * @throws Exception if any errors occur
     */
    public void testBigInt() throws Exception {
        try {
            stmt.executeUpdate("DROP TABLE IF EXISTS bigIntRegression");
            stmt.executeUpdate(
                "CREATE TABLE bigIntRegression ( val BIGINT NOT NULL)");
            stmt.executeUpdate(
                "INSERT INTO bigIntRegression VALUES (6692730313872877584)");
            rs = stmt.executeQuery("SELECT val FROM bigIntRegression");

            while (rs.next()) {
                // check retrieval
                long retrieveAsLong = rs.getLong(1);
                assertTrue(retrieveAsLong == 6692730313872877584L);
            }

            rs.close();
            stmt.executeUpdate("DROP TABLE IF EXISTS bigIntRegression");

            String bigIntAsString = "6692730313872877584";

            long parsedBigIntAsLong = Long.parseLong(bigIntAsString);

            // check JDK parsing
            assertTrue(bigIntAsString.equals(String.valueOf(parsedBigIntAsLong)));
        } finally {
            stmt.executeUpdate("DROP TABLE IF EXISTS bigIntRegression");
        }
    }

    /**
     * Tests correct type assignment for MySQL FLOAT and REAL datatypes.
     *
     * @throws Exception if the test fails.
     */
    public void testFloatsAndReals() throws Exception {
        try {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS floatsAndReals");
            this.stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS floatsAndReals(floatCol FLOAT, realCol REAL, doubleCol DOUBLE)");
            this.stmt.executeUpdate(
                "INSERT INTO floatsAndReals VALUES (0, 0, 0)");

            this.rs = this.stmt.executeQuery(
                    "SELECT floatCol, realCol, doubleCol FROM floatsAndReals");

            ResultSetMetaData rsmd = this.rs.getMetaData();

            this.rs.next();

            assertTrue(rsmd.getColumnClassName(1).equals("java.lang.Float"));
            assertTrue(this.rs.getObject(1).getClass().getName().equals("java.lang.Float"));

            assertTrue(rsmd.getColumnClassName(2).equals("java.lang.Double"));
            assertTrue(this.rs.getObject(2).getClass().getName().equals("java.lang.Double"));

            assertTrue(rsmd.getColumnClassName(3).equals("java.lang.Double"));
            assertTrue(this.rs.getObject(3).getClass().getName().equals("java.lang.Double"));
        } finally {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS floatsAndReals");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testIntShouldReturnLong() throws Exception {
        try {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testIntRetLong");
            this.stmt.executeUpdate("CREATE TABLE testIntRetLong(field1 INT)");
            this.stmt.executeUpdate("INSERT INTO testIntRetLong VALUES (1)");

            this.rs = this.stmt.executeQuery("SELECT * FROM testIntRetLong");
            this.rs.next();

            assertTrue(this.rs.getObject(1).getClass().equals(java.lang.Integer.class));
        } finally {
            if (this.rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) {
                    // ignore
                }

                this.rs = null;
            }

            this.stmt.executeUpdate("DROP TABLE IF EXISTS testIntRetLong");
        }
    }

    /**
     * Tests PreparedStatement.setDouble()/setFloat() truncating  values for
     * +/- INF and NaN, and that the corresponding warnings are issued.
     *
     * @throws Exception if the testcase fails.
     */
    public void testPStmtClipping() throws Exception {
        this.rs = this.stmt.executeQuery("SHOW VARIABLES LIKE 'DATADIR'");

        boolean serverOnWindows = false;

        if (this.rs.next()) {
            String dataDir = this.rs.getString(2);

            serverOnWindows = (dataDir != null) && (dataDir.indexOf(":") != -1);
        }

        if (!serverOnWindows) {
            try {
                this.stmt.executeUpdate("DROP TABLE IF EXISTS pStmtClip");
                this.stmt.executeUpdate(
                    "CREATE TABLE pStmtClip(dval DOUBLE, fval FLOAT)");

                PreparedStatement pStmt = this.conn.prepareStatement(
                        "INSERT INTO pStmtClip VALUES (?, ?)");

                pStmt.setDouble(1, Double.NEGATIVE_INFINITY);
                pStmt.setFloat(2, Float.NEGATIVE_INFINITY);
                System.out.println(pStmt.toString());

                pStmt.executeUpdate();

                System.out.println(pStmt);

                SQLWarning warn = pStmt.getWarnings();

                int truncWarnCount = 0;

                while (warn != null) {
                    if ("01004".equals(warn.getSQLState())) {
                        truncWarnCount++;
                    }

                    System.out.println(warn.getMessage());

                    warn = warn.getNextWarning();
                }

                assertTrue((truncWarnCount == 0) || (truncWarnCount == 2));

                ResultSet rs = this.stmt.executeQuery(
                        "SELECT dval, fval FROM pStmtClip");

                rs.next();

                if (truncWarnCount == 0) {
                    assertTrue("-inf".equalsIgnoreCase(rs.getString(1))
                        || "-infinity".equalsIgnoreCase(rs.getString(1)));
                    assertTrue("-3.40282e+38".equalsIgnoreCase(rs.getString(2)));
                }

                this.stmt.executeUpdate("TRUNCATE TABLE pStmtClip");

                pStmt.setDouble(1, Double.POSITIVE_INFINITY);
                pStmt.setFloat(2, Float.POSITIVE_INFINITY);

                pStmt.executeUpdate();

                warn = pStmt.getWarnings();

                truncWarnCount = 0;

                while (warn != null) {
                    if ("01004".equals(warn.getSQLState())) {
                        truncWarnCount++;
                    }

                    System.out.println(warn.getMessage());

                    warn = warn.getNextWarning();
                }

                assertTrue((truncWarnCount == 0) || (truncWarnCount == 2));

                rs = this.stmt.executeQuery("SELECT dval, fval FROM pStmtClip");

                rs.next();

                if (truncWarnCount == 0) {
                    assertTrue("inf".equalsIgnoreCase(rs.getString(1))
                        || "infinity".equalsIgnoreCase(rs.getString(1)));
                    assertTrue("3.40282e+38".equalsIgnoreCase(rs.getString(2)));
                }

                this.stmt.executeUpdate("TRUNCATE TABLE pStmtClip");

                pStmt.setDouble(1, Double.NaN);
                pStmt.setFloat(2, Float.NaN);

                pStmt.executeUpdate();

                warn = pStmt.getWarnings();
                truncWarnCount = 0;

                while (warn != null) {
                    if ("01004".equals(warn.getSQLState())) {
                        truncWarnCount++;
                    }

                    System.out.println(warn.getMessage());

                    warn = warn.getNextWarning();
                }

                assertTrue((truncWarnCount == 0) || (truncWarnCount == 2));

                rs = this.stmt.executeQuery("SELECT dval, fval FROM pStmtClip");

                rs.next();

                if (truncWarnCount == 0) {
                    assertTrue("nan".equalsIgnoreCase(rs.getString(1)));
                    assertTrue("nan".equalsIgnoreCase(rs.getString(2)));
                }
            } finally {
                this.stmt.executeUpdate("DROP TABLE IF EXISTS pStmtClip");
            }
        }
    }

    /**
     * Tests that ResultSetMetaData precision and scale methods work correctly
     * for all numeric types.
     *
     * @throws Exception if any errors occur
     */
    public void testPrecisionAndScale() throws Exception {
        testPrecisionForType("TINYINT", 8, -1, false);
        testPrecisionForType("TINYINT", 8, -1, true);
        testPrecisionForType("SMALLINT", 8, -1, false);
        testPrecisionForType("SMALLINT", 8, -1, true);
        testPrecisionForType("MEDIUMINT", 8, -1, false);
        testPrecisionForType("MEDIUMINT", 8, -1, true);
        testPrecisionForType("INT", 8, -1, false);
        testPrecisionForType("INT", 8, -1, true);
        testPrecisionForType("BIGINT", 8, -1, false);
        testPrecisionForType("BIGINT", 8, -1, true);

        testPrecisionForType("FLOAT", 8, 4, false);
        testPrecisionForType("FLOAT", 8, 4, true);
        testPrecisionForType("DOUBLE", 8, 4, false);
        testPrecisionForType("DOUBLE", 8, 4, true);

        testPrecisionForType("DECIMAL", 8, 4, false);
        testPrecisionForType("DECIMAL", 8, 4, true);

        testPrecisionForType("DECIMAL", 9, 0, false);
        testPrecisionForType("DECIMAL", 9, 0, true);
    }

    private void testPrecisionForType(String typeName, int m, int d,
        boolean unsigned) throws Exception {
        try {
            stmt.executeUpdate(
                "DROP TABLE IF EXISTS precisionAndScaleRegression");

            StringBuffer createStatement = new StringBuffer(
                    "CREATE TABLE precisionAndScaleRegression ( val ");
            createStatement.append(typeName);
            createStatement.append("(");
            createStatement.append(m);

            if (d != -1) {
                createStatement.append(",");
                createStatement.append(d);
            }

            createStatement.append(")");

            if (unsigned) {
                createStatement.append(" UNSIGNED ");
            }

            createStatement.append(" NOT NULL)");

            stmt.executeUpdate(createStatement.toString());

            rs = stmt.executeQuery(
                    "SELECT val FROM precisionAndScaleRegression");

            ResultSetMetaData rsmd = rs.getMetaData();
            assertTrue("Precision returned incorrectly for type " + typeName
                + ", " + m + " != rsmd.getPrecision() = "
                + rsmd.getPrecision(1), rsmd.getPrecision(1) == m);

            if (d != -1) {
                assertTrue("Scale returned incorrectly for type " + typeName
                    + ", " + d + " != rsmd.getScale() = " + rsmd.getScale(1),
                    rsmd.getScale(1) == d);
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception ex) {
                    // ignore
                }
            }

            stmt.executeUpdate(
                "DROP TABLE IF EXISTS precisionAndScaleRegression");
        }
    }
}
