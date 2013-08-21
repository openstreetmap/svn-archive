package org.openstreetmap.josm.plugins.canvec_helper;

import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.Main;

public class CanvecHelper extends Plugin {
    
	public CanvecHelper(PluginInformation info) {
		super(info);
		Main.main.menu.imagerySubMenu.add(new CanvecHelperAction(this));
	}
	
    @Override
	public void mapFrameInitialized(MapFrame old, MapFrame new1) {
		updateLayer();
	}
    
	private synchronized void updateLayer() {
	}
}
