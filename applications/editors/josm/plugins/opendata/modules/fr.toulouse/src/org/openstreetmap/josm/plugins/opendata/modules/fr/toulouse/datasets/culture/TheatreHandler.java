// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.culture;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class TheatreHandler extends ToulouseDataSetHandler {

    public TheatreHandler() {
        super(12448, "amenity=theatre");
        setWikiPage("Théâtres");
        setCategory(CAT_CULTURE);
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsCsvKmzTabFilename(filename, "Theatre");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Node n : ds.getNodes()) {
            replace(n, "NOMS", "name");
            replace(n, "Site_Internet", "contact:website");
            n.put("amenity", "theatre");
            n.remove("ADRESSES");
            n.remove("Description");
            n.remove("Index");
            n.remove("NUM");
            replacePhone(n, "Telephone");
            String type = n.get("Type");
            if (type != null) {
                if (type.equals("MUNICIPAL")) {
                    n.put("operator", "Mairie de Toulouse");
                }
                n.remove("Type");
            }
        }
    }
}
