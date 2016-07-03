// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.InputStream;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.io.AbstractImporter;

public class KmlKmzImporter extends AbstractImporter {

    public static final ExtensionFileFilter KML_KMZ_FILE_FILTER = new ExtensionFileFilter(
            OdConstants.KML_EXT+","+OdConstants.KMZ_EXT, OdConstants.KMZ_EXT, tr("KML/KMZ files") +
            " (*."+OdConstants.KML_EXT+",*."+OdConstants.KMZ_EXT+")");

    public KmlKmzImporter() {
        super(KML_KMZ_FILE_FILTER);
    }

    @Override
    protected DataSet parseDataSet(InputStream in, ProgressMonitor instance)
            throws IllegalDataException {
        try {
            if (file.getName().toLowerCase().endsWith(OdConstants.KML_EXT)) {
                return KmlReader.parseDataSet(in, instance);
            } else {
                return KmzReader.parseDataSet(in, instance);
            }
        } catch (Exception e) {
            throw new IllegalDataException(e);
        }
    }
}
