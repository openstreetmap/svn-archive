// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.citoyennete;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class BureauxVoteHandler extends ToulouseDataSetHandler {

    public BureauxVoteHandler() {
        super(12550, "polling_station");
        setWikiPage("Bureaux de vote 2012");
        setCategory(CAT_CITOYENNETE);
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsKmzTabFilename(filename, "Lieu_vote_2012");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Relation r : ds.getRelations()) {
            replace(r, "TEXT", "name");
            r.put("polling_station", "2012");
            r.put("building", "yes");
            String name = r.getName();
            if (name != null) {
                if (name.contains("ECOLE") || name.contains("MATERNELLE")) {
                    r.put("amenity", "school");
                } else if (name.contains("MAIRIE")) {
                    r.put("amenity", "townhall");
                } else if (name.contains("PISCINE")) {
                    r.put("leisure", "swimming_pool");
                }
            }
        }
    }
}
