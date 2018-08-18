// License: GPL. For details, see LICENSE file.
package CommandLine;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.AWTEvent;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Logging;

public class PointAction extends MapMode implements AWTEventListener {
    private final CommandLine parentPlugin;
    private final Cursor cursorCrosshair;
    private final Cursor cursorJoinNode;
    private Cursor currentCursor;
    private Point mousePos;
    private Node nearestNode;
    private final ArrayList<String> pointList;
    private boolean isCtrlDown;

    public PointAction(CommandLine parentPlugin) {
        super(null, "addsegment.png", null, ImageProvider.getCursor("crosshair", null));
        this.parentPlugin = parentPlugin;
        cursorCrosshair = ImageProvider.getCursor("crosshair", null);
        cursorJoinNode = ImageProvider.getCursor("crosshair", "joinnode");
        currentCursor = cursorCrosshair;
        nearestNode = null;
        pointList = new ArrayList<>();
    }

    @Override public void enterMode() {
        super.enterMode();
        if (getLayerManager().getEditDataSet() == null) {
            MainApplication.getMap().selectSelectTool(false);
            return;
        }
        currentCursor = cursorCrosshair;
        MainApplication.getMap().mapView.addMouseListener(this);
        MainApplication.getMap().mapView.addMouseMotionListener(this);
        try {
            Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
        } catch (SecurityException ex) {
            Logging.warn(ex);
        }
    }

    @Override public void exitMode() {
        super.exitMode();
        MainApplication.getMap().mapView.removeMouseListener(this);
        MainApplication.getMap().mapView.removeMouseMotionListener(this);
        try {
            Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        } catch (SecurityException ex) {
            Logging.warn(ex);
        }
    }

    @Override public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (isCtrlDown) {
                if (pointList.size() > 0) {
                    pointList.remove(pointList.size() - 1);
                    updateTextEdit();
                }
            } else {
                LatLon coor;
                if (nearestNode == null)
                    coor = MainApplication.getMap().mapView.getLatLon(e.getX(), e.getY());
                else
                    coor = nearestNode.getCoor();
                if (coor.isOutSideWorld()) {
                    JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("Can not draw outside of the world."));
                    return;
                }
                String point = String.valueOf(coor.getX()) + "," + String.valueOf(coor.getY());
                int maxInstances = parentPlugin.currentCommand.parameters.get(parentPlugin.currentCommand.currentParameterNum).maxInstances;
                if (maxInstances == 1) {
                    parentPlugin.loadParameter(point, true);
                } else {
                    if (pointList.size() < maxInstances || maxInstances == 0) {
                        pointList.add(point);
                        updateTextEdit();
                    } else
                        Logging.info("Maximum instances!");
                }
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (!MainApplication.getMap().mapView.isActiveLayerDrawable())
            return;
        processMouseEvent(e);
        updCursor();
        MainApplication.getMap().mapView.repaint();
    }

    @Override
    public void eventDispatched(AWTEvent arg0) {
        if (!(arg0 instanceof KeyEvent))
            return;
        KeyEvent ev = (KeyEvent) arg0;
        isCtrlDown = (ev.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0;
        if (ev.getKeyCode() == KeyEvent.VK_ESCAPE && ev.getID() == KeyEvent.KEY_PRESSED) {
            ev.consume();
            cancelDrawing();
        }
    }

    private void updCursor() {
        if (mousePos != null) {
            if (!MainApplication.isDisplayingMapView())
                return;
            nearestNode = MainApplication.getMap().mapView.getNearestNode(mousePos, OsmPrimitive::isUsable);
            if (nearestNode != null) {
                setCursor(cursorJoinNode);
            } else {
                setCursor(cursorCrosshair);
            }
        }
    }

    private void processMouseEvent(MouseEvent e) {
        if (e != null) {
            mousePos = e.getPoint();
        }
    }

    private void setCursor(final Cursor c) {
        if (currentCursor.equals(c))
            return;
        try {
            // We invoke this to prevent strange things from happening
            EventQueue.invokeLater(() -> {
                // Don't change cursor when mode has changed already
                if (!(MainApplication.getMap().mapMode instanceof PointAction))
                    return;
                MainApplication.getMap().mapView.setCursor(c);
            });
            currentCursor = c;
        } catch (Exception e) {
            Logging.warn(e);
        }
    }

    public void cancelDrawing() {
        if (!MainApplication.isDisplayingMapView())
            return;
        MapFrame map = MainApplication.getMap();
        map.statusLine.setHeading(-1);
        map.statusLine.setAngle(-1);
        map.mapView.repaint();
        updateStatusLine();
        parentPlugin.abortInput();
    }

    public String currentValue() {
        String out = "";
        boolean first = true;
        for (String point : pointList) {
            if (!first)
                out += ";";
            out += point;
            first = false;
        }
        return out;
    }

    private void updateTextEdit() {
        Parameter currentParameter = parentPlugin.currentCommand.parameters.get(parentPlugin.currentCommand.currentParameterNum);
        String prefix = tr(currentParameter.description);
        prefix += parentPlugin.commandSymbol;
        String value = currentValue();
        parentPlugin.textField.setText(prefix + value);
    }
}
