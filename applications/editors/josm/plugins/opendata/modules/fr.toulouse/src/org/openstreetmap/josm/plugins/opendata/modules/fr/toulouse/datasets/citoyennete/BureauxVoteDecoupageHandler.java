// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.citoyennete;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class BureauxVoteDecoupageHandler extends ToulouseDataSetHandler {

    public BureauxVoteDecoupageHandler() {
        super(14401, "boundary=polling_station");
        setWikiPage("Découpage des bureaux de vote");
        setCategory(CAT_CITOYENNETE);
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsCsvKmzTabFilename(filename, "Bureaux_vote_decoupage");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Relation r : ds.getRelations()) {
            replace(r, "NOM", "name");
            replace(r, "IDENTIFIANT", REF_TOULOUSE_METROPOLE);
            r.put("boundary", "polling_station");
            r.remove("ADRESSE");
            r.remove("color");
        }
    }
}
