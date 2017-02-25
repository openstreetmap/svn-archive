// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.TestUtils;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.testutils.JOSMTestRules;

/**
 * Unit tests of {@link GmlReader} class.
 */
public class GmlReaderTest {

    /**
     * Setup test.
     */
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().preferences().projection().timeout(60000);

    /**
     * Non-regression test for ticket <a href="https://josm.openstreetmap.de/ticket/11624">#11624</a>
     * @throws IOException if an error occurs during reading
     */
    @Test
    public void testTicket11624() throws IOException, XMLStreamException, FactoryConfigurationError {
        File file = new File(TestUtils.getRegressionDataFile(11624, "temp3.gml"));
        try (InputStream is = new FileInputStream(file)) {
            for (Node n : GmlReader.parseDataSet(is, null, null).getNodes()) {
                assertNotNull(n.toString(), n.getCoor());
                assertTrue(n.get("KICH_URL"), n.get("KICH_URL").startsWith("http://"));
            }
        }
    }
}
