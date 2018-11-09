// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.public_transport.commands;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.plugins.public_transport.actions.GTFSImporterAction;

public class GTFSCatchCommand extends AbstractGTFSCatchJoinCommand {

    public GTFSCatchCommand(GTFSImporterAction controller) {
        super(controller, true);
    }

    @Override
    public String getDescriptionText() {
        return tr("Public Transport: Catch GTFS stops");
    }
}
