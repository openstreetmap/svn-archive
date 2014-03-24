// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.enfance;

import org.apache.commons.lang3.text.WordUtils;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class CrechesHandler extends ToulouseDataSetHandler {

    public CrechesHandler() {
        super(12462, "amenity=kindergarten");
        setWikiPage("Crèches");
        setCategory(CAT_ENFANCE);
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsCsvKmzTabFilename(filename, "Creches");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Node n : ds.getNodes()) {
            n.put("amenity", "kindergarten");
            replace(n, "NOM", "name");
            n.put("name", WordUtils.capitalizeFully(n.get("name")));
            n.remove("QUARTIER");
            n.remove("RUE");
            replace(n, "NUM", "addr:housenumber");
            n.remove("CP");
            n.remove("STIADR");
            if (n.hasKey("NATURE")) {
                String nature = n.get("NATURE");
                if (nature.equals("CC")) {
                    nature = "Crèche collective";
                } else if (nature.equals("CF")) {
                    nature = "Crèche familiale";
                } else if (nature.equals("HG")) {
                    nature = "Halte-Garderie";
                } else if (nature.equals("JE")) {
                    nature = "Jardin d'Enfants";
                } else if (nature.equals("MA")) {
                    nature = "Multi-Accueil";
                }
                n.remove("NATURE");
                n.put("description", nature);
            }
            if (n.hasKey("NATGEST")) {
                String gest = n.get("NATGEST").split(" ")[1];
                if (gest.equals("ASS")) {
                    gest = "Association";
                } else if (gest.equals("CCAS")) {
                    gest = "Centre Communal d'Action Sociale";
                } else if (gest.equals("CHU")) {
                    gest = "CHU de Toulouse";
                } else if (gest.equals("PRIV")) {
                    gest = "private";
                } else if (gest.equals("VT")) {
                    gest = "Mairie de Toulouse";
                } else if (gest.equals("CAF")) {
                    gest = "Caisse d'Allocations familiales";
                } else if (gest.equals("MUT")) {
                    gest = "Mutuelle";
                } else if (gest.equals("UPS")) {
                    gest = "Université Paul Sabatier";
                }
                n.remove("NATGEST");
                n.put("operator", gest);
            }
        }
    }
}
