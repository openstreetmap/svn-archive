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

import testsuite.BaseTestCase;

import java.sql.Blob;
import java.sql.PreparedStatement;


/**
 * Tests for blob-related regressions.
 *
 * @author Mark Matthews
 * @version $Id: BlobRegressionTest.java,v 1.1.4.3 2004/04/24 15:49:45 mmatthew Exp $
 */
public class BlobRegressionTest extends BaseTestCase {
    /**
     * Creates a new BlobRegressionTest.
     *
     * @param name name of the test to run
     */
    public BlobRegressionTest(String name) {
        super(name);
    }

    /**
     * Runs all test cases in this test suite
     *
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(BlobRegressionTest.class);
    }

    /**
     *
     *
     * @throws Exception ...
     */
    public void testBug2670() throws Exception {
        try {
            byte[] blobData = new byte[32];

            for (int i = 0; i < blobData.length; i++) {
                blobData[i] = 1;
            }

            this.stmt.executeUpdate("DROP TABLE IF EXISTS testBug2670");
            this.stmt.executeUpdate(
                "CREATE TABLE testBug2670(blobField LONGBLOB)");

            PreparedStatement pStmt = this.conn.prepareStatement(
                    "INSERT INTO testBug2670 (blobField) VALUES (?)");
            pStmt.setBytes(1, blobData);
            pStmt.executeUpdate();

            this.rs = this.stmt.executeQuery(
                    "SELECT blobField FROM testBug2670");
            this.rs.next();

            Blob blob = this.rs.getBlob(1);

            //
            // Test mid-point insertion
            //
            blob.setBytes(4, new byte[] { 2, 2, 2, 2 });

            byte[] newBlobData = blob.getBytes(1L, (int) blob.length());

            assertTrue("Blob changed length", blob.length() == blobData.length);

            assertTrue("New data inserted wrongly",
                ((newBlobData[3] == 2) && (newBlobData[4] == 2)
                && (newBlobData[5] == 2) && (newBlobData[6] == 2)));

            //
            // Test end-point insertion
            //
            blob.setBytes(32, new byte[] { 2, 2, 2, 2 });

            assertTrue("Blob length should be 3 larger",
                blob.length() == (blobData.length + 3));
        } finally {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testUpdateLongBlob");
        }
    }

    /**
     *
     *
     * @throws Exception ...
     */
    public void testUpdateLongBlobGT16M() throws Exception {
        try {
            byte[] blobData = new byte[18 * 1024 * 1024]; // 18M blob

            this.stmt.executeUpdate("DROP TABLE IF EXISTS testUpdateLongBlob");
            this.stmt.executeUpdate(
                "CREATE TABLE testUpdateLongBlob(blobField LONGBLOB)");
            this.stmt.executeUpdate(
                "INSERT INTO testUpdateLongBlob (blobField) VALUES (NULL)");

            PreparedStatement pStmt = this.conn.prepareStatement(
                    "UPDATE testUpdateLongBlob SET blobField=?");
            pStmt.setBytes(1, blobData);
            pStmt.executeUpdate();
        } finally {
            this.stmt.executeUpdate("DROP TABLE IF EXISTS testUpdateLongBlob");
        }
    }
}
