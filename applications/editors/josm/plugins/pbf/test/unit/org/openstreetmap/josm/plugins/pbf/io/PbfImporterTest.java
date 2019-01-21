// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pbf.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.TestUtils;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.User;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.testutils.JOSMTestRules;

/**
 * Unit tests for {@link PbfImporter}.
 */
public class PbfImporterTest {

    /**
     * Setup test.
     */
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().preferences();

    private static void checkUserNull(OsmPrimitive osm, boolean hasToBeNull) {
        User usr = osm.getUser();
        if (hasToBeNull) {
            assertNull(osm + " -> " + usr, usr);
        } else {
            assertNotNull(osm + " -> " + usr, usr);
        }
    }

    private static void doTestMonaco(String file, boolean hasMetadataToBeNull) throws IOException, IllegalDataException {
        DataSet ds = new PbfImporter().parseDataSet(file);
        assertNotNull(ds);
        assertEquals(18685, ds.getNodes().size());
        assertEquals(16735, ds.getWays().size());
        assertEquals(476, ds.getRelations().size());

        checkUserNull(ds.getPrimitiveById(1790048269, OsmPrimitiveType.NODE), hasMetadataToBeNull);
        checkUserNull(ds.getPrimitiveById(4227155, OsmPrimitiveType.WAY), hasMetadataToBeNull);
        checkUserNull(ds.getPrimitiveById(393226, OsmPrimitiveType.RELATION), hasMetadataToBeNull);
    }

    /**
     * Unit test of {@link PbfImporter#parseDataSet(String)}.
     * @throws Exception if an error occurs
     */
    @Test
    public void testParseDataSet() throws Exception {
        doTestMonaco(TestUtils.getTestDataRoot() + "/monaco-latest.osm.pbf", false);
    }

    /**
     * Non-regression test for <a href="https://josm.openstreetmap.de/ticket/10132">Ticket #10132</a>.
     * @throws Exception if an error occurs
     */
    @Test
    public void testTicket10132() throws Exception {
        doTestMonaco(TestUtils.getRegressionDataFile(10132, "Monaco-SP.osm.pbf"), true);
    }

    /**
     * Non-regression test for <a href="https://josm.openstreetmap.de/ticket/12567">Ticket #12567</a>.
     * @throws Exception if an error occurs
     */
    @Test
    public void testTicket12567() throws Exception {
        DataSet ds = new PbfImporter().parseDataSet(TestUtils.getRegressionDataFile(12567, "12390008.osm.pbf"));
        assertNotNull(ds);
        assertEquals(103210, ds.getNodes().size());
        assertEquals(8727, ds.getWays().size());
        assertEquals(97, ds.getRelations().size());
    }

    /**
     * Non-regression test for <a href="https://josm.openstreetmap.de/ticket/14545">Ticket #14545</a>.
     * @throws Exception if an error occurs
     */
    @Test
    public void testTicket14545() throws Exception {
        DataSet ds = new PbfImporter().parseDataSet(TestUtils.getRegressionDataFile(14545, "reg14545.osm.pbf"));
        assertNotNull(ds);
        assertEquals(12, ds.getNodes().size());
        assertEquals(2, ds.getWays().size());
        assertEquals(1, ds.getRelations().size());
    }

}
