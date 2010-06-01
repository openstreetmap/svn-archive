package org.openstreetmap.josm.plugins.videomapping.actions;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.plugins.videomapping.GpsPlayer;
import org.openstreetmap.josm.plugins.videomapping.PositionLayer;
import org.openstreetmap.josm.tools.Shortcut;

import javax.swing.KeyStroke;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import static org.openstreetmap.josm.tools.I18n.tr;

public class StartStopAction extends JosmAction {
	private PositionLayer layer;
	
	public StartStopAction() {
		super("start/pause", "videomapping", "starts/pauses video playback",
				Shortcut.registerShortcut("videomapping:startstop","",KeyEvent.VK_SPACE, Shortcut.GROUP_MENU), false);
		setEnabled(false);
	}

	public void actionPerformed(ActionEvent arg0) {
		layer.pause();

	}


	public void setLayer(PositionLayer layer) {
		this.layer =layer;
		
	}

}
