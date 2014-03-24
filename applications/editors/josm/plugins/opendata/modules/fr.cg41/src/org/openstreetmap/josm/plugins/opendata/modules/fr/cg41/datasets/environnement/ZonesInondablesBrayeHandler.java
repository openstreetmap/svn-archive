// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.cg41.datasets.environnement;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.plugins.opendata.modules.fr.cg41.datasets.Cg41DataSetHandler;

public class ZonesInondablesBrayeHandler extends Cg41DataSetHandler {
	public ZonesInondablesBrayeHandler() {
		super(14624, "Zone-inondable-de-la-Braye-au-1-25000-(partie-Loir-et-Cher)-30383255");
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsMifFilename(filename, "aleas_braye");
	}

	@Override
	public void updateDataSet(DataSet ds) {
		// TODO Auto-generated method stub
	}
}
