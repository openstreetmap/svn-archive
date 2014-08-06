// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openstreetmap.josm.JOSMFixture;
import org.openstreetmap.josm.TestUtils;
import org.openstreetmap.josm.plugins.opendata.core.io.NonRegFunctionalTests;

/**
 * Unit tests of {@link MifReader} class.
 */
public class MifReaderTest {

    /**
     * Setup test.
     */
    @BeforeClass
    public static void setUp() {
        JOSMFixture.createUnitTestFixture().init();
    }
    
    /**
     * Non-regression test for ticket <a href="https://josm.openstreetmap.de/ticket/9592">#9592</a>
     * @throws IOException if an error occurs during reading
     */
    @Test
    public void testTicket9592() throws IOException {
        File file = new File(TestUtils.getRegressionDataFile(9592, "bg.mif"));
        try (InputStream is = new FileInputStream(file)) {
            NonRegFunctionalTests.testGeneric(MifReader.parseDataSet(is, file, null, null));
        }
    }
}
