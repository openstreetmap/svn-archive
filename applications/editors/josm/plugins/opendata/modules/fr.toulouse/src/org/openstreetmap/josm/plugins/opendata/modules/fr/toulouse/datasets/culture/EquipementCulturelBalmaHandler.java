// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.culture;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class EquipementCulturelBalmaHandler extends ToulouseDataSetHandler {

    public EquipementCulturelBalmaHandler() {
        super(13997);
        setWikiPage("Équipements Culturels");
        setCategory(CAT_CULTURE);
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsKmzFilename(filename, "culture");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Node n : ds.getNodes()) {
            if (n.get("name").equalsIgnoreCase("Bibliothèque")) {
                n.put("amenity", "library");
            } else if (n.get("name").equalsIgnoreCase("Auditorium")) {
                n.put("amenity", "auditorium");
            }
        }
    }
}
