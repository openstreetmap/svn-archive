package org.openstreetmap.josm.plugins.imageryxmlbounds.actions;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.testutils.JOSMTestRules;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Unit tests of {@link ComputeBoundsAction}
 */
public class ComputeBoundsActionTest {

    /**
     * Setup rule
     */
    @Rule
    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    public JOSMTestRules test = new JOSMTestRules();

    /**
     * Unit test of {@link ComputeBoundsAction#getBounds}
     */
    @Test
    public void testGetBounds() {
        assertEquals("        <bounds min-lat='0' min-lon='0' max-lat='0' max-lon='0'>\n",
                ComputeBoundsAction.getBounds(new Node(LatLon.ZERO), false));
        assertEquals("        <bounds min-lat='0' min-lon='0' max-lat='0' max-lon='0' />",
                ComputeBoundsAction.getBounds(new Node(LatLon.ZERO), true));
        Way w = new Way();
        w.setNodes(Arrays.asList(
                new Node(LatLon.ZERO),
                new Node(new LatLon(0.123456789, 0.987654321)),
                new Node(new LatLon(1.987654321, 0.123456789)),
                new Node(LatLon.ZERO)));
        assertEquals("        <bounds min-lat='0' min-lon='0' max-lat='1.98766' max-lon='0.98766' />",
                ComputeBoundsAction.getBounds(w, true));
    }
}
