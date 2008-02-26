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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JComponent;

import org.openstreetmap.josmng.osm.Coordinate;
import org.openstreetmap.josmng.osm.CoordinateImpl;
import org.openstreetmap.josmng.osm.Node;
import org.openstreetmap.josmng.ui.mode.SelectMode;

/**
 *
 * @author nenik
 */
public class MapView extends JComponent {
    private int factor = 260;
    // Loc: 50°8'8.637"N, 14°9'54.05" - Hrebec
    Projection proj = Projection.MERCATOR; //EPSG4326;
    
    private ViewCoords center = proj.coordToView(new CoordinateImpl(50.135732, 14.165013));
    
    private EditMode currentMode;
    
    private Collection<Layer> layers = new ArrayList<Layer>();
    private EditableLayer editLayer;
    
    
    public static final String PROP_LAYER = "layer";
    
    public MapView() {
        new Navigator();

        Meter meter = new Meter();
        add(meter);
        meter.setBounds(5,25, 101, 101);

        setEditMode(new SelectMode(this));
    }

    public void setEditMode(EditMode mode) {
        if (currentMode != mode) {
            if (currentMode != null) currentMode.exit();
            currentMode = mode;
            mode.enter();
        }
    }
    
    public void addLayer(Layer layer) {
        layers.add(layer);
        if (layer instanceof EditableLayer) {
            Layer old = editLayer;
            editLayer = (EditableLayer) layer;
            firePropertyChange(PROP_LAYER, old, editLayer);
        }
        repaint();
    }
    
    public EditableLayer getCurrentEditLayer() {
        return editLayer;
    }
    
    public ViewCoords getCenter() {
        return center;
    }
    
    public Projection getProjection() {
        return proj;
    }
    
    public Point getPoint(ViewCoords c) {
        int x = getWidth()/2 + (c.getIntLon() - center.getIntLon()) / factor;
        int y = getHeight()/2 - (c.getIntLat() - center.getIntLat()) / factor;
        return new Point(x, y);
    }

    public ViewCoords getPoint(Point p) {
        Point view = screenToView(p);
        return new ViewCoords(view.x, view.y);
    }

    /**
     * Return the nearest point to the screen point given.
     * If a node within 10 pixel is found, the nearest node is returned.
     */
    public final Node getNearestNode(Point p) {
        if (editLayer == null) return null;

        return editLayer.getNearestNode(p);
    }
    
    void setCenter(ViewCoords newCenter) {
        center = newCenter;
        repaint();
    }
   
    public int getScaleFactor() {
        return factor;
    }
    
    void setScaleFactor(int factor) {
        this.factor = factor;
        repaint();
    }

    
    /**
     * Convert a point in screen-relative coordinates to
     * view-space coordinates.
     * 
     * @param p a point in screen-space coordinates, that is,
     * based on current zoom factor and center point
     * @return point representing the same placein absolute, view-space
     * coordinates.
     */
    private Point screenToView(Point p) {
        int x = factor * (p.x - getWidth()/2) + center.getIntLon();
        int y = factor * (getHeight()/2 - p.y) + center.getIntLat();
        return new Point(x,y);
    }
    
    /**
     * Convert a rectangle in screen-relative coordinates to
     * view-space coordinates.
     * 
     * @param r a rectangle in screen-space coordinates, that is,
     * based on current zoom factor and center point
     * @return rectangle representing the same area in absolute, view-space
     * coordinates.
     */
    public Rectangle screenToView(Rectangle r) {
        Rectangle view = new Rectangle(screenToView(r.getLocation()));
        Point br = new Point(r.x + r.width, r.y + r.height);
        view.add(screenToView(br));
        return view;
    }

    public @Override void paint(Graphics g) {
        paintBackground(g.create());
        for(Layer layer : layers) {
            layer.paint(g.create());
        }

        super.paint(g);
    }    

    private void paintBackground(Graphics g) {
        g.setColor(Color.BLACK);
        Rectangle paint = g.getClipBounds();
        g.fillRect(paint.x, paint.y, paint.width, paint.height);
    }

    private class Navigator extends MouseAdapter implements MouseMotionListener, MouseWheelListener {
        private boolean drag;
        private ViewCoords origin;

        Navigator() {
            addMouseListener(this);
            addMouseMotionListener(this);
            addMouseWheelListener(this);
        }

        
        public void mouseDragged(MouseEvent e) {
            if ((e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) != 0) {
                if (!drag) {
                    drag = true;
                    origin = getPoint(e.getPoint());
                } else {
                    // do the move
                    ViewCoords center = getCenter();
                    ViewCoords mouseCenter = getPoint(e.getPoint());
                    setCenter(center.movedByDelta(origin, mouseCenter));
                }
            } else {
                // cancel movement
            }
	}

        public @Override void mouseReleased(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON3) {
                drag = false;
            }
	}

	/**
	 * Zoom the map by 1/5th of current zoom per wheel-delta.
	 * @param e The wheel event.
	 */
	public void mouseWheelMoved(MouseWheelEvent e) {
            // remember where the cursor was
            ViewCoords cursor = getPoint(e.getPoint());
            
            // perform zoom, view center is invariant
            int fact = getScaleFactor();
            int clicks = e.getWheelRotation();
            fact = Math.max(6, (int)(Math.pow(1.2, clicks) * fact));
            setScaleFactor(fact);

            // sample where is the cursor now
            ViewCoords cursor2 = getPoint(e.getPoint());

            //adjust the center to make cursor the invariant            
            ViewCoords center = getCenter();
            setCenter(center.movedByDelta(cursor, cursor2));
        }

	public void mouseMoved(MouseEvent e) {}
    }
    
    private class Meter extends JComponent {
        
        public @Override Dimension getPreferredSize() {
            return new Dimension(101, 101);
        }

        // in m
        private double dist(Coordinate c1, Coordinate c2) {
            double a1 = Math.PI * c1.getLatitude() / 180;
            double b1 = Math.PI * c1.getLongitude() / 180;
            double a2 = Math.PI * c2.getLatitude() / 180;
            double b2 = Math.PI * c2.getLongitude() / 180;
            return Math.acos(Math.cos(a1)*Math.cos(b1)*Math.cos(a2)*Math.cos(b2)
                    + Math.cos(a1)*Math.sin(b1)*Math.cos(a2)*Math.sin(b2)
                    + Math.sin(a1)*Math.sin(a2)) * 6378000;
        }
        
        public @Override void paint(Graphics g) {
            Coordinate ltc = proj.viewToCoord(getPoint(new Point(0,0)));
            Coordinate lbc = proj.viewToCoord(getPoint(new Point(0,100)));
            Coordinate rtc = proj.viewToCoord(getPoint(new Point(100,0)));
            
            double dist_h = dist(ltc, rtc);
            double dist_v = dist(ltc, lbc);
            
            g.setColor(Color.ORANGE);
            g.drawLine(0, 0, 100, 0);
            g.drawLine(100, 0, 100, 5);
            g.drawString(format(METERS, dist_h, dist_h/1000), 30, 14);

            if (dist_v/dist_h > 1.01) {
                g.drawLine(0, 0, 0, 100);
                g.drawLine(0, 100, 5, 100);
                g.drawString(format(METERS, dist_v, dist_v/1000), 2, 60);
            } else {
                g.drawLine(0, 0, 0, 5);
            }
        }
    }

    private MessageFormat METERS = new MessageFormat("{0,choice,0#{0,number,integer}m|1000<{1,number,0.00}km|10000<{1,number,0.0}km|100000<{1,number,integer}km}");
    
    private static String format(MessageFormat format, Object ... args) {
        return format.format(args, new StringBuffer(), null).toString();
    }
}
