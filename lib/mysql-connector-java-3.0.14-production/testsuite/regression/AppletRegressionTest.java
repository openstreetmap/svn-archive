/*
   Copyright (C) 2003 MySQL AB

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

import sun.applet.AppletSecurity;

import testsuite.BaseTestCase;

import java.util.Properties;


/**
 * Tests various applet-related issues.
 *
 * @author Mark Matthews
 * @version $Id: AppletRegressionTest.java,v 1.1.2.3 2004/04/24 15:49:45 mmatthew Exp $
 */
public class AppletRegressionTest extends BaseTestCase {
    /**
     * DOCUMENT ME!
     *
     * @param name
     */
    public AppletRegressionTest(String name) {
        super(name);

        // TODO Auto-generated constructor stub
    }

    /**
     * Runs all test cases in this test suite
     *
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(AppletRegressionTest.class);
    }

    /**
     * Tests if the driver wors with an Applet security manager installed.
     *
     * @throws Exception if the test fails
     */
    public void testAppletSecurityManager() throws Exception {
        System.setSecurityManager(new CustomAppletSecurity());

        getConnectionWithProps(new Properties());
    }

    /**
     * We need to customize the security manager a 'bit', so that JUnit still
     * works (and we can connect to various databases).
     */
    class CustomAppletSecurity extends AppletSecurity {
        /* (non-Javadoc)
         * @see java.lang.SecurityManager#checkAccess(java.lang.Thread)
         */
        public synchronized void checkAccess(Thread arg0) {
        }

        /* (non-Javadoc)
         * @see java.lang.SecurityManager#checkConnect(java.lang.String, int, java.lang.Object)
         */
        public void checkConnect(String host, int port, Object context) {
        }

        /* (non-Javadoc)
         * @see java.lang.SecurityManager#checkConnect(java.lang.String, int)
         */
        public void checkConnect(String host, int port) {
        }
    }
}
