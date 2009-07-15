package org.openstreetmap.josm.plugins.graphview.core.util;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.josm.plugins.graphview.core.data.MapBasedTagGroup;
import org.openstreetmap.josm.plugins.graphview.core.data.Tag;
import org.openstreetmap.josm.plugins.graphview.core.data.TagGroup;

public class TagConditionLogicTest {

	TagGroup groupA;
	TagGroup groupB;

	@Before
	public void setUp() {
		Map<String, String> mapA = new HashMap<String, String>();
		mapA.put("key1", "value1");
		mapA.put("key2", "value2");
		mapA.put("key3", "value1");
		groupA = new MapBasedTagGroup(mapA);

		Map<String, String> mapB = new HashMap<String, String>();
		mapB.put("key1", "value1");
		mapB.put("key4", "value4");
		groupB = new MapBasedTagGroup(mapB);
	}

	@Test
	public void testTag() {
		TagCondition condition = TagConditionLogic.tag(new Tag("key3", "value1"));
		assertTrue(condition.matches(groupA));
		assertFalse(condition.matches(groupB));
	}

	@Test
	public void testKey() {
		TagCondition condition = TagConditionLogic.key("key3");
		assertTrue(condition.matches(groupA));
		assertFalse(condition.matches(groupB));
	}

	@Test
	public void testAnd() {
		TagCondition condition1 = TagConditionLogic.tag(new Tag("key2", "value2"));
		TagCondition conditionAnd1a = TagConditionLogic.and(condition1);
		TagCondition conditionAnd1b = TagConditionLogic.and(Arrays.asList(condition1));

		assertTrue(conditionAnd1a.matches(groupA));
		assertTrue(conditionAnd1b.matches(groupA));
		assertFalse(conditionAnd1a.matches(groupB));
		assertFalse(conditionAnd1b.matches(groupB));

		TagCondition condition2 = TagConditionLogic.tag(new Tag("key1", "value1"));
		TagCondition conditionAnd2a = TagConditionLogic.and(condition1, condition2);
		TagCondition conditionAnd2b = TagConditionLogic.and(Arrays.asList(condition1, condition2));

		assertTrue(conditionAnd2a.matches(groupA));
		assertTrue(conditionAnd2b.matches(groupA));
		assertFalse(conditionAnd2a.matches(groupB));
		assertFalse(conditionAnd2b.matches(groupB));

		TagCondition condition3 = TagConditionLogic.tag(new Tag("key4", "value4"));
		TagCondition conditionAnd3a = TagConditionLogic.and(condition1, condition2, condition3);
		TagCondition conditionAnd3b = TagConditionLogic.and(Arrays.asList(condition1, condition2, condition3));

		assertFalse(conditionAnd3a.matches(groupA));
		assertFalse(conditionAnd3b.matches(groupA));
		assertFalse(conditionAnd3a.matches(groupB));
		assertFalse(conditionAnd3b.matches(groupB));
	}

	@Test
	public void testOr() {
		TagCondition condition1 = TagConditionLogic.tag(new Tag("key42", "value42"));
		TagCondition conditionOr1a = TagConditionLogic.or(condition1);
		TagCondition conditionOr1b = TagConditionLogic.or(Arrays.asList(condition1));

		assertFalse(conditionOr1a.matches(groupA));
		assertFalse(conditionOr1b.matches(groupA));
		assertFalse(conditionOr1a.matches(groupB));
		assertFalse(conditionOr1b.matches(groupB));

		TagCondition condition2 = TagConditionLogic.tag(new Tag("key3", "value1"));
		TagCondition conditionOr2a = TagConditionLogic.or(condition1, condition2);
		TagCondition conditionOr2b = TagConditionLogic.or(Arrays.asList(condition1, condition2));

		assertTrue(conditionOr2a.matches(groupA));
		assertTrue(conditionOr2b.matches(groupA));
		assertFalse(conditionOr2a.matches(groupB));
		assertFalse(conditionOr2b.matches(groupB));

		TagCondition condition3 = TagConditionLogic.tag(new Tag("key1", "value1"));
		TagCondition conditionOr3a = TagConditionLogic.or(condition1, condition2, condition3);
		TagCondition conditionOr3b = TagConditionLogic.or(Arrays.asList(condition1, condition2, condition3));

		assertTrue(conditionOr3a.matches(groupA));
		assertTrue(conditionOr3b.matches(groupA));
		assertTrue(conditionOr3a.matches(groupB));
		assertTrue(conditionOr3b.matches(groupB));
	}

	@Test
	public void testNot() {
		TagCondition condition = TagConditionLogic.not(TagConditionLogic.key("key3"));
		assertFalse(condition.matches(groupA));
		assertTrue(condition.matches(groupB));
	}

}
