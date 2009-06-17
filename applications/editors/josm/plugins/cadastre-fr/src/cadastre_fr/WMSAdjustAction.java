package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.gui.layer.Layer;

public class WMSAdjustAction extends MapMode implements
        MouseListener, MouseMotionListener{

    private static final long serialVersionUID = 1L;
    GeorefImage selectedImage;
    private ArrayList<WMSLayer> modifiedLayers = new ArrayList<WMSLayer>();
    WMSLayer selectedLayer;
    private boolean rasterMoved;
    private EastNorth prevEastNorth;
    enum Mode { moveXY, moveZ, rotate}
    private Mode mode = null;

    public WMSAdjustAction(MapFrame mapFrame) {
        super(tr("Adjust WMS"), "adjustxywms",
                        tr("Adjust the position of the WMS layer (raster images only)"), mapFrame,
                        ImageProvider.getCursor("normal", "move"));
    }

    @Override public void enterMode() {
        super.enterMode();
        Main.map.mapView.addMouseListener(this);
        Main.map.mapView.addMouseMotionListener(this);
        rasterMoved = false;
        /*/ FOR TEST
        for (Layer layer : Main.map.mapView.getAllLayers()) {
            if (layer.visible && layer instanceof WMSLayer) {
                WMSLayer wmsLayer = (WMSLayer)layer;
                wmsLayer.rotate(Math.PI/4);
            }
        }
        Main.map.mapView.repaint();*/
    }

    @Override public void exitMode() {
        super.exitMode();
        Main.map.mapView.removeMouseListener(this);
        Main.map.mapView.removeMouseMotionListener(this);
        if (rasterMoved && CacheControl.cacheEnabled) {
            int reply = JOptionPane.showConfirmDialog(null,
                    "Save the changes in cache ?",
                    "Update cache",
                    JOptionPane.YES_NO_OPTION);
            if (reply == JOptionPane.OK_OPTION) {
                saveModifiedLayers();
            }
        }
        modifiedLayers.clear();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1)
            return;
        boolean ctrl = (e.getModifiers() & ActionEvent.CTRL_MASK) != 0;
        // boolean alt = (e.getModifiers() & ActionEvent.ALT_MASK) != 0;
        boolean shift = (e.getModifiers() & ActionEvent.SHIFT_MASK) != 0;
        if (shift)
            mode = Mode.moveZ;
        else if (ctrl)
            mode = Mode.rotate;
        else
            mode = Mode.moveXY;
        for (Layer layer : Main.map.mapView.getAllLayers()) {
            if (layer.visible && layer instanceof WMSLayer && ((WMSLayer)layer).isRaster()) {
                prevEastNorth = Main.map.mapView.getEastNorth(e.getX(), e.getY());
                selectedLayer = ((WMSLayer) layer);
                selectedImage = selectedLayer.findImage(prevEastNorth);
                if (selectedImage != null) {
                    Main.map.mapView.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }
        }
    }

    @Override public void mouseDragged(MouseEvent e) {
        if(selectedImage != null && (mode == Mode.moveXY || mode == Mode.moveZ || mode == Mode.rotate)) {
            EastNorth newEastNorth = Main.map.mapView.getEastNorth(e.getX(),e.getY());
            if (mode == Mode.moveXY) {
                displace(prevEastNorth, newEastNorth);
            } else if (mode == Mode.moveZ) {
                resize(newEastNorth);
            } else if (mode == Mode.rotate) {
                rotate(prevEastNorth, newEastNorth);
            }
            rasterMoved = true;
            if (!modifiedLayers.contains(selectedLayer))
                modifiedLayers.add(selectedLayer);
            Main.map.mapView.repaint();
            prevEastNorth = newEastNorth;
        }
    }

    private void displace(EastNorth start, EastNorth end) {
        selectedLayer.displace(end.east()-start.east(), end.north()-start.north());
    }

    private void resize(EastNorth newEastNorth) {
        double dPrev = prevEastNorth.distance(selectedLayer.getRasterCenter().east(), selectedLayer.getRasterCenter().north());
        double dNew = newEastNorth.distance(selectedLayer.getRasterCenter().east(), selectedLayer.getRasterCenter().north());
        selectedLayer.resize(1 - dNew/dPrev);
    }

    private void rotate(EastNorth start, EastNorth end) {
        EastNorth pivot = selectedLayer.getRasterCenter();
        double startAngle = Math.atan2(start.east()-pivot.east(), start.north()-pivot.north());
        double endAngle = Math.atan2(end.east()-pivot.east(), end.north()-pivot.north());
        double rotationAngle = endAngle - startAngle;
        selectedLayer.rotate(rotationAngle);
    }

    @Override public void mouseReleased(MouseEvent e) {
        //Main.map.mapView.repaint();
        Main.map.mapView.setCursor(Cursor.getDefaultCursor());
        selectedImage = null;
        prevEastNorth = null;
        selectedLayer = null;
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
        for (WMSLayer wmsLayer : modifiedLayers) {
            wmsLayer.saveNewCache();
        }
    }
}
