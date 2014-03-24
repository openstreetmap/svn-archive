// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.citoyennete;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class QuartiersHandler extends ToulouseDataSetHandler {

    public QuartiersHandler() {
        super(12574, "admin_level=11");
        setWikiPage("Quartiers de proximité");
        setCategory(CAT_CITOYENNETE);
        setMenuIcon("presets/boundaries.png");
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsKmzTabFilename(filename, "Quartiers");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Relation r : ds.getRelations()) {
            r.remove("name");
            replace(r, "Quartier", "ref");
            replace(r, "Nom_Quartier", "description");
            r.put("type", "boundary");
            r.put("boundary", "administrative");
            r.put("admin_level", "11");
            r.remove("Adjoint_Secteur");
            r.remove("Elu_Referent_Quartier");
            r.remove("Id");
            r.remove("Nom_Secteur");
            r.remove("Secteur");
        }
    }
}
