/**
 * 
 */
package at.dallermassl.josm.plugin.surveyor.action;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JToggleButton;

import livegps.LiveGpsLock;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.markerlayer.Marker;
import org.openstreetmap.josm.gui.layer.markerlayer.MarkerLayer;

import at.dallermassl.josm.plugin.surveyor.GpsActionEvent;
import at.dallermassl.josm.plugin.surveyor.SurveyorAction;

/**
 * Action that sets a marker into a marker layer. The first parameter of the action
 * is used as the text of the marker, the second (if it exists) as an icon.
 * 
 * @author cdaller
 *
 */
public class SetWaypointAction extends AbstractSurveyorAction {
    private MarkerLayer markerLayer;
    public static final String MARKER_LAYER_NAME = "surveyorwaypointlayer";
    
    /**
     * Default Condstructor
     */
    public SetWaypointAction() {
        
    }


    /* (non-Javadoc)
     * @see at.dallermassl.josm.plugin.surveyor.ButtonAction#actionPerformed(at.dallermassl.josm.plugin.surveyor.GpsActionEvent, java.util.List)
     */
    public void actionPerformed(GpsActionEvent event) {
        LatLon coordinates = event.getCoordinates();
        System.out.println(getClass().getSimpleName() + " KOORD: " + coordinates.lat() + ", " + coordinates.lon());
        MarkerLayer layer = getGpsLayer();
        String markerTitle = getParameters().get(0);
        Object source = event.getSource();
        if(source instanceof JToggleButton) {
            if(((JToggleButton)source).isSelected()) {
                markerTitle = markerTitle + " " + tr("start");
            } else {
                markerTitle = markerTitle + " " + tr("end");                
            }
        }
        
        String iconName = getParameters().size() > 1 ? getParameters().get(1) : null;
        synchronized(LiveGpsLock.class) {
            layer.data.add(new Marker(event.getCoordinates(), markerTitle, iconName));
        }
        Main.map.repaint();
    }
    
    public MarkerLayer getGpsLayer() {
        if(markerLayer == null) {
            Collection<Layer> layers = Main.map.mapView.getAllLayers();
            for (Layer layer : layers) {
                if(MARKER_LAYER_NAME.equals(layer.name)) {
                    markerLayer = (MarkerLayer) layer;
                    break;
                }
            }
            // not found:
            if(markerLayer == null) {
                markerLayer = new MarkerLayer(new ArrayList<Marker>(), MARKER_LAYER_NAME, null);
                Main.main.addLayer(markerLayer);
            }
        }
        return markerLayer;
    }

}
