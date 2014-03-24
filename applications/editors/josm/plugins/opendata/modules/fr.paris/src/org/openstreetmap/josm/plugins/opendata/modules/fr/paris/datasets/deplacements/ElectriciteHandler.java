// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.paris.datasets.deplacements;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.plugins.opendata.modules.fr.paris.datasets.ParisDataSetHandler;

public class ElectriciteHandler extends ParisDataSetHandler {

	public ElectriciteHandler() {
		super(95);
		setName("Électricité");
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsShpFilename(filename, "electricite") || acceptsZipFilename(filename, "electricite");
	}
	
	@Override
	public void updateDataSet(DataSet ds) {
		// TODO
	}

	@Override
	protected String getDirectLink() {
		return PORTAL+"hn/electricite.zip";
	}
}
