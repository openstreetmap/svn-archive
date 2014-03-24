// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.enfance;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class EcoleMaternelleHandler extends ToulouseDataSetHandler {

    public EcoleMaternelleHandler() {
        super(12490, "amenity=school");
        setWikiPage("Écoles maternelles publiques");
        setCategory(CAT_ENFANCE);
        for (String forbidden : new String[]{"élémentaire","primaire","collège","lycée","secondaire"}) {
            addForbiddenTag("school:FR="+forbidden);
        }
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsCsvKmzTabFilename(filename, "Ecoles_Mat_Publiques");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Node n : ds.getNodes()) {
            n.put("amenity", "school");
            n.put("school:FR", "maternelle");
            n.put("operator", "public");
            replace(n, "Ecole", "name");
        }
    }
}
