// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.buildings_tools;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.tools.Shortcut;

public class BuildingCircleAction extends JosmAction {

    public BuildingCircleAction() {
        super(tr("Set building shape to circle"), "mapmode/silo", tr("Set building shape to circle"),
                Shortcut.registerShortcut("buildings_tools:circle",
                        tr("Data: {0}", tr("Set buildings shape to circle")),
                        KeyEvent.VK_Z, Shortcut.ALT),
                true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ToolSettings.saveShape(ToolSettings.Shape.CIRCLE);
    }
}
