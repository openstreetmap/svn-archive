// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.lemans.datasets;

import org.openstreetmap.josm.data.osm.DataSet;

public class CodesPostauxHandler extends LeMansDataSetHandler {

	public CodesPostauxHandler() {
		super("F758F369-550EA533-37695DD8-84EB87F5");
		setName("Codes postaux");
		setKmzShpUuid("6449CF5B-550EA533-7E7BB44A-2DB0261B", "644A524A-550EA533-7E7BB44A-9B34A76A");
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsKmzShpFilename(filename, "CODES_POSTAUX") || acceptsZipFilename(filename, "Les codes postaux .*");
	}

	@Override
	public void updateDataSet(DataSet ds) {
		// TODO
	}
}
