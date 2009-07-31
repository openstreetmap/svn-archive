package org.openstreetmap.josm.plugins.czechaddress.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.czechaddress.CzechAddressPlugin;
import org.openstreetmap.josm.plugins.czechaddress.gui.PointManipulatorDialog;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Action adding a menu item for editing address points
 *
 * @see PointManipulatorDialog
 *
 * @author Radomír Černoch radomir.cernoch@gmail.com
 */
public class PointManipulatorAction extends JosmAction {

    /**
     * Default constructor, which sets the title, shortcut, ...
     */
    public PointManipulatorAction() {
        super("Vytvořit/upravit adresu",
              "envelope-open-star-big.png",
              "Vytvoří nebo upraví adresní bod z čísla popisného.",
                Shortcut.registerShortcut("tools:newaddress",
                        "Adresy: Vytvořit/upravit adres",
                        KeyEvent.VK_A, Shortcut.GROUP_DIRECT, Shortcut.SHIFT_DEFAULT),
                true);
    }

    /**
     * Checks precoditions and eventually shows the dialog.
     *
     * <p>This method checks, whether exactly one {@link OsmPrimitive} has been
     * selected and whether the location was selected
     * (the {@link PointManipulatorDialog} assumes a location is already
     * selected. If both conditions are satisfied, dialog is opened.</p>
     */
    public void actionPerformed(ActionEvent e) {
        Collection<OsmPrimitive> data = Main.ds.getSelected();

        if (data.size() != 1) return;
        OsmPrimitive primitive = (OsmPrimitive) data.toArray()[0];

        if (CzechAddressPlugin.getLocation() == null) return;

        PointManipulatorDialog dialog = new PointManipulatorDialog(primitive);
        dialog.setVisible(true);
    }
}
