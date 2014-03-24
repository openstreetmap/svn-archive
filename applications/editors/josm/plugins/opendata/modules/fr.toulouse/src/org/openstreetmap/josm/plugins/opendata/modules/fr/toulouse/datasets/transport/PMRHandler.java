// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.transport;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class PMRHandler extends ToulouseDataSetHandler {

    public PMRHandler() {
        super(12538, "amenity=parking_space");
        setWikiPage("PMR");
        setCategory(CAT_TRANSPORT);
        setMenuIcon("styles/standard/vehicle/parking/handicapped.png");
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsCsvKmzTabFilename(filename, "Emplacements_PMR");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Node n : ds.getNodes()) {
            n.remove("name");
            n.put("amenity", "parking_space");
            n.put("access:disabled", "designated");
            n.put("wheelchair", "designated");
            replace(n, "nb_places", "capacity:disabled");
            n.remove("Lib_voie");
            n.remove("No");
            n.remove("commune");
            n.remove("code_insee");
            n.remove("color");
            replace(n, "id_PMR", REF_TOULOUSE_METROPOLE);
        }
    }
}
