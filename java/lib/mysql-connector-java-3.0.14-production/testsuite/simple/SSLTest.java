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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;


/**
 * Tests SSL functionality in the driver.
 *
 * @author Mark Matthews
 */
public class SSLTest extends BaseTestCase {
    /**
     * Constructor for SSLTest.
     *
     * @param name the name of the test to run.
     */
    public SSLTest(String name) {
        super(name);

        System.setProperty("javax.net.debug", "all");

        StringBuffer sslUrl = new StringBuffer(dbUrl);

        if (dbUrl.indexOf("?") == -1) {
            sslUrl.append("?");
        } else {
            sslUrl.append("&");
        }

        sslUrl.append("useSSL=true");
        sslUrl.append("&requireSSL=true");
    }

    /**
     * Runs all test cases in this test suite
     *
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(SSLTest.class);
    }

    /**
     * Tests SSL Connection
     *
     * @throws Exception if an error occurs
     */
    public void testConnect() throws Exception {
        String hasOpenSSL = getMysqlVariable("have_openssl");

        if ("YES".equalsIgnoreCase(hasOpenSSL)) {
            PrintStream oldOut = System.out;
            PrintStream oldErr = System.err;

            try {
                PrintStream newOut = new PrintStream(new ByteArrayOutputStream());
                PrintStream newErr = new PrintStream(new ByteArrayOutputStream());

                System.setOut(newOut);
                System.setErr(oldErr);

                System.out.println(
                    "<<<<<<<<<<< Look for SSL debug output >>>>>>>>>>>");
            } finally {
                System.setOut(oldOut);
                System.setErr(oldErr);
            }
        } else {
            System.out.println(
                "Not running SSL test as MySQL server isn't compiled with SSL functionality");
        }
    }
}
