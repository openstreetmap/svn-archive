/*
 *      PrintAction.java
 *      
 *      Copyright 2011 Kai Pastor
 *      
 *      This program is free software; you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation; either version 2 of the License, or
 *      (at your option) any later version.
 *      
 *      This program is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *      
 *      You should have received a copy of the GNU General Public License
 *      along with this program; if not, write to the Free Software
 *      Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 *      MA 02110-1301, USA.
 *      
 *      
 */

package org.openstreetmap.josm.plugins.print;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.SwingUtilities;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * The PrintAction controls basic printing of the MapView
 * and takes care of reasonable temporary adjustments to the preferences.
 */
public class PrintAction extends JosmAction implements Runnable {
    
    /**
     * Create a new PrintAction.
     */
    public PrintAction() {
        super(tr("Print..."), null, tr("Print the map"), 
        Shortcut.registerShortcut("print:print", tr("File: {0}", tr("Print...")), 
	KeyEvent.VK_P, Shortcut.GROUP_MENU),
        true, "print/print", true);
    }

    /**
     * Trigger the printing dialog.
     * 
     * @param e not used.
     */
    public void actionPerformed(ActionEvent e) {
        // Allow the JOSM GUI to be redrawn before modifying preferences
        SwingUtilities.invokeLater(this);
    }
    
    /**
     * Open the printing dialog
     * 
     * This will temporarily modify the mappaint preferences.
     */
    public void run () {
        PrintPlugin.adjustPrefs();
        PrintDialog window = new PrintDialog(Main.main.parent);
        window.setVisible(true);
        PrintPlugin.restorePrefs();
    }
}
