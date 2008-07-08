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
    Listener l = new Listener();
    
    private Hash<ViewNode, ViewNode> hash = new Hash<ViewNode, ViewNode>() {
        public int getHashCode(ViewNode k) {
            return k.getPrimitive().hashCode();
        }

        public boolean equals(ViewNode k, ViewNode t) {
            return k.getPrimitive().equals(t.getPrimitive());
        }
    };
    
    private SortedSet<ViewCoords> nodes = new TreeSet<ViewCoords>(new ViewCoordsComparator());
    private Storage<ViewNode> taggedNodes = new Storage<ViewNode>(hash);
    private Storage<ViewNode> nodesId = new Storage<ViewNode>(hash);
    
    private Map<Node,ViewNode> nodeToView = nodesId.foreignKey(new Hash<Node,ViewNode>() {

        public int getHashCode(Node k) {
            return k.hashCode();
        }

        public boolean equals(Node k, ViewNode t) {
            return k.equals(t.getPrimitive());
        }
    });

    
    
    
    
    private Collection<ViewWay> ways = new ArrayList<ViewWay>();

    public ViewData(OsmLayer view, DataSet source) {
        this.view = view;
        this.source = source;
        source.addDataSetListener(l);
        recreate();
    }

    void checkProjection() {
        if (projCache != view.parent.getProjection()) recreate();
    }
    
    private void recreate() {
        nodes.clear();
        taggedNodes.clear();
        nodesId.clear();
        ways.clear();
        projCache = view.parent.getProjection();
        new AddVisitor().visitCollection(source.getPrimitives(Bounds.WORLD));
    }
    
    private static boolean valid(OsmPrimitive prim) {
        return !prim.isDeleted() && prim.isVisible();
    }

    private void addNode(Node n) {
        if (nodeToView.containsKey(n)) return;
        ViewNode vn = new ViewNode(n, projCache.coordToView(n));
        nodes.add(vn);
        nodesId.add(vn);
        if (vn.isTagged()) taggedNodes.add(vn);
    }

    private ViewNode getViewForNode(Node n) {
        addNode(n);
        return nodeToView.get(n);
    }

    private ViewWay getViewForWay(Way w) {
        // XXX
        for (ViewWay vw : ways) {
            if (vw.getPrimitive() == w) return vw;
        }
 
        return null; // should not be called this way.
    }
    
    private View getViewForPrimitive(OsmPrimitive osm) {
        return osm instanceof Node ? getViewForNode((Node)osm) :
            osm instanceof Way ? getViewForWay((Way)osm) : null;
    }
    
    private void addWay(Way way) {
        List<Node> wNodes = way.getNodes();
        ViewNode[] wvNodes = new ViewNode[wNodes.size()];
        for (int i=0; i<wvNodes.length; i++) {
            wvNodes[i] = getViewForNode(wNodes.get(i));
        }
        ViewWay ww = new ViewWay(way, getBBox(wvNodes), wvNodes);
        ways.add(ww);
    }

    private BBox getBBox(ViewNode[] vns) {
        BBox r = new BBox();
        for (ViewNode vn : vns) {
            r.addPoint(vn.getIntLon(), vn.getIntLat());
        }
        return r;
    }

    /**
     * Get nodes inside given rectangle
     * @param hint
     * @return a collection of at least all nodes inside given rectangle.
     */
    private Collection<? extends ViewCoords> getTaggedNodes(BBox viewSpaceHint) {
        // XXX filtering
        return taggedNodes;
    }
    /**
     * Get nodes inside given rectangle
     * @param hint
     * @return a collection of at least all nodes inside given rectangle.
     */
    private Collection<? extends ViewCoords> getNodes(BBox viewSpaceHint) {
        Collection<ViewCoords> nd = nodes.subSet(viewSpaceHint.getTopLeft(),
                viewSpaceHint.getBottomRight());
        return nd;
    }
    
    public Collection<? extends View> getViews(BBox viewSpaceHint, int zoom) {
        update(); // reindex moved nodes and affected ways
        Collection<View> matching = new ArrayList();
        
        for (ViewWay way : ways) {
            if (viewSpaceHint.intersects(way.bbox)) {
                matching.add(way);
            }
        }

        Collection<? extends ViewCoords> inView = zoom > 1000 ? getTaggedNodes(viewSpaceHint) : getNodes(viewSpaceHint);
        for (ViewCoords vc : inView) {
            ViewNode vn = (ViewNode)vc;
            if (viewSpaceHint.contains(vc.getIntLon(), vc.getIntLat()))
                matching.add(vn);
        }
        
        return matching;
    }

    private void fireChange() {
        view.callRepaint();
    }

    private Set<Node> toUpdate = new Storage<Node>();

    private void update() {
        if (toUpdate.isEmpty()) return;

        BBox r = new BBox();
        for (Node n : toUpdate) {
            ViewNode vn = getViewForNode(n);
            r.addPoint(vn.getIntLon(), vn.getIntLat());

            // update the coords index
            nodes.remove(vn);
            vn.updatePosition(projCache.coordToView(n));
            nodes.add(vn);
        }
        
        // should use more effective update, but as long as the dataset
        // fits in memory, this is fast enough - few ms for hundreds
        // of thousands of nodes
        for (ViewWay w : ways) {
            if (! r.intersects(w.bbox)) continue;
            for (ViewNode t : w.nodes) {
                if (toUpdate.contains(t.getPrimitive())) {
                    w.bbox = getBBox(w.nodes);
                    break;
                }
            }
        }

        toUpdate.clear();
    }
    
    private void updateWay(ViewWay w) {
        List<Node> wNodes = w.getPrimitive().getNodes();
        ViewNode[] wvNodes = new ViewNode[wNodes.size()];
        for (int i=0; i<wvNodes.length; i++) {
            wvNodes[i] = getViewForNode(wNodes.get(i));
        }
        w.nodes = wvNodes;
        w.bbox = getBBox(wvNodes);
    }

    // o1 < o2 iff compare(o1,o2) < 0
    private class ViewCoordsComparator implements Comparator<ViewCoords> {
        public int compare(ViewCoords o1, ViewCoords o2) {
            int diff = o1.getIntLon() - o2.getIntLon();
            if (diff == 0) diff = o1.getIntLat() - o2.getIntLat();
            // XXX: might overflow
            if (diff == 0) {
                if (o1 instanceof ViewNode) {
                    if (o2 instanceof ViewNode) {
                        diff = (int)(((ViewNode)o1).node.getId() - ((ViewNode)o2).node.getId());
                    } else {
                        diff = 1;
                    }
                } else {
                    if (o2 instanceof ViewNode) {
                        diff = -1;
                    } // else diff = 0;
                }
            }
            return diff;
        }
    }

    private class AddVisitor extends Visitor {
        protected @Override void visit(Node n) {
            if (valid(n)) addNode(n);
        }
        protected @Override void visit(Way w) {
            if (valid(w)) addWay(w);
        }
    }
    
    private class Listener implements DataSetListener {

        public void primtivesAdded(Collection<? extends OsmPrimitive> added) {
            new AddVisitor().visitCollection(added);
            fireChange();
        }

        public void primtivesRemoved(Collection<? extends OsmPrimitive> removed) {
            final Set<OsmPrimitive> sel = new LinkedHashSet<OsmPrimitive>(view.getSelection());
            
            new Visitor() {
                protected @Override void visit(Node n) {
                    sel.remove(n);
                    nodes.remove(getViewForNode(n));
                }
                protected @Override void visit(Way w) {
                    sel.remove(w);
                    ways.remove(getViewForWay(w));
                }
            }.visitCollection(removed);

            view.setSelection(sel);
            fireChange();
        }

        public void tagsChanged(OsmPrimitive prim) {
            View view = getViewForPrimitive(prim);
            if (view != null) view.resetStyle();
            fireChange();
        }

        public void nodeMoved(Node node) {
            toUpdate.add(node);
            fireChange();
        }

        public void wayNodesChanged(Way way) {
            updateWay(getViewForWay(way));
            fireChange();
        }
    }
}
