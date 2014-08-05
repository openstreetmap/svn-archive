// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.tabular;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.io.InputStream;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.io.AbstractImporter;

public class OdsImporter extends AbstractImporter {
	
    public static final ExtensionFileFilter ODS_FILE_FILTER = new ExtensionFileFilter(
            OdConstants.ODS_EXT, OdConstants.ODS_EXT, tr("ODS files") + " (*."+OdConstants.ODS_EXT+")");
    
    public OdsImporter() {
        super(ODS_FILE_FILTER);
    }

	@Override
	protected DataSet parseDataSet(InputStream in, ProgressMonitor instance)
			throws IllegalDataException {
		try {
			return OdsReader.parseDataSet(in, handler, instance);
		} catch (IOException e) {
			throw new IllegalDataException(e);
		}
	}
}
