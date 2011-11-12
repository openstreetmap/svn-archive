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
import java.awt.print.*;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * The PrintAction controls basic printing of the MapView
 * and takes care of reasonable temporary adjustments to the preferences.
 */
public class PrintAction extends JosmAction {
    
    /**
     * Create a new PrintAction.
     */
    public PrintAction() {
        super(tr("Print..."), null, tr("Print the map"), 
        Shortcut.registerShortcut(
          "print:print", 
          tr("File: {0}", tr("Print...")), 
          KeyEvent.VK_P, 
          Shortcut.GROUP_MENU), 
        true);
    }

    /**
     * Trigger the printing process.
     * 
     * @param e not used.
     */
    public void actionPerformed(ActionEvent e) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(new PrintableMapView());
        if (job.printDialog()) {
            try {
                PrintPlugin.adjustPrefs();
                job.print();
            }
            catch (PrinterAbortException ex) {
                String msg = ex.getLocalizedMessage();
                if (msg.length() == 0) {
                    msg = tr("Printing has been cancelled.");
                }
                JOptionPane.showMessageDialog(Main.main.parent, msg,
                  tr("Printing stopped"),
                  JOptionPane.WARNING_MESSAGE);
            }
            catch (PrinterException ex) {
                String msg = ex.getLocalizedMessage();
                if (msg.length() == 0) {
                    msg = tr("Printing has failed.");
                }
                JOptionPane.showMessageDialog(Main.main.parent, msg,
                  tr("Printing stopped"),
                  JOptionPane.ERROR_MESSAGE);
            }
            finally {
                PrintPlugin.restorePrefs();
            }
        }
    }
    
}
