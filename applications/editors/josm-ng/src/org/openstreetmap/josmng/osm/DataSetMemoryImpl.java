/*
 *  JOSMng - a Java Open Street Map editor, the next generation.
 * 
 *  Copyright (C) 2008 Petr Nejedly <P.Nejedly@sh.cvut.cz>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package org.openstreetmap.josmng.osm;

import java.util.Collections;
import java.util.Map;
import org.openstreetmap.josmng.utils.Hash;
import org.openstreetmap.josmng.utils.Storage;

/**
 * A simple, memory-bound implementation of the DataSet backing store.
 * 
 * @author nenik
 */
final class DataSetMemoryImpl extends DataSet {
    private final Storage<OsmPrimitive> primitives = new Storage<OsmPrimitive>();
    
    // Beware, maps declared this way have all other types inside their
    // values() collection. Do not use these maps in other way than
    // id-based lookup
    private final Map<Long,Node> nodes = asMap(primitives, Node.class);
    private final Map<Long,Way> ways = asMap(primitives, Way.class);
    private final Map<Long,Relation> relations = asMap(primitives, Relation.class);
        
    public Node getNode(long id) {
        return nodes.get(id);
    }
    
    public Way getWay(long id) {
        return ways.get(id);
    }

    public Relation getRelation(long id) {
        return relations.get(id);
    }
    
    protected @Override void addPrimitive(OsmPrimitive prim) {
        primitives.add(prim);
    }

    protected @Override void removePrimitive(OsmPrimitive prim) {
        primitives.remove(prim);
    }

    public Iterable<OsmPrimitive> getPrimitives(Bounds b) {
        return Collections.unmodifiableCollection(primitives);
    }

    private static <T extends OsmPrimitive> Map<Long,T> asMap(Storage<? super T> data, Class<T> type) {
        return data.foreignKey(new IdHash(type));
    }
        
    private static class IdHash<T extends OsmPrimitive> implements Hash<Long,T> {
        Class<T> cls;
        
        IdHash(Class<T> cls) {
            this.cls = cls;
        }

        public int getHashCode(Long k) {
            return (int)k.intValue() ^ cls.hashCode();
        }

        public boolean equals(Long k, T t) {
            if (k == 0 || t == null) return false;
            return t.getId() == k.longValue() && t.getClass() == cls;
        }        
    }
}
