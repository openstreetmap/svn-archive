// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.o5m.io;

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
 * Unit tests for {@link O5mImporter}.
 */
public class O5mImporterTest {

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
        DataSet ds = new O5mImporter().parseDataSet(file);
        assertNotNull(ds);
        assertEquals(18685, ds.getNodes().size());
        assertEquals(16735, ds.getWays().size());
        assertEquals(476, ds.getRelations().size());

        checkUserNull(ds.getPrimitiveById(1790048269, OsmPrimitiveType.NODE), hasMetadataToBeNull);
        checkUserNull(ds.getPrimitiveById(4227155, OsmPrimitiveType.WAY), hasMetadataToBeNull);
        checkUserNull(ds.getPrimitiveById(393226, OsmPrimitiveType.RELATION), hasMetadataToBeNull);
    }

    /**
     * Unit test of {@link O5mImporter#parseDataSet(String)}.
     * File should contain same data as corresponding file monaco-latest.osm.pbf.
     * @throws Exception if an error occurs
     */
    @Test
    public void testParseDataSet() throws Exception {
        doTestMonaco(TestUtils.getTestDataRoot() + "/monaco-latest.o5m", false);
    }

    /**
     * Unit test of {@link O5mImporter#parseDataSet(String)} with stripped file.
     * File was created with osmconvert monaco-latest --drop-version -o=monaco-drop-version.o5m
     * @throws Exception if an error occurs
     */
    @Test
    public void testParseDataSetDropVersion() throws Exception {
        doTestMonaco(TestUtils.getTestDataRoot() + "/monaco-drop-version.o5m", true);
    }
}
