/**
 * 
 */
package at.dallermassl.josm.plugin.surveyor;

import java.awt.event.ActionEvent;

import org.openstreetmap.josm.data.coor.LatLon;

/**
 * @author cdaller
 *
 */
public class GpsActionEvent extends ActionEvent {
    private LatLon coordinates;
    

    /**
     * @param e
     * @param latitude
     * @param longitude
     */
    public GpsActionEvent(ActionEvent e, double latitude, double longitude) {
        super(e.getSource(), e.getID(), e.getActionCommand(), e.getWhen(), e.getModifiers());
        coordinates = new LatLon(latitude, longitude);
    }


    /**
     * @return the coordinates
     */
    public LatLon getCoordinates() {
        return this.coordinates;
    }

 

}
