package org.openstreetmap.josm.plugins.graphview.core.access;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.openstreetmap.josm.plugins.graphview.core.data.MapBasedTagGroup;
import org.openstreetmap.josm.plugins.graphview.core.data.Tag;
import org.openstreetmap.josm.plugins.graphview.core.data.TagGroup;


public class AccessRulesetReaderTest {

	@Test
	public void testReadAccessRuleset_valid_classes() throws IOException {

		InputStream is = new FileInputStream("plugins/graphview/test/files/accessRuleset_valid.xml");
		AccessRuleset ruleset = AccessRulesetReader.readAccessRuleset(is);
		assertNotNull(ruleset);


		assertEquals("vehicle", ruleset.getAccessHierarchyAncestors("vehicle").get(0));

		assertEquals("motor_vehicle", ruleset.getAccessHierarchyAncestors("motor_vehicle").get(0));
		assertEquals("vehicle", ruleset.getAccessHierarchyAncestors("motor_vehicle").get(1));

		assertEquals("bus", ruleset.getAccessHierarchyAncestors("bus").get(0));
		assertEquals("motor_vehicle", ruleset.getAccessHierarchyAncestors("bus").get(1));
		assertEquals("vehicle", ruleset.getAccessHierarchyAncestors("bus").get(2));

		assertEquals("bicycle", ruleset.getAccessHierarchyAncestors("bicycle").get(0));
		assertEquals("vehicle", ruleset.getAccessHierarchyAncestors("bicycle").get(1));

		assertFalse(ruleset.getAccessHierarchyAncestors("bus").contains("bicycle"));

		assertSame(ruleset.getAccessHierarchyAncestors("boat").size(), 0);

	}

	@Test
	public void testReadAccessRuleset_valid_basetags() throws IOException {

		InputStream is = new FileInputStream("plugins/graphview/test/files/accessRuleset_valid.xml");
		AccessRuleset ruleset = AccessRulesetReader.readAccessRuleset(is);
		assertNotNull(ruleset);

		assertSame(2, ruleset.getBaseTags().size());

		assertTrue(ruleset.getBaseTags().contains(new Tag("highway", "residential")));
		assertTrue(ruleset.getBaseTags().contains(new Tag("highway", "cycleway")));
		assertFalse(ruleset.getBaseTags().contains(new Tag("building", "residential")));
		assertFalse(ruleset.getBaseTags().contains(new Tag("highway", "stop")));

	}

	@Test
	public void testReadAccessRuleset_valid_implications() throws IOException {

		InputStream is = new FileInputStream("plugins/graphview/test/files/accessRuleset_valid.xml");
		AccessRuleset ruleset = AccessRulesetReader.readAccessRuleset(is);
		assertNotNull(ruleset);

		List<Implication> implications = ruleset.getImplications();

		assertSame(3, implications.size());

		TagGroup[] tagGroups = new TagGroup[4];
		tagGroups[0] = createTagGroup(new Tag("highway", "cycleway"));
		tagGroups[1] = createTagGroup(new Tag("highway", "steps"));
		tagGroups[2] = createTagGroup(new Tag("highway", "steps"), new Tag("escalator", "yes"));
		tagGroups[3] = createTagGroup(new Tag("disused", "yes"), new Tag("construction", "no"));

		for (Implication implication : implications) {
			for (int i = 0; i < tagGroups.length; i++) {
				tagGroups[i] = implication.apply(tagGroups[i]);
			}
		}

		assertSame(2, tagGroups[0].size());
		assertTrue(tagGroups[0].contains(new Tag("bicycle", "designated")));

		assertSame(2, tagGroups[1].size());
		assertTrue(tagGroups[1].contains(new Tag("normal_steps", "yes")));

		assertSame(2, tagGroups[2].size());
		assertFalse(tagGroups[2].contains(new Tag("normal_steps", "yes")));

		assertSame(3, tagGroups[3].size());
		assertTrue(tagGroups[3].contains(new Tag("usable", "no")));
	}

	private static TagGroup createTagGroup(Tag... tags) {
		Map<String, String> tagMap = new HashMap<String, String>();
		for (Tag tag : tags) {
			tagMap.put(tag.key, tag.value);
		}
		return new MapBasedTagGroup(tagMap);
	}

}
