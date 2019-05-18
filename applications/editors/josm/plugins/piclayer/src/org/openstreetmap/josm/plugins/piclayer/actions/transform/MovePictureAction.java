// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.piclayer.actions.transform;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.MouseEvent;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.piclayer.actions.GenericPicTransformAction;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * This class handles the input during moving the picture.
 */
public class MovePictureAction extends GenericPicTransformAction {

    /**
     * Constructor
     */
    public MovePictureAction() {
        super(tr("PicLayer move"), tr("Moved"), "move", tr("Drag to move the picture"),
                ImageProvider.getCursor("crosshair", null));
    }

    @Override
    protected void doAction(MouseEvent e) {
        EastNorth eastNorth = MainApplication.getMap().mapView.getEastNorth(e.getX(), e.getY());
        currentLayer.movePictureBy(
            eastNorth.east() - prevEastNorth.east(),
            eastNorth.north() - prevEastNorth.north()
        );
    }

}
