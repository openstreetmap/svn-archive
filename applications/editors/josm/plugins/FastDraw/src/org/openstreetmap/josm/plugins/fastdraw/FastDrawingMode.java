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
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Tag;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.annotation.Target;
import java.util.*;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.PasteTagsAction.TagPaster;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.ChangeNodesCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.MapViewPaintable;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

class FastDrawingMode extends MapMode implements MapViewPaintable,
        AWTEventListener {
    private static final String SIMPLIFYMODE_MESSAGE=
            tr("Q=Options, Enter=save, Ctrl-Enter=save with tags, Up/Down=tune");
    private static final String DRAWINGMODE_MESSAGE=
    tr("Click or Click&drag to continue, Ctrl-Click to add fixed node, Shift-Click to delete, Enter to simplify or save, Ctrl-Shift-Click to start new line");

    private FDSettings settings;

    private DrawnPolyLine line;
    private MapView mv;
    private String statusText;
    private boolean drawing;
    private boolean ctrl;
    private boolean shift;
    private double eps;
    private final Stroke strokeForSimplified;
    private final Stroke strokeForOriginal;
    private final Cursor cursorDraw;
    private final Cursor cursorCtrl;
    private final Cursor cursorShift;
    private final Cursor cursorReady;
    private final Cursor cursorNode;
    private final Cursor cursorDrawing;
    private boolean nearpoint;
    private LatLon highlighted;
    private int nearestIdx;
    private Stroke strokeForDelete;
    private int dragNode=-1;
    private SequenceCommand delCmd;
    private List<Node> oldNodes;


    FastDrawingMode(MapFrame mapFrame) {
        super(tr("FastDrawing"), "turbopen.png", tr("Fast drawing mode"), Shortcut.registerShortcut(
                "mapmode/building",
                tr("Mode: {0}", tr("Fast drawing mode")),
                KeyEvent.VK_T, Shortcut.GROUP_EDIT), mapFrame, Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        line=new DrawnPolyLine();
        strokeForOriginal = new BasicStroke();
        strokeForDelete = new BasicStroke(3);
        strokeForSimplified = new BasicStroke(1,BasicStroke.CAP_ROUND,BasicStroke.JOIN_BEVEL,5f,
                new float[]{5.f,5f},0f);
        cursorDraw = ImageProvider.getCursor("crosshair", null);
        cursorCtrl = ImageProvider.getCursor("crosshair", "fixed");
        cursorShift = ImageProvider.getCursor("crosshair", "new");
        cursorReady = ImageProvider.getCursor("crosshair", "ready");
        cursorNode = ImageProvider.getCursor("crosshair", "joinnode");
        cursorDrawing = ImageProvider.getCursor("crosshair", "mode");
        //loadPrefs();
    }

// <editor-fold defaultstate="collapsed" desc="Event listeners">

    @Override
    public void enterMode() {
        if (!isEnabled()) return;
        super.enterMode();
        settings=new FDSettings();
        settings.loadPrefs();
        settings.savePrefs();
        
        eps=settings.startingEps;
        mv = Main.map.mapView;
        line.setMv(mv);

        if (getCurrentDataSet() == null) return;

        Main.map.mapView.addMouseListener(this);
        Main.map.mapView.addMouseMotionListener(this);
        Main.map.mapView.addTemporaryLayer(this);

        updateCursor();
        Collection<Way> selectedWays = Main.main.getCurrentDataSet().getSelectedWays();
        if (selectedWays!=null // if there is a selection
            && selectedWays.size()==1 // and one way is selected
            && line.getPoints().size()==0) /* and ther is no already drawn line */ {
            // we can start drawing new way starting from old one
            Way w = selectedWays.iterator().next();
            
            if (w.isNew()) loadFromWay(w);
        }

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
        settings.savePrefs();
        Main.map.mapView.setCursor(cursorDraw);
        repaint();
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
        LinkedList<LatLon> pts=line.getPoints();
        if (pts.isEmpty()) return;

        if (line.wasSimplified()) {
            // we are drawing simplified version, that exists
            g.setStroke(strokeForSimplified);
        } else {
            g.setStroke(strokeForOriginal);
        }

        Point p1, p2;
        LatLon pp1, pp2;
        p1 = line.getPoint(pts.get(0));
        g.setColor(settings.COLOR_FIXED);
        g.fillOval(p1.x - 3, p1.y - 3, 7, 7);
        Color lineColor=settings.COLOR_NORMAL;
        if (pts.size() > 1) {
        Iterator<LatLon> it1,it2;
        it1=pts.listIterator(0);
        it2=pts.listIterator(1);
            for (int i = 0; i < pts.size() - 1; i++) {
                pp1 = it1.next();
                p1 = line.getPoint(pp1);
                pp2 = it2.next();
                p2 = line.getPoint(pp2);
                if (shift && highlighted==pp1 && nearestIdx<0) {lineColor=settings.COLOR_SELECTEDFRAGMENT;}
                if (!shift && line.isLastPoint(i)) { lineColor=settings.COLOR_EDITEDFRAGMENT; }
                g.setColor(lineColor);
                g.drawLine(p1.x, p1.y, p2.x, p2.y);
  //                  g.fillOval(p2.x - 5, p2.y - 5, 11, 11);
                if (line.isFixed(pp2)) {
                    lineColor=settings.COLOR_NORMAL;
                    g.setColor(settings.COLOR_FIXED);
                    g.fillOval(p2.x - 3, p2.y - 3, 7, 7);
                } else {
                    g.fillRect(p2.x - 1, p2.y - 1, 3, 3);
                }
                if (!drawing) {
                    if (shift && !line.wasSimplified() && nearestIdx==i+1 ) {
                        // highlight node to delete
                        g.setStroke(strokeForDelete);
                        g.setColor(settings.COLOR_DELETE);
                        g.drawLine(p2.x - 5, p2.y - 5,p2.x + 5, p2.y + 5);
                        g.drawLine(p2.x - 5, p2.y + 5,p2.x + 5, p2.y - 5);
                        g.setStroke(strokeForOriginal);
                    }
                    if (ctrl && !line.wasSimplified() && nearestIdx==i+1 ) {
                        // highlight node to toggle fixation
                        g.setStroke(strokeForDelete);
                        g.setColor( line.isFixed(pp2) ? settings.COLOR_NORMAL: settings.COLOR_FIXED);
                        g.drawOval(p2.x - 5, p2.y - 5, 11, 11);
                        g.setStroke(strokeForOriginal);
                    }
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
        if (event.getID() == KeyEvent.KEY_RELEASED) {
            doKeyReleaseEvent((KeyEvent) event);
        }
        updateCursor();
//        updateStatusLine();
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!isEnabled()) return;
        if (e.getButton() != MouseEvent.BUTTON1) return;


        int idx=line.findClosestPoint(e.getPoint(),settings.maxDist);
        if (idx==0 && !line.isClosed()) {
            line.closeLine();
            // the way should become closed
            drawing=false;
            dragNode=0;
            updateCursor();
            return;
        }

        if (ctrl && shift) {newDrawing();repaint();return;}
        if (!ctrl && shift) {
            if (idx>=0) {line.deleteNode(idx); nearestIdx=-1;}
            else line.tryToDeleteSegment(e.getPoint());
            return;
        }
        if (idx>=0) {
            if (ctrl) {
                // toggle fixed point
                line.toggleFixed(idx);
            }
            // node dragging
            dragNode=idx;
            return;
        }
        startDrawing(e.getPoint());
    }

    private void startDrawing(Point point) {
        //if (line.isClosed()) { setStatusLine(tr(SIMPLIFYMODE_MESSAGE));return;  }
        drawing = true;

        LatLon p = mv.getLatLon(point.x, point.y);
        Node nd1 = getNearestNode(point, settings.maxDist);
        if (nd1!=null) {
            // found node, make it fixed point of the line
            //System.out.println("node "+nd1);
            p=nd1.getCoor();
            line.fixPoint(p);
        }

        line.addLast(p);
        if (ctrl) line.fixPoint(p);
        line.clearSimplifiedVersion();

        setStatusLine(tr("Please move the mouse to draw new way"));
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1) return;
        stopDrawing();
    }
    
    private void stopDrawing() {
        if (!isEnabled()) return;
        dragNode = -1;
        drawing = false;
        highlighted=null;
        if (!line.isClosed()) setStatusLine(DRAWINGMODE_MESSAGE);
        updateCursor();
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (!isEnabled()) return;
        Node nd1 = getNearestNode(e.getPoint(), settings.maxDist);
        boolean nearpoint2=nd1!=null;
        if (nearpoint!=nearpoint2) {nearpoint=nearpoint2;updateCursor();}

        nearestIdx=line.findClosestPoint(e.getPoint(),settings.maxDist);
        
        if (!drawing) {
            if (dragNode>=0) {
                line.moveNode(dragNode,getLatLon(e));
                repaint();
                return;
            }

            if (shift) {
                // find line fragment to highlight
                LatLon h2=line.findBigSegment(e.getPoint());
                if (highlighted!=h2) {
                    highlighted=h2;
                    repaint();
                }
            }
            return;
        }
        updateCursor();
        if (line.isClosed()) setStatusLine(SIMPLIFYMODE_MESSAGE);

        // do not draw points close to existing points - we do not want self-intersections
        if (nearestIdx>=0) { return; }

        Point lastP = line.getLastPoint(); // last point of line fragment being edited

            if (nearpoint){
            if ( Math.hypot(e.getX() - lastP.x, e.getY() - lastP.y) > 1e-2) {
                line.addFixed(nd1.getCoor()); // snap to node coords
                repaint();
            }
        } else {
            if (Math.hypot(e.getX() - lastP.x, e.getY() - lastP.y) > settings.minPixelsBetweenPoints) {
                          line.addLast(getLatLon(e)); // free mouse-drawing
                repaint();
            }
        }

        //statusText = getLatLon(e).toString();        updateStatusLine();
    }

    private void doKeyEvent(KeyEvent e) {
        ///  System.out.println(e);
        switch(e.getKeyCode()) {
        case KeyEvent.VK_BACK_SPACE:
            if (line.wasSimplified()) {
                line.clearSimplifiedVersion();
                repaint();
                eps=settings.startingEps;
            }
            back();
        break;
        case KeyEvent.VK_ENTER:
            // first Enter = simplify, second = save the way
            if (!line.wasSimplified()) {
                //line.simplify(eps);
                eps = line.autoSimplify(settings.startingEps, settings.epsilonMult, 
                        settings.pkmBlockSize, settings.maxPointsPerKm);
                repaint();
                showSimplifyHint();
            } else saveAsWay();
        break;
        case KeyEvent.VK_DOWN:
            // more details
            e.consume();
            changeEpsilon(settings.epsilonMult);
        break;
        case KeyEvent.VK_UP:
            // less details
            e.consume();
            changeEpsilon(1/settings.epsilonMult);
        break;
        case KeyEvent.VK_ESCAPE:
            // less details
            e.consume();
            line.moveToTheEnd();
        break;
        case KeyEvent.VK_Q:
            // less details
            e.consume();
            try {
                Toolkit.getDefaultToolkit().removeAWTEventListener(this);
                new FastDrawConfigDialog(settings);
                eps = line.autoSimplify(settings.startingEps, settings.epsilonMult, settings.pkmBlockSize,settings.maxPointsPerKm);
                //System.out.println("final eps="+eps);
                Toolkit.getDefaultToolkit().addAWTEventListener(this,
                    AWTEvent.KEY_EVENT_MASK);
            } catch (SecurityException ex) {  }
            repaint();
        break;
        case KeyEvent.VK_SPACE:
            if (!drawing) {
                Point p = Main.map.mapView.getMousePosition();
                if (p!=null) startDrawing(p);
            }
        break;
        }
    }
    
    private void doKeyReleaseEvent(KeyEvent keyEvent) {
            if (keyEvent.getKeyCode()==KeyEvent.VK_SPACE) stopDrawing();
    }
    /**
     * Updates shift and ctrl key states
     */
    private void updateKeyModifiers(InputEvent e) {
        ctrl = (e.getModifiers() & ActionEvent.CTRL_MASK) != 0;
        shift = (e.getModifiers() & ActionEvent.SHIFT_MASK) != 0;
    }

    @Override
    protected void updateStatusLine() {
        Main.map.statusLine.setHelpText(statusText);
        Main.map.statusLine.repaint();
    }
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="Different action helper methods">
    public void newDrawing() {
        delCmd=null; oldNodes=null;
        eps=settings.startingEps;
        line.clear();
    }
    private void saveAsWay() {
        List<LatLon> pts=line.getPoints();
        int n = pts.size();
        if (n == 0) return;

        Collection<Command> cmds = new LinkedList<Command>();
        int i = 0;
        Way w = new Way();
        LatLon first=pts.get(0);
        Node firstNode=null;

        for (LatLon p : pts) {
            Node nd=null;
            //if (line.isFixed(p)) {
                // there may be a node with same ccoords!
                nd = Main.map.mapView.getNearestNode(line.getPoint(p), OsmPrimitive.isUsablePredicate);
            //}
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
        if (ctrl) {
            // paste tags - from ctrl-shift-v
            Set <OsmPrimitive> ts = new HashSet<OsmPrimitive>();
            ts.add(w);
            TagPaster tp = new TagPaster(Main.pasteBuffer.getDirectlyAdded(), ts);
            List<Tag> execute = tp.execute();
            Map<String,String> tgs=new HashMap<String,String>();
            for (Tag t : execute) {
                w.put(t.getKey(), t.getValue());
            }
        }
        if (delCmd!=null) {
            List<Node> nodes = w.getNodes();
            for (Node nd: oldNodes) {
                // node from old way but not in new way 
                if (!nodes.contains(nd)) {
                    List<OsmPrimitive> refs = nd.getReferrers();
                    // does someone need this node? if no-delete it.
                    if (refs.isEmpty()) cmds.add(new DeleteCommand(nd));                                       
                }
            }
            cmds.add(new AddCommand(w));
        } else cmds.add(new AddCommand(w));
        Command c = new SequenceCommand(tr("Draw the way by mouse"), cmds);
        Main.main.undoRedo.add(c);
        newDrawing(); // stop drawing
        // Select this way and switch drawing mode off
        exitMode();
        getCurrentDataSet().setSelected(w);
        Main.map.selectSelectTool(false);
    }

    private void repaint() {
        Main.map.mapView.repaint();
    }

    public void back() {
        line.undo();
        repaint();
    }

    void changeEpsilon(double k) {
        //System.out.println(tr("Eps={0}", eps));
        eps*=k;
        line.simplify(eps);
        /* I18N: Eps = Epsilon, the tolerance parameter */ 
        showSimplifyHint();
        repaint();
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




    private void updateCursor() {
        if (shift) Main.map.mapView.setCursor(cursorShift); else
        if (line.isClosed()) Main.map.mapView.setCursor(cursorReady); else
        if (ctrl) Main.map.mapView.setCursor(cursorCtrl); else
        if (nearpoint) Main.map.mapView.setCursor(cursorCtrl); else
        if (drawing) Main.map.mapView.setCursor(cursorDrawing); else
        Main.map.mapView.setCursor(cursorDraw);


    }
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="Helper functions">

    private Node getNearestNode(Point point, double maxDist) {
       Node nd = Main.map.mapView.getNearestNode(point, OsmPrimitive.isUsablePredicate);
       if (nd!=null && line.getPoint(nd.getCoor()).distance(point)<=maxDist) return nd;
       else return null;
    }

    LatLon getLatLon(MouseEvent e) {
        return mv.getLatLon(e.getX(), e.getY());
    }
// </editor-fold>

    private void showSimplifyHint() {
            setStatusLine(tr("Eps={0}, {1} points, {2} p/km", 
                eps, line.getSimplePointsCount(),line.getNodesPerKm(settings.pkmBlockSize))+" "
            +SIMPLIFYMODE_MESSAGE);
    }

    private void loadFromWay(Way w) {
        List<LatLon> pts=line.getPoints();

        Collection<Command> cmds = new LinkedList<Command>();
        
        Node firstNode=null;
        Object[] nodes = w.getNodes().toArray();
        int n=nodes.length;
        if (w.isClosed()) n--;
        for (int i=0;i<n;i++) {
            Node nd=(Node) nodes[i];
            line.addLast(nd.getCoor());
        }
        if (w.isClosed()) line.closeLine();
        oldNodes = w.getNodes();
        cmds.add(new DeleteCommand(w));
        delCmd = new SequenceCommand(tr("Convert way to FastDraw line"), cmds);
        Main.main.undoRedo.add(delCmd);
    }

}
