// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openstreetmap.josm.JOSMFixture;
import org.openstreetmap.josm.TestUtils;
import org.openstreetmap.josm.plugins.opendata.core.io.NonRegFunctionalTests;

/**
 * Unit tests of {@link KmlReader} class.
 */
public class KmlReaderTest {

    /**
     * Setup test.
     */
    @BeforeClass
    public static void setUp() {
        JOSMFixture.createUnitTestFixture().init();
    }
    
    /**
     * Non-regression test for ticket <a href="https://josm.openstreetmap.de/ticket/10214">#10214</a>
     * @throws IOException if an error occurs during reading
     */
    @Test
    public void testTicket10214() throws IOException, XMLStreamException, FactoryConfigurationError {
        File file = new File(TestUtils.getRegressionDataFile(10214, "utf8_test.kml"));
        try (InputStream is = new FileInputStream(file)) {
            NonRegFunctionalTests.testTicket10214(KmlReader.parseDataSet(is, null));
        }
    }
    
    /**
     * Non-regression test for ticket <a href="https://josm.openstreetmap.de/ticket/7714">#7714</a>
     * @throws IOException if an error occurs during reading
     */
    @Test
    public void testTicket7714() throws IOException, XMLStreamException, FactoryConfigurationError {
        File file = new File(TestUtils.getRegressionDataFile(7714, "doc.kml"));
        try (InputStream is = new FileInputStream(file)) {
            NonRegFunctionalTests.testGeneric("#7714", KmlReader.parseDataSet(is, null));
        }
    }
}
