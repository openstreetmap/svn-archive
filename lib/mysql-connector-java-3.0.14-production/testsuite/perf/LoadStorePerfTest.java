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
package testsuite.perf;

import testsuite.BaseTestCase;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.text.NumberFormat;


/**
 * Simple performance testing unit test.
 *
 * @author Mark Matthews
 */
public class LoadStorePerfTest extends BasePerfTest {
    /** The table type to use (only for MySQL), 'HEAP' by default */
    private String tableType = "HEAP";
    private boolean takeMeasurements = false;
    private boolean useColumnNames = false;

    /**
     * Constructor for LoadStorePerfTest.
     *
     * @param name the name of the test to run
     */
    public LoadStorePerfTest(String name) {
        super(name);

        String newTableType = System.getProperty(
                "com.mysql.jdbc.test.tabletype");

        if ((newTableType != null) && (newTableType.length() > 0)) {
            this.tableType = newTableType;

            System.out.println("Using specified table type of '" + tableType
                + "'");
        }
    }

    /**
     * Runs all test cases in this test suite
     *
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(LoadStorePerfTest.class);
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    public void setUp() throws Exception {
        super.setUp();

        try {
            stmt.executeUpdate("DROP TABLE perfLoadStore");
        } catch (SQLException sqlEx) {
            // ignore
        }

        String dateTimeType = "DATETIME";

        if (BaseTestCase.dbUrl.indexOf("oracle") != -1) {
            dateTimeType = "TIMESTAMP";
        }

        //
        // Approximate a run-of-the-mill entity in a business application
        //
        String query = "CREATE TABLE perfLoadStore (priKey INT NOT NULL, "
            + "fk1 INT NOT NULL, " + "fk2 INT NOT NULL, " + "dtField "
            + dateTimeType + ", " + "charField1 CHAR(32), "
            + "charField2 CHAR(32), " + "charField3 CHAR(32), "
            + "charField4 CHAR(32), " + "intField1 INT, " + "intField2 INT, "
            + "intField3 INT, " + "intField4 INT, " + "doubleField1 DECIMAL,"
            + "doubleField2 DOUBLE," + "doubleField3 DOUBLE,"
            + "doubleField4 DOUBLE," + "PRIMARY KEY (priKey))";

        if (BaseTestCase.dbUrl.indexOf("mysql") != -1) {
            query += (" TYPE=" + tableType);
        }

        stmt.executeUpdate(query);

        String currentDateValue = "NOW()";

        if (BaseTestCase.dbUrl.indexOf("sqlserver") != -1) {
            currentDateValue = "GETDATE()";
        }

        if (BaseTestCase.dbUrl.indexOf("oracle") != -1) {
            currentDateValue = "CURRENT_TIMESTAMP";
        }

        stmt.executeUpdate("INSERT INTO perfLoadStore (" + "priKey, " + "fk1, "
            + "fk2, " + "dtField, " + "charField1, " + "charField2, "
            + "charField3, " + "charField4, " + "intField1, " + "intField2, "
            + "intField3, " + "intField4, " + "doubleField1," + "doubleField2,"
            + "doubleField3," + "doubleField4" + ") VALUES (" + "1," // priKey
            + "2," // fk1
            + "3," // fk2
            + currentDateValue + "," // dtField
            + "'0123456789ABCDEF0123456789ABCDEF'," // charField1
            + "'0123456789ABCDEF0123456789ABCDEF'," // charField2
            + "'0123456789ABCDEF0123456789ABCDEF'," // charField3
            + "'0123456789ABCDEF0123456789ABCDEF'," // charField4
            + "7," // intField1
            + "8," // intField2
            + "9," // intField3
            + "10," // intField4
            + "1.20," // doubleField1
            + "2.30," // doubleField2
            + "3.40," // doubleField3
            + "4.50" // doubleField4
            + ")");
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    public void tearDown() throws Exception {
        try {
            stmt.executeUpdate("DROP TABLE perfLoadStore");
        } catch (SQLException sqlEx) {
            // ignore
        }

        super.tearDown();
    }

    /**
     * Tests and times 1000 load/store type transactions
     *
     * @throws Exception if an error occurs
     */
    public void test1000Transactions() throws Exception {
        this.takeMeasurements = false;
        warmUp();
        this.takeMeasurements = true;
        doIterations(29);

        reportResults("\n\nResults for instance # 1: ");
    }

    /**
     * Runs one iteration of the test.
     *
     * @see testsuite.perf.BasePerfTest#doOneIteration()
     */
    protected void doOneIteration() throws Exception {
        PreparedStatement pStmtStore = conn.prepareStatement(
                "UPDATE perfLoadStore SET " + "priKey = ?, " + "fk1 = ?, "
                + "fk2 = ?, " + "dtField = ?, " + "charField1 = ?, "
                + "charField2 = ?, " + "charField3 = ?, " + "charField4 = ?, "
                + "intField1 = ?, " + "intField2 = ?, " + "intField3 = ?, "
                + "intField4 = ?, " + "doubleField1 = ?," + "doubleField2 = ?,"
                + "doubleField3 = ?," + "doubleField4 = ?" + " WHERE priKey=?");
        PreparedStatement pStmtCheck = conn.prepareStatement(
                "SELECT COUNT(*) FROM perfLoadStore WHERE priKey=?");
        PreparedStatement pStmtLoad = conn.prepareStatement("SELECT "
                + "priKey, " + "fk1, " + "fk2, " + "dtField, " + "charField1, "
                + "charField2, " + "charField3, " + "charField4, "
                + "intField1, " + "intField2, " + "intField3, " + "intField4, "
                + "doubleField1," + "doubleField2, " + "doubleField3,"
                + "doubleField4" + " FROM perfLoadStore WHERE priKey=?");

        NumberFormat numFormatter = NumberFormat.getInstance();
        numFormatter.setMaximumFractionDigits(4);
        numFormatter.setMinimumFractionDigits(4);

        int transactionCount = 5000;

        long begin = System.currentTimeMillis();

        for (int i = 0; i < transactionCount; i++) {
            conn.setAutoCommit(false);
            pStmtCheck.setInt(1, 1);
            rs = pStmtCheck.executeQuery();

            while (rs.next()) {
                rs.getInt(1);
            }

            rs.close();

            pStmtLoad.setInt(1, 1);
            rs = pStmtLoad.executeQuery();

            while (rs.next()) {
                int key = rs.getInt(1);

                if (!useColumnNames) {
                    pStmtStore.setInt(1, key); // priKey
                    pStmtStore.setInt(2, rs.getInt(2)); // fk1
                    pStmtStore.setInt(3, rs.getInt(3)); // fk2
                    pStmtStore.setTimestamp(4, rs.getTimestamp(4)); // dtField
                    pStmtStore.setString(5, rs.getString(5)); // charField1
                    pStmtStore.setString(6, rs.getString(7)); // charField2
                    pStmtStore.setString(7, rs.getString(7)); // charField3
                    pStmtStore.setString(8, rs.getString(8)); // charField4
                    pStmtStore.setInt(9, rs.getInt(9)); // intField1
                    pStmtStore.setInt(10, rs.getInt(10)); // intField2
                    pStmtStore.setInt(11, rs.getInt(11)); // intField3
                    pStmtStore.setInt(12, rs.getInt(12)); // intField4
                    pStmtStore.setDouble(13, rs.getDouble(13)); // doubleField1
                    pStmtStore.setDouble(14, rs.getDouble(14)); // doubleField2
                    pStmtStore.setDouble(15, rs.getDouble(15)); // doubleField3
                    pStmtStore.setDouble(16, rs.getDouble(16)); // doubleField4

                    pStmtStore.setInt(17, key);
                } else {
                    /*
                     * "UPDATE perfLoadStore SET " + "priKey = ?, " + "fk1 = ?, "
                    + "fk2 = ?, " + "dtField = ?, " + "charField1 = ?, "
                    + "charField2 = ?, " + "charField3 = ?, " + "charField4 = ?, "
                    + "intField1 = ?, " + "intField2 = ?, " + "intField3 = ?, "
                    + "intField4 = ?, " + "doubleField1 = ?," + "doubleField2 = ?,"
                    + "doubleField3 = ?," + "doubleField4 = ?" + " WHERE priKey=?");
                     */
                    pStmtStore.setInt(1, key); // priKey
                    pStmtStore.setInt(2, rs.getInt("fk1")); // fk1
                    pStmtStore.setInt(3, rs.getInt("fk2")); // fk2
                    pStmtStore.setTimestamp(4, rs.getTimestamp("dtField")); // dtField
                    pStmtStore.setString(5, rs.getString("charField1")); // charField1
                    pStmtStore.setString(6, rs.getString("charField2")); // charField2
                    pStmtStore.setString(7, rs.getString("charField3")); // charField3
                    pStmtStore.setString(8, rs.getString("charField4")); // charField4
                    pStmtStore.setInt(9, rs.getInt("intField1")); // intField1
                    pStmtStore.setInt(10, rs.getInt("intField2")); // intField2
                    pStmtStore.setInt(11, rs.getInt("intField3")); // intField3
                    pStmtStore.setInt(12, rs.getInt("intField4")); // intField4
                    pStmtStore.setDouble(13, rs.getDouble("doubleField1")); // doubleField1
                    pStmtStore.setDouble(14, rs.getDouble("doubleField2")); // doubleField2
                    pStmtStore.setDouble(15, rs.getDouble("doubleField3")); // doubleField3
                    pStmtStore.setDouble(16, rs.getDouble("doubleField4")); // doubleField4

                    pStmtStore.setInt(17, key);
                }

                pStmtStore.executeUpdate();
            }

            rs.close();

            conn.commit();
            conn.setAutoCommit(true);
        }

        pStmtStore.close();
        pStmtCheck.close();
        pStmtLoad.close();

        long end = System.currentTimeMillis();

        long timeElapsed = (end - begin);

        double timeElapsedSeconds = (double) timeElapsed / 1000;
        double tps = transactionCount / timeElapsedSeconds;

        if (this.takeMeasurements) {
            addResult(tps);
            System.out.print("1 [ " + numFormatter.format(getMeanValue())
                + " ] ");
        } else {
            System.out.println("Warm-up: " + tps + " trans/sec");
        }
    }

    /**
     * Runs the test 10 times to get JIT going, and GC going
     *
     * @throws Exception if an error occurs.
     */
    protected void warmUp() throws Exception {
        try {
            System.out.print("Warm-up period (10 iterations)");

            for (int i = 0; i < 10; i++) {
                doOneIteration();
                System.out.print(".");
            }

            System.out.println();
            System.out.println("Warm-up period ends");
            System.out.println("\nUnits for this test are transactions/sec.");
        } catch (Exception ex) {
            ex.printStackTrace();

            throw ex;
        }
    }
}
