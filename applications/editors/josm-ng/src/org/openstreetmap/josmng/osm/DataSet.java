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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.swing.event.EventListenerList;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.openstreetmap.josmng.utils.Storage;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

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
        
    public Way createWay(Node ... nodes) {
        Way way = new Way(this, 0, -1, null, true, nodes);
        addRemovePrimitive(way, true);
        return way;
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
        Throwable cause;
        try {
            OsmStreamReader osr = new OsmStreamReader();
            InputSource src = new InputSource(new BufferedInputStream(is));
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            
            parser.parse(src, osr);
            return osr.factory.create();
        } catch (ParserConfigurationException ex) {
            cause = ex;
        } catch (SAXException ex) {
            cause = ex;
        }
        IOException ioe = new IOException("Can't read the source");
        ioe.initCause(cause);
        throw ioe;
        
    }
    
    public static final class Factory {
        private DataSet ds;

        private Factory(int capa) {
            ds = new DataSetMemoryImpl(capa);
        }

        public DataSet create() {
            DataSet ret = ds;
            ds = null; // disable further modifications
            return ret;
        }
        public Node getNode(long id) {
            return ds.getNode(id);
        }
                
        public Node node(long id, double lat, double lon, int time, String user, boolean visible) {
            ds.getClass(); // null check
            Node n = new Node(ds, id, lat, lon, time, user, visible);
            ds.addPrimitive(n);
            return n;
        }

        public Way getWay(long id) {
            return ds.getWay(id);
        }

        public Way way(long id, int time, String user, boolean visible, Node[] nodes) {
            ds.getClass(); // null check
            Way w = new Way(ds, id, time, user, visible, nodes); // XXX vis
            ds.addPrimitive(w);
            return w;
        }
        
        public void setTags(OsmPrimitive prim, String[] pairs) {
            ds.getClass(); // null check
            prim.setTags(pairs);
        }
    }
    
    private static class OsmStreamReader extends DefaultHandler {
        private OsmPrimitive current;
        private DataSet.Factory factory = DataSet.factory(1000);
        private List<Node> wayNodes = new ArrayList<Node>();
    
        private Storage<String> strings = new Storage<String>();
        private Map<Long,Node> newNodes = new HashMap<Long, Node>();
        private Map<Long,Way> newWays = new HashMap<Long, Way>();
        private Map<Long,Relation> newRels = new HashMap<Long, Relation>();
        
    
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
                int time = getDate(atts, "timestamp");
                String user = atts.getValue("user");
                boolean vis = getBoolean(atts, "visible", true);

                double lat = getDouble(atts, "lat");
                double lon = getDouble(atts, "lon");
                
                Node n = factory.node(id < 0 ? 0 : id, lat, lon, time, user, vis);
                if (id < 0) newNodes.put(id, n);

                updateFlags(n, getString(atts, "action"));
                current = n;
            } else if (qName.equals("way")) {
                // common attribs
                long id = getLong(atts, "id");
                int time = getDate(atts, "timestamp");
                String user = atts.getValue("user");
                boolean vis = getBoolean(atts, "visible", true);

                Way w = factory.way(id < 0 ? 0 : id, time, user, vis, null);
                if (id < 0) newWays.put(id, w);

                updateFlags(w, getString(atts, "action"));
                current = w;
            } else if (qName.equals("nd")) {
                assert current instanceof Way;
                long nid = getLong(atts, "ref");
                Node n = getNode(nid);
                //assert n != null;
                if (n != null) wayNodes.add(n);
            } else if (qName.equals("tag")) {
    //            assert current != null;
                if (current != null) current.putTagImpl(getString(atts, "k"), getString(atts, "v"));
            } else if (qName.equals("relation")) {
                // TODO: relation parsing 
            } else if (qName.equals("member")) {
                // TODO
            }
        }

        private void updateFlags(OsmPrimitive prim, String action) {
            if ("delete".equals(action)) {
                prim.setDeletedImpl(true);
            } else if ("modify".equals(action)) {
                prim.setModified(true);
            }
        }
        
        private Node getNode(long id) {
            if (id < 0) {
                return newNodes.get(id);
            } else {
                return factory.getNode(id);
            }
        }
        
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals("tag")) return;
            if (qName.equals("way")) {
                assert current instanceof Way;
                ((Way)current).setNodesImpl(wayNodes.toArray(new Node[wayNodes.size()]));
                wayNodes.clear();
            }
            if (qName.equals("node") || qName.equals("way") || qName.equals("relation")) {
                current = null;
            }
        }
    
        private int getDate(Attributes atts, String name) {
            String orig = atts.getValue(name);
            if (orig == null) return -1;
            return (int)getTimestamp(orig).getTime()/1000;
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

        // An instance reused throughout the lifetime of the parser.
        private GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        { calendar.setTimeInMillis(0);}

        private Date getTimestamp(String str) {
            // "2007-07-25T09:26:24{Z|{+|-}01:00}"
            if (checkLayout(str, "xxxx-xx-xxTxx:xx:xxZ") ||
                    checkLayout(str, "xxxx-xx-xxTxx:xx:xx+xx:00") ||
                    checkLayout(str, "xxxx-xx-xxTxx:xx:xx-xx:00")) {
                calendar.set(
                    parsePart(str, 0, 4),
                    parsePart(str, 5, 2)-1,
                    parsePart(str, 8, 2),
                    parsePart(str, 11, 2),
                    parsePart(str, 14,2),
                    parsePart(str, 17, 2));
                
                if (str.length() == 25) {
                    int plusHr = parsePart(str, 20, 2);
                    int mul = str.charAt(19) == '+' ? -3600000 : 3600000;
                    calendar.setTimeInMillis(calendar.getTimeInMillis()+plusHr*mul);
                }
                
                return calendar.getTime();
            }
            
            try {
                return XML_DATE.newXMLGregorianCalendar((String)str).toGregorianCalendar().getTime();
            } catch (Exception ex) {
                return new Date();
            }
        }
        
        private boolean checkLayout(String text, String pattern) {
            if (text.length() != pattern.length()) return false;
            for (int i=0; i<pattern.length(); i++) {
                if (pattern.charAt(i) == 'x') continue;
                if (pattern.charAt(i) != text.charAt(i)) return false;
            }
            return true;
        }

    }
    
    private static final DatatypeFactory XML_DATE;

    static {
        DatatypeFactory fact = null;
        try {
            fact = DatatypeFactory.newInstance();
        } catch(DatatypeConfigurationException ce) {
            ce.printStackTrace();
        }
        XML_DATE = fact;
    }


    private static int parsePart(String str, int off, int len) {
        return Integer.valueOf(str.substring(off, off+len));
    }

}
