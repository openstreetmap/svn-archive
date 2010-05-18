package org.openstreetmap.josm.plugins.videomapping;

import java.awt.event.ActionEvent;

import org.openstreetmap.josm.actions.JosmAction;

public class MoveAction extends JosmAction {

	public void actionPerformed(ActionEvent e) {
		System.out.println(e.getActionCommand());

	}

}
