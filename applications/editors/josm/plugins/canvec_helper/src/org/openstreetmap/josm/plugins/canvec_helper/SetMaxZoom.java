package org.openstreetmap.josm.plugins.canvec_helper;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

class SetMaxZoom extends AbstractAction {
	private canvec_layer parent;
	private int level;
	public SetMaxZoom(canvec_layer parent,int level) {
		super(""+level);
		this.level = level;
		this.parent = parent;
	}
	public void actionPerformed(ActionEvent ev) {
		parent.setMaxZoom(level);
	}
}
