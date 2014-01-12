package org.openstreetmap.josm.plugins.waypointSearch;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class WaypointSearchPlugin extends Plugin implements LayerChangeListener  {
    /**
    * Will be invoked by JOSM to bootstrap the plugin
    *
    * @param info  information about the plugin and its local installation    
    */
    private final Engine engine = new Engine();
    private SelectWaypointDialog waypointDialog;
    
    public WaypointSearchPlugin(PluginInformation info) {
       super(info);
       MapView.addLayerChangeListener(this);
    }
     
	@Override
	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
		if (newFrame != null) {
			newFrame.addToggleDialog(waypointDialog = new SelectWaypointDialog(
					tr("Waypoint search"), "ToolbarIcon", tr("Search after waypoint. Click and move the map view to the waypoint."), null, 100));
		} else {
			waypointDialog = null;
		}
	}

	@Override
    public void activeLayerChange(Layer oldLayer, Layer newLayer) {
    }
    
    @Override
    public void layerAdded(Layer newLayer) {
        // update search
        if (waypointDialog != null && engine.gpxLayersExist()) {
            waypointDialog.updateSearchResults();
        }
    }

    @Override
    public void layerRemoved(Layer oldLayer) {
        if (waypointDialog != null && !engine.gpxLayersExist()) {
            waypointDialog.updateSearchResults();
        }   
    }
}
