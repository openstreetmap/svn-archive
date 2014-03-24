// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pbf.io;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.OsmServerReader;
import org.openstreetmap.josm.io.OsmTransferException;

public class PbfServerReader extends OsmServerReader {

	private String url;
	
	public PbfServerReader(String url) {
		this.url = url;
	}

	@Override
	public DataSet parseOsm(ProgressMonitor progressMonitor)
			throws OsmTransferException {
        try {
            progressMonitor.beginTask(tr("Contacting Server...", 10));
            return new PbfImporter().parseDataSet(url);
        } catch (Exception e) {
            throw new OsmTransferException(e);
        } finally {
            progressMonitor.finishTask();
        }
	}
}
