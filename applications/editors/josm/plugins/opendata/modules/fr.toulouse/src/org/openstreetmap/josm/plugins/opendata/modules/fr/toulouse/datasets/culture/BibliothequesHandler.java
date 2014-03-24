// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.culture;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class BibliothequesHandler extends ToulouseDataSetHandler {

    public BibliothequesHandler() {
        super(12402, "amenity=library");
        setWikiPage("Médiathèques, bibliothèques et bibliobus");
        setCategory(CAT_CULTURE);
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsCsvKmzTabFilename(filename, "Bibliotheques");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Node n : ds.getNodes()) {
            replace(n, "Nom", "name");
            replace(n, "Site_Internet", "website");
            n.put("amenity", "library");
        }
    }
}
