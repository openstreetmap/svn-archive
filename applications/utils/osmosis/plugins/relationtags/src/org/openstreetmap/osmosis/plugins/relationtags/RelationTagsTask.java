package org.openstreetmap.osmosis.plugins.relationtags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.container.v0_6.*;
import org.openstreetmap.osmosis.core.domain.v0_6.*;
import org.openstreetmap.osmosis.core.filter.common.IdTracker;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.IndexedObjectStore;
import org.openstreetmap.osmosis.core.store.IndexedObjectStoreReader;
import org.openstreetmap.osmosis.core.store.SimpleObjectStore;
import org.openstreetmap.osmosis.core.store.SingleClassObjectSerializationFactory;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.task.v0_6.SinkSource;

/**
 * For every member of specified relations sets relationtype_relationtag tags.
 * 
 * This class (and other classes in the package) are in Public Domain.
 * 
 * @author Zverik
 */
public class RelationTagsTask implements SinkSource, EntityProcessor {
    private Sink sink;
    private TreeSet<String> types;
    private String separator;
    private String multi;
    private boolean sort;

    private final SimpleObjectStore<EntityContainer> entities;
    private final IndexedObjectStore<Relation> relations;
    private ReferenceTracker refs;

    public RelationTagsTask() {
        this(new String[0], "_", ";", false);
    }

    public RelationTagsTask( String[] types, String separator, String multi, boolean sort ) {
        this.types = new TreeSet(Arrays.asList(types));
        this.separator = separator;
        this.multi = multi;
        this.sort = sort;

        entities = new SimpleObjectStore<EntityContainer>(new EntityContainerObjectSerializationFactory(), "entities", true);
        relations = new IndexedObjectStore<Relation>(new SingleClassObjectSerializationFactory(Relation.class), "relations");
        refs = new ReferenceTracker();
    }

    public void process( EntityContainer entityContainer ) {
        entityContainer.process(this);
    }

    public void process( BoundContainer boundContainer ) {
        sink.process(boundContainer);
    }

    public void process( NodeContainer container ) {
        entities.add(container);
    }

    public void process( WayContainer container ) {
        entities.add(container);
    }

    public void process( RelationContainer container ) {
        Relation r = container.getEntity();
        String type = getRelationType(r);
        if( type != null && types.contains(type) ) {
            relations.add(r.getId(), r);
            for( RelationMember m : r.getMembers() ) {
                if( m.getMemberType() == EntityType.Node || m.getMemberType() == EntityType.Way )
                    refs.set(m.getMemberId(), r.getId());
            }
        }
        sink.process(container);
    }

    private String getRelationType( Relation r ) {
        for( Tag tag : r.getTags() ) {
            if( tag.getKey().equals("type") )
                return tag.getValue();
        }
        return null;
    }

    public void complete() {
        entities.complete();
        relations.complete();
        IndexedObjectStoreReader<Relation> relationReader = relations.createReader();
        ReleasableIterator<EntityContainer> iter = entities.iterate();
        while( iter.hasNext() ) {
            EntityContainer container = iter.next();
            EntityType entityType = container.getEntity().getType();
            long entityId = container.getEntity().getId();
            final IdTracker relIdTracker = refs.get(entityId);
            if( relIdTracker != null ) {
                Iterator<Long> relationIds = relIdTracker.iterator();
                while( relationIds.hasNext() ) {
                    Relation r = relationReader.get(relationIds.next());
                    String relationTypePrefix = getRelationType(r) + separator;
                    for( RelationMember m : r.getMembers() ) {
                        if( m.getMemberType() == entityType && m.getMemberId() == entityId ) {
                            container = container.getWriteableInstance();

                            for( Tag tag : r.getTags() )
                                if( !tag.getKey().equals("type") )
                                    appendValue(container.getEntity(), relationTypePrefix + tag.getKey(), tag.getValue());
                            if( m.getMemberRole() != null && m.getMemberRole().length() > 0 )
                                appendValue(container.getEntity(), relationTypePrefix + "role", m.getMemberRole());
                        }
                    }
                }
            }
            sink.process(container);
        }
        refs = null;
        iter.release();
        relationReader.release();
        sink.complete();
    }

    private void appendValue( Entity e, String key, String value ) {
        String oldValue = null;
        for( Tag t : e.getTags() ) {
            if( t.getKey().equals(key) ) {
                oldValue = t.getValue();
                e.getTags().remove(t);
                break;
            }
        }
        if( oldValue != null && oldValue.length() > 0 ) {
            value = oldValue + multi + value;
            if( sort ) {
                String[] values = simpleSplit(multi, value);
                Arrays.sort(values, valueComparator);
                value = simpleJoin(multi, values);
            }
        }
        e.getTags().add(new Tag(key, value));
    }

    private static String[] simpleSplit( String divider, String src ) {
        List<String> result = new ArrayList<String>();
        int last = 0, pos;
        while( (pos = src.indexOf(divider, last)) >= 0 ) {
            result.add(src.substring(last, pos));
            last = pos + divider.length();
        }
        result.add(src.substring(last));
        return result.toArray(new String[result.size()]);
    }

    private static String simpleJoin( String divider, String[] src ) {
        StringBuilder result = new StringBuilder();
        for( int i = 0; i < src.length; i++ ) {
            if( i > 0 )
                result.append(divider);
            result.append(src[i]);
        }
        return result.toString();
    }

    public void release() {
        if( entities != null ) entities.release();
        if( relations != null ) relations.release();
        sink.release();
    }

    public void setSink( Sink sink ) {
        this.sink = sink;
    }

    private final static Comparator<String> valueComparator = new ValueComparator();

    private static class ValueComparator implements Comparator<String> {
        public int compare( String s1, String s2 ) {
            if( s1.length() == 0 )
                return s2.length() == 0 ? 0 : -1;
            else if( s2.length() == 0 )
                return 1;

            if( isNumber(s1) && isNumber(s2) ) {
                return Long.valueOf(s1).compareTo(Long.valueOf(s2));
            } else
                return s1.compareTo(s2);
        }

        private boolean isNumber( String s ) {
            boolean isNumber = true;
            for( int i = 0; i < s.length(); i++ )
                if( !Character.isDigit(s.charAt(i)) )
                    isNumber = false;
            return isNumber;
        }
    }

    public static void main( String[] args ) {
        System.out.println(Arrays.toString(simpleSplit("ab", "abcdababfghtuabkj")));
        System.out.println(Arrays.toString(simpleSplit(";", "1;13;46;0")));
        String[] values = simpleSplit(";", "f;5;aw;54;67;6;tr;Q1;;100;");
        Arrays.sort(values, valueComparator);
        System.out.println(Arrays.toString(values));
    }

    private static final Logger log = Logger.getLogger(RelationTagsTask.class.getName());
}
