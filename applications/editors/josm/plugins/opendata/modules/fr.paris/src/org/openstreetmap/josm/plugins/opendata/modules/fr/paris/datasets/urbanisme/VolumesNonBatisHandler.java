// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.paris.datasets.urbanisme;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.plugins.opendata.modules.fr.paris.datasets.ParisDataSetHandler;

public class VolumesNonBatisHandler extends ParisDataSetHandler {

	public VolumesNonBatisHandler() {
		super(106);
		setName("Volumes non bâtis");
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsShpFilename(filename, "VOL_NBATI") || acceptsZipFilename(filename, "VOL_NBATI");
	}
	
	@Override
	public void updateDataSet(DataSet ds) {
		// TODO
	}

	@Override
	protected String getDirectLink() {
		return PORTAL+"hn/VOL_NBATI.zip";
	}
}
