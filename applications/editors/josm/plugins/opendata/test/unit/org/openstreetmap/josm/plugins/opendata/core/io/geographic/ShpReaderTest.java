// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.TestUtils;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.core.io.NonRegFunctionalTests;
import org.openstreetmap.josm.testutils.JOSMTestRules;

/**
 * Unit tests of {@link ShpReader} class.
 */
public class ShpReaderTest {

    /**
     * Setup test.
     */
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().preferences().projection().timeout(60000);

    /**
     * Non-regression test for ticket <a href="https://josm.openstreetmap.de/ticket/12714">#12714</a>
     * @throws Exception if an error occurs during reading
     */
    @Test
    public void testTicket12714() throws Exception {
        File file = new File(TestUtils.getRegressionDataFile(12714, "linhas.shp"));
        try (InputStream is = new FileInputStream(file)) {
            for (Node n : ShpReader.parseDataSet(is, file, null, null).getNodes()) {
                assertNotNull(n.toString(), n.getCoor());
            }
        }
    }

    /**
     * Non-regression test for ticket <a href="https://josm.openstreetmap.de/ticket/11761">#11761</a>
     * @throws Exception if an error occurs during reading
     */
    @Test
    @Ignore("work in progress")
    public void testTicket11761() throws Exception {
        File file = new File(TestUtils.getRegressionDataFile(11761, "HAR.shp"));
        try (InputStream is = new FileInputStream(file)) {
            for (Node n : ShpReader.parseDataSet(is, file, null, null).getNodes()) {
                assertNotNull(n.toString(), n.getCoor());
                assertFalse(n.toString(), LatLon.ZERO.equals(n.getCoor()));
                assertFalse(n.toString(), n.getCoor().isOutSideWorld());
                assertTrue(n.toString(), n.getCoor().isValid());
            }
        }
    }

    /**
     * Non-regression test for ticket <a href="https://josm.openstreetmap.de/ticket/10214">#10214</a>
     * @throws Exception if an error occurs during reading
     */
    @Test
    public void testTicket10214() throws Exception {
        File file = new File(TestUtils.getRegressionDataFile(10214, "utf8_test.shp"));
        try (InputStream is = new FileInputStream(file)) {
            NonRegFunctionalTests.testTicket10214(ShpReader.parseDataSet(is, file, null, null));
        }
    }

    /**
     * Non-regression test for ticket <a href="https://josm.openstreetmap.de/ticket/8309">#8309</a>
     * @throws Exception if an error occurs during reading
     */
    @Test
    public void testTicket8309() throws Exception {
        File file = new File(TestUtils.getRegressionDataFile(8309, "new_ti_declarada.shp"));
        try (InputStream is = new FileInputStream(file)) {
            NonRegFunctionalTests.testGeneric("#8309", ShpReader.parseDataSet(is, file, null, null));
        }
    }
}
