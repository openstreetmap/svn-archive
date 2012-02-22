package org.openstreetmap.josm.plugins.canvec_helper;

import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.Main;

public class canvec_helper extends Plugin {
	public canvec_helper(PluginInformation info) {
		super(info);
		System.out.println("in constructor");
	}
	public void mapFrameInitialized(MapFrame old, MapFrame new1) {
		System.out.println("mapFrame made!");
		updateLayer();
	}
	private synchronized void updateLayer() {
		if(layer == null) {
			layer = new canvec_layer("canvec tile helper",this);
			Main.main.addLayer(layer);
		}
	}
	private canvec_layer layer;
}
