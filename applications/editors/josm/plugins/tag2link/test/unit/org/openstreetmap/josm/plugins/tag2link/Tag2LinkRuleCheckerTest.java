package org.openstreetmap.josm.plugins.tag2link;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.plugins.tag2link.data.Link;

public class Tag2LinkRuleCheckerTest {

    @BeforeClass
    public static void load() {
        Tag2LinkRuleChecker.init();
    }

    @Test
    public void testImageCommons() {
        final Collection<Link> links = Tag2LinkRuleChecker.getLinks(new Tag("image", "File:Witten Brücke Gasstraße.jpg"));
        assertEquals(1, links.size());
        assertEquals("https://commons.wikimedia.org/wiki/File%3AWitten_Br%C3%BCcke_Gasstra%C3%9Fe.jpg", links.iterator().next().url);
    }

    @Test
    public void testBrandWikidata() {
        final Collection<Link> links = Tag2LinkRuleChecker.getLinks(new Tag("brand:wikidata", "Q259340"));
        assertEquals(1, links.size());
        assertEquals("https://www.wikidata.org/wiki/Q259340", links.iterator().next().url);
    }
}
