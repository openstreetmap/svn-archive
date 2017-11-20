// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.graphview.plugin.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.graphview.core.data.DataSource;
import org.openstreetmap.josm.plugins.graphview.core.data.DataSourceObserver;
import org.openstreetmap.josm.plugins.graphview.core.data.MapBasedTagGroup;
import org.openstreetmap.josm.plugins.graphview.core.data.TagGroup;

/**
 * DataSource that gets data from JOSM;
 * this DataSource type does not send updates!
 */

public class JOSMDataSource implements DataSource<Node, Way, Relation, RelationMember> {

    @Override
    public double getLat(Node node) {
        return node.getCoor().lat();
    }

    @Override
    public double getLon(Node node) {
        return node.getCoor().lon();
    }

    @Override
    public Iterable<RelationMember> getMembers(Relation relation) {
        return relation.getMembers();
    }

    @Override
    public Iterable<Node> getNodes(Way way) {
        return new FilteredOsmPrimitiveIterable<>(way.getNodes());
    }

    @Override
    public Iterable<Node> getNodes() {
        return new FilteredOsmPrimitiveIterable<>(MainApplication.getLayerManager().getEditDataSet().getNodes());
    }

    @Override
    public Iterable<Relation> getRelations() {
        return new FilteredRelationIterable(MainApplication.getLayerManager().getEditDataSet().getRelations());
    }

    @Override
    public Iterable<Way> getWays() {
        return new FilteredOsmPrimitiveIterable<>(MainApplication.getLayerManager().getEditDataSet().getWays());
    }

    @Override
    public TagGroup getTagsN(Node node) {
        return getTags(node);
    }

    @Override
    public TagGroup getTagsW(Way way) {
        return getTags(way);
    }

    @Override
    public TagGroup getTagsR(Relation relation) {
        return getTags(relation);
    }

    private TagGroup getTags(OsmPrimitive primitive) {
        if (primitive.getKeys() == null) {
            return EMPTY_TAG_GROUP;
        } else {
            return new MapBasedTagGroup(primitive.getKeys());
        }
    }

    @Override
    public Object getMember(RelationMember member) {
        return member.getMember();
    }

    @Override
    public String getRole(RelationMember member) {
        return member.getRole();
    }

    @Override
    public boolean isNMember(RelationMember member) {
        return member.getMember() instanceof Node;
    }

    @Override
    public boolean isWMember(RelationMember member) {
        return member.getMember() instanceof Way;
    }

    @Override
    public boolean isRMember(RelationMember member) {
        return member.getMember() instanceof Relation;
    }


    private static final TagGroup EMPTY_TAG_GROUP;
    static {
        Map<String, String> emptyMap = new HashMap<>(0);
        EMPTY_TAG_GROUP = new MapBasedTagGroup(emptyMap);
    }

    /**
     * Iterable of OsmPrimitive objects based on an existing Iterable,
     * will filter incomplete and deleted objects from the iterator.
     *
     * @param <P>  OsmPrimitive subtype
     */
    public static class FilteredOsmPrimitiveIterable<P extends OsmPrimitive> implements Iterable<P> {

        private final Iterable<P> originalIterable;

        public FilteredOsmPrimitiveIterable(Iterable<P> originalIterable) {
            this.originalIterable = originalIterable;
        }

        /** returns an iterator. The iterator does not support {@link Iterator#remove()}. */
        @Override
        public Iterator<P> iterator() {
            return new FilteredIterator(originalIterable.iterator());
        }

        private class FilteredIterator implements Iterator<P> {

            private final Iterator<P> originalIterator;

            private P next;

            FilteredIterator(Iterator<P> originalIterator) {
                this.originalIterator = originalIterator;
                updateNext();
            }

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public P next() {
                if (next != null) {
                    P result = next;
                    updateNext();
                    return result;
                } else {
                    throw new NoSuchElementException();
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            private void updateNext() {
                next = null;
                while (originalIterator.hasNext()) {
                    P originalNext = originalIterator.next();
                    if (accept(originalNext)) {
                        next = originalNext;
                        break;
                    }
                }
            }

        }

        protected boolean accept(P primitive) {
            return !primitive.isDeleted() && !primitive.isIncomplete();
        }
    }

    /**
     * Relation-specific variant of the FilteredOsmPrimitiveIterable,
     * also checks completeness of relation's members
     */
    public static class FilteredRelationIterable extends FilteredOsmPrimitiveIterable<Relation> {

        public FilteredRelationIterable(Iterable<Relation> originalIterable) {
            super(originalIterable);
        }

        @Override
        protected boolean accept(Relation relation) {
            boolean complete = true;
            for (org.openstreetmap.josm.data.osm.RelationMember member : relation.getMembers()) {
                if (member.getMember() == null || member.getMember().isDeleted() || member.getMember().isIncomplete()) {
                    complete = false;
                }
            }
            return complete && super.accept(relation);
        }
    }

    static class RelationMemberImpl {
        private final String role;
        private final Object member;

        RelationMemberImpl(org.openstreetmap.josm.data.osm.RelationMember originalMember) {
            this.role = originalMember.getRole();
            this.member = originalMember.getMember();
        }

        public String getRole() {
            return role;
        }

        public Object getMember() {
            return member;
        }
    }

    private final Set<DataSourceObserver> observers = new HashSet<>();

    @Override
    public void addObserver(DataSourceObserver observer) {
        observers.add(observer);
    }

    @Override
    public void deleteObserver(DataSourceObserver observer) {
        observers.remove(observer);
    }

}
