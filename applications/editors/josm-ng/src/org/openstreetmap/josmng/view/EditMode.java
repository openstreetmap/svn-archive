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

package org.openstreetmap.josmng.view;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.AbstractAction;
import org.openstreetmap.josmng.osm.Coordinate;
import org.openstreetmap.josmng.osm.DataSet;
import org.openstreetmap.josmng.osm.OsmPrimitive;
import org.openstreetmap.josmng.view.osm.OsmLayer;

/**
 * An abstracion of an editation mode.
 * 
 * @author nenik
 */
public abstract class EditMode extends AbstractAction implements MouseListener, MouseMotionListener {

    private final Listener listener = new Listener();
    
    protected final MapView mapView;
    private OsmLayer layer;
    private Projection projection;
    
    public EditMode(String name, MapView view) {
        super(name);
        mapView = view;
    }

    public final void actionPerformed(ActionEvent e) {
        mapView.setEditMode(this);
    }

    public final void enter() {
        mapView.addMouseListener(listener);
        mapView.addMouseMotionListener(listener);
        update();
        entered();
    }
    
    public final void exit() {
        exited();
        mapView.removeMouseMotionListener(listener);
        mapView.removeMouseListener(listener);
    }

    protected abstract void exited();
    protected abstract void entered();

    protected MapView getMapView() {
        return mapView;
    }
    
    private boolean update() {
        EditableLayer edit = mapView.getCurrentEditLayer();
        if (edit instanceof OsmLayer) {
            layer = (OsmLayer)edit;
            projection = mapView.getProjection();
            return true;
        }
        return false;
    }
    
    protected final EditableLayer getLayer() {
        return layer;
    }
    
    protected final DataSet getData() {
        return layer.getDataSet();
    }
    
    protected final OsmPrimitive getNearestPrimitive(Point at, int[] idx) {
        return layer.getNearestPrimitive(at, idx);
    }
    
    protected final ViewCoords pointToView(Point p) {
        return mapView.getPoint(p);
    }
    
    protected final ViewCoords coordToView(Coordinate c) {
        return projection.coordToView(c);
    }
    
    protected final Coordinate viewToCoord(ViewCoords vc) {
        return projection.viewToCoord(vc);
    }
    
    protected final Coordinate moveOnScreen(Coordinate c, Point from, Point to) {
        ViewCoords start = pointToView(from);
        ViewCoords end = pointToView(to);
        return viewToCoord(coordToView(c).movedByDelta(pointToView(to), pointToView(from)));
    }

    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseDragged(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}

    private class Listener implements MouseListener, MouseMotionListener {
        public void mouseClicked(MouseEvent e) {
            if (update()) EditMode.this.mouseClicked(e);
        }
        
        public void mouseEntered(MouseEvent e) {
            if (update()) EditMode.this.mouseEntered(e);
        }
        
        public void mouseExited(MouseEvent e) {
            if (update()) EditMode.this.mouseExited(e);
        }
        
        public void mousePressed(MouseEvent e) {
            if (update()) EditMode.this.mousePressed(e);
        }
        
        public void mouseReleased(MouseEvent e) {
            if (update()) EditMode.this.mouseReleased(e);
        }
        public void mouseDragged(MouseEvent e) {
            if (update()) EditMode.this.mouseDragged(e);
        }
        public void mouseMoved(MouseEvent e) {
            if (update()) EditMode.this.mouseMoved(e);
        }
    }
}
