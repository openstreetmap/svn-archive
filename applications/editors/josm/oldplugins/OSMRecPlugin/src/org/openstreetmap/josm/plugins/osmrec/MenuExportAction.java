// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.osmrec;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import org.openstreetmap.josm.actions.JosmAction;

/**
 * Exports the OSMRec toggle dialog.
 *
 * @author imis-nkarag
 *
 */
public class MenuExportAction extends JosmAction {

    /**
     * Constructs a new {@code MenuExportAction}.
     */
    public MenuExportAction() {
        super(tr("OSM Recommendation"), "dialogs/logo-osmrec.png",
                tr("Recommend categories to your newly created instances."), null, false);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (OSMRecPlugin.getCurrentMapFrame() != null) {
            OSMRecToggleDialog pro = new OSMRecToggleDialog();
            OSMRecPlugin.getCurrentMapFrame().addToggleDialog(pro);
        }
    }
}
