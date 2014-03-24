// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.culture;

import org.apache.commons.lang3.text.WordUtils;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class MuseeHandler extends ToulouseDataSetHandler {

    public MuseeHandler() {
        super(12426, "tourism=museum");
        setWikiPage("Musées");
        setCategory(CAT_CULTURE);
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsCsvKmzTabFilename(filename, "Musee");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Node n : ds.getNodes()) {
            replace(n, "NOMS", "name");
            replace(n, "SITE_INTERNET", "contact:website");
            n.put("tourism", "museum");
            n.remove("ADRESSES");
            n.remove("Num");
            n.remove("Index");
            replacePhone(n, "TELEPHONE");
            String name = WordUtils.capitalizeFully(n.get("name")).replace("Musee", "Musée").replace(" D ", " d'").replace(" L ", " l'").trim();
            int index = name.indexOf("Musée");
            if (index > 1) {
                name = name.substring(index) + " " + name.substring(0, index-1);
            }
            while (name.contains("  ")) {
                name = name.replace("  ", " ");
            }
            n.put("name", name);
        }
    }
}
