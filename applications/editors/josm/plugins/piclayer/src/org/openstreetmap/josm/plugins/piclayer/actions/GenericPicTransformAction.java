// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.piclayer.actions;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.piclayer.command.TransformCommand;
import org.openstreetmap.josm.plugins.piclayer.layer.PicLayerAbstract;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Superclass of transformation actions.
 */
public abstract class GenericPicTransformAction extends MapMode implements MouseListener, MouseMotionListener {

    protected boolean isDragging = false;
    protected PicLayerAbstract currentLayer = null;
    protected Point2D selectedPoint = null;
    protected EastNorth prevEastNorth = null;
    protected Point2D prevMousePoint = null;
    protected TransformCommand currentCommand = null;
    private String actionName;

    public GenericPicTransformAction(String name, String actionName, String iconName,
            String tooltip, Shortcut shortcut, MapFrame mapFrame, Cursor cursor) {
        super(name, iconName, tooltip, shortcut, cursor);
        this.actionName = actionName;
    }

    public GenericPicTransformAction(String name, String actionName, String iconName, String tooltip, Cursor cursor) {
        super(name, iconName, tooltip, cursor);
        this.actionName = actionName;
    }

    @Override
    public void enterMode() {
        super.enterMode();
        MainApplication.getMap().mapView.addMouseListener(this);
        MainApplication.getMap().mapView.addMouseMotionListener(this);
    }

    @Override
    public void exitMode() {
        super.exitMode();
        MainApplication.getMap().mapView.removeMouseListener(this);
        MainApplication.getMap().mapView.removeMouseMotionListener(this);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // Start action
        if (MainApplication.getLayerManager().getActiveLayer() instanceof PicLayerAbstract) {
            currentLayer = (PicLayerAbstract) MainApplication.getLayerManager().getActiveLayer();

            if (currentLayer != null && e.getButton() == MouseEvent.BUTTON1) {
                requestFocusInMapView();
                isDragging = true;
                prevMousePoint = new Point(e.getPoint());
                prevEastNorth = MainApplication.getMap().mapView.getEastNorth(e.getX(), e.getY());
                // try to find and fill selected point if possible
                selectedPoint = currentLayer.findSelectedPoint(e.getPoint());
                currentCommand = new TransformCommand(currentLayer, actionName);
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Call action performing
        if (isDragging && currentLayer != null) {
            doAction(e);
            prevMousePoint = new Point(e.getPoint());
            prevEastNorth = MainApplication.getMap().mapView.getEastNorth(e.getX(), e.getY());
            currentLayer.invalidate();
        }
    }

    protected abstract void doAction(MouseEvent e);

    @Override
    public void mouseReleased(MouseEvent e) {
        // End action
        isDragging = false;
        if (currentCommand != null)
            currentCommand.addIfChanged();
    }

    @Override
    public boolean layerIsSupported(Layer l) {
        return l instanceof PicLayerAbstract;
    }

    protected void updateDrawPoints(boolean value) {
        Layer active = MainApplication.getLayerManager().getActiveLayer();
        if (active instanceof PicLayerAbstract) {
            ((PicLayerAbstract) active).setDrawOriginPoints(value);
            active.invalidate();
        }
    }
}
