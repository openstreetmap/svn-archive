package org.openstreetmap.josm.plugins.czechaddress.actions;

import java.awt.event.ActionEvent;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.plugins.czechaddress.gui.ManagerDialog;

/**
 * Shows the manager window for editing the database.
 *
 * @author Radomír Černoch, radomir.cernoch@gmail.com
 *
 * @see ManagerDialog
 */
public class ManagerAction extends JosmAction {

    public ManagerAction() {
        super("Upravit databázi",
              (String)null,//"envelope-closed-big.png",
              "Upravit jména elemntů dle mapy",
              null,
              true, "czechaddress/manager", true);
    }

    @Override
	public void actionPerformed(ActionEvent e) {
        (new ManagerDialog()).setVisible(true);
    }
}
