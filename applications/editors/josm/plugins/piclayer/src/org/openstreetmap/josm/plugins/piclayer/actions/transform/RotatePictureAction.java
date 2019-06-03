// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.piclayer.actions.transform;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.piclayer.actions.GenericPicTransformAction;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * This class handles the input during rotating the picture.
 */
public class RotatePictureAction extends GenericPicTransformAction {

    /**
     * Constructor
     */
    public RotatePictureAction() {
        super(tr("PicLayer rotate"), tr("Rotated"), "rotate", tr("Drag to rotate the picture"),
                ImageProvider.getCursor("crosshair", null));
    }

    @Override
    protected void doAction(MouseEvent e) {
        Point2D center = new Point(MainApplication.getMap().mapView.getWidth()/2, MainApplication.getMap().mapView.getHeight()/2);
        double alpha1 = Math.atan2(e.getY() - center.getY(), e.getX() - center.getX());
        double alpha0 = Math.atan2(prevMousePoint.getY() - center.getY(), prevMousePoint.getX() - center.getX());
        currentLayer.rotatePictureBy(alpha1 - alpha0);
    }
}
