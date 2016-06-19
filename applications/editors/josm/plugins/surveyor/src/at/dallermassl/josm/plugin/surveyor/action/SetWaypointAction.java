/**
 * Copyright by Christof Dallermassl
 * This program is free software and licensed under GPL.
 */
package at.dallermassl.josm.plugin.surveyor.action;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Collection;

import javax.swing.JToggleButton;

import livegps.LiveGpsLayer;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.markerlayer.Marker;
import org.openstreetmap.josm.gui.layer.markerlayer.MarkerLayer;

import at.dallermassl.josm.plugin.surveyor.GpsActionEvent;
import at.dallermassl.josm.plugin.surveyor.SurveyorLock;
import at.dallermassl.josm.plugin.surveyor.SurveyorPlugin;
import at.dallermassl.josm.plugin.surveyor.action.gui.DialogClosingThread;
import at.dallermassl.josm.plugin.surveyor.action.gui.WaypointDialog;
import at.dallermassl.josm.plugin.surveyor.util.LayerUtil;

/**
 * Action that sets a marker into a marker layer. The first parameter of the action
 * is used as the text of the marker, the second (if it exists) as an icon.
 *
 * @author cdaller
 *
 */
public class SetWaypointAction extends AbstractSurveyorAction {
    private LiveGpsLayer liveGpsLayer;
    private MarkerLayer markerLayer;
    public static final String MARKER_LAYER_NAME = tr("Surveyor waypoint layer");
    private WaypointDialog dialog;

    /**
     * Default Constructor.
     */
    public SetWaypointAction() {

    }

    public void actionPerformed(GpsActionEvent event) {
        String markerTitle = getParameters().get(0);
        Object source = event.getSource();
        if(source instanceof JToggleButton) {
            if(((JToggleButton)source).isSelected()) {
                markerTitle = tr("{0} start", markerTitle);
            } else {
                markerTitle = tr("{0} end", markerTitle);
            }
        }
        
        String iconName = getParameters().size() > 1 ? getParameters().get(1).trim() : null;

        long timeout = DialogClosingThread.DEFAULT_TIMEOUT;
        if (getParameters().size() > 2) {
            try {
                timeout = Integer.parseInt(getParameters().get(2));
            } catch (NumberFormatException e) {
                Main.error(e.getMessage());
            }
        }
        
        String markerText = markerTitle;
        
        if (timeout > 0) {
            if (dialog == null) {
                dialog = new WaypointDialog();
            }
    
            String inputText = dialog.openDialog(SurveyorPlugin.getSurveyorFrame(), tr("Waypoint Description"), timeout*1000);
            if(inputText != null && inputText.length() > 0) {
                inputText = inputText.replaceAll("<", "_"); // otherwise the gpx file is ruined
                markerText = markerText + " " + inputText;
            }
        }

        // add the waypoint to the marker layer AND to the gpx layer
        // (easy export of data + waypoints):
        MarkerLayer layer = getMarkerLayer();
        GpxLayer gpsLayer = getGpxLayer();
        WayPoint waypoint = new WayPoint(event.getCoordinates());
        waypoint.attr.put("name", markerText);
        if(iconName != null && !iconName.isEmpty())
            waypoint.attr.put("sym", iconName);
        synchronized(SurveyorLock.class) {
            layer.data.add(new Marker(event.getCoordinates(), markerText, iconName, null, -1.0, 0.0));
            if(gpsLayer != null) {
                gpsLayer.data.waypoints.add(waypoint);
            }
        }

        Main.map.repaint();
    }

    /**
     * Returns the marker layer with the name {@link #MARKER_LAYER_NAME}.
     * @return the marker layer with the name {@link #MARKER_LAYER_NAME}.
     */
    public MarkerLayer getMarkerLayer() {
        if(markerLayer == null) {
            markerLayer = LayerUtil.findGpsLayer(MARKER_LAYER_NAME, MarkerLayer.class);

            if(markerLayer == null) {
                // not found, add a new one
                markerLayer = new MarkerLayer(new GpxData(), MARKER_LAYER_NAME, null, null);
                Main.main.addLayer(markerLayer);
            }
        }
        return markerLayer;
    }

    /**
     * Returns the gpx layer that is filled by the live gps data.
     * @return the gpx layer that is filled by the live gps data.
     */
    public GpxLayer getGpxLayer() {
        if(liveGpsLayer == null) {
            Collection<Layer> layers = Main.getLayerManager().getLayers();
            for (Layer layer : layers) {
                if(layer instanceof LiveGpsLayer) {
                    liveGpsLayer = (LiveGpsLayer) layer;
                    break;
                }
            }
        }
        return liveGpsLayer;
    }
}
