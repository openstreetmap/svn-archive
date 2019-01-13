// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.o5m;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.o5m.io.O5mImporter;

/**
 * 
 * o5m Plugin
 * @author GerdP
 *
 */
public class O5mPlugin extends Plugin {

    public O5mPlugin(PluginInformation info) {
        super(info);
        // Allow JOSM to import *.o5m files
        ExtensionFileFilter.addImporter(new O5mImporter());
    }
}
