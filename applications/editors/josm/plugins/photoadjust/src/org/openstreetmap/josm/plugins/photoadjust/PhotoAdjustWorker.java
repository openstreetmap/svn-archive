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
     * Mouse click handler.  Control+click changes the image direction if
     * there is a photo selected on the map.  Shift+click positions the photo
     * from the ImageViewerDialog.  Click without shift or control checks if
     * there is a photo under the mouse.
     *
     * @param evt Mouse event from MouseAdapter mousePressed().
     * @param imageLayers List of GeoImageLayers to be considered.
     */
    public void doMousePressed(MouseEvent evt,
                               List<GeoImageLayer> imageLayers) {
        reset();

        if (evt.getButton() == MouseEvent.BUTTON1
            && imageLayers != null && imageLayers.size() > 0) {
            // Check if modifier key is pressed and change to
            // image viewer photo if it is.
            final boolean isShift = (evt.getModifiers() & InputEvent.SHIFT_MASK) != 0;
            final boolean isCtrl = (evt.getModifiers() & InputEvent.CTRL_MASK) != 0;
            if (isShift || isCtrl) {
                final GeoImageLayer viewerLayer = ImageViewerDialog.getCurrentLayer();
                final ImageEntry img = ImageViewerDialog.getCurrentImage();
                if ( img != null && viewerLayer != null
                     && viewerLayer.isVisible()
                     && imageLayers.contains(viewerLayer)) {
                    // Change direction if control is pressed, position
                    // otherwise.  Shift+control changes direction, similar to
                    // rotate in select mode.
                    //
                    // Combinations:
                    // S ... shift pressed
                    // C ... control pressed
                    // pos ... photo has a position set == is displayed on the map
                    // nopos ... photo has no position set
                    //
                    // S + pos: position at mouse
                    // S + nopos: position at mouse
                    // C + pos: change orientation
                    // C + nopos: ignored
                    // S + C + pos: change orientation
                    // S + C + nopos: ignore
                    if (isCtrl) {
                        if (img.getPos() != null) {
                            changeDirection(img, viewerLayer, evt);
                        }
                    }
                    else { // shift pressed
                        movePhoto(img, viewerLayer, evt);
                    }
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
                            setDragOffset(dragPhoto, evt);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Mouse drag handler.  Changes direction or moves photo.
     *
     * @param evt Mouse event from MouseMotionAdapter mouseDragged().
     */
    public void doMouseDragged(MouseEvent evt) {
        if ( dragLayer != null && dragLayer.isVisible()
             && dragPhoto != null) {
            if ((evt.getModifiers() & InputEvent.CTRL_MASK) != 0) {
                changeDirection(dragPhoto, dragLayer, evt);
            }
            else {
                movePhoto(dragPhoto, dragLayer, evt);
            }
        }
    }

    /**
     * Set the offset between a photo and the current mouse position.
     *
     * @param photo The photo to move.
     * @param evt Mouse event from one of the mouse adapters.
     */
    private void setDragOffset(ImageEntry photo, MouseEvent evt) {
        final Point2D centerPoint = Main.map.mapView.getPoint2D(photo.getPos());
        dragOffset = new Point2D.Double(centerPoint.getX() - evt.getX(),
                                        centerPoint.getY() - evt.getY());
    }

    /**
     * Move the photo to the mouse position.
     *
     * @param photo The photo to move.
     * @param layer GeoImageLayer of the photo.
     * @param evt Mouse event from one of the mouse adapters.
     */
    private void movePhoto(ImageEntry photo, GeoImageLayer layer,
                           MouseEvent evt) {
        LatLon newPos;
        if (dragOffset != null) {
            newPos = Main.map.mapView.getLatLon(dragOffset.getX() + evt.getX(),
                                                dragOffset.getY() + evt.getY());
        }
        else {
            newPos = Main.map.mapView.getLatLon(evt.getX(), evt.getY());
        }
        photo.setPos(newPos);
        photo.flagNewGpsData();
        layer.updateBufferAndRepaint();
        // Need to re-display the photo because the OSD data might change (new
        // coordinates).
        ImageViewerDialog.showImage(viewerLayer, img);
    }

    /**
     * Set the image direction, i.e. let it point to where the mouse is.
     *
     * @param photo The photo to move.
     * @param layer GeoImageLayer of the photo.
     * @param evt Mouse event from one of the mouse adapters.
     */
    private void changeDirection(ImageEntry photo, GeoImageLayer layer,
                                MouseEvent evt) {
        final LatLon photoLL = photo.getPos();
        final LatLon mouseLL = Main.map.mapView.getLatLon(evt.getX(), evt.getY());
        // The projection doesn't matter here.
        double direction = 360.0 - photoLL.heading(mouseLL) * 360.0 / 2.0 / Math.PI;
        if (direction < 0.0) {
            direction += 360.0;
        } else if (direction >= 360.0) {
            direction -= 360.0;
        }
        photo.setExifImgDir(direction);
        photo.flagNewGpsData();
        layer.updateBufferAndRepaint();
        ImageViewerDialog.showImage(layer, photo);
        setDragOffset(photo, evt);
    }
}
