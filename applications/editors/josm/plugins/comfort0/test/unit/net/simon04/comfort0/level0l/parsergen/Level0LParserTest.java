package net.simon04.comfort0.level0l.parsergen;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.StringReader;

import org.junit.Test;
import org.openstreetmap.josm.data.osm.NodeData;

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
}