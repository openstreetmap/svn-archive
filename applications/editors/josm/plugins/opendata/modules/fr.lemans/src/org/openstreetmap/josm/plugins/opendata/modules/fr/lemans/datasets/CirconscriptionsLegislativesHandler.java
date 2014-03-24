// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.lemans.datasets;

import org.openstreetmap.josm.data.osm.DataSet;

public class CirconscriptionsLegislativesHandler extends LeMansDataSetHandler {

	public CirconscriptionsLegislativesHandler() {
		super("F7D06B39-550EA533-37695DD8-95CA6762");
		setName("Circonscriptions législatives");
		setKmzShpUuid("64468222-550EA533-7E7BB44A-39A6B127", "6447510C-550EA533-7E7BB44A-9971DB1A");
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsKmzShpFilename(filename, "CIRCONSCRIPTIONS") || acceptsZipFilename(filename, "Les circonscriptions .*");
	}

	@Override
	public void updateDataSet(DataSet ds) {
		// TODO
	}
}
