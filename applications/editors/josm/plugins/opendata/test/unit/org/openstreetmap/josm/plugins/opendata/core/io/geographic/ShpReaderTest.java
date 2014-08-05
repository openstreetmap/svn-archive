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
 * Unit tests of {@link ShpReader} class.
 */
public class ShpReaderTest {

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
        File file = new File(TestUtils.getRegressionDataFile(10214, "utf8_test.shp"));
        try (InputStream is = new FileInputStream(file)) {
            NonRegFunctionalTests.testTicket10214(ShpReader.parseDataSet(is, file, null, null));
        }
    }
    
    /**
     * Non-regression test for ticket <a href="https://josm.openstreetmap.de/ticket/8309">#8309</a>
     * @throws IOException if an error occurs during reading
     */
    @Test
    public void testTicket8309() throws IOException, XMLStreamException, FactoryConfigurationError {
        File file = new File(TestUtils.getRegressionDataFile(8309, "new_ti_declarada.shp"));
        try (InputStream is = new FileInputStream(file)) {
            NonRegFunctionalTests.testGeneric(ShpReader.parseDataSet(is, file, null, null));
        }
    }
}
