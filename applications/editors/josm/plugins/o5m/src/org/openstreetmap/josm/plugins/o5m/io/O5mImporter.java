// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.o5m.io;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.io.InputStream;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.io.importexport.OsmImporter;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.CachedFile;

/**
 * OSM Importer for o5m format (*.o5m).
 * @author GerdP
 *
 */
public class O5mImporter extends OsmImporter {
    /**
     * File extension.
     */
    private static final String EXTENSION = "o5m";
    
    public O5mImporter() {
        super(new ExtensionFileFilter(EXTENSION, EXTENSION, 
                tr("OSM Server Files o5m compressed") + " (*."+EXTENSION+")"));
    }

    @Override
    protected DataSet parseDataSet(InputStream in, ProgressMonitor progressMonitor) throws IllegalDataException {
        return O5mReader.parseDataSet(in, progressMonitor);
    }

    protected DataSet parseDataSet(final String source) throws IOException, IllegalDataException {
        try (CachedFile cf = new CachedFile(source)) {
            return parseDataSet(cf.getInputStream(), NullProgressMonitor.INSTANCE);
        }
    }
}
