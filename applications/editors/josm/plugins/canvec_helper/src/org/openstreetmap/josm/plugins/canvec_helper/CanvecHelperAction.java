package org.openstreetmap.josm.plugins.canvec_helper;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.Main;

public class CanvecHelperAction extends JosmAction {
	private canvec_helper parent_temp;
	public CanvecHelperAction(canvec_helper parent) {
		super("CanVec Helper","layericon24",null,null,false);
		parent_temp = parent;
	}
	public void actionPerformed(java.awt.event.ActionEvent action) {
		canvec_layer layer;
		layer = new canvec_layer("canvec tile helper",parent_temp);
		Main.main.addLayer(layer);
	}
}
