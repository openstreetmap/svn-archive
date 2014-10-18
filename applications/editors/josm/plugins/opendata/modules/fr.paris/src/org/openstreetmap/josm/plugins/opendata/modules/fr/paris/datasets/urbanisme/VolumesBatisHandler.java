// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.paris.datasets.urbanisme;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.plugins.opendata.modules.fr.paris.datasets.ParisDataSetHandler;

public class VolumesBatisHandler extends ParisDataSetHandler {

    public VolumesBatisHandler() {
        super(80);
        setName("Volumes bâtis");
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsShpFilename(filename, "VOL_BATI") || acceptsZipFilename(filename, "VOL_BATI");
    }
    
    @Override
    public void updateDataSet(DataSet ds) {
        // TODO
    }

    @Override
    protected String getDirectLink() {
        return PORTAL+"hn/VOL_BATI.zip";
    }
}
