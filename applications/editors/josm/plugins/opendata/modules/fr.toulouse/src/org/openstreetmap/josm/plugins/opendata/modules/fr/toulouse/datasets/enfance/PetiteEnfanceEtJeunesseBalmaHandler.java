// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.enfance;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class PetiteEnfanceEtJeunesseBalmaHandler extends ToulouseDataSetHandler {

    public PetiteEnfanceEtJeunesseBalmaHandler() {
        super(14001);
        setWikiPage("Petite enfance et jeunesse");
        setCategory(CAT_ENFANCE);
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsKmzFilename(filename, "Petite enfance et jeunesse");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Node n : ds.getNodes()) {
            if (n.get("name").equalsIgnoreCase("Crèche")) {
                n.put("amenity", "kindergarten");
            }
        }
    }
}
