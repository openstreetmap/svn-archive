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

package testsuite.regression;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import testsuite.BaseTestCase;

import com.mysql.jdbc.NonRegisteringDriver;


/**
 * Regression tests for Connections
 *
 * @author Mark Matthews
 * @version $Id: ConnectionRegressionTest.java,v 1.1.2.7 2004/05/17 16:32:41 mmatthew Exp $
 */
public class ConnectionRegressionTest extends BaseTestCase {
    /**
     * DOCUMENT ME!
     *
     * @param name the name of the testcase
     */
    public ConnectionRegressionTest(String name) {
        super(name);
    }

    /**
     * Runs all test cases in this test suite
     *
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ConnectionRegressionTest.class);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception ...
     */
    public void testBug1914() throws Exception {
        System.out.println(this.conn.nativeSQL(
                "{fn convert(foo(a,b,c), BIGINT)}"));
        System.out.println(this.conn.nativeSQL(
                "{fn convert(foo(a,b,c), BINARY)}"));
        System.out.println(this.conn.nativeSQL("{fn convert(foo(a,b,c), BIT)}"));
        System.out.println(this.conn.nativeSQL("{fn convert(foo(a,b,c), CHAR)}"));
        System.out.println(this.conn.nativeSQL("{fn convert(foo(a,b,c), DATE)}"));
        System.out.println(this.conn.nativeSQL(
                "{fn convert(foo(a,b,c), DECIMAL)}"));
        System.out.println(this.conn.nativeSQL(
                "{fn convert(foo(a,b,c), DOUBLE)}"));
        System.out.println(this.conn.nativeSQL(
                "{fn convert(foo(a,b,c), FLOAT)}"));
        System.out.println(this.conn.nativeSQL(
                "{fn convert(foo(a,b,c), INTEGER)}"));
        System.out.println(this.conn.nativeSQL(
                "{fn convert(foo(a,b,c), LONGVARBINARY)}"));
        System.out.println(this.conn.nativeSQL(
                "{fn convert(foo(a,b,c), LONGVARCHAR)}"));
        System.out.println(this.conn.nativeSQL("{fn convert(foo(a,b,c), TIME)}"));
        System.out.println(this.conn.nativeSQL(
                "{fn convert(foo(a,b,c), TIMESTAMP)}"));
        System.out.println(this.conn.nativeSQL(
                "{fn convert(foo(a,b,c), TINYINT)}"));
        System.out.println(this.conn.nativeSQL(
                "{fn convert(foo(a,b,c), VARBINARY)}"));
        System.out.println(this.conn.nativeSQL(
                "{fn convert(foo(a,b,c), VARCHAR)}"));
    }

    /**
     * Tests if the driver configures character sets correctly for 4.1.x
     * servers.
     *     
     * Requires that the 'admin connection' is configured, as this test
     * needs to create/drop databases.
     *
     * @throws Exception if an error occurs
     */
    public void testCollation41() throws Exception {
        if (versionMeetsMinimum(4, 1) && isAdminConnectionConfigured()) {
            Map charsetsAndCollations = getCharacterSetsAndCollations();
            charsetsAndCollations.remove("latin7"); // Maps to multiple Java charsets
            charsetsAndCollations.remove("ucs2");   // can't be used as a connection charset

            Iterator charsets = charsetsAndCollations.keySet().iterator();

            while (charsets.hasNext()) {
                Connection charsetConn = null;
                Statement charsetStmt = null;

                try {
                    String charsetName = charsets.next().toString();
                    String collationName = charsetsAndCollations.get(charsetName)
                                                                .toString();
                    Properties props = new Properties();
                    props.put("characterEncoding", charsetName);

                    System.out.println("Testing character set " + charsetName);

                    charsetConn = getAdminConnectionWithProps(props);

                    charsetStmt = charsetConn.createStatement();

                    charsetStmt.executeUpdate(
                        "DROP DATABASE IF EXISTS testCollation41");
                    charsetStmt.executeUpdate(
                        "DROP TABLE IF EXISTS testCollation41");

                    charsetStmt.executeUpdate(
                        "CREATE DATABASE testCollation41 DEFAULT CHARACTER SET "
                        + charsetName);
                    charsetConn.setCatalog("testCollation41");

                    // We've switched catalogs, so we need to recreate the statement to pick this up...
                    charsetStmt = charsetConn.createStatement();

                    StringBuffer createTableCommand = new StringBuffer(
                            "CREATE TABLE testCollation41"
                            + "(field1 VARCHAR(255), field2 INT)");

                    charsetStmt.executeUpdate(createTableCommand.toString());

                    charsetStmt.executeUpdate(
                        "INSERT INTO testCollation41 VALUES ('abc', 0)");

                    int updateCount = charsetStmt.executeUpdate(
                            "UPDATE testCollation41 SET field2=1 WHERE field1='abc'");
                    assertTrue(updateCount == 1);
                } finally {
                    if (charsetStmt != null) {
                        charsetStmt.executeUpdate(
                            "DROP TABLE IF EXISTS testCollation41");
                        charsetStmt.executeUpdate(
                            "DROP DATABASE IF EXISTS testCollation41");
                        charsetStmt.close();
                    }

                    if (charsetConn != null) {
                        charsetConn.close();
                    }
                }
            }
        }
    }

    /**
     * Tests setReadOnly() being reset during failover
     *
     * @throws Exception if an error occurs.
     */
    public void testSetReadOnly() throws Exception {
        Properties props = new Properties();
        props.put("autoReconnect", "true");

        String sepChar = "?";

        if (BaseTestCase.dbUrl.indexOf("?") != -1) {
            sepChar = "&";
        }

        Connection reconnectableConn = DriverManager.getConnection(BaseTestCase.dbUrl
                + sepChar + "autoReconnect=true", props);

        rs = reconnectableConn.createStatement().executeQuery("SELECT CONNECTION_ID()");
        rs.next();

        String connectionId = rs.getString(1);

        reconnectableConn.setReadOnly(true);

        boolean isReadOnly = reconnectableConn.isReadOnly();

        System.out.println("You have 30 seconds to kill connection id "
            + connectionId + "...");
        Thread.sleep(30000);
        System.out.println("Executing statement on reconnectable connection...");

        try {
            reconnectableConn.createStatement().executeQuery("SELECT 1");
        } catch (SQLException sqlEx) {
            ; // ignore
        }

        reconnectableConn.createStatement().executeQuery("SELECT 1");

        assertTrue(reconnectableConn.isReadOnly() == isReadOnly);
    }

    private Map getCharacterSetsAndCollations() throws Exception {
        Map charsetsToLoad = new HashMap();

        try {
            this.rs = this.stmt.executeQuery("SHOW character set");

            while (rs.next()) {
                charsetsToLoad.put(rs.getString("Charset"),
                    rs.getString("Default collation"));
            }

            //
            // These don't have mappings in Java...
            //
            charsetsToLoad.remove("swe7");
            charsetsToLoad.remove("hp8");
            charsetsToLoad.remove("dec8");
            charsetsToLoad.remove("koi8u");
            charsetsToLoad.remove("keybcs2");
            charsetsToLoad.remove("geostd8");
            charsetsToLoad.remove("armscii8");
        } finally {
            if (this.rs != null) {
                this.rs.close();
            }
        }

        return charsetsToLoad;
    }
    
    /**
     * Tests fix for BUG#3554 - Not specifying database in URL causes 
     * MalformedURL exception.
     * 
     * @throws Exception if an error ocurrs.
     */
    public void testBug3554() throws Exception {
    	try {
    		new NonRegisteringDriver().connect("jdbc:mysql://localhost:3306/?user=root&password=root", new Properties());
    	} catch (SQLException sqlEx) {
    		assertTrue(sqlEx.getMessage().indexOf("Malformed") == -1);
    	}
    }
    
    public void testBug3790() throws Exception {
    	String field2OldValue = "foo";
    	String field2NewValue = "bar";
    	int field1OldValue = 1;
    	
    	Connection conn1 = null;
    	Connection conn2 = null;
    	Statement stmt1 = null;
    	Statement stmt2 = null;
    	ResultSet rs2 = null;
    	
    	Properties props = new Properties();
    		
    	try {
    		this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug3790");
    		this.stmt.executeUpdate("CREATE TABLE testBug3790 (field1 INT NOT NULL PRIMARY KEY, field2 VARCHAR(32)) TYPE=InnoDB");
    		this.stmt.executeUpdate("INSERT INTO testBug3790 VALUES (" + field1OldValue + ", '" + field2OldValue + "')");
    		
    		conn1 = getConnectionWithProps(props); // creates a new connection
    		conn2 = getConnectionWithProps(props); // creates another new connection
    		conn1.setAutoCommit(false);
    		conn2.setAutoCommit(false);
    		
    		stmt1 = conn1.createStatement();
    		stmt1.executeUpdate("UPDATE testBug3790 SET field2 = '" + field2NewValue + "' WHERE field1=" + field1OldValue);
    		conn1.commit();
    		
    		stmt2 = conn2.createStatement();
    		
    		rs2 = stmt2.executeQuery("SELECT field1, field2 FROM testBug3790");
    		
    		assertTrue(rs2.next());
    		assertTrue(rs2.getInt(1) == field1OldValue);
    		assertTrue(rs2.getString(2).equals(field2NewValue));
    	} finally {
    		this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug3790");
    		
    		if (rs2 != null) {
    			rs2.close();
    		}
    		
    		if (stmt2 != null) {
    			stmt2.close();
    		}
    		
    		if (stmt1 != null) {
    			stmt1.close();
    		}
    		
    		if (conn1 != null) {
    			conn1.close();
    		}
    		
    		if (conn2 != null) {
    			conn2.close();
    		}
    	}
    }
}
