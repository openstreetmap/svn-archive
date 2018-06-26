// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo.pci;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.io.importexport.OsmImporter;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.CachedFile;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.plugins.fr.cadastre.download.CadastreDownloadData;
import org.openstreetmap.josm.tools.Logging;

/**
 * Importer for French Cadastre - Edigéo files.
 */
public class EdigeoPciImporter extends OsmImporter {

    static final ExtensionFileFilter EDIGEO_FILE_FILTER = new ExtensionFileFilter(
            "thf,tar.bz2", "thf", tr("Cadastre Edigeo files") + " (*.thf, *.tar.bz2)");

    protected File file;
    protected CadastreDownloadData data;

    /**
     * Constructs a new {@code EdigeoImporter}.
     */
    public EdigeoPciImporter() {
        super(EDIGEO_FILE_FILTER);
    }

    @Override
    public void importData(File file, ProgressMonitor progressMonitor)
            throws IOException, IllegalDataException {
        this.file = file;
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
            return EdigeoPciReader.parseDataSet(in, file, data, instance);
        } catch (IOException e) {
            throw new IllegalDataException(e);
        }
    }

    /**
     * Import data from an URL.
     * @param source source URL
     * @param data defines which data has to be downloaded
     * @return imported data set
     * @throws IOException if any I/O error occurs
     * @throws IllegalDataException if an error was found while parsing the data
     */
    public DataSet parseDataSet(final String source, CadastreDownloadData data) throws IOException, IllegalDataException {
        try (CachedFile cf = new CachedFile(source)) {
            this.file = cf.getFile();
            this.data = Objects.requireNonNull(data);
            return parseDataSet(cf.getInputStream(), NullProgressMonitor.INSTANCE);
        }
    }
}
