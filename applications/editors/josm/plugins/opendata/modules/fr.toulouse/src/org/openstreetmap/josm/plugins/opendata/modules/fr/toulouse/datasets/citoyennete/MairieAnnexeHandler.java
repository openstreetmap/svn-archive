// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.citoyennete;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;

public class MairieAnnexeHandler extends MairieHandler {
    
    public MairieAnnexeHandler() {
        super(12560, "Mairies annexes");
        setCategory(CAT_CITOYENNETE);
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsCsvKmzTabFilename(filename, "Mairie_Annexe");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        super.updateDataSet(ds);
        for (Node n : ds.getNodes()) {
            replaceFax(n, "Fax");
            n.remove("Num");
        }
    }
}
