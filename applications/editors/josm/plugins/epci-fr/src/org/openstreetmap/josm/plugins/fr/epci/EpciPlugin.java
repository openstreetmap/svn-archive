// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.epci;

import org.openstreetmap.josm.gui.DefaultNameFormatter;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * @author Don-vip
 * @version 1.0
 * History:
 * 1.0 20-Oct-2011 Version 1.0, includes newly created EPCI type "metropole"
 * 0.1 08-Jul-2011 first prototype
 */
public class EpciPlugin extends Plugin {

    /**
     * Constructs a new {@code EpciPlugin}.
     * @param info plugin info
     */
    public EpciPlugin(PluginInformation info) {
        super(info);
        DefaultNameFormatter.registerFormatHook(new EpciNameFormatter());
    }
}
