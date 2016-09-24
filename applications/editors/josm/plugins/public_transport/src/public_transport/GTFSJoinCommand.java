package public_transport;

import static org.openstreetmap.josm.tools.I18n.tr;

public class GTFSJoinCommand extends AbstractGTFSCatchJoinCommand {

    public GTFSJoinCommand(GTFSImporterAction controller) {
        super(controller, false);
    }

    @Override
    public String getDescriptionText() {
        return tr("Public Transport: Join GTFS stops");
    }
}
