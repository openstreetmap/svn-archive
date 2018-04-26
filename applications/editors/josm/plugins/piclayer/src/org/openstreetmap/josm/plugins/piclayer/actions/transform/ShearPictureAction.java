// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.piclayer.actions.transform;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.MouseEvent;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.piclayer.actions.GenericPicTransformAction;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * This class handles the input during shearing of the picture.
 */
public class ShearPictureAction extends GenericPicTransformAction {

    /**
     * Constructor
     */
    public ShearPictureAction() {
        super(tr("PicLayer shear"), tr("Sheared"), "shear", tr("Drag to shear the picture"), ImageProvider.getCursor("crosshair", null));
    }

    @Override
    protected void doAction(MouseEvent e) {
        EastNorth eastNorth = MainApplication.getMap().mapView.getEastNorth(e.getX(), e.getY());
        currentLayer.shearPictureBy(
            1000* (eastNorth.east() - prevEastNorth.east()),
            1000* (eastNorth.north() - prevEastNorth.north())
        );
    }
}
