// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.lemans.datasets;

import org.openstreetmap.josm.data.osm.DataSet;

public class ConseilsQuartiersHandler extends LeMansDataSetHandler {

	public ConseilsQuartiersHandler() {
		super("F7713FAB-550EA533-37695DD8-A9755461");
		setName("Conseils de quartiers");
		setKmzShpUuid("644BD601-550EA533-7E7BB44A-0B649A3D", "644C87D8-550EA533-7E7BB44A-6284E60D");
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsKmzShpFilename(filename, "CONSEILS_DE_QUARTIER") || acceptsZipFilename(filename, "Les conseils de quartiers .*");
	}

	@Override
	public void updateDataSet(DataSet ds) {
		// TODO
	}
}
