// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.seamapeditor;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class SmedPlugin extends Plugin {

    SmedAction dialog = new SmedAction();

    public SmedPlugin(PluginInformation info) {
        super(info);
        MainMenu.add(MainApplication.getMenu().toolsMenu, dialog);
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (newFrame == null) {
            dialog.closeDialog();
        }
    }
}
