// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.ecologie;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.DataGouvDataSetHandler;

public class ReservesBiologiquesHandler extends DataGouvDataSetHandler {

    public ReservesBiologiquesHandler() {
        super("Réserves-biologiques-de-métropole-30378855");
        setName("Réserves biologiques de métropole");
        setDownloadFileName("ONF_RB_2011_Officielles_L93.zip");
    }
    
    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsZipFilename(filename, "ONF_RB_20.._Officielles_L93") || acceptsShpFilename(filename, "ONF_RB_20.._Officielles_L93");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        // TODO
    }
}
