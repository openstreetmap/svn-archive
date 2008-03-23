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
import java.util.ArrayList;
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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.openstreetmap.josmng.utils.Hash;
import org.openstreetmap.josmng.utils.Storage;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A single encapsulated OSM dataset. It might be parsed from file, loaded from
 * server or obtained by any other means.
 * 
 * @author nenik
 */
public final class DataSet {
    private final UndoableEditSupport undoSupport = new ComposingUndoSupport();
    private final EventListenerList listeners = new EventListenerList();

    private final Map<Long,Node> nodes = createMap(Node.class);
    private final Collection<Node> nodesCol = nodes.values();
    
    private final Map<Long,Way> ways = createMap(Way.class);
    private final Collection<Way> waysCol = ways.values();
    
    private final Map<Long,Relation> relations = createMap(Relation.class);
    private final Collection<Relation> relationsCol = relations.values();
    
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

    void addRemovePrimitive(OsmPrimitive prim, boolean add) {
        UndoableEdit edit = new AddRemovePrimitiveEdit(prim, add);
        addRemovePrimitiveImpl(prim, add);
        postEdit(edit);
    }
    private void addRemovePrimitiveImpl(OsmPrimitive prim, boolean add) {
        Collection toModify = (prim instanceof Node) ? nodesCol :
                (prim instanceof Way) ? waysCol : relationsCol;
            
        if (add) {
            toModify.add(prim);
            firePrimitivesAdded(Collections.singleton(prim));
        } else {
            toModify.remove(prim);
            firePrimitivesRemoved(Collections.singleton(prim));
        }
    }
            

    public Node createNode(double lat, double lon) {
        Node n = new Node(this, 0, lat, lon, null, null, true);
        addRemovePrimitive(n, true);
        return n;
    }

    public Collection<Node> getNodes() {
        return Collections.unmodifiableCollection(nodesCol);
    }
    
    public Node getNode(long id) {
        return nodes.get(id);
    }
    
    public Way createWay(Node ... nodes) {
        Way way = new Way(this, 0, null, null, true);
        way.setNodes(Arrays.asList(nodes));
        addRemovePrimitive(way, true);
        return way;
    }

    public Collection<Way> getWays() {
        return Collections.unmodifiableCollection(waysCol);
    }
    
    public Way getWay(long id) {
        return ways.get(id);
    }

    public Collection<Relation> getRelations() {
        return Collections.unmodifiableCollection(relationsCol);        
    }

    public Relation getRelation(long id) {
        return relations.get(id);
    }
    
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
    
    private static <T extends OsmPrimitive> Map<Long,T> createMap(Class<T> contents) {
        return new Storage<T>().foreignKey(new IdHash(contents));
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
    
    public static DataSet fromStream(InputStream is) throws  IOException {
        Throwable cause;
        try {
            OsmStreamReader osr = new OsmStreamReader();
            InputSource src = new InputSource(is);
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            
            parser.parse(src, osr);
            return osr.constructed;
        } catch (ParserConfigurationException ex) {
            cause = ex;
        } catch (SAXException ex) {
            cause = ex;
        }
        IOException ioe = new IOException("Can't read the source");
        ioe.initCause(cause);
        throw ioe;
        
    }
    
    private static class OsmStreamReader extends DefaultHandler {
        private OsmPrimitive current;
        private DataSet constructed = new DataSet();
        private List<Node> wayNodes = new ArrayList<Node>();
    
        private Storage<String> strings = new Storage<String>();
       
    
        public @Override void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
            if (qName.equals("osm")) {
                    if (atts == null || !"0.5".equals(atts.getValue("version")))
                        throw new SAXException("Unknown version");
            } else if (qName.equals("bound")) {
            } else if (qName.equals("node")) {
                //  <node id='704062' timestamp='2007-07-25T09:26:24+01:00' user='Kubajz' visible='true' lat='50.0461188' lon='14.4748857'>
                //    <tag k='created_by' v='JOSM' />
                //  </node>

                // common attribs
                long id = getLong(atts, "id");
                String time = getString(atts, "timestamp");
                String user = atts.getValue("user");
                boolean vis = getBoolean(atts, "visible", true);

                double lat = getDouble(atts, "lat");
                double lon = getDouble(atts, "lon");

                Node n = new Node(constructed, id, lat, lon, time, user, vis);
                constructed.addRemovePrimitive(n, true);
                current = n;
            } else if (qName.equals("way")) {
                // common attribs
                long id = getLong(atts, "id");
                String time = getString(atts, "timestamp");
                String user = atts.getValue("user");
                boolean vis = getBoolean(atts, "visible", true);

                Way w = new Way(constructed, id, time, user, vis);
                constructed.addRemovePrimitive(w, true);
                current = w;
            } else if (qName.equals("nd")) {
                assert current instanceof Way;
                long nid = getLong(atts, "ref");
                Node n = constructed.getNode(nid);
                //assert n != null;
                if (n != null) wayNodes.add(n);
            } else if (qName.equals("tag")) {
    //            assert current != null;
                if (current != null) current.putTag(getString(atts, "k"), getString(atts, "v"));
            } else if (qName.equals("relation")) {
                // TODO: relation parsing 
            } else if (qName.equals("member")) {
                // TODO
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals("tag")) return;
            if (qName.equals("way")) {
                assert current instanceof Way;
                ((Way)current).setNodes(wayNodes);
                wayNodes.clear();
            }
            if (qName.equals("node") || qName.equals("way") || qName.equals("relation")) {
                current = null;
            }
        }
    
        private String getString(Attributes atts, String name) {
            String orig = atts.getValue(name);
            if (orig == null) return null;
            return strings.putUnique(orig);
        }

        private double getDouble(Attributes atts, String name) {
            return Double.parseDouble(atts.getValue(name));
        }
        private long getLong(Attributes atts, String name) {
            return Long.parseLong(atts.getValue(name));
        }

        private boolean getBoolean(Attributes atts, String name, boolean def) {
            String val = atts.getValue(name);
            return val == null ? def : Boolean.parseBoolean(val);
        }
    }
}
