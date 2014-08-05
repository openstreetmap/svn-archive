// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.io.InputStream;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.io.AbstractImporter;

public class ShpImporter extends AbstractImporter {
	
    public static final ExtensionFileFilter SHP_FILE_FILTER = new ExtensionFileFilter(
            OdConstants.SHP_EXT, OdConstants.SHP_EXT, tr("Shapefiles") + " (*."+OdConstants.SHP_EXT+")");

    public ShpImporter() {
        super(SHP_FILE_FILTER);
    }

	@Override
	protected DataSet parseDataSet(InputStream in, ProgressMonitor instance)
			throws IllegalDataException {
		try {
			return ShpReader.parseDataSet(in, file, handler, instance);
		} catch (IOException e) {
			throw new IllegalDataException(e);
		}
	}
}
