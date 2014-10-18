// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.lemans.datasets;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;

public class ServicesCommunautairesMunicipauxHandler extends LeMansDataSetHandler {

    public ServicesCommunautairesMunicipauxHandler() {
        super("F7F65F15-550EA533-37695DD8-F7A74F05");
        setName("Services communautaires et municipaux");
        setKmzShpUuid("66C925DA-550EA533-7E7BB44A-BCF0B629", "66C972AD-550EA533-7E7BB44A-E842FFAD");
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsKmzShpFilename(filename, "SERVICES_VDM_LMM") || acceptsZipFilename(filename, "Les services de le Mans Métropole et de la Ville du Mans .*");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Node n : ds.getNodes()) {
            replace(n, "NOM", "name");
        }
    }
}
