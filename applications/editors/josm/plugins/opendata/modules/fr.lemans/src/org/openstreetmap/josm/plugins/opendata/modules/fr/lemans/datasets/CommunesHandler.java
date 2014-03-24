// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.lemans.datasets;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

public class CommunesHandler extends LeMansDataSetHandler {

	public CommunesHandler() {
		super("F7B756B1-550EA533-37695DD8-FE094AE7");
		setName("Communes");
		setKmzShpUuid("64527774-550EA533-7E7BB44A-FD17EEF0", "6452D942-550EA533-7E7BB44A-C184920B");
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsKmzShpFilename(filename, "LIMITES_DE_COMMUNES") || acceptsZipFilename(filename, "Les limites de communes .*");
	}

	@Override
	public void updateDataSet(DataSet ds) {
		for (OsmPrimitive p : ds.allPrimitives()) {
			if (p.hasKey("CODCOM")) {
				p.put("type", "boundary");
				p.put("boundary", "administrative");
				p.put("admin_level", "8");
				replace(p, "COMMUNE", "name");
				p.remove("COMMUNE_2");
				replace(p, "ID", "ref:INSEE");
				p.remove("CODCOM");
			}
		}
	}
}
