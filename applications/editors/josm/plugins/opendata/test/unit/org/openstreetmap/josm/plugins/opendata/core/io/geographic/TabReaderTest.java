// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.TestUtils;
import org.openstreetmap.josm.plugins.opendata.core.io.NonRegFunctionalTests;
import org.openstreetmap.josm.testutils.JOSMTestRules;

/**
 * Unit tests of {@link TabReader} class.
 */
public class TabReaderTest {

    /**
     * Setup test.
     */
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().preferences().projection().timeout(60000);

    /**
     * Non-regression test for ticket <a href="https://josm.openstreetmap.de/ticket/15159">#15159</a>
     * @throws Exception if an error occurs during reading
     */
    @Test
    public void testTicket15159() throws Exception {
        File file = new File(TestUtils.getRegressionDataFile(15159, "Sanisette.tab"));
        try (InputStream is = new FileInputStream(file)) {
            NonRegFunctionalTests.testGeneric("#15159", TabReader.parseDataSet(is, file, null, null));
        }
    }
}
