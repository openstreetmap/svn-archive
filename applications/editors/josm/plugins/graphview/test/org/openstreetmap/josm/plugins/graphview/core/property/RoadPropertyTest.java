package org.openstreetmap.josm.plugins.graphview.core.property;

import static org.junit.Assert.assertEquals;

import org.openstreetmap.josm.plugins.graphview.core.TestDataSource;
import org.openstreetmap.josm.plugins.graphview.core.data.Tag;

abstract public class RoadPropertyTest {

	protected static <P> void testEvaluateW(RoadPropertyType<P> property, P expectedForward, P expectedBackward, Tag... wayTags) {

		TestDataSource ds = new TestDataSource();
		TestDataSource.TestWay testWay = new TestDataSource.TestWay();
		for (Tag tag : wayTags) {
			testWay.tags.put(tag.key, tag.value);
		}
		ds.ways.add(testWay);

		assertEquals(expectedForward, property.evaluateW(testWay, true, null, ds));
		assertEquals(expectedBackward, property.evaluateW(testWay, false, null, ds));

	}

	protected static <P> void testEvaluateN(RoadPropertyType<P> property, P expected, Tag... nodeTags) {

		TestDataSource ds = new TestDataSource();
		TestDataSource.TestNode testNode = new TestDataSource.TestNode();
		for (Tag tag : nodeTags) {
			testNode.tags.put(tag.key, tag.value);
		}
		ds.nodes.add(testNode);

		RoadMaxspeed m = new RoadMaxspeed();

		assertEquals(expected, m.evaluateN(testNode, null, ds));

	}

	protected static <P> void testEvaluateBoth(RoadPropertyType<P> property, P expected, Tag... nodeTags) {
		testEvaluateW(property, expected, expected, nodeTags);
		testEvaluateN(property, expected, nodeTags);
	}

}
