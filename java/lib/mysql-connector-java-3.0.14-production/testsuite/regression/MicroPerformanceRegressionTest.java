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

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.sql.Timestamp;

import java.util.HashMap;
import java.util.Map;


/**
 * Microperformance benchmarks to track increase/decrease in performance of
 * core methods in the driver over time.
 *
 * @author Mark Matthews
 * @version $Id: MicroPerformanceRegressionTest.java,v 1.1.2.4 2004/04/24 15:49:45 mmatthew Exp $
 */
public class MicroPerformanceRegressionTest extends BaseTestCase {
    private final static int ORIGINAL_LOOP_TIME_MS = 2300;
    private final static Map BASELINE_TIMES = new HashMap();

    static {
        BASELINE_TIMES.put("ResultSet.getInt()", new Double(0.00661));
        BASELINE_TIMES.put("ResultSet.getDouble()", new Double(0.00671));
        BASELINE_TIMES.put("ResultSet.getTime()", new Double(0.02033));
        BASELINE_TIMES.put("ResultSet.getTimestamp()", new Double(0.02363));
        BASELINE_TIMES.put("ResultSet.getDate()", new Double(0.02223));
        BASELINE_TIMES.put("ResultSet.getString()", new Double(0.00982));
        BASELINE_TIMES.put("ResultSet.getObject() on a string",
            new Double(0.00861));
        BASELINE_TIMES.put("Connection.prepareStatement()", new Double(0.18547));
        BASELINE_TIMES.put("PreparedStatement.setInt()", new Double(0.0011));
        BASELINE_TIMES.put("PreparedStatement.setDouble()", new Double(0.00671));
        BASELINE_TIMES.put("PreparedStatement.setTime()", new Double(0.0642));
        BASELINE_TIMES.put("PreparedStatement.setTimestamp()",
            new Double(0.03184));
        BASELINE_TIMES.put("PreparedStatement.setDate()", new Double(0.12248));
        BASELINE_TIMES.put("PreparedStatement.setString()", new Double(0.01512));
        BASELINE_TIMES.put("PreparedStatement.setObject() on a string",
            new Double(0.01923));
    }

    private final double LEEWAY = 2.0;
    private double scaleFactor = 1.0;

    /**
     * Creates a new MicroPerformanceRegressionTest object.
     *
     * @param name DOCUMENT ME!
     */
    public MicroPerformanceRegressionTest(String name) {
        super(name);
    }

    /**
     * Runs all test cases in this test suite
     *
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(MicroPerformanceRegressionTest.class);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    public void setUp() throws Exception {
        super.setUp();

        System.out.println("Calculating performance scaling factor...");

        // Run this simple test to get some sort of performance scaling factor, compared to
        // the development environment. This should help reduce false-positives on this test.
        int numLoops = 10000;

        long start = System.currentTimeMillis();

        for (int j = 0; j < 2000; j++) {
            StringBuffer buf = new StringBuffer(numLoops);

            for (int i = 0; i < numLoops; i++) {
                buf.append('a');
            }
        }

        long elapsedTime = System.currentTimeMillis() - start;

        System.out.println("Elapsed time for factor: " + elapsedTime);

        this.scaleFactor = (double) elapsedTime / (double) ORIGINAL_LOOP_TIME_MS;

        System.out.println("Performance scaling factor is: " + this.scaleFactor);
    }

    /**
     * Tests result set accessors performance.
     *
     * @throws Exception if the performance of these methods does not meet
     *         expectations.
     */
    public void testResultSetAccessors() throws Exception {
        try {
            this.stmt.executeUpdate(
                "DROP TABLE IF EXISTS testResultSetAccessors");
            this.stmt.executeUpdate(
                "CREATE TABLE testResultSetAccessors(intField INT, floatField DOUBLE, timeField TIME, datetimeField DATETIME, stringField VARCHAR(64))");
            this.stmt.executeUpdate(
                "INSERT INTO testResultSetAccessors VALUES (123456789, 12345.6789, NOW(), NOW(), 'abcdefghijklmnopqrstuvABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@')");

            this.rs = this.stmt.executeQuery(
                    "SELECT intField, floatField, timeField, datetimeField, stringField FROM testResultSetAccessors");

            this.rs.next();

            int numLoops = 100000;

            long start = System.currentTimeMillis();

            for (int i = 0; i < numLoops; i++) {
                rs.getInt(1);
            }

            double getIntAvgMs = (double) (System.currentTimeMillis() - start) / numLoops;

            checkTime("ResultSet.getInt()", getIntAvgMs);

            start = System.currentTimeMillis();

            for (int i = 0; i < numLoops; i++) {
                rs.getDouble(2);
            }

            double getDoubleAvgMs = (double) (System.currentTimeMillis()
                - start) / numLoops;

            checkTime("ResultSet.getDouble()", getDoubleAvgMs);

            start = System.currentTimeMillis();

            for (int i = 0; i < numLoops; i++) {
                rs.getTime(3);
            }

            double getTimeAvgMs = (double) (System.currentTimeMillis() - start) / numLoops;

            checkTime("ResultSet.getTime()", getTimeAvgMs);

            start = System.currentTimeMillis();

            for (int i = 0; i < numLoops; i++) {
                rs.getTimestamp(4);
            }

            double getTimestampAvgMs = (double) (System.currentTimeMillis()
                - start) / numLoops;

            checkTime("ResultSet.getTimestamp()", getTimestampAvgMs);

            start = System.currentTimeMillis();

            for (int i = 0; i < numLoops; i++) {
                rs.getDate(4);
            }

            double getDateAvgMs = (double) (System.currentTimeMillis() - start) / numLoops;

            checkTime("ResultSet.getDate()", getDateAvgMs);

            start = System.currentTimeMillis();

            for (int i = 0; i < numLoops; i++) {
                rs.getString(5);
            }

            double getStringAvgMs = (double) (System.currentTimeMillis()
                - start) / numLoops;

            checkTime("ResultSet.getString()", getStringAvgMs);

            start = System.currentTimeMillis();

            for (int i = 0; i < numLoops; i++) {
                rs.getObject(5);
            }

            double getStringObjAvgMs = (double) (System.currentTimeMillis()
                - start) / numLoops;

            checkTime("ResultSet.getObject() on a string", getStringObjAvgMs);

            start = System.currentTimeMillis();

            int numPrepares = 100000;

            for (int i = 0; i < numPrepares; i++) {
                PreparedStatement pStmt = this.conn.prepareStatement(
                        "INSERT INTO testResultSetAccessors VALUES (?, ?, ?, ?, ?)");
                pStmt.close();
            }

            double getPrepareStmtAvgMs = (double) (System.currentTimeMillis()
                - start) / numPrepares;

            checkTime("Connection.prepareStatement()", getPrepareStmtAvgMs);

            PreparedStatement pStmt = this.conn.prepareStatement(
                    "INSERT INTO testResultSetAccessors VALUES (?, ?, ?, ?, ?)");

            start = System.currentTimeMillis();

            for (int i = 0; i < numLoops; i++) {
                pStmt.setInt(1, 1);
            }

            double setIntAvgMs = (double) (System.currentTimeMillis() - start) / numLoops;

            checkTime("PreparedStatement.setInt()", setIntAvgMs);

            start = System.currentTimeMillis();

            for (int i = 0; i < numLoops; i++) {
                pStmt.setDouble(2, 1234567890.1234);
            }

            double setDoubleAvgMs = (double) (System.currentTimeMillis()
                - start) / numLoops;

            checkTime("PreparedStatement.setDouble()", getDoubleAvgMs);

            start = System.currentTimeMillis();

            Time tm = new Time(start);

            for (int i = 0; i < numLoops; i++) {
                pStmt.setTime(3, tm);
            }

            double setTimeAvgMs = (double) (System.currentTimeMillis() - start) / numLoops;

            checkTime("PreparedStatement.setTime()", setTimeAvgMs);

            start = System.currentTimeMillis();

            Timestamp ts = new Timestamp(start);

            for (int i = 0; i < numLoops; i++) {
                pStmt.setTimestamp(4, ts);
            }

            double setTimestampAvgMs = (double) (System.currentTimeMillis()
                - start) / numLoops;

            checkTime("PreparedStatement.setTimestamp()", setTimestampAvgMs);

            start = System.currentTimeMillis();

            Date dt = new Date(start);

            for (int i = 0; i < numLoops; i++) {
                pStmt.setDate(4, dt);
            }

            double setDateAvgMs = (double) (System.currentTimeMillis() - start) / numLoops;

            checkTime("PreparedStatement.setDate()", setDateAvgMs);

            start = System.currentTimeMillis();

            for (int i = 0; i < numLoops; i++) {
                pStmt.setString(5,
                    "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@");
            }

            double setStringAvgMs = (double) (System.currentTimeMillis()
                - start) / numLoops;

            checkTime("PreparedStatement.setString()", setStringAvgMs);

            start = System.currentTimeMillis();

            for (int i = 0; i < numLoops; i++) {
                pStmt.setObject(5,
                    "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@");
            }

            double setStringObjAvgMs = (double) (System.currentTimeMillis()
                - start) / numLoops;

            checkTime("PreparedStatement.setObject() on a string",
                setStringObjAvgMs);

            start = System.currentTimeMillis();
        } finally {
            this.stmt.executeUpdate(
                "DROP TABLE IF EXISTS testResultSetAccessors");
        }
    }

    private void checkTime(String testType, double avgExecTimeMs)
        throws Exception {
        System.out.println("Execution time for " + testType + ": "
            + avgExecTimeMs);

        Double baselineExecTimeMs = (Double) BASELINE_TIMES.get(testType);

        if (baselineExecTimeMs == null) {
            throw new Exception("No baseline time recorded for test '"
                + testType + "'");
        }

        double acceptableTime = LEEWAY * baselineExecTimeMs.doubleValue() * this.scaleFactor;

        assertTrue("Average execution time of " + avgExecTimeMs
            + " ms. exceeded baseline * leeway of " + acceptableTime + " ms.",
            (avgExecTimeMs <= acceptableTime));
    }
}
