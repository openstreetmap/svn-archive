package org.openstreetmap.josm.plugins.czechaddress.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.plugins.czechaddress.CzechAddressPlugin;
import org.openstreetmap.josm.plugins.czechaddress.gui.ConflictResolver;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Action, which shows the dialog for resolving conflicts.
 *
 * @see ConflictResolver
 *
 * @author Radomír Černoch, radomir.cernoch@gmail.com
 */
public class ConflictResolveAction extends JosmAction {

    /**
     * Default constructor, which sets the title, shortcut, ...
     */
    public ConflictResolveAction() {
        super("Zobrazit konflikty",
              "envelope-closed-exclamation-big.png",
              "Zobrazí okno s konflikty, které vznikly během přiřazování " +
                  "objektů mapy k objektům v databázi.",
              Shortcut.registerShortcut("address:resolveconflict",
                        "Adresy: Zobrazit konflikty",
                        KeyEvent.CHAR_UNDEFINED, Shortcut.NONE),
              true);
    }

    /**
     * If the {@link ConflictResolver} window is not visible, it makes so.
     *
     * <p><b>NOTE:</b> There should be only a single such window in the
     * whole JOSM. Therefore the reference to the unique window is obtained
     * from {@link CzechAddressPlugin}{@code .conflictResolver}.
     */
    public void actionPerformed(ActionEvent e) {
        ConflictResolver.getInstance().setVisible(true);
    }
}
