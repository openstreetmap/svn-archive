// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.tools.CheckParameterUtil;

public abstract class NonRegFunctionalTests {
    
    /**
     * Non-regression generic test.
     */
    public static void testGeneric(DataSet ds) {
        CheckParameterUtil.ensureParameterNotNull(ds, "ds");
        Collection<OsmPrimitive> prims = ds.allPrimitives();
        assertFalse(prims.isEmpty());
    }
    
    /**
     * Non-regression test for ticket <a href="https://josm.openstreetmap.de/ticket/10214">#10214</a>
     */
    public static void testTicket10214(DataSet ds) {
        testGeneric(ds);
        boolean found = false;
        for (Node n : ds.getNodes()) {
            if (n.hasTag("id", "1")) {
                found = true;
                String expected = "à as italian località";
                String actual = n.get("some_text");
                assertEquals(expected, actual);
            }
        }
        assertTrue(found);
    }
}
