// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.photoadjust;

import java.awt.event.MouseEvent;
import java.util.List;

import org.openstreetmap.josm.data.ImageData;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.projection.ProjectionRegistry;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.geoimage.GeoImageLayer;
import org.openstreetmap.josm.gui.layer.geoimage.ImageEntry;
import org.openstreetmap.josm.gui.layer.geoimage.ImageViewerDialog;

/**
 * Class that does the actual work.
 */
public class PhotoAdjustWorker {

    private ImageEntry dragPhoto;
    private ImageData dragData;
    // Offset between center of the photo and point where it is
    // clicked.  This must be in pixels to maintain the same offset if
    // the photo is moved very far.
    private EastNorth dragOffset;
    private boolean centerViewIsDisabled = false;
    private boolean centerViewNeedsEnable = false;

    /**
     * Reset the worker.
     */
    public void reset() {
        dragPhoto = null;
        dragData = null;
        dragOffset = EastNorth.ZERO;
    }

    /**
     * Disable the "center view" button.  The map is moved instead of the
     * photo if the center view is enabled while a photo is moved.  The method
     * disables the center view to avoid such behavior.  Call
     * restoreCenterView() to restore the original state.
     */
    public synchronized void disableCenterView() {
        if (!centerViewIsDisabled && ImageViewerDialog.isCenterView()) {
            centerViewIsDisabled = true;
            centerViewNeedsEnable = ImageViewerDialog.setCentreEnabled(false);
        }
    }

    /**
     * Restore the center view state that was active before
     * disableCenterView() was called.
     */
    public synchronized void restoreCenterView() {
        if (centerViewIsDisabled) {
            if (centerViewNeedsEnable) {
                centerViewNeedsEnable = false;
                ImageViewerDialog.setCentreEnabled(true);
            }
            centerViewIsDisabled = false;
        }
    }

    /**
     * Mouse click handler.  Control+click changes the image direction if
     * there is a photo selected on the map.  Control+alt+click positions the
     * selected photo.  Click without shift or control checks if there is a
     * photo under the mouse and uses it for dragging.
     *
     * @param evt Mouse event from MouseAdapter mousePressed().
     * @param imageLayers List of GeoImageLayers to be considered.
     */
    public void doMousePressed(MouseEvent evt,
            List<GeoImageLayer> imageLayers) {
        reset();

        if (evt.getButton() == MouseEvent.BUTTON1
                && imageLayers != null && !imageLayers.isEmpty()) {
            // Check if modifier key is pressed and change to
            // image viewer photo if it is.
            final boolean isAlt = (evt.getModifiersEx() & MouseEvent.ALT_DOWN_MASK) == MouseEvent.ALT_DOWN_MASK;
            final boolean isCtrl = (evt.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK;
            final boolean isShift = (evt.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) == MouseEvent.SHIFT_DOWN_MASK;
            // ignore key press with shift, to not conflict with selection
            if (isShift) {
                return;
            }
            if (isAlt || isCtrl) {
                for (GeoImageLayer layer: imageLayers) {
                    if (layer.isVisible()) {
                        final List<ImageEntry> entries = layer.getImageData().getSelectedImages();
                        if (!entries.isEmpty()) {
                            // Change direction if control is pressed, position
                            // with control+alt.
                            //
                            // Combinations:
                            // A ... alt pressed
                            // C ... control pressed
                            // pos ... photo has a position set == is displayed on the map
                            // nopos ... photo has no position set
                            //
                            // C + A + pos: position at mouse
                            // C + A + nopos: position at mouse
                            // C + pos: change orientation
                            // C + nopos: ignored
                            for (ImageEntry img: entries) {
                                if (isCtrl && !isAlt) {
                                    if (img.getPos() != null) {
                                        changeDirection(img, layer.getImageData(), evt);
                                    }
                                } else if (isCtrl && isAlt) {
                                    movePhoto(img, layer.getImageData(), evt);
                                }
                                dragPhoto = img;
                            }
                            dragData = layer.getImageData();
                            break;
                        }
                    }
                }
            } else {
                // Start with the top layer.
                for (GeoImageLayer layer: imageLayers) {
                    if (layer.isVisible()) {
                        dragPhoto = layer.getPhotoUnderMouse(evt);
                        if (dragPhoto != null) {
                            dragData = layer.getImageData();
                            setDragOffset(dragPhoto, evt);
                            disableCenterView();
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Mouse release handler.
     *
     * @param evt Mouse event from MouseAdapter mouseReleased().
     */
    public void doMouseReleased(MouseEvent evt) {
        restoreCenterView();
    }

    /**
     * Mouse drag handler.  Changes direction or moves photo.
     *
     * @param evt Mouse event from MouseMotionAdapter mouseDragged().
     */
    public void doMouseDragged(MouseEvent evt) {
        if (dragData != null && dragPhoto != null) {
            if ((evt.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK) {
                if (dragData.isImageSelected(dragPhoto)) {
                    for (ImageEntry photo: dragData.getSelectedImages()) {
                        changeDirection(photo, dragData, evt);
                    }
                } else {
                    changeDirection(dragPhoto, dragData, evt);
                }
            } else {
                disableCenterView();
                final EastNorth startEN = dragPhoto.getPos().getEastNorth(ProjectionRegistry.getProjection()).subtract(dragOffset);
                final EastNorth currentEN = MainApplication.getMap().mapView.getEastNorth(evt.getX(), evt.getY());
                final EastNorth translation = currentEN.subtract(startEN);

                if (dragData.isImageSelected(dragPhoto)) {
                    for (ImageEntry photo: dragData.getSelectedImages()) {
                        translatePhoto(photo, translation);
                    }
                } else {
                    translatePhoto(dragPhoto, translation);
                }
                dragData.notifyImageUpdate();
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
        final EastNorth centerEN = photo.getPos().getEastNorth(ProjectionRegistry.getProjection());
        final EastNorth offsetEN = MainApplication.getMap().mapView.getEastNorth(evt.getX(), evt.getY());
        dragOffset = centerEN.subtract(offsetEN);
    }

    /**
     * Move the photo to the mouse position.
     *
     * @param photo The photo to move.
     * @param data ImageData of the photo.
     * @param evt Mouse event from one of the mouse adapters.
     */
    private void movePhoto(ImageEntry photo, ImageData data, MouseEvent evt) {
        LatLon newPos = MainApplication.getMap().mapView.getLatLon(evt.getX(), evt.getY());
        data.updateImagePosition(photo, newPos);
    }

    /**
     * Apply the given translation to the photo
     * @param photo The photo to move
     * @param translation the translation to apply
     */
    private void translatePhoto(ImageEntry photo, EastNorth translation) {
        final EastNorth startEN = photo.getPos().getEastNorth(ProjectionRegistry.getProjection());
        final EastNorth newPosEN = startEN.add(translation);
        final LatLon newPos = MainApplication.getMap().mapView.getProjection().eastNorth2latlon(newPosEN);
        photo.setPos(newPos);
        photo.flagNewGpsData();
    }

    /**
     * Set the image direction, i.e. let it point to where the mouse is.
     *
     * @param photo The photo to move.
     * @param data ImageData of the photo.
     * @param evt Mouse event from one of the mouse adapters.
     */
    private void changeDirection(ImageEntry photo, ImageData data,
            MouseEvent evt) {
        final LatLon photoLL = photo.getPos();
        if (photoLL == null) {
            // Direction cannot be set if image doesn't have a position.
            return;
        }
        final LatLon mouseLL = MainApplication.getMap().mapView.getLatLon(evt.getX(), evt.getY());
        // The projection doesn't matter here.
        double direction = photoLL.bearing(mouseLL) * 360.0 / 2.0 / Math.PI;
        if (direction < 0.0) {
            direction += 360.0;
        } else if (direction >= 360.0) {
            direction -= 360.0;
        }
        data.updateImageDirection(photo, direction);
        setDragOffset(photo, evt);
    }
}
