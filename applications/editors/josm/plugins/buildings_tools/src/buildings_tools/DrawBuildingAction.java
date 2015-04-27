// License: GPL. For details, see LICENSE file.
package buildings_tools;

import static buildings_tools.BuildingsToolsPlugin.latlon2eastNorth;
import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.MapViewPaintable;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.util.KeyPressReleaseListener;
import org.openstreetmap.josm.gui.util.ModifierListener;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

@SuppressWarnings("serial")
public class DrawBuildingAction extends MapMode implements MapViewPaintable, SelectionChangedListener,
        KeyPressReleaseListener, ModifierListener {
    private enum Mode {
        None, Drawing, DrawingWidth, DrawingAngFix
    }

    private final Cursor cursorCrosshair;
    private final Cursor cursorJoinNode;
    private Cursor currCursor;
    private Cursor customCursor;

    private Mode mode = Mode.None;
    private Mode nextMode = Mode.None;

    private Color selectedColor = Color.red;
    private Point drawStartPos;
    private Point mousePos;

    final Building building = new Building();

    public DrawBuildingAction(MapFrame mapFrame) {
        super(tr("Draw buildings"), "building", tr("Draw buildings"),
                Shortcut.registerShortcut("mapmode:buildings",
                        tr("Mode: {0}", tr("Draw buildings")),
                        KeyEvent.VK_B, Shortcut.DIRECT),
                mapFrame, getCursor());

        cursorCrosshair = getCursor();
        cursorJoinNode = ImageProvider.getCursor("crosshair", "joinnode");
        currCursor = cursorCrosshair;
    }

    private static Cursor getCursor() {
        try {
            return ImageProvider.getCursor("crosshair", "building");
        } catch (Exception e) {
            Main.error(e);
        }
        return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    }

    /**
     * Displays the given cursor instead of the normal one.
     *
     * @param c One of the available cursors
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
            Main.error(e);
        }
    }

    private void showAddrDialog(Way w) {
        AddressDialog dlg = new AddressDialog();
        if (!alt) {
            dlg.showDialog();
            if (dlg.getValue() != 1)
                return;
        }
        dlg.saveValues();
        String tmp = dlg.getHouseNum();
        if (tmp != null && !tmp.isEmpty())
            w.put("addr:housenumber", tmp);
        tmp = dlg.getStreetName();
        if (tmp != null && !tmp.isEmpty())
            w.put("addr:street", tmp);
    }

    @Override
    public void enterMode() {
        super.enterMode();
        if (getCurrentDataSet() == null) {
            Main.map.selectSelectTool(false);
            return;
        }
        selectedColor = Main.pref.getColor(marktr("selected"), selectedColor);
        currCursor = cursorCrosshair;
        Main.map.mapView.addMouseListener(this);
        Main.map.mapView.addMouseMotionListener(this);
        Main.map.mapView.addTemporaryLayer(this);
        Main.map.keyDetector.addKeyListener(this);
        Main.map.keyDetector.addModifierListener(this);
        DataSet.addSelectionListener(this);
        updateSnap(getCurrentDataSet().getSelected());
    }

    @Override
    public void exitMode() {
        super.exitMode();
        Main.map.mapView.removeMouseListener(this);
        Main.map.mapView.removeMouseMotionListener(this);
        Main.map.mapView.removeTemporaryLayer(this);
        Main.map.keyDetector.removeKeyListener(this);
        Main.map.keyDetector.removeModifierListener(this);
        DataSet.removeSelectionListener(this);
        if (mode != Mode.None)
            Main.map.mapView.repaint();
        mode = Mode.None;
    }

    public final void cancelDrawing() {
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
    public void modifiersChanged(int modifiers) {
        boolean oldCtrl = ctrl;
        boolean oldShift = shift;
        updateKeyModifiers(modifiers);
        if (ctrl != oldCtrl || shift != oldShift) {
            processMouseEvent(null);
            updCursor();
            if (mode != Mode.None)
                Main.map.mapView.repaint();
        }
    }

    @Override
    public void doKeyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (mode != Mode.None)
                e.consume();

            cancelDrawing();
        }
    }

    @Override
    public void doKeyReleased(KeyEvent e) {
    }

    private EastNorth getEastNorth() {
        Node n;
        if (ctrl) {
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
        return building.isRectDrawing() && (!shift || ToolSettings.isBBMode());
    }

    private Mode modeDrawing() {
        EastNorth p = getEastNorth();
        if (isRectDrawing()) {
            building.setPlaceRect(p);
            return shift ? Mode.DrawingAngFix : Mode.None;
        } else {
            building.setPlace(p, ToolSettings.getWidth(), ToolSettings.getLenStep(), shift);
            Main.map.statusLine.setDist(building.getLength());
            this.nextMode = ToolSettings.getWidth() == 0 ? Mode.DrawingWidth : Mode.None;
            return this.nextMode;
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
            updateKeyModifiers(e);
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
        } else {
            throw new AssertionError("Invalid drawing mode");
        }
    }

    @Override
    public void paint(Graphics2D g, MapView mv, Bounds bbox) {
        if (mode == Mode.None || building.getLength() == 0) {
            return;
        }

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
            if (w != null) {
                if (!alt || ToolSettings.isUsingAddr())
                    for (Entry<String, String> kv : ToolSettings.getTags().entrySet())
                        w.put(kv.getKey(), kv.getValue());
                if (ToolSettings.isUsingAddr())
                    showAddrDialog(w);
                if (ToolSettings.isAutoSelect()
                        && (Main.main.getCurrentDataSet().getSelected().isEmpty() || shift)) {
                    Main.main.getCurrentDataSet().setSelected(w);
                }
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

        requestFocusInMapView();

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
        if (!ctrl)
            n = Main.map.mapView.getNearestNode(mousePos, OsmPrimitive.isUsablePredicate);
        if (n != null) {
            setCursor(cursorJoinNode);
        } else {
            if (customCursor != null && (!ctrl || isRectDrawing()))
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

    public final void updateSnap(Collection<? extends OsmPrimitive> newSelection) {
        building.clearAngleSnap();
        // update snap only if selection isn't too big
        if (newSelection.size() <= 10) {
            LinkedList<Node> nodes = new LinkedList<>();
            LinkedList<Way> ways = new LinkedList<>();

            for (OsmPrimitive p : newSelection) {
                switch (p.getType()) {
                case NODE:
                    nodes.add((Node) p);
                    break;
                case WAY:
                    ways.add((Way) p);
                    break;
                default:
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
