// License: GPL. Copyright 2011 by Josh Doe and others
package org.openstreetmap.josm.plugins.conflation;

import java.awt.event.KeyEvent;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.tools.Shortcut;

import static org.openstreetmap.josm.tools.I18n.tr;

public class ConflationPlugin extends Plugin {

    private ConflationToggleDialog dialog = null;

    /**
     * constructor
     */
    public ConflationPlugin(PluginInformation info) {
        super(info);
    }

    // add dialog the first time the mapframe is loaded
    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (oldFrame == null && newFrame != null) {
            if (dialog == null) {
//                Shortcut shortcut = null; Shortcut.registerShortcut("Conflation", tr("Toggle: {0}", tr("Open Conflation")),
//                        KeyEvent.VK_0, Shortcut.ALT_SHIFT);
                Shortcut shortcut = null;
                String name = "Conflation";
                String tooltip = "Activates the conflation plugin";
                dialog = new ConflationToggleDialog(tr(name), "conflation.png", tr(tooltip),
                        shortcut, 150, this);
            }
            newFrame.addToggleDialog(dialog);
        }
    }
}
