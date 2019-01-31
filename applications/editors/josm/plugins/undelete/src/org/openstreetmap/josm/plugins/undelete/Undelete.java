// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.undelete;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * Undelete action allows to restore one or more deleted objects.
 */
public class Undelete extends Plugin {

    /**
     * Create the undelete plugin.
     * @param info plugin information
     */
    public Undelete(PluginInformation info) {
        super(info);
        MainMenu.addAfter(MainApplication.getMenu().fileMenu, new UndeleteAction(), false, MainApplication.getMenu().updateModified);
    }
}
