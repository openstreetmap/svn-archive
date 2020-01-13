package net.simon04.comfort0.level0l.parsergen;

import static org.CustomMatchers.hasSize;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.StringReader;
import java.util.List;

import org.junit.Test;
import org.openstreetmap.josm.data.osm.NodeData;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.PrimitiveData;
import org.openstreetmap.josm.data.osm.RelationData;
import org.openstreetmap.josm.data.osm.WayData;

public class Level0LParserTest {
    @Test
    public void testNode() throws Exception {
        final String level0l = "" +
                "node 298884272: 54.0901447, 12.2516513\n" +
                "  name = Neu Broderstorf\n" +
                "  traffic_sign = city_limit\n";
        final NodeData node = new Level0LParser(new StringReader(level0l)).node();
        assertThat(node.getId(), is(298884272L));
        assertThat(node.getCoor().lat(), is(54.0901447));
        assertThat(node.getCoor().lon(), is(12.2516513));
        assertThat(node.getKeys().size(), is(2));
        assertThat(node.getKeys().get("name"), is("Neu Broderstorf"));
        assertThat(node.getKeys().get("traffic_sign"), is("city_limit"));
    }

    @Test
    public void testWay() throws Exception {
        final String level0l = "" +
                "way 26659127\n" +
                "  nd 292403538\n" +
                "  nd 298884289\n" +
                "  nd 261728686\n" +
                "  highway = unclassified\n" +
                "  name = Pastower Straße\n";
        final WayData way = new Level0LParser(new StringReader(level0l)).way();
        assertThat(way.getId(), is(26659127L));
        assertThat(way.getNodesCount(), is(3));
        assertThat(way.getNodeId(0), is(292403538L));
        assertThat(way.getNodeId(1), is(298884289L));
        assertThat(way.getNodeId(2), is(261728686L));
        assertThat(way.getKeys().size(), is(2));
        assertThat(way.getKeys().get("highway"), is("unclassified"));
        assertThat(way.getKeys().get("name"), is("Pastower Straße"));
    }

    @Test
    public void testRelation() throws Exception {
        final String level0l = "" +
                "relation 56688 # member types: nd, wy, rel; roles are put after ids\n" +
                "  nd 294942404\n" +
                "  nd 364933006 # the second node\n" +
                "  wy 4579143 forward\n" +
                "  nd 249673494 stop # the end\n" +
                "  name = Küstenbus Linie 123\n" +
                "  network = VVW\n" +
                "  operator = Regionalverkehr Küste\n" +
                "  ref = 123\n" +
                "  route = bus\n" +
                "  type = route\n";
        final RelationData relation = new Level0LParser(new StringReader(level0l)).relation();
        assertThat(relation.getId(), is(56688L));
        assertThat(relation.getMembersCount(), is(4));
        assertThat(relation.getMembers().get(0).getMemberId(), is(294942404L));
        assertThat(relation.getMembers().get(0).getMemberType(), is(OsmPrimitiveType.NODE));
        assertThat(relation.getMembers().get(0).getRole(), is(""));
        assertThat(relation.getMembers().get(2).getMemberId(), is(4579143L));
        assertThat(relation.getMembers().get(2).getMemberType(), is(OsmPrimitiveType.WAY));
        assertThat(relation.getMembers().get(2).getRole(), is("forward"));
        assertThat(relation.getKeys().size(), is(6));
    }

    @Test
    public void testExampleFromDocs() throws Exception {
        // https://wiki.openstreetmap.org/wiki/Level0L#Examples
        final String level0l = "" +
                "node 298884269: 54.0901746, 12.2482632 # made by user SvenHRO (46882) in changeset 676636 on 2008-09-21T21:37:45Z\n" +
                "node 261728686: 54.0906309, 12.2441924 # versions should be stored server-side\n" +
                "node 1831881213: 54.0900666, 12.2539381 # comments are not allowed in tag values, '=' is screened (\\=) only in keys\n" +
                "  name = Neu Broderstorf\n" +
                "  traffic_sign = city_limit\n" +
                "\n" +
                "node 298884272: 54.0901447, 12.2516513\n" +
                "\n" +
                "way 26659127\n" +
                "  nd 292403538\n" +
                "  nd 298884289\n" +
                "  nd 261728686\n" +
                "  highway = unclassified\n" +
                "  name = Pastower Straße\n" +
                "\n" +
                "relation 56688 # member types: nd, wy, rel; roles are put after ids\n" +
                "  nd 294942404\n" +
                "  nd 364933006\n" +
                "  wy 4579143 forward\n" +
                "  nd 249673494\n" +
                "  name = Küstenbus Linie 123\n" +
                "  network = VVW\n" +
                "  operator = Regionalverkehr Küste\n" +
                "  ref = 123\n" +
                "  route = bus\n" +
                "  type = route\n";
        final List<PrimitiveData> primitives = new Level0LParser(new StringReader(level0l)).primitives();
        assertThat(primitives, hasSize(6));
        assertThat(primitives.get(0).toString(), is("298884269 null V NODE LatLon[lat=54.0901746,lon=12.2482632]"));
        assertThat(primitives.get(1).toString(), is("261728686 null V NODE LatLon[lat=54.0906309,lon=12.2441924]"));
        assertThat(primitives.get(2).toString(), is("1831881213 [name, Neu Broderstorf, traffic_sign, city_limit] V NODE LatLon[lat=54.0900666,lon=12.2539381]"));
        assertThat(primitives.get(3).toString(), is("298884272 null V NODE LatLon[lat=54.0901447,lon=12.2516513]"));
        assertThat(primitives.get(4).toString(), is("26659127 [highway, unclassified, name, Pastower Straße] V WAY[292403538, 298884289, 261728686]"));
        assertThat(primitives.get(5).toString(), is("56688 [name, Küstenbus Linie 123, network, VVW, operator, Regionalverkehr Küste, ref, 123, route, bus, type, route] V REL [node 294942404, node 364933006, way 4579143, node 249673494]"));
    }

}
