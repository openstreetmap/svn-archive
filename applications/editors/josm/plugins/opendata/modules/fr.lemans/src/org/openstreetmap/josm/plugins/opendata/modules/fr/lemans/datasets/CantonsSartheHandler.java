// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.lemans.datasets;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;

public class CantonsSartheHandler extends LeMansDataSetHandler {

	public CantonsSartheHandler() {
		super("F7D936DF-550EA533-37695DD8-29CFF55B");
		setName("Cantons de la Sarthe");
		setKmzShpUuid("62DFCA8F-550EA533-7E7BB44A-7D1AA2D4", "62E017CA-550EA533-7E7BB44A-23772121");
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsKmzShpFilename(filename, "CANTONS_72") || acceptsZipFilename(filename, "Les cantons de la Sarthe .*");
	}

	@Override
	public void updateDataSet(DataSet ds) {
		for (Node n : ds.getNodes()) {
			replace(n, "NOM", "name");
		}
	}
}
