// License: GPL. v2 and later. Copyright 2008-2009 by Pieren <pieren3@gmail.com> and others
package cadastre_fr;

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

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.tools.ImageProvider;

public class WMSAdjustAction extends MapMode implements
        MouseListener, MouseMotionListener{

    private static final long serialVersionUID = 1L;
    private WMSLayer modifiedLayer = null;
    private boolean rasterMoved;
    private EastNorth prevEastNorth;
    enum Mode { moveXY, moveZ, rotate}
    private static Mode mode = null;
    private static EastNorth[] croppedRaster = new EastNorth[5];;

    public WMSAdjustAction(MapFrame mapFrame) {
        super(tr("Adjust WMS"), "adjustxywms",
                        tr("Adjust the position of the WMS layer (saved for raster images only)"), mapFrame,
                        ImageProvider.getCursor("normal", "move"));
    }

    @Override public void enterMode() {
        if (Main.map != null) {
            if (Main.map.mapView.getActiveLayer() instanceof WMSLayer) {
                modifiedLayer = (WMSLayer)Main.map.mapView.getActiveLayer();
                super.enterMode();
                Main.map.mapView.addMouseListener(this);
                Main.map.mapView.addMouseMotionListener(this);
                rasterMoved = false;
                modifiedLayer.adjustModeEnabled = true;
            } else {
//                JOptionPane.showMessageDialog(Main.parent,tr("This mode works only if active layer is\n"
//                        +"a cadastre layer"));
                exitMode();
                Main.map.selectMapMode((MapMode)Main.map.getDefaultButtonAction());
            }
        }
    }

    @Override public void exitMode() {
        super.exitMode();
        Main.map.mapView.removeMouseListener(this);
        Main.map.mapView.removeMouseMotionListener(this);
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
        prevEastNorth = Main.map.mapView.getEastNorth(e.getX(), e.getY());
        Main.map.mapView.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }

    @Override public void mouseDragged(MouseEvent e) {
        EastNorth newEastNorth = Main.map.mapView.getEastNorth(e.getX(),e.getY());
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
        Main.map.mapView.repaint();
    }
    
    public static void paintAdjustFrames(Graphics2D g, final MapView mv) {
        if (mode == Mode.rotate && croppedRaster != null) {
            g.setColor(Color.red);
            for (int i=0; i<4; i++)
                g.drawLine(mv.getPoint(croppedRaster[i]).x,
                        mv.getPoint(croppedRaster[i]).y,
                        mv.getPoint(croppedRaster[i+1]).x,
                        mv.getPoint(croppedRaster[i+1]).y);
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
                for (int i=0; i<4; i++) {
                    croppedRaster[i] = modifiedLayer.getImage(0).orgCroppedRaster[i].rotate(pivot, rotationAngle);
                }
                croppedRaster[4] = croppedRaster[0];
            }
        }
    }

    @Override public void mouseReleased(MouseEvent e) {
        //Main.map.mapView.repaint();
        if (mode == Mode.rotate) {
            EastNorth newEastNorth = Main.map.mapView.getEastNorth(e.getX(),e.getY());
            rotate(prevEastNorth, newEastNorth);
            Main.map.mapView.repaint();
        }
        Main.map.mapView.setCursor(Cursor.getDefaultCursor());
        prevEastNorth = null;
        mode = null;
    }

    public void mouseEntered(MouseEvent e) {
    }
    public void mouseExited(MouseEvent e) {
    }
    public void mouseMoved(MouseEvent e) {
    }

    @Override public void mouseClicked(MouseEvent e) {
    }

    private void saveModifiedLayers() {
            modifiedLayer.grabThread.saveNewCache();
    }
}
