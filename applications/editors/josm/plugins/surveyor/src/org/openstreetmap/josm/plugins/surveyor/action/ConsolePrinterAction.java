// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.surveyor.action;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.plugins.surveyor.GpsActionEvent;

/**
 * @author cdaller
 *
 */
public class ConsolePrinterAction extends AbstractSurveyorAction {

    @Override
    public void actionPerformed(GpsActionEvent event) {
        LatLon coordinates = event.getCoordinates();
        System.out.println(getClass().getSimpleName() + " KOORD: " + coordinates.lat() + ", "
            + coordinates.lon() + " params: " + getParameters());
    }
}
