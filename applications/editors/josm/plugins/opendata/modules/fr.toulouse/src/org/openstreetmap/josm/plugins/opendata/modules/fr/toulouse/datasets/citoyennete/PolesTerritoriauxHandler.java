// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.citoyennete;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class PolesTerritoriauxHandler extends ToulouseDataSetHandler {

    public PolesTerritoriauxHandler() {
        super(12568);
        setName("Pôles territoriaux ");
        setCategory(CAT_CITOYENNETE);
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsKmzTabFilename(filename, "Poles_territoriaux");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Relation r : ds.getRelations()) {
            replace(r, "Nom_pole", "name");
        }
    }
}
