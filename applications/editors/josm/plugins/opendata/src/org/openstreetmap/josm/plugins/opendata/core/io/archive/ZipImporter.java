// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.archive;

import java.io.File;
import java.io.InputStream;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.plugins.opendata.core.io.AbstractImporter;

public class ZipImporter extends AbstractImporter {

	public ZipImporter() {
		super(ZIP_FILE_FILTER);
	}

	@Override
	protected DataSet parseDataSet(InputStream in, ProgressMonitor instance)
			throws IllegalDataException {
		try {
			return ZipReader.parseDataSet(in, handler, instance, true);
		} catch (Exception e) {
			throw new IllegalDataException(e);
		}
	}

    @Override
    public boolean acceptFile(File pathname) {
        return super.acceptFile(pathname) && !pathname.getName().endsWith(".osm.zip");
    }
}
