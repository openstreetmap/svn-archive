// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.associations;

import org.apache.commons.lang3.text.WordUtils;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class Club3eAgeHandler extends ToulouseDataSetHandler {

    public Club3eAgeHandler() {
        super(12587, "social_facility=outreach", "social_facility:for=senior");
        setWikiPage("Clubs du 3ème âge");
        setCategory(CAT_ASSOCIATIONS);
    }
    
    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsCsvKmzTabFilename(filename, "Club_3E_AGE");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Node n : ds.getNodes()) {
            replace(n, "Nom", "name");
            replace(n, "Telephone", "contact:phone");
            n.put("name", WordUtils.capitalizeFully(n.get("name")));
            n.put("social_facility", "outreach");
            n.put("social_facility:for", "senior");
            n.remove("Adresse");
            n.remove("CP");
            n.remove("Classe");
            n.remove("CodSTI");
            n.remove("Description");
            n.remove("Numero");
            n.remove("Ville");
            n.remove("color");
        }
    }
}
