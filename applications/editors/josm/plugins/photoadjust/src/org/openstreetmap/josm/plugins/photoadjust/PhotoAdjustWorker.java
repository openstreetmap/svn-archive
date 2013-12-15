package org.openstreetmap.josm.plugins.photoadjust;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.layer.geoimage.GeoImageLayer;
import org.openstreetmap.josm.gui.layer.geoimage.ImageEntry;
import org.openstreetmap.josm.gui.layer.geoimage.ImageViewerDialog;

/**
 * Class that does the actual work.
 */
public class PhotoAdjustWorker {

    private ImageEntry dragPhoto = null;
    private GeoImageLayer dragLayer = null;
    // Offset between center of the photo and point where it is
    // clicked.  This must be in pixels to maintain the same offset if
    // the photo is moved very far.
    private Point2D dragOffset = null;

    /**
     * Reset the worker.
     */
    public void reset() {
        dragPhoto = null;
        dragLayer = null;
        dragOffset = null;
    }

    /**
     * Mouse click handler.  Shift+click positions the photo from the
     * ImageViewerDialog.  Click without shift checks if there is a
     * photo under the mouse.
     *
     * @param evt Mouse event from MouseAdapter mousePressed().
     * @param imageLayers GeoImageLayer to be considered.
     */
    public void doMousePressed(MouseEvent evt,
                               List<GeoImageLayer> imageLayers) {
        reset();

        if (evt.getButton() == MouseEvent.BUTTON1
            && imageLayers != null && imageLayers.size() > 0) {
            // Check if modifier key was pressed and change to
            // image viewer photo if it was.
            if ((evt.getModifiers() & InputEvent.SHIFT_MASK) != 0) {
                GeoImageLayer viewerLayer = ImageViewerDialog.getCurrentLayer();
                ImageEntry img = ImageViewerDialog.getCurrentImage();
                if ( img != null && viewerLayer != null
                     && viewerLayer.isVisible()
                     && imageLayers.contains(viewerLayer)) {
                    img.setPos(Main.map.mapView.getLatLon(evt.getX(), evt.getY()));
                    img.flagNewGpsData();
                    viewerLayer.updateBufferAndRepaint();
                    // Need to re-display the photo because the
                    // OSD data might change (new coordinates).
                    ImageViewerDialog.showImage(viewerLayer, img);
                    dragPhoto = img;
                    dragLayer = viewerLayer;
                }
            }
            else {
                // Start with the top layer.
                for (GeoImageLayer layer: imageLayers) {
                    if (layer.isVisible()) {
                        dragPhoto = layer.getPhotoUnderMouse(evt);
                        if (dragPhoto != null) {
                            dragLayer = layer;
                            Point2D centerPoint = Main.map.mapView.getPoint2D(dragPhoto.getPos());
                            dragOffset = new Point2D.Double(centerPoint.getX() - evt.getX(),
                                                            centerPoint.getY() - evt.getY());
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Mouse drag handler.  Moves photo.
     *
     * @param evt Mouse event from MouseMotionAdapter mouseDragged().
     */
    public void doMouseDragged(MouseEvent evt) {
        if ( dragLayer != null && dragLayer.isVisible()
             && dragPhoto != null) {
            LatLon newPos;
            if (dragOffset != null) {
                newPos = Main.map.mapView.getLatLon(dragOffset.getX() + evt.getX(),
                                                    dragOffset.getY() + evt.getY());
            }
            else {
                newPos = Main.map.mapView.getLatLon(evt.getX(), evt.getY());
            }
            dragPhoto.setPos(newPos);
            dragPhoto.flagNewGpsData();
            dragLayer.updateBufferAndRepaint();
        }
    }
}
