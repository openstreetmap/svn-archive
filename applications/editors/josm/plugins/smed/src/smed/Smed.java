// License: GPL. For details, see LICENSE file.
package smed;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class Smed extends Plugin {

    SmedAction dialog = new SmedAction();

    public Smed(PluginInformation info) {
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
