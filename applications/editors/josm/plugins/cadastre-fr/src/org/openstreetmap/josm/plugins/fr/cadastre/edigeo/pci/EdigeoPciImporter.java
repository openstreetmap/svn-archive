// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo.pci;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.io.importexport.OsmImporter;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.tools.Logging;

/**
 * Importer for French Cadastre - Edigéo files.
 */
public class EdigeoPciImporter extends OsmImporter {

    static final ExtensionFileFilter EDIGEO_FILE_FILTER = new ExtensionFileFilter(
            "thf", "thf", tr("Cadastre Edigeo files") + " (*.thf)");

    protected File file;

    /**
     * Constructs a new {@code EdigeoImporter}.
     */
    public EdigeoPciImporter() {
        super(EDIGEO_FILE_FILTER);
    }

    @Override
    public void importData(File file, ProgressMonitor progressMonitor)
            throws IOException, IllegalDataException {
        if (file != null) {
            this.file = file;
        }
        // Do not call super.importData because Compression.getUncompressedFileInputStream skips the first entry
        try (InputStream in = new FileInputStream(file)) {
            importData(in, file, progressMonitor);
        } catch (FileNotFoundException e) {
            Logging.error(e);
            throw new IOException(tr("File ''{0}'' does not exist.", file.getName()), e);
        }
    }

    @Override
    protected DataSet parseDataSet(InputStream in, ProgressMonitor instance) throws IllegalDataException {
        try {
            return EdigeoPciReader.parseDataSet(in, file, instance);
        } catch (IOException e) {
            throw new IllegalDataException(e);
        }
    }

    @Override
    protected OsmDataLayer createLayer(DataSet dataSet, File associatedFile, String layerName) {
        // FIXME: mapping pci => osm
        //DataSetUpdater.updateDataSet(dataSet, handler, associatedFile);
        return new OsmDataLayer(dataSet, layerName, associatedFile);
    }
}
