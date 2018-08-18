// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.commandline;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;

import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.preferences.NamedColorProperty;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.MapViewPaintable;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Logging;

public class LengthAction extends MapMode implements MapViewPaintable, AWTEventListener {
    private final CommandLine parentPlugin;
    private final Cursor cursorCrosshair;
    private final Cursor cursorJoinNode;
    private Cursor currentCursor;
    private final Color selectedColor;
    private Point drawStartPos;
    private Point drawEndPos;
    private LatLon startCoor;
    private LatLon endCoor;
    private Point mousePos;
    private Node nearestNode;
    private boolean drawing;

    public LengthAction(CommandLine parentPlugin) {
        super(null, "addsegment.png", null, ImageProvider.getCursor("crosshair", null));
        this.parentPlugin = parentPlugin;
        selectedColor = new NamedColorProperty(marktr("selected"), Color.red).get();
        cursorCrosshair = ImageProvider.getCursor("crosshair", null);
        cursorJoinNode = ImageProvider.getCursor("crosshair", "joinnode");
        currentCursor = cursorCrosshair;
        nearestNode = null;
    }

    @Override
    public void enterMode() {
        super.enterMode();
        MapView mapView = MainApplication.getMap().mapView;
        mapView.addMouseListener(this);
        mapView.addMouseMotionListener(this);
        mapView.addTemporaryLayer(this);
        try {
            Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
        } catch (SecurityException ex) {
            Logging.warn(ex);
        }
    }

    @Override
    public void exitMode() {
        super.exitMode();
        MapView mapView = MainApplication.getMap().mapView;
        mapView.removeMouseListener(this);
        mapView.removeMouseMotionListener(this);
        mapView.removeTemporaryLayer(this);
        try {
            Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        } catch (SecurityException ex) {
            Logging.warn(ex);
        }
        if (drawing)
            mapView.repaint();
    }

    public void cancelDrawing() {
        MapFrame map = MainApplication.getMap();
        if (map == null || map.mapView == null)
            return;
        map.statusLine.setHeading(-1);
        map.statusLine.setAngle(-1);
        updateStatusLine();
        parentPlugin.abortInput();
    }

    @Override
    public void eventDispatched(AWTEvent event) {
        if (!(event instanceof KeyEvent))
            return;
        KeyEvent ev = (KeyEvent) event;
        if (ev.getKeyCode() == KeyEvent.VK_ESCAPE && ev.getID() == KeyEvent.KEY_PRESSED) {
            if (drawing)
                ev.consume();
            cancelDrawing();
        }
    }

    private void processMouseEvent(MouseEvent e) {
        if (e != null) {
            mousePos = e.getPoint();
        }
    }

    @Override
    public void paint(Graphics2D g, MapView mv, Bounds bbox) {
        if (!drawing)
            return;

        g.setColor(selectedColor);
        g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        GeneralPath b = new GeneralPath();
        Point pp1 = drawStartPos;
        Point pp2 = drawEndPos;

        b.moveTo(pp1.x, pp1.y);
        b.lineTo(pp2.x, pp2.y);
        g.draw(b);

        g.setStroke(new BasicStroke(1));
    }

    private void drawingStart(MouseEvent e) {
        mousePos = e.getPoint();
        if (nearestNode != null) {
            drawStartPos = MainApplication.getMap().mapView.getPoint(nearestNode.getCoor());
        } else {
            drawStartPos = mousePos;
        }
        drawEndPos = drawStartPos;
        startCoor = MainApplication.getMap().mapView.getLatLon(drawStartPos.x, drawStartPos.y);
        endCoor = startCoor;
        drawing = true;
        updateStatusLine();
    }

    private void drawingFinish() {
        parentPlugin.loadParameter(String.valueOf(startCoor.greatCircleDistance(endCoor)), true);
        drawStartPos = null;
        drawing = false;
        exitMode();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (!MainApplication.getMap().mapView.isActiveLayerDrawable())
                return;
            requestFocusInMapView();
            drawingStart(e);
        } else
            drawing = false;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1)
            return;
        if (!MainApplication.getMap().mapView.isActiveLayerDrawable())
            return;
        boolean dragged = true;
        if (drawStartPos != null)
            dragged = drawEndPos.distance(drawStartPos) > 10;
            if (drawing && dragged)
                drawingFinish();
            drawing = false;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        processMouseEvent(e);
        updCursor();
        MapFrame map = MainApplication.getMap();
        if (nearestNode != null)
            drawEndPos = map.mapView.getPoint(nearestNode.getCoor());
        else
            drawEndPos = mousePos;
        endCoor = map.mapView.getLatLon(drawEndPos.x, drawEndPos.y);
        if (drawing) {
            map.statusLine.setDist(startCoor.greatCircleDistance(endCoor));
            map.mapView.repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (!MainApplication.getMap().mapView.isActiveLayerDrawable())
            return;
        processMouseEvent(e);
        updCursor();
        if (drawing)
            MainApplication.getMap().mapView.repaint();
    }

    @Override
    public String getModeHelpText() {
        if (drawing)
            return tr("Point on the start");
        else
            return tr("Point on the end");
    }

    @Override
    public boolean layerIsSupported(Layer l) {
        return l instanceof OsmDataLayer;
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

    private void setCursor(final Cursor c) {
        if (currentCursor.equals(c))
            return;
        try {
            // We invoke this to prevent strange things from happening
            EventQueue.invokeLater(() -> {
                // Don't change cursor when mode has changed already
                if (!(MainApplication.getMap().mapMode instanceof LengthAction))
                    return;
                MainApplication.getMap().mapView.setCursor(c);
            });
            currentCursor = c;
        } catch (Exception e) {
            Logging.warn(e);
        }
    }
}
