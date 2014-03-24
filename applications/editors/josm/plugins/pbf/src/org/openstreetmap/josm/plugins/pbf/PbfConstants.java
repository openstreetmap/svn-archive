// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pbf;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.actions.ExtensionFileFilter;

/**
 * 
 * @author Don-vip
 *
 */
public interface PbfConstants {
    
    /**
     * File extension.
     */
    public static final String EXTENSION = "osm.pbf";
    
    /**
     * File filter used in import/export dialogs.
     */
    public static final ExtensionFileFilter FILE_FILTER = new ExtensionFileFilter(EXTENSION, EXTENSION, tr("OSM Server Files pbf compressed") + " (*."+EXTENSION+")");
}
