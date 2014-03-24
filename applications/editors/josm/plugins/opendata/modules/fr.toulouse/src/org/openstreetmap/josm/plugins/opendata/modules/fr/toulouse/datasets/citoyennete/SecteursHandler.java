// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.citoyennete;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class SecteursHandler extends ToulouseDataSetHandler {

    public SecteursHandler() {
        super(12580, "admin_level=10");
        setWikiPage("Secteurs de proximité");
        setCategory(CAT_CITOYENNETE);
        setMenuIcon("presets/boundaries.png");
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsKmzTabFilename(filename, "Secteurs");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Relation r : ds.getRelations()) {
            r.remove("name");
            replace(r, "Secteur", "ref");
            replace(r, "Nom_Secteur", "description");
            r.put("type", "boundary");
            r.put("boundary", "administrative");
            r.put("admin_level", "10");
            r.remove("Adjoint_Secteur");
            r.remove("Charge_de_Secteur");
        }
    }
}
