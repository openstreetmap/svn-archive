// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.enfance;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class EcoleBalmaHandler extends ToulouseDataSetHandler {

    public EcoleBalmaHandler() {
        super(13993, "amenity=school");
        setWikiPage("Écoles");
        setCategory(CAT_ENFANCE);
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsKmzFilename(filename, "Ecoles");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Node n : ds.getNodes()) {
            n.put("amenity", "school");
        }
    }
}
