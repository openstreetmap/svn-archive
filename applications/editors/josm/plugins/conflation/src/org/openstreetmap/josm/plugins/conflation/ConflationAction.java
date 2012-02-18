// License: GPL. Copyright 2011 by Josh Doe and others
// Connects from JOSM menu action to Plugin
package org.openstreetmap.josm.plugins.conflation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.Main;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.tools.Shortcut;

//@SuppressWarnings("serial")
public class ConflationAction extends JosmAction {
    public ConflationAction() {
        super(tr("Conflation"), "conflation", tr("Conflation tool for merging data"),
                Shortcut.registerShortcut("tool:conflation", tr("Tool: {0}", tr("Conflation")),
                KeyEvent.VK_A, Shortcut.CTRL_SHIFT), true);
        //setEnabled(false);
        //DataSet.selListeners.add(this);

    }

    public void actionPerformed(ActionEvent e) {
        // get list of OsmDataLayers
        List<OsmDataLayer> layerList = null;
        if (Main.map != null && Main.map.mapView != null) {
            layerList = Main.map.mapView.getLayersOfType(OsmDataLayer.class);
        }
        if (layerList == null || layerList.isEmpty()) {
            JOptionPane.showMessageDialog(Main.parent, tr("There are no data layers "
                    + "present. Please open or create at least one data layer and try again."),
                    tr("Cannot perform conflation"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        // show options dialog
        ConflationOptionsDialog conflationDialog = new ConflationOptionsDialog(Main.parent, layerList);
        conflationDialog.setVisible(true);
    }
}
