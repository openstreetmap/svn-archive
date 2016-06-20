// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.TestUtils;
import org.openstreetmap.josm.plugins.opendata.core.io.NonRegFunctionalTests;
import org.openstreetmap.josm.testutils.JOSMTestRules;

/**
 * Unit tests of {@link KmlReader} class.
 */
public class KmlReaderTest {

    /**
     * Setup test.
     */
    @Rule
    public JOSMTestRules rules = new JOSMTestRules();

    /**
     * Unit test of {@link KmlReader#COLOR_PATTERN}
     */
    @Test
    public void testColorPattern() {
        assertTrue(KmlReader.COLOR_PATTERN.matcher("00112233").matches());
        assertTrue(KmlReader.COLOR_PATTERN.matcher("44556677").matches());
        assertTrue(KmlReader.COLOR_PATTERN.matcher("8899aabb").matches());
        assertTrue(KmlReader.COLOR_PATTERN.matcher("CCDDEEFF").matches());
        assertFalse(KmlReader.COLOR_PATTERN.matcher("0011223").matches());
        assertFalse(KmlReader.COLOR_PATTERN.matcher("001122330").matches());
        assertFalse(KmlReader.COLOR_PATTERN.matcher("gg112233").matches());
        assertFalse(KmlReader.COLOR_PATTERN.matcher("red").matches());
        assertFalse(KmlReader.COLOR_PATTERN.matcher("yellow").matches());
    }
    
    /**
     * Non-regression test for ticket <a href="https://josm.openstreetmap.de/ticket/12694">#12694</a>
     * @throws IOException if an error occurs during reading
     */
    @Test
    public void testTicket12694() throws IOException, XMLStreamException, FactoryConfigurationError {
        try (InputStream is = TestUtils.getRegressionDataStream(12694, "Alvinรณpolis_314946.kml")) {
            NonRegFunctionalTests.testGeneric("#12694", KmlReader.parseDataSet(is, null));
        }
    }

    /**
     * Non-regression test for ticket <a href="https://josm.openstreetmap.de/ticket/10214">#10214</a>
     * @throws IOException if an error occurs during reading
     */
    @Test
    public void testTicket10214() throws IOException, XMLStreamException, FactoryConfigurationError {
        try (InputStream is = TestUtils.getRegressionDataStream(10214, "utf8_test.kml")) {
            NonRegFunctionalTests.testTicket10214(KmlReader.parseDataSet(is, null));
        }
    }

    /**
     * Non-regression test for ticket <a href="https://josm.openstreetmap.de/ticket/7714">#7714</a>
     * @throws IOException if an error occurs during reading
     */
    @Test
    public void testTicket7714() throws IOException, XMLStreamException, FactoryConfigurationError {
        try (InputStream is = TestUtils.getRegressionDataStream(7714, "doc.kml")) {
            NonRegFunctionalTests.testGeneric("#7714", KmlReader.parseDataSet(is, null));
        }
    }
}
