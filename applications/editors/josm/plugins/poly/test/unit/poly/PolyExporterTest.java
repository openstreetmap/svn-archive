// License: GPL. For details, see LICENSE file.
package poly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.TestUtils;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.testutils.JOSMTestRules;

/**
 * Unit tests for {@link PolyExporter}.
 * @author Gerd Petermann
 */
public class PolyExporterTest {

    /**
     * Setup test.
     */
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().preferences().timeout(20000);

    /**
     * Import file, export it, import the exported file and compare content
     * @throws Exception if an error occurs
     */
    @Test
    public void testExport() throws Exception {
        DataSet dsIn1 = new PolyImporter().parseDataSet(TestUtils.getTestDataRoot() + "/holes.poly");
        assertNotNull(dsIn1);
        assertEquals(76, dsIn1.getNodes().size());
        assertEquals(4, dsIn1.getWays().size());
        assertEquals(1, dsIn1.getRelations().size());

        Path out = Files.createTempFile("holes-out", "poly");
        new PolyExporter().exportData(out.toFile(), new OsmDataLayer(dsIn1, null, null));
        DataSet dsIn2 = new PolyImporter().parseDataSet(out.toString());
        assertNotNull(dsIn2);
        assertEquals(76, dsIn2.getNodes().size());
        assertEquals(4, dsIn2.getWays().size());
        assertEquals(1, dsIn2.getRelations().size());
        
        Files.delete(out);
    }
}
