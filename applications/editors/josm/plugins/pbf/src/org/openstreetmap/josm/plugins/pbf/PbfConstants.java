// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pbf;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.actions.ExtensionFileFilter;

/**
 * PBF constants.
 * @author Don-vip
 */
public interface PbfConstants {

    /**
     * File extension.
     */
    String EXTENSION = "osm.pbf";

    /**
     * File filter used in import/export dialogs.
     */
    ExtensionFileFilter FILE_FILTER = new ExtensionFileFilter(EXTENSION, EXTENSION,
            tr("OSM Server Files pbf compressed") + " (*."+EXTENSION+")");
}
