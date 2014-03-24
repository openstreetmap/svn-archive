// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.transport;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class MetroStationHandler extends ToulouseDataSetHandler {

    public MetroStationHandler() {
        super(12542, "subway=yes");
        setName("Stations de métro");
        setCategory(CAT_TRANSPORT);
        setMenuIcon("styles/standard/transport/railway_station.png");
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsCsvKmzTabFilename(filename, "Metro_Station");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Node n : ds.getNodes()) {
            n.put("public_transport", "stop_position");
            n.put("subway", "yes");
            replace(n, "NOM", "name");
        }
    }
}
