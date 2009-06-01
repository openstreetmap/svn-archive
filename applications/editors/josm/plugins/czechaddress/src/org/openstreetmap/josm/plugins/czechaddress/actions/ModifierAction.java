/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openstreetmap.josm.plugins.czechaddress.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.plugins.czechaddress.gui.Renamer;
import org.openstreetmap.josm.tools.Shortcut;

/**
 *
 * @author Radomír Černoch, radomir.cernoch@gmail.com
 */
public class ModifierAction extends JosmAction {

    public ModifierAction() {
        super("Upravit databázi",
              null,//"envelope-closed-big.png",
              "Upravit jména elemntů dle mapy",
              Shortcut.registerShortcut("address:assignaddress",
                        "Adresy: Přiřadit adresy",
                        KeyEvent.VK_P, Shortcut.GROUP_DIRECT, Shortcut.SHIFT_DEFAULT),
              true);
    }

    public void actionPerformed(ActionEvent e) {
        (new Renamer()).setVisible(true);
    }
}
