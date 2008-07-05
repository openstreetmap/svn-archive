package org.openstreetmap.josm.plugins.openLayers;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;

public class ShowOpenLayersAction extends JosmAction {

    public ShowOpenLayersAction(String name) {
	super(name, "OpenLayers", "Show layer" + name, 0, 0, false);
    }

    public void actionPerformed(ActionEvent e) {
	final OpenLayersLayer layer = OpenLayersPlugin.layer != null ? OpenLayersPlugin.layer : new OpenLayersLayer();
	OpenLayersPlugin.layer = layer;
	Main.main.addLayer(layer);
	
	EventQueue.invokeLater(new Runnable() {
	    public void run() {
		layer.setSize(Main.map.mapView.getSize());
	    }
	});
	    
	// Get notifications of scale and position
	Main.map.mapView.addPropertyChangeListener("scale", layer);
	Main.map.mapView.addPropertyChangeListener("center", layer);
	
    }
};
