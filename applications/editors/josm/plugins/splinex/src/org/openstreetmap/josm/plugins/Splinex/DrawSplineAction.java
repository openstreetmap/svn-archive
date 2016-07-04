// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.Splinex;

import static org.openstreetmap.josm.plugins.Splinex.SplinexPlugin.EPSILON;
import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.MoveCommand;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.visitor.paint.MapPaintSettings;
import org.openstreetmap.josm.data.osm.visitor.paint.PaintColors;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerAddEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerOrderChangeEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerRemoveEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeListener;
import org.openstreetmap.josm.gui.layer.MapViewPaintable;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.util.KeyPressReleaseListener;
import org.openstreetmap.josm.gui.util.ModifierListener;
import org.openstreetmap.josm.plugins.Splinex.Spline.PointHandle;
import org.openstreetmap.josm.plugins.Splinex.Spline.SplinePoint;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

@SuppressWarnings("serial")
public class DrawSplineAction extends MapMode implements MapViewPaintable, KeyPressReleaseListener, ModifierListener,
        LayerChangeListener, ActiveLayerChangeListener {
    private final Cursor cursorJoinNode;
    private final Cursor cursorJoinWay;

    private Color rubberLineColor;

    private final Shortcut backspaceShortcut;
    private final BackSpaceAction backspaceAction;

    boolean drawHelperLine;

    public DrawSplineAction(MapFrame mapFrame) {
        super(tr("Spline drawing"), // name
                "spline2", // icon name
                tr("Draw a spline curve"), // tooltip
                mapFrame, getCursor());

        backspaceShortcut = Shortcut.registerShortcut("mapmode:backspace", tr("Backspace in Add mode"),
                KeyEvent.VK_BACK_SPACE, Shortcut.DIRECT);
        backspaceAction = new BackSpaceAction();
        cursorJoinNode = ImageProvider.getCursor("crosshair", "joinnode");
        cursorJoinWay = ImageProvider.getCursor("crosshair", "joinway");
        Main.getLayerManager().addLayerChangeListener(this);
        Main.getLayerManager().addActiveLayerChangeListener(this);
        readPreferences();
    }

    private static Cursor getCursor() {
        try {
            return ImageProvider.getCursor("crosshair", "spline");
        } catch (Exception e) {
            Main.error(e);
        }
        return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    }

    @Override
    public void enterMode() {
        if (!isEnabled())
            return;
        super.enterMode();

        Main.registerActionShortcut(backspaceAction, backspaceShortcut);

        Main.map.mapView.addMouseListener(this);
        Main.map.mapView.addMouseMotionListener(this);
        Main.map.mapView.addTemporaryLayer(this);

        Main.map.keyDetector.addModifierListener(this);
        Main.map.keyDetector.addKeyListener(this);
    }

    int initialMoveDelay, initialMoveThreshold;

    @Override
    protected void readPreferences() {
        rubberLineColor = Main.pref.getColor(marktr("helper line"), null);
        if (rubberLineColor == null)
            rubberLineColor = PaintColors.SELECTED.get();

        initialMoveDelay = Main.pref.getInteger("edit.initial-move-", 200);
        initialMoveThreshold = Main.pref.getInteger("edit.initial-move-threshold", 5);
        initialMoveThreshold *= initialMoveThreshold;
        drawHelperLine = Main.pref.getBoolean("draw.helper-line", true);
    }

    @Override
    public void exitMode() {
        super.exitMode();
        Main.map.mapView.removeMouseListener(this);
        Main.map.mapView.removeMouseMotionListener(this);
        Main.map.mapView.removeTemporaryLayer(this);
        Main.unregisterActionShortcut(backspaceAction, backspaceShortcut);

        Main.map.statusLine.activateAnglePanel(false);
        Main.map.keyDetector.removeModifierListener(this);
        Main.map.keyDetector.removeKeyListener(this);
        removeHighlighting();
        Main.map.mapView.repaint();
    }

    @Override
    public void modifiersChanged(int modifiers) {
        updateKeyModifiers(modifiers);
    }

    private Long mouseDownTime;
    private PointHandle ph;
    private Point helperEndpoint;
    private Point clickPos;
    public int index = 0;
    boolean lockCounterpart;
    private MoveCommand mc;
    private boolean dragControl;
    private boolean dragSpline;

    @Override
    public void mousePressed(MouseEvent e) {
        mouseDownTime = null;
        updateKeyModifiers(e);
        if (e.getButton() != MouseEvent.BUTTON1) {
            helperEndpoint = null; // Hide helper line when panning
            return;
        }
        if (!Main.map.mapView.isActiveLayerDrawable()) return;
        Spline spl = getSpline();
        if (spl == null) return;
        helperEndpoint = null;
        dragControl = false;
        dragSpline = false;
        mouseDownTime = System.currentTimeMillis();
        ph = spl.getNearestPoint(Main.map.mapView, e.getPoint());
        if (e.getClickCount() == 2) {
            if (!spl.isClosed() && spl.nodeCount() > 1 && ph != null && ph.point == SplinePoint.ENDPOINT
                    && ((ph.idx == 0 && direction == 1) || (ph.idx == spl.nodeCount() - 1 && direction == -1))) {
                Main.main.undoRedo.add(spl.new CloseSplineCommand());
                return;
            }
            spl.finishSpline();
            Main.map.repaint();
            return;
        }
        clickPos = e.getPoint();
        if (ph != null) {
            if (ctrl) {
                if (ph.point == SplinePoint.ENDPOINT) {
                    ph = ph.otherPoint(SplinePoint.CONTROL_NEXT);
                    lockCounterpart = true;
                } else
                    lockCounterpart = false;
            } else {
                lockCounterpart = (ph.point != SplinePoint.ENDPOINT
                        && Math.abs(ph.sn.cprev.east() + ph.sn.cnext.east()) < EPSILON && Math.abs(ph.sn.cprev.north()
                        + ph.sn.cnext.north()) < EPSILON);
            }
            if (ph.point == SplinePoint.ENDPOINT && !Main.main.undoRedo.commands.isEmpty()) {
                Command cmd = Main.main.undoRedo.commands.getLast();
                if (cmd instanceof MoveCommand) {
                    mc = (MoveCommand) cmd;
                    Collection<Node> pp = mc.getParticipatingPrimitives();
                    if (pp.size() != 1 || !pp.contains(ph.sn.node))
                        mc = null;
                    else
                        mc.changeStartPoint(ph.sn.node.getEastNorth());
                }
            }
            if (ph.point != SplinePoint.ENDPOINT && !Main.main.undoRedo.commands.isEmpty()) {
                Command cmd = Main.main.undoRedo.commands.getLast();
                if (!(cmd instanceof Spline.EditSplineCommand && ((Spline.EditSplineCommand) cmd).sn == ph.sn))
                    dragControl = true;
            }
            return;
        }
        if (!ctrl && spl.doesHit(e.getX(), e.getY(), Main.map.mapView)) {
            dragSpline = true;
            return;
        }
        if (spl.isClosed()) return;
        if (direction == 0)
            if (spl.nodeCount() < 2)
                direction = 1;
            else
                return;
        Node n = null;
        boolean existing = false;
        if (!ctrl) {
            n = Main.map.mapView.getNearestNode(e.getPoint(), OsmPrimitive.isUsablePredicate);
            existing = true;
        }
        if (n == null) {
            n = new Node(Main.map.mapView.getLatLon(e.getX(), e.getY()));
            existing = false;
        }
        int idx = direction == -1 ? 0 : spl.nodeCount();
        Main.main.undoRedo.add(spl.new AddSplineNodeCommand(new Spline.SNode(n), existing, idx));
        ph = spl.new PointHandle(idx, direction == -1 ? SplinePoint.CONTROL_PREV : SplinePoint.CONTROL_NEXT);
        lockCounterpart = true;
        Main.map.repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mc = null;
        mouseDownTime = null;
        dragSpline = false;
        clickPos = null;
        mouseMoved(e);
        if (direction == 0 && ph != null) {
            if (ph.idx >= ph.getSpline().nodeCount() - 1)
                direction = 1;
            else if (ph.idx == 0)
                direction = -1;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        updateKeyModifiers(e);
        if (mouseDownTime == null) return;
        if (!Main.map.mapView.isActiveLayerDrawable()) return;
        if (System.currentTimeMillis() - mouseDownTime < initialMoveDelay) return;
        Spline spl = getSpline();
        if (spl == null) return;
        if (spl.isEmpty()) return;
        if (clickPos != null && clickPos.distanceSq(e.getPoint()) < initialMoveThreshold)
            return;
        EastNorth en = Main.map.mapView.getEastNorth(e.getX(), e.getY());
        if (Main.getProjection().eastNorth2latlon(en).isOutSideWorld())
            return;
        if (dragSpline) {
            if (mc == null) {
                mc = new MoveCommand(spl.getNodes(), Main.map.mapView.getEastNorth(clickPos.x, clickPos.y), en);
                Main.main.undoRedo.add(mc);
                clickPos = null;
            } else
                mc.applyVectorTo(en);
            Main.map.repaint();
            return;
        }
        clickPos = null;
        if (ph == null) return;
        if (ph.point == SplinePoint.ENDPOINT) {
            if (mc == null) {
                mc = new MoveCommand(ph.sn.node, ph.sn.node.getEastNorth(), en);
                Main.main.undoRedo.add(mc);
            } else
                mc.applyVectorTo(en);
        } else {
            if (dragControl) {
                Main.main.undoRedo.add(spl.new EditSplineCommand(ph.sn));
                dragControl = false;
            }
            ph.movePoint(en);
            if (lockCounterpart) {
                if (ph.point == SplinePoint.CONTROL_NEXT)
                    ph.sn.cprev = new EastNorth(0, 0).subtract(ph.sn.cnext);
                else if (ph.point == SplinePoint.CONTROL_PREV)
                    ph.sn.cnext = new EastNorth(0, 0).subtract(ph.sn.cprev);
            }
        }
        Main.map.repaint();
    }

    Node nodeHighlight;
    short direction;

    @Override
    public void mouseMoved(MouseEvent e) {
        updateKeyModifiers(e);
        if (!Main.map.mapView.isActiveLayerDrawable()) return;
        Spline spl = getSpline();
        if (spl == null) return;
        Point oldHelperEndpoint = helperEndpoint;
        PointHandle oldph = ph;
        boolean redraw = false;
        ph = spl.getNearestPoint(Main.map.mapView, e.getPoint());
        if (ph == null)
            if (!ctrl && spl.doesHit(e.getX(), e.getY(), Main.map.mapView)) {
                helperEndpoint = null;
                Main.map.mapView.setNewCursor(Cursor.MOVE_CURSOR, this);
            } else {
                Node n = null;
                if (!ctrl)
                    n = Main.map.mapView.getNearestNode(e.getPoint(), OsmPrimitive.isUsablePredicate);
                if (n == null) {
                    redraw = removeHighlighting();
                    helperEndpoint = e.getPoint();
                    Main.map.mapView.setNewCursor(cursor, this);
                } else {
                    redraw = setHighlight(n);
                    Main.map.mapView.setNewCursor(cursorJoinNode, this);
                    helperEndpoint = Main.map.mapView.getPoint(n);
                }
            }
        else {
            helperEndpoint = null;
            Main.map.mapView.setNewCursor(cursorJoinWay, this);
            if (ph.point == SplinePoint.ENDPOINT)
                redraw = setHighlight(ph.sn.node);
            else
                redraw = removeHighlighting();
        }
        if (!drawHelperLine || spl.isClosed() || direction == 0)
            helperEndpoint = null;

        if (redraw || oldHelperEndpoint != helperEndpoint || (oldph == null && ph != null)
                || (oldph != null && !oldph.equals(ph)))
            Main.map.repaint();
    }

    /**
     * Repaint on mouse exit so that the helper line goes away.
     */
    @Override
    public void mouseExited(MouseEvent e) {
        if (!Main.map.mapView.isActiveLayerDrawable())
            return;
        removeHighlighting();
        helperEndpoint = null;
        Main.map.mapView.repaint();
    }

    private boolean setHighlight(Node n) {
        if (nodeHighlight == n)
            return false;
        removeHighlighting();
        nodeHighlight = n;
        n.setHighlighted(true);
        return true;
    }

    /**
     * Removes target highlighting from primitives. Issues repaint if required.
     * Returns true if a repaint has been issued.
     */
    private boolean removeHighlighting() {
        if (nodeHighlight != null) {
            nodeHighlight.setHighlighted(false);
            nodeHighlight = null;
            return true;
        }
        return false;
    }

    @Override
    public void paint(Graphics2D g, MapView mv, Bounds box) {
        Spline spl = getSpline();
        if (spl == null)
            return;
        spl.paint(g, mv, rubberLineColor, Color.green, helperEndpoint, direction);
        if (ph != null && (ph.point != SplinePoint.ENDPOINT || (nodeHighlight != null && nodeHighlight.isDeleted()))) {
            g.setColor(MapPaintSettings.INSTANCE.getSelectedColor());
            Point p = mv.getPoint(ph.getPoint());
            g.fillRect(p.x - 1, p.y - 1, 3, 3);
        }
    }

    @Override
    public boolean layerIsSupported(Layer l) {
        return l instanceof OsmDataLayer;
    }

    @Override
    protected void updateEnabledState() {
        setEnabled(getLayerManager().getEditLayer() != null);
    }

    public class BackSpaceAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            Main.main.undoRedo.undo();
        }
    }

    private Spline splCached;

    Spline getSpline() {
        if (splCached != null)
            return splCached;
        Layer l = getLayerManager().getEditLayer();
        if (!(l instanceof OsmDataLayer))
            return null;
        splCached = layerSplines.get(l);
        if (splCached == null)
            splCached = new Spline();
        layerSplines.put(l, splCached);
        return splCached;
    }

    @Override
    public void activeOrEditLayerChanged(ActiveLayerChangeEvent e) {
        splCached = layerSplines.get(Main.getLayerManager().getActiveLayer());
    }

    Map<Layer, Spline> layerSplines = new HashMap<>();

    @Override
    public void layerOrderChanged(LayerOrderChangeEvent e) {
        // Do nothing
    }

    @Override
    public void layerAdded(LayerAddEvent e) {
        // Do nothing
    }

    @Override
    public void layerRemoving(LayerRemoveEvent e) {
        layerSplines.remove(e.getRemovedLayer());
        splCached = null;
    }

    @Override
    public void doKeyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DELETE && ph != null) {
            Spline spl = ph.getSpline();
            if (spl.nodeCount() == 3 && spl.isClosed() && ph.idx == 1)
                return; // Don't allow to delete node when it results with two-node closed spline
            Main.main.undoRedo.add(spl.new DeleteSplineNodeCommand(ph.idx));
            e.consume();
        }
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE && direction != 0) {
            direction = 0;
            Main.map.mapView.repaint();
            e.consume();
        }
    }

    @Override
    public void doKeyReleased(KeyEvent e) {
        // Do nothing
    }
}
