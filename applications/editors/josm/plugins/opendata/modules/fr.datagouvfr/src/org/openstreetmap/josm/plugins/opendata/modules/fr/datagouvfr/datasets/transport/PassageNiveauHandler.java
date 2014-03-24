// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.transport;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.DataGouvDataSetHandler;

public class PassageNiveauHandler extends DataGouvDataSetHandler {

	public PassageNiveauHandler() {
		super("Passages-à-niveau-30383135");
		setName("Passages à niveau");
		setDownloadFileName("passage_a_niveau.csv");
		getCsvHandler().setCharset(ISO8859_15);
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsCsvFilename(filename, "passage_a_niveau(\\.csv-fr)?");
	}

	@Override
	public void updateDataSet(DataSet ds) {
		for (Node n : ds.getNodes()) {
			n.put("railway", "level_crossing");
		}
	}
}
