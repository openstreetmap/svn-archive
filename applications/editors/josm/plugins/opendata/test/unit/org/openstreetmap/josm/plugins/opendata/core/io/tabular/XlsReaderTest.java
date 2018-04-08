// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.tabular;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.TestUtils;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.projection.Projections;
import org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.core.io.NonRegFunctionalTests;
import org.openstreetmap.josm.testutils.JOSMTestRules;

/**
 * Unit tests of {@link XlsReader} class.
 */
public class XlsReaderTest {

    /**
     * Setup test.
     */
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().preferences().projection();

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
        handler.setSpreadSheetHandler(new DefaultSpreadSheetHandler() {
            @Override
            public boolean handlesProjection() {
                return true;
            }

            @Override
            public LatLon getCoor(EastNorth en, String[] fields) {
                return Projections.getProjectionByCode(epsgCode).eastNorth2latlon(en);
            }
        });
        return handler;
    }

    /**
     * Non-regression test for ticket <a href="https://josm.openstreetmap.de/ticket/15980">#15980</a>
     * @throws Exception if an error occurs during reading
     */
    @Test
    public void testTicket15980() throws Exception {
        try (InputStream is = TestUtils.getRegressionDataStream(15980, "qry_OSM_Import_Orte.xls")) {
            DataSet ds = XlsReader.parseDataSet(is, newHandler("EPSG:4326"), null);
            NonRegFunctionalTests.testGeneric("#15980", ds);
            doTest15980(ds, "Straße 19", "19", null);
            doTest15980(ds, "Straße 20", "20", null);
            doTest15980(ds, "Straße", null, "highway");
        }
    }

    private static void doTest15980(DataSet ds, String name, String addr, String fixme) {
        OsmPrimitive osm = ds.getPrimitives(o -> name.equals(o.get("name"))).iterator().next();
        assertNotNull(name, osm);
        assertEquals(addr, osm.get("addr:housenumber"));
        assertEquals(fixme, osm.get("fixme"));
    }
}
