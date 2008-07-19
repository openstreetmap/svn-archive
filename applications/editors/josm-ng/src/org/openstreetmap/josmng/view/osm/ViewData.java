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

package org.openstreetmap.josmng.view.osm;

import java.util.*;

import org.openstreetmap.josmng.view.*;
import org.openstreetmap.josmng.osm.*;
import org.openstreetmap.josmng.utils.Hash;
import org.openstreetmap.josmng.utils.Storage;

/**
 * A cache for effective implementation of the view component of MVC.
 * 
 * @author nenik
 */
class ViewData {
    private final DataSet source;
    OsmLayer view;
    Projection projCache;
    final Listener l = new Listener();
    
    private static final Hash<View,View> idHash = new Hash<View,View>() {
        public int getHashCode(View v) {return v.getPrimitive().hashCode();}
        public boolean equals(View k, View t) {return k.getPrimitive() == t.getPrimitive();}
    };
    
    private static final Hash<OsmPrimitive,View> foreignHash = new Hash<OsmPrimitive,View>() {
        public int getHashCode(OsmPrimitive k) { return k.hashCode();}
        public boolean equals(OsmPrimitive k, View t) {return k == t.getPrimitive();}
    };
    
    private Storage<View> viewsId;
    private Map<OsmPrimitive,View> primToView;


    public ViewData(OsmLayer view, DataSet source) {
        this.view = view;
        this.source = source;
        viewsId = new Storage<View>(idHash, 1000000);
        primToView = viewsId.foreignKey(foreignHash);
        source.addDataSetListener(l);
        recreate();
    }

    void checkProjection() {
        if (projCache != view.parent.getProjection()) recreate();
    }
    
    private void recreate() {
        qCreate();
        viewsId.clear();
        projCache = view.parent.getProjection();
        for (OsmPrimitive p : source.getPrimitives(Bounds.WORLD)) if (valid(p)) add(p);
        qTrim();
    }

    private int[] sizes = new int[] {100000, 40000, 4000, -1};
    private QTree views[] = new QTree[sizes.length];
    
    private void qCreate() {
        for (int i = 0; i < views.length; i++) views[i] = new QTree();
    }

    private void qTrim() {
        for (QTree tree : views) tree.trim();
    }
    

    private void qAdd(View v) {
        int scale = v.getMaxScale();
        for (int i = 0; i<sizes.length; i++) {
            if (scale > sizes[i]) {
                views[i].add(v);
                break;
            }
        }
    }

    private Collection<View> qGet(BBox bbox, int zoom) {
        Set<View> ret = new HashSet<View>();
        for (int i = 0; i<sizes.length; i++) {
            views[i].gather(ret, bbox);
            if (zoom > sizes[i]) break;
        }
        return ret;
    }
    
    private void qRemove(View v) {
        for (QTree tree : views) tree.remove(v);
    }

    private void qUpdate(View v, boolean members) {
        qRemove(v);
        v.update(this, members);
        qAdd(v);
    }

    private static boolean valid(OsmPrimitive prim) {
        return !prim.isDeleted() && prim.isVisible();
    }

    <T extends OsmPrimitive> View<T> add(T prim) {
        View v = primToView.get(prim);
        if (v == null) {
            v = new Convertor().convert(prim);

            viewsId.add(v);
            qAdd(v);
        }
        return v;
    }

    <T extends OsmPrimitive> View<T> getViewForPrimitive(T prim) {
        return primToView.get(prim);
    }
    
    public Collection<? extends View> getViews(BBox viewSpaceHint, int zoom) {
        update(); // reindex moved nodes and affected ways

        Collection<View> guess = qGet(viewSpaceHint, zoom);
        for (Iterator<View> it = guess.iterator(); it.hasNext();) {
            if (!it.next().intersects(viewSpaceHint)) it.remove();
        }
        return guess;
    }

    private void fireChange() {
        view.callRepaint();
    }

    private QueueVisitor toUpdate = new QueueVisitor();

    // delayed update after node positional changes
    // it is delayed to speed up way movement, where many subsequent events
    // would affect a single way
    private void update() {
        for (OsmPrimitive prim : toUpdate) {
            // while processing the direct affectees, keep collecting the deps.
            toUpdate.visitCollection(prim.getReferrers());
            View v = add(prim);
            qUpdate(v, false);
        }
    }

    private class Listener implements DataSetListener {

        public void primtivesAdded(Collection<? extends OsmPrimitive> added) {
            for (OsmPrimitive p : added) if (valid(p)) add(p);
            fireChange();
        }

        public void primtivesRemoved(Collection<? extends OsmPrimitive> removed) {
            final Set<OsmPrimitive> sel = view.getSelection();
            for (OsmPrimitive rem : removed) {
                sel.remove(rem);
                View v = getViewForPrimitive(rem);
                if (v != null) {
                    qRemove(v);
                    viewsId.remove(v);
                }
            }

            view.setSelection(sel);
            fireChange();
        }

        public void tagsChanged(OsmPrimitive prim) {
            // tag change might influence the detail level
            View view = getViewForPrimitive(prim);
            if (view != null) {
                int maxScale = view.getMaxScale();
                view.resetStyle();
                if (maxScale != view.getMaxScale()) {
                    qRemove(view);
                    qAdd(view);
                }
            }
            fireChange();
        }

        public void nodeMoved(Node node) {
            toUpdate.visit(node);
            fireChange();
        }

        public void wayNodesChanged(Way way) {
            qUpdate(getViewForPrimitive(way), true);
            fireChange();
        }

        public void relationMembersChanged(Relation r) {
            qUpdate(getViewForPrimitive(r), true); // with nodes
            fireChange();
        }
    }

    
    
    private class Convertor extends Visitor {
        View conv;

        protected @Override void visit(Node n) {
            conv = new ViewNode(n, ViewData.this);
        }

        protected @Override void visit(Way w) {
            conv = new ViewWay(w, ViewData.this);
        }

        protected @Override void visit(Relation r) {
            conv = new ViewRelation(r, ViewData.this);
        }

        public View convert(OsmPrimitive prim) {
            prim.visit(this);
            return conv;
        }
    }
    
    /**
     * A visitor that puts all the visited elements to a queue,
     * which can be concurrently iterated.
     * 
     * When iterated, all available Nodes are processed first, then ways,
     * relations last.
     */
    private class QueueVisitor extends Visitor implements Iterable<OsmPrimitive>, Iterator<OsmPrimitive> {
        private Set<OsmPrimitive> inQueue = new HashSet<OsmPrimitive>();
        private Queue<OsmPrimitive> nodes = new LinkedList<OsmPrimitive>();
        private Queue<OsmPrimitive> ways = new LinkedList<OsmPrimitive>();
        private Queue<OsmPrimitive> relations = new LinkedList<OsmPrimitive>();

        public Iterator<OsmPrimitive> iterator() {
            return this;
        }

        public boolean hasNext() {
            return !nodes.isEmpty() || !ways.isEmpty() || !relations.isEmpty();
        }

        public OsmPrimitive next() {
            OsmPrimitive ret = (nodes.isEmpty() ? ways.isEmpty() ?
                    relations : ways : nodes).remove(); // or throw NSEE
            inQueue.remove(ret);
            return ret;
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected @Override void visit(Node n) {
            if (inQueue.add(n)) nodes.add(n);
        }

        protected @Override void visit(Way w) {
            if (inQueue.add(w)) ways.add(w);
        }

        protected @Override void visit(Relation r) {
            if (inQueue.add(r)) relations.add(r);
        }
    }
}
