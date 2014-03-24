// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

import java.io.InputStream;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.plugins.opendata.core.io.AbstractImporter;

public class KmlKmzImporter extends AbstractImporter {
	
    public KmlKmzImporter() {
        super(KML_KMZ_FILE_FILTER);
    }

	@Override
	protected DataSet parseDataSet(InputStream in, ProgressMonitor instance)
			throws IllegalDataException {
		try {
			if (file.getName().toLowerCase().endsWith(KML_EXT)) {
				return KmlReader.parseDataSet(in, instance);
			} else {
				return KmzReader.parseDataSet(in, instance);
			}
		} catch (Exception e) {
			throw new IllegalDataException(e);
		}
	}
}
