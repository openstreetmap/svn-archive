// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.culture;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class LudothequeHandler extends ToulouseDataSetHandler {

    public LudothequeHandler() {
        super(12420, "amenity=toy_library");
        setWikiPage("Ludothèques");
        setCategory(CAT_CULTURE);
    }
    
    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsCsvKmzTabFilename(filename, "Ludotheques");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Node n : ds.getNodes()) {
            replace(n, "NOM", "name");
            n.put("amenity", "toy_library");
            n.remove("ADRESSE");
            n.remove("CP");
            n.remove("INSEE");
            n.remove("NATURE");
            n.remove("NUM");
            n.remove("QUARTIER");
            n.remove("STIADR");
        }
    }
}
