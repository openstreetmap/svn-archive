// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.download;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Objects;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.OsmServerReader;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.plugins.fr.cadastre.edigeo.pci.EdigeoPciImporter;

/**
 * This DataReader reads Cadastre files directly from an URL.
 */
public class CadastreServerReader extends OsmServerReader {

    private final String url;
    private final CadastreDownloadData data;

    /**
     * Constructs a new {@code CadastreServerReader}.
     * @param url source URL
     * @param data defines which data has to be downloaded
     */
    public CadastreServerReader(String url, CadastreDownloadData data) {
        this.url = Objects.requireNonNull(url);
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public DataSet parseOsm(ProgressMonitor progressMonitor) throws OsmTransferException {
        try {
            progressMonitor.beginTask(tr("Contacting Server...", 10));
            return new EdigeoPciImporter().parseDataSet(url, data);
        } catch (Exception e) {
            throw new OsmTransferException(e);
        } finally {
            progressMonitor.finishTask();
        }
    }
}
