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

import java.awt.Point;
import java.awt.event.MouseEvent;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import org.openstreetmap.josmng.osm.Node;
import org.openstreetmap.josmng.osm.OsmPrimitive;
import org.openstreetmap.josmng.osm.Way;
import org.openstreetmap.josmng.view.EditMode;
import org.openstreetmap.josmng.view.MapView;

/**
 *
 * @author nenik
 */
public class SelectMode extends EditMode {
    Point pressPoint;
    boolean pressed;
    Collection<Node> dragged;
    Object moveToken;

    public SelectMode(MapView view) {
        super("Select", view);
    }

    protected @Override void entered() {}

    protected @Override void exited() {}

    private void moveNodeTo(final Point p) {
        getData().atomicEdit(new Runnable() { public void run() {
            for (Node n : dragged) {
                n.setCoordinate(moveOnScreen(n, pressPoint, p));
            }
        }}, moveToken);
        pressPoint = p;
    }
    
    public @Override void mousePressed(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1) return;
        
        pressPoint = e.getPoint();
        OsmPrimitive prim = getNearestPrimitive(pressPoint, null);
        
        if (prim == null) return;
        
        if (prim instanceof Node) {
            dragged = Collections.singleton((Node)prim);
        } else {
            // drag each node only once even when it is present several times on a way
            dragged = new HashSet(((Way)prim).getNodes());
        }
        moveToken = new Object();
    }
    
    public @Override void mouseReleased(MouseEvent e) {
        if (dragged != null) moveNodeTo(e.getPoint());
        pressPoint = null;
        dragged = null;
        moveToken = null;
    }

    public @Override void mouseDragged(MouseEvent e) {
        if (dragged == null) return; // or do panning
        moveNodeTo(e.getPoint());
    }

    // can highlight potential targed
    // public void mouseMoved(MouseEvent e) {}

}
