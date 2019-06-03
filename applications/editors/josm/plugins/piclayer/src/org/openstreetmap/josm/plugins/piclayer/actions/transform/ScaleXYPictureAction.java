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
 * This class handles the input during scaling the picture.
 */
public class ScaleXYPictureAction extends GenericPicTransformAction {

    /**
     * Constructor
     */
    public ScaleXYPictureAction() {
        super(tr("PicLayer scale"), tr("Scaled"), "scale", tr("Drag to scale the picture in the X and Y Axis"),
                ImageProvider.getCursor("crosshair", null));
    }

    @Override
    protected void doAction(MouseEvent e) {
        double centerX = MainApplication.getMap().mapView.getWidth()/2;
        double centerY = MainApplication.getMap().mapView.getHeight()/2;
        double d0 = Math.max(prevMousePoint.distance(centerX, centerY), 10);
        Point2D mousePoint = new Point(e.getX(), e.getY());
        double d = mousePoint.distance(centerX, centerY);
        double scale = Math.max(d / d0, 0.9);
        currentLayer.scalePictureBy(scale, scale);
    }
}
