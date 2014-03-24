// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

import java.io.IOException;
import java.io.InputStream;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.plugins.opendata.core.io.AbstractImporter;

public class MifTabImporter extends AbstractImporter {
	
    public MifTabImporter() {
        super(MIF_TAB_FILE_FILTER);
    }

	@Override
	protected DataSet parseDataSet(InputStream in, ProgressMonitor instance)
			throws IllegalDataException {
		try {
			if (file.getName().toLowerCase().endsWith(MIF_EXT)) {
				return MifReader.parseDataSet(in, file, handler, instance);
			} else {
				return TabReader.parseDataSet(in, file, handler, instance);
			}
		} catch (IOException e) {
			throw new IllegalDataException(e);
		}
	}
}
