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

import com.mysql.jdbc.Driver;

import testsuite.BaseTestCase;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.util.HashMap;


/**
 * Regression tests for DatabaseMetaData
 *
 * @author Mark Matthews
 * @version $Id: MetaDataRegressionTest.java,v 1.3.2.12 2004/04/27 16:12:27 mmatthew Exp $
 */
public class MetaDataRegressionTest extends BaseTestCase {
    /**
     * Creates a new MetaDataRegressionTest.
     *
     * @param name the name of the test
     */
    public MetaDataRegressionTest(String name) {
        super(name);
    }

    /**
     * Runs all test cases in this test suite
     *
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(MetaDataRegressionTest.class);
    }

    /**
     * Tests fix for BUG#2852, where RSMD is not returning correct (or
     * matching) types for TINYINT and SMALLINT.
     *
     * @throws Exception if the test fails.
     */
    public void testBug2852() throws Exception {
        try {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug2852");
            this.stmt.executeUpdate(
                "CREATE TABLE testBug2852 (field1 TINYINT, field2 SMALLINT)");
            this.stmt.executeUpdate("INSERT INTO testBug2852 VALUES (1,1)");

            this.rs = this.stmt.executeQuery("SELECT * from testBug2852");

            assertTrue(this.rs.next());

            ResultSetMetaData rsmd = this.rs.getMetaData();

            assertTrue(rsmd.getColumnClassName(1).equals(rs.getObject(1)
                                                           .getClass().getName()));
            assertTrue("java.lang.Integer".equals(rsmd.getColumnClassName(1)));

            assertTrue(rsmd.getColumnClassName(2).equals(rs.getObject(2)
                                                           .getClass().getName()));
            assertTrue("java.lang.Integer".equals(rsmd.getColumnClassName(2)));
        } finally {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug2852");
        }
    }

    /**
     * Tests fix for BUG#2855, where RSMD is not returning correct (or
     * matching) types for FLOAT.
     *
     * @throws Exception if the test fails.
     */
    public void testBug2855() throws Exception {
        try {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug2855");
            this.stmt.executeUpdate("CREATE TABLE testBug2855 (field1 FLOAT)");
            this.stmt.executeUpdate("INSERT INTO testBug2855 VALUES (1)");

            this.rs = this.stmt.executeQuery("SELECT * from testBug2855");

            assertTrue(this.rs.next());

            ResultSetMetaData rsmd = this.rs.getMetaData();

            assertTrue(rsmd.getColumnClassName(1).equals(rs.getObject(1)
                                                           .getClass().getName()));
            assertTrue("java.lang.Float".equals(rsmd.getColumnClassName(1)));
        } finally {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug2855");
        }
    }

    /**
     * Tests fix for BUG#3570 -- inconsistent reporting of column type
     *
     * @throws Exception if an error occurs
     */
    public void testBug3570() throws Exception {
        String createTableQuery =
            " CREATE TABLE testBug3570(field_tinyint TINYINT"
            + ",field_smallint SMALLINT" + ",field_mediumint MEDIUMINT"
            + ",field_int INT" + ",field_integer INTEGER"
            + ",field_bigint BIGINT" + ",field_real REAL"
            + ",field_float FLOAT" + ",field_decimal DECIMAL"
            + ",field_numeric NUMERIC" + ",field_double DOUBLE"
            + ",field_char CHAR(3)" + ",field_varchar VARCHAR(255)"
            + ",field_date DATE" + ",field_time TIME" + ",field_year YEAR"
            + ",field_timestamp TIMESTAMP" + ",field_datetime DATETIME"
            + ",field_tinyblob TINYBLOB" + ",field_blob BLOB"
            + ",field_mediumblob MEDIUMBLOB" + ",field_longblob LONGBLOB"
            + ",field_tinytext TINYTEXT" + ",field_text TEXT"
            + ",field_mediumtext MEDIUMTEXT" + ",field_longtext LONGTEXT"
            + ",field_enum ENUM('1','2','3')" + ",field_set SET('1','2','3'))";

        try {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug3570");
            this.stmt.executeUpdate(createTableQuery);

            ResultSet dbmdRs = this.conn.getMetaData().getColumns(this.conn
                    .getCatalog(), null, "testBug3570", "%");

            this.rs = this.stmt.executeQuery("SELECT * FROM testBug3570");

            ResultSetMetaData rsmd = this.rs.getMetaData();

            while (dbmdRs.next()) {
                String columnName = dbmdRs.getString(4);
                int typeFromGetColumns = dbmdRs.getInt(5);
                int typeFromRSMD = rsmd.getColumnType(this.rs.findColumn(
                            columnName));

                //
                // TODO: Server needs to send these types correctly....
                //
                if (!"field_tinyblob".equals(columnName)
                        && !"field_tinytext".equals(columnName)) {
                    assertTrue(columnName + " -> type from DBMD.getColumns("
                        + typeFromGetColumns
                        + ") != type from RSMD.getColumnType(" + typeFromRSMD
                        + ")", typeFromGetColumns == typeFromRSMD);
                }
            }
        } finally {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug3570");
        }
    }

    /**
     * Tests char/varchar bug
     *
     * @throws Exception if any errors occur
     */
    public void testCharVarchar() throws Exception {
        try {
            stmt.execute("DROP TABLE IF EXISTS charVarCharTest");
            stmt.execute("CREATE TABLE charVarCharTest ("
                + "  TableName VARCHAR(64)," + "  FieldName VARCHAR(64),"
                + "  NextCounter INTEGER);");

            String query = "SELECT TableName, FieldName, NextCounter FROM charVarCharTest";
            rs = stmt.executeQuery(query);

            ResultSetMetaData rsmeta = rs.getMetaData();

            assertTrue(rsmeta.getColumnTypeName(1).equalsIgnoreCase("VARCHAR"));

            //			 is "CHAR", expected "VARCHAR"
            assertTrue(rsmeta.getColumnType(1) == 12);

            //			 is 1 (java.sql.Types.CHAR), expected 12 (java.sql.Types.VARCHAR)
        } finally {
            stmt.execute("DROP TABLE IF EXISTS charVarCharTest");
        }
    }

    /**
     * Tests bug reported by OpenOffice team with getColumns and LONGBLOB
     *
     * @throws Exception if any errors occur
     */
    public void testGetColumns() throws Exception {
        try {
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS longblob_regress(field_1 longblob)");

            DatabaseMetaData dbmd = conn.getMetaData();
            ResultSet dbmdRs = null;

            try {
                dbmdRs = dbmd.getColumns("", "", "longblob_regress", "%");

                while (dbmdRs.next()) {
                    dbmdRs.getInt(7);
                }
            } finally {
                if (dbmdRs != null) {
                    try {
                        dbmdRs.close();
                    } catch (SQLException ex) {
                        ;
                    }
                }
            }
        } finally {
            stmt.execute("DROP TABLE IF EXISTS longblob_regress");
        }
    }

    /**
     * Tests fix for Bug#
     *
     * @throws Exception if an error occurs
     */
    public void testGetColumnsBug1099() throws Exception {
        try {
            this.stmt.executeUpdate(
                "DROP TABLE IF EXISTS testGetColumnsBug1099");

            DatabaseMetaData dbmd = this.conn.getMetaData();

            rs = dbmd.getTypeInfo();

            StringBuffer types = new StringBuffer();

            HashMap alreadyDoneTypes = new HashMap();

            while (rs.next()) {
                String typeName = rs.getString("TYPE_NAME");
                String createParams = rs.getString("CREATE_PARAMS");

                if ((typeName.indexOf("BINARY") == -1)
                        && !typeName.equals("LONG VARCHAR")) {
                    if (!alreadyDoneTypes.containsKey(typeName)) {
                        alreadyDoneTypes.put(typeName, null);

                        if (types.length() != 0) {
                            types.append(", \n");
                        }

                        int typeNameLength = typeName.length();
                        StringBuffer safeTypeName = new StringBuffer(typeNameLength);

                        for (int i = 0; i < typeNameLength; i++) {
                            char c = typeName.charAt(i);

                            if (Character.isWhitespace(c)) {
                                safeTypeName.append("_");
                            } else {
                                safeTypeName.append(c);
                            }
                        }

                        types.append(safeTypeName);
                        types.append("Column ");
                        types.append(typeName);

                        if (typeName.indexOf("CHAR") != -1) {
                            types.append(" (1)");
                        } else if (typeName.equalsIgnoreCase("enum")
                                || typeName.equalsIgnoreCase("set")) {
                            types.append("('a', 'b', 'c')");
                        }
                    }
                }
            }

            this.stmt.executeUpdate("CREATE TABLE testGetColumnsBug1099("
                + types.toString() + ")");

            dbmd.getColumns(null, this.conn.getCatalog(),
                "testGetColumnsBug1099", "%");
        } finally {
            this.stmt.executeUpdate(
                "DROP TABLE IF EXISTS testGetColumnsBug1099");
        }
    }

    /**
     * Tests whether or not unsigned columns are reported correctly in
     * DBMD.getColumns
     *
     * @throws Exception
     */
    public void testGetColumnsUnsigned() throws Exception {
        try {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testGetUnsignedCols");
            this.stmt.executeUpdate(
                "CREATE TABLE testGetUnsignedCols (field1 SMALLINT, field2 SMALLINT UNSIGNED)");

            DatabaseMetaData dbmd = this.conn.getMetaData();

            this.rs = dbmd.getColumns(this.conn.getCatalog(), null,
                    "testGetUnsignedCols", "%");

            while (this.rs.next()) {
                System.out.println(rs.getString(6));
            }
        } finally {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testGetUnsignedCols");
        }
    }

    /**
     * Tests whether bogus parameters break Driver.getPropertyInfo().
     *
     * @throws Exception if an error occurs.
     */
    public void testGetPropertyInfo() throws Exception {
        new Driver().getPropertyInfo("", null);
    }

    /**
     * Tests whether ResultSetMetaData returns correct info for CHAR/VARCHAR
     * columns.
     *
     * @throws Exception if the test fails
     */
    public void testIsCaseSensitive() throws Exception {
        try {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testIsCaseSensitive");
            this.stmt.executeUpdate(
                "CREATE TABLE testIsCaseSensitive (bin_char CHAR(1) BINARY, bin_varchar VARCHAR(64) BINARY, ci_char CHAR(1), ci_varchar VARCHAR(64))");
            this.rs = this.stmt.executeQuery(
                    "SELECT bin_char, bin_varchar, ci_char, ci_varchar FROM testIsCaseSensitive");

            ResultSetMetaData rsmd = this.rs.getMetaData();
            assertTrue(rsmd.isCaseSensitive(1));
            assertTrue(rsmd.isCaseSensitive(2));
            assertTrue(!rsmd.isCaseSensitive(3));
            assertTrue(!rsmd.isCaseSensitive(4));
        } finally {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testIsCaseSensitive");
        }
    }

    /**
     * Tests whether or not DatabaseMetaData.getColumns() returns the correct
     * java.sql.Types info.
     *
     * @throws Exception if the test fails.
     */
    public void testLongText() throws Exception {
        try {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testLongText");
            this.stmt.executeUpdate(
                "CREATE TABLE testLongText (field1 LONGTEXT)");

            this.rs = this.conn.getMetaData().getColumns(this.conn.getCatalog(),
                    null, "testLongText", "%");

            this.rs.next();

            assertTrue(this.rs.getInt("DATA_TYPE") == java.sql.Types.LONGVARCHAR);
        } finally {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testLongText");
        }
    }

    /**
     * Tests for types being returned correctly
     *
     * @throws Exception if an error occurs.
     */
    public void testTypes() throws Exception {
        try {
            stmt.execute("DROP TABLE IF EXISTS typesRegressTest");
            stmt.execute("CREATE TABLE typesRegressTest ("
                + "varcharField VARCHAR(32)," + "charField CHAR(2),"
                + "enumField ENUM('1','2')," + "setField  SET('1','2','3'),"
                + "tinyblobField TINYBLOB," + "mediumBlobField MEDIUMBLOB,"
                + "longblobField LONGBLOB," + "blobField BLOB)");

            rs = stmt.executeQuery("SELECT * from typesRegressTest");

            ResultSetMetaData rsmd = rs.getMetaData();

            int numCols = rsmd.getColumnCount();

            for (int i = 0; i < numCols; i++) {
                String columnName = rsmd.getColumnName(i + 1);
                String columnTypeName = rsmd.getColumnTypeName(i + 1);
                System.out.println(columnName + " -> " + columnTypeName);
            }
        } finally {
            stmt.execute("DROP TABLE IF EXISTS typesRegressTest");
        }
    }
}
