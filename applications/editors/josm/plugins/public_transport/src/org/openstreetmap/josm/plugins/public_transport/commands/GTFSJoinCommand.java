// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.public_transport.commands;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.plugins.public_transport.actions.GTFSImporterAction;

public class GTFSJoinCommand extends AbstractGTFSCatchJoinCommand {

    public GTFSJoinCommand(GTFSImporterAction controller) {
        super(controller, false);
    }

    @Override
    public String getDescriptionText() {
        return tr("Public Transport: Join GTFS stops");
    }
}
