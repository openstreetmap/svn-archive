// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.urbanisme;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class CommuneHandler extends ToulouseDataSetHandler {

    public CommuneHandler() {
        super(12582, "admin_level=8");
        setName("Communes");
        setCategory(CAT_URBANISME);
        setMenuIcon("presets/boundaries.png");
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsKmzTabFilename(filename, "Limites_Communes");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Relation r : ds.getRelations()) {
            r.put("type", "boundary");
            r.put("boundary", "administrative");
            r.put("admin_level", "8");
            replace(r, "libelle", "name");
            replace(r, "code_insee", "ref:INSEE");
        }
    }
}
