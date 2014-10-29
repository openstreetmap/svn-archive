package org.openstreetmap.josm.plugins.waypointSearch;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * This plugin enables a user to search for waypoint imported from a gpx file.
 * After the plugin is installed a new button is presented on the left side of the map window. 
 * Pressing this buttons open the search dialog on the right side.
 * Click on one of the search results/waypoints to move the map.
 */
public class WaypointSearchPlugin extends Plugin implements LayerChangeListener  {
    private SelectWaypointDialog waypointDialog;
    
    /**
     * Will be invoked by JOSM to bootstrap the plugin
     *
     * @param info  information about the plugin and its local installation    
     */
    public WaypointSearchPlugin(PluginInformation info) {
        super(info);
        MapView.addLayerChangeListener(this);
    }
     
    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (newFrame != null) {
            waypointDialog = new SelectWaypointDialog(
                    tr("Waypoint search"), "ToolbarIcon", 
                    tr("Search after waypoint. Click and move the map view to the waypoint."), null, 100);
            newFrame.addToggleDialog(waypointDialog);
        } else {
            waypointDialog = null;
        }
    }

    @Override
    public void activeLayerChange(Layer oldLayer, Layer newLayer) {
        // Do nothing
    }
    
    @Override
    public void layerAdded(Layer newLayer) {
        // update search
        if (waypointDialog != null && Engine.gpxLayersExist()) {
            waypointDialog.updateSearchResults();
        }
    }

    @Override
    public void layerRemoved(Layer oldLayer) {
        if (waypointDialog != null && !Engine.gpxLayersExist()) {
            waypointDialog.updateSearchResults();
        }   
    }
}
