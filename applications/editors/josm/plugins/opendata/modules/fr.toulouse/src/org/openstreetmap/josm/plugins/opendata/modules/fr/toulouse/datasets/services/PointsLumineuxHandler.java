// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.services;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class PointsLumineuxHandler extends ToulouseDataSetHandler {

    public PointsLumineuxHandler() {
        super(25051, "highway=street_lamp");
        setName("Points lumineux");
        setCategory(CAT_SERVICES);
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsCsvKmzTabFilename(filename, "Points_lumineux");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Node n : ds.getNodes()) {
            n.put("highway", "street_lamp");
            replace(n, "Point_geo", "ref");
            n.remove("color");
        }
    }
}
