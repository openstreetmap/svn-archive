// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.citoyennete;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class MairieHandler extends ToulouseDataSetHandler {

    public MairieHandler() {
        this(12554, "Mairies");
        setCategory(CAT_CITOYENNETE);
    }
    
    protected MairieHandler(int portalId, String wikiPage) {
        super(portalId, "amenity=townhall");
        setWikiPage(wikiPage);
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsCsvKmzTabFilename(filename, "Mairie");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Node n : ds.getNodes()) {
            replace(n, "Mairie", "name");
            n.put("amenity", "townhall");
            replaceOpeningHours(n, "Horaire_ouverture");
            n.remove("INSEE");
            n.remove("Libelle");
            replacePhone(n, "Telephone");
            n.remove("Type");
        }
    }
}
