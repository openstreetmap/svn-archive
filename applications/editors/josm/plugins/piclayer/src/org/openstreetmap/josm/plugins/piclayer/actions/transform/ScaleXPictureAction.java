// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.piclayer.actions.transform;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.MouseEvent;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.piclayer.actions.GenericPicTransformAction;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * This class handles the input during scaling the picture.
 */
public class ScaleXPictureAction extends GenericPicTransformAction {

    /**
     * Constructor
     */
    public ScaleXPictureAction() {
        super(tr("PicLayer scale X"), tr("Scaled by X"), "scale_x", tr("Drag to scale the picture in the X Axis"),
                ImageProvider.getCursor("crosshair", null));
    }

    @Override
    protected void doAction(MouseEvent e) {
        double centerX = MainApplication.getMap().mapView.getWidth()/2;
        double dx0 = Math.max(Math.abs(prevMousePoint.getX() - centerX), 10);
        double dx = Math.abs(e.getX() - centerX);
        double scaleX = Math.max(dx / dx0, 0.9);
        currentLayer.scalePictureBy(scaleX, 1.0);
    }
}
