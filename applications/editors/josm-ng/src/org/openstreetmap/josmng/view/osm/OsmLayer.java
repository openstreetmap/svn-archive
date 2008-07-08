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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;

import java.util.HashSet;
import java.util.Set;
import org.openstreetmap.josmng.osm.Bounds;
import org.openstreetmap.josmng.view.*;
import org.openstreetmap.josmng.osm.DataSet;
import org.openstreetmap.josmng.osm.Node;
import org.openstreetmap.josmng.osm.OsmPrimitive;
import org.openstreetmap.josmng.osm.Way;
import org.openstreetmap.josmng.utils.UndoHelper;

/**
 *
 * @author nenik
 */
public class OsmLayer extends EditableLayer {
    private String name;
    private DataSet data;
    private UndoHelper undo = new UndoHelper();
    ViewData mapData;

    public OsmLayer(MapView parent, String name, DataSet data) {
        super(parent);
        this.name = name;
        this.data = data;
        data.addUndoableEditListener(undo);
        mapData = new ViewData(this, data);
    }

    public @Override UndoHelper getUndoManager() {
        return undo;
    }

    public @Override boolean isEmpty() {
        return !data.getPrimitives(Bounds.WORLD).iterator().hasNext();
    }
    
    void callRepaint() {
        parent.repaint();
    }

    public @Override void paint(Graphics g) {
        mapData.checkProjection();
        BBox viewBox = parent.screenToView(g.getClipBounds());
        Collection<OsmPrimitive> sel = getSelection();

        Drawer drawer = new Drawer();
        for (View v : mapData.getViews(viewBox, parent.getScaleFactor())) {
              v.collect(drawer, parent, sel.contains(v.getPrimitive()));
        }

        drawer.draw((Graphics2D)g);
    }

    @Override
    public String getName() {
        return name;
    }

    public Collection<OsmPrimitive> getPrimitivesInRect(Rectangle r, boolean contained) {
        Set<OsmPrimitive> s = new HashSet();
        BBox viewR = parent.screenToView(r);
        Rectangle r2 = viewR.toRectangle();

        Collection<? extends View> near = mapData.getViews(viewR, 1);
        for (View v : near) {
            OsmPrimitive prim = v.getPrimitive();
            
            if (prim instanceof Node) { // should be in, but double check
                Node n = (Node)prim;
                ViewCoords vc = parent.getProjection().coordToView(n); // XXX can do better
                Point onScreen = parent.getPoint(vc);
                if (r.contains(onScreen)) s.add(prim);
            } else if (prim instanceof Way) {
                ViewWay vw = (ViewWay)v;
                if (contained) {
                    if (viewR.contains(vw.bbox)) s.add(prim);
                } else { // at least touch a segment
                    ViewNode last = null;
                    for (ViewNode act : vw.getNodes()) {
                        if (last != null) {
                            // XXX intersectsLine algorithm missing in bbox yet
                            if (r2.intersectsLine(last.getIntLon(), last.getIntLat(), act.getIntLon(), act.getIntLat())) {
                                s.add(prim);
                                break;
                            }
                        }
                        last = act;
                    }
                    
                }
            }
        }
        
        return s;
    }
    
    /*
     * dot-product of vectors (ax) and (xc)
     */ 
    private int dotProduct(Point a, Point x, Point c) {
        return (x.x - a.x)*(c.x - x.x) + (x.y - a.y)*(c.y - x.y);
    }

    /*
     * cross-product of vectors (ab) and (bc)
     */ 
    private int crossProduct(Point a, Point b, Point c) {
        return (b.x - a.x)*(c.y - a.y) - (b.y - a.y)*(c.x - a.x);
    }

    private double pointSegmentDistanceSq(Point l1, Point l2, Point p) {
        if (dotProduct(l1, l2, p) > 0) return l2.distanceSq(p);
        if (dotProduct(l2, l1, p) > 0) return l1.distanceSq(p);
        double tmp = crossProduct(l1, l2, p) / l1.distance(l2);
        return tmp*tmp;
    }

    /** Finds the primitive nearest the given point, up to 10px far.
     * The algorithm strictly prefers Nodes over Ways as it would be very hard
     * to pick a node on a stright line segment otherwise.
     */ 
    public OsmPrimitive getNearestPrimitive(Point p, int[] idx) {
        Rectangle r = new Rectangle(p);
        r.grow(10, 10);
        BBox viewR = parent.screenToView(r);
        
        double minDistanceSq = 100;
        OsmPrimitive minPrimitive = null;


        Collection<? extends View> near = mapData.getViews(viewR, 1);

        for (View v : near) {
            OsmPrimitive prim = v.getPrimitive();
            if (prim.isDeleted() || prim.isIncomplete()) continue;
            
            if (v instanceof ViewNode) {            
                Point sp = parent.getPoint((ViewNode)v);
                double dist = p.distanceSq(sp);
                if (dist < minDistanceSq || (minPrimitive instanceof Way && dist < 100)) {
                    minDistanceSq = dist;
                    minPrimitive = prim;
                }
            } else if (!(minPrimitive instanceof Node)) {
                ViewWay vw = (ViewWay)v;
                
                for (int i=0; i<vw.nodes.length-1; i++) {
                    // this is completly wrong!
                    Point A = parent.getPoint(vw.nodes[i]);
                    Point B = parent.getPoint(vw.nodes[i+1]);
                    double dist = pointSegmentDistanceSq(A, B, p);
                    if (dist < minDistanceSq) {
                        minDistanceSq = dist;
                        if (idx != null) idx[0] = i;
                        minPrimitive = prim;
                    }
                }
            }
        }
        return minPrimitive;
    }

    public DataSet getDataSet() {
        return data;
    }
}
