// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.enfance;

import org.apache.commons.lang3.text.WordUtils;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class EcoleElementaireHandler extends ToulouseDataSetHandler {

    public EcoleElementaireHandler() {
        super(12474, "amenity=school");
        setWikiPage("Écoles élémentaires publiques");
        setCategory(CAT_ENFANCE);
        for (String forbidden : new String[]{"maternelle","primaire","collège","lycée","secondaire"}) {
            addForbiddenTag("school:FR="+forbidden);
        }
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsCsvKmzTabFilename(filename, "Ecoles_Elem_Publiques");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Node n : ds.getNodes()) {
            n.put("amenity", "school");
            n.put("school:FR", "élémentaire");
            n.put("operator:type", "public");
            replace(n, "Ecole", "name");
            n.put("name", WordUtils.capitalizeFully(n.get("name")));
            n.remove("Codpos");
            n.remove("Codsti");
            n.remove("color");
            n.remove("Index");
            n.remove("Libelle");
            n.remove("Num");
            replace(n, "RNE", "ref:UAI");
            replace(n, "Tel", "phone");
        }
    }
}
