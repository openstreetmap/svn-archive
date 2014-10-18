// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.be.datagovbe.datasets.culture;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.plugins.opendata.modules.be.datagovbe.datasets.DataGovDataSetHandler;

public class ArchitecturalHeritageHandler extends DataGovDataSetHandler {

    public ArchitecturalHeritageHandler() {
        super("inventar-des-architektonischen-erbes", "inventory-architectural-heritage", "iventaire-heritage-architectural", "inventaris-bouwkundig-erfgoed");
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsKmzFilename(filename, "dibe(_geheel)?") || acceptsZipFilename(filename, "dibegis") || acceptsShpFilename(filename, "dibe_(gehelen|orgels|relicten)");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        // TODO Auto-generated method stub
    }
}
