package org.openstreetmap.josm.plugins.graphview.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openstreetmap.josm.plugins.graphview.core.data.DataSource;
import org.openstreetmap.josm.plugins.graphview.core.data.DataSourceObserver;
import org.openstreetmap.josm.plugins.graphview.core.data.MapBasedTagGroup;
import org.openstreetmap.josm.plugins.graphview.core.data.TagGroup;

public class TestDataSource implements DataSource<TestDataSource.TestNode, TestDataSource.TestWay, TestDataSource.TestRelation> {

	public static class TestPrimitive {
		public final Map<String, String> tags = new HashMap<String, String>();
	};

	public static class TestNode extends TestPrimitive {
		public final double lat;
		public final double lon;
		public TestNode() {
			this(0, 0);
		}
		public TestNode(double lat, double lon) {
			this.lat = lat;
			this.lon = lon;
		}
		@Override
		public String toString() {
			return "(" + lat + ", " + lon + "); " + tags;
		}
	}

	public static class TestWay extends TestPrimitive {
		public final List<TestNode> nodes = new LinkedList<TestNode>();
		@Override
		public String toString() {
			return nodes + "; " + tags;
		}
	}

	public static class TestRelation extends TestPrimitive {
		public final Collection<RelationMember> members = new LinkedList<RelationMember>();
		@Override
		public String toString() {
			return members + "; " + tags;
		}
	}

	public static class TestRelationMember implements RelationMember {
		public final String role;
		public final TestPrimitive member;
		public TestRelationMember(String role, TestPrimitive member) {
			this.role = role;
			this.member = member;
		}
		public TestPrimitive getMember() {
			return member;
		}
		public String getRole() {
			return role;
		}
		@Override
		public String toString() {
			return role + "=" + member;
		}
	}


	public final Collection<TestNode> nodes = new LinkedList<TestNode>();
	public final Collection<TestWay> ways = new LinkedList<TestWay>();
	public final Collection<TestRelation> relations = new LinkedList<TestRelation>();


	public double getLat(TestNode node) {
		return node.lat;
	}
	public double getLon(TestNode node) {
		return node.lon;
	}

	public Iterable<RelationMember> getMembers(TestRelation relation) {
		return relation.members;
	}

	public Iterable<TestNode> getNodes() {
		return nodes;
	}

	public Iterable<TestNode> getNodes(TestWay way) {
		return way.nodes;
	}

	public Iterable<TestWay> getWays() {
		return ways;
	}

	public Iterable<TestRelation> getRelations() {
		return relations;
	}

	public TagGroup getTagsN(TestNode node) {
		return new MapBasedTagGroup(node.tags);
	}

	public TagGroup getTagsW(TestWay way) {
		return new MapBasedTagGroup(way.tags);
	}

	public TagGroup getTagsR(TestRelation relation) {
		return new MapBasedTagGroup(relation.tags);
	}

	public void addObserver(DataSourceObserver observer) {
		// not needed for test
	}

	public void deleteObserver(DataSourceObserver observer) {
		// not needed for test
	}

}
