// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.print;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.SwingUtilities;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * The PrintAction controls basic printing of the MapView
 * and takes care of reasonable temporary adjustments to the preferences.
 * @author Kai Pastor
 */
public class PrintAction extends JosmAction implements Runnable {

    /**
     * Create a new PrintAction.
     */
    public PrintAction() {
        super(tr("Print..."), (String) null, tr("Print the map"),
                Shortcut.registerShortcut("system:print", tr("File: {0}", tr("Print...")),
                        KeyEvent.VK_P, Shortcut.CTRL), true, "print/print", true);
    }

    /**
     * Trigger the printing dialog.
     *
     * @param e not used.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // Allow the JOSM GUI to be redrawn before modifying preferences
        SwingUtilities.invokeLater(this);
    }

    /**
     * Open the printing dialog
     *
     * This will temporarily modify the mappaint preferences.
     */
    @Override
    public void run() {
        PrintPlugin.adjustPrefs();
        PrintDialog window = new PrintDialog(MainApplication.getMainFrame());
        window.setVisible(true);
        PrintPlugin.restorePrefs();
    }
}
