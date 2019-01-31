// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.undelete;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * Create the undelete plugin.
 */
public class Undelete extends Plugin {

    public Undelete(PluginInformation info) {
        super(info);
        MainMenu.addAfter(MainApplication.getMenu().fileMenu, new UndeleteAction(), false, MainApplication.getMenu().updateModified);
    }
}
