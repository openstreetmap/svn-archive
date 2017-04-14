// License: GPL. For details, see LICENSE file.
package CommandLine;

import java.awt.AWTEvent;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.tools.ImageProvider;

public abstract class AbstractOsmAction<T extends OsmPrimitive> extends MapMode implements AWTEventListener {
    private final CommandLine parentPlugin;
    private final Cursor cursorNormal;
    private final Cursor cursorActive;
    private Cursor currentCursor;
    private Point mousePos;
    private T nearestPrimitive;
    private boolean isCtrlDown;

    protected AbstractOsmAction(CommandLine parentPlugin, String activeCursorIcon) {
        super(null, "addsegment.png", null, ImageProvider.getCursor("normal", "selection"));
        this.parentPlugin = parentPlugin;
        cursorNormal = ImageProvider.getCursor("normal", "selection");
        cursorActive = ImageProvider.getCursor("normal", activeCursorIcon);
        currentCursor = cursorNormal;
        nearestPrimitive = null;
    }

    @Override public void enterMode() {
        super.enterMode();
        currentCursor = cursorNormal;
        Main.map.mapView.addMouseListener(this);
        Main.map.mapView.addMouseMotionListener(this);
        try {
            Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
        } catch (SecurityException ex) {
            Main.warn(ex);
        }
    }

    @Override public void exitMode() {
        super.exitMode();
        Main.map.mapView.removeMouseListener(this);
        Main.map.mapView.removeMouseMotionListener(this);
        try {
            Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        } catch (SecurityException ex) {
            Main.warn(ex);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (!Main.map.mapView.isActiveLayerDrawable())
            return;
        processMouseEvent(e);
        updCursor();
        Main.map.mapView.repaint();
        super.mouseMoved(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!Main.map.mapView.isActiveLayerDrawable())
            return;
        processMouseEvent(e);
        if (nearestPrimitive != null) {
            DataSet ds = Main.getLayerManager().getEditDataSet();
            if (isCtrlDown) {
                ds.clearSelection(nearestPrimitive);
                Main.map.mapView.repaint();
            } else {
                int maxInstances = parentPlugin.currentCommand.parameters.get(parentPlugin.currentCommand.currentParameterNum).maxInstances;
                switch (maxInstances) {
                case 0:
                    ds.addSelected(nearestPrimitive);
                    Main.map.mapView.repaint();
                    break;
                case 1:
                    ds.addSelected(nearestPrimitive);
                    Main.map.mapView.repaint();
                    parentPlugin.loadParameter(nearestPrimitive, true);
                    Main.map.selectSelectTool(false);
                    break;
                default:
                    if (ds.getSelected().size() < maxInstances) {
                        ds.addSelected(nearestPrimitive);
                        Main.map.mapView.repaint();
                    } else
                        parentPlugin.printHistory("Maximum instances is " + maxInstances);
                }
            }
        }
        super.mousePressed(e);
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
            if (!Main.isDisplayingMapView())
                return;
            nearestPrimitive = getNearest(mousePos);
            if (nearestPrimitive != null) {
                setCursor(cursorActive);
            } else {
                setCursor(cursorNormal);
            }
        }
    }

    protected abstract T getNearest(Point mousePos);

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
                if (!AbstractOsmAction.this.getClass().isAssignableFrom(Main.map.mapMode.getClass()))
                    return;
                Main.map.mapView.setCursor(c);
            });
            currentCursor = c;
        } catch (Exception e) {
            Main.warn(e);
        }
    }

    public void cancelDrawing() {
        if (Main.map == null || Main.map.mapView == null)
            return;
        Main.map.statusLine.setHeading(-1);
        Main.map.statusLine.setAngle(-1);
        Main.map.mapView.repaint();
        updateStatusLine();
        parentPlugin.abortInput();
    }
}
