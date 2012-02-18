package org.openstreetmap.josm.plugins.czechaddress.actions;

import org.openstreetmap.josm.plugins.czechaddress.gui.GroupManipulatorDialog;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.plugins.czechaddress.CzechAddressPlugin;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Action adding a menu item for doing address completion.
 *
 * @see GroupManipulatorDialog
 *
 * @author Radomír Černoch radomir.cernoch@gmail.com
 */
public class GroupManipulatorAction extends JosmAction {

    /**
     * Default constructor, which sets the title, shortcut, ...
     */
    public GroupManipulatorAction() {
        super("Přiřadit adresy",
              "envelope-closed-big.png",
              "Přiřadit adresy v celé stáhnuté oblasti",
              Shortcut.registerShortcut("address:assignaddress",
                        "Adresy: Přiřadit adresy",
                        KeyEvent.VK_P, Shortcut.SHIFT),
              true);
    }

    /**
     * Makes the {@link GroupManipulatorDialog} dialog visible.
     *
     * <p><b>NOTE:</b> This dialog assumes that the location has already been
     * selected by the user. Therefore this action checks if the location is not
     * {@code null}. If so, nothing happens.</p>
     */
    public void actionPerformed(ActionEvent e) {
        if (CzechAddressPlugin.getLocation() == null) return;
        GroupManipulatorDialog.getInstence().setVisible(true);
    }
}
