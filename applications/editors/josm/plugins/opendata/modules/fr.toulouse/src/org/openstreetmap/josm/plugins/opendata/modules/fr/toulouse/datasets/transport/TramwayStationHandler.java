// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.transport;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class TramwayStationHandler extends ToulouseDataSetHandler {

    public TramwayStationHandler() {
        super(12611, "tram=yes");
        setName("Stations de tramway");
        setCategory(CAT_TRANSPORT);
        setMenuIcon("styles/standard/transport/railway_station.png");
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsCsvKmzTabFilename(filename, "Tramway_Stations");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Node n : ds.getNodes()) {
            n.put("public_transport", "stop_position");
            n.put("tram", "yes");
            replace(n, "NOM", "name");
        }
    }
}
