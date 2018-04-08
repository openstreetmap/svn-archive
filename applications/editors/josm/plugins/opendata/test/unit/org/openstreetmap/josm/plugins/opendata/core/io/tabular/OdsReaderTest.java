// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.tabular;

import java.io.InputStream;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.TestUtils;
import org.openstreetmap.josm.plugins.opendata.core.io.NonRegFunctionalTests;
import org.openstreetmap.josm.testutils.JOSMTestRules;

/**
 * Unit tests of {@link OdsReader} class.
 */
public class OdsReaderTest {

    /**
     * Setup test.
     */
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().preferences().projection();

    /**
     * Non-regression test for ticket <a href="https://josm.openstreetmap.de/ticket/13821">#13821</a>
     * @throws Exception if an error occurs during reading
     */
    @Test
    public void testTicket13821() throws Exception {
        try (InputStream is = TestUtils.getRegressionDataStream(13821, "1_set_v_0.6_2016_06_21_06_00_23_a.ods")) {
            NonRegFunctionalTests.testGeneric("#13821", OdsReader.parseDataSet(is, null, null));
        }
    }
}
