// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.transport;

import org.apache.commons.lang3.text.WordUtils;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class VeloToulouseHandler extends ToulouseDataSetHandler {

    public VeloToulouseHandler() {
        super(12546, "amenity=bicycle_rental");
        setWikiPage("Vélô Toulouse");
        setCategory(CAT_TRANSPORT);
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsCsvKmzTabFilename(filename, "Velo_Toulouse");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Node n : ds.getNodes()) {
            n.put("amenity", "bicycle_rental");
            n.put("network", "VélôToulouse");
            n.put("operator", "JCDecaux");
            if (n.hasKey("M_en_S_16_nov_07") && n.get("M_en_S_16_nov_07").equals("O")) {
                n.put("start_date", "2007-11-16");
            }
            n.remove("M_en_S_16_nov_07");
            n.remove("Mot_Directeur");
            n.remove("No");
            n.remove("Nrivoli");
            n.remove("street");
            replace(n, "nb_bornettes", "capacity");
            replace(n, "num_station", "ref");
            replace(n, "nom", "name");
            n.put("name", WordUtils.capitalizeFully(n.get("name")));
            n.remove("code_insee");
            n.remove("commune");
            n.remove("color");
            if (n.hasKey("En_service") && n.get("En_service").equals("N")) {
                n.put("fixme", "Station pas en service");
            }
            n.remove("En_service");
        }
    }
}
