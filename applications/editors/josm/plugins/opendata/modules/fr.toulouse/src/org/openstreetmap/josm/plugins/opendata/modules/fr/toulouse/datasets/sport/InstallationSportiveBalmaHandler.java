// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.sport;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class InstallationSportiveBalmaHandler extends ToulouseDataSetHandler {

    public InstallationSportiveBalmaHandler() {
        super(14010);
        setWikiPage("Installations sportives (Balma)");
        setCategory(CAT_SPORT);
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsKmzFilename(filename, "Sports");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Node n : ds.getNodes()) {
            if (n.get("name").startsWith("Gymnase")) {
                n.put("leisure", "sports_centre");
            } else if (n.get("name").startsWith("Piscine")) {
                n.put("leisure", "swimming_pool");
                n.put("sport", "swimming");
            } else if (n.get("name").startsWith("Skate")) {
                n.put("leisure", "skate_park");
                n.put("sport", "skateboard");
            } else if (n.get("name").startsWith("Tennis")) {
                n.put("leisure", "pitch");
                n.put("sport", "tennis");
            } else if (n.get("name").startsWith("Stade")) {
                n.put("leisure", "pitch");
            } else if (n.get("name").startsWith("Dojo")) {
                n.put("amenity", "dojo");
            } else if (n.get("name").startsWith("Boulodrome")) {
                n.put("sport", "boules");
            }
        }
    }
}
