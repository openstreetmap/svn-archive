package org.openstreetmap.josm.plugins.turnrestrictions;

import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class TurnRestrictionsPlugin extends Plugin{
	public TurnRestrictionsPlugin(PluginInformation info) {
		super(info);
	}
	
	/**
	 * Called when the JOSM map frame is created or destroyed. 
	 */
	@Override
	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {				
		if (oldFrame == null && newFrame != null) { // map frame added
			TurnRestrictionsListDialog dialog  = new TurnRestrictionsListDialog();
			// add the dialog
			newFrame.addToggleDialog(dialog);
		}
	}
}
