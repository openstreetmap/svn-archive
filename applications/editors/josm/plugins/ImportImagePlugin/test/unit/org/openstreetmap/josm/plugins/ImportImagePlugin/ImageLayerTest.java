// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.ImportImagePlugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.TestUtils;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.io.OsmReader;
import org.openstreetmap.josm.testutils.JOSMTestRules;

/**
 * Test that some geotiff sample files can be read.
 * Data downloaded from <a href="ftp://ftp.remotesensing.org/pub/geotiff/samples">remotesensing.org</a>.
 */
public class ImageLayerTest {

    /**
     * Setup test.
     */
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().preferences().projection();

    /**
     * Non-regression test for ticket <a href="https://josm.openstreetmap.de/ticket/14894">#14894</a>
     * @throws Exception if an error occurs during reading
     */
    @Test
    public void testTicket14894() throws Exception {
        assertTrue(MainApplication.getLayerManager().getLayers().isEmpty());
        // Step 1: add .osm layer
        try (InputStream in = TestUtils.getRegressionDataStream(14894, "14894.osm")) {
            MainApplication.getLayerManager().addLayer(new OsmDataLayer(OsmReader.parseDataSet(in, null), "14894", null));
        }
        // Step 2: try to import image
        MainApplication.getLayerManager().addLayer(new ImageLayer(new File(TestUtils.getRegressionDataFile(14894, "14894.png"))));
        assertEquals(2, MainApplication.getLayerManager().getLayers().size());
    }
}
