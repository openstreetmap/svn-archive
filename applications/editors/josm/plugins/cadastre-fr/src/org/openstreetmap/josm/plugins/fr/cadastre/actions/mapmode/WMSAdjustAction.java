// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.actions.mapmode;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.plugins.fr.cadastre.wms.CacheControl;
import org.openstreetmap.josm.plugins.fr.cadastre.wms.WMSLayer;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Logging;

public class WMSAdjustAction extends MapMode implements
        MouseListener, MouseMotionListener {

    private static final long serialVersionUID = 1L;
    private WMSLayer modifiedLayer = null;
    private boolean rasterMoved;
    private EastNorth prevEastNorth;
    enum Mode { moveXY, moveZ, rotate }

    private static Mode mode = null;
    private static EastNorth[] croppedRaster = new EastNorth[5];;

    /**
     * Constructs a new {@code WMSAdjustAction} map mode.
     */
    public WMSAdjustAction() {
        super(tr("Adjust WMS"), "adjustxywms",
                        tr("Adjust the position of the WMS layer (saved for raster images only)"),
                        ImageProvider.getCursor("normal", "move"));
    }

    @Override
    public void enterMode() {
        if (MainApplication.getMap() != null) {
            if (MainApplication.getLayerManager().getActiveLayer() instanceof WMSLayer) {
                modifiedLayer = (WMSLayer) MainApplication.getLayerManager().getActiveLayer();
                super.enterMode();
                MainApplication.getMap().mapView.addMouseListener(this);
                MainApplication.getMap().mapView.addMouseMotionListener(this);
                rasterMoved = false;
                modifiedLayer.adjustModeEnabled = true;
            } else {
                // This mode works only if active layer is a cadastre layer
                if (Boolean.TRUE.equals(getValue("active"))) {
                    exitMode();
                }
                MainApplication.getMap().selectMapMode((MapMode) MainApplication.getMap().getDefaultButtonAction());
            }
        }
    }

    @Override
    public void exitMode() {
        try {
            super.exitMode();
        } catch (IllegalArgumentException e) {
            Logging.error(e);
        }
        MainApplication.getMap().mapView.removeMouseListener(this);
        MainApplication.getMap().mapView.removeMouseMotionListener(this);
        if (rasterMoved && CacheControl.cacheEnabled && modifiedLayer.isRaster()) {
            int reply = JOptionPane.showConfirmDialog(null,
                    "Save the changes in cache ?",
                    "Update cache",
                    JOptionPane.YES_NO_OPTION);
            if (reply == JOptionPane.OK_OPTION) {
                saveModifiedLayers();
            }
        }
        rasterMoved = false;
        if (modifiedLayer != null) {
            modifiedLayer.adjustModeEnabled = false;
            modifiedLayer = null;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1)
            return;
        requestFocusInMapView();
        boolean ctrl = (e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0;
        // boolean alt = (e.getModifiers() & ActionEvent.ALT_MASK) != 0;
        boolean shift = (e.getModifiers() & ActionEvent.SHIFT_MASK) != 0;
        if (shift && !ctrl && modifiedLayer.isRaster())
            mode = Mode.moveZ;
        else if (shift && ctrl && modifiedLayer.isRaster())
            mode = Mode.rotate;
        else
            mode = Mode.moveXY;
        rasterMoved = true;
        prevEastNorth = MainApplication.getMap().mapView.getEastNorth(e.getX(), e.getY());
        MainApplication.getMap().mapView.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }

    @Override public void mouseDragged(MouseEvent e) {
        EastNorth newEastNorth = MainApplication.getMap().mapView.getEastNorth(e.getX(), e.getY());
        if (mode == Mode.rotate) {
            rotateFrameOnly(prevEastNorth, newEastNorth);
        } else {
            if (mode == Mode.moveXY) {
                displace(prevEastNorth, newEastNorth);
            } else if (mode == Mode.moveZ) {
                resize(newEastNorth);
            }
            prevEastNorth = newEastNorth;
        }
        if (modifiedLayer != null) {
            modifiedLayer.invalidate();
        }
    }

    public static void paintAdjustFrames(Graphics2D g, final MapView mv) {
        if (mode == Mode.rotate && croppedRaster != null) {
            g.setColor(Color.red);
            for (int i = 0; i < 4; i++) {
                g.drawLine(mv.getPoint(croppedRaster[i]).x,
                        mv.getPoint(croppedRaster[i]).y,
                        mv.getPoint(croppedRaster[i+1]).x,
                        mv.getPoint(croppedRaster[i+1]).y);
            }
        }
    }

    private void displace(EastNorth start, EastNorth end) {
        modifiedLayer.displace(end.east()-start.east(), end.north()-start.north());
    }

    private void resize(EastNorth newEastNorth) {
        EastNorth center = modifiedLayer.getRasterCenter();
        double dPrev = prevEastNorth.distance(center.east(), center.north());
        double dNew = newEastNorth.distance(center.east(), center.north());
        modifiedLayer.resize(center, dNew/dPrev);
    }

    private void rotate(EastNorth start, EastNorth end) {
        EastNorth pivot = modifiedLayer.getRasterCenter();
        double startAngle = Math.atan2(start.east()-pivot.east(), start.north()-pivot.north());
        double endAngle = Math.atan2(end.east()-pivot.east(), end.north()-pivot.north());
        double rotationAngle = endAngle - startAngle;
        modifiedLayer.rotate(pivot, rotationAngle);
    }

    private void rotateFrameOnly(EastNorth start, EastNorth end) {
        if (start != null && end != null) {
            EastNorth pivot = modifiedLayer.getRasterCenter();
            double startAngle = Math.atan2(start.east()-pivot.east(), start.north()-pivot.north());
            double endAngle = Math.atan2(end.east()-pivot.east(), end.north()-pivot.north());
            double rotationAngle = endAngle - startAngle;
            if (modifiedLayer.getImage(0).orgCroppedRaster != null) {
                for (int i = 0; i < 4; i++) {
                    croppedRaster[i] = modifiedLayer.getImage(0).orgCroppedRaster[i].rotate(pivot, rotationAngle);
                }
                croppedRaster[4] = croppedRaster[0];
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (mode == Mode.rotate) {
            EastNorth newEastNorth = MainApplication.getMap().mapView.getEastNorth(e.getX(), e.getY());
            rotate(prevEastNorth, newEastNorth);
            if (modifiedLayer != null) {
                modifiedLayer.invalidate();
            }
        }
        MainApplication.getMap().mapView.setCursor(Cursor.getDefaultCursor());
        prevEastNorth = null;
        mode = null;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override public void mouseClicked(MouseEvent e) {
    }

    private void saveModifiedLayers() {
        modifiedLayer.grabThread.saveNewCache();
    }
}
