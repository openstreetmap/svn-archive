package net.simon04.comfort0;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.InputStream;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.io.OsmReader;
import org.openstreetmap.josm.testutils.JOSMTestRules;

public class OsmToLevel0LTest {

    /**
     * Setup rule
     */
    @Rule
    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    public JOSMTestRules test = new JOSMTestRules().preferences();

    @Before
    public void setUp() throws Exception {
        Preferences.main().putBoolean("osm-primitives.showcoor", true);
    }

    @Test
    public void testNode() throws Exception {
        final Node node = new Node(1234L, 42);
        node.setCoor(new LatLon(123.45, 67.89));
        node.put("name", "Comfort0");
        final OsmToLevel0L converter = new OsmToLevel0L();
        converter.visit(node);
        assertThat(converter.toString(), is(""
                + "node 1234: 123.45, 67.89 #Comfort0 \u200E(123.45, 67.89)\n"
                + "  name = Comfort0\n"));
    }

    @Test
    public void testLargerExample() throws Exception {
        // from https://wiki.openstreetmap.org/wiki/OSM_XML#OSM_XML_file_format
        // compare to https://wiki.openstreetmap.org/wiki/Level0L#Examples
        try (InputStream in = getClass().getClassLoader().getResource("example.osm").openStream()) {
            final DataSet dataSet = OsmReader.parseDataSet(in, null);
            final OsmToLevel0L converter = new OsmToLevel0L();
            converter.visit(dataSet.allPrimitives());
            assertThat(converter.toString(), is("" +
                    "way 4579143 #\u200Eincomplete\n" +
                    "\n" +
                    "way 26659127 #\u200EPastower Straße\u200E (3 nodes)\n" +
                    "  highway = unclassified\n" +
                    "  name = Pastower Straße\n" +
                    "  nd 292403538 #incomplete\n" +
                    "  nd 298884289 #incomplete\n" +
                    "  nd 261728686 #261728686 \u200E(54.0906309, 12.2441924)\n" +
                    "\n" +
                    "relation 56688 #route (\"Küstenbus Linie 123\", 4 members, incomplete)\n" +
                    "  name = Küstenbus Linie 123\n" +
                    "  network = VVW\n" +
                    "  operator = Regionalverkehr Küste\n" +
                    "  ref = 123\n" +
                    "  route = bus\n" +
                    "  type = route\n" +
                    "  nd 294942404  #incomplete\n" +
                    "  nd 364933006  #incomplete\n" +
                    "  wy 4579143  #\u200Eincomplete\n" +
                    "  nd 249673494  #incomplete\n" +
                    "\n" +
                    "node 249673494 #incomplete\n" +
                    "\n" +
                    "node 261728686: 54.0906309, 12.2441924 #261728686 \u200E(54.0906309, 12.2441924)\n" +
                    "\n" +
                    "node 292403538 #incomplete\n" +
                    "\n" +
                    "node 294942404 #incomplete\n" +
                    "\n" +
                    "node 298884269: 54.0901746, 12.2482632 #298884269 \u200E(54.0901746, 12.2482632)\n" +
                    "\n" +
                    "node 298884272: 54.0901447, 12.2516513 #298884272 \u200E(54.0901447, 12.2516513)\n" +
                    "\n" +
                    "node 298884289 #incomplete\n" +
                    "\n" +
                    "node 364933006 #incomplete\n" +
                    "\n" +
                    "node 1831881213: 54.0900666, 12.2539381 #Neu Broderstorf \u200E(54.0900666, 12.2539381)\n" +
                    "  name = Neu Broderstorf\n" +
                    "  traffic_sign = city_limit\n"));
        }
    }
}
