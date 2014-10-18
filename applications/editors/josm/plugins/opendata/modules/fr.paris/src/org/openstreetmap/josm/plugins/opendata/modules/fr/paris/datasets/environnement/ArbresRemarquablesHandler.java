// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.paris.datasets.environnement;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.paris.datasets.ParisDataSetHandler;

public class ArbresRemarquablesHandler extends ParisDataSetHandler {

    public ArbresRemarquablesHandler() {
        super(107);
        setName("Arbres remarquables");
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsShpFilename(filename, "arbres_remarquables") || acceptsZipFilename(filename, "arbres_remarquables_20..");
    }
    
    @Override
    public void updateDataSet(DataSet ds) {
        for (Node n : ds.getNodes()) {
            n.put("natural", "tree");
            replace(n, "ANNEE__PLA", "start_date");
        }
    }

    @Override
    protected String getDirectLink() {
        return PORTAL+"hn/arbres_remarquables_2011.zip";
    }
}
