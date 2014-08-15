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
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.projection.Projections;
import org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler;
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
    
    private static AbstractDataSetHandler newHandler(final String epsgCode) {
        AbstractDataSetHandler handler = new AbstractDataSetHandler() {
            @Override
            public boolean acceptsFilename(String filename) {
                return true;
            }
            @Override
            public void updateDataSet(DataSet ds) {
            }
        };
        DefaultMifHandler mifHandler = new DefaultMifHandler();
        mifHandler.setCoordSysNonEarthProjection(Projections.getProjectionByCode(epsgCode));
        handler.setMifHandler(mifHandler);
        return handler;
    }
    
    /**
     * Non-regression test for ticket <a href="https://josm.openstreetmap.de/ticket/9592">#9592</a>
     * @throws IOException if an error occurs during reading
     */
    @Test
    public void testTicket9592() throws IOException {
        File file = new File(TestUtils.getRegressionDataFile(9592, "bg.mif"));
        try (InputStream is = new FileInputStream(file)) {
            NonRegFunctionalTests.testGeneric("#9592", MifReader.parseDataSet(is, file, newHandler("EPSG:32635"), null));
        }
    }
}
