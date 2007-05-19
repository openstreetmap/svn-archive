/**
 * 
 */
package at.dallermassl.josm.plugin.surveyor.action;

import java.util.List;

import javax.swing.AbstractButton;

import org.openstreetmap.josm.data.coor.LatLon;

import at.dallermassl.josm.plugin.surveyor.SurveyorAction;
import at.dallermassl.josm.plugin.surveyor.GpsActionEvent;

/**
 * @author cdaller
 *
 */
public class ConsolePrinterAction extends AbstractSurveyorAction {

    /* (non-Javadoc)
     * @see at.dallermassl.josm.plugin.surveyor.ButtonAction#actionPerformed(at.dallermassl.josm.plugin.surveyor.GpsActionEvent, java.util.List)
     */
    public void actionPerformed(GpsActionEvent event) {
        LatLon coordinates = event.getCoordinates();
        System.out.println(getClass().getSimpleName() + " KOORD: " + coordinates.lat() + ", " 
            + coordinates.lon() + " params: " + getParameters());
    }

}
