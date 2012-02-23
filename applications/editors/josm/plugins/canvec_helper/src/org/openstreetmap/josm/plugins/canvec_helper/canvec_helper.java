package org.openstreetmap.josm.plugins.canvec_helper;

import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.Main;

public class canvec_helper extends Plugin {
	public canvec_helper(PluginInformation info) {
		super(info);
		System.out.println("in constructor");
		Main.main.menu.imageryMenu.add(new CanvecHelperAction(this));
	}
	public void mapFrameInitialized(MapFrame old, MapFrame new1) {
		updateLayer();
	}
	private synchronized void updateLayer() {
	}
	private canvec_layer layer;
}
