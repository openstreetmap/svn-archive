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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;

import javax.swing.KeyStroke;
import org.openstreetmap.josmng.osm.Coordinate;
import org.openstreetmap.josmng.osm.CoordinateImpl;
import org.openstreetmap.josmng.osm.Node;

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
    
    private static final String[] KEYMAP = new String[] {
        "control UP", Navigator.UP,
        "control DOWN", Navigator.DOWN,
        "control LEFT", Navigator.LEFT,
        "control RIGHT", Navigator.RIGHT,
        "control PAGE_UP", Navigator.ZOOM_OUT,
        "control PAGE_DOWN", Navigator.ZOOM_IN
    };
    
    public MapView() {
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();
        Navigator nav = new Navigator(); // registers itself as mouse tracker too

        for (int i=0; i<KEYMAP.length; i += 2) {
            im.put(KeyStroke.getKeyStroke(KEYMAP[i]), KEYMAP[i+1]);
            am.put(KEYMAP[i+1], new ActionWrapper(KEYMAP[i+1], nav));
        }

        Meter meter = new Meter();
        add(meter);
        meter.setBounds(5,25, 101, 101);
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
            EditableLayer old = editLayer;
            editLayer = (EditableLayer) layer;
            firePropertyChange(PROP_LAYER, old, editLayer);
            if (old != null && old.isEmpty()) layers.remove(old);
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
    
    public void setProjection(Projection proj) {
        center = proj.coordToView(this.proj.viewToCoord(center));
        this.proj = proj;
        repaint();
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

    /* A horizontal scale in m/px in the middle of the canvas.
     */
    private double getMeanScaleFactor() {
        Point p1 = new Point(getWidth()/2-50, getHeight()/2);
        Point p2 = new Point(p1.x+100, p1.y);
        Coordinate left = proj.viewToCoord(getPoint(p1));
        Coordinate right = proj.viewToCoord(getPoint(p2));
        return dist(left, right)/100;
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

    private boolean inPaint = false;
    private double scaleCache;

    public int getWidthInPixels(int inMeters, int minWidth) {
        assert inPaint;
        return Math.max((int)(inMeters / scaleCache), minWidth);
    }

    public @Override void paint(Graphics g) {
        inPaint = true;
        scaleCache = getMeanScaleFactor();
        
        paintBackground(g.create());
        for(Layer layer : layers) {
            layer.paint(g.create());
        }

        super.paint(g);
        inPaint = false;
    }    

    private void paintBackground(Graphics g) {
        g.setColor(Color.WHITE);
        Rectangle paint = g.getClipBounds();
        g.fillRect(paint.x, paint.y, paint.width, paint.height);
    }

    private class Navigator extends MouseAdapter implements MouseMotionListener, MouseWheelListener, ActionListener {
        private static final String ZOOM_IN = "zoom_in";
        private static final String ZOOM_OUT = "zoom_out";
        private static final String LEFT = "left";
        private static final String RIGHT = "right";
        private static final String UP = "up";
        private static final String DOWN = "down";
        
        private boolean drag;
        private ViewCoords origin;

        Navigator() {
            addMouseListener(this);
            addMouseMotionListener(this);
            addMouseWheelListener(this);
        }

        private boolean acceptPanGesture(MouseEvent e) {
            return ((e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) != 0) ||
                 ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0);
        }

        public @Override void mousePressed(MouseEvent e) {
            if (acceptPanGesture(e)) {
                drag = true;
                origin = getPoint(e.getPoint());
            }
        }
        
        public void mouseDragged(MouseEvent e) {
            if (drag) {
                // do the move
                ViewCoords center = getCenter();
                ViewCoords mouseCenter = getPoint(e.getPoint());
                setCenter(center.movedByDelta(origin, mouseCenter));
            }
	}

        public @Override void mouseReleased(MouseEvent e) {
            drag = false;
	}

        public void mouseMoved(MouseEvent e) {}

	/**
	 * Zoom the map by 1/5th of current zoom per wheel-delta.
	 * @param e The wheel event.
	 */
	public void mouseWheelMoved(MouseWheelEvent e) {
            zoomBy(e.getWheelRotation(), e.getPoint());
        }
        
        private void zoomBy(int steps, Point invariant) {
            // center on the screen if not specified otherwise
            if (invariant == null) invariant = new Point(getWidth()/2, getHeight()/2);

            // remember the invariant position
            ViewCoords pos = getPoint(invariant);
            
            // perform zoom, view center is invariant
            int fact = getScaleFactor();
            fact = Math.max(6, (int)(Math.pow(1.2, steps) * fact));
            setScaleFactor(fact);

            // sample what location is the invariant now
            ViewCoords pos2 = getPoint(invariant);

            //adjust the center to keep the invariant
            ViewCoords center = getCenter();
            setCenter(center.movedByDelta(pos, pos2));
        }

        private void moveBy(Dimension percent) {
            Point ref = new Point(0,0);
            Point place = new Point(percent.width * getWidth() / 100,
                    percent.height * getHeight() / 100);
            
            ViewCoords pos = getPoint(ref);
            ViewCoords pos2 = getPoint(place);

            setCenter(getCenter().movedByDelta(pos, pos2));
        }
        
        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            if (ZOOM_IN.equals(cmd)) {
                zoomBy(-1, null);
            } else if (ZOOM_OUT.equals(cmd)) {
                zoomBy(1, null);                
            } else if (LEFT.equals(cmd)) {
                moveBy(new Dimension(-20, 0));
            } else if (RIGHT.equals(cmd)) {
                moveBy(new Dimension(20, 0));
            } else if (UP.equals(cmd)) {
                moveBy(new Dimension(0, 20));
            } else if (DOWN.equals(cmd)) {
                moveBy(new Dimension(0, -20));
            }
        }
    }
    
    private class ActionWrapper extends AbstractAction {
        private final String cmd;
        private final ActionListener delegate;

        public ActionWrapper(String cmd, ActionListener delegate) {
            this.cmd = cmd;
            this.delegate = delegate;
        }

        public void actionPerformed(ActionEvent e) {
            ActionEvent evt = new ActionEvent(e.getSource(), e.getID(),
                    cmd, e.getWhen(), e.getModifiers());
            delegate.actionPerformed(evt);
        }
        
    }

    // in m
    private static double dist(Coordinate c1, Coordinate c2) {
        double a1 = Math.PI * c1.getLatitude() / 180;
        double b1 = Math.PI * c1.getLongitude() / 180;
        double a2 = Math.PI * c2.getLatitude() / 180;
        double b2 = Math.PI * c2.getLongitude() / 180;
        return Math.acos(Math.cos(a1)*Math.cos(b1)*Math.cos(a2)*Math.cos(b2)
                + Math.cos(a1)*Math.sin(b1)*Math.cos(a2)*Math.sin(b2)
                + Math.sin(a1)*Math.sin(a2)) * 6378000;
    }

    private class Meter extends JComponent {
        
        public @Override Dimension getPreferredSize() {
            return new Dimension(101, 101);
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
