// License: GPL. For details, see LICENSE file.
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

public class TestDataSource
implements DataSource<TestDataSource.TestNode, TestDataSource.TestWay, TestDataSource.TestRelation, TestDataSource.TestRelationMember> {

    public static class TestPrimitive {
        public final Map<String, String> tags = new HashMap<>();
    }

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
        public final List<TestNode> nodes = new LinkedList<>();
        @Override
        public String toString() {
            return nodes + "; " + tags;
        }
    }

    public static class TestRelation extends TestPrimitive {
        public final Collection<TestRelationMember> members = new LinkedList<>();
        @Override
        public String toString() {
            return members + "; " + tags;
        }
    }

    public static class TestRelationMember {
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


    public final Collection<TestNode> nodes = new LinkedList<>();
    public final Collection<TestWay> ways = new LinkedList<>();
    public final Collection<TestRelation> relations = new LinkedList<>();

    @Override
    public double getLat(TestNode node) {
        return node.lat;
    }

    @Override
    public double getLon(TestNode node) {
        return node.lon;
    }

    @Override
    public Iterable<TestRelationMember> getMembers(TestRelation relation) {
        return relation.members;
    }

    @Override
    public Iterable<TestNode> getNodes() {
        return nodes;
    }

    @Override
    public Iterable<TestNode> getNodes(TestWay way) {
        return way.nodes;
    }

    @Override
    public Iterable<TestWay> getWays() {
        return ways;
    }

    @Override
    public Iterable<TestRelation> getRelations() {
        return relations;
    }

    @Override
    public TagGroup getTagsN(TestNode node) {
        return new MapBasedTagGroup(node.tags);
    }

    @Override
    public TagGroup getTagsW(TestWay way) {
        return new MapBasedTagGroup(way.tags);
    }

    @Override
    public TagGroup getTagsR(TestRelation relation) {
        return new MapBasedTagGroup(relation.tags);
    }

    @Override
    public Object getMember(TestRelationMember member) {
        return member.getMember();
    }

    @Override
    public String getRole(TestRelationMember member) {
        return member.getRole();
    }

    @Override
    public boolean isNMember(TestRelationMember member) {
        return member.getMember() instanceof TestNode;
    }

    @Override
    public boolean isWMember(TestRelationMember member) {
        return member.getMember() instanceof TestWay;
    }

    @Override
    public boolean isRMember(TestRelationMember member) {
        return member.getMember() instanceof TestRelation;
    }

    @Override
    public void addObserver(DataSourceObserver observer) {
        // not needed for test
    }

    @Override
    public void deleteObserver(DataSourceObserver observer) {
        // not needed for test
    }

}
