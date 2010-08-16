package org.openstreetmap.josm.plugins.waypointSearch;

import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import static org.openstreetmap.josm.tools.I18n.tr;

public class WaypointSearchPlugin extends Plugin implements LayerChangeListener  {
	/**
    * Will be invoked by JOSM to bootstrap the plugin
    *
    * @param info  information about the plugin and its local installation    
    */
	private Engine engine = new Engine();
	private SelectWaypointDialog waypointDialog = new SelectWaypointDialog(tr("Waypoint search"), "ToolbarIcon", tr("Search after waypoint. Click and move the map view to the waypoint."), null, 100);
	
    public WaypointSearchPlugin(PluginInformation info) {
       super(info);
       MapView.addLayerChangeListener(this);
    }
     
	@Override
	public void activeLayerChange(Layer oldLayer, Layer newLayer) {
		// TODO Auto-generated method stub
		
	}

	
	@Override
	public void layerAdded(Layer newLayer) {
		//add dialog
		if (Main.map.getToggleDialog(SelectWaypointDialog.class)==null) {
			Main.map.addToggleDialog(waypointDialog);
		}
		//Enable to menu
		if (engine.gpxLayersExist()) {
			waypointDialog.updateSearchResults();
		}
	}



	@Override
	public void layerRemoved(Layer oldLayer) {
		if (!engine.gpxLayersExist()) {
			waypointDialog.updateSearchResults();
		}	
	}
 
}



    
    
    

















    
    






