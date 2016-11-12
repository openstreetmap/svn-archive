// License: GPL. For details, see LICENSE file.
package public_transport;

import static org.openstreetmap.josm.tools.I18n.tr;

public class GTFSCatchCommand extends AbstractGTFSCatchJoinCommand {

    public GTFSCatchCommand(GTFSImporterAction controller) {
        super(controller, true);
    }

    @Override
    public String getDescriptionText() {
        return tr("Public Transport: Catch GTFS stops");
    }
}
