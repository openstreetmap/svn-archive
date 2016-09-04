// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pbf.io;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.TestUtils;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.io.Compression;
import org.openstreetmap.josm.io.OsmReader;
import org.openstreetmap.josm.testutils.JOSMTestRules;

/**
 * Unit tests for {@link PbfExporter}.
 */
public class PbfExporterTest {

    /**
     * Setup test.
     */
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().preferences().timeout(20000);

    /**
     * Non-regression test for <a href="https://josm.openstreetmap.de/ticket/11169">Ticket #11169</a>.
     * @throws Exception if an error occurs
     */
    @Test
    public void testTicket11169() throws Exception {
        try (InputStream is = Compression.ZIP.getUncompressedInputStream(
                new FileInputStream(TestUtils.getRegressionDataFile(11169, "Portsmouth_Area.osm.zip")))) {
            DataSet ds = OsmReader.parseDataSet(is, null);
            Path out = Files.createTempFile("pbf-bug-11169", "pbf");
            new PbfExporter().doSave(out.toFile(), new OsmDataLayer(ds, null, null));
            Files.delete(out);
        }
    }
}
