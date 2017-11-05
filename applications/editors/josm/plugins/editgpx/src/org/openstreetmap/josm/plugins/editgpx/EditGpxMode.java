/**
 * License: GPL. Copyright 2008. Martin Garbe (leo at running-sheep dot com)
 */
package org.openstreetmap.josm.plugins.editgpx;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerAddEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerOrderChangeEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerRemoveEvent;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.editgpx.data.EditGpxData;
import org.openstreetmap.josm.plugins.editgpx.data.EditGpxTrack;
import org.openstreetmap.josm.plugins.editgpx.data.EditGpxTrackSegment;
import org.openstreetmap.josm.plugins.editgpx.data.EditGpxWayPoint;

public class EditGpxMode extends MapMode implements LayerChangeListener {

    private static final long serialVersionUID = 7940589057093872411L;
    Point pointPressed;
    private final MapFrame mapFrame;
    Rectangle oldRect;
    transient EditGpxLayer currentEditLayer;

    /**
     * Constructs a new {@code EditGpxMode}.
     * @param mapFrame map frame
     */
    public EditGpxMode(MapFrame mapFrame) {
        super("editgpx", "editgpx_mode.png", tr("edit gpx tracks"), Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        this.mapFrame = mapFrame;
    }

    @Override
    public void enterMode() {
        super.enterMode();
        MainApplication.getMap().mapView.addMouseListener(this);
        MainApplication.getMap().mapView.addMouseMotionListener(this);
        MainApplication.getMap().mapView.getLayerManager().addLayerChangeListener(this);
        updateLayer();
    }

    @Override
    public void exitMode() {
        super.exitMode();
        MainApplication.getMap().mapView.removeMouseListener(this);
        MainApplication.getMap().mapView.removeMouseMotionListener(this);
        MainApplication.getMap().mapView.getLayerManager().removeLayerChangeListener(this);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        pointPressed = new Point(e.getPoint());
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if ( (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) ==  InputEvent.BUTTON1_DOWN_MASK) {
            //if button1 is hold, draw the rectangle.
            paintRect(pointPressed, e.getPoint());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1) {
            return;
        }

        requestFocusInMapView();

        Rectangle r = createRect(e.getPoint(), pointPressed);

        //go through nodes and mark the ones in the selection rect as deleted
        if (currentEditLayer != null) {
            Projection projection = Main.getProjection();
            for (EditGpxTrack track: currentEditLayer.data.getTracks()) {
                for (EditGpxTrackSegment segment: track.getSegments()) {
                    for (EditGpxWayPoint wayPoint: segment.getWayPoints()) {
                        Point p = MainApplication.getMap().mapView.getPoint(wayPoint.getCoor().getEastNorth(projection));
                        if (r.contains(p)) {
                            wayPoint.setDeleted(true);
                        }
                    }
                }
            }
        }
        oldRect = null;
        MainApplication.getMap().mapView.repaint();
    }

    /**
     * create rectangle out of two given corners
     */
    public Rectangle createRect(Point p1, Point p2) {
        int x,y,w,h;
        if (p1.x == p2.x && p1.y == p2.y) {
            //if p1 and p2 same points draw a small rectangle around them
            x = p1.x -1;
            y = p1.y -1;
            w = 3;
            h = 3;
        } else {
            if (p1.x < p2.x){
                x = p1.x;
                w = p2.x-p1.x;
            } else {
                x = p2.x;
                w = p1.x-p2.x;
            }
            if (p1.y < p2.y) {
                y = p1.y;
                h = p2.y-p1.y;
            } else {
                y = p2.y;
                h = p1.y-p2.y;
            }
        }
        return new Rectangle(x,y,w,h);
    }

    /**
     * Draw a selection rectangle on screen.
     */
    private void paintRect(Point p1, Point p2) {
        if (mapFrame != null) {
            Graphics g = mapFrame.getGraphics();

            Rectangle r = oldRect;
            if (r != null) {
                //overwrite old rct
                g.setXORMode(Color.BLACK);
                g.setColor(Color.WHITE);
                g.drawRect(r.x,r.y,r.width,r.height);
            }

            g.setXORMode(Color.BLACK);
            g.setColor(Color.WHITE);
            r = createRect(p1,p2);
            g.drawRect(r.x,r.y,r.width,r.height);
            oldRect = r;
        }
    }

    /**
     * create new layer, add listeners and try importing gpx data.
     */
    private void updateLayer() {

        List<EditGpxLayer> layers = MainApplication.getMap().mapView.getLayerManager().getLayersOfType(EditGpxLayer.class);
        currentEditLayer = layers.isEmpty() ? null : layers.get(0);

        if(currentEditLayer == null) {
            currentEditLayer = new EditGpxLayer(new EditGpxData());
            MainApplication.getLayerManager().addLayer(currentEditLayer);
            currentEditLayer.initializeImport();
        }
        MainApplication.getMap().mapView.repaint();
    }

    @Override
    public void layerAdded(LayerAddEvent e) {
        // Do nothing
    }

    @Override
    public void layerRemoving(LayerRemoveEvent e) {
        if (e.getRemovedLayer() instanceof EditGpxLayer) {
            currentEditLayer = null;
            if (MainApplication.getMap().mapMode instanceof EditGpxMode) {
                if (MainApplication.getMap().mapView.getLayerManager().getActiveLayer() instanceof OsmDataLayer) {
                    MainApplication.getMap().selectSelectTool(false);
                } else {
                    MainApplication.getMap().selectZoomTool(false);
                }
            }
        }
    }

    @Override
    public void layerOrderChanged(LayerOrderChangeEvent e) {
        // Do nothing
    }
}
