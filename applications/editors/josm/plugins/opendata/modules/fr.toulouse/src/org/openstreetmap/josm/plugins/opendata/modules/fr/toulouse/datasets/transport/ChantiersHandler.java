// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.transport;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public abstract class ChantiersHandler extends ToulouseDataSetHandler {

    public ChantiersHandler(int portalId, String name) {
        super(portalId, "highway=construction");
        setWikiPage("Chantiers en cours");
        setName(name);
        setCategory(CAT_TRANSPORT);
    }
    
    @Override
    public void updateDataSet(DataSet ds) {
        for (OsmPrimitive p : ds.allPrimitives()) {
            if (p.hasKeys()) {
                p.put("highway", "construction");
                p.remove("name");
                p.remove("color");
                replace(p, "Lien", "website");
            }
        }
    }
}
