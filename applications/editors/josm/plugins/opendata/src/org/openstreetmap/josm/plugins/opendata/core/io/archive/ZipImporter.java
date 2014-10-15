// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.archive;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.InputStream;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.io.AbstractImporter;

public class ZipImporter extends AbstractImporter {

    public static final ExtensionFileFilter ZIP_FILE_FILTER = new ExtensionFileFilter(
            OdConstants.ZIP_EXT, OdConstants.ZIP_EXT, tr("Zip Files") + " (*."+OdConstants.ZIP_EXT+")");

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
