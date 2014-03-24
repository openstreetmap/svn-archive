// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.lemans.datasets;

import org.openstreetmap.josm.data.osm.DataSet;

public class PointsApportVolontaireHandler extends LeMansDataSetHandler {

	public PointsApportVolontaireHandler() {
		super("F8213494-550EA533-37695DD8-28F0B08D");
		setName("Points d'apport volontaire");
		setKmzShpUuid("66972849-550EA533-7E7BB44A-7AB7F366", "66977265-550EA533-7E7BB44A-1859F0AC");
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsKmzShpFilename(filename, "Points d'Apports Volontaires") || acceptsZipFilename(filename, "Les points dapport volontaire .*");
	}

	@Override
	public void updateDataSet(DataSet ds) {
		// TODO
	}
}
