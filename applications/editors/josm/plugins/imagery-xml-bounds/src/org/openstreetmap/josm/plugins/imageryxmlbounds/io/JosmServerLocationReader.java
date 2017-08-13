// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.imageryxmlbounds.io;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.OsmServerReader;
import org.openstreetmap.josm.io.OsmTransferException;
import org.xml.sax.SAXException;

/**
 * Read XML bounds from JOSM server.
 */
public class JosmServerLocationReader extends OsmServerReader {

    private final String url;

    /**
     * Constructs a new {@code JosmServerLocationReader}.
     * @param url URL to read
     */
    public JosmServerLocationReader(String url) {
        this.url = url;
    }

    @Override
    public DataSet parseOsm(ProgressMonitor progressMonitor)
            throws OsmTransferException {
        try {
            progressMonitor.beginTask(tr("Contacting Server...", 10));
            return new XmlBoundsImporter().parseDataSet(url);
        } catch (IOException | SAXException e) {
            throw new OsmTransferException(e);
        } finally {
            progressMonitor.finishTask();
        }
    }
}
