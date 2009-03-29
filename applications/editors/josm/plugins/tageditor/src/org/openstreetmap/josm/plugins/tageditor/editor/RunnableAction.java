package org.openstreetmap.josm.plugins.tageditor.editor;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public abstract class RunnableAction extends AbstractAction implements Runnable {

	public RunnableAction() {		
	}

	@Override
	public abstract void run();

	@Override
	public void actionPerformed(ActionEvent arg0) {
		run();		
	}
}
