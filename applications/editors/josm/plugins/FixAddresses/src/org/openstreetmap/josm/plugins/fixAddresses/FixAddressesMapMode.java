// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;

import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.gui.MapFrame;

@SuppressWarnings("serial")
public class FixAddressesMapMode extends MapMode {

    public FixAddressesMapMode(MapFrame mapFrame) {
        super(tr("Fix addresses"), "incompleteaddress_24",
                tr("Show dialog with incomplete addresses"),
                mapFrame,
                Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
}
