// License: GPL. See LICENSE file for details.
package org.openstreetmap.josm.plugins.undelete;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class Undelete extends Plugin {

    public Undelete(PluginInformation info) {
        super(info);
        MainMenu.addAfter(Main.main.menu.fileMenu, new UndeleteAction(), false, Main.main.menu.updateModified);
    }
}
