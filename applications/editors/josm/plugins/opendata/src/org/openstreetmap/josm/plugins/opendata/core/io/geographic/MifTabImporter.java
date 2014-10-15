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

public class MifTabImporter extends AbstractImporter {
    
    public static final ExtensionFileFilter MIF_TAB_FILE_FILTER = new ExtensionFileFilter(
            OdConstants.MIF_EXT+","+OdConstants.TAB_EXT, OdConstants.MIF_EXT, tr("MapInfo files") + " (*."+OdConstants.MIF_EXT+",*."+OdConstants.TAB_EXT+")");

    public MifTabImporter() {
        super(MIF_TAB_FILE_FILTER);
    }

    @Override
    protected DataSet parseDataSet(InputStream in, ProgressMonitor instance)
            throws IllegalDataException {
        try {
            if (file.getName().toLowerCase().endsWith(OdConstants.MIF_EXT)) {
                return MifReader.parseDataSet(in, file, handler, instance);
            } else {
                return TabReader.parseDataSet(in, file, handler, instance);
            }
        } catch (IOException e) {
            throw new IllegalDataException(e);
        }
    }
}
