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

package org.openstreetmap.josmng.ui.mode;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import static java.awt.event.MouseEvent.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.openstreetmap.josmng.osm.Coordinate;
import org.openstreetmap.josmng.osm.DataSet;
import org.openstreetmap.josmng.osm.Node;
import org.openstreetmap.josmng.osm.OsmPrimitive;
import org.openstreetmap.josmng.osm.Way;
import org.openstreetmap.josmng.ui.StatusBar;
import org.openstreetmap.josmng.view.EditMode;
import org.openstreetmap.josmng.view.MapView;

/**
 * An edit mode that is enough to implement completly modeless controll
 * of the map editor.
 * 
 * @author nenik
 */
public class TheOnlyMode extends EditMode {
    private Point pressPoint;
    private Object moveToken;

    private OsmPrimitive clicked;
    
    
    private enum Mode { NONE, SELECT, MOVE, MOVED, NODE }
    private Mode mode = Mode.NONE;
    
    public TheOnlyMode(MapView view) {
        super("Select", view);
        putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/org/openstreetmap/josmng/ui/resources/select.png")));
    }

    protected @Override void entered() {
        StatusBar.getDefault().setText("Alt - create node, Shift - Toggle/block select");
    }

    protected @Override void exited() {
        StatusBar.getDefault().setText(" ");
    }

    public @Override void mousePressed(MouseEvent e) {
        moveToken = new Object();
        mode = Mode.NONE;
        clicked = null;
        
        if (e.getButton() != MouseEvent.BUTTON1) return;
        pressPoint = e.getPoint();
        if (modMask(e, SHIFT_DOWN_MASK)) {
            OsmPrimitive prim = getNearestPrimitive(pressPoint, null);
            if (prim != null) {
                mode = Mode.NONE;
                getLayer().toggleSelected(prim);
            } else {
                mode = Mode.SELECT;
            }
        } else if (modMask(e, 0)) { // no modifiers
            OsmPrimitive prim = getNearestPrimitive(pressPoint, null);
            clicked = prim;
            if (prim == null) {
                getLayer().setSelection(Collections.<OsmPrimitive>emptyList());
                mode = Mode.NONE;
            } else {
                mode = Mode.MOVE;
                if (!getLayer().getSelection().contains(prim)) 
                    getLayer().setSelection(Collections.<OsmPrimitive>singleton(prim));
            }
        } else if (modMask(e, ALT_DOWN_MASK)) {
            mode = Mode.NODE;
            final DataSet ds = getData(); ds.atomicEdit(new Runnable() { public void run() {

            int[] idx = new int[1];
            OsmPrimitive prim = getNearestPrimitive(pressPoint, idx);

            if (prim instanceof Node) {
System.err.println("Preexisting node:");
                clicked = prim;
                extendWay(ds, (Node)clicked);
                ((Node)clicked).setCoordinate((Coordinate)clicked); // post fake edit to save initial coords
            } else if (prim instanceof Way) { // add a node into a way
System.err.println("Preexisting way:");
                Coordinate c = viewToCoord(pointToView(pressPoint));
                clicked = ds.createNode(c.getLatitude(), c.getLongitude());
                insertNodeInto((Way)prim, idx[0], (Node)clicked);
                extendWay(ds, (Node)clicked);
            } else {
                assert prim == null;
System.err.println("Adding new node:");
                Coordinate c = viewToCoord(pointToView(pressPoint));
                clicked = ds.createNode(c.getLatitude(), c.getLongitude());
                extendWay(ds, (Node)clicked);
            }
            
            }}, moveToken);
        }

        mapView.repaint();
    }
    
    public @Override void mouseReleased(MouseEvent e) {
        if (mode == Mode.MOVE) {
            // clicked on a selection member but didn't move - change the selection
            if (clicked != null) getLayer().setSelection(Collections.<OsmPrimitive>singleton(clicked));
        }
        mode = Mode.NONE;
        pressPoint = null;
    }

    public @Override void mouseDragged(MouseEvent e) {
        switch (mode) {
            case SELECT:
                Rectangle current = new Rectangle(pressPoint);
                current.add(e.getPoint());
                Collection<OsmPrimitive> newSel = getLayer().getPrimitivesInRect(current, pressPoint.y < e.getPoint().y);
                getLayer().setSelection(newSel);

                drawRect(current);
                break;
                
            case MOVE:
                mode = Mode.MOVED;
                // fall through
            case MOVED:
                moveSelectionTo(e.getPoint());
                break;
                
            case NODE:
                moveNodesTo(Collections.<Node>singleton((Node)clicked), e.getPoint());
                break;
        }
    }

    private void drawRect(Rectangle rect) {
        Graphics g = mapView.getGraphics();
        g.setClip(0, 0, mapView.getWidth(), mapView.getHeight());
        mapView.paint(g);
        g.setColor(Color.BLACK);
        g.setXORMode(Color.WHITE);
        g.drawRect(rect.x, rect.y, rect.width, rect.height);
    }
    // can highlight potential targed
    // public void mouseMoved(MouseEvent e) {}

    private boolean modMask(MouseEvent ev, int mods) {
        int mask = mods | ALT_DOWN_MASK | ALT_GRAPH_DOWN_MASK | CTRL_DOWN_MASK | SHIFT_DOWN_MASK | META_DOWN_MASK;
        return (ev.getModifiersEx() & mask) == mods; 
    }
    private void moveNodesTo(final Collection<Node> nodes, final Point p) {
        getData().atomicEdit(new Runnable() { public void run() {
            for (Node n : nodes) {
                n.setCoordinate(moveOnScreen(n, pressPoint, p));
            }
        }}, moveToken);
        pressPoint = p;
    }
    
    private void moveSelectionTo(Point p) {
        Set<Node> s = new HashSet<Node>();
        for (OsmPrimitive prim : getLayer().getSelection()) {
            if (prim instanceof Node) {
                s.add((Node)prim);
            } else {
                s.addAll(((Way)prim).getNodes());
            }
        }
        moveNodesTo(s, p);
    }

    
    private Node last;

    private void doAddNode(Way way, Node n, boolean prepend) {
        List<Node> nodes = new ArrayList<Node>(way.getNodes());
        if (prepend) { // prepend
            if (nodes.size() > 1 && nodes.get(1) == n) return;
            nodes.add(0, n);
        } else {
            if (nodes.size() > 1 && nodes.get(nodes.size()-2) == n) return;
            nodes.add(n);
        }
        way.setNodes(nodes);
        last = n;
    }
    
    /**
     * Creates a way segment between last two entities, possibly updating the
     * the selection. The rules are:
     * Selected     Last        Action          Selection
     * null         null           -            the node
     * node         whatever    Way(last/n)     the way
     * way          null        extend(near)    the way
     * way          node        extend(end)     the way
     * more         whatever       -         the node
     * @param ds
     * @param n
     */
    private void extendWay(DataSet ds, Node n) {
        Collection<OsmPrimitive> sel = getLayer().getSelection();
        ExtensionTuple et = getFromSelection();
        if (et.way != null) { // selected way, extend
            if (et.node == null) et.node = closerEnd(et.way, n);
            if (et.node == n) return;
            doAddNode(et.way, n, et.way.getNodes().get(0) == et.node);
            getLayer().setSelection(Collections.<OsmPrimitive>singleton(et.way));
        } else if (et.node != null) { // selected node only, create way
            if (et.node == n) return; // don't self-append
            Way way = ds.createWay(et.node, n);
            getLayer().setSelection(Collections.<OsmPrimitive>singleton(way));
            last = n;
        } else {
            getLayer().setSelection(Collections.<OsmPrimitive>singleton(n));
        }
    }

    private static class ExtensionTuple {
        Way way;
        Node node;
    }

    private Node closerEnd(Way w, Node x) {
        List<Node> nodes = new ArrayList<Node>(w.getNodes());
        Node begin = nodes.get(0);
        Node end = nodes.get(nodes.size()-1);
        return dist(begin, x) <= dist(end, x) ? begin : end;
    }
    
    private boolean isEndpoint(Way way, Node n) {
        List<Node> nodes = way.getNodes();
        return (nodes.size() > 0 && (nodes.get(0) == n || nodes.get(nodes.size()-1) == n));
    }
            
    private ExtensionTuple getFromSelection() {
        ExtensionTuple tuple = new ExtensionTuple();
        List<OsmPrimitive> sel = new ArrayList<OsmPrimitive>(getLayer().getSelection());
        if (sel.size() == 1) {
            OsmPrimitive prim = sel.get(0);
            if (prim instanceof Way) {
                tuple.way = (Way)prim;
                if (isEndpoint(tuple.way, last)) tuple.node = last;
            } else if (prim instanceof Node) {
                tuple.node = (Node)prim;
            }
        } else if (sel.size() == 2) {
            OsmPrimitive prim1 = sel.get(0);
            OsmPrimitive prim2 = sel.get(1);
            if (prim1 instanceof Way && prim2 instanceof Node && isEndpoint((Way)prim1, (Node)prim2)) {
                tuple.way = (Way)prim1;
                tuple.node = (Node)prim2;
            } else if (prim2 instanceof Way && prim1 instanceof Node && isEndpoint((Way)prim2, (Node)prim1)) {
                tuple.way = (Way)prim2;
                tuple.node = (Node)prim1;
            }
        }
        return tuple;
    }

    private static double dist(Node n1, Node n2) {
        double dLat = n1.getLatitude() - n2.getLatitude();
        double dLon = n1.getLongitude() - n2.getLongitude();
        return dLat*dLat + dLon*dLon;
    }
    
    private void insertNodeInto(Way way, int idx, Node n) {
        List<Node> nodes = new ArrayList<Node>(way.getNodes());
        nodes.add(idx+1, n);
        way.setNodes(nodes);
    }

}
