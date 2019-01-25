// License: GPL. For details, see LICENSE file.
package poly;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.actions.ExtensionFileFilter;

/**
 * Extension and file filter for poly type.
 *
 * @author zverik
 * @author Gerd Petermann
 */

final class PolyType {
    private static final String EXTENSION = "poly";
    
    /** filter for osmosis poly files */
    static final ExtensionFileFilter FILE_FILTER = new ExtensionFileFilter(
            EXTENSION, EXTENSION, tr("Osmosis polygon filter files") + " (*." + EXTENSION + ")");
    private PolyType() {}
}
