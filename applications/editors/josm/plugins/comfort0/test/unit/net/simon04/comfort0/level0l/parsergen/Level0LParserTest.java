package net.simon04.comfort0.level0l.parsergen;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.io.StringReader;

import org.junit.Test;
import org.openstreetmap.josm.data.osm.NodeData;

public class Level0LParserTest {
    @Test
    public void testNode() throws Exception {
        final NodeData node = new Level0LParser(new StringReader("node 298884272: 54.0901447, 12.2516513\n")).node();
        assertThat(node.getId(), is(298884272L));
        assertThat(node.getCoor().lat(), is(54.0901447));
        assertThat(node.getCoor().lon(), is(12.2516513));

    }
}