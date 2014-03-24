// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.patrimoine;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class Parcelles1680Handler extends ToulouseDataSetHandler {

    public Parcelles1680Handler() {
        super(12514);
        setName("Parcellaire de 1680");
        setCategory(CAT_PATRIMOINE);
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsKmzTabFilename(filename, "Parcelles_1680");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Relation r : ds.getRelations()) {
            replace(r, "Nom_prenom", "name");
        }
    }
}
