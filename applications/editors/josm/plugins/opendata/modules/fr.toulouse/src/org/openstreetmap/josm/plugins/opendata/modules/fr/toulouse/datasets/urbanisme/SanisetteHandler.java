// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.urbanisme;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class SanisetteHandler extends ToulouseDataSetHandler {

    public SanisetteHandler() {
        super(12584, "amenity=toilets");
        setWikiPage("Sanisettes");
        setCategory(CAT_URBANISME);
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsCsvKmzTabFilename(filename, "Sanisette");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Node n : ds.getNodes()) {
            n.remove("name");
            n.put("amenity", "toilets");
            n.put("supervised", "no");
            n.put("unisex", "yes");
            n.put("fee", "no");
            n.put("operator", "JCDecaux");
            n.put("opening_hours", "24/7");
            replace(n, "numero", REF_TOULOUSE_METROPOLE);
            replace(n, "PMR", "wheelchair", new String[]{"true", "false"}, new String[]{"yes", "no"});
            String valide = n.get("emplacement_valide");
            if (valide != null && valide.equalsIgnoreCase("non")) {
                n.put("fixme", "L'emplacement semble invalide !");
            } else {
                n.remove("emplacement_valide");
            }
            n.remove("adresse");
            n.remove("INSEE");
            n.remove("color");
        }
    }
}
