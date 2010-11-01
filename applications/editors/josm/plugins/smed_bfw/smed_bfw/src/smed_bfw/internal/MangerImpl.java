package smed_bfw.internal;

import javax.swing.JTabbedPane;

import aQute.bnd.annotation.component.Component;

import smed_bfw.api.IManager;

@Component
public class MangerImpl implements IManager {
	
	JTabbedPane pane = null;

	@Override
	public JTabbedPane getTabbedPane() {
		if(pane == null) pane = new JTabbedPane();
		
		return pane;
	}

}
