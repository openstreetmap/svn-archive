// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.topographie;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class AltimetrieVoieHandler extends ToulouseDataSetHandler  {
    
    public AltimetrieVoieHandler() {
        super(12660, "ele");
        setName("Altimétrie des voies");
        setCategory(CAT_TOPOGRAPHIE);
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsCsvKmzTabFilename(filename, "XYZ_PT_ALTI_VOIES");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Node n : ds.getNodes()) {
            replace(n, "Altimetrie", "ele");
            n.put("name", n.get("ele")); // name pour voir la hauteur directement (hack, FIXME)
        }
    }
}
