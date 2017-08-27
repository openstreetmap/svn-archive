// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.canvec_helper;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * Canvec Helper plugin.
 */
public class CanvecHelper extends Plugin {

    /**
     * Constructs a new {@code CanvecHelper} plugin.
     * @param info plugin info
     */
    public CanvecHelper(PluginInformation info) {
        super(info);
        MainApplication.getMenu().imagerySubMenu.add(new CanvecHelperAction(this));
    }
}
