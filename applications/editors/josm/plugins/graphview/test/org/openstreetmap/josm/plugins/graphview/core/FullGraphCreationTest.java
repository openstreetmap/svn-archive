package org.openstreetmap.josm.plugins.graphview.core;

import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.junit.Test;
import org.openstreetmap.josm.plugins.graphview.core.TestDataSource.TestNode;
import org.openstreetmap.josm.plugins.graphview.core.TestDataSource.TestRelation;
import org.openstreetmap.josm.plugins.graphview.core.TestDataSource.TestRelationMember;
import org.openstreetmap.josm.plugins.graphview.core.TestDataSource.TestWay;
import org.openstreetmap.josm.plugins.graphview.core.access.AccessParameters;
import org.openstreetmap.josm.plugins.graphview.core.access.AccessRuleset;
import org.openstreetmap.josm.plugins.graphview.core.access.AccessType;
import org.openstreetmap.josm.plugins.graphview.core.access.Implication;
import org.openstreetmap.josm.plugins.graphview.core.data.Tag;
import org.openstreetmap.josm.plugins.graphview.core.graph.TSBasedWayGraph;
import org.openstreetmap.josm.plugins.graphview.core.graph.WayGraph;
import org.openstreetmap.josm.plugins.graphview.core.property.RoadPropertyType;
import org.openstreetmap.josm.plugins.graphview.core.property.RoadWidth;
import org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyType;
import org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyTypes;
import org.openstreetmap.josm.plugins.graphview.core.transition.GenericTransitionStructure;
import org.openstreetmap.josm.plugins.graphview.core.transition.TransitionStructure;
import org.openstreetmap.josm.plugins.graphview.plugin.preferences.PreferenceAccessParameters;
import org.openstreetmap.josm.plugins.graphview.plugin.preferences.VehiclePropertyStringParser.PropertyValueSyntaxException;

public class FullGraphCreationTest {

	private static final AccessParameters ACCESS_PARAMS;
	static {
		Map<VehiclePropertyType<?>, String> vehiclePropertyValues =
			new HashMap<VehiclePropertyType<?>, String>();
		vehiclePropertyValues.put(VehiclePropertyTypes.WIDTH, "3.0");

		try {
			ACCESS_PARAMS = new PreferenceAccessParameters(
					"test_vehicle",
					Arrays.asList(AccessType.UNDEFINED),
					vehiclePropertyValues);
		} catch (PropertyValueSyntaxException e) {
			throw new Error(e);
		}
	}

	private static final AccessRuleset TEST_RULESET = new AccessRuleset() {
		public java.util.List<String> getAccessHierarchyAncestors(String transportMode) {
			return Arrays.asList(transportMode);
		}
		public java.util.Collection<Tag> getBaseTags() {
			return Arrays.asList(new Tag("highway", "test"));
		}
		public java.util.List<Implication> getImplications() {
			return new LinkedList<Implication>();
		}
	};

	@Test
	public void testTJunction() {

		TestDataSource ds = new TestDataSource();

		TestNode nodeN = new TestNode(2, 1);
		TestNode nodeW = new TestNode(1, 0);
		TestNode nodeS = new TestNode(0, 1);
		TestNode nodeC = new TestNode(1, 1);

		ds.nodes.addAll(Arrays.asList(nodeN, nodeW, nodeS, nodeC));

		TestWay wayNC = new TestWay();
		wayNC.tags.put("highway", "test");
		wayNC.nodes.addAll(Arrays.asList(nodeN, nodeC));
		TestWay wayCS = new TestWay();
		wayCS.tags.put("highway", "test");
		wayCS.nodes.addAll(Arrays.asList(nodeC, nodeS));
		TestWay wayCW = new TestWay();
		wayCW.tags.put("highway", "test");
		wayCW.nodes.addAll(Arrays.asList(nodeC, nodeW));

		ds.ways.add(wayNC);
		ds.ways.add(wayCS);
		ds.ways.add(wayCW);

		/* variant 1: no restrictions */
		{
			TransitionStructure ts1 = createTestTransitionStructure(ds);

			assertSame(4, size(ts1.getNodes()));
			assertSame(6, size(ts1.getSegments()));
			assertSame(0, size(ts1.getRestrictions()));

			WayGraph graph1 = new TSBasedWayGraph(ts1);

			assertSame(12, graph1.getNodes().size());
			assertSame(24, graph1.getEdges().size());
		}
		/* variant 2: no left turn from S to W */
		{
			ds.relations.add(createTurnRestrictionRelation(wayCS, nodeC, wayCW, "no_left_turn"));
			TransitionStructure ts2 = createTestTransitionStructure(ds);

			assertSame(4, size(ts2.getNodes()));
			assertSame(6, size(ts2.getSegments()));
			assertSame(1, size(ts2.getRestrictions()));

			WayGraph graph2 = new TSBasedWayGraph(ts2);

			assertSame(12, graph2.getNodes().size());
			assertSame(23, graph2.getEdges().size());
		}

	}

	@Test
	public void testBarrier() {

		TestDataSource ds = new TestDataSource();

		TestNode node1 = new TestNode(0, 1);
		TestNode nodeB = new TestNode(0, 2);
		nodeB.tags.put("width", "1");
		TestNode node2 = new TestNode(0, 3);

		ds.nodes.addAll(Arrays.asList(node1, nodeB, node2));

		TestWay way = new TestWay();
		way.tags.put("highway", "test");
		way.tags.put("oneway", "yes");
		way.nodes.addAll(Arrays.asList(node1, nodeB, node2));
		ds.ways.add(way);

		/* variant 1: no restrictions */

		TransitionStructure ts = createTestTransitionStructure(ds);

		assertSame(3, size(ts.getNodes()));
		assertSame(2, size(ts.getSegments()));
		assertSame(1, size(ts.getRestrictions()));

		WayGraph graph = new TSBasedWayGraph(ts);

		assertSame(4, graph.getNodes().size());
		assertSame(2, graph.getEdges().size());

	}

	private TestRelation createTurnRestrictionRelation(
			TestWay from, TestNode via, TestWay to, String restriction) {
		TestRelation resultRelation = new TestRelation();
		resultRelation.tags.put("type", "restriction");
		resultRelation.tags.put("restriction", restriction);
		resultRelation.members.add(new TestRelationMember("from", from));
		resultRelation.members.add(new TestRelationMember("via", via));
		resultRelation.members.add(new TestRelationMember("to", to));
		return resultRelation;
	}

	private TransitionStructure createTestTransitionStructure(TestDataSource dataSource) {

		LinkedList<RoadPropertyType<?>> properties = new LinkedList<RoadPropertyType<?>>();
		properties.add(new RoadWidth());

		return new GenericTransitionStructure<TestNode, TestWay, TestRelation>(
				TestNode.class, TestWay.class, TestRelation.class,
				ACCESS_PARAMS, TEST_RULESET, dataSource, properties);
	}

	private static int size(Iterable<?> iterable) {
		Iterator<?> iterator = iterable.iterator();
		int size = 0;
		while (iterator.hasNext()) {
			iterator.next();
			size ++;
		}
		return size;
	}

}
