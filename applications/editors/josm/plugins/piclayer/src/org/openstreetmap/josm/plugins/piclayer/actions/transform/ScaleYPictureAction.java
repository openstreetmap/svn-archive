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
public class ScaleYPictureAction extends GenericPicTransformAction {

    /**
     * Constructor
     */
    public ScaleYPictureAction() {
        super(tr("PicLayer scale Y"), tr("Scaled by Y"), "scale_y", tr("Drag to scale the picture in the Y Axis"),
                ImageProvider.getCursor("crosshair", null));
    }

    @Override
    protected void doAction(MouseEvent e) {
        double centerY = MainApplication.getMap().mapView.getHeight()/2;
        double dy0 = Math.max(Math.abs(prevMousePoint.getY() - centerY), 10);
        double dy = Math.abs(e.getY() - centerY);
        double scaleY = Math.max(dy / dy0, 0.9);
        currentLayer.scalePictureBy(1.0, scaleY);
    }
}
