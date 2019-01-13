// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.o5m;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.actions.ExtensionFileFilter;

/**
 * Constants for o5m plugin.
 * @author GerdP
 *
 */
public interface O5mConstants {
    
    /**
     * File extension.
     */
    String EXTENSION = "o5m";
    
    /**
     * File filter used in import/export dialogs.
     */
    ExtensionFileFilter FILE_FILTER = new ExtensionFileFilter(EXTENSION, EXTENSION, 
            tr("OSM Server Files o5m compressed") + " (*."+EXTENSION+")");
}
