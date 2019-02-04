// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;

import org.openstreetmap.josm.actions.mapmode.MapMode;

/**
 * Map mode that shows dialog with incomplete addresses
 */
@SuppressWarnings("serial")
public class FixAddressesMapMode extends MapMode {

    public FixAddressesMapMode() {
        super(tr("Fix addresses"), "incompleteaddress_24",
                tr("Show dialog with incomplete addresses"),
                Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
}
