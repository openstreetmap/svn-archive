package org.openstreetmap.josm.plugins.opendata.modules.be.bruxelles.datasets.culture;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.be.bruxelles.datasets.BruxellesDataSetHandler;

public class BDHandler extends BruxellesDataSetHandler {

	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsCsvFilename(filename, "textfile");
	}

	@Override
	public void updateDataSet(DataSet ds) {
		for (Node n : ds.getNodes()) {
			n.put("tourism", "artwork");
			replace(n, "Description", "name");
		}
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getCsvSeparator()
	 */
	@Override
	public String getCsvSeparator() {
		return ",";
	}
}
