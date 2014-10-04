// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.urbanisme;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.plugins.opendata.core.util.NamesFrUtils;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class NumerosRueHandler extends ToulouseDataSetHandler {

    public NumerosRueHandler() {
        super(12673, "addr:housenumber");
        setWikiPage("Numéros de rue");
        setCategory(CAT_URBANISME);
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsCsvKmzTabFilename(filename, "Numeros");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        Map<String, Relation> associatedStreets = new HashMap<>();
        
        for (Node n : ds.getNodes()) {
            replace(n, "no", "addr:housenumber");
            n.remove("numero");
            replace(n, "lib_off", "addr:street");
            n.remove("mot_directeur");
            n.remove("name");
            n.remove("rivoli");
            String fantoir = n.get("nrivoli").substring(6);
            n.remove("nrivoli");
            n.remove("color");
            String streetName = NamesFrUtils.checkStreetName(n, "addr:street");
            Relation street = associatedStreets.get(n.get("sti"));
            if (street == null) {
                associatedStreets.put(n.get("sti"), street = new Relation());
                street.put("type", "associatedStreet");
                street.put("name", streetName);
                street.put("ref:FR:FANTOIR", fantoir);
                ds.addPrimitive(street);
            }
            street.addMember(new RelationMember("house", n));
            n.remove("sti");
        }
    }
}
