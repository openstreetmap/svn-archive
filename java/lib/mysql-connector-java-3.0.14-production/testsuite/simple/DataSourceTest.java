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

import testsuite.BaseTestCase;

import java.io.File;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;


/**
 * DOCUMENT ME!
 *
 * @author Mark Matthews
 * @version $Id: DataSourceTest.java,v 1.10.2.3 2004/05/27 17:49:45 mmatthew Exp $
 */
public class DataSourceTest extends BaseTestCase {
    private Context ctx;
    private File tempDir;

    /**
     * Creates a new DataSourceTest object.
     *
     * @param name DOCUMENT ME!
     */
    public DataSourceTest(String name) {
        super(name);
    }

    /**
     * Runs all test cases in this test suite
     *
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(DataSourceTest.class);
    }

    /**
     * Sets up this test, calling registerDataSource() to bind a  DataSource
     * into JNDI, using the FSContext JNDI provider from Sun
     *
     * @throws Exception if an error occurs.
     */
    public void setUp() throws Exception {
        super.setUp();
        registerDataSource();
    }

    /**
     * Un-binds the DataSource, and cleans up the filesystem
     *
     * @throws Exception if an error occurs
     */
    public void tearDown() throws Exception {
        ctx.unbind(tempDir.getAbsolutePath() + "/test");
        ctx.close();
        tempDir.delete();
        super.tearDown();
    }

    /**
     * Tests that we can get a connection from the DataSource bound in JNDI
     * during test setup
     *
     * @throws Exception if an error occurs
     */
    public void testDataSource() throws Exception {
        NameParser nameParser = ctx.getNameParser("");
        Name datasourceName = nameParser.parse(tempDir.getAbsolutePath()
                + "/test");
        Object obj = ctx.lookup(datasourceName);
        ConnectionPoolDataSource boundDs = null;

        if (obj instanceof DataSource) {
            boundDs = (ConnectionPoolDataSource) obj;
        } else if (obj instanceof Reference) {
            //
            // For some reason, this comes back as a Reference
            // instance under CruiseControl !?
            //
            Reference objAsRef = (Reference) obj;
            ObjectFactory factory = (ObjectFactory) Class.forName(objAsRef
                    .getFactoryClassName()).newInstance();
            boundDs = (ConnectionPoolDataSource) factory.getObjectInstance(objAsRef,
                    datasourceName, ctx, new Hashtable());
        }

        assertTrue("Datasource not bound", boundDs != null);

        Connection con = boundDs.getPooledConnection().getConnection();
        con.close();
        assertTrue("Connection can not be obtained from data source",
            con != null);
    }
    
    /**
     * This method is separated from the rest of the example since you normally
     * would NOT register a JDBC driver in your code.  It would likely be
     * configered into your naming and directory service using some GUI.
     *
     * @throws Exception if an error occurs
     */
    private void registerDataSource() throws Exception {
        tempDir = File.createTempFile("jnditest", null);
        tempDir.delete();
        tempDir.mkdir();
        tempDir.deleteOnExit();

        MysqlConnectionPoolDataSource ds;
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
            "com.sun.jndi.fscontext.RefFSContextFactory");
        ctx = new InitialContext(env);
        assertTrue("Naming Context not created", ctx != null);
        ds = new MysqlConnectionPoolDataSource();
        ds.setUrl(dbUrl); // from BaseTestCase
        ds.setDatabaseName("test");
        ctx.bind(tempDir.getAbsolutePath() + "/test", ds);
        
        MysqlConnectionPoolDataSource noUrlDs = new MysqlConnectionPoolDataSource();
        noUrlDs.setDatabaseName("test");
        noUrlDs.setServerName("localhost");
        ctx.bind(tempDir.getAbsolutePath() + "/testNoUrl", noUrlDs);
    }
}
