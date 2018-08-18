// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.buildings_tools;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.tools.Shortcut;

public class BuildingRectangleAction extends JosmAction {

    public BuildingRectangleAction() {
        super(tr("Set building shape to rectangle"), "mapmode/rectangular", tr("Set building shape to rectangle"),
                Shortcut.registerShortcut("buildings_tools:rectangle",
                        tr("Data: {0}", tr("Set buildings shape to rectangle")),
                        KeyEvent.VK_R, Shortcut.ALT),
                true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ToolSettings.saveShape(ToolSettings.Shape.RECTANGLE);
    }
}
