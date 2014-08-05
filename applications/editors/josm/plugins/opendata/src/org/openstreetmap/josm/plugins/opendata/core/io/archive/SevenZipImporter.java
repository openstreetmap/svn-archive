// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.archive;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.InputStream;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.io.AbstractImporter;

public class SevenZipImporter extends AbstractImporter {

    public static final ExtensionFileFilter SEVENZIP_FILE_FILTER = new ExtensionFileFilter(
            OdConstants.SEVENZIP_EXT, OdConstants.SEVENZIP_EXT, tr("7Zip Files") + " (*."+OdConstants.SEVENZIP_EXT+")");

	public SevenZipImporter() {
		super(SEVENZIP_FILE_FILTER);
	}

	@Override
	protected DataSet parseDataSet(InputStream in, ProgressMonitor instance)
			throws IllegalDataException {
		try {
			return SevenZipReader.parseDataSet(in, handler, instance, true);
		} catch (Exception e) {
			throw new IllegalDataException(e);
		}
	}
}
