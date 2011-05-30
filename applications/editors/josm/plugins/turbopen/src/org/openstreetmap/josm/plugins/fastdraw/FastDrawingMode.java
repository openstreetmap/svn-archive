/*
 * This file is part of ImproveWayAccuracy plugin for JOSM.
 * http://wiki.openstreetmap.org/wiki/JOSM/Plugins/ImproveWayAccuracy
 *
 * Licence: GPL v2 or later
 * Author:  Alexei Kasatkin, 2011
 * Thanks to authors of BuildingTools, ImproveWayAccuracy and LakeWalker 
 * for good sample code
 */
package org.openstreetmap.josm.plugins.fastdraw;

import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import org.openstreetmap.josm.data.coor.LatLon;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.plaf.basic.BasicArrowButton;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.SimplifyWayAction;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.MapViewPaintable;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.tools.Geometry;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

class FastDrawingMode extends MapMode implements MapViewPaintable,
        AWTEventListener {
    private static final String SIMPLIFYMODE_MESSAGE=
            "Press Enter to simplify or save, Up/Down to tune simplification";
    private static final String DRAWINGMODE_MESSAGE=
    "Click or Click&drag to continue, Ctrl-Click to add fixed node, Shift-Click to start new line";
    
    private double maxDist;
    private double epsilonMult;
    //private double deltaLatLon;
    /// When drawing line, distance between points will be this
    private double minPixelsBetweenPoints;
    /// Initial tolerance for Douglas-Pecker algorithm
    private double startingEps;
    
    private MapView mv;
    private ArrayList<LatLon> points = new ArrayList<LatLon>(100);
    private ArrayList<LatLon> simplePoints = new ArrayList<LatLon>(100);
    private String statusText;
    private boolean drawing;
    private boolean ctrl;
    private boolean shift;
    private boolean oldCtrl;
    private boolean oldShift;
    Set<LatLon> used;
    Set<LatLon> fixed = new HashSet<LatLon>();
    private double eps=startingEps;
    private final Stroke strokeForSimplified;
    private final Stroke strokeForOriginal;
    private boolean ready;
    private final Cursor cursorDraw;
    private final Cursor cursorCtrl;
    private final Cursor cursorShift;
    private final Cursor cursorReady;
    private final Cursor cursorNode;
    private boolean nearpoint;
    

    FastDrawingMode(MapFrame mapFrame) {
        super(tr("FastDrawing"), "turbopen.png", tr("Fast drawing mode"), Shortcut.registerShortcut(
                "mapmode:FastDraw",
                tr("Mode: {0}", tr("Fast drawing mode")),
                KeyEvent.VK_T, Shortcut.GROUP_EDIT), mapFrame, Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        strokeForOriginal = new BasicStroke();
        strokeForSimplified = new BasicStroke(1,BasicStroke.CAP_ROUND,BasicStroke.JOIN_BEVEL,5f,
                new float[]{5.f,5f},0f);
        cursorDraw = ImageProvider.getCursor("crosshair", null);
        cursorCtrl = ImageProvider.getCursor("crosshair", "fixed");
        cursorShift = ImageProvider.getCursor("crosshair", "new");
        cursorReady = ImageProvider.getCursor("crosshair", "ready");
        cursorNode = ImageProvider.getCursor("crosshair", "joinnode");
        //loadPrefs();
    }

    @Override
    public void enterMode() {
        if (!isEnabled()) return;
        super.enterMode();
        loadPrefs();
        mv = Main.map.mapView;

        if (getCurrentDataSet() == null) return;
        
        Main.map.mapView.addMouseListener(this);
        Main.map.mapView.addMouseMotionListener(this);
        Main.map.mapView.addTemporaryLayer(this);
        
        Main.map.mapView.setCursor(cursorDraw);

        try {
            Toolkit.getDefaultToolkit().addAWTEventListener(this,
                    AWTEvent.KEY_EVENT_MASK);
        } catch (SecurityException ex) {
        }
    }

    @Override
    public void exitMode() {
        super.exitMode();

        Main.map.mapView.removeMouseListener(this);
        Main.map.mapView.removeMouseMotionListener(this);

        Main.map.mapView.removeTemporaryLayer(this);

        try {
            Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        } catch (SecurityException ex) {
        }
        savePrefs();
        Main.map.mapView.setCursor(cursorDraw);
        Main.map.mapView.repaint();
    }

    @Override
    public boolean layerIsSupported(Layer l) {
        return l instanceof OsmDataLayer;
    }

    @Override
    protected void updateEnabledState() {
        setEnabled(getEditLayer() != null);
    }

    //////////    Event listener methods
    
    @Override
    public void paint(Graphics2D g, MapView mv, Bounds bbox) {
        if (points.size() == 0) return;
        List<LatLon> pts=points;
        if (simplePoints!=null && simplePoints.size()>0) {
            // we are drawing simplified version, that exists
            pts=simplePoints;
            g.setStroke(strokeForSimplified);
        } else {
            g.setStroke(strokeForOriginal);
        }
        
        Point p1, p2;
        LatLon pp2;
        p1 = getPoint(pts.get(0));
        g.setColor(Color.green);
        g.fillOval(p1.x - 3, p1.y - 3, 7, 7);
        if (pts.size() > 1) {
            for (int i = 0; i < pts.size() - 1; i++) {
                g.setColor(Color.red);
                p1 = getPoint(pts.get(i));
                pp2 = pts.get(i + 1);
                p2 = getPoint(pts.get(i + 1));

                g.drawLine(p1.x, p1.y, p2.x, p2.y);
                if (fixed.contains(pp2)) {
                    g.setColor(Color.green);
                    g.fillOval(p2.x - 3, p2.y - 3, 7, 7);
                } else {
                    g.fillRect(p2.x - 1, p2.y - 1, 3, 3);
                }
            }
        }
    }

    @Override
    public void eventDispatched(AWTEvent event) {
        if (Main.map == null || Main.map.mapView == null
                || !Main.map.mapView.isActiveLayerDrawable()) {
            return;
        }
        updateKeyModifiers((InputEvent) event);
        if (event.getID() == KeyEvent.KEY_PRESSED) {
            doKeyEvent((KeyEvent) event);
        }
        updateCursor();
//        updateStatusLine();
        Main.map.mapView.repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!isEnabled()) return;
        if (e.getButton() != MouseEvent.BUTTON1) return;
        
        int idx = findClosestPoint(e.getX(),e.getY(),maxDist);
        if (idx==0) {
            // the way should become closed
            points.add(points.get(idx));
            drawing=false;
            ready=true;
            updateCursor();
            return;
        } 
        
        if (shift) newDrawing();
        if (ready) {
            setStatusLine(tr(SIMPLIFYMODE_MESSAGE));
            return;
        }
        
        LatLon p = getLatLon(e);
        Node nd1 = getNearestNode(e.getPoint(), maxDist);

        if (nd1!=null) {
            // found node, make it fixed point of the line
            //System.out.println("node "+nd1);
            p=nd1.getCoor();
            fixed.add(p);
        }         
        
        drawing = true;
        points.add(p);
        if (ctrl) fixed.add(p);
        simplePoints=null;
               
        setStatusLine(tr("Please move the mouse to draw new way"));
        Main.map.mapView.repaint();

    }
    
    

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!isEnabled()) return;
        if (e.getButton() != MouseEvent.BUTTON1) return;
        drawing = false;
        if (!ready) setStatusLine(tr(DRAWINGMODE_MESSAGE)
                        + tr(SIMPLIFYMODE_MESSAGE));
        Main.map.mapView.repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (!isEnabled()) return;
        Node nd1 = getNearestNode(e.getPoint(), maxDist);
        boolean nearpoint2=nd1!=null; 
        if (nearpoint!=nearpoint2) {nearpoint=nearpoint2;updateCursor();}
        
        if (!drawing) return;
        if (ready) {
            setStatusLine(tr(SIMPLIFYMODE_MESSAGE));
        }
        
        Point lastP = getPoint(points.get(points.size() - 1));

        if (nearpoint){
            if ( Math.hypot(e.getX() - lastP.x, e.getY() - lastP.y) > 1e-2) {
                points.add(nd1.getCoor());
                fixed.add(nd1.getCoor());
                Main.map.mapView.repaint();
            } 
        } else {
            if (Math.hypot(e.getX() - lastP.x, e.getY() - lastP.y) > minPixelsBetweenPoints) {
                points.add(getLatLon(e));
                Main.map.mapView.repaint();
            }
        }
        
        //statusText = getLatLon(e).toString();        updateStatusLine();
    }

    @Override
    protected void updateStatusLine() {
        Main.map.statusLine.setHelpText(statusText);
        Main.map.statusLine.repaint();

    }

    LatLon getLatLon(MouseEvent e) {
        return mv.getLatLon(e.getX(), e.getY());
    }

    Point getPoint(LatLon p) {
        return mv.getPoint(p);
    }

    public void newDrawing() {
        points.clear();
        used=null;
        fixed.clear();
        eps=startingEps;
        ready=false;
        simplePoints=null;
    }

    public void back() {
        if (points.size() == 0) {
            return;
        }
        points.remove(points.size() - 1);
        Main.map.mapView.repaint();
    }

    /**
     * Updates shift and ctrl key states
     */
    private void updateKeyModifiers(InputEvent e) {
        oldCtrl = ctrl;
        oldShift = shift;
        ctrl = (e.getModifiers() & ActionEvent.CTRL_MASK) != 0;
        shift = (e.getModifiers() & ActionEvent.SHIFT_MASK) != 0;
    }

    private void doKeyEvent(KeyEvent e) {
        ///  System.out.println(e);
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            if (simplePoints!=null) {
                simplePoints=null;
                Main.map.mapView.repaint();
                eps=startingEps;
            }
            back();
        }
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            // first Enter = simplify, second = save the way
            if (simplePoints==null) {
                simplify(eps);
                setStatusLine(tr(SIMPLIFYMODE_MESSAGE));
            } else saveAsWay();
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            // more details
            e.consume();
            changeEpsilon(epsilonMult);
        }
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            // less details
            e.consume();
            changeEpsilon(1/epsilonMult);
        }
    }

    
    void changeEpsilon(double k) {
        //System.out.println(tr("Eps={0}", eps));
        eps*=k;
        setStatusLine(tr("Eps={0}", eps));
        simplify(eps);
        Main.map.mapView.repaint();
    }
    
    /**
     * Simplified drawn line, not touching the nodes includes in "fixed" set.
     */
    private void simplify(double epsilon) {
        //System.out.println("Simplify polyline...");
        int n = points.size();
        if (n < 3) return;
        used = new HashSet<LatLon>(n);
        int start = 0;
        for (int i = 0; i < n; i++) {
            LatLon p = points.get(i);
            if (fixed.contains(p) || i == n - 1) {
                if (start < 0) {
                    start = i;
                } else {
                    douglasPeucker(start, i, epsilon, 0);
                }
            }
        }
        simplePoints = new ArrayList<LatLon>(n);
        simplePoints.addAll(points);
        simplePoints.retainAll(used);
        Main.map.mapView.repaint();
        used = null;
    }

    /**
     * Simplification of the line specified by "points" field.
     * Remainin points are included to "used" set.
     * @param start - starting index
     * @param end - ending index
     * @param epsilon - min point-to line distance in pixels (tolerance)
     * @param depth - recursion level
     */
    private void douglasPeucker(int start, int end, double epsilon, int depth) {
        if (depth > 500) return;
        if (end - start < 1) return; // incorrect invocation
        LatLon first = points.get(start);
        LatLon last = points.get(end);
        Point firstp = getPoint(first);
        Point lastp = getPoint(last);
        used.add(first);
        used.add(last);

        if (end - start < 2) return;
        
        int farthest_node = -1;
        double farthest_dist = 0;

        ArrayList<double[]> new_nodes = new ArrayList<double[]>();

        double d = 0;

        for (int i = start + 1; i < end; i++) {
            d = pointLineDistance(getPoint(points.get(i)), firstp, lastp);
            if (d > farthest_dist) {
                farthest_dist = d;
                farthest_node = i;
            }
        }

        if (farthest_dist > epsilon) {
            douglasPeucker(start, farthest_node, epsilon, depth + 1);
            douglasPeucker(farthest_node, end, epsilon, depth + 1);
        }
    }

    /** Modfified funclion from LakeWalker
     * Gets distance from point p1 to line p2-p3
     */
    public double pointLineDistance(Point p1, Point p2, Point p3) {
        double x0 = p1.x;        double y0 = p1.y;
        double x1 = p2.x;        double y1 = p2.y;
        double x2 = p3.x;        double y2 = p3.y;
        if (x2 == x1 && y2 == y1) {
            return Math.hypot(x1 - x0, y1 - y0);
        } else {
            return Math.abs((x2-x1)*(y1-y0)-(x1-x0)*(y2-y1))/Math.hypot(x2 - x1,y2 - y1);
        }
    }

    private void saveAsWay() {
        List<LatLon> pts=points;
        if (simplePoints!=null && simplePoints.size()>0) {
            // we are drawig simplified version, that exists
            pts=simplePoints;
        }
            
        int n = pts.size();
        if (n == 0) return;
        
        Collection<Command> cmds = new LinkedList<Command>();
        int i = 0;
        Way w = new Way();
        LatLon first=pts.get(0);
        Node firstNode=null;
        
        for (LatLon p : pts) {
            Node nd=null;
            if (fixed.contains(p)) {
                // there may be a node with same ccoords!
                nd = Main.map.mapView.getNearestNode(getPoint(p), OsmPrimitive.isUsablePredicate);
            }
            if (nd==null) {
                if (i>0 && p.equals(first)) nd=firstNode; else {
                    nd = new Node(p);
                    cmds.add(new AddCommand(nd));
                }
            }
            if (nd.getCoor().isOutSideWorld()) {
                JOptionPane.showMessageDialog(Main.parent,
                        tr("Cannot place node outside of the world."));
                return;
            }
            if (i==0) firstNode=nd;
            w.addNode(nd);
            i++;
        }
        cmds.add(new AddCommand(w));
        Command c = new SequenceCommand(tr("Draw the way by mouse"), cmds);
        Main.main.undoRedo.add(c);
        newDrawing(); // stop drawing
        // Select this way and switch drawing mode off
        exitMode();
        getCurrentDataSet().setSelected(w);
        Main.map.selectSelectTool(false);
    }

    private int findClosestPoint(double x, double y, double d) {
        int n=points.size();
        int idx=-1;
        double dist,minD=1e10;
        for (int i=0;i<n;i++) {
            dist = Math.sqrt(getPoint(points.get(i)).distanceSq(x,y));
            if (dist<d && dist<minD) {
                idx=i;
                minD=dist;
            };
        }
        return idx;
    }

    private void setStatusLine(String tr) {
        statusText=tr;
        updateStatusLine();
    }

    /*private Node findClosestNode(LatLon p, double d) {
        Node nn=null;
        double dist,minD=1e10,x,y;
        Point pscreen=getPoint(p);   x=pscreen.x; y=pscreen.y;
        BBox b=new BBox(new LatLon(p.lat()-deltaLatLon,p.lon()-deltaLatLon),
                new LatLon(p.lat()+deltaLatLon,p.lon()+deltaLatLon));
        List<Node> nodes = getCurrentDataSet().searchNodes(b);
        for (Node n: nodes) {
            dist = Math.sqrt(getPoint(n.getCoor()).distanceSq(x,y));
            if (dist<d && dist<minD) {
                nn=n;
                minD=dist;
            };
        }
        return nn;
    }*/

    
    void loadPrefs() {
        maxDist = Main.pref.getDouble("fastdraw.maxdist", 5);
        epsilonMult = Main.pref.getDouble("fastdraw.epsilonmult", 1.1);
        //deltaLatLon = Main.pref.getDouble("fastdraw.deltasearch", 0.01);
        minPixelsBetweenPoints = Main.pref.getDouble("fastdraw.mindelta", 20);
        startingEps = Main.pref.getDouble("fastdraw.startingEps", 20);
    }
    
    void savePrefs() {
         Main.pref.putDouble("fastdraw.maxdist", maxDist);
         Main.pref.putDouble("fastdraw.epsilonmult", epsilonMult);
         //Main.pref.putDouble("fastdraw.deltasearch", deltaLatLon);
         Main.pref.putDouble("fastdraw.mindelta",minPixelsBetweenPoints);
         Main.pref.putDouble("fastdraw.startingEps",startingEps);
         try {Main.pref.save();} catch (IOException e) {
             System.err.println(tr("Can not save preferences"));
         }
    }

    private void updateCursor() {
        if (shift) Main.map.mapView.setCursor(cursorShift); else
        if (ready) Main.map.mapView.setCursor(cursorReady); else
        if (ctrl) Main.map.mapView.setCursor(cursorCtrl); else
        if (nearpoint) Main.map.mapView.setCursor(cursorCtrl); else
        Main.map.mapView.setCursor(cursorDraw);
        
        
    }

    private Node getNearestNode(Point point, double maxDist) {
       Node nd = Main.map.mapView.getNearestNode(point, OsmPrimitive.isUsablePredicate);
       if (nd!=null && getPoint(nd.getCoor()).distance(point)<=maxDist) return nd; 
       else return null;
    }

}
