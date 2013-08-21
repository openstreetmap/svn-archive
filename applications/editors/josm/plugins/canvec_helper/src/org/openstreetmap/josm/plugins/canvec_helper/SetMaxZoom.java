package org.openstreetmap.josm.plugins.canvec_helper;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

class SetMaxZoom extends AbstractAction {
	private CanvecLayer parent;
	private int level;
	public SetMaxZoom(CanvecLayer parent,int level) {
		super(""+level);
		this.level = level;
		this.parent = parent;
	}
        @Override
	public void actionPerformed(ActionEvent ev) {
		parent.setMaxZoom(level);
	}
}
