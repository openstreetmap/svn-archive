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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.EventListenerList;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;
import org.openstreetmap.josmng.osm.io.OsmFormat;

/**
 * A single encapsulated OSM dataset. It might be parsed from file, loaded from
 * server or obtained by any other means. It can be held in memory or
 * implemented over a database, or even using some hybrid storage, based
 * on subclassing.
 * 
 * @author nenik
 */
public abstract class DataSet {
    private final UndoableEditSupport undoSupport = new ComposingUndoSupport();
    private final EventListenerList listeners = new EventListenerList();

    private final Map<Integer,String> users = new HashMap<Integer, String>();
    private final Map<String,Integer> usersBack = new HashMap<String, Integer>();
    
    private final ThreadLocal<Object> currentToken = new  ThreadLocal<Object>();
    
    public void atomicEdit(Runnable r, Object token) {
        Object oldToken = currentToken.get();
        currentToken.set(token);
        undoSupport.beginUpdate();
        try {
            r.run();
        } finally {
            undoSupport.endUpdate();
            currentToken.set(oldToken);
        }
    }
    
    public void removeUndoableEditListener(UndoableEditListener l) {
        undoSupport.removeUndoableEditListener(l);
    }

    public void addUndoableEditListener(UndoableEditListener l) {
        undoSupport.addUndoableEditListener(l);
    }

    public void addDataSetListener(DataSetListener dsl) {
        listeners.add(DataSetListener.class, dsl);
    }

    public void removeDataSetListener(DataSetListener dsl) {
        listeners.remove(DataSetListener.class, dsl);
    }

    void firePrimitivesAdded(Collection<? extends OsmPrimitive> added) {
        for (DataSetListener dsl : listeners.getListeners(DataSetListener.class)) {
            dsl.primtivesAdded(added);
        }
    }

    void firePrimitivesRemoved(Collection<? extends OsmPrimitive> removed) {
        for (DataSetListener dsl : listeners.getListeners(DataSetListener.class)) {
            dsl.primtivesRemoved(removed);
        }
    }

    void fireTagsChanged(OsmPrimitive prim) {
        for (DataSetListener dsl : listeners.getListeners(DataSetListener.class)) {
            dsl.tagsChanged(prim);
        }        
    }

    void fireNodeMoved(Node node) {
        for (DataSetListener dsl : listeners.getListeners(DataSetListener.class)) {
            dsl.nodeMoved(node);
        }
    }
    
    void fireWayNodesChanged(Way way) {
        for (DataSetListener dsl : listeners.getListeners(DataSetListener.class)) {
            dsl.wayNodesChanged(way);
        }
    }

    void fireRelationMembersChanged(Relation r) {
        for (DataSetListener dsl : listeners.getListeners(DataSetListener.class)) {
            dsl.relationMembersChanged(r);
        }
    }

    void addRemovePrimitive(OsmPrimitive prim, boolean add) {
        UndoableEdit edit = new AddRemovePrimitiveEdit(prim, add);
        addRemovePrimitiveImpl(prim, add);
        postEdit(edit);
    }

    private void addRemovePrimitiveImpl(OsmPrimitive prim, boolean add) {
        if (add) {
            addPrimitive(prim);
            firePrimitivesAdded(Collections.singleton(prim));
        } else {
            removePrimitive(prim);
            firePrimitivesRemoved(Collections.singleton(prim));
        }
    }

    public Node createNode(double lat, double lon) {
        Node n = new Node(this, 0, lat, lon, -1, null, true);
        addRemovePrimitive(n, true);
        return n;
    }
        
    public Way createWay(final Node ... nodes) {
        final Way[] w = new Way[1];
        atomicEdit(new Runnable() {
            public void run() {
                w[0] = new Way(DataSet.this, 0, -1, null, true, null);
                addRemovePrimitive(w[0], true);
                w[0].setNodes(Arrays.asList(nodes));
            }
        }, null);
        return w[0];
    }

    public Relation createRelation(final Map<OsmPrimitive,String>members) {
        final Relation[] r = new Relation[1];
        atomicEdit(new Runnable() {
            public void run() {
                r[0] = new Relation(DataSet.this, 0, -1, null, true, null);
                addRemovePrimitive(r[0], true);
                for (Map.Entry<OsmPrimitive, String> entry : members.entrySet()) {
                    r[0].addMember(entry.getKey(), entry.getValue());
                }
            }
        }, null);
        return r[0];
    }

    // The minimal interface to the backing store implementation.
    protected abstract void addPrimitive(OsmPrimitive prim);
    protected abstract void removePrimitive(OsmPrimitive prim);

    /**
     * Get the primitives inside given {@link Bounds}. The backing store
     * implementation can return data outside of the bounds, or may
     * even return all the data it knows about, without any filtering,
     * but it must return all the data within bounds.
     * 
     * @param b the Bounds limiting the query
     * @return all the primitives within bounds.
     */
    abstract public Iterable<OsmPrimitive> getPrimitives(Bounds b);
    abstract public Way getWay(long id);    
    abstract public Node getNode(long id);
    abstract public Relation getRelation(long id);
    
    void postEdit(UndoableEdit edit) {
        undoSupport.postEdit(edit);        
    }
    
    String getUserForId(int id) {
        assert users.containsKey(id);
        return users.get(id);
    }
    
    int getIdForUser(String user) {
        Integer id = usersBack.get(user);
        if (id == null) { // new user, generate ID
            id = usersBack.size();
            assert !users.containsKey(id);
            usersBack.put(user, id);
            users.put(id, user);
        }
        return id;
    }

    abstract static class BaseToggleEdit extends AbstractUndoableEdit {
        private final String name;
        
        protected BaseToggleEdit(String dispName) {
            this.name = dispName;
        }

        public @Override String getPresentationName() {
            return name;
        }

        public @Override void undo() throws CannotUndoException {
            super.undo(); // to validate
            doToggle();
        }

        public @Override void redo() throws CannotRedoException {
            super.redo(); // to validate
            doToggle();
        }

        protected void doToggle() {
            toggle();
        }
        
        protected abstract void toggle();
    }

    private class AddRemovePrimitiveEdit extends BaseToggleEdit {
        OsmPrimitive prim;
        boolean addEdit;

        public AddRemovePrimitiveEdit(OsmPrimitive prim, boolean addEdit) {
            super("add");
            this.prim = prim;
            this.addEdit = addEdit;
        }
        
        
        protected void toggle() {
            addEdit = !addEdit;
            addRemovePrimitiveImpl(prim, addEdit);
        }
    }
    
    private class ComposingUndoSupport extends UndoableEditSupport {
        ComposingUndoSupport() {
            super(DataSet.this);
        }
        
        protected @Override CompoundEdit createCompoundEdit() {
            Object token = currentToken.get();
            return new SlidingCompoundEdit(token);
        }
    }
    
    private class SlidingCompoundEdit extends CompoundEdit {
        private Object token;

        public SlidingCompoundEdit(Object token) {
            this.token = token != null ? token : new Object();
        }

        public @Override boolean addEdit(UndoableEdit anEdit) {
            assert anEdit instanceof BaseToggleEdit;
            if (super.addEdit(anEdit)) return true; // in progress: collected
            
            // already closed edit, check if the comming edit is about
            // to supersede current one (based on the token)
            if (anEdit instanceof SlidingCompoundEdit) {
                SlidingCompoundEdit sce = (SlidingCompoundEdit)anEdit;
                if (sce.token.equals(token)) {
                    token = sce.token;
                    return true;
                }
            }
            return false;
        }
    }

    public static DataSet empty() {
        return factory(1000).create();
    }

    public static Factory factory(int capacity) {
        return new Factory(capacity);
    }
    
    public static DataSet fromStream(InputStream is) throws  IOException {
        return OsmFormat.read(is);
    }
    
    public static final class Factory {
        private DataSet ds;
        private OsmPrimitive lastPrimitive;
        private Map<Long,Node> newNodes = new HashMap<Long, Node>();
        private Map<Long,Way> newWays = new HashMap<Long, Way>();
        private Map<Long,Relation> newRels = new HashMap<Long, Relation>();

        private Factory(int capa) {
            ds = new DataSetMemoryImpl(capa);
        }

        public DataSet create() {
            DataSet ret = ds;
            ds = null; // disable further modifications
            return ret;
        }
        public Node getNode(long id) {
            return id < 0 ? newNodes.get(id) : ds.getNode(id);
        }
                
        public Node node(long id, double lat, double lon, int time, String user, boolean visible) {
            ds.getClass(); // null check
            Node n = new Node(ds, id < 0 ? 0 : id, lat, lon, time, user, visible);
            ds.addPrimitive(n);
            lastPrimitive = n;
            if (id < 0) newNodes.put(id, n);
            return n;
        }

        public Way getWay(long id) {
            return id < 0 ? newWays.get(id) : ds.getWay(id);
        }

        public Way way(long id, int time, String user, boolean visible, Node[] nodes) {
            ds.getClass(); // null check
            Way w = new Way(ds, id < 0 ? 0 : id, time, user, visible, nodes); // XXX vis
            ds.addPrimitive(w);
            lastPrimitive = w;
            if (id < 0) newWays.put(id, w);
            return w;
        }
        
        public Relation getRelation(long id) {
            return id < 0 ? newRels.get(id) : ds.getRelation(id);
        }
                
        public Relation relation(long id, int time, String user, boolean visible, Map<OsmPrimitive,String> members) {
            ds.getClass(); // null check
            Relation r = new Relation(ds, id < 0 ? 0 : id, time, user, visible, members); // XXX vis
            ds.addPrimitive(r);
            lastPrimitive = r;
            if (id < 0) newRels.put(id, r);
            return r;
        }
        
        /**
         * Replace the tags of the last created primitive.
         * @param pairs
         */
        public void setTags(String[] pairs) {
            ds.getClass(); // null check
            lastPrimitive.setTags(pairs);
        }

        /**
         * Set a tag on the last created primitive.
         * @param pairs
         */
        public void putTag(String key, String val) {
            ds.getClass(); // null check
            lastPrimitive.putTagImpl(key, val);
        }

        /**
         * Set nodes of the last created primitive.
         * @param pairs
         */
        public void setNodes(List<Node> nodes) {
            ds.getClass(); // null check
            ((Way)lastPrimitive).setNodesImpl(nodes.toArray(new Node[nodes.size()]));
        }
        
        /**
         * Add a member to the last relation
         * @param pairs
         */
        public void addMember(OsmPrimitive member, String role) {
            ds.getClass(); // null check
            ((Relation)lastPrimitive).setMemberRoleImpl(member, role);
        }
        
        /**
         * Adjust the flags of the last created primitive.
         * @param pairs
         */
        public void setFlags(boolean modified, boolean deleted) {
            ds.getClass(); // null check
            lastPrimitive.setModified(modified);
            lastPrimitive.setDeletedImpl(deleted);
        }
    }
}
