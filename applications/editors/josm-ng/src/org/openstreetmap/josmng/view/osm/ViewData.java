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

import java.awt.Point;
import java.awt.Rectangle;
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
    DataSet source;
    OsmLayer view;
    Projection proj = Projection.MERCATOR;
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
        source.addDataSetListener(l);
        for (Node n : source.getNodes()) addNode(n);
        
        for (Way w : source.getWays()) addWay(w);
    }

    private void addNode(Node n) {
        ViewNode vn = new ViewNode(n, proj.coordToView(n));
        nodes.add(vn);
        nodesId.add(vn);
        if (vn.isTagged()) taggedNodes.add(vn);
    }

    private ViewNode getViewForNode(Node n) {
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
        Rectangle r = null;
        for (int i=0; i<wvNodes.length; i++) {
            Node n = wNodes.get(i);
            ViewNode vn = getViewForNode(n);
            Point p = new Point(vn.getIntLon(), vn.getIntLat());
            if (r == null) {
                r = new Rectangle(p);
            } else {
                r.add(p);
            }
            wvNodes[i] = vn;
        }
        ViewWay ww = new ViewWay(way, r, wvNodes);
        ways.add(ww);
    }

    private Rectangle getBBox(Way w) {
        Rectangle r = null;
        for (Node n : w.getNodes()) {
            ViewNode vn = getViewForNode(n);
            Point p = new Point(vn.getIntLon(), vn.getIntLat());
            if (r == null) {
                r = new Rectangle(p);
            } else {
                r.add(p);
            }
        }
        return r;
    }

    private ViewCoords getTopLeft(Rectangle viewSpace) {
        return new ViewCoords(viewSpace.x, viewSpace.y);
    }

    private ViewCoords getBottomRight(Rectangle viewSpace) {
        return new ViewCoords(viewSpace.x+viewSpace.width, viewSpace.y+viewSpace.height);
    }

    /**
     * Get nodes inside given rectangle
     * @param hint
     * @return a collection of at least all nodes inside given rectangle.
     */
    public Collection<? extends ViewCoords> getTaggedNodes(Rectangle viewSpaceHint) {
        // XXX filtering
        return taggedNodes;
    }
    /**
     * Get nodes inside given rectangle
     * @param hint
     * @return a collection of at least all nodes inside given rectangle.
     */
    public Collection<? extends ViewCoords> getNodes(Rectangle viewSpaceHint) {
        Collection<ViewCoords> nd = nodes.subSet(getTopLeft(viewSpaceHint),
                getBottomRight(viewSpaceHint));
        return nd;
    }
    
    public Collection<? extends View> getViews(Rectangle viewSpaceHint, int zoom) {
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

    private void updateNodeAndWays(Node n) {
        ViewNode vn = getViewForNode(n);

        // update the coords index
        nodes.remove(vn);
        vn.updatePosition(proj.coordToView(n));
        nodes.add(vn);
        
        for (ViewWay w : ways) {
            // should use more effective update, but as long as the dataset
            // fits in memory, this is fast enough - few ms for hundreds
            // of thousands of nodes
            for (ViewNode t : w.nodes) {
                if (t == vn) {
                    w.bbox = getBBox(w.getPrimitive());
                }
            }
        }
        fireChange();
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

    
    private class Listener implements DataSetListener {

        public void primtivesAdded(Collection<? extends OsmPrimitive> added) {
            for (OsmPrimitive prim : added) {
                if (prim instanceof Node) {
                    addNode((Node)prim);
                } else if (prim instanceof Way) {
                    addWay((Way)prim);
                } else {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            }
        }

        public void primtivesRemoved(Collection<? extends OsmPrimitive> removed) {
            for (OsmPrimitive prim : removed) {
                if (prim instanceof Node) {
                    nodes.remove(getViewForNode((Node)prim));
                } else if (prim instanceof Way) {
                    ways.remove(getViewForWay((Way)prim));
                } else {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            }
        }

        public void tagsChanged(OsmPrimitive prim) {
            View view = getViewForPrimitive(prim);
            if (view != null) view.resetStyle();
        }

        public void nodeMoved(Node node) {
            updateNodeAndWays(node);
        }

        public void wayNodesChanged(Way way) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        
    }
}
