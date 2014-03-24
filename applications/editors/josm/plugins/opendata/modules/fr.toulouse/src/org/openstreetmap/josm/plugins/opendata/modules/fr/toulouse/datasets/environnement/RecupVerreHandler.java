// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.environnement;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class RecupVerreHandler extends ToulouseDataSetHandler {

    public RecupVerreHandler() {
        super(12496, "amenity=recycling");
        setWikiPage("Récup' Verre");
        setCategory(CAT_ENVIRONNEMENT);
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsCsvKmzTabFilename(filename, "Recup_Verre");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Node n : ds.getNodes()) {
            n.remove("name");
            n.put("amenity", "recycling");
            n.put("recycling:glass", "no");
            n.put("recycling:glass_bottles", "yes");
        }
    }
}
