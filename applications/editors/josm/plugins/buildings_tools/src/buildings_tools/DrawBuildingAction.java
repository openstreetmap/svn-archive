package buildings_tools;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import static buildings_tools.BuildingsToolsPlugin.latlon2eastNorth;

import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.LinkedList;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.coor.*;
import org.openstreetmap.josm.data.osm.DataSet;
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

@SuppressWarnings("serial")
public class DrawBuildingAction extends MapMode implements MapViewPaintable, AWTEventListener, SelectionChangedListener {
    private enum Mode {
        None, Drawing, DrawingWidth, DrawingAngFix
    }

    final private Cursor cursorCrosshair;
    final private Cursor cursorJoinNode;
    private Cursor currCursor;
    private Cursor customCursor;

    private Mode mode = Mode.None;
    private Mode nextMode = Mode.None;

    private final Color selectedColor;
    private Point drawStartPos;
    private Point mousePos;
    private boolean isCtrlDown;
    private boolean isShiftDown;
    private boolean isAltDown;

    Building building = new Building();

    public DrawBuildingAction(MapFrame mapFrame) {
        super(tr("Draw buildings"), "building", tr("Draw buildings"),
                Shortcut.registerShortcut("mapmode:buildings",
                        tr("Mode: {0}", tr("Draw buildings")),
                        KeyEvent.VK_B, Shortcut.GROUP_EDIT),
                mapFrame, getCursor());

        cursorCrosshair = getCursor();
        cursorJoinNode = ImageProvider.getCursor("crosshair", "joinnode");
        currCursor = cursorCrosshair;

        selectedColor = Main.pref.getColor(marktr("selected"), Color.red);
    }

    private static Cursor getCursor() {
        try {
            return ImageProvider.getCursor("crosshair", "building");
        } catch (Exception e) {
        }
        return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    }

    /**
     * Displays the given cursor instead of the normal one
     *
     * @param Cursors
     *            One of the available cursors
     */
    private void setCursor(final Cursor c) {
        if (currCursor.equals(c))
            return;
        try {
            // We invoke this to prevent strange things from happening
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    // Don't change cursor when mode has changed already
                    if (!(Main.map.mapMode instanceof DrawBuildingAction))
                        return;
                    Main.map.mapView.setCursor(c);
                }
            });
            currCursor = c;
        } catch (Exception e) {
        }
    }

    private void showAddrDialog(Way w) {
        AddressDialog dlg = new AddressDialog();
        if (!isAltDown) {
            dlg.showDialog();
            if (dlg.getValue() != 1)
                return;
        }
        dlg.saveValues();
        String tmp;
        tmp = dlg.getHouseNum();
        if (tmp != null && tmp != "")
            w.put("addr:housenumber", tmp);
        tmp = dlg.getStreetName();
        if (tmp != null && tmp != "")
            w.put("addr:street", tmp);
    }

    @Override
    public void enterMode() {
        super.enterMode();
        if (getCurrentDataSet() == null) {
            Main.map.selectSelectTool(false);
            return;
        }
        currCursor = cursorCrosshair;
        Main.map.mapView.addMouseListener(this);
        Main.map.mapView.addMouseMotionListener(this);
        Main.map.mapView.addTemporaryLayer(this);
        DataSet.addSelectionListener(this);
        updateSnap(getCurrentDataSet().getSelected());
        try {
            Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
        } catch (SecurityException ex) {
        }
    }

    @Override
    public void exitMode() {
        super.exitMode();
        Main.map.mapView.removeMouseListener(this);
        Main.map.mapView.removeMouseMotionListener(this);
        Main.map.mapView.removeTemporaryLayer(this);
        DataSet.removeSelectionListener(this);
        try {
            Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        } catch (SecurityException ex) {
        }
        if (mode != Mode.None)
            Main.map.mapView.repaint();
        mode = Mode.None;
    }

    public void cancelDrawing() {
        mode = Mode.None;
        if (Main.map == null || Main.map.mapView == null)
            return;
        Main.map.statusLine.setHeading(-1);
        Main.map.statusLine.setAngle(-1);
        building.reset();
        Main.map.mapView.repaint();
        updateStatusLine();
    }

    @Override
    public void eventDispatched(AWTEvent arg0) {
        if (!(arg0 instanceof KeyEvent))
            return;
        KeyEvent ev = (KeyEvent) arg0;
        int modifiers = ev.getModifiersEx();
        boolean isCtrlDown = (modifiers & KeyEvent.CTRL_DOWN_MASK) != 0;
        boolean isShiftDown = (modifiers & KeyEvent.SHIFT_DOWN_MASK) != 0;
        if (this.isCtrlDown != isCtrlDown || this.isShiftDown != isShiftDown) {
            this.isCtrlDown = isCtrlDown;
            this.isShiftDown = isShiftDown;
            processMouseEvent(null);
            updCursor();
            if (mode != Mode.None)
                Main.map.mapView.repaint();
        }
        isAltDown = (modifiers & KeyEvent.ALT_DOWN_MASK) != 0;

        if (ev.getKeyCode() == KeyEvent.VK_ESCAPE && ev.getID() == KeyEvent.KEY_PRESSED) {
            if (mode != Mode.None)
                ev.consume();

            cancelDrawing();
        }
    }

    private EastNorth getEastNorth() {
        Node n;
        if (isCtrlDown) {
            n = null;
        } else {
            n = Main.map.mapView.getNearestNode(mousePos, OsmPrimitive.isUsablePredicate);
        }
        if (n == null) {
            return latlon2eastNorth(Main.map.mapView.getLatLon(mousePos.x, mousePos.y));
        } else {
            return latlon2eastNorth(n.getCoor());
        }
    }

    private boolean isRectDrawing() {
        return building.isRectDrawing() && (!isShiftDown || ToolSettings.isBBMode());
    }

    private Mode modeDrawing() {
        EastNorth p = getEastNorth();
        if (isRectDrawing()) {
            building.setPlaceRect(p);
            return isShiftDown ? Mode.DrawingAngFix : Mode.None;
        } else {
            building.setPlace(p, ToolSettings.getWidth(), ToolSettings.getLenStep(), isShiftDown);
            Main.map.statusLine.setDist(building.getLength());
            return this.nextMode = ToolSettings.getWidth() == 0 ? Mode.DrawingWidth : Mode.None;
        }
    }

    private Mode modeDrawingWidth() {
        building.setWidth(getEastNorth());
        Main.map.statusLine.setDist(Math.abs(building.getWidth()));
        return Mode.None;
    }

    private Mode modeDrawingAngFix() {
        building.angFix(getEastNorth());
        return Mode.None;
    }

    private void processMouseEvent(MouseEvent e) {
        if (e != null) {
            mousePos = e.getPoint();
            isCtrlDown = e.isControlDown();
            isShiftDown = e.isShiftDown();
            isAltDown = e.isAltDown();
        }
        if (mode == Mode.None) {
            nextMode = Mode.None;
            return;
        }

        if (mode == Mode.Drawing) {
            nextMode = modeDrawing();
        } else if (mode == Mode.DrawingWidth) {
            nextMode = modeDrawingWidth();
        } else if (mode == Mode.DrawingAngFix) {
            nextMode = modeDrawingAngFix();
        } else
            throw new AssertionError("Invalid drawing mode");
    }

    @Override
    public void paint(Graphics2D g, MapView mv, Bounds bbox) {
        if (mode == Mode.None)
            return;
        if (building.getLength() == 0)
            return;

        g.setColor(selectedColor);
        g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        building.paint(g, mv);

        g.setStroke(new BasicStroke(1));

    }

    private void drawingStart(MouseEvent e) {
        mousePos = e.getPoint();
        drawStartPos = mousePos;

        Node n = Main.map.mapView.getNearestNode(mousePos, OsmPrimitive.isUsablePredicate);
        if (n == null) {
            building.setBase(latlon2eastNorth(Main.map.mapView.getLatLon(mousePos.x, mousePos.y)));
        } else {
            building.setBase(n);
        }
        mode = Mode.Drawing;
        updateStatusLine();
    }

    private void drawingAdvance(MouseEvent e) {
        processMouseEvent(e);
        if (this.mode != Mode.None && this.nextMode == Mode.None) {
            drawingFinish();
        } else {
            mode = this.nextMode;
            updateStatusLine();
        }
    }

    private void drawingFinish() {
        if (building.getLength() != 0) {
            Way w = building.create();
            if (w != null && ToolSettings.isUsingAddr())
                showAddrDialog(w);
            if (ToolSettings.isAutoSelect() &&
                    (Main.main.getCurrentDataSet().getSelected().isEmpty() || isShiftDown)) {
                Main.main.getCurrentDataSet().setSelected(w);
            }
        }
        cancelDrawing();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1)
            return;
        if (!Main.map.mapView.isActiveLayerDrawable())
            return;

        if (mode == Mode.None)
            drawingStart(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        processMouseEvent(e);
        updCursor();
        if (mode != Mode.None)
            Main.map.mapView.repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1)
            return;
        if (!Main.map.mapView.isActiveLayerDrawable())
            return;
        boolean dragged = true;
        if (drawStartPos != null)
            dragged = e.getPoint().distance(drawStartPos) > 10;
        drawStartPos = null;

        if (mode == Mode.Drawing && !dragged)
            return;
        if (mode == Mode.None)
            return;

        drawingAdvance(e);
    }

    private void updCursor() {
        if (mousePos == null)
            return;
        if (!Main.isDisplayingMapView())
            return;
        Node n = null;
        if (!isCtrlDown)
            n = Main.map.mapView.getNearestNode(mousePos, OsmPrimitive.isUsablePredicate);
        if (n != null) {
            setCursor(cursorJoinNode);
        } else {
            if (customCursor != null && (!isShiftDown || isRectDrawing()))
                setCursor(customCursor);
            else
                setCursor(cursorCrosshair);
        }

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (!Main.map.mapView.isActiveLayerDrawable())
            return;
        processMouseEvent(e);
        updCursor();
        if (mode != Mode.None)
            Main.map.mapView.repaint();
    }

    @Override
    public String getModeHelpText() {
        if (mode == Mode.None)
            return tr("Point on the corner of the building to start drawing");
        if (mode == Mode.Drawing)
            return tr("Point on opposite end of the building");
        if (mode == Mode.DrawingWidth)
            return tr("Set width of the building");
        return "";
    }

    @Override
    public boolean layerIsSupported(Layer l) {
        return l instanceof OsmDataLayer;
    }

    public void updateSnap(Collection<? extends OsmPrimitive> newSelection) {
        building.clearAngleSnap();
        // update snap only if selection isn't too big
        if (newSelection.size() <= 10) {
            LinkedList<Node> nodes = new LinkedList<Node>();
            LinkedList<Way> ways = new LinkedList<Way>();

            for (OsmPrimitive p : newSelection) {
                switch (p.getType()) {
                case NODE:
                    nodes.add((Node) p);
                    break;
                case WAY:
                    ways.add((Way) p);
                    break;
                }
            }

            building.addAngleSnap(nodes.toArray(new Node[0]));
            for (Way w : ways) {
                building.addAngleSnap(w);
            }
        }
        updateCustomCursor();
    }

    private void updateCustomCursor() {
        Double angle = building.getDrawingAngle();
        if (angle == null || !ToolSettings.isSoftCursor()) {
            customCursor = null;
            return;
        }
        final int R = 9; // crosshair outer radius
        final int r = 3; // crosshair inner radius
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        GeneralPath b = new GeneralPath();
        b.moveTo(16 - Math.cos(angle) * R, 16 - Math.sin(angle) * R);
        b.lineTo(16 - Math.cos(angle) * r, 16 - Math.sin(angle) * r);
        b.moveTo(16 + Math.cos(angle) * R, 16 + Math.sin(angle) * R);
        b.lineTo(16 + Math.cos(angle) * r, 16 + Math.sin(angle) * r);
        b.moveTo(16 + Math.sin(angle) * R, 16 - Math.cos(angle) * R);
        b.lineTo(16 + Math.sin(angle) * r, 16 - Math.cos(angle) * r);
        b.moveTo(16 - Math.sin(angle) * R, 16 + Math.cos(angle) * R);
        b.lineTo(16 - Math.sin(angle) * r, 16 + Math.cos(angle) * r);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(Color.WHITE);
        g.draw(b);

        g.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(Color.BLACK);
        g.draw(b);

        customCursor = Toolkit.getDefaultToolkit().createCustomCursor(img, new Point(16, 16), "custom crosshair");

        updCursor();
    }

    @Override
    public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
        updateSnap(newSelection);
    }
}
