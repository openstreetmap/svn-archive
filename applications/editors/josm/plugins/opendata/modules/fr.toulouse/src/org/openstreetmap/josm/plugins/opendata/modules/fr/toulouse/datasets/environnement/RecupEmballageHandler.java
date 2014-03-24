// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.environnement;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class RecupEmballageHandler extends ToulouseDataSetHandler {

    public RecupEmballageHandler() {
        super(12494, "amenity=recycling");
        setWikiPage("Récup' Emballage");
        setCategory(CAT_ENVIRONNEMENT);
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsCsvKmzTabFilename(filename, "Recup_Emballage");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Node n : ds.getNodes()) {
            n.remove("name");
            n.put("amenity", "recycling");
            n.put("recycling_type", "container");
            n.put("recycling:plastic_bottles", "yes");
            n.put("recycling:beverage_cartons", "yes");
            n.put("recycling:cardboard", "yes");
            n.put("recycling:newspaper", "yes");
            n.put("recycling:magazines", "yes");
        }
    }
}
