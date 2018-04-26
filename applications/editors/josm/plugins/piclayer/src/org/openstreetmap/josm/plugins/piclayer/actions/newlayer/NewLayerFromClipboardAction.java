// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.piclayer.actions.newlayer;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.piclayer.layer.PicLayerFromClipboard;

/**
 * Action responsible for creation of a new layer based on
 * the content of the clipboard.
 */
public class NewLayerFromClipboardAction extends JosmAction {

    /**
     * Constructor...
     */
    public NewLayerFromClipboardAction() {
        super(tr("New picture layer from clipboard"), "layericonclp", null, null, false);
    }

    /**
     * Action handler
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {
        // Create layer from clipboard
        PicLayerFromClipboard layer = new PicLayerFromClipboard();
        // Add layer only if successfully initialized
        try {
            layer.initialize();
        } catch (IOException e) {
            // Failed
            System.out.println("NewLayerFromClipboardAction::actionPerformed - " + e.getMessage());
            JOptionPane.showMessageDialog(null, e.getMessage(), tr("Problem occurred"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Add layer
        MainApplication.getLayerManager().addLayer(layer);
    }
}
