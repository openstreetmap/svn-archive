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

public class XlsImporter extends AbstractImporter {
	
    public static final ExtensionFileFilter XLS_FILE_FILTER = new ExtensionFileFilter(
            OdConstants.XLS_EXT, OdConstants.XLS_EXT, tr("XLS files") + " (*."+OdConstants.XLS_EXT+")");
    
    public XlsImporter() {
        super(XLS_FILE_FILTER);
    }

	@Override
	protected DataSet parseDataSet(InputStream in, ProgressMonitor instance)
			throws IllegalDataException {
		try {
			return XlsReader.parseDataSet(in, handler, instance);
		} catch (IOException e) {
			throw new IllegalDataException(e);
		}
	}
}
