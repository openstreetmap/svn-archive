// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.environnement;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class StationEpurationHandler extends ToulouseDataSetHandler {

    public StationEpurationHandler() {
        super(12500, "man_made=wastewater_plant");
        setWikiPage("Stations d'épuration");
        setCategory(CAT_ENVIRONNEMENT);
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsCsvKmzTabFilename(filename, "Stations_epurations");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Node n : ds.getNodes()) {
            replace(n, "NOM_STATION", "name");
            n.put("man_made", "wastewater_plant");
        }
    }
}
