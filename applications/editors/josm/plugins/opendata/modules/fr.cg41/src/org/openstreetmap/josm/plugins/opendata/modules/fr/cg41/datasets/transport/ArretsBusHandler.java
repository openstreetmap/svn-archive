// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.cg41.datasets.transport;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.plugins.opendata.modules.fr.cg41.datasets.Cg41DataSetHandler;

public class ArretsBusHandler extends Cg41DataSetHandler {

    public ArretsBusHandler() {
        super(14615, "Points-d'arrêt-du-réseau-de-transport-départemental-\"Route41\"-30383156");
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsShpFilename(filename, "Points_ROUTE41");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        // TODO Auto-generated method stub
    }
}
